/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.CasePolicy;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceHierarchyFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IllegalOperationException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.RuntimeEnvironmentInfo;
import org.eclipse.stardust.ui.web.bcc.ProcessSearchProvider;
import org.eclipse.stardust.ui.web.rest.common.ProcessSearchParameterConstants;
import org.eclipse.stardust.ui.web.rest.dto.ActivityDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityFilterAttributesDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.FilterAttributesDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessTableFilterDTO;
import org.eclipse.stardust.ui.web.rest.dto.WorklistFilterDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.springframework.stereotype.Component;

/**
 * @author Aditya.Gaikwad
 *
 */
@Component
public class ProcessActivityUtils
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private org.eclipse.stardust.ui.web.rest.component.util.ProcessInstanceUtils processInstanceUtilsREST;

   private static enum SEARCH_OPTION {
      PROCESSES, ACTIVITIES
   }

   public final static int PROCESS_INSTANCE_STATE_ALIVE = 1;

   public final static int PROCESS_INSTANCE_STATE_COMPLETED = 2;

   public final static int PROCESS_INSTANCE_STATE_ABORTED = 3;

   public final static int PROCESS_INSTANCE_STATE_INTERRUPTED = 4;
   
   public final static int PROCESS_INSTANCE_STATE_HALTED = 5;

   public final static int PROCESS_INSTANCE_STATE_ALL = 6;

   public static final int ALL_PRIORITIES = -9999;

   private FilterAttributesDTO filterAttributesDTO;
   private List<DataMappingWrapper> descriptorItems = new ArrayList<DataMappingWrapper>();
   private DataPath[] commonDescriptors;

   public final static int ACTIVITY_INSTANCE_STATE_ALIVE = 3;

   public final static int ACTIVITY_INSTANCE_STATE_ALL = 10;

   private ActivityFilterAttributesDTO activityFilterAttributesDTO;

   /**
    * 
    * @param options
    * @param postData
    * @param processSearchAttributes
    * @param commonDescriptors
    * @return
    */
   public QueryResult<ProcessInstance> performProcessSearch(DataTableOptionsDTO options,
         String postData, ProcessSearchCriteriaDTO processSearchAttributes, List<DescriptorColumnDTO> availableDescriptors)
   {
      
      // Validate start time and end time
      if (!org.eclipse.stardust.ui.web.viewscommon.utils.DateUtils.validateDateRange(processSearchAttributes.procStartFrom , processSearchAttributes.procStartTo ))
      {
         return null;
      }

      if (!org.eclipse.stardust.ui.web.viewscommon.utils.DateUtils.validateDateRange(processSearchAttributes.procEndFrom, processSearchAttributes.procEndTo))
      {
         return null;
      }
      
      
      if (!isValidOID(processSearchAttributes.processSrchRootProcessOID))
      {
         return null;
      }
      
      if (!isValidOID(processSearchAttributes.processSrchProcessOID ))
      {
         return null;
      }
      
      // set case attributes
      if (processSearchAttributes.procSearchHierarchySelected.equals(ProcessSearchParameterConstants.HIERARCHY_CASE)
            && processSearchAttributes.processSrchCaseOwner != null)
      {
         try
         {
            filterAttributesDTO.setUser(serviceFactoryUtils.getUserService().getUser(
                  processSearchAttributes.processSrchCaseOwner));
         }
         catch (ObjectNotFoundException e)
         {
            e.printStackTrace();
         }
         catch (IllegalOperationException e)
         {
            e.printStackTrace();
         }
      }

      List<ProcessDefinitionDTO> selectedProcs = processSearchAttributes.procSrchProcessSelected;
      List<String> procDefIDs = new ArrayList<String>(selectedProcs.size());
      for (ProcessDefinitionDTO processDefinitionDTO : selectedProcs)
      {
         procDefIDs.add(processDefinitionDTO.id);
      }
      List<ProcessDefinition> processDefinitions = ProcessDefinitionUtils.getProcessDefinitions(procDefIDs);
      commonDescriptors = ProcessDefinitionUtils.getCommonDescriptors(processDefinitions, true);

      Query query = null;
      
      filterAttributesDTO = new FilterAttributesDTO();

      // Set hiearchy
      setHierarchyValue(processSearchAttributes.procSearchHierarchySelected);

      refreshDescriptors(processSearchAttributes.filterObject, filterAttributesDTO, commonDescriptors);

      prePopulateProcessCriteria(processSearchAttributes);
      query = createQuery(processSearchAttributes);
      
      // For Criteria Descriptors
      ProcessTableFilterDTO descFilterDTO = new ProcessTableFilterDTO();
      ProcessInstanceUtils.populateDescriptorFilters(descFilterDTO, processSearchAttributes.descriptors, availableDescriptors);
      ProcessInstanceUtils.addDescriptorFilters(query, descFilterDTO);
      
      // For Filter Descriptors of table
      ProcessInstanceUtils.populatePostData(options, postData, availableDescriptors);

      if (query instanceof ProcessInstanceQuery)
      {
         return getAllProcessInstances(options, (ProcessInstanceQuery) query);
      }
      else
      {
         QueryResult<ActivityInstance> aiResult = allActivityInstances(options, (ActivityInstanceQuery) query);
         List<ProcessInstance> result = new ArrayList<ProcessInstance>();
         for (ActivityInstance ai : aiResult)
         {
            result.add(ai.getProcessInstance());
         }
         return new RawQueryResult<ProcessInstance>(result, null, aiResult.hasMore(), Long.valueOf(aiResult
               .getTotalCount()));
      }

   }

   /**
    * 
    * @param options
    * @param postData
    * @param processSearchAttributes
    * @param availableDescriptors
    * @return
    */
   public QueryResult<ActivityInstance> performActivitySearch(DataTableOptionsDTO options,
         String postData, ProcessSearchCriteriaDTO processSearchAttributes, List<DescriptorColumnDTO> availableDescriptors)
   {
      // Validate start time and modify time
      if (!org.eclipse.stardust.ui.web.viewscommon.utils.DateUtils.validateDateRange(processSearchAttributes.actStartFrom , processSearchAttributes.actStartTo))
      {
         return null;
      }

      if (!org.eclipse.stardust.ui.web.viewscommon.utils.DateUtils.validateDateRange(processSearchAttributes.actModifyFrom, processSearchAttributes.actModifyTo))
      {
         return null;
      }

      if (!isValidOID(processSearchAttributes.activitySrchActivityOID))
      {
         return null;
      }

      activityFilterAttributesDTO = new ActivityFilterAttributesDTO();

      prePopulateActivityCriteria(processSearchAttributes);
      Query query = activityFilterAttributesDTO.buildQuery();

      FilterTerm filter = query.getFilter().addOrTerm();

      ProcessDefinitionDTO processDefinitionDTO = processSearchAttributes.procSrchProcessSelected.get(0);
      List<ActivityDTO> activityDTOs = processDefinitionDTO.activities;

      if (CollectionUtils.isNotEmpty(activityDTOs))
      {
         for (ActivityDTO activity : activityDTOs)
         {
            filter.add(ActivityInstanceQuery.ACTIVITY_OID.isEqual(activity.runtimeElementOid));
         }
      }
      else
      {
         // For Case PI search, selectedActivities will be null
         if (processSearchAttributes.procSearchHierarchySelected.equals(ProcessSearchParameterConstants.HIERARCHY_CASE))
         {
            filter.add(ActivityFilter.forProcess(PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID,
                  PredefinedConstants.CASE_PROCESS_ID, false));
         }
         else
         {
            // Adding a dummy filter which will guarantee return of no activities
            // As an activity id will never be -1
            filter.add(ActivityFilter.forAnyProcess("-1"));
         }
      }

      applyFilter(processSearchAttributes, query);
      
      // For Criteria Descriptors
      WorklistFilterDTO descFilterDTO = new WorklistFilterDTO();
      ActivityTableUtils.populateDescriptorFilters(descFilterDTO, processSearchAttributes.descriptors, availableDescriptors);
      ActivityTableUtils.addDescriptorFilters(query, descFilterDTO);
      
      // For Filter Descriptors of table
      ActivityTableUtils.populatePostData(options, postData, availableDescriptors);

      return allActivityInstances(options, (ActivityInstanceQuery) query);
   }

   public Query createQuery(ProcessSearchCriteriaDTO processSearchAttributes)
   {

      Query query = buildQuery(processSearchAttributes);

      FilterAndTerm filter = query.getFilter().addAndTerm();

      if (filterAttributesDTO.isCaseOnlySearch())
      {
         filter = DescriptorFilterUtils.createCaseDescriptors(descriptorItems, filter);
      }
      else
      {
         if (CollectionUtils.isNotEmpty(processSearchAttributes.procSrchProcessSelected))
         {
            List<ProcessDefinitionDTO> selectedProcesses = processSearchAttributes.procSrchProcessSelected;
            FilterTerm or = filter.addOrTerm();
            for (Iterator<ProcessDefinitionDTO> iterator = selectedProcesses.iterator(); iterator.hasNext();)
            {
               ProcessDefinitionDTO processDefinitionDTO = iterator.next();
               or.add(new ProcessDefinitionFilter(processDefinitionDTO.id, false));
            }
            ProcessDefinitionDTO processDefDetails = selectedProcesses.get(0);
            if (PredefinedConstants.CASE_PROCESS_ID.equals(processDefDetails.id) & selectedProcesses.size() == 1)
            {
               filter = DescriptorFilterUtils.createCaseDescriptors(descriptorItems, filter);
            }
            else
            {
               DescriptorFilterUtils.evaluateAndApplyFilters(query, descriptorItems, commonDescriptors);
               if (filterAttributesDTO.isIncludeCaseSearch())
               {
                  query.setPolicy(CasePolicy.INCLUDE_CASES);
               }
            }
         }
         else
         {
            filter.and(ProcessInstanceQuery.OID.isEqual(0));
         }
      }
      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      return query;
   }
   
   public String getLastArchivedEntry()
   {
      String auditTrailOldestPI;
      RuntimeEnvironmentInfo runtimeEnvironmentInfo = serviceFactoryUtils.getQueryService().getRuntimeEnvironmentInfo();
      Long lastArchivingTime = runtimeEnvironmentInfo.getLastArchivingTime();
      if (lastArchivingTime == null)
      {
         auditTrailOldestPI = "";
      }
      else
      {
         Date date = new Date(lastArchivingTime);
         auditTrailOldestPI = DateUtils.formatDateTime(date);
      }
      return auditTrailOldestPI;
   }

   /**
    * @param query
    * @return QueryResult
    */
   private QueryResult<ProcessInstance> getAllProcessInstances(DataTableOptionsDTO options, ProcessInstanceQuery query)
   {

      QueryResult< ? extends ProcessInstance> queryResult = processInstanceUtilsREST
            .getProcessInstances(query, options);
      return (QueryResult<ProcessInstance>) queryResult;
   }

   /**
    * 
    * @param query
    * @return
    */
   private QueryResult<ActivityInstance> allActivityInstances(DataTableOptionsDTO options, ActivityInstanceQuery query)
   {
      QueryResult<ActivityInstance> result = null;
      ActivityTableUtils.addDescriptorPolicy(options, query);

      ActivityTableUtils.addSortCriteria(query, options);

      ActivityTableUtils.addFilterCriteria(query, options);

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      query.setPolicy(subsetPolicy);

      result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);
      return result;
   }

   /**
    * @return ProcessInstanceQuery
    */
   protected Query buildQuery(ProcessSearchCriteriaDTO processSearchAttributes)
   {

      Query query = null;

      // Case search by ActivityInstanceQuery
      if (filterAttributesDTO.isCaseOnlySearch() && null != filterAttributesDTO.getUser())
      {
         query = getActivityQueryByProcessState(filterAttributesDTO.getState());
         FilterAndTerm filter = query.getFilter().addAndTerm();
         if (null != filterAttributesDTO.getOid())
         {
            filter.and(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(filterAttributesDTO.getOid()));
         }
         filter.add(new PerformingUserFilter(filterAttributesDTO.getUser().getOID()));

         filter.add(ActivityFilter.forProcess(PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID,
               PredefinedConstants.CASE_PROCESS_ID, false));

         if (ProcessSearchProvider.ALL_PRIORITIES != filterAttributesDTO.getPriority())
            ;
         {
            filter.add(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(filterAttributesDTO.getPriority()));
         }

         if (null != filterAttributesDTO.getStartedFrom() && null != filterAttributesDTO.getStartedTo())
         {
            filter.and(ActivityInstanceQuery.START_TIME.between(filterAttributesDTO.getStartedFrom().getTime(),
                  filterAttributesDTO.getStartedTo().getTime()));
         }
         else if (null != filterAttributesDTO.getStartedTo())
         {
            filter.and(ActivityInstanceQuery.START_TIME.lessOrEqual(filterAttributesDTO.getStartedTo().getTime()));
         }
         else if (null != filterAttributesDTO.getStartedFrom())
         {
            filter.and(ActivityInstanceQuery.START_TIME.greaterOrEqual(filterAttributesDTO.getStartedFrom().getTime()));
         }

         return query;
      }

      // else create ProcessInstanceQuery

      else if (filterAttributesDTO.getState() == PROCESS_INSTANCE_STATE_ALIVE)
      {
         query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
               ProcessInstanceState.Active, ProcessInstanceState.Interrupted, ProcessInstanceState.Aborting});
      }
      else if (filterAttributesDTO.getState() == PROCESS_INSTANCE_STATE_COMPLETED)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Completed);
      }
      else if (filterAttributesDTO.getState() == PROCESS_INSTANCE_STATE_ABORTED)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted);
      }
      else if (filterAttributesDTO.getState() == PROCESS_INSTANCE_STATE_INTERRUPTED)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Interrupted);
      }
      else if (filterAttributesDTO.getState() == PROCESS_INSTANCE_STATE_HALTED)
      {
         query = ProcessInstanceQuery.findInState(ProcessInstanceState.Halted);
      }
      else
      {
         query = ProcessInstanceQuery.findInState(new ProcessInstanceState[] {
               ProcessInstanceState.Active, ProcessInstanceState.Completed, ProcessInstanceState.Interrupted,
               ProcessInstanceState.Aborted, ProcessInstanceState.Aborting, ProcessInstanceState.Halting,
               ProcessInstanceState.Halted});
      }
      FilterAndTerm filter = query.getFilter().addAndTerm();

      if (null != filterAttributesDTO.getStartedFrom() && null != filterAttributesDTO.getStartedTo())
      {
         filter.and(ProcessInstanceQuery.START_TIME.between(filterAttributesDTO.getStartedFrom().getTime(),
               filterAttributesDTO.getStartedTo().getTime()));
      }
      else if (filterAttributesDTO.getStartedTo() != null)
      {
         filter.and(ProcessInstanceQuery.START_TIME.lessOrEqual(filterAttributesDTO.getStartedTo().getTime()));
      }
      else if (filterAttributesDTO.getStartedFrom() != null)
      {
         filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(filterAttributesDTO.getStartedFrom().getTime()));
      }

      if (null != filterAttributesDTO.getEndTimeFrom() && null != filterAttributesDTO.getEndTimeTo())
      {
         filter.and(ProcessInstanceQuery.TERMINATION_TIME.between(filterAttributesDTO.getEndTimeFrom().getTime(),
               filterAttributesDTO.getEndTimeTo().getTime()));
      }
      else if (filterAttributesDTO.getEndTimeTo() != null)
      {
         filter.and(ProcessInstanceQuery.TERMINATION_TIME.notEqual(0));
         filter.and(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(filterAttributesDTO.getEndTimeTo().getTime()));
      }
      else if (filterAttributesDTO.getEndTimeFrom() != null)
      {
         filter.and(ProcessInstanceQuery.TERMINATION_TIME
               .greaterOrEqual(filterAttributesDTO.getEndTimeFrom().getTime()));
      }

      if (filterAttributesDTO.getOid() != null)
      {
         filter.and(ProcessInstanceQuery.OID.isEqual(filterAttributesDTO.getOid().longValue()));
      }
      if (filterAttributesDTO.getRootOid() != null)
      {
         filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID
               .isEqual(filterAttributesDTO.getRootOid().longValue()));
      }
      if (ALL_PRIORITIES != filterAttributesDTO.getPriority())
      {
         filter.and(ProcessInstanceQuery.PRIORITY.isEqual(filterAttributesDTO.getPriority()));
      }

      if (!filterAttributesDTO.isIncludeCaseSearch())
      {
         FilterTerm orFilter = filter.addOrTerm();
         ProcessDefinition caseProcessDefination = ModelCache.findModelCache().getCaseProcessDefination();
         orFilter
               .add(ProcessInstanceQuery.PROCESS_DEFINITION_OID.notEqual(caseProcessDefination.getRuntimeElementOID()));
      }

      if (filterAttributesDTO.isIncludeRootProcess())
      {
         filter.and(ProcessInstanceHierarchyFilter.ROOT_PROCESS);
      }
      if (filterAttributesDTO.isCaseOnlySearch())
      {
         String qualifiedGroupId = "{" + PredefinedConstants.PREDEFINED_MODEL_ID + "}"
               + PredefinedConstants.CASE_PROCESS_ID;
         filter.add(new ProcessDefinitionFilter(qualifiedGroupId, false));
      }
      return query;
   }

   private void setHierarchyValue(String hierarchy)
   {
      if (ProcessSearchParameterConstants.HIERARCHY_CASE.equals(hierarchy))
      {
         filterAttributesDTO.setCaseOnlySearch(true);
         filterAttributesDTO.setIncludeCaseSearch(true);
         filterAttributesDTO.setIncludeRootProcess(false);
      }
      else if (ProcessSearchParameterConstants.HIERARCHY_ROOT_PROCESS.equals(hierarchy))
      {
         filterAttributesDTO.setCaseOnlySearch(false);
         filterAttributesDTO.setIncludeCaseSearch(false);
         filterAttributesDTO.setIncludeRootProcess(true);
      }
      else
      {
         if (ProcessSearchParameterConstants.HIERARCHY_PROCESS_AND_CASE.equals(hierarchy))
         {
            filterAttributesDTO.setIncludeCaseSearch(true);
         }
         else if (ProcessSearchParameterConstants.HIERARCHY_PROCESS.equals(hierarchy))
         {
            filterAttributesDTO.setIncludeCaseSearch(false);
         }
         filterAttributesDTO.setCaseOnlySearch(false);
         filterAttributesDTO.setIncludeRootProcess(false);
      }
   }

   /**
    * @param params
    */
   private void prePopulateProcessCriteria(ProcessSearchCriteriaDTO processSearchCriteriaDTO)
   {
      if (SEARCH_OPTION.PROCESSES.ordinal() == 0)
      {
         if (processSearchCriteriaDTO.procStartFrom != null)
            filterAttributesDTO.setStartedFrom(processSearchCriteriaDTO.procStartFrom);

         if (processSearchCriteriaDTO.procStartTo != null)
            filterAttributesDTO.setStartedTo(processSearchCriteriaDTO.procStartTo);

         if (processSearchCriteriaDTO.procEndFrom != null)
            filterAttributesDTO.setEndTimeFrom(processSearchCriteriaDTO.procEndFrom);

         if (processSearchCriteriaDTO.procEndTo != null)
            filterAttributesDTO.setEndTimeTo(processSearchCriteriaDTO.procEndTo);

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.procSrchStateSelected))
            filterAttributesDTO.setState(Integer.parseInt(processSearchCriteriaDTO.procSrchStateSelected));

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.processSrchPrioritySelected))
            filterAttributesDTO.setPriority(Integer.parseInt(processSearchCriteriaDTO.processSrchPrioritySelected));

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.processSrchRootProcessOID))
            filterAttributesDTO.setRootOid(Long.parseLong(processSearchCriteriaDTO.processSrchRootProcessOID));

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.processSrchProcessOID))
            filterAttributesDTO.setOid(Long.parseLong(processSearchCriteriaDTO.processSrchProcessOID));

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.procSearchHierarchySelected))
            setHierarchyValue(processSearchCriteriaDTO.procSearchHierarchySelected);
      }
   }

   /**
    * @param params
    */
   private void prePopulateActivityCriteria(ProcessSearchCriteriaDTO processSearchCriteriaDTO)
   {
      // preSearch = false;

      if (processSearchCriteriaDTO.filterObject == 1)
      {
         if (processSearchCriteriaDTO.actStartFrom != null)
            activityFilterAttributesDTO.setStartedFrom(processSearchCriteriaDTO.actStartFrom);

         if (processSearchCriteriaDTO.actStartTo != null)
            activityFilterAttributesDTO.setStartedTo(processSearchCriteriaDTO.actStartTo);

         if (processSearchCriteriaDTO.actModifyFrom != null)
            activityFilterAttributesDTO.setModifyTimeFrom(processSearchCriteriaDTO.actModifyFrom);

         if (processSearchCriteriaDTO.actModifyTo != null)
            activityFilterAttributesDTO.setModifyTimeTo(processSearchCriteriaDTO.actModifyTo);

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.activitySrchStateSelected))
            activityFilterAttributesDTO.setState(Integer.parseInt(processSearchCriteriaDTO.activitySrchStateSelected));

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.processSrchPrioritySelected))
            activityFilterAttributesDTO.setPriority(Integer
                  .parseInt(processSearchCriteriaDTO.processSrchPrioritySelected));

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.activitySrchCriticalitySelected))
            activityFilterAttributesDTO.setCriticality(processSearchCriteriaDTO.activitySrchCriticalitySelected);

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.activitySrchActivityOID))
            activityFilterAttributesDTO
                  .setActivityOID(Long.parseLong(processSearchCriteriaDTO.activitySrchActivityOID));

         if (StringUtils.isNotEmpty(processSearchCriteriaDTO.activitySrchPerformer))
         {
            try
            {
               activityFilterAttributesDTO.setUser(serviceFactoryUtils.getUserService().getUser(
                     processSearchCriteriaDTO.activitySrchPerformer));
            }
            catch (ObjectNotFoundException e)
            {
               e.printStackTrace();
            }
            catch (IllegalOperationException e)
            {
               e.printStackTrace();
            }
         }
      }
   }

   /**
    * @param query
    */
   private void applyFilter(ProcessSearchCriteriaDTO processSearchAttributes, Query query)
   {
      if (CollectionUtils.isNotEmpty(processSearchAttributes.procSrchProcessSelected))
      {
         // TODO: Review following code later
         boolean applyCaseFilter = false;
         List<ProcessDefinitionDTO> selectedProcesses = processSearchAttributes.procSrchProcessSelected;
         ProcessDefinitionDTO processDefDetails = selectedProcesses.get(0);
         if (PredefinedConstants.CASE_PROCESS_ID.equals(processDefDetails.id) & selectedProcesses.size() == 1)
         {
            FilterAndTerm filter = query.getFilter().addAndTerm();
            applyCaseFilter = true;
            filter = DescriptorFilterUtils.createCaseDescriptors(descriptorItems, filter);
         }
      }
   }

   /**
    * 
    * @param state
    * @return
    */
   private ActivityInstanceQuery getActivityQueryByProcessState(int state)
   {
      ActivityInstanceQuery query = null;
      switch (state)
      {
      case PROCESS_INSTANCE_STATE_ALIVE:
         query = ActivityInstanceQuery.findAlive();
         break;
      case PROCESS_INSTANCE_STATE_COMPLETED:
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Completed);
         break;
      case PROCESS_INSTANCE_STATE_ABORTED:
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Aborted);
         break;
      case PROCESS_INSTANCE_STATE_INTERRUPTED:
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Interrupted);
         break;
      case PROCESS_INSTANCE_STATE_HALTED:
         query = ActivityInstanceQuery.findInState(ActivityInstanceState.Halted);
         break;
      default:
         query = ActivityInstanceQuery.findAll();
         break;

      }
      return query;
   }

   /**
    * Evaluate the descriptors to be displayed
    */
   private void refreshDescriptors(Long selectedSearchOption, FilterAttributesDTO filterAttributesDTO,
         DataPath[] commonDescriptors)
   {
      descriptorItems.clear();
      boolean removedCaseProcess = false;
      if (CollectionUtils.isNotEmpty(commonDescriptors))
      {
         GenericDataMapping mapping;
         DataMappingWrapper dmWrapper;
         boolean includeCaseDescriptor = false;

         for (int i = 0; i < commonDescriptors.length; i++)
         {
            if (!includeCaseDescriptor)
            {
               includeCaseDescriptor = checkCaseProcess(commonDescriptors[i]);
            }
            mapping = new GenericDataMapping(commonDescriptors[i]);
            dmWrapper = new DataMappingWrapper(mapping, null, false);
            descriptorItems.add(dmWrapper);
            dmWrapper.setDefaultValue(null);
         }

         // Add case descriptor to filter criteria if 'ALL Processes' search is set or
         // Hierarchy is Case RootPI add Case descriptors
         if (!includeCaseDescriptor
               & ((SEARCH_OPTION.PROCESSES.ordinal() == 0 & filterAttributesDTO.isCaseOnlySearch()) || removedCaseProcess))
         {
            // init case descriptors
            List<DataPath> caseDataPath = null;
            ProcessDefinition caseProcessDefinition = org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils
                  .getProcessDefinition(PredefinedConstants.CASE_PROCESS_ID);

            if (caseProcessDefinition != null)
            {
               caseDataPath = caseProcessDefinition.getAllDataPaths();
            }

            List<DataMappingWrapper> caseDescriptorItems = fetchCaseDescriptors(caseDataPath);
            descriptorItems.addAll(caseDescriptorItems);
         }
      }
   }

   /**
    * If selected Process is Case Process, return true
    * 
    * @param dp
    * @return
    */
   private boolean checkCaseProcess(DataPath dp)
   {
      if (dp.getData().equals(PredefinedConstants.CASE_DATA_ID))
      {
         return true;
      }
      else
         return false;
   }

   /**
    * 
    */
   private List<DataMappingWrapper> fetchCaseDescriptors(List<DataPath> caseDataPath)
   {
      List<DataMappingWrapper> caseDescriptorItems = new ArrayList<DataMappingWrapper>();
      GenericDataMapping mapping;
      DataMappingWrapper dmWrapper;
      for (DataPath dp : caseDataPath)
      {
         if (Direction.IN.equals(dp.getDirection()) && dp.isDescriptor()
               && (DescriptorFilterUtils.isDataFilterable(dp)))
         {
            mapping = new GenericDataMapping(dp);
            dmWrapper = new DataMappingWrapper(mapping, null, false);
            caseDescriptorItems.add(dmWrapper);
            dmWrapper.setDefaultValue(null);
         }
      }
      return caseDescriptorItems;
   }
   
   /**
    * 
    * @param dp
    * @return
    */
   private boolean isValidOID(String oid)
   {
      if (oid != null && !oid.isEmpty())
      {
         try
         {
            Integer.parseInt(oid);
         }
         catch (NumberFormatException e1)
         {
            e1.printStackTrace();
            return false;
         }
      }
      return true;
   }

}