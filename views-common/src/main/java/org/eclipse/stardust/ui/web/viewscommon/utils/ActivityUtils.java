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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.EventHandler;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.utils.PermissionHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUser;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.user.impl.IppUserProvider;

/**
 * @author Yogesh.Manware
 * 
 */
public class ActivityUtils
{

   public static final Comparator<Activity> ACTIVITY_ORDER = new Comparator<Activity>()
   {
      public int compare(Activity activity1, Activity activity2)
      {
         return I18nUtils.getActivityName(activity1).compareTo(I18nUtils.getActivityName(activity2));
      }
   };

   
   /**
    * returns all activities from all version giving preference to latest version
    * 
    * @param ws
    * @param processDefinitionQId
    * @param doFilterAccess
    * @return
    */
   @SuppressWarnings("unchecked")
   public static List<Activity> getAllActivities(WorkflowService ws, String processDefinitionQId, boolean doFilterAccess)
   {
      Map<String, Activity> activityMap = CollectionUtils.newMap();

      List<ProcessDefinition> processes;
      List<Activity> activities, filteredActivities;

      // Following function assumes that model list contains the hierarchy defined from
      // model management i.e. active model prior to old models
      List<DeployedModel> models = ModelUtils.getAllModelsActiveFirst();
      for (Model model : models)
      {
         processes = model.getAllProcessDefinitions();
         for (ProcessDefinition processDefinition : processes)
         {
            if (processDefinition.getQualifiedId().equals(processDefinitionQId))
            {
               activities = processDefinition.getAllActivities();
               filteredActivities = doFilterAccess == true ? filterAccessibleActivities(ws, activities) : activities;
               for (Activity activity : filteredActivities)
               {
                  if (!activityMap.containsKey(activity.getQualifiedId()))
                  {
                     activityMap.put(activity.getQualifiedId(), activity);
                  }
               }
            }
         }
      }
      return CollectionUtils.newArrayList(activityMap.values());
   }

   /**
    * Returns interactive activities if boolean is true otherwise returns non-interactive
    * activities
    * 
    * @param allActivities
    * @param interactive
    * @return
    */
   public static List<Activity> filterInteractiveActivities(List<Activity> allActivities, Boolean interactive)
   {
      List<Activity> interactiveactivities = CollectionUtils.newArrayList();
      List<Activity> nonInteractiveactivities = CollectionUtils.newArrayList();
      for (Activity activity : allActivities)
      {
         if (activity.isInteractive())
         {
            interactiveactivities.add(activity);
         }
         else
         {
            nonInteractiveactivities.add(activity);
         }
      }
      if (!interactive)
      {
         return interactiveactivities;
      }
      else
      {
         return nonInteractiveactivities;
      }
   }

   /**
    * Returns list of non auxiliary activities
    * 
    * @param allActivities
    * @return
    */
   public static List<Activity> filterAuxiliaryActivities(List<Activity> allActivities)
   {
      List<Activity> nonAuxActivities = CollectionUtils.newArrayList();
      for (Iterator<Activity> iterator = allActivities.iterator(); iterator.hasNext();)
      {
         Activity activity = iterator.next();
         if (!ActivityInstanceUtils.isAuxiliaryActivity(activity))
         {
            nonAuxActivities.add(activity);
         }
      }
      return nonAuxActivities;
   }

   /**
    * @param ws
    * @param activities
    * @return
    */
   public static List<Activity> filterAccessibleActivities(WorkflowService ws, List<Activity> activities)
   {
      PermissionHelper permissionHelper = ((IppUser) IppUserProvider.getInstance().getUser()).getPermissionHelper();
      return permissionHelper.filterActivityAccess(ws, activities);
   }

   public static EventHandler getEventHandler(ActivityInstance ai, String conditionType, String id)
   {
      return null != ai ? getEventHandler(ai.getActivity(), conditionType, id) : null;
   }

   public static EventHandler getEventHandler(Activity activity, String conditionType, String id)
   {
      EventHandler result = null;

      try
      {
         if (null != activity)
         {
            for (Iterator i = activity.getAllEventHandlers().iterator(); i.hasNext();)
            {
               EventHandler handler = (EventHandler) i.next();
               if (id.equals(handler.getId()))
               {
                  String condition = (String) handler
                        .getTypeAttribute(PredefinedConstants.CONDITION_CONDITION_CLASS_ATT);
                  // Pre-stardust classes(ag.carnot) are handled trasparently by Reflect
                  // class
                  Class classFromClassName = Reflect.getClassFromClassName(condition);
                  condition = classFromClassName.getName();

                  if (StringUtils.isEmpty(conditionType) || conditionType.equals(condition))
                  {
                     result = handler;
                     break;
                  }
               }
            }
         }
      }
      catch (ObjectNotFoundException onfe)
      {
         // TODO
      }

      return result;
   }

   /**
    * Activity QID can be duplicate in the same model as it does not contain process related info.
    * Duplicate Process in the same model is very rare.
    * return activity's unique key
    * 
    * @param activity
    * @return
    */
   public static String getActivityKey(Activity activity)
   {
      return activity.getProcessDefinitionId() + activity.getQualifiedId();
   }
}