/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.rest.component.exception.ExceptionHelper;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.springframework.stereotype.Component;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 */
@Component
public class DelegationHandlerBean implements IDelegationHandler
{
   private static final long serialVersionUID = 8478350143579783691L;

   public final static String BEAN_ID = "portalDefaultDelegationHandler";

   protected final static Logger trace = LogManager.getLogger(DelegationHandlerBean.class);

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   @Resource
   private ProcessInstanceUtils ProcessInstanceUtilsREST;

   @Resource
   private ExceptionHelper exceptionHelper;

   public NotificationMap delegateActivities(List<ActivityInstance> activities, ParticipantInfo toParticipantInfo,
         Map<String, Object> params)
   {
      if (activities == null)
      {
         throw new I18NException(restCommonClientMessages.getString("activity.delegation.noActivities.error"));
      }
      if (toParticipantInfo == null)
      {
         throw new I18NException(restCommonClientMessages.getString("activity.delegation.noParticipantSelected.error"));
      }

      return delegateActivitiesToParticipant(activities, toParticipantInfo, params);
   }

   public NotificationMap delegateActivities(List<ActivityInstance> activities, Department department,
         Map<String, Object> params)
   {
      if (activities == null)
      {
         throw new I18NException(restCommonClientMessages.getString("activity.delegation.noActivities.error"));
      }
      if (department == null)
      {
         throw new I18NException(restCommonClientMessages.getString("activity.delegation.noParticipantSelected.error"));
      }

      return delegateActivitiesToDepartment(activities, department, params);
   }

