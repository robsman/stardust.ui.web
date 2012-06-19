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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ConditionalPerformer;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.TaskAssignmentConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.provider.DefaultAssemblyLineActivityProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.provider.IAssemblyLineActivityProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IDescriptorFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.SpiConstants;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpiUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.WorklistUtils;


/**
 * 
 * @author mgille
 * 
 */
public abstract class AbstractProcessExecutionPortal implements Resetable
{
   private static final Logger trace = LogManager.getLogger(AbstractProcessExecutionPortal.class);
   
   public static final String EMPTY_PANEL = "<Empty Panel>";

   public static final String GENERIC_PANEL = "<Generic Interaction Panel>";  
   
   public final static int ROLE_WORKLIST = 2;
   
   private ActivityInstance currentActivityInstance;
   private boolean pushService;
   private Set<String> participantsId;
   private Set<String> allAssemblyLineParticipants = new HashSet<String>();
   

   private Boolean isUserAdmin;
   
   

   private int targetWorklistForSuspend;

   private IDescriptorFilter descriptorFilter;
   
   private String currentParticipantId;
   
   protected abstract void onCurrentActivityInstanceChanged();

   public AbstractProcessExecutionPortal()
   {
      targetWorklistForSuspend = ROLE_WORKLIST;      
   }
   
   public ServiceFactory getServiceFactory()
   {
      SessionContext sessionContext = SessionContext.findSessionContext();
      return (null != sessionContext) ? sessionContext.getServiceFactory() : null;
   }

  
   
   protected void logInfo(Object object)
   {
      trace.info(object);
   }
   
  

   /**
    * 
    * @return
    */
   public ActivityInstance getCurrentActivityInstance()
   {
      return currentActivityInstance;
   }
   
   private void setWorkMode()
   {
      Boolean assemblyLineModus  = Boolean.FALSE;
      Object mode = null; 
      if(currentActivityInstance != null)
      {
         ModelParticipant participant = currentActivityInstance.getActivity().
            getDefaultPerformer();
         if (participant instanceof ConditionalPerformer)
         {
            ConditionalPerformer cp = (ConditionalPerformer) participant;
            Participant p = cp.getResolvedPerformer();
            mode = p.getAttribute(
                  TaskAssignmentConstants.ASSIGNMENT_MODE);
         }
         else
         {
            mode = participant != null ? participant.getAllAttributes().get(
                  TaskAssignmentConstants.ASSIGNMENT_MODE) : null;
         }
         assemblyLineModus = mode != null &&
         TaskAssignmentConstants.WORK_MODE_ASSEMBLY_LINE.equals(mode) ? 
               Boolean.TRUE : Boolean.FALSE;
         if(!Parameters.instance().getBoolean(
               ProcessPortalConstants.ASSEMBLY_LINE_MODE_ENABLED, true))
         {
            assemblyLineModus = Boolean.FALSE;
         }
//         Map<String, Object> param = RequestContext.getCurrentInstance().getPageFlowScope();
//         param.put(ProcessportalConstants.ASSEMBLY_LINE_MODE, assemblyLineModus);
      }
   }

   public void setCurrentActivityInstance(ActivityInstance activityInstance)
   {
      currentActivityInstance = activityInstance;

      onCurrentActivityInstanceChanged();

      setWorkMode();
   }

   public Model getActiveModel()
   {
      return ModelCache.findModelCache().getActiveModel();
   }
   
   public Collection<DeployedModel> getAllModels()
   {
      return ModelCache.findModelCache().getAllModels();
   }
   
   public List getStartableProcessDefinitions()
   {
      WorkflowService ws = getWorkflowService();
      if(ws != null)
      {
         return ws.getStartableProcessDefinitions();
      }
      return Collections.EMPTY_LIST;
   }

   

   public boolean isActiveModel()
   {
      return null != ModelCache.findModelCache().getActiveModel();
   }

 

   /**
    * 
    * @return
    */
   public List getCumulatedWorklistItems()
   {
      WorkflowService ws = getWorkflowService();
      if(ws != null)
      {
         return ws.getWorklist(WorklistQuery.findCompleteWorklist())
            .getCumulatedItems();
      }
      return Collections.EMPTY_LIST;
   }

   public List getPrivateWorkListItems()
   {
      WorkflowService ws = getWorkflowService();
      if(ws != null)
      {
         return ws.getWorklist(WorklistQuery.findPrivateWorklist())
            .getCumulatedItems();
      }
      return Collections.EMPTY_LIST;
   }

