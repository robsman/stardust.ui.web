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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;


public class ProcessHistoryDataModel implements IProcessHistoryDataModel, Serializable
{
   private static final long serialVersionUID = 1L;

   private transient QueryService queryService;
   private SessionContext sessionCtx;
   private boolean restricted;

   /**
    * 
    */
   public ProcessHistoryDataModel()
   {
      super();
      sessionCtx = SessionContext.findSessionContext();
      queryService = sessionCtx.getServiceFactory().getQueryService();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryDataModel#getActivityDataModel(org.eclipse.stardust.engine.api.runtime.ProcessInstance, java.util.List, boolean)
    */
   public IProcessHistoryTableEntry getActivityDataModel(ProcessInstance processInstance,
         List<ProcessInstance> processInstances, boolean includeEvents)
   {
      List<ActivityInstance> activityInstances = getAllActivities(processInstance, includeEvents);

      return buildActivityHierarchy(processInstance, processInstances, activityInstances);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryDataModel#getAllProcesses(org.eclipse.stardust.engine.api.runtime.ProcessInstance, boolean)
    */
   public List<ProcessInstance> getAllProcesses(ProcessInstance process, boolean includeEvents)
   {
      if (sessionCtx.isSessionInitialized() && (process != null))
      {
         long rootProcessOid = process.getRootProcessInstanceOID();
         ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
         query.getFilter().and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(rootProcessOid));
         query.orderBy(ProcessInstanceQuery.START_TIME);

         if (includeEvents)
         {
            query.setPolicy(HistoricalEventPolicy.ALL_EVENTS);
         }

         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

         ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.Default);
         processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_HIERARCHY_INFO);
         query.setPolicy(processInstanceDetailsPolicy);

         return queryService.getAllProcessInstances(query);
      }

      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryDataModel#getCurrentProcessByActivityInstance(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public ProcessInstance getCurrentProcessByActivityInstance(ActivityInstance activityInstance)
   {
      ProcessInstance currentProcessInstance = null;

      if (activityInstance != null)
      {
         long processOid = activityInstance.getProcessInstanceOID();
         ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
         query.getFilter().and(ProcessInstanceQuery.OID.isEqual(processOid));

         ProcessInstances pis = queryService.getAllProcessInstances(query);
         Iterator<ProcessInstance> piIter = pis.iterator();
         currentProcessInstance = piIter.hasNext() ? piIter.next() : null;
      }

      return currentProcessInstance;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryDataModel#getProcessHistoryDataModel(org.eclipse.stardust.engine.api.runtime.ActivityInstance, java.util.List, boolean)
    */
   public IProcessHistoryTableEntry getProcessHistoryDataModel(ActivityInstance activityInstance,
         List<ProcessInstance> processInstances, boolean includeEvents)
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();

      if (sessionCtx.isSessionInitialized() && (activityInstance != null))
      {
         queryService = sessionCtx.getServiceFactory().getQueryService();

         ProcessInstance processInstance = getCurrentProcessByActivityInstance(activityInstance);

         return getProcessHistoryDataModel(processInstance, processInstances, includeEvents);

      }
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.process.history.IProcessHistoryDataModel#getProcessHistoryDataModel(org.eclipse.stardust.engine.api.runtime.ProcessInstance, java.util.List, boolean)
    */
   public IProcessHistoryTableEntry getProcessHistoryDataModel(ProcessInstance processInstance,
         List<ProcessInstance> processInstances, boolean includeEvents)
   {
      IProcessHistoryTableEntry rootProcess = null;

      if (sessionCtx.isSessionInitialized())
      {
         if ((processInstances != null) && !processInstances.isEmpty())
         {
            ProcessInstance rootInstance = getRootProcessInstance(processInstances, processInstance);
            rootProcess = buildProcessHierarchy(rootInstance,rootInstance, processInstances);

            if (rootProcess != null)
            {
               rootProcess.setNodePathToActivityInstance(true);
               setProcessInstancePath(rootProcess, processInstance);
            }
         }
      }

      return rootProcess;
   }

   public boolean isDataModelRestricted()
   {
      return restricted;
   }

   /**
    * @param processInstance
    * @param processInstances
    * @param allActivity
    * @return
    */
   @SuppressWarnings("unchecked")
   private IProcessHistoryTableEntry buildActivityHierarchy(ProcessInstance processInstance,
         List<ProcessInstance> processInstances, List<ActivityInstance> allActivity)
   {
      IProcessHistoryTableEntry root = new ActivityInstanceHistoryItem(processInstance, null,
            new ArrayList<IProcessHistoryTableEntry>());

      Map<Long, ProcessInstance> startingActivityToProcessMap = new HashMap<Long, ProcessInstance>();

      for (ProcessInstance subProcess : processInstances)
      {
         ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) subProcess;
         startingActivityToProcessMap.put(processInstanceDetails.getStartingActivityInstanceOID(), subProcess);
      }

      for (ActivityInstance activityInstance : allActivity)
      {
         if (startingActivityToProcessMap.containsKey(activityInstance.getOID()))
         {
            IProcessHistoryTableEntry currentNode = new ActivityInstanceHistoryItem(processInstance, activityInstance,
                  new ArrayList<IProcessHistoryTableEntry>());
            root.getChildren().add(currentNode);

            IProcessHistoryTableEntry procesNode = new ProcessInstanceHistoryItem(startingActivityToProcessMap
                  .get(activityInstance.getOID()),processInstance, null);
            currentNode.getChildren().add(procesNode);
         }
         else
         {
            IProcessHistoryTableEntry currentNode = new ActivityInstanceHistoryItem(processInstance, activityInstance);
            root.getChildren().add(currentNode);
         }
      }

      return root;
   }

