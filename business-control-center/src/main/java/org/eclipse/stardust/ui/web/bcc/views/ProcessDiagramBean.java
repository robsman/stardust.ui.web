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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.RuntimeObject;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.bcc.jsf.InvalidServiceException;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ProcessDiagramBean extends PopupUIComponentBean implements ResourcePaths
{
   private static final long serialVersionUID = -5438797603265752843L;

   public final static boolean diagramSupported;

   protected final static String DATA_MODEL = "carnotBcDiagram/dataModel";

   protected final static String PROCESS_INSTANCE = "carnotBcDiagram/processInstance";

   private InstanceStack instanceData;

   private SessionContext sessionCtx;

   private ProcessInstance processInstance;

   private Long processInstanceOID;

   static
   {
      diagramSupported = !StringUtils.isEmpty(BusinessControlCenterConstants
            .getDiagramUrl(new String[0]));
   }

   /**
    * 
    */
   public ProcessDiagramBean()
   {
      super(V_processDiagramView);
      sessionCtx = SessionContext.findSessionContext();

      View focusView = PortalApplication.getInstance().getFocusView();
      String pOID = focusView.getParamValue("processInstanceOId");
      if (!StringUtils.isEmpty(pOID))
         processInstanceOID = Long.parseLong(pOID);
      initialize();
   }

   @Override
   public void initialize()
   {
      if (processInstanceOID == null)
         return;
      ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
      query.getFilter().add(
            ProcessInstanceQuery.OID.isEqual(processInstanceOID.longValue()));
      try
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         Iterator< ? > pIter = facade.getAllProcessInstances(query).iterator();
         if (pIter.hasNext())
         {
            processInstance = (ProcessInstance) pIter.next();
         }
      }
      catch (InvalidServiceException e)
      {
      }
      if (processInstance != null)
      {
         Pair allInstances = getAllProcessAndActivityInstances(processInstance);
         instanceData = buildProcessHierarchy(processInstance, allInstances);
         sessionCtx.bind(DATA_MODEL, instanceData);
         sessionCtx.bind(PROCESS_INSTANCE, processInstance);
      }
      else
      {
         instanceData = (InstanceStack) sessionCtx.lookup(DATA_MODEL);
      }

   }

   /**
    * Moves process up
    * 
    * @param event
    */
   public void moveProcessUp(ActionEvent event)
   {
      if (!isRootProcess())
      {
         instanceData.getNextParent();
      }
   }

   /**
    * Moves process down
    * 
    * @param event
    */
   public void moveProcessDown(ActionEvent event)
   {
      if (!isNoSubProcessAvailable())
      {
         instanceData.getNextChild();
      }
   }

   /**
    * @param processInstance
    * @param allInstances
    * @return
    */
   private InstanceStack buildProcessHierarchy(ProcessInstance processInstance,
         Pair allInstances)
   {
      List pis = allInstances != null ? (List) allInstances.getFirst() : null;
      List ais = allInstances != null ? (List) allInstances.getSecond() : null;

      ProcessInstance pi = processInstance;
      Stack parentStack = new Stack();
      long startingAiOid = ((ProcessInstanceDetails) pi).getStartingActivityInstanceOID();
      Iterator aiIter = ais.iterator();
      while (aiIter.hasNext() && startingAiOid != 0)
      {
         ActivityInstance ai = (ActivityInstance) aiIter.next();
         if (ai.getOID() == startingAiOid)
         {
            if (ImplementationType.SubProcess.equals(ai.getActivity()
                  .getImplementationType()))
            {
               pi = (ProcessInstance) getRuntimeObjectFromList(
                     ai.getProcessInstanceOID(), pis);

               parentStack.push(new Pair(pi, ai));
               startingAiOid = ((ProcessInstanceDetails) pi)
                     .getStartingActivityInstanceOID();
               aiIter = ais.iterator();
            }
         }
      }
      return new InstanceStack(parentStack, processInstance);
   }

   /**
    * @param oid
    * @param runtimeObjects
    * @return
    */
   private RuntimeObject getRuntimeObjectFromList(long oid, List runtimeObjects)
   {
      for (Iterator roIter = runtimeObjects.iterator(); roIter.hasNext();)
      {
         RuntimeObject ro = (RuntimeObject) roIter.next();
         if (ro.getOID() == oid)
         {
            return ro;
         }
      }
      return null;
   }

   /**
    * @param processInstance
    * @return
    */
   private Pair getAllProcessAndActivityInstances(ProcessInstance processInstance)
   {
      Pair pair = null;
      if (processInstance != null)
      {
         long rootProcessOid = processInstance.getRootProcessInstanceOID();
         ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
         query.getFilter().and(
               ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(rootProcessOid));

         ActivityInstanceQuery aiQuery = new ActivityInstanceQuery();
         aiQuery.where(new ProcessInstanceFilter(rootProcessOid, true));

         if (processInstance.getOID() != rootProcessOid
               && sessionCtx.isSessionInitialized())
         {
            QueryService queryService = sessionCtx.getServiceFactory().getQueryService();
            pair = new Pair(queryService.getAllProcessInstances(query), queryService
                  .getAllActivityInstances(aiQuery));
         }
         else
         {
            List<ProcessInstance> processList = new ArrayList<ProcessInstance>(1);
            processList.add(processInstance);
            pair = new Pair(processList, new ArrayList());
         }
      }
      return pair;
   }

   // ********************** Modified Getter and Setter methods *****************
   public ProcessInstance getCurrentProcessInstance()
   {
      return instanceData != null ? instanceData.getCurrentProcessInstance() : null;
   }

   public ActivityInstance getCurrentActivityInstance()
   {
      return instanceData != null
            ? instanceData.getCurrentStartingActivityInstance()
            : null;
   }

   public String getDiagramUrl()
   {
      ProcessInstance pi = getCurrentProcessInstance();
      if (pi != null)
      {
         List<String> params = new ArrayList<String>();
         params.add("processInstanceOid=" + pi.getOID());
         params.add("CurrentTime=" + new Date().getTime());
         params.add("partitionId=" + UserUtils.getPartitionID());
         params.add("realmId=" + UserUtils.getRealmId());

         ActivityInstance startingAi = getCurrentActivityInstance();
         if (startingAi != null)
         {
            params.add("activityInstanceOid=" + startingAi.getOID());
         }
         return BusinessControlCenterConstants.getDiagramUrl((String[]) params
               .toArray(new String[0]));
      }
      return "";
   }

   // ************************* Default Getter & Setter Methods***********************
   public boolean isRootProcess()
   {
      return instanceData != null ? !instanceData.hasParent() : true;
   }

   public boolean isNoSubProcessAvailable()
   {
      return instanceData != null ? !instanceData.hasChild() : true;
   }

   public boolean isDiagramSupported()
   {
      return diagramSupported;
   }

   /**
    * @author Ankita.Patel
    * @version $Revision: $
    */
   public static class InstanceStack
   {
      private Stack parentItems;

      private Pair currentItem;

      private Stack childItems;

      public InstanceStack(Stack initParentStack, Pair currentInstance)
      {
         parentItems = initParentStack;
         childItems = new Stack();
         currentItem = currentInstance;
      }

      public InstanceStack(Stack initParentStack, ProcessInstance processInstance)
      {
         this(initParentStack, new Pair(processInstance, null));
      }

      public InstanceStack(Stack initParentStack, ProcessInstance currentPI,
            ActivityInstance currentAI)
      {
         this(initParentStack, new Pair(currentPI, currentAI));
      }

      public Pair getNextParent()
      {
         Pair pair = null;
         if (hasParent())
         {
            pair = (Pair) parentItems.pop();
            childItems.push(currentItem);
            currentItem = pair;
         }
         return pair;
      }

      public boolean hasParent()
      {
         return !(parentItems.empty());
      }

      public Pair getNextChild()
      {
         Pair pair = null;
         if (hasChild())
         {
            pair = (Pair) childItems.pop();
            parentItems.push(currentItem);
            currentItem = pair;
         }
         return pair;
      }

      public boolean hasChild()
      {
         return !(childItems.empty());
      }

      public ProcessInstance getCurrentProcessInstance()
      {
         return currentItem != null ? (ProcessInstance) currentItem.getFirst() : null;
      }

      public ActivityInstance getCurrentStartingActivityInstance()
      {
         return currentItem != null ? (ActivityInstance) currentItem.getSecond() : null;
      }
   }

}