   public List<ProcessDefinition> getCumulatedProcesses()
   {
      return getCumulatedProcesses(true);
   }

   public List<ProcessDefinition> getCumulatedProcesses(boolean filterEnabled)
   {
      Map<String, ProcessDefinition> map = new TreeMap<String, ProcessDefinition>();
      Iterator<DeployedModel> modelIter = ModelCache.findModelCache().getAllModels()
            .iterator();
      User currentUser = SessionContext.findSessionContext().getUser();
      Set<String> workshopParticipants = Collections.emptySet();
      if(currentUser != null)
      {
         workshopParticipants = WorklistUtils.categorizeParticipants(currentUser).workshopParticipants;
      }
      while (modelIter.hasNext())
      {
         Model model = modelIter.next();
         for (Iterator i = model.getAllProcessDefinitions().iterator(); i.hasNext();)
         {
            ProcessDefinition pd = (ProcessDefinition) i.next();
            if(!map.containsKey(pd.getId()) &&
                  hasProcessPerformingActivity(pd, workshopParticipants))
            {
               map.put(pd.getId(), pd);
            }
         }
      }
      return new ArrayList<ProcessDefinition>(map.values());
   }
   
   protected boolean hasProcessPerformingActivity(ProcessDefinition pd, Set participants)
   {
      Iterator aIter = pd.getAllActivities().iterator();
      while (aIter.hasNext())
      {
         Activity activity = (Activity) aIter.next();
         if(activity.isInteractive())
         {
            ModelParticipant performer = activity.getDefaultPerformer();
            if(performer instanceof ConditionalPerformer ||
                  participants.contains(performer.getId()))
            {
               return true;
            }
         }
      }
      return false;
   }
   public boolean isWithAssemblyLineAssignments()
   {
      return !WorklistUtils.getAssemblyLineAssignmentParticipants().isEmpty();
   }
   
   private IAssemblyLineActivityProvider getAssemblyLineActivityProvider()
   {
      IAssemblyLineActivityProvider aiProvider = null;

      Object providerHandle = Parameters.instance().get(
            SpiConstants.ASSEMBLY_LINE_ACTIVITY_PROVIDER);
      if (providerHandle instanceof IAssemblyLineActivityProvider)
      {
         aiProvider = (IAssemblyLineActivityProvider) providerHandle;
      }
      else if (providerHandle instanceof String)
      {
         try
         {
            aiProvider = (IAssemblyLineActivityProvider) Reflect.createInstance((String) providerHandle);
         }
         catch (Exception e)
         {
            trace.info("Failed instantiating assembly line activity provider '"
                  + providerHandle + "'.", e);
         }
      }
      
      if (null == aiProvider)
      {
         aiProvider = new DefaultAssemblyLineActivityProvider();
      }
      return aiProvider;
   }
   
 
   
   public void setPushService(boolean enable)
   {
      pushService = enable;
   }
   
   public boolean isPushServiceEnabled()
   {
      return pushService;
   }

   public ActivityInstance activateNextAssemblyLineActivity(Set<String> participantIds) throws PortalException
   {
      try
      {
         this.participantsId = participantIds;
         IAssemblyLineActivityProvider aiProvider = getAssemblyLineActivityProvider();
         ActivityInstance ai = aiProvider.getNextAssemblyLineActivity(this, participantIds);
         if (null != ai)
         {
            // after successful lock, perform full activation
            activateActivity(ai.getOID());

            return ai;
         }
      }
      catch (PortalException pe)
      {
         throw pe;
      }
      catch (Exception e)
      {
         throw new PortalException(
               ProcessPortalErrorClass.UNABLE_TO_ACTIVATE_ACTIVITY, e);
      }
      
      return null;
   }
   
   private void setNextPushedActivity() throws PortalException
   {
      if(isPushServiceEnabled())
      {
         setCurrentActivityInstance(
               activateNextAssemblyLineActivity(participantsId));
      }
   }
   
 
   
  

   /**
    * @throws PortalException
    * 
    * 
    */
   public void suspendActivity() throws PortalException
   {
      try
      {
         logInfo("Suspend activity");

         WorkflowService ws = getWorkflowService();
         if(ws != null)
         {
            if(targetWorklistForSuspend == ROLE_WORKLIST)
            {
               if (currentParticipantId == null)
               {
                  ws.suspendToDefaultPerformer(currentActivityInstance.getOID());
               }
               else
               {
                  ws.suspendToParticipant(currentActivityInstance.getOID(),
                        currentParticipantId);
               }
            }
            else
            {
               ws.suspendToUser(currentActivityInstance.getOID());
            }
         }

         if(!isPushServiceEnabled())
         {
            setCurrentActivityInstance(null);
         }
         else
         {
            setNextPushedActivity();
         }

         onCurrentActivityInstanceChanged();
      }
      catch (Exception e)
      {
         throw new PortalException(
               ProcessPortalErrorClass.UNABLE_TO_SUSPEND_ACTIVITY, e);
      }
   }   

