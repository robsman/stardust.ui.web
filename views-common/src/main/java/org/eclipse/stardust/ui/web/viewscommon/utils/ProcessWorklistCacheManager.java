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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.springframework.beans.factory.InitializingBean;



/**
 * @author anoop.nair
 * 
 */
public class ProcessWorklistCacheManager implements InitializingBean
{
   public static final String BEAN_ID = "ippProcessWorklistCacheManager";
   public static final Logger trace = LogManager.getLogger(ProcessWorklistCacheManager.class);

   private Map<String, ProcessDefinition> processDefinitions = new HashMap<String, ProcessDefinition>();
   private Map<ProcessDefinition, ProcessWorklistCacheEntry> processWorklists = new HashMap<ProcessDefinition, ProcessWorklistCacheEntry>();
   private boolean initialized = false;

   /**
    * @return
    */
   public static ProcessWorklistCacheManager getInstance()
   {
      ProcessWorklistCacheManager cacheManager = (ProcessWorklistCacheManager) FacesUtils.getBeanFromContext(BEAN_ID);
      if (!cacheManager.initialized)
      {
         cacheManager.reset();
         cacheManager.initialized = true;
      }
      return cacheManager;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      // NOP
      // For lazy loading nature of the Launch Panel. Explicit loading is required by
      // calling reset() method
   }

   /**
    * 
    */
   private void reset()
   {
      processDefinitions = new LinkedHashMap<String, ProcessDefinition>();
      processWorklists = new LinkedHashMap<ProcessDefinition, ProcessWorklistCacheEntry>();

      List<ProcessDefinition> processDefs = ProcessDefinitionUtils.getProcessDefinitions_forUser();

      ActivityInstances ais;
      for (ProcessDefinition processDefinition : processDefs)
      {
         ais = WorklistUtils.getActivityInstances_anyActivatableByProcess(processDefinition);
         
         processDefinitions.put(processDefinition.getQualifiedId(), processDefinition);
         processWorklists.put(processDefinition, new ProcessWorklistCacheEntry(ais.getTotalCount(), ais.getQuery(), ais.getTotalCountThreshold()));
      }

      // printCache("reset()");
   }

   /**
    * @param processDefinition
    * @return
    */
   public long getWorklistCount(ProcessDefinition processDefinition)
   {
      // Get the Process Definition object from the first Map (using id) since
      // ProcessDefinition does not implement equals()
      ProcessDefinition processDefinitionKey = processDefinitions.get(processDefinition.getQualifiedId());
      ProcessWorklistCacheEntry processWorklistCacheEntry = processWorklists.get(processDefinitionKey);
      if (null != processWorklistCacheEntry)
      {
         return processWorklistCacheEntry.getCount();
      }

      return 0;
   }
   
   /**
    * @param processDefinition
    * @return
    */
   public long getWorklistCountThreshold(ProcessDefinition processDefinition)
   {
      // Get the Process Definition object from the first Map (using id) since
      // ProcessDefinition does not implement equals()
      ProcessDefinition processDefinitionKey = processDefinitions.get(processDefinition.getQualifiedId());
      ProcessWorklistCacheEntry processWorklistCacheEntry = processWorklists.get(processDefinitionKey);
      if (null != processWorklistCacheEntry)
      {
         return processWorklistCacheEntry.getTotalCountThreshold();
      }

      return Long.MAX_VALUE;
   }

   /**
    * @return
    */
   public Set<ProcessDefinition> getProcesses()
   {
      return Collections.unmodifiableSet(processWorklists.keySet());
   }

   /**
    * @param processDefinition
    * @return
    */
   public ActivityInstanceQuery getActivityInstanceQuery(ProcessDefinition processDefinition)
   {
      // Get the Process Definition object from the first Map (using id) since
      // ProcessDefinition does not implement equals()
      ProcessDefinition processDefinitionKey = processDefinitions.get(processDefinition.getQualifiedId());
      ProcessWorklistCacheEntry processWorklistCacheEntry = processWorklists.get(processDefinitionKey);
      if (null != processWorklistCacheEntry)
      {
         return (ActivityInstanceQuery) QueryUtils.getClonedQuery(processWorklistCacheEntry.getActivityInstanceQuery());
      }

      return null;
   }

   /**
    * @param processId
    * @param count
    */
   public void setWorklistCount(String processId, long count)
   {
      ProcessDefinition processDefinitionKey = processDefinitions.get(processId);
      ProcessWorklistCacheEntry processWorklistCacheEntry = processWorklists.get(processDefinitionKey);
      if (null != processWorklistCacheEntry)
      {
         processWorklistCacheEntry.setCount(count);
      }
   }
   
   /**
    * @param processId
    * @param count
    */
   public void setWorklistThresholdCount(String processId, long count)
   {
      ProcessDefinition processDefinitionKey = processDefinitions.get(processId);
      ProcessWorklistCacheEntry processWorklistCacheEntry = processWorklists.get(processDefinitionKey);
      if (null != processWorklistCacheEntry)
      {
         processWorklistCacheEntry.setTotalCountThreshold(count);
      }
   }

   /**
    * @param oldAi
    * @param event
    */
   public void handleActivityEvent(ActivityInstance oldAi, ActivityEvent event)
   {
      ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(event.getActivityInstance().getModelOID(),
            event.getActivityInstance().getProcessDefinitionId());
      ProcessWorklistCacheEntry entry = processWorklists.get(processDefinitions.get(pd.getQualifiedId()));
      if (null != entry)
      {
         if (ActivityEvent.ACTIVATED.equals(event.getType()))
         {
            // oldAi can be null if it's the first AI of PI is Activated
            if (null == oldAi && entry.getCount() < Long.MAX_VALUE)
            {
               entry.setCount(entry.getCount() + 1);
            }
         }
         else if (ActivityEvent.ABORTED.equals(event.getType()) || ActivityEvent.COMPLETED.equals(event.getType()))
         {
            if (entry.getCount() > 0 && entry.getCount() < entry.getTotalCountThreshold())
            {
               entry.setCount(entry.getCount() - 1);
            }
         }
      }

      // printCache("After handleActivityEvent()");
   }

   /**
    * Only Added for Development Purpose. Call to this function can be removed later.
    * 
    * @param msg
    */
   private void printCache(String msg)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("ProcessWorklistCacheManager>> " + msg);
         for (Entry<ProcessDefinition, ProcessWorklistCacheEntry> entry : processWorklists.entrySet())
         {
            trace.debug("\t" + entry.getKey() + "=>" + entry.getValue());
         }
      }
   }
}
