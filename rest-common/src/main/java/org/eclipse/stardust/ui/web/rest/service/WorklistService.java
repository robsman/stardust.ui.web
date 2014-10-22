/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

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
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.WorklistUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;

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

   /**
    * @param participantQId
    * @return
    */
   public QueryResultDTO getWorklistForParticipant(String participantQId, String context, Options options)
   {
      QueryResult<?> queryResult = worklistUtils.getWorklistForParticipant(participantQId, options);
      return buildWorklistResult(queryResult, context);
   }

   /**
    * @param userId
    * @return
    */
   public QueryResultDTO getWorklistForUser(String userId, String context, Options options)
   {
      QueryResult<?> queryResult = worklistUtils.getWorklistForUser(userId, options);
      return buildWorklistResult(queryResult, context);
   }

   /**
    * @param queryResult
    * @return
    */
   private QueryResultDTO buildWorklistResult(QueryResult<?> queryResult, String context)
   {
      List<ActivityInstanceDTO> list = new ArrayList<ActivityInstanceDTO>();
      for (Object object : queryResult)
      {
         if (object instanceof ActivityInstance)
         {
            ActivityInstance ai = (ActivityInstance) object;

            ActivityInstanceDTO dto;
            if (null == context)
            {
               dto = DTOBuilder.build(ai, ActivityInstanceDTO.class);
            }
            else
            {
               TrivialActivityInstanceDTO trivialDto = DTOBuilder.build(ai, TrivialActivityInstanceDTO.class);
               trivialDto.trivial = activityInstanceUtils.isTrivialManualActivity(ai, context);
               dto = trivialDto;
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
               dto.descriptors = new LinkedHashMap();
               for (ProcessDescriptor processDescriptor : processDescriptorsList)
               {
                  dto.descriptors.put(processDescriptor.getId(), processDescriptor);
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
