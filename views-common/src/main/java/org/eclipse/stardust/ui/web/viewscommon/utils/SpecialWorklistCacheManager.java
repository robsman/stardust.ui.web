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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.springframework.beans.factory.InitializingBean;

/**
 * 
 * @author Yogesh.Manware
 * 
 */
public class SpecialWorklistCacheManager implements InitializingBean
{
   public static final String BEAN_ID = "ippSpecialWorklistCacheManager";
   public static final Logger trace = LogManager.getLogger(SpecialWorklistCacheManager.class);
   public static final String ALL_ACTVITIES = "allActivities";
   public static final String CRITICAL_ACTVITIES = "criticalActivities";

   private Map<String, ProcessWorklistCacheEntry> worklists;
   private CriticalityCategory definedHighCriticality;
   private boolean initialized = false;

   /**
    * @return SpecialWorklistCacheManager
    */
   public static SpecialWorklistCacheManager getInstance()
   {
      SpecialWorklistCacheManager cacheManager = (SpecialWorklistCacheManager) FacesUtils.getBeanFromContext(BEAN_ID);
      if (!cacheManager.initialized)
      {
         cacheManager.initialize();
         cacheManager.initialized = true;
      }
      return cacheManager;
   }

   private void initialize()
   {
      worklists = new LinkedHashMap<String, ProcessWorklistCacheEntry>();
      ActivityInstances result = WorklistUtils.getAllAssignedActivities();
      worklists.put(ALL_ACTVITIES, new ProcessWorklistCacheEntry(result.getTotalCount(), result.getQuery()));
      definedHighCriticality = CriticalityConfigurationHelper.getInstance().getCriticality(
            CriticalityConfigurationUtil.PORTAL_CRITICALITY_MAX);
      result = WorklistUtils.getCriticalActivities(definedHighCriticality);
      worklists.put(CRITICAL_ACTVITIES, new ProcessWorklistCacheEntry(result.getTotalCount(), result.getQuery()));
   }

   /**
    * @param worklistName
    * @return
    */
   public long getWorklistCount(String worklistName)
   {
      return worklists.get(worklistName).getCount();
   }

   /**
    * @param worklistName
    * @return
    */
   public Object getWorklistQuery(String worklistName)
   {
      return worklists.get(worklistName).getActivityInstanceQuery();
   }

   public void setWorklistCount(String id, long totalCount)
   {
      worklists.get(id).setCount(totalCount);
   }
   
   /**
    * @param oldAi
    * @param event
    */
   public void handleActivityEvent(ActivityInstance oldAi, ActivityEvent event)
   {
      ProcessWorklistCacheEntry allActivities = worklists.get(ALL_ACTVITIES);
      ProcessWorklistCacheEntry criticialActivities = worklists.get(CRITICAL_ACTVITIES);
 
      if (ActivityEvent.ACTIVATED.equals(event.getType()))
      {
         // oldAi can be null if it's the first AI of PI is Activated
         if (null == oldAi)
         {
            allActivities.setCount(allActivities.getCount() + 1);

            if (isActivityCritical(event.getActivityInstance()))
            {
               criticialActivities.setCount(criticialActivities.getCount() + 1);
            }
         }
      }
      else if (ActivityEvent.ABORTED.equals(event.getType()) || ActivityEvent.COMPLETED.equals(event.getType()))
      {
         allActivities.setCount(allActivities.getCount() - 1);

         if (isActivityCritical(oldAi))
         {
            criticialActivities.setCount(criticialActivities.getCount() - 1);
         }
      }
   }

   /**
    * @param ai
    * @return
    */
   private boolean isActivityCritical(ActivityInstance ai)
   {
      if (null != ai)
      {
         CriticalityCategory criticality = CriticalityConfigurationHelper.getInstance().getCriticality(
               CriticalityConfigurationUtil.getPortalCriticality(ai.getCriticality()));

         if (definedHighCriticality.equals(criticality))
         {
            return true;
         }
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {}
}