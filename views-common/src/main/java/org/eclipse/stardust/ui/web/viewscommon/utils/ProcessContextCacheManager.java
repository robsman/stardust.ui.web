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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.stardust.common.LRUCache;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;



/**
 * @author Subodh.Godbole
 *
 */
public class ProcessContextCacheManager
{
   public static final String PARAM_ENABLED = "Carnot.Client.Caching.ProcessInstanceContext.Enabled";

   public static final String PARAM_CACHE_SIZE = "Carnot.Client.Caching.ProcessInstanceContext.CacheSize";

   public static final String PARAM_CACHE_TTL = "Carnot.Client.Caching.ProcessInstanceContext.CacheTTL";

   public static final String BEAN_ID = "ippProcessContextCacheManager";
   
   private static Logger trace = LogManager.getLogger(ProcessContextCacheManager.class);
   
   private final LRUCache/*<Long, ProcessContextCache>*/ cache;
   
   private final boolean cacheEnabled;
   
   public static ProcessContextCacheManager getInstance()
   {
      return (ProcessContextCacheManager) ManagedBeanUtils.getManagedBean(BEAN_ID);
   }
   
   public ProcessContextCacheManager()
   {
      Parameters params = Parameters.instance();
      cacheEnabled = params.getBoolean(PARAM_ENABLED, false);
      
      if (cacheEnabled)
      {
         this.cache = new LRUCache(params.getLong(PARAM_CACHE_TTL, 120) * 1000L,
               params.getInteger(PARAM_CACHE_SIZE, 100), false);
      }
      else
      {
         this.cache = null;
      }
   }

   public ProcessInstance getProcessInstance(long oid, boolean forceReload)
   {
      if (null == cache)
      {
         return null;
      }

      ProcessInstance pi;

      ProcessContextCache entry = (ProcessContextCache) cache.get(oid);
      if (!forceReload && null != entry)
      {
         pi = entry.getProcessInstance();
      }
      else
      {
         pi = getProcessInstances(Arrays.asList(oid)).get(0);

         if (null != pi)
         {
            cache.put(oid, new ProcessContextCache(this, pi));
         }
      }
      return pi;
   }
   
   public ProcessInstance getProcessInstance(long oid)
   {
      return getProcessInstance(oid, false);
   }

   /**
    * @param oids
    * @param forceReload
    * @param withDescriptors
    * @return
    */
   public List<ProcessInstance> getProcessInstances(List<Long> oids, boolean forceReload, boolean withDescriptors)
   {
      if (null == cache || null == oids)
      {
         return null;
      }

      if(trace.isDebugEnabled())
      {
         trace.debug("Getting Process Instances: " + oids);
      }

      List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>(); 
      if (!oids.isEmpty())
      {
         ProcessInstanceQuery piQuery = ProcessInstanceQuery.findAll();
         FilterOrTerm orTerm =  piQuery.getFilter().addOrTerm();
         
         if (withDescriptors)
         {
            piQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         }
         
         // Prepare Data to fetch
         boolean fetchData = false;
         for (Long oid : oids)
         {
            if (forceReload || null == getCacheProcessInstance(oid))
            {
               orTerm.add(new ProcessInstanceFilter(oid, false));
               fetchData = true;
            }
         }

         // Fetch the Data from Engine
         if (fetchData)
         {
           
            ProcessInstances pis = ServiceFactoryUtils.getQueryService().getAllProcessInstances(piQuery);
            if (null != pis)
            {
               for (ProcessInstance pi : pis)
               {
                  cache.put(pi.getOID(), new ProcessContextCache(this, pi));
               }
            }
         }
         
         // Return from Cache
         for (Long oid : oids)
         {
            processInstances.add(getCacheProcessInstance(oid));
         }
      }

      return processInstances;
   }

   /**
    * @param oids
    * @return
    */
   public List<ProcessInstance> getProcessInstances(List<Long> oids)
   {
      return getProcessInstances(oids, false, false);
   }
   
   /**
    * @param oid
    * @return
    */
   private ProcessInstance getCacheProcessInstance(long oid)
   {
      ProcessContextCache entry = (ProcessContextCache) cache.get(oid);
      if (null != entry)
      {
         return entry.getProcessInstance();
      }
      return null;
   }

   public boolean isCacheEnabled()
   {
      return cacheEnabled;
   }
}