   /**
    * Build process hierarchy
    * 
    * @param currentProcess
    * @param allProcess
    * @return
    */
  private IProcessHistoryTableEntry buildProcessHierarchy(ProcessInstance currentProcess,ProcessInstance rootProcessInstance,
         List<ProcessInstance> allProcess)
   {
      List<IProcessHistoryTableEntry> childs = new ArrayList<IProcessHistoryTableEntry>();

      // process root's child nodes
      for (ProcessInstance processInstance : allProcess)
      {
         if (processInstance.getParentProcessInstanceOid() == currentProcess.getOID())
         {
            IProcessHistoryTableEntry entry = buildProcessHierarchy(processInstance,rootProcessInstance, allProcess);
            childs.add(entry);
         }
      }

      return new ProcessInstanceHistoryItem(currentProcess,rootProcessInstance, childs);
   }

   /**
    * @param processInstance
    * @param includeEvents
    * @return
    */
   private List<ActivityInstance> getAllActivities(ProcessInstance processInstance, boolean includeEvents)
   {
      if (sessionCtx.isSessionInitialized())
      {
         ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
         ProcessInstanceFilter processFilter = new ProcessInstanceFilter(processInstance.getOID(), false);
         aiQuery.where(processFilter);
         aiQuery.orderBy(ActivityInstanceQuery.START_TIME).and(ActivityInstanceQuery.OID);

         if (includeEvents)
         {
            aiQuery.setPolicy(HistoricalEventPolicy.ALL_EVENTS);
         }

         return queryService.getAllActivityInstances(aiQuery);
      }

      return null;
   }

   /**
    * @param processInstances
    * @param currentInstance
    * @return
    */
   private ProcessInstance getRootProcessInstance(List<ProcessInstance> processInstances,
         ProcessInstance currentInstance)
   {
      for (ProcessInstance instance : processInstances)
      {
         if (currentInstance.getRootProcessInstanceOID() == instance.getOID())
         {
            return instance;
         }
      }

      return null;
   }

   /**
    * @param rootProcess
    * @param targetPi
    * @return
    */
   @SuppressWarnings("unchecked")
   private boolean setProcessInstancePath(IProcessHistoryTableEntry rootProcess, ProcessInstance targetPi)
   {
      boolean disclose = false;

      if ((rootProcess != null) && (targetPi != null))
      {
         List childList = rootProcess.getChildren();

         if (childList != null)
         {
            for (int index = 0; (index < childList.size()) && !disclose; index++)
            {
               IProcessHistoryTableEntry item = (IProcessHistoryTableEntry) childList.get(index);

               if (item.getRuntimeObject() instanceof ProcessInstance
                     && (targetPi.getOID() == item.getRuntimeObject().getOID()))
               {
                  item.setNodePathToActivityInstance(true);

                  return true;
               }
               disclose = setProcessInstancePath(item, targetPi);

               if (disclose)
               {
                  item.setNodePathToActivityInstance(true);
               }
            }
         }
      }
      return disclose;
   }
}