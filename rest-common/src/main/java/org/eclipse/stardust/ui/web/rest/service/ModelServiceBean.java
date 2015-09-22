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

import static org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isAuxiliaryActivity;
import static org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils.isAuxiliaryProcess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ModelDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ParticipantManagementUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Sidharth.Singh
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Component
public class ModelServiceBean
{
   /**
    * 
    * @return
    */
   public List<ModelDTO> getModels(Boolean allActive, boolean includePredefinedModel) throws Exception
   {
      List<ModelDTO> modelList = CollectionUtils.newArrayList();
      try
      {
         Collection<DeployedModel> models = CollectionUtils.newArrayList();
         if (allActive.equals(true))
         {
            models = ModelCache.findModelCache().getActiveModels();
         }
         else
         {
            models = ModelCache.findModelCache().getAllModels();
         }

         for (DeployedModel model : models)
         {
            if (includePredefinedModel || !(PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId())))
            {
               ModelDTO modelDTO = DTOBuilder.build(model, ModelDTO.class);
               String modelName = I18nUtils.getModelName(model);
               List<ProcessDefinitionDTO> processDefitionDTOList = CollectionUtils.newArrayList();
               List<Data> modelData = model.getAllData();
               List<ProcessDefinition> processDefinitions = model.getAllProcessDefinitions();
               List<ActivityDTO> activityDTOList = CollectionUtils.newArrayList();
               List<DataDTO> dataDTOList = CollectionUtils.newArrayList();

               // Create DataDTO list
               for (Data data : modelData)
               {
                  DataDTO dataDTO = DTOBuilder.build(data, DataDTO.class);
                  dataDTOList.add(dataDTO);
               }

               // Create ProcessDefinitionDTO list
               for (ProcessDefinition processDefinition : processDefinitions)
               {
                  ProcessDefinitionDTO processDefinitionDTO = DTOBuilder.build(processDefinition,
                        ProcessDefinitionDTO.class);
                  processDefinitionDTO.modelName = modelName;
                  processDefinitionDTO.auxillary = isAuxiliaryProcess(processDefinition);
                  processDefinitionDTO.name = I18nUtils.getProcessName(processDefinition);
                  processDefitionDTOList.add(processDefinitionDTO);
                  activityDTOList = CollectionUtils.newArrayList();
                  List<Activity> activities = processDefinition.getAllActivities();
                  // Create ActivityDTO list
                  for (Activity activity : activities)
                  {
                     ActivityDTO activityDTO = DTOBuilder.build(activity, ActivityDTO.class);
                     activityDTO.auxillary = isAuxiliaryActivity(activity);
                     activityDTO.name = I18nUtils.getActivityName(activity);
                     activityDTO.runtimeElementOid = activity.getRuntimeElementOID();
                     activityDTOList.add(activityDTO);
                  }
                  processDefinitionDTO.activities = activityDTOList;
               }
               modelDTO.processDefinitions = processDefitionDTOList;
               modelDTO.data = dataDTOList;
               modelList.add(modelDTO);

               // Add all top-level Organizations
               updateTopLevelOrganizations(model, modelDTO);

               // Add all top-level Roles
               updateTopLevelRoles(model, modelDTO);
            }
         }
      }
      catch (Exception e)
      {
         throw e;
      }

      return modelList;
   }

   /**
    * @param model
    * @param modelDto
    */
   private void updateTopLevelOrganizations(Model model, ModelDTO modelDto)
   {
      List<Organization> topLevelOrganizations = null;

      topLevelOrganizations = model.getAllTopLevelOrganizations();
      modelDto.allTopLevelOrganizations = new ArrayList<ParticipantDTO>();

      for (Organization organization : topLevelOrganizations)
      {
         String modelId = ModelUtils.extractModelId(organization.getQualifiedId());
         if (modelId.equals(model.getId()))
         {
            ParticipantDTO participantDTO = new ParticipantDTO(organization);
            participantDTO.type = ParticipantManagementUtils.getParticipantType(organization).name();
            modelDto.allTopLevelOrganizations.add(participantDTO);
         }
      }
   }

   /**
    * @param model
    * @param modelDto
    * @param adminRoleAdded
    */
   private void updateTopLevelRoles(Model model, ModelDTO modelDto)
   {
      List<Role> topLevelRoles = null;
      topLevelRoles = model.getAllTopLevelRoles();
      modelDto.allTopLevelRoles = new ArrayList<ParticipantDTO>();

      for (Role role : topLevelRoles)
      {
         // We the "Administrator" role that belong to Predefined model
         if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(role.getId()))
         {
            if (PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
            {
               ParticipantDTO participantDTO = new ParticipantDTO(role);
               participantDTO.type = ParticipantManagementUtils.getParticipantType(role).name();
               modelDto.allTopLevelRoles.add(participantDTO);
            }
            continue;
         }

         String modelId = ModelUtils.extractModelId(role.getQualifiedId());

         if ((modelId == null) || (modelId.equals(model.getId())))
         {
            ParticipantDTO participantDTO = new ParticipantDTO(role);
            participantDTO.type = ParticipantManagementUtils.getParticipantType(role).name();
            modelDto.allTopLevelRoles.add(participantDTO);
         }
      }
   }
}