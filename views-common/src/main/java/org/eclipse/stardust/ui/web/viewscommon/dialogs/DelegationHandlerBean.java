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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.FacesException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



public class DelegationHandlerBean implements IDelegationHandler
{
   private static final long serialVersionUID = 8478350143579783691L;

   private List dataModelIds;

   public final static String BEAN_ID = "portalDefaultDelegationHandler";

   protected final static Logger trace = LogManager
         .getLogger(DelegationHandlerBean.class);
   private MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
   
   public List<ActivityInstance> delegateActivities(List<ActivityInstance> activities, Participant toParticipant,
         Map<String, Object> params) throws FacesException
   {
      return delegateActivities(activities, (ParticipantInfo) toParticipant, params);
   }

   public List<ActivityInstance> delegateActivities(List<ActivityInstance> activities,
         ParticipantInfo toParticipantInfo, Map<String, Object> params) throws FacesException
   {
      if (toParticipantInfo != null && activities != null)
      {
         try
         {
            return delegateActivitiesToParticipant(activities, toParticipantInfo, params);
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE, e);
         }
         finally
         {
            SessionContext context = SessionContext.findSessionContext();
            if (context != null && dataModelIds != null)
            {
               Iterator dataModelIter = dataModelIds.iterator();
               while (dataModelIter.hasNext())
               {
                  String id = (String) dataModelIter.next();
                  context.bind(id, null);
               }
            }
         }
      }

      return null;
   }   
   
   public List<ActivityInstance> delegateActivities(List<ActivityInstance> activities, Department department,
         Map<String, Object> params) throws FacesException
   {
      if (department != null && activities != null)
      {
         try
         {
            return delegateActivitiesToDepartment(activities, department, params);
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE, e);
         }
         finally
         {
            SessionContext context = SessionContext.findSessionContext();
            if (context != null && dataModelIds != null)
            {
               Iterator dataModelIter = dataModelIds.iterator();
               while (dataModelIter.hasNext())
               {
                  String id = (String) dataModelIter.next();
                  context.bind(id, null);
               }
            }
         }
      }

      return null;
   }

   private List<ActivityInstance> delegateActivitiesToDepartment(List<ActivityInstance> activities,
         Department department, Map<String, Object> params)
   {
      List<ActivityInstance> delegatedActivities = CollectionUtils.newList();
      SessionContext sessionCtx = SessionContext.findSessionContext();

      WorkflowService workflowService = sessionCtx != null
            && sessionCtx.isSessionInitialized() ? sessionCtx.getServiceFactory()
            .getWorkflowService() : null;

      AdministrationService adminService = sessionCtx != null && sessionCtx.isSessionInitialized() ? sessionCtx
            .getServiceFactory().getAdministrationService() : null;

      if (activities != null && workflowService != null)
      {
         String notes = (String) params.get(DelegationBean.NOTE_PARAM);
         boolean notesEnabled = ((Boolean) params.get(DelegationBean.NOTE_ENABLED))
               .booleanValue();
         for (Iterator<ActivityInstance> aiIter = activities.iterator(); aiIter
               .hasNext();)
         {
            ActivityInstance ai = aiIter.next();
            // Refetch AI to get the latest state
            ai = ActivityInstanceUtils.getActivityInstance(ai.getOID());

            try
            {
               // Get the current performer for the AI
               ParticipantInfo currentPerformer = ai.getCurrentPerformer();
               
               if (ai.getProcessInstance().isCaseProcessInstance())
               {
                  Organization org = department.getOrganization();
                  ModelParticipantInfo scopedParticipantInfo = department.getScopedParticipant((ModelParticipant) org);

                  workflowService.delegateCase(ai.getProcessInstanceOID(), scopedParticipantInfo);
                  delegatedActivities.add(ai);
                  continue;
               }
               
               else if (currentPerformer instanceof ModelParticipantInfo)
               {
                  // Get the ModelParticipant for with the current performer
                  ModelParticipantInfo modelParticipantInfo = (ModelParticipantInfo) currentPerformer;
                  Participant participant = ModelUtils.getModelCache().getParticipant(
                        modelParticipantInfo.getId());
                  if (participant instanceof ModelParticipant)
                  {
                     if (!suspendActivity(workflowService, adminService, ai))
                        continue;

                     ModelParticipantInfo scopedParticipantInfo = department
                           .getScopedParticipant((ModelParticipant) participant);
                     ai = workflowService.delegateToParticipant(ai.getOID(),
                           scopedParticipantInfo);

                     if (notesEnabled)
                     {
                        setNotes(workflowService, ai.getProcessInstanceOID(), notes);
                     }
                     delegatedActivities.add(ai);
                  }
               }
            }
            catch (AccessForbiddenException e)
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                     new PortalException(PortalErrorClass.DELEGATE_FORBIDDEN, e));
               trace.error("User does not have the permission to Delegate the Activity", e);
            }
            catch (ConcurrencyException e)
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                     new PortalException(PortalErrorClass.UNABLE_TO_DELEGATE_ACTIVITY_NOT_IN_WORKLIST, e));
               trace.error("Unable to Delegate Activity", e);
            }
            catch (Exception e)
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                     new PortalException(PortalErrorClass.UNABLE_TO_DELEGATE_ACTIVITY, e));
               trace.error("Unable to Delegate Activity", e);
            }
         }
      }
      return delegatedActivities;
   }

   protected void setNotes(WorkflowService workflowService, long processInstanceOid,
         String notes)
   {
      try
      {
         if (!StringUtils.isEmpty(notes))
         {
            ProcessInstance pi = ProcessInstanceUtils.getProcessInstance(processInstanceOid, true);
            if (pi.getOID() != pi.getScopeProcessInstanceOID())
            {
               pi = workflowService.getProcessInstance(pi.getScopeProcessInstanceOID());
            }
            ProcessInstanceAttributes pia = pi.getAttributes();
            pia.addNote(notes);
            workflowService.setProcessInstanceAttributes(pia);
         }
      }
      catch (Exception e)
      {
         trace.error("Unable to set the note for the specified process instance OID "
               + processInstanceOid, e);
      }
   }

   protected List<ActivityInstance> delegateActivitiesToParticipant(
         List<ActivityInstance> activityInstanceOids, ParticipantInfo participant,
         Map<String, Object> params)
   {
      List<ActivityInstance> delegatedActivities = CollectionUtils.newList();
      SessionContext sessionCtx = SessionContext.findSessionContext();

      WorkflowService workflowService = sessionCtx != null
            && sessionCtx.isSessionInitialized() ? sessionCtx.getServiceFactory()
            .getWorkflowService() : null;

      AdministrationService adminService = sessionCtx != null && sessionCtx.isSessionInitialized() ? sessionCtx
            .getServiceFactory().getAdministrationService() : null;

      if (activityInstanceOids != null && workflowService != null)
      {
         String notes = (String) params.get(DelegationBean.NOTE_PARAM);
         boolean notesEnabled = ((Boolean) params.get(DelegationBean.NOTE_ENABLED))
               .booleanValue();
         for (Iterator<ActivityInstance> aiIter = activityInstanceOids.iterator(); aiIter
               .hasNext();)
         {
            ActivityInstance ai = aiIter.next();
            // Refetch AI to get the latest state
            ai = ActivityInstanceUtils.getActivityInstance(ai.getOID());

            try
            {
               if (!suspendActivity(workflowService, adminService, ai))
                  continue;
               
               if (ai.getProcessInstance().isCaseProcessInstance())
               {
                  workflowService.delegateCase(ai.getProcessInstanceOID(), participant);
               }
               
               else if (participant instanceof User)
               {
                  ai = workflowService.delegateToUser(ai.getOID(), ((User) participant)
                        .getOID());
               }
               else if (participant instanceof ModelParticipant)
               {
                  ModelParticipant modelParticipant = (ModelParticipant) participant;
                  // findCorrectModelParticipant(ai, participant);
                  ai = workflowService.delegateToParticipant(ai.getOID(),
                        ((ModelParticipant) modelParticipant).getId());
               }
               if (notesEnabled)
               {
                  setNotes(workflowService, ai.getProcessInstanceOID(), notes);
               }
               delegatedActivities.add(ai);
            }
            catch (AccessForbiddenException e)
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                     new PortalException(PortalErrorClass.DELEGATE_FORBIDDEN, e));
               trace.error("User does not have the permission to Delegate the Activity", e);
            }
            catch (ConcurrencyException e)
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                     new PortalException(PortalErrorClass.UNABLE_TO_DELEGATE_ACTIVITY_NOT_IN_WORKLIST, e));
               trace.error("Unable to Delegate Activity", e);
            }
            catch (Exception e)
            {
               ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE,
                     new PortalException(PortalErrorClass.UNABLE_TO_DELEGATE_ACTIVITY, e));
               trace.error("Unable to Delegate Activity", e);
            }
         }
      }
      return delegatedActivities;
   }

   protected boolean suspendActivity(WorkflowService workflowService, AdministrationService adminService,
         ActivityInstance ai)
   {
      if (ai.getState() == ActivityInstanceState.Application)
      {
         try
         {
            forceSuspend(adminService, ai);
         }
         catch (Exception e)
         {
            MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getParamString("delegation.error",
                  String.valueOf(ai.getOID())));
            return false;
         }
      }
      else
      {
         try
         {
            normalSuspend(workflowService, ai);
         }
         catch (AccessForbiddenException af)
         {
            MessageDialog.addErrorMessage(propsBean.getParamString("delegation.notAuthorizedonSuspend.message", af.getMessage()));
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      return true;
   }

   protected void normalSuspend(WorkflowService workflowService, ActivityInstance ai)
   {
      if (ai.isAssignedToUser() && ActivityInstanceState.Application.equals(ai.getState()))
      {
         workflowService.suspendToUser(ai.getOID(), ai.getUserPerformerOID());
      }
   }

   protected void forceSuspend(AdministrationService adminService, ActivityInstance ai)
   {
      if (ai != null)
      {
         DeployedModel model=  ModelCache.findModelCache().getModel(ai.getModelOID());  
         boolean forceSuspend = AuthorizationUtils.canForceSuspend();
         if (forceSuspend && adminService != null)
         {
            ai = adminService.forceSuspendToDefaultPerformer(ai.getOID());
         }
      }
   }

   public void setDataModelIds(List dataModelIds)
   {
      this.dataModelIds = dataModelIds;
   }
}
