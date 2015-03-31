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

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.getAssignedToLabel;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.getLastPerformer;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isAbortable;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isActivatable;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isDelegable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DepartmentInfoDetails;
import org.eclipse.stardust.engine.api.dto.OrganizationInfoDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.RoleInfoDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.FilterDTO.BooleanDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.RangeDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.TextSearchDTO;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.ParticipantSearchComponent;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CriticalityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PriorityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.StatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantSearchResponseDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.NumberRange;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils.ParticipantType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDocumentDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Subodh.Godbole
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
public class ActivityTableUtils
{

   private static final String COL_ACTIVITY_NAME = "activityName";

   private static final String COL_ACTIVITY_INSTANCE_OID = "activityOID";

   private static final String COL_PROCESS_OID = "processOid";

   private static final String COL_START_TIME = "startTime";

   private static final String COL_LAST_MODIFICATION_TIME = "lastModified";

   private static final String COL_CRITICALITY = "criticality";

   private static final String COL_PRIOIRTY = "priority";
   
   private static final String TODAY = "today";

   private static final String THIS_WEEK = "thisWeek";

   private static final String THIS_MONTH = "thisMonth";

   private static final String THIS_QUARTER = "thisQuarter";

   private static final String LAST_SIX_MONTHS = "lastSixMonths";

   private static final String LAST_YEAR = "lastYear";

   private static final String ALL = "all";

   public static double PORTAL_CRITICALITY_MUL_FACTOR = 1000;
   
   public static enum MODE {
      ACTIVITY_TABLE, WORKLIST;
   }

   private static final Logger trace = LogManager.getLogger(ActivityTableUtils.class);