   /**
    * 
    * @param oid
    * @throws PortalException
    */
   public void activateActivity(long oid) throws PortalException
   {
      try
      {
         WorkflowService ws = getWorkflowService();
         if(ws != null)
         {
            ActivityInstance ai = ws.getActivityInstance(oid);
            currentParticipantId = ai != null ? ai.getParticipantPerformerID() : null;
            setCurrentActivityInstance(ws.activate(oid));
            obtainApplicationInParameters();
            onCurrentActivityInstanceChanged();
         }
      }
      catch (AccessForbiddenException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new PortalException(
               ProcessPortalErrorClass.UNABLE_TO_ACTIVATE_ACTIVITY, e);
      }
   }

   
   
   
  

   /**
    * 
    * @return
    */
   public boolean getAllowsAbort()
   {
      if (currentActivityInstance != null)
      {
         return currentActivityInstance.getActivity().isAbortable();
      }
      else
      {
         return false;
      }
   }

 

   /**
    * 
    * @return
    */

   public WorkflowService getWorkflowService()
   {
      ServiceFactory sf = getServiceFactory();
      return sf != null ? sf.getWorkflowService() : null;
   }

   public QueryService getQueryService()
   {
      ServiceFactory sf = getServiceFactory();
      return sf != null ? sf.getQueryService() : null;
   }

   public UserService getUserService()
   {
      ServiceFactory sf = getServiceFactory();
      return sf != null ? sf.getUserService() : null;
   }
   
   public boolean isUserAdministrator()
   {
      if (isUserAdmin == null)
      {
         isUserAdmin = new Boolean(false);
         SessionContext sessionCtx = SessionContext.findSessionContext();
         User user = sessionCtx != null ? sessionCtx.getUser() : null;
         Iterator grants = user != null ? user.getAllGrants().iterator() : null;
         if(grants != null)
         {
            while (grants.hasNext())
            {
               Grant grant = (Grant) grants.next();
   
               if (grant.getQualifiedId().equals(PredefinedConstants.ADMINISTRATOR_ROLE))
               {
                  isUserAdmin = Boolean.TRUE;
                  break;
               }
            }
         }
      }
      return isUserAdmin.booleanValue();
   }

   protected void obtainApplicationInParameters() throws PortalException
   {
      ActivityInstance activityInstance = getCurrentActivityInstance();
      Activity activity = activityInstance != null
            ? activityInstance.getActivity()
            : null;
      Long activityOID = activityInstance != null
            ? new Long(activityInstance.getOID())
            : null;

      WorkflowService ws = getWorkflowService();
            
      IActivityInteractionController interactionController = SpiUtils.getInteractionController(activity);
      if (null != interactionController)
      {
         String contextId = interactionController.getContextId(activityInstance);

         Map inData = (null != ws) //
               ? ws.getInDataValues(activityOID.longValue(), contextId, null)
               : Collections.EMPTY_MAP;

         interactionController.initializePanel(activityInstance, inData);
      }
      else
      {
         logInfo("Did not find an interaction controller for the current activity instance.");
      }
   }

   protected abstract void onChangeActivityList();

   public boolean isValueBindingNullable()
   {
      return false;
   }

   public void reset()
   {
      ActivityInstance activity = getCurrentActivityInstance();
      if (activity != null)
      {
         try
         {
            suspendActivity();
         }
         catch (PortalException e)
         {
           // ExceptionHandler.handleException(PortalBackingBean.GLOBAL_PORTAL_MESSAGE_ID, e);
         }
      }
      allAssemblyLineParticipants.clear();
      isUserAdmin = null;
   }
  
   public Set<String> getAllAssemblyLineParticipants()
   {
      if(allAssemblyLineParticipants.isEmpty())
      {
         allAssemblyLineParticipants = 
            WorklistUtils.getAssemblyLineAssignmentParticipants();
      }
      return allAssemblyLineParticipants;
   }

   public int getTargetWorklistForSuspend()
   {
      return targetWorklistForSuspend;
   }

   public void setTargetWorklistForSuspend(int targetWorklistForSuspend)
   {
      this.targetWorklistForSuspend = targetWorklistForSuspend;
   }
   
  
   
 
}