   /**
    * @param activities
    * @param department
    * @param params
    * @return
    */
   private NotificationMap delegateActivitiesToDepartment(List<ActivityInstance> activities, Department department,
         Map<String, Object> params)
   {
      NotificationMap notificationMap = new NotificationMap();

      WorkflowService workflowService = serviceFactoryUtils.getWorkflowService();
      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();

      String notes = (String) params.get(DelegationBean.NOTE_PARAM);
      boolean notesEnabled = ((Boolean) params.get(DelegationBean.NOTE_ENABLED)).booleanValue();
      
      for (Iterator<ActivityInstance> aiIter = activities.iterator(); aiIter.hasNext();)
      {
         ActivityInstance ai = aiIter.next();
         // Refetch AI to get the latest state
         ai = activityInstanceUtils.getActivityInstance(ai.getOID());

         try
         {
            // Get the current performer for the AI
            ParticipantInfo currentPerformer = ai.getCurrentPerformer();

            if (ai.getProcessInstance().isCaseProcessInstance())
            {
               Organization org = department.getOrganization();
               ModelParticipantInfo scopedParticipantInfo = department.getScopedParticipant((ModelParticipant) org);

               workflowService.delegateCase(ai.getProcessInstanceOID(), scopedParticipantInfo);
               notificationMap.addSuccess(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai),
                     ""));
               continue;
            }

            else if (currentPerformer instanceof ModelParticipantInfo)
            {
               // Get the ModelParticipant for with the current performer
               ModelParticipantInfo modelParticipantInfo = (ModelParticipantInfo) currentPerformer;
               Participant participant = ModelUtils.getModelCache().getParticipant(modelParticipantInfo.getId(), null);
               if (participant instanceof ModelParticipant)
               {
                  if (!suspendActivity(workflowService, adminService, ai, notificationMap))
                     continue;

                  ModelParticipantInfo scopedParticipantInfo = department
                        .getScopedParticipant((ModelParticipant) participant);
                  ai = workflowService.delegateToParticipant(ai.getOID(), scopedParticipantInfo);

                  if (notesEnabled)
                  {
                     setNotes(workflowService, ai.getProcessInstanceOID(), notes);
                  }
                  notificationMap.addSuccess(new NotificationDTO(ai.getOID(), activityInstanceUtils
                        .getActivityLabel(ai), ""));
               }
            }
         }
         catch (AccessForbiddenException e)
         {
            trace.error("User does not have the permission to Delegate the Activity", e);
            String msg = restCommonClientMessages.getString("activity.delegation.notAuthorized.error");
            notificationMap
                  .addFailure(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai), msg));
         }
         catch (ConcurrencyException e)
         {
            trace.error("Unable to Delegate Activity, activity not in worklist", e);
            String msg = restCommonClientMessages.getString("activity.concurrencyError");
            notificationMap
                  .addFailure(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai), msg));
         }
         catch (Exception e)
         {
            trace.error("Unable to Delegate Activity", e);
            String msg = exceptionHelper.getMessageFromProvider(e, ManagedBeanUtils.getLocale(),
                  restCommonClientMessages.getString("activity.delegation.error")).getMessage();
            notificationMap
                  .addFailure(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai), msg));
         }
      }
      return notificationMap;
   }

   /**
    * @param workflowService
    * @param processInstanceOid
    * @param notes
    */
   protected void setNotes(WorkflowService workflowService, long processInstanceOid, String notes)
   {
      try
      {
         if (!StringUtils.isEmpty(notes))
         {
            ProcessInstance pi = ProcessInstanceUtilsREST.getProcessInstance(processInstanceOid);
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
         trace.error("Unable to set the note for the specified process instance OID " + processInstanceOid, e);
      }
   }

   /**
    * @param activityInstanceOids
    * @param participant
    * @param params
    * @return
    */
   protected NotificationMap delegateActivitiesToParticipant(List<ActivityInstance> activityInstanceOids,
         ParticipantInfo participant, Map<String, Object> params)
   {
      NotificationMap notificationMap = new NotificationMap();

      WorkflowService workflowService = serviceFactoryUtils.getWorkflowService();
      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();

      if (activityInstanceOids != null && workflowService != null)
      {
         String notes = (String) params.get(DelegationBean.NOTE_PARAM);
         boolean notesEnabled = ((Boolean) params.get(DelegationBean.NOTE_ENABLED)).booleanValue();
         for (Iterator<ActivityInstance> aiIter = activityInstanceOids.iterator(); aiIter.hasNext();)
         {
            ActivityInstance ai = aiIter.next();
            // Refetch AI to get the latest state
            ai = activityInstanceUtils.getActivityInstance(ai.getOID());

            try
            {
               if (!suspendActivity(workflowService, adminService, ai, notificationMap))
                  continue;

               if (ai.getProcessInstance().isCaseProcessInstance())
               {
                  workflowService.delegateCase(ai.getProcessInstanceOID(), participant);
               }

               else if (participant instanceof User)
               {
                  ai = workflowService.delegateToUser(ai.getOID(), ((User) participant).getOID());
               }
               else if (participant instanceof ModelParticipant)
               {
                  ModelParticipant modelParticipant = (ModelParticipant) participant;
                  // findCorrectModelParticipant(ai, participant);
                  ai = workflowService
                        .delegateToParticipant(ai.getOID(), ((ModelParticipant) modelParticipant).getId());
               }
               if (notesEnabled)
               {
                  setNotes(workflowService, ai.getProcessInstanceOID(), notes);
               }
               notificationMap.addSuccess(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai),
                     ""));
            }
            catch (AccessForbiddenException e)
            {
               trace.error("User does not have the permission to Delegate the Activity", e);
               String msg = restCommonClientMessages.getString("activity.delegation.notAuthorized.error");
               notificationMap.addFailure(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai),
                     msg));
            }
            catch (ConcurrencyException e)
            {
               trace.error("Unable to Delegate Activity, activity not in worklist", e);
               String msg = restCommonClientMessages.getString("activity.concurrencyError");
               notificationMap.addFailure(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai),
                     msg));
            }
            catch (Exception e)
            {
               trace.error("Unable to Delegate Activity", e);
               String msg = exceptionHelper.getMessageFromProvider(e, ManagedBeanUtils.getLocale(),
                     restCommonClientMessages.getString("activity.delegation.error")).getMessage();
               notificationMap.addFailure(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai),
                     msg));
            }
         }
      }
      return notificationMap;
   }

   /**
    * @param workflowService
    * @param adminService
    * @param ai
    * @param notificationMap
    * @return
    */
   protected boolean suspendActivity(WorkflowService workflowService, AdministrationService adminService,
         ActivityInstance ai, NotificationMap notificationMap)
   {
      try
      {
         if (ai.getState() == ActivityInstanceState.Application)
         {
            forceSuspend(adminService, ai);
         }
         else
         {
            normalSuspend(workflowService, ai);
         }
      }
      catch (Exception e)
      {
         trace.error(e);
         String msg = exceptionHelper.getMessageFromProvider(e, ManagedBeanUtils.getLocale(),
               restCommonClientMessages.getString("activity.suspend.error")).getMessage();
         notificationMap.addFailure(new NotificationDTO(ai.getOID(), activityInstanceUtils.getActivityLabel(ai), msg));
         return false;
      }
      return true;
   }

   /**
    * @param workflowService
    * @param ai
    */
   protected void normalSuspend(WorkflowService workflowService, ActivityInstance ai)
   {
      if (ai.isAssignedToUser() && ActivityInstanceState.Application.equals(ai.getState()))
      {
         workflowService.suspendToUser(ai.getOID(), ai.getUserPerformerOID());
      }
   }

   /**
    * @param adminService
    * @param ai
    */
   protected void forceSuspend(AdministrationService adminService, ActivityInstance ai)
   {
      if (ai != null)
      {
         boolean forceSuspend = AuthorizationUtils.canForceSuspend();
         if (forceSuspend && adminService != null)
         {
            ai = adminService.forceSuspendToDefaultPerformer(ai.getOID());
         }
      }
   }
}