   /**
    * Adds filter criteria to the query
    *
    * @param query
    *           Query
    * @param options
    *           Options
    */
   public static void addFilterCriteria(Query query, Options options)
   {

      WorklistFilterDTO filterDTO = (WorklistFilterDTO) options.filter;

      if (filterDTO == null)
      {
         return;
      }

      FilterAndTerm filter = query.getFilter().addAndTerm();

      boolean worklistQuery = query instanceof WorklistQuery;

      // Activity ID
      if (null != filterDTO.activityOID)
      {
         if (null != filterDTO.activityOID.from)
         {
            filter.and((worklistQuery
                  ? WorklistQuery.ACTIVITY_INSTANCE_OID
                        : ActivityInstanceQuery.OID).greaterOrEqual(filterDTO.activityOID.from));
         }
         if (null != filterDTO.activityOID.to)
         {
            filter.and((worklistQuery
                  ? WorklistQuery.ACTIVITY_INSTANCE_OID
                        : ActivityInstanceQuery.OID).lessOrEqual(filterDTO.activityOID.to));
         }
      }
      //Process Instance Oid
      if (null != filterDTO.processOID)
      {
         if (null != filterDTO.processOID.from)
         {
            filter.and((worklistQuery
                  ? WorklistQuery.PROCESS_INSTANCE_OID
                        : ActivityInstanceQuery.PROCESS_INSTANCE_OID).greaterOrEqual(filterDTO.processOID.from));
         }
         if (null != filterDTO.processOID.to)
         {
            filter.and((worklistQuery
                  ? WorklistQuery.PROCESS_INSTANCE_OID
                        : ActivityInstanceQuery.PROCESS_INSTANCE_OID).lessOrEqual(filterDTO.processOID.to));
         }
      }
      // Start Filter
      if (null != filterDTO.startTime)
      {

         if (filterDTO.startTime.from != null)
         {
            Date fromDate = new Date(filterDTO.startTime.from);
            filter.and((worklistQuery
                  ? WorklistQuery.START_TIME
                        : ActivityInstanceQuery.START_TIME).greaterOrEqual(fromDate.getTime()));
         }

         if (filterDTO.startTime.to != null)
         {
            Date toDate = new Date(filterDTO.startTime.to);
            filter.and((worklistQuery
                  ? WorklistQuery.START_TIME
                        : ActivityInstanceQuery.START_TIME).lessOrEqual(toDate.getTime()));
         }
      }
      // Modified Filter
      if (null != filterDTO.lastModified)
      {

         if (filterDTO.lastModified.from != null)
         {
            Date fromDate = new Date(filterDTO.lastModified.from);

            filter.and((worklistQuery
                  ? WorklistQuery.LAST_MODIFICATION_TIME
                        : ActivityInstanceQuery.LAST_MODIFICATION_TIME).greaterOrEqual(fromDate
                              .getTime()));
         }

         if (filterDTO.lastModified.to != null)
         {
            Date toDate = new Date(filterDTO.lastModified.to);

            filter.and((worklistQuery
                  ? WorklistQuery.LAST_MODIFICATION_TIME
                        : ActivityInstanceQuery.LAST_MODIFICATION_TIME).lessOrEqual(toDate
                              .getTime()));
         }
      }
      // Status Filter
      if (null != filterDTO.status)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (String status : filterDTO.status.like)
         {
            Integer actState = Integer.parseInt(status);
            if (!worklistQuery)
            {
               or.add(ActivityInstanceQuery.STATE.isEqual(Long.parseLong(status
                     .toString())));
            }
            else if (worklistQuery)
            {
               // Worklist Query uses ActivityStateFilter.
               or.add(new ActivityStateFilter(ActivityInstanceState.getState(actState)));
            }
         }
      }
      // Priority Filter
      if (null != filterDTO.priority)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (String priority : filterDTO.priority.like)
         {
            or.or((worklistQuery
                  ? WorklistQuery.PROCESS_INSTANCE_PRIORITY
                        : ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY).isEqual(Integer
                              .valueOf(priority)));
         }
      }
      // Criticality Filter
      if (null != filterDTO.criticality)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (RangeDTO criticality : filterDTO.criticality.rangeLike)
         {
            or.or((worklistQuery
                  ? WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY
                        : ActivityInstanceQuery.CRITICALITY).between(
                              (criticality.from / PORTAL_CRITICALITY_MUL_FACTOR), criticality.to
                              / PORTAL_CRITICALITY_MUL_FACTOR));
         }

      }
      // Activities Filter
      if (null != filterDTO.activityName)
      {

         if (!CollectionUtils.isEmpty(filterDTO.activityName.activities))
         {
            FilterOrTerm or = filter.addOrTerm();
            if (!filterDTO.activityName.activities.contains("-1"))
            {
               for (String activity : filterDTO.activityName.activities)
               {

                  or.add(ActivityFilter.forAnyProcess(activity));
               }
            }
         }

         if (!CollectionUtils.isEmpty(filterDTO.activityName.processes))
         {
            FilterOrTerm or = filter.addOrTerm();
            if (!filterDTO.activityName.processes.contains("-1"))
            {
               for (String processQId : filterDTO.activityName.processes)
               {
                  or.add(new ProcessDefinitionFilter(processQId, false));
               }
            }
         }
      }
      // Process Filter
      if (null != filterDTO.processName)
      {
         FilterOrTerm or = filter.addOrTerm();
         if (!filterDTO.processName.processes.contains("-1"))
         {
            for (String processQId : filterDTO.processName.processes)
            {
               or.add(new ProcessDefinitionFilter(processQId, false));
            }
         }
      }
      // Assigned To
      if (null != filterDTO.assignedTo)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (ParticipantSearchResponseDTO participant : filterDTO.assignedTo.participants)
         {  

            if(ParticipantType.USER.toString().equals( participant.type) ){

               or.add(new org.eclipse.stardust.engine.api.query.PerformingUserFilter( Long.valueOf( participant.OID)));
            } 
            else if (ParticipantType.ROLE.toString().endsWith( participant.type))
            {
               RoleInfoDetails roleInfo = new RoleInfoDetails(participant.qualifiedId);
               or.add(org.eclipse.stardust.engine.api.query.PerformingParticipantFilter.forParticipant(roleInfo));                        
            }else if(ParticipantType.ORGANIZATION.toString().equals( participant.type)){

               OrganizationInfoDetails organizationInfo = new OrganizationInfoDetails(participant.qualifiedId);
               or.add(org.eclipse.stardust.engine.api.query.PerformingParticipantFilter.forParticipant(organizationInfo));     
            }else if(ParticipantSearchComponent.PerformerTypeUI.Department.name().equals(participant.type)){

               DepartmentInfo departmentInfo = new DepartmentInfoDetails(participant.OID, participant.id, participant.name, participant.runtimeOrganizationOid);
               or.add(org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter.forDepartment(departmentInfo));
            }
         }
      }
      //Completed By
      if ( null != filterDTO.completedBy)
      {
         FilterOrTerm or = filter.addOrTerm();
         for (ParticipantSearchResponseDTO user : filterDTO.completedBy.participants)
         {
            or.add(new org.eclipse.stardust.engine.api.query.PerformedByUserFilter(user.OID));
         }
      }
      addDescriptorFilters(query, filterDTO);
   }

   /**
    * Add descriptor policy
    * @param options
    * @param query
    */
   public static void addDescriptorPolicy(Options options, Query query)
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
    * @param worklistDTO
    */
   public static void addDescriptorFilters(Query query, WorklistFilterDTO worklistDTO)
   {

      Map<String, DescriptorFilterDTO> descFilterMap = worklistDTO.descriptorFilterMap;

      if (null != descFilterMap)
      {

         Map<String, DataPath> descriptors = ProcessDefinitionUtils.getAllDescriptors(false);
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
   public static void addSortCriteria(Query query, Options options)
   {
      boolean worklistQuery = query instanceof WorklistQuery;

      if (COL_ACTIVITY_NAME.equals(options.orderBy))
      {
         query.orderBy(ActivityInstanceQuery.ACTIVITY_NAME.ascendig(options.asc));
      }
      else if (COL_ACTIVITY_INSTANCE_OID.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.ACTIVITY_INSTANCE_OID
                     : ActivityInstanceQuery.OID, options.asc);
      }
      else if (COL_START_TIME.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.START_TIME
                     : ActivityInstanceQuery.START_TIME, options.asc);
      }
      else if (COL_LAST_MODIFICATION_TIME.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.LAST_MODIFICATION_TIME
                     : ActivityInstanceQuery.LAST_MODIFICATION_TIME, options.asc);
      }
      else if (COL_PRIOIRTY.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.PROCESS_INSTANCE_PRIORITY
                     : ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY, options.asc);
      }
      else if (COL_CRITICALITY.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY
                     : ActivityInstanceQuery.CRITICALITY, options.asc);
      }

      else if (COL_PROCESS_OID.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.PROCESS_INSTANCE_OID
                     : ActivityInstanceQuery.PROCESS_INSTANCE_OID, options.asc);
      }
   }

   /**
    * Get the filters from the JSON string
    * @param jsonFilterString
    * @return
    */
   public static WorklistFilterDTO getFilters(String jsonFilterString, List<DescriptorColumnDTO> availableDescriptorColumns)
   {
      WorklistFilterDTO worklistFilter = null;
      if (StringUtils.isNotEmpty(jsonFilterString))
      {
         try
         {
            JsonMarshaller jsonIo = new JsonMarshaller();
            JsonObject json = jsonIo.readJsonObject(jsonFilterString);
            worklistFilter = DTOBuilder.buildFromJSON(json, WorklistFilterDTO.class,
                  WorklistFilterDTO.getCustomTokens());
            if (StringUtils.contains(jsonFilterString, "descriptorValues"))
            {
               populateDescriptorFilters(worklistFilter, json, availableDescriptorColumns);
            }
         }
         catch (Exception e)
         {
            trace.error("Error in Deserializing filter JSON", e);
         }
      }

      return worklistFilter;
   }

   /**
    * Populate the options with the post data.
    * @param options
    * @param postData
    * @return
    */
   public static Options populatePostData(Options options, String postData, List<DescriptorColumnDTO> availableDescriptorColumns)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      // For filter
      JsonObject filters = postJSON.getAsJsonObject("filters");
      if (null != filters)
      {
         options.filter = getFilters(filters.toString(), availableDescriptorColumns);
      }


      JsonArray visbleColumns = postJSON.getAsJsonObject("descriptors").get("visbleColumns").getAsJsonArray();
      List<String> columnsList = new ArrayList<String>();
      for (JsonElement jsonElement : visbleColumns)
      {
         columnsList.add(StringUtils.substringAfter(jsonElement.getAsString(), "descriptorValues."));
      }
      options.visibleDescriptorColumns = columnsList;
      options.allDescriptorsVisible = postJSON.getAsJsonObject("descriptors").get("fetchAll").getAsBoolean();

      return options;
   }

   /**
    * Populates the descriptor filter values.
    * @param worklistFilter
    * @param descriptorColumnsFilterJson
    */
   public static void populateDescriptorFilters(WorklistFilterDTO worklistFilter,
         JsonObject descriptorColumnsFilterJson , List<DescriptorColumnDTO> availableDescriptorColumns)
   {

      List<DescriptorColumnDTO> descriptorColumns = availableDescriptorColumns;

      Map<String, DescriptorFilterDTO> descriptorColumnMap = new HashMap<String, DescriptorFilterDTO>();

      for (DescriptorColumnDTO descriptorColumnDTO : descriptorColumns)
      {
         Object filterDTO = null;
         if (null != descriptorColumnsFilterJson.get(descriptorColumnDTO.id))
         {
            // String TYPE
            if (ColumnDataType.STRING.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     WorklistFilterDTO.TextSearchDTO.class);

            }
            else if (ColumnDataType.DATE.toString().equals(descriptorColumnDTO.type)
                  || ColumnDataType.NUMBER.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     WorklistFilterDTO.RangeDTO.class);
            }
            else if (ColumnDataType.BOOLEAN.toString().equals(descriptorColumnDTO.type))
            {
               filterDTO = new Gson().fromJson(
                     descriptorColumnsFilterJson.get(descriptorColumnDTO.id),
                     WorklistFilterDTO.BooleanDTO.class);
            }
            descriptorColumnMap.put(descriptorColumnDTO.id, new DescriptorFilterDTO(
                  descriptorColumnDTO.type, filterDTO));
         }
      }

      worklistFilter.descriptorFilterMap = descriptorColumnMap;
   }


   /**
    * @param queryResult
    * @return
    */
   public static QueryResultDTO buildTableResult(QueryResult<?> queryResult , MODE mode)
   {
      List<ActivityInstanceDTO> list = new ArrayList<ActivityInstanceDTO>();

      List<CriticalityCategory>  criticalityConfigurations = CriticalityUtils.getCriticalityConfiguration();

      ModelCache modelCache = ModelCache.findModelCache();

      for (Object object : queryResult)
      {
         if (object instanceof ActivityInstance)
         {
            ActivityInstance ai = (ActivityInstance) object;

            ActivityInstanceDTO dto;
            if (!ActivityInstanceUtils.isTrivialManualActivity(ai))
            {
               dto = DTOBuilder.build(ai, ActivityInstanceDTO.class);
            }
            else
            {
               TrivialActivityInstanceDTO trivialDto = DTOBuilder.build(ai, TrivialActivityInstanceDTO.class);
               trivialDto.trivial = true;
               dto = trivialDto;
            }

            dto.duration = ActivityInstanceUtils.getDuration(ai);
            dto.assignedTo = getAssignedToLabel(ai);
            dto.criticality = populateCriticalityDTO(criticalityConfigurations, ai);
            dto.priority = DTOBuilder.build(ai, PriorityDTO.class);
            dto.status =  DTOBuilder.build(ai, StatusDTO.class);;
            dto.status.label = ActivityInstanceUtils.getActivityStateLabel(ai);
            dto.descriptorValues =  getProcessDescriptors(modelCache, ai);
            dto.activatable = findIfActivatable(ai);
            dto.defaultCaseActivity= ActivityInstanceUtils.isDefaultCaseActivity(ai);
            
            if ( !dto.defaultCaseActivity )
            {
               dto.abortActivity = isAbortable(ai);
               dto.delegable = isDelegable(ai);
            }
            
            if(mode.equals(MODE.ACTIVITY_TABLE))
            {
               dto.completedBy = ActivityInstanceUtils.getPerformedByName(ai);
               dto.participantPerformer = getParticipantPerformer(ai);
            }
            else
            {
               dto.lastPerformer = getLastPerformer(ai, UserUtils.getDefaultUserNameDisplayFormat());
            }
            list.add(dto);
         }
      }
      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = list;
      resultDTO.totalCount = queryResult.getTotalCount();

      return resultDTO;
   }
   
   /**
    * 
    * @param fromDateString
    * @return
    */
   public static Date determineDate(String fromDateString)
   {
      Calendar calendar = new GregorianCalendar(PortalApplication.getInstance().getTimeZone());
      if (fromDateString.equals(TODAY))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
      }
      else if (fromDateString.equals(THIS_WEEK))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
      }
      else if (fromDateString.equals(THIS_MONTH))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.set(Calendar.DAY_OF_MONTH, 1);
      }
      else if (fromDateString.equals(THIS_QUARTER))
      {
         int month = calendar.get(Calendar.MONTH);

         int quarter = month / 3;

         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.set(Calendar.MONTH, quarter * 3);
         calendar.set(Calendar.DAY_OF_MONTH, 1);
      }
      else if (fromDateString.equals(LAST_SIX_MONTHS))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         calendar.add(Calendar.MONTH, -6);
      }
      else if (fromDateString.equals(LAST_YEAR))
      {
         calendar.set(Calendar.HOUR_OF_DAY, 0);
         calendar.set(Calendar.MINUTE, 0);
         calendar.set(Calendar.SECOND, 0);
         calendar.set(Calendar.MILLISECOND, 0);
         // TODO did FS mean since last year, only last year or since begin
         // of last year
         // ect.
         // right now uses the interval of [(now - 1year), now]
         calendar.add(Calendar.YEAR, -1);
      }
      else if (fromDateString.equals(ALL))
      {
         calendar.setTime(new Date(0));
      }else{
         throw new IllegalArgumentException();
      }
      return calendar.getTime();
   }
   
   /**
    * Adds the filter sort descriptor criterias and the subset policy
    * @param query
    * @param options
    */
   public static void addCriterias(Query query,Options options){
      addDescriptorPolicy(options, query);
      addSortCriteria(query, options);
      addFilterCriteria(query, options);
      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip,
            true);
      query.setPolicy(subsetPolicy);
   }

   /**
    * 
    * @param ai
    * @return
    */
   private static boolean findIfActivatable(ActivityInstance ai)
   {
      boolean isActivable = isActivatable(ai);
      if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(ai.getQualityAssuranceState()))
      {
         long monitoredActivityPerformerOID = ai.getQualityAssuranceInfo().getMonitoredInstance()
               .getPerformedByOID();
         long currentPerformerOID = SessionContext.findSessionContext().getUser().getOID();
         if (monitoredActivityPerformerOID == currentPerformerOID)
         {
            isActivable = false;
         }
      }
      
      return isActivable;
   }

   /**
    * 
    * @param criticalityConfigurations
    * @param ai
    * @return
    */
   private static CriticalityDTO populateCriticalityDTO(List<CriticalityCategory> criticalityConfigurations,
         ActivityInstance ai)
   {
      int criticalityValue = CriticalityUtils.getPortalCriticalityValue(ai.getCriticality());
      CriticalityCategory criticalCategory =  CriticalityUtils.getCriticalityCategory(criticalityValue, criticalityConfigurations);
      CriticalityDTO criticalityDTO = DTOBuilder.build(criticalCategory, CriticalityDTO.class);
      criticalityDTO.value = criticalityValue;
      return criticalityDTO;
   }

   /**
    * 
    * @param ai
    * @param dto
    * @return
    */
   private static String  getParticipantPerformer(ActivityInstance ai)
   {
      Participant  participantPerformer = null;
      Activity activity = ai.getActivity();
      ModelParticipant performer = activity.getDefaultPerformer();
      if (performer != null)
      {
         participantPerformer = performer;
         if (performer instanceof ConditionalPerformer)
         {
            Participant p = ((ConditionalPerformer) performer).getResolvedPerformer();
            if (p != null && !(p instanceof User))
            {
               participantPerformer = p;
            }
            else
            {
               participantPerformer = null;
            }
         }
      }
      if(null != participantPerformer)
      {
        return participantPerformer != null ? I18nUtils.getParticipantName(participantPerformer) : null;
      }
      
      return null;
   }

   /**
    * 
    */
   private static Map<String, DescriptorDTO> getProcessDescriptors( ModelCache modelCache , ActivityInstance ai) 
   {
      List<ProcessDescriptor> processDescriptorsList = CollectionUtils.newList();

      Model model = modelCache.getModel(ai.getModelOID());
      ProcessDefinition processDefinition = model != null ? model.getProcessDefinition(ai.getProcessDefinitionId()) : null;
      if (processDefinition != null)
      {
         ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) ai.getProcessInstance();
         Map<String, Object> descriptorValues = processInstanceDetails.getDescriptors();
         CommonDescriptorUtils.updateProcessDocumentDescriptors(descriptorValues, ai.getProcessInstance(), processDefinition);
         if (processInstanceDetails.isCaseProcessInstance())
         {
            processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                  processInstanceDetails.getDescriptorDefinitions(), descriptorValues, processDefinition, true);
         }
         else
         {
            processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(descriptorValues,
                  processDefinition, true ,true);

         }
         
         Map<String, DescriptorDTO>  descriptors= new LinkedHashMap<String, DescriptorDTO>();
         for (Object descriptor : processDescriptorsList)
         {
            if( descriptor instanceof ProcessDocumentDescriptor) {
               ProcessDocumentDescriptor desc = (ProcessDocumentDescriptor) descriptor;

               List<DocumentDTO> documents = new ArrayList<DocumentDTO>();

               for (DocumentInfo documentInfo : desc.getDocuments()) {
                  DocumentDTO documentDTO = new DocumentDTO();
                  documentDTO.name = documentInfo.getName();
                  documentDTO.uuid = documentInfo.getId();
                  documentDTO.contentType = (MimeTypesHelper.detectMimeType(documentInfo.getName(), null).getType());
                  documents.add(documentDTO);
               }

               DescriptorDTO descriptorDto = new DescriptorDTO(desc.getKey() , desc.getValue(), true, documents);
               descriptors.put(desc.getId(), descriptorDto);
            }else{
               ProcessDescriptor desc = (ProcessDescriptor) descriptor;
               DescriptorDTO descriptorDto = new DescriptorDTO(desc.getKey() , desc.getValue(), false, null);
               descriptors.put(desc.getId(), descriptorDto);
            }
         }
         return descriptors;
      }
    return null;
   }
}
