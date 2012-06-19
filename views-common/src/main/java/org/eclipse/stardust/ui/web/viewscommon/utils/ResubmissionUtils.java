/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.CompareHelper;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.EventHandler;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.EventHandlerBinding;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.extensions.conditions.timer.TimeStampCondition;
import org.eclipse.stardust.engine.core.runtime.beans.ActivityInstanceProperty;


public class ResubmissionUtils
{
   public static class ModelResubmissionActivity
   {
      private final String activityId;
      private final String processId;
      private final Set<Long> modelOids;
      
      public ModelResubmissionActivity(String activityId, String processId)
      {
         if(StringUtils.isEmpty(activityId) || StringUtils.isEmpty(processId))
         {
            new IllegalArgumentException("NULL parameters are not allowed");
         }
         this.activityId = activityId;
         this.processId = processId;
         modelOids = new HashSet<Long>();
      }
      
      public String getActivityId()
      {
         return activityId;
      }
      
      public String getProcessId()
      {
         return processId;
      }
      
      public Set<Long> getModelOids()
      {
         return modelOids;
      }
      
      public void addModelOid(long modelOid)
      {
         modelOids.add(new Long(modelOid));
      }

      public int hashCode()
      {
         final int PRIME = 31;
         int result = 1;
         result = PRIME * result + ((activityId == null) ? 0 : activityId.hashCode());
         result = PRIME * result + ((processId == null) ? 0 : processId.hashCode());
         return result;
      }

      public boolean equals(Object obj)
      {
         if (this == obj)
         {
            return true;
         }
         if(obj instanceof ModelResubmissionActivity)
         {
            final ModelResubmissionActivity other = (ModelResubmissionActivity) obj;
            return CompareHelper.areEqual(activityId, other.activityId) &&
               CompareHelper.areEqual(processId, other.processId);
         }
         return false;
      }
   }
   
   public static void fillListWithResubmissionActivities(List<ModelResubmissionActivity> resubAIs)
   {
      if(resubAIs == null)
      {
         throw new IllegalArgumentException("List for resubmission activities is null");
      }
      resubAIs.clear();
      Iterator<DeployedModel> modelIter = ModelCache.findModelCache().
         getAllModels().iterator();
      final List<ModelResubmissionActivity> resubmissionActivities = CollectionUtils.newList();
      while(modelIter.hasNext())
      {
         Model model = modelIter.next();
         Iterator pds = model.getAllProcessDefinitions().iterator();
         while(pds.hasNext())
         {
            ProcessDefinition pd = (ProcessDefinition)pds.next();
            Iterator ais = pd.getAllActivities().iterator();
            while(ais.hasNext())
            {
               Activity activity = (Activity)ais.next();
               EventHandler handler = ActivityUtils.getEventHandler(activity,
                     TimeStampCondition.class.getName(), "Resubmission");
               if(handler != null && activity.isInteractive())
               {
                  ModelResubmissionActivity modelActivity = 
                     new ModelResubmissionActivity(activity.getId(), pd.getId());
                  int index = resubmissionActivities.indexOf(modelActivity);
                  if(index > -1)
                  {
                     modelActivity =  resubmissionActivities.get(index);
                  }
                  else
                  {
                     resubmissionActivities.add(modelActivity);
                  }
                  modelActivity.addModelOid(model.getModelOID());
               }
            }
         }
      }
      resubAIs.addAll(resubmissionActivities);
   }
   
   public static Date getResubmissionDate(ActivityInstance ai, WorkflowService ws)
   {
      EventHandlerBinding binding = ws.getActivityInstanceEventHandler(
            ai.getOID(), "Resubmission");
      Long time = null;
      if(binding != null && binding.isBound())
      {
         ActivityInstanceProperty aiProperty = (ActivityInstanceProperty)binding.getAttribute(
            PredefinedConstants.TARGET_TIMESTAMP_ATT);
         time = aiProperty != null ? new Long(aiProperty.getLongValue()) : null;
      }
      return time != null ? new Date(time.longValue()) : null;
   }

   public static boolean isResubmissionActivity(ActivityInstance ai)
   {
      return ActivityInstanceState.Hibernated.equals(ai.getState()) &&
         ai.getActivity().getEventHandler("Resubmission") != null;
   }
}
