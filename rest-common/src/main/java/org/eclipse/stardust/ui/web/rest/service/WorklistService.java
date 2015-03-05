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
package org.eclipse.stardust.ui.web.rest.service;

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.getAssignedToLabel;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.getLastPerformer;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isAbortable;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isActivatable;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isDelegable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.DataDetails;
import org.eclipse.stardust.engine.api.dto.DataPathDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CriticalityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.StatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.CriticalityUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.WorklistUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentInfo;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDocumentDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;
/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
public class WorklistService
{
   @Resource
   private WorklistUtils worklistUtils;

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   @Resource
   private CriticalityUtils criticalityUtils;

   /**
    * @param participantQId
    * @return
    */
   public QueryResultDTO getWorklistForParticipant(String participantQId, String context, Options options)
   {
      QueryResult<?> queryResult = worklistUtils.getWorklistForParticipant(participantQId, options);
      return buildWorklistResult(queryResult);
   }

   /**
    * @param userId
    * @return
    */
   public QueryResultDTO getWorklistForUser(String userId, String context, Options options)
   {
      QueryResult<?> queryResult = worklistUtils.getWorklistForUser(userId, options);
      return buildWorklistResult(queryResult);
   }

   /**
    * @param queryResult
    * @return
    */
   private QueryResultDTO buildWorklistResult(QueryResult<?> queryResult)
   {
      List<ActivityInstanceDTO> list = new ArrayList<ActivityInstanceDTO>();

      List<CriticalityCategory>  criticalityConfigurations = criticalityUtils.getCriticalityConfiguration();

      for (Object object : queryResult)
      {
         if (object instanceof ActivityInstance)
         {
            ActivityInstance ai = (ActivityInstance) object;

            ActivityInstanceDTO dto;
            if (!activityInstanceUtils.isTrivialManualActivity(ai))
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

            int criticalityValue = criticalityUtils.getPortalCriticalityValue(ai.getCriticality());
            CriticalityCategory criticalCategory =  criticalityUtils.getCriticalityCategory(criticalityValue, criticalityConfigurations);
            CriticalityDTO criticalityDTO = DTOBuilder.build(criticalCategory, CriticalityDTO.class);
            criticalityDTO.value = criticalityValue;
            dto.criticality = criticalityDTO;

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
   private Map<String, DescriptorDTO> getProcessDescriptors(  List<ProcessDescriptor> processDescriptorsList) 
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
   
   
   public static List<ProcessDescriptor> createProcessDescriptorsNew(Map<String, Object> descriptors,
	         ProcessDefinition processDefinition, boolean evaluateBlankDescriptors)
	   {
	      //escape special character to avoid UI
	      descriptors = CommonDescriptorUtils.escapeDescriptors(descriptors);
	      boolean suppressBlankDescriptors = false;
	      List<ProcessDescriptor> processDescriptors = new ArrayList<ProcessDescriptor>();
	      
	      if (evaluateBlankDescriptors)
	      {
	         if (CommonDescriptorUtils.isSuppressBlankDescriptorsEnabled())
	         {
	            suppressBlankDescriptors = true;
	         }
	      }
	      try
	      {
	         Map<String, DataPathDetails> datapathMap = getDatapathMap(processDefinition);

	         ProcessDescriptor processDescriptor;
	         for (Entry<String, DataPathDetails> entry : datapathMap.entrySet())
	         {
	            Object descriptorValue = descriptors.get(entry.getKey());
	            if (suppressBlankDescriptors && isEmpty(descriptorValue))
	            {
	               // filter out the descriptor
	            }
	            else
	            {
	               DataPathDetails dataPathDetails = entry.getValue();
	               Model model = ModelCache.findModelCache().getModel(dataPathDetails.getModelOID());
	               DataDetails dataDetails = model != null ? (DataDetails) model.getData(dataPathDetails.getData()) : null;
	               if ((DmsConstants.DATA_ID_ATTACHMENTS.equals(dataPathDetails.getData()))
	                     || (null != dataDetails && (DmsConstants.DATA_TYPE_DMS_DOCUMENT.equals(dataDetails.getTypeId()) || DmsConstants.DATA_TYPE_DMS_DOCUMENT_LIST
	                           .equals(dataDetails.getTypeId()))))
	               {
	                  continue;
	               }
	               else
	               {
	                  processDescriptor = new ProcessDescriptor(entry.getKey(), I18nUtils.getDataPathName(entry.getValue()),
	                		  CommonDescriptorUtils.formatDescriptorValue(descriptors.get(entry.getKey()), entry.getValue().getAccessPath()));
	                  processDescriptors.add(processDescriptor);
	               }
	            }
	         }
	      }
	      catch (Exception e)
	      {
	        // trace.error("Error occured at create Process Descriptors", e);
	      }
	      return processDescriptors;
	   }

   
   /**
    * @param processDefinition
    * @return
    */
   private static Map<String, DataPathDetails> getDatapathMap(ProcessDefinition processDefinition)
   {
      List<DataPathDetails> dataPaths = processDefinition.getAllDataPaths();
      Map<String, DataPathDetails> datapathMap = new LinkedHashMap<String, DataPathDetails>();
      DataPathDetails dataPathDetails;
      int size = dataPaths.size();
      for (int i = 0; i < size; i++)
      {
         dataPathDetails = (DataPathDetails) dataPaths.get(i);
         if (dataPathDetails.isDescriptor())
         {
            datapathMap.put(dataPathDetails.getId(), dataPathDetails);
         }
      }
      return datapathMap;
   }   
   
   /**
    * method to check if object type contain empty value
    * add more types if required
    * @param value
    * @return
    */
   private static boolean isEmpty(Object value)
   {
      if (null == value)
      {
         return true;
      }
      else if (value instanceof List && CollectionUtils.isEmpty((List<?>) value))
      {
         return true;
      }
      else
      {
         return StringUtils.isEmpty(String.valueOf(value));
      }
   }
   
   
   
}
