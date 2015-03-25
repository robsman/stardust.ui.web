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
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.FilterDTO.BooleanDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.RangeDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.TextSearchDTO;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessTableFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.NumberRange;
import org.springframework.stereotype.Component;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component("ProcessInstanceUtilsREST")
public class ProcessInstanceUtils
{

   private static final Logger trace = LogManager.getLogger(ProcessInstanceUtils.class);
   
   private static final String COL_PROCESS_NAME = "processName";

   private static final String COL_PROCESS_INSTANCE_OID = "oid";
   
   private static final String PROCESS_INSTANCE_ROOT_OID = "processInstanceRootOID";

   private static final String COL_START_TIME = "startTime";

   private static final String COL_END_TIME = "endTime";

   private static final String COL_STATUS = "status";

   private static final String COL_PRIOIRTY = "priority";
   
   private static final String STARTING_USER = "startingUser";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ModelUtils modelUtils;

   @Resource
   private ProcessDefinitionUtils processDefinitionUtils;
   
   @Resource
   private ProcessDefinitionUtils processDefUtils;

   /**
    * @param oid
    * @return
    */
   public ProcessInstance getProcessInstance(long oid)
   {
      ProcessInstance pi = null;
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.where(ProcessInstanceQuery.OID.isEqual(oid));
      ProcessInstances pis = serviceFactoryUtils.getQueryService()
            .getAllProcessInstances(query);

      if (!pis.isEmpty())
      {
         pi = pis.get(0);
      }

      return pi;
   }

   /**
    * @param oid
    * @return
    */
   public List<Document> getProcessAttachments(long oid)
   {
      List<Document> processAttachments = CollectionUtils.newArrayList();

      boolean supportsProcessAttachments = supportsProcessAttachments(oid);
      if (supportsProcessAttachments)
      {
         ProcessInstance processInstance = getProcessInstance(oid);

         WorkflowService ws = serviceFactoryUtils.getWorkflowService();
         Object o = ws.getInDataPath(processInstance.getOID(),
               DmsConstants.PATH_ID_ATTACHMENTS);

         DataDetails data = (DataDetails) modelUtils.getModel(
               processInstance.getModelOID()).getData(DmsConstants.PATH_ID_ATTACHMENTS);
         if (DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST.equals(data.getTypeId()))
         {
            processAttachments = (List<Document>) o;
         }
      }

      return processAttachments;
   }

   /**
    * return true if the provided Process Instance supports Process Attachments
    *
    * @param oid
    * @return
    */
   public boolean supportsProcessAttachments(long oid)
   {
      boolean supportsProcessAttachments = false;

      ProcessInstance processInstance = getProcessInstance(oid);

      if (processInstance != null)
      {
         Model model = modelUtils.getModel(processInstance.getModelOID());
         ProcessDefinition pd = model != null ? model
               .getProcessDefinition(processInstance.getProcessID()) : null;

         supportsProcessAttachments = processDefinitionUtils
               .supportsProcessAttachments(pd);
      }

      return supportsProcessAttachments;
   }
   
   /**
    * Get all process instances count
    * 
    * @return List
    */
   public InstanceCountsDTO getAllCounts()
   {

      InstanceCountsDTO countDTO = new InstanceCountsDTO();

      try
      {
         countDTO.aborted = getAbortedProcessInstancesCount();
         countDTO.active = getActiveProcessInstancesCount();
         countDTO.total = getTotalProcessInstancesCount();
         countDTO.waiting = getInterruptedProcessInstancesCount();
         countDTO.completed = getCompletedProcessInstancesCount();
      }
      catch (PortalException e)
      {
         trace.error("Error occurred.", e);
      }
      
      return countDTO;

   }
   
   private Long getProcessInstancesCount(ProcessInstanceQuery query)
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      return new Long(service.getProcessInstancesCount(query));
   }
   
   private long getTotalProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findAll());
   }

   private long getActiveProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findActive());
   }

   private long getInterruptedProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findInterrupted());
   }

   private long getCompletedProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findCompleted());
   }

   private long getAbortedProcessInstancesCount() throws PortalException
   {
      return getProcessInstancesCount(ProcessInstanceQuery.findInState(ProcessInstanceState.Aborted));
   }
   
   
   /**
    * 
    * @param options
    * @return
    */
   public ProcessInstances getProcessInstances(Options options)
   {
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      
      addDescriptorPolicy(options, query);

      addSortCriteria(query, options);

      addFilterCriteria(query, options);
      
      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip,
            true);
      query.setPolicy(subsetPolicy);
      
      return serviceFactoryUtils.getQueryService().getAllProcessInstances(query);      
   }
   
   /**
    * Adds filter criteria to the query
    *
    * @param query
    *           Query
    * @param options
    *           Options
    */
   private void addFilterCriteria(Query query, Options options)
   {

      if (options.filter == null)
      {
         return;
      }

      ProcessTableFilterDTO filterDTO = (ProcessTableFilterDTO) options.filter;

      FilterAndTerm filter = query.getFilter().addAndTerm();

      // Root process instance OID
      if (null != filterDTO.processInstanceRootOID)
      {

         if (null != filterDTO.processInstanceRootOID.from)
         {
            filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.greaterOrEqual(filterDTO.processInstanceRootOID.from));
         }
         if (null != filterDTO.processInstanceRootOID.to)
         {
            filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.lessOrEqual(filterDTO.processInstanceRootOID.to));
         }
      }
      // process instance OID
      if (null != filterDTO.oid)
      {
         if (null != filterDTO.oid.from)
         {
            filter.and(ProcessInstanceQuery.OID.greaterOrEqual(filterDTO.oid.from));
         }
         if (null != filterDTO.oid.to)
         {
            filter.and(ProcessInstanceQuery.OID.greaterOrEqual(filterDTO.oid.to));
         }
      }

      // Start Filter
      if (null != filterDTO.startTime)
      {

         if (filterDTO.startTime.from != null)
         {
            Date fromDate = new Date(filterDTO.startTime.from);

            filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(fromDate.getTime()));
         }

         if (filterDTO.startTime.to != null)
         {
            Date toDate = new Date(filterDTO.startTime.to);

            filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(toDate.getTime()));
         }
      }

      // endTime Filter
      if (null != filterDTO.endTime)
      {

         if (filterDTO.endTime.from != null)
         {
            Date fromDate = new Date(filterDTO.endTime.from);

            filter.and(ProcessInstanceQuery.TERMINATION_TIME.greaterOrEqual(fromDate.getTime()));
         }

         if (filterDTO.endTime.to != null)
         {
            Date toDate = new Date(filterDTO.endTime.to);

            filter.and(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(toDate.getTime()));
         }
      }

      // status Filter
      if (null != filterDTO.status)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (String status : filterDTO.status.like)
         {
            or.add(ProcessInstanceQuery.STATE.isEqual(Long.parseLong(status)));
         }
      }

      // process name Filter
      else if (null != filterDTO.processName)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (String processQId : filterDTO.processName.like)
         {
            or.add(new ProcessDefinitionFilter(processQId, false));
         }
      }

      // Priority Filter
      if (null != filterDTO.priority)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (String priority : filterDTO.priority.like)
         {
            or.or(ProcessInstanceQuery.PRIORITY.isEqual(Integer.valueOf(priority)));
         }
      }

      // starting user Filter
      if (null != filterDTO.startingUser)
      {
         // TODO

         // FilterOrTerm or = filter.addOrTerm();
         // for (UserWrapper user : users)
         // {
         // or.add(new
         // org.eclipse.stardust.engine.api.query.StartingUserFilter(user.getUser().getOID()));
         // }
      }

      // descriptors Filter
      addDescriptorFilters(query, filterDTO);

   }
   /**
    * Add descriptor policy
    * @param options
    * @param query
    */

   private void addDescriptorPolicy(Options options, Query query)
   {
      
      if(options.allDescriptorsVisible){
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      }else if(CollectionUtils.isNotEmpty(options.visibleDescriptorColumns)){          
         query.setPolicy(DescriptorPolicy.withIds(new HashSet<String>(options.visibleDescriptorColumns)));
      }else{
         query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
      }
   }

   /**
    * Add filter on descriptor columns .
    * @param query
    * @param processListFilterDTO
    */
   private void addDescriptorFilters(Query query, ProcessTableFilterDTO processListFilterDTO)
   {

      Map<String, DescriptorFilterDTO> descFilterMap = processListFilterDTO.descriptorFilterMap;

      if (null != descFilterMap)
      {
        
         Map<String, DataPath> descriptors = processDefUtils.getAllDescriptors(false);
         GenericDescriptorFilterModel filterModel = GenericDescriptorFilterModel
               .create(descriptors.values());
         filterModel.setFilterEnabled(true);

         for (Map.Entry<String, DescriptorFilterDTO> descriptor : descFilterMap
               .entrySet())
         {
            Object value = null;
            String key = StringUtils.substringAfter(descriptor.getKey(),
                  "descriptorValues.");

            // Boolean type desc
            if (descriptor.getValue().type.equals(ColumnDataType.BOOLEAN.toString()))
            {
               value = ((BooleanDTO)descriptor.getValue().value).equals;
            }
            // String type desc
            else if (descriptor.getValue().type.equals(ColumnDataType.STRING.toString()))
            {
               value = ((TextSearchDTO) descriptor.getValue().value).textSearch;
            }
            // Number type desc
            else if (descriptor.getValue().type.equals(ColumnDataType.NUMBER.toString()))
            {
               Number from = ((RangeDTO) descriptor.getValue().value).from;
               Number to = ((RangeDTO) descriptor.getValue().value).to;
               value = new NumberRange(from, to);
            }
            // Date type desc
            else if (descriptor.getValue().type.equals(ColumnDataType.DATE.toString()))
            {
               Long from = ((RangeDTO) descriptor.getValue().value).from;
               Long to = ((RangeDTO) descriptor.getValue().value).to;
               value = new DateRange();
               if (null != from)
               {
                  ((DateRange) value).setFromDateValue(new Date(from));
               }
               if (null != to)
               {
                  ((DateRange) value).setToDateValue(new Date(to));
               }
            }

            filterModel.setFilterValue(key, (Serializable) value);
         }

         DescriptorFilterUtils.applyFilters(query, filterModel);
      }

   }
   
   /**
    * @param query
    * @param options
    */
   private void addSortCriteria(Query query, Options options)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("options.orderBy = " + options.orderBy);
      }

      //TODO sorting for descriptors
      
      
      if (COL_PROCESS_NAME.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.PROC_DEF_NAME.ascendig(options.asc));
      }
      else if (PROCESS_INSTANCE_ROOT_OID.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID, options.asc);
      }
      else if (COL_PROCESS_INSTANCE_OID.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.OID, options.asc);
      }
      else if (COL_START_TIME.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.START_TIME, options.asc);
      }
      else if (COL_END_TIME.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.TERMINATION_TIME, options.asc);
      }
      else if (COL_PRIOIRTY.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.PRIORITY, options.asc);
      }
      else if (COL_STATUS.equals(options.orderBy))
      {
         query.orderBy(ProcessInstanceQuery.STATE, options.asc);
      }
      else if (STARTING_USER.equals(options.orderBy))
      {
         CustomOrderCriterion o = ProcessInstanceQuery.USER_ACCOUNT.ascendig(options.asc);
         query.orderBy(o);
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("ProcessInstanceUtils.addSortCriteria(Query, Options): Sorting not implemented for " + options.asc);
         }
      }
   }
}
