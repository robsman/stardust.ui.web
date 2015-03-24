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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
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
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.FilterDTO.BooleanDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.RangeDTO;
import org.eclipse.stardust.ui.web.rest.FilterDTO.TextSearchDTO;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.Options;
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
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.NumberRange;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
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
   
   private static final String COL_ACTIVITY_NAME = "overview";

   private static final String COL_ACTIVITY_INSTANCE_OID = "oid";

   private static final String COL_START_TIME = "started";

   private static final String COL_LAST_MODIFICATION_TIME = "lastModified";

   private static final String COL_CRITICALITY = "criticality";

   private static final String COL_PRIOIRTY = "priority";

   private static double PORTAL_CRITICALITY_MUL_FACTOR = 1000;
   
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
      if (null != filterDTO.oid)
      {
         if (null != filterDTO.oid.from)
         {
            filter.and((worklistQuery
                  ? WorklistQuery.ACTIVITY_INSTANCE_OID
                  : ActivityInstanceQuery.OID).greaterOrEqual(filterDTO.oid.from));
         }
         if (null != filterDTO.oid.to)
         {
            filter.and((worklistQuery
                  ? WorklistQuery.ACTIVITY_INSTANCE_OID
                  : ActivityInstanceQuery.OID).lessOrEqual(filterDTO.oid.to));
         }
      }

      // Start Filter
      if (null != filterDTO.started)
      {

         if (filterDTO.started.from != null)
         {
            Date fromDate = new Date(filterDTO.started.from);
            filter.and((worklistQuery
                  ? WorklistQuery.START_TIME
                  : ActivityInstanceQuery.START_TIME).greaterOrEqual(fromDate.getTime()));
         }

         if (filterDTO.started.to != null)
         {
            Date toDate = new Date(filterDTO.started.to);
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
      if (null != filterDTO.overview)
      {

         if (!CollectionUtils.isEmpty(filterDTO.overview.activities))
         {
            FilterOrTerm or = filter.addOrTerm();
            if (filterDTO.overview.activities.contains("-1"))
            {
            }
            else
            {
               for (String activity : filterDTO.overview.activities)
               {

                  or.add(ActivityFilter.forAnyProcess(activity));
               }
            }
         }

         if (!CollectionUtils.isEmpty(filterDTO.overview.processes))
         {
            FilterOrTerm or = filter.addOrTerm();
            if (!filterDTO.overview.processes.contains("-1"))
            {
               for (String processQId : filterDTO.overview.processes)
               {

                  or.add(new ProcessDefinitionFilter(processQId, false));
               }
            }
         }
      }

      // Process Filter
      if (null != filterDTO.processDefinition)
      {
         FilterOrTerm or = filter.addOrTerm();
         if (!filterDTO.processDefinition.processes.contains("-1"))
         {
            for (String processQId : filterDTO.processDefinition.processes)
            {

               or.add(new ProcessDefinitionFilter(processQId, false));
            }
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
   }
   
   /**
    * Get the filters from the JSON string
    * @param jsonFilterString
    * @return
    */
   public static  WorklistFilterDTO getFilters(String jsonFilterString, List<DescriptorColumnDTO> availableDescriptorColumns)
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
   public static  Options populatePostData(Options options, String postData, List<DescriptorColumnDTO> availableDescriptorColumns)
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
   public static QueryResultDTO buildWorklistResult(QueryResult<?> queryResult)
   {
      List<ActivityInstanceDTO> list = new ArrayList<ActivityInstanceDTO>();

      List<CriticalityCategory>  criticalityConfigurations = CriticalityUtils.getCriticalityConfiguration();

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
            dto.lastPerformer = getLastPerformer(ai, UserUtils.getDefaultUserNameDisplayFormat());
            dto.assignedTo = getAssignedToLabel(ai);

            StatusDTO status = DTOBuilder.build(ai, StatusDTO.class);
            status.label = ActivityInstanceUtils.getActivityStateLabel(ai);
            dto.status = status;

            int criticalityValue = CriticalityUtils.getPortalCriticalityValue(ai.getCriticality());
            CriticalityCategory criticalCategory =  CriticalityUtils.getCriticalityCategory(criticalityValue, criticalityConfigurations);
            CriticalityDTO criticalityDTO = DTOBuilder.build(criticalCategory, CriticalityDTO.class);
            criticalityDTO.value = criticalityValue;
            dto.criticality = criticalityDTO;
            
            dto.priority = DTOBuilder.build(ai, PriorityDTO.class);

            dto.defaultCaseActivity= ActivityInstanceUtils.isDefaultCaseActivity(ai);
            if ( !dto.defaultCaseActivity )
            {
               dto.abortActivity = isAbortable(ai);
               dto.delegable = isDelegable(ai);
            }

            List<ProcessDescriptor> processDescriptorsList = CollectionUtils.newList();

            ModelCache modelCache = ModelCache.findModelCache();
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
            }
            

            if (!processDescriptorsList.isEmpty()) {
                dto.descriptorValues = getProcessDescriptors(processDescriptorsList);
            }
            
         

            dto.activatable = isActivatable(ai);
            if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(ai.getQualityAssuranceState()))
            {
               long monitoredActivityPerformerOID = ai.getQualityAssuranceInfo().getMonitoredInstance()
                     .getPerformedByOID();
               long currentPerformerOID = SessionContext.findSessionContext().getUser().getOID();
               if (monitoredActivityPerformerOID == currentPerformerOID)
               {
                  dto.activatable = false;
               }
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
    */
    private static Map<String, DescriptorDTO> getProcessDescriptors(  List<ProcessDescriptor> processDescriptorsList) 
     {
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
    
}