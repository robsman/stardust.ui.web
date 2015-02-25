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

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.*;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.CriticalityUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.WorklistUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
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
               if (processInstanceDetails.isCaseProcessInstance())
               {
                  processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                        processInstanceDetails.getDescriptorDefinitions(), descriptorValues, processDefinition, true);
               }
               else
               {
                  processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(descriptorValues,
                        processDefinition, true);
               }
            }

            if (!processDescriptorsList.isEmpty()) {
               dto.descriptors = new LinkedHashMap<String, ProcessDescriptor>();
               for (ProcessDescriptor processDescriptor : processDescriptorsList)
               {
                  dto.descriptors.put(processDescriptor.getId(), processDescriptor);
               }
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
}
