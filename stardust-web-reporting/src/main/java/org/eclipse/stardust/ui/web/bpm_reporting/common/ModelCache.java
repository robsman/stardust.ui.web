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
package org.eclipse.stardust.ui.web.bpm_reporting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.servlet.ServletContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Resetable;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.LocalizerCache;


/**
 * @author Yogesh.Manware
 * TODO: review and move to the common place later
 *
 */
public class ModelCache implements Resetable, Serializable
{
   private final static long serialVersionUID = 1l;

   private final List<DeployedModelDescription> modelDescriptions;

   private final Map<Long, DeployedModel> cache;

   private final static String MODEL_CACHE_ID = "carnotBbm/modelCache";

   private final static Logger trace = LogManager.getLogger(ModelCache.class);

   private final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

   private Activity defaultCaseActivity;

   private ProcessDefinition caseProcessDefination;

   private DeployedModel predefinedModel;

   private SessionContext sessionContext;

   public ModelCache(SessionContext sessionContext)
   {
      this.sessionContext = sessionContext;
      this.modelDescriptions = CollectionUtils.newList();
      this.cache = CollectionUtils.newHashMap();
      if (null == modelDescriptions || null == cache)
      {
         throw new OutOfMemoryError();
      }
      reset();
   }

   /**
    * 
    * @return
    */

   public Map<Long, DeployedModel> getCache()
   {

      Map<Long, DeployedModel> tempCache = null;
      try
      {
         lock.readLock().lock();// acquire read lock-all
         tempCache = cache;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      finally
      {
         lock.readLock().unlock();// release read lock
      }

      return tempCache;
   }

   @SuppressWarnings("unchecked")
   public static ModelCache getModelCache(SessionContext sessionContext2, ServletContext servletContext)
   {
      String partitionId = sessionContext2.getUser().getPartitionId();

      Map<String, ModelCache> modelCacheMap = (Map<String, ModelCache>) servletContext.getAttribute(MODEL_CACHE_ID);

      if (null == modelCacheMap)
      {
         modelCacheMap = CollectionUtils.newMap();
         servletContext.setAttribute(MODEL_CACHE_ID, modelCacheMap);
      }
      if (modelCacheMap.containsKey(partitionId))
      {
         return modelCacheMap.get(partitionId);
      }
      else
      {
         ModelCache partitionCache = new ModelCache(sessionContext2);
         modelCacheMap.put(partitionId, partitionCache);
         return partitionCache;
      }
   }

   public boolean isValueBindingNullable()
   {
      // keep model cache
      return false;
   }

   /**
    * method remove unused DeployedModel objects from cache and also remove if model's
    * active status change.
    * 
    * @param models
    */
   private Set<Long> findStaleCacheData(List<DeployedModelDescription> models)
   {
      Set<Long> unusedModels = new HashSet<Long>();
      // remove all deleted models from model cache
      for (Long modelOID : getCache().keySet())
      {
         boolean isStale = true;
         for (DeployedModelDescription modelDesc : models)
         {
            if (modelDesc.getModelOID() == modelOID && (modelDesc.isActive() == getCache().get(modelOID).isActive()))
            {
               isStale = false;
               break;
            }
         }
         if (isStale)
         {
            unusedModels.add(modelOID);

         }

      }
      return unusedModels;
   }

   /**
    * method reset cache if DeployedModel's active status changed ,model deleted or
    * override etc.
    */
   public void reset()
   {
      ServiceFactory serviceFactory = sessionContext.getServiceFactory();
      if (serviceFactory != null)
      {
         try
         {
            Models models = serviceFactory.getQueryService().getModels(DeployedModelQuery.findAll());
            // creating temp cache so that actual map should not modified
            Map<Long, DeployedModel> tempCache = new HashMap<Long, DeployedModel>(getCache());
            // 1)find stale model's Id removing from temp map
            Set<Long> unusedModels = findStaleCacheData(models);
            if (!unusedModels.isEmpty())
            {
               tempCache.keySet().removeAll(unusedModels);
            }

            Map<Long, DeployedModel> updatedModels = new HashMap<Long, DeployedModel>();

            for (DeployedModelDescription modelDesc : models)
            {

               DeployedModel cachedModel = tempCache.get(new Long(modelDesc.getModelOID()));
               // 2) update models (if any model updated then remove old cache value and
               // add new model in cache
               if (cachedModel != null)
               {
                  Date deploymentDate = cachedModel.getDeploymentTime();
                  if (deploymentDate != null && deploymentDate.getTime() != modelDesc.getDeploymentTime().getTime())
                  {
                     DeployedModel model = serviceFactory.getQueryService().getModel(modelDesc.getModelOID(), false);
                     updatedModels.put(new Long(modelDesc.getModelOID()), model);
                  }
               }// 3) add new model if it is not preset in cache
               else
               {
                  DeployedModel model = serviceFactory.getQueryService().getModel(modelDesc.getModelOID(), false);
                  updatedModels.put(new Long(modelDesc.getModelOID()), model);

                  if (PredefinedConstants.PREDEFINED_MODEL_ID.equals(model.getId()))
                  {
                     predefinedModel = model;
                     caseProcessDefination = model.getProcessDefinition(PredefinedConstants.CASE_PROCESS_ID);
                     defaultCaseActivity = caseProcessDefination
                           .getActivity(PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID);
                  }

               }

            }
            // 4)clear old modelDescriptions and add newly fetched data
            if (modelDescriptions.size() > 0)
            {
               modelDescriptions.clear();
               trace.debug("ModelCache.reset ::Model descriptor cleaned ");
            }
            modelDescriptions.addAll(models);

            if (CollectionUtils.isNotEmpty(updatedModels) || CollectionUtils.isNotEmpty(unusedModels))
            {
               // 5)remove stale/deleted models and add updated models
               lock.writeLock().lock();// acquire write lock-all read and write thread
                                       // will wait
               if (CollectionUtils.isNotEmpty(unusedModels))
               {
                  getCache().keySet().removeAll(unusedModels);
               }
               if (CollectionUtils.isNotEmpty(updatedModels))
               {
                  getCache().putAll(updatedModels);
               }
               LocalizerCache.reset();
            }

            tempCache = null;
            models = null;
            updatedModels = null;
            unusedModels = null;

         }
         finally
         {
            // 6)release write lock
            if (lock.isWriteLockedByCurrentThread())
            {
               lock.writeLock().unlock();
            }
         }
      }

   }

   @Deprecated
   public Model getActiveModel()
   {
      Model result = null;

      for (Iterator<DeployedModelDescription> i = modelDescriptions.iterator(); i.hasNext();)
      {
         DeployedModelDescription modelDescr = i.next();
         if (modelDescr.isActive())
         {
            result = getModel(modelDescr.getModelOID());
            break;
         }
      }

      return result;
   }

   /**
    * if grant is available prefer using getActiveModel(Grant grant) instead of this
    * method
    * 
    * @param id
    * @return
    */
   public DeployedModel getActiveModel(String id)
   {
      DeployedModel result = null;

      List<DeployedModel> activeModels = getActiveModels();
      for (DeployedModel model : activeModels)
      {
         if (model.getId().equals(id))
         {
            result = getModel(model.getModelOID());
            break;
         }
      }

      return result;
   }

   /**
    * returns grant's associated model and for predefined ADMIN role, it returns any of
    * the available model
    * 
    * @param grant
    * @return
    */
   public DeployedModel getActiveModel(Grant grant)
   {
      DeployedModel result = null;
      String id = grant.getNamespace();
      List<DeployedModel> activeModels = getActiveModels();
      if (null != id)
      {

         for (DeployedModel model : activeModels)
         {
            if (model.getId().equals(id))
            {
               result = getModel(model.getModelOID());
               break;
            }
         }
      }
      else if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(grant.getId()) && !activeModels.isEmpty())
      {
         result = activeModels.get(0);
      }
      return result;
   }

   public List<DeployedModel> getActiveModels()
   {
      List<DeployedModel> result = CollectionUtils.newList();

      for (DeployedModelDescription modelDescr : modelDescriptions)
      {
         if (modelDescr.isActive())
         {
            result.add(getModel(modelDescr.getModelOID()));
         }
      }

      return Collections.unmodifiableList(result);
   }

   public Collection<DeployedModel> getAllModels()
   {
      List<DeployedModel> allModels = new ArrayList<DeployedModel>();
      allModels.addAll(getCache().values());

      return Collections.unmodifiableCollection(allModels);
   }

   public Collection<Participant> getAllParticipants()
   {
      Set<Participant> allParticipants = new HashSet<Participant>();
      Set<String> part = new HashSet<String>();
      for (Iterator<DeployedModel> mIter = getAllModels().iterator(); mIter.hasNext();)
      {
         Model model = mIter.next();
         for (@SuppressWarnings("unchecked")
         Iterator<Participant> pIter = model.getAllParticipants().iterator(); pIter.hasNext();)
         {
            Participant p = pIter.next();
            if (!part.contains(p.getQualifiedId()))
            {
               part.add(p.getQualifiedId());
               allParticipants.add(p);
            }
         }
      }
      return Collections.unmodifiableCollection(allParticipants);
   }

   @Deprecated
   public Participant getParticipant(String id)
   {
      return getParticipant(id, null);
   }

   public Participant getParticipant(String id, @SuppressWarnings("rawtypes") Class type)
   {
      Participant p = null;
      for (Iterator<DeployedModel> mIter = getAllModels().iterator(); mIter.hasNext() && p == null;)
      {
         DeployedModel model = mIter.next();
         if (Organization.class.isInstance(type))
         {
            p = model.getOrganization(id);
         }
         else if (Role.class.isInstance(type))
         {
            p = model.getRole(id);
         }
         else
         {
            p = model.getParticipant(id);
         }
      }
      return p;
   }

   public void updateModel(long oid)
   {
      Map<Long, DeployedModel> modelCache = getCache();
      ServiceFactory serviceFactory = sessionContext.getServiceFactory();
      DeployedModel model = serviceFactory.getQueryService().getModel(oid, false);
      modelCache.put(new Long(oid), model);
   }

   /**
    * @return default Activity of Predefined Model
    * 
    */
   public Activity getDefaultCaseActivity()
   {
      return defaultCaseActivity;
   }

   /**
    * @return Case ProcessDefinition of Predefined Model
    * 
    */
   public ProcessDefinition getCaseProcessDefination()
   {
      return caseProcessDefination;
   }

   /**
    * 
    * @return DeployedModel
    */

   public DeployedModel getPredefinedModel()
   {
      return predefinedModel;
   }

   public DeployedModel getModel(long oid)
   {
      Map<Long, DeployedModel> modelCache = getCache();
      DeployedModel model = modelCache.get(new Long(oid));

      if (null == model && oid != 0)
      {
         // just sticking to a model description is much cheaper for production audit
         // trails, as alive is very costly to be evaluated
         ServiceFactory serviceFactory = sessionContext.getServiceFactory();
         try
         {
            model = serviceFactory.getQueryService().getModel(oid, false);
         }
         catch (ObjectNotFoundException e)
         {
            trace.error("unable to resolve model", e);
         }

         if (model != null)// update modelDescriptions
         {
            modelCache.put(new Long(oid), model);
            boolean contains = false;
            for (DeployedModelDescription desc : modelDescriptions)
            {
               if (desc.getModelOID() == oid)
               {
                  contains = true;
                  break;
               }
            }
            if (!contains)
            {
               modelDescriptions.add(model);
            }
         }

      }
      return model;
   }
}
