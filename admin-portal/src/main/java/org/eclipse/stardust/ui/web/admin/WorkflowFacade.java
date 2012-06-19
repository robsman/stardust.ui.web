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
package org.eclipse.stardust.ui.web.admin;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.eclipse.stardust.common.config.ParametersFacade;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.LogEntries;
import org.eclipse.stardust.engine.api.query.LogEntryQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.UserGroupQuery;
import org.eclipse.stardust.engine.api.query.UserGroups;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Resetable;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;





public class WorkflowFacade implements Resetable, Serializable
{
   private final static long serialVersionUID = 1l;
   
   protected static final Logger trace = LogManager.getLogger(WorkflowFacade.class);
   
   private WebDesktopModel model;
   
   public WorkflowFacade()
   {
      try
      {
         Context context = new InitialContext();
         Context environment = (Context) context.lookup("java:comp/env");
         ParametersFacade.setGlobalContext(environment, "web");
      }
      catch (NamingException e)
      {
         trace.warn("", e);
      }
   }
   
//   public void login(String account, String password)
//   {
//      setServiceFactory(ServiceFactoryLocator.get(account, password));
//   }
   
   public long getActiveActivityInstancesCount()
   {
      synchronized (model)
      {
         return model.getActiveActivityInstancesCount();
      }
   }
   
   public long getTotalUsersCount()
   {
      synchronized (model)
      {
         return model.getTotalUsersCount();
      }
   }

   public long getActiveUsersCount()
   {
      synchronized (model)
      {
         return model.getActiveUsersCount();
      }
   }

   public long getTotalUserGroupsCount()
   {
      synchronized (model)
      {
         return model.getTotalUserGroupsCount();
      }
   }

   public long getActiveUserGroupsCount()
   {
      synchronized (model)
      {
         return model.getActiveUserGroupsCount();
      }
   }

   public long getTotalProcessInstancesCount()
   {
      synchronized (model)
      {
         return model.getTotalProcessInstancesCount();
      }
   }

   public long getActiveProcessInstancesCount()
   {
      synchronized (model)
      {
         return model.getActiveProcessInstancesCount();
      }
   }

   public long getPendingProcessInstancesCount()
   {
      synchronized (model)
      {
         return model.getPendingProcessInstancesCount();
      }
   }

   public long getInterruptedProcessInstancesCount()
   {
      synchronized (model)
      {
         return model.getInterruptedProcessInstancesCount();
      }
   }

   public long getCompletedProcessInstancesCount()
   {
      synchronized (model)
      {
         return model.getCompletedProcessInstancesCount();
      }
   }

   public long getAbortedProcessInstancesCount()
   {
      synchronized (model)
      {
         return model.getAbortedProcessInstancesCount();
      }
   }

   public long getTotalActivityInstancesCount()
   {
      synchronized (model)
      {
         return model.getTotalActivityInstancesCount();
      }
   }

   public long getPendingActivityInstancesCount()
   {
      synchronized (model)
      {
         return model.getPendingActivityInstancesCount();
      }
   }

   public long getCompletedActivityInstancesCount()
   {
      synchronized (model)
      {
         return model.getCompletedActivityInstancesCount();
      }
   }

   public long getAbortedActivityInstancesCount()
   {
      synchronized (model)
      {
         return model.getAbortedActivityInstancesCount();
      }
   }

   public long getInterruptedActivitiyInstancesCount()
   {
      synchronized (model)
      {
         return model.getInterruptedActivitiyInstancesCount();
      }
   }

   public long getSuspendedActivityInstancesCount()
   {
      synchronized (model)
      {
         return model.getSuspendedActivityInstancesCount();
      }
   }

   public long getHibernatedActivityInstancesCount()
   {
      synchronized (model)
      {
         return model.getHibernatedActivityInstancesCount();
      }
   }
   
   public void setServiceFactory(ServiceFactory serviceFactory)
   {
      clear();
      model = new WebDesktopModel(serviceFactory);
   }

   /**
    * @return The service factory.
    */
   public ServiceFactory getServiceFactory()
   {
      return SessionContext.findSessionContext().getServiceFactory();
   }

   private void clear()
   {
      model = null;
   }
   
   public LogEntries getAllLogEntries(LogEntryQuery query)
   {
      return model.getServiceFactory().getQueryService().getAllLogEntries(query);
   }
   
   public ProcessInstances getAllProcessInstances(ProcessInstanceQuery query)
   {
      return model.getServiceFactory().getQueryService().getAllProcessInstances(query);
   }
   
   public ActivityInstances getAllActivitiesEntries(ActivityInstanceQuery query)
   {
      return model.getServiceFactory().getQueryService().getAllActivityInstances(query);
   }
   
   public Users getAllUsers(UserQuery query)
   {
      return model.getServiceFactory().getQueryService().getAllUsers(query);
   }
   
   public UserGroups getAllUserGroups(UserGroupQuery query)
   {
      return model.getServiceFactory().getQueryService().getAllUserGroups(query);
   }
   
   public List getAllDaemons()
   {
      AdministrationService service = model.getServiceFactory().getAdministrationService();
      return service.getAllDaemons(true);
   }
   
   public List<ProcessDefinition> getCumulatedProcessDefinitions()
   {
      Set<String> pdSet = new HashSet<String>();
      List<ProcessDefinition> pds = new ArrayList<ProcessDefinition>();
      ModelCache modelCache = ModelCache.findModelCache();
      if(modelCache != null)
      {
         Iterator<DeployedModel> modelIter = modelCache.getAllModels().iterator();
         while(modelIter.hasNext())
         {
            Model model = modelIter.next();
            Iterator pdIter = model.getAllProcessDefinitions().iterator();
            while(pdIter.hasNext())
            {
               ProcessDefinition pd = (ProcessDefinition) pdIter.next();
               if(!pdSet.contains(pd.getId()))
               {
                  pdSet.add(pd.getId());
                  pds.add(pd);
               }
            }
         }
      }
      return Collections.unmodifiableList(pds);
   }
   
   public QueryService getQueryService()
   {
      return model.getServiceFactory().getQueryService();
   }

//   public ProcessInstance abortProcessInstance(long oid)
//   {
//      return model.getServiceFactory().getAdministrationService().abortProcessInstance(oid);
//   }
//   
//   public void abortActivity(ActivityInstance activity, AbortScope abortScope)
//   {
//      model.getServiceFactory().getWorkflowService().abortActivityInstance(
//            activity.getOID(), abortScope);
//   }
//   
//   public ProcessInstance recoverProcessInstance(long oid)
//   {
//      return model.getServiceFactory().getAdministrationService().recoverProcessInstance(oid);
//   }
   
   public List getAllParticipants()
   {
      return model.getServiceFactory().getQueryService().getAllParticipants();
   }

   public boolean isValueBindingNullable()
   {
      return false;
   }

   public void reset()
   {
      if(model != null)
      {
         model.reset();
      }
   }
}
