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

import java.util.Collection;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.core.pojo.data.Type;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ModelDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
@Component
public class ModelServiceBean
{
   /**
    * 
    * @return
    */
   public List<ModelDTO> getModels(Boolean allActive) throws Exception
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
            if (!(PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId())))
            {
               ModelDTO modelDTO = DTOBuilder.build(model, ModelDTO.class);
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
                  processDefitionDTOList.add(processDefinitionDTO);
                  List<Activity> activities = processDefinition.getAllActivities();
                  // Create ActivityDTO list
                  for (Activity activity : activities)
                  {
                     ActivityDTO activityDTO = DTOBuilder.build(activity, ActivityDTO.class);
                     activityDTOList.add(activityDTO);
                  }
                  processDefinitionDTO.activities = activityDTOList;
               }
               modelDTO.processDefinitions = processDefitionDTOList;
               modelDTO.data = dataDTOList;
               modelList.add(modelDTO);
            }
         }
      }
      catch (Exception e)
      {
         throw e;
      }

      return modelList;
   }

}
