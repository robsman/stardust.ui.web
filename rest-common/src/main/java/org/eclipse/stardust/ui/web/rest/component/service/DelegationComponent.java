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

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.rest.component.exception.ExceptionHelper;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.service.ParticipantSearchComponent.PerformerTypeUI;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.DelegationRequestDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.common.exceptions.I18NException;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationHandlerBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils.ParticipantType;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * @author Yogesh.Manware
 *
 *         Assist in activity Delegation
 */
@Component
public class DelegationComponent
{
   public static final Logger trace = LogManager.getLogger(DelegationComponent.class);

   public static final String NOTE_ENABLED = "noteEnabled";
   public static final String NOTE_PARAM = "note";
   public static final String ACTIVITY_DATA = "activityData";

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;
   @Resource
   private IDelegationHandler delegationHandler;
   @Resource
   private IDelegatesProvider delegatesProvider;
   @Resource
   private IDepartmentProvider departmentDelegatesProvider;
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;
   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   @Resource
   private ExceptionHelper exceptionHelper;

   private Boolean updateNotes;
   private Boolean buildDefaultNotes;

   /**
    * @param request
    * @return
    */
   public String delegate(String request)
   {
      // convert request json to request object
      Gson gson = new GsonBuilder()
            .registerTypeAdapter(DelegationRequestDTO.class, new JsonMapDeSerializationHandler()).create();
      DelegationRequestDTO delegationReqDTO = gson.fromJson(request, DelegationRequestDTO.class);

      tuneRequestObject(delegationReqDTO);

      NotificationMap result = delegate(delegationReqDTO);

      return GsonUtils.toJsonHTMLSafeString(result);
   }

   /**
    * // Adjust request object considering injected parameters
    * 
    * @param delegationReqDTO
    */
   public void tuneRequestObject(DelegationRequestDTO delegationReqDTO)
   {
      if (this.getUpdateNotes() == null)
      {
         if (delegationReqDTO.getUpdateNotes() == null)
         {
            delegationReqDTO.setUpdateNotes(false); // default
         }
      }
      else
      {
         delegationReqDTO.setUpdateNotes(this.getUpdateNotes());
      }

      if (this.getBuildDefaultNotes() == null)
      {
         if (delegationReqDTO.getBuildDefaultNotes() == null)
         {
            delegationReqDTO.setBuildDefaultNotes(false); // default
         }
      }
      else
      {
         delegationReqDTO.setBuildDefaultNotes(this.getBuildDefaultNotes());
      }
   }

   /**
    * @param delegationReqDTO
    * @return
    */
   public NotificationMap delegate(DelegationRequestDTO delegationReqDTO)
   {
      NotificationMap notificationMap = new NotificationMap();

      // grab performer
      Participant participant = null;
      Department department = null;

      if (PerformerTypeUI.Department.name().equalsIgnoreCase(delegationReqDTO.getParticipantType()))
      {
         department = serviceFactoryUtils.getAdministrationService().getDepartment(
               Long.valueOf(delegationReqDTO.getParticipant()));
      }
      else
      {
         ParticipantType participantType = ParticipantUtils.ParticipantType.valueOf(delegationReqDTO
               .getParticipantType().toUpperCase());
         participant = ParticipantUtils.getParticipant(delegationReqDTO.getParticipant(), participantType);
      }

      Map<String, Object> params = CollectionUtils.newMap();

      params.put(NOTE_ENABLED, Boolean.valueOf(delegationReqDTO.getUpdateNotes()));

      String notes = "";
      if (delegationReqDTO.getUpdateNotes().booleanValue())
      {
         if (delegationReqDTO.getBuildDefaultNotes().booleanValue() && StringUtils.isEmpty(delegationReqDTO.getNotes()))
         {
            notes = buildDefaultNotes(participant);
         }
         params.put(NOTE_PARAM, notes);
      }

      IDelegationHandler delHandler = delegationHandler;
      if (null == delHandler)
      {
         delHandler = (IDelegationHandler) ManagedBeanUtils.getManagedBean(DelegationHandlerBean.BEAN_ID);
      }

      // OutData is only valid when delegating one Activity
      List<ActivityInstance> activityInstances = activityInstanceUtils.getActivityInstancesFor(delegationReqDTO
            .getActivities());

      if (delegationReqDTO.getActivityData() != null && activityInstances.size() == 1)
      {

         ActivityInstance activityInstance = activityInstances.get(0);

         boolean activitySuspended = false;
         try
         {
            // Perform Suspend And Save
            ActivityInstance suspendedAi = activityInstanceUtils.suspendToUserWorklist(activityInstance,
                  delegationReqDTO.getContext(), delegationReqDTO.getActivityData());
            activityInstances.set(0, suspendedAi);
            activitySuspended = true;
         }
         catch (ConcurrencyException ce)
         {
            trace.error("Unable to Delegate Activity, activity not in worklist", ce);
            String msg = restCommonClientMessages.getString("activity.concurrencyError");
            notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(), activityInstanceUtils
                  .getActivityLabel(activityInstance), msg));
         }
         catch (AccessForbiddenException af)
         {
            trace.error("User not authorized to suspend", af);
            String msg = restCommonClientMessages.getString("activity.suspend.notAuthorized.error");
            notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(), activityInstanceUtils
                  .getActivityLabel(activityInstance), msg));
         }
         catch (Exception e)
         {
            trace.error("Exception occurred while suspeding the activity", e);
            String msg = exceptionHelper.getMessageFromProvider(e, ManagedBeanUtils.getLocale(),
                  restCommonClientMessages.getString("activity.suspend.error")).getMessage();
            notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(), activityInstanceUtils
                  .getActivityLabel(activityInstance), msg));
         }

         if (!activitySuspended)
         {
            return notificationMap;
         }
      }

      Iterator<ActivityInstance> activityIterator = activityInstances.iterator();

      while (activityIterator.hasNext())
      {
         ActivityInstance activityInstance = (ActivityInstance) activityIterator.next();
         if (!delegationReqDTO.isDelegateCase() && ActivityInstanceUtils.isDefaultCaseActivity(activityInstance))
         {
            String msg = restCommonClientMessages.getString("activity.delegation.notAuthorized.error");
            notificationMap.addFailure(new NotificationDTO(activityInstance.getOID(), activityInstanceUtils
                  .getActivityLabel(activityInstance), msg));
            activityIterator.remove();
         }
      }

      if (department != null)
      {
         notificationMap.addAll(delHandler.delegateActivities(activityInstances, department, params));
      }
      // this will delegate to Case and non Case activity
      else if (null != participant && participant instanceof ParticipantInfo)
      {
         notificationMap
               .addAll(delHandler.delegateActivities(activityInstances, (ParticipantInfo) participant, params));
      }
      else
      {
         throw new I18NException(restCommonClientMessages.getString("activity.delegation.noParticipantSelected.error"));
      }
      return notificationMap;
   }
   
   
   /**
    * Performs default delegate
    * 
    * @param request
    * @return
    */
   public String performDefaultDelegate(String request)
   {
      NotificationMap notification = new NotificationMap();
      Map<String, Object> activityStatusMap = JsonDTO.getAsMap(request);

      try
      {
         WorkflowService workflowService = serviceFactoryUtils.getWorkflowService();
         for (Entry<String, Object> entry : activityStatusMap.entrySet())
         {
            int status = (Integer) entry.getValue();
            Long oid = Long.valueOf(entry.getKey());
            if (ActivityInstanceState.Application.getValue() == status)
            {
               forceSuspend(oid);
            }
            workflowService.delegateToDefaultPerformer(oid);
            notification.addSuccess(new NotificationDTO(oid, null, null));
         }
      }
      catch (Exception e)
      {
         trace.error("Error in performing default delegate", e);
         notification.addFailure(new NotificationDTO(null, null, e.getMessage()));
      }
      return GsonUtils.toJsonHTMLSafeString(notification);
   }
   
   
   /**
    * Activity instance is in Application state, force suspend will be done
    * 
    * @param ai
    */
   public  void forceSuspend(Long oid)
   {
      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();

      boolean forceSuspend = AuthorizationUtils.canForceSuspend();
      try
      {
         if (forceSuspend && adminService != null)
         {
             adminService.forceSuspendToDefaultPerformer(oid);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * if buildDefaultNotes parameter is set to true,it creates default note on delegation
    * 
    * @param participant
    * @return
    */
   private String buildDefaultNotes(Participant participant)
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();
      User loginUser = sessionCtx != null ? sessionCtx.getUser() : null;
      if (loginUser != null)
      {
         String account = loginUser.getAccount();
         Calendar cal = Calendar.getInstance();
         DateFormat formater = new SimpleDateFormat();
         String timestamp = formater.format(cal.getTime());
         String defaultNote = Localizer.getString(LocalizerKey.DELEGATE_NOTES, "DATE", timestamp);

         defaultNote = StringUtils.replace(defaultNote, "FROMUSER", account + " (" + I18nUtils.getUserLabel(loginUser)
               + ")");
         if (participant instanceof User)
         {
            User user = (User) participant;
            defaultNote = StringUtils.replace(defaultNote, "TOPARTICIPANT", user.getAccount());
         }
         else if (participant instanceof ModelParticipant)
         {
            defaultNote = StringUtils.replace(defaultNote, "TOPARTICIPANT", participant.getName());
         }
         return defaultNote;
      }
      return null;
   }

   public Boolean getUpdateNotes()
   {
      return updateNotes;
   }

   public void setUpdateNotes(Boolean updateNotes)
   {
      this.updateNotes = updateNotes;
   }

   public Boolean getBuildDefaultNotes()
   {
      return buildDefaultNotes;
   }

   public void setBuildDefaultNotes(Boolean buildDefaultNotes)
   {
      this.buildDefaultNotes = buildDefaultNotes;
   }

   public void setDelegationHandler(IDelegationHandler delegationHandler)
   {
      this.delegationHandler = delegationHandler;
   }

   public void setDepartmentDelegatesProvider(IDepartmentProvider departmentDelegatesProvider)
   {
      this.departmentDelegatesProvider = departmentDelegatesProvider;
   }
}

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
class JsonMapDeSerializationHandler implements JsonDeserializer<DelegationRequestDTO>
{
   @Override
   public DelegationRequestDTO deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
         throws JsonParseException
   {
      JsonObject request = json.getAsJsonObject();
      Map<String, Object> activityDataMap = null;
      if (request.has(DelegationComponent.ACTIVITY_DATA))
      {
         JsonObject activityData = request.remove(DelegationComponent.ACTIVITY_DATA).getAsJsonObject();
         activityDataMap = GsonUtils.processJson(activityData);
      }

      DelegationRequestDTO delegationReqDTO = GsonUtils.fromJson(request.toString(), DelegationRequestDTO.class);
      delegationReqDTO.setActivityData(activityDataMap);

      return delegationReqDTO;
   }
}