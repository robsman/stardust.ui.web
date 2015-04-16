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
/**
 * @author Johnson.Quadras
 */

package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;

public class ActivityUtils
{
   /**
    * 
    * @param processQualifiedId
    * @param activityQualiedId
    * @return
    */
   public static Activity getActivity(String processQualifiedId, String activityQualiedId)
   {
      List<DeployedModel> models = CollectionUtils.newArrayList(ModelUtils.getAllModels());
      for (DeployedModel model : models)
      {
         // get all process definitions from the model
         List<ProcessDefinition> processes = model.getAllProcessDefinitions();
         for (ProcessDefinition processDefinition : processes)
         {
            if (processDefinition.getQualifiedId().equals(processQualifiedId))
            {
               List<Activity> activities = processDefinition.getAllActivities();
               for (Activity activity : activities)
               {
                  if (activity.getQualifiedId().equals(activityQualiedId))
                  {
                     return activity;
                  }
               }
            }
         }

      }
      return null;
   }
}
