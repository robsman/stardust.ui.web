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

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.HistoricalState;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ContextData;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.model.utils.ActivityReportUtils;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Subodh.Godbole
 */
public class ActivityInstanceUtils
{
   private static final Logger trace = LogManager.getLogger(ActivityInstanceUtils.class);

   private static final String STATUS_PREFIX = "views.activityTable.statusFilter.";
   public static final String IMAGE_BASE_PATH = "/plugins/views-common/images/icons/process-history/";
   private static final Map<String, String> iconMap;
   
   static
   {
      iconMap = new LinkedHashMap<String, String>();
      iconMap.put("ApplicationActivity", "activity_application.png");
      iconMap.put("ManualActivity", "activity_manual.png");
      iconMap.put("Auxiliary", "activity_auxiliary.png");
      iconMap.put("Delegate", "delegate.png");
      iconMap.put("Exception", "exception.png");
      iconMap.put("ActivityCompleted", "activity_completed.png");
      iconMap.put("StateChange", "activity_state.png");
      iconMap.put("ProcessInstance", "processrootblue.png");
      iconMap.put("SubProcess", "/plugins/views-common/images/icons/process.png");
      iconMap.put("AuxiliaryProcess", "process_auxiliary.png");
      iconMap.put("CaseInstance", "/plugins/views-common/images/icons/envelope.png");
      iconMap.put("activityQAFailed", Constants.ACTIVITY_QA_FAILED_IMAGE);
      iconMap.put("activityQAPassed", Constants.ACTIVITY_QA_PASSED_IMAGE);
      iconMap.put("activityQAAwait", Constants.ACTIVITY_QA_AWAIT_IMAGE);
      iconMap.put("Note", "/plugins/views-common/images/icons/mime-types/notes-filled.png");
   }
   
   /**
    * @param event
    */
   public static void openActivity(ActionEvent event)
   {
      Map<String, String> param = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      Long oid = Long.parseLong(param.get("oid"));

      ActivityInstance ai = getActivityInstance(oid);
      openActivity(ai);
   }

   /**
    * @param ai
    */
   public static void openActivity(ActivityInstance ai)
   {
      openActivity(ai, null);
   }

   /**
    * @param ai
    * @param viewParams
    */
   public static void openActivity(ActivityInstance ai, Map<String, Object> viewParams)
   {
      openActivity(ai, viewParams, -1);
   }
   
   /**
    * @param ai
    * @param viewParams
    * @param viewIndex
    */
   public static void openActivity(ActivityInstance ai, Map<String, Object> viewParams, int viewIndex)
   {
      if (null != ai)
      {
         if (!isSupportsWeb(ai.getActivity()))
         {
            MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getString(
                  "views.common.notSupportedOnWeb"));
            return;
         }
         if (ActivityInstanceUtils.isDefaultCaseActivity(ai))
         {
            Map<String, Object> params = CollectionUtils.newTreeMap();
            params.put("processInstanceOID", String.valueOf(ai.getProcessInstanceOID()));
            
            PortalApplication.getInstance().openViewById("caseDetailsView",
                  "processInstanceOID=" + String.valueOf(ai.getProcessInstanceOID()), params, null, false);
            
            return;
         }

         if (!isActivatable(ai))
         {
            MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance()
                  .getString("views.common.notActivatable"));
            return;
         }

         ActivityInstance activatedAi = activate(ai);
         if (null != activatedAi)
         {

            Map<String, Object> params = CollectionUtils.newTreeMap();
            if (CollectionUtils.isNotEmpty(viewParams))
            {
               params.putAll(viewParams);
            }

            params.put(ActivityInstance.class.getName(), activatedAi);
            params.put("oid", Long.toString(activatedAi.getOID()));
            params.put("activityName", I18nUtils.getActivityName(activatedAi.getActivity()));

            PortalApplication.getInstance().openViewById("activityPanel", "oid=" + activatedAi.getOID(), params, null,
                  false, viewIndex);

         }
      }
   }

   /**
    * @param ai
    * @return
    */
   public static boolean isActivatable(ActivityInstance ai)
   {
      if (isSupportsWeb(ai.getActivity()))
      {
         if (ai.getState().equals(ActivityInstanceState.Application)
               || ai.getState().equals(ActivityInstanceState.Suspended))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * @param activity
    * @return
    */
   public static boolean isSupportsWeb(Activity activity)
   {
      IActivityInteractionController interactionController = SpiUtils.getInteractionController(activity);
      return (interactionController != null);
   }

   /**
    * @param ai
    * @return
    */
   public static boolean isIframeBased(ActivityInstance ai)
   {
      IActivityInteractionController interactionController = SpiUtils.getInteractionController(ai.getActivity());
      if (null != interactionController)
      {
         String contextId = interactionController.getContextId(ai);
         if (PredefinedConstants.DEFAULT_CONTEXT.equals(contextId))
         {
            return false;
         }
         else
         {
            return true;
         }
      }
      
      return false;
   }

   /**
    * @param activity
    * @return
    */
   public static IActivityInteractionController getInteractionController(Activity activity)
   {
      IActivityInteractionController interactionController = SpiUtils.getInteractionController(activity);

      if (SpiUtils.DEFAULT_MANUAL_ACTIVITY_CONTROLLER == interactionController
            && activity.getApplicationContext(PredefinedConstants.JFC_CONTEXT) != null)
      {
         // fix bug in SpiUtils
         interactionController = null;
      }

      return interactionController;
   }

   /**
    * @param ai
    * @return
    */
   public static boolean isDelegable(ActivityInstance ai)
   {
      boolean delegable = !(ActivityInstanceState.Completed.equals(ai.getState()) || ActivityInstanceState.Aborted
            .equals(ai.getState()));
      ImplementationType implType = ai.getActivity().getImplementationType();
      delegable = delegable
            && ((ImplementationType.Application.equals(implType) && ai.getActivity().isInteractive()) || ImplementationType.Manual
                  .equals(implType)) && AuthorizationUtils.hasDelegatePermission(ai);
      return delegable;
   }

   /**
    * @param ai
    * @return
    */
   public static boolean isAbortable(ActivityInstance ai)
   {
      boolean abortable = !ActivityInstanceState.Aborted.equals(ai.getState())
            && !ActivityInstanceState.Completed.equals(ai.getState()) && AuthorizationUtils.hasAbortPermission(ai);
      return abortable;
   }

   /**
    * Gets the last performer for the ActivityInstance
    * 
    * @param ai
    * @return
    */
   public static String getLastPerformer(ActivityInstance ai)
   {
      for (HistoricalState hs : ai.getHistoricalStates())
      {
         Participant performer = hs.getPerfomer();
         if (performer instanceof User && hs.getState() == ActivityInstanceState.Application)
         {
            return I18nUtils.getUserLabel((User) performer);
         }
      }
      return null;
   }
   /**
    * returns assigned to performer name
    * 
    * @param ai
    * @return
    */
   public static String getAssignedToLabel(ActivityInstance ai)
   {
      String performerName = null;
      if (ai.isAssignedToUser())
      {
         UserInfo userInfo = (UserInfo) ai.getCurrentPerformer();
         User user = ServiceFactoryUtils.getUserService().getUser(userInfo.getId());
         performerName = I18nUtils.getUserLabel(user);
      }
      else
      {
         if (ai.getCurrentPerformer() != null)
         {
            Participant participant = ParticipantUtils.getParticipant(ai.getCurrentPerformer());
            if (null != participant)
            {
               performerName = I18nUtils.getParticipantName(participant);
            }
         }
         if (null == performerName)
         {
            performerName = ai.getParticipantPerformerName();
         }
      }
      return performerName;
   }

   /**
    * 
    * @param ais
    */
   public static void delegateToDefaultPerformer(List<ActivityInstance> ais)
   {
      if (null != ais)
      {
         try
         {
            WorkflowService workflowService = ServiceFactoryUtils.getWorkflowService();
            for (ActivityInstance activityInstance : ais)
            {
               if (ActivityInstanceState.Application.equals(activityInstance.getState()))
               {
                  forceSuspend(activityInstance);
               }
               workflowService.delegateToDefaultPerformer(activityInstance.getOID());
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
   }

   /**
    * Activity instance is in Application state, force suspend will be done
    * 
    * @param ai
    */
   public static void forceSuspend(ActivityInstance ai)
   {
      AdministrationService adminService = ServiceFactoryUtils.getAdministrationService();
      DeployedModel model = ModelCache.findModelCache().getModel(ai.getModelOID());

      boolean forceSuspend = AuthorizationUtils.canForceSuspend();
      try
      {
         if (forceSuspend && adminService != null)
         {
            ai = adminService.forceSuspendToDefaultPerformer(ai.getOID());
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param oid
    * @return
    */
   public static ActivityInstance getActivityInstance(long oid)
   {
      ActivityInstance ai = null;
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      query.where(ActivityInstanceQuery.OID.isEqual(oid));
      ActivityInstances ais = ServiceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (!ais.isEmpty())
      {
         ai = ais.get(0);
      }

      return ai;
   }

   public static ActivityInstance getActivityInstance(ProcessInstance pi)
   {
      ActivityInstance ai = null;
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      ProcessInstanceFilter processFilter = new ProcessInstanceFilter(pi.getOID(), false);
      aiQuery.where(processFilter);
      ActivityInstances ais=ServiceFactoryUtils.getQueryService().getAllActivityInstances(aiQuery);
      if (!ais.isEmpty())
      {
         ai = ais.get(0);
      }

      return ai;
   }
   /**
    * @param activityInstance
    * @param activityOutData
    * @param callbackHandler
    */
   public static void openDelegateDialog(ActivityInstance activityInstance, Map<String, Serializable> activityOutData,
         String activityContext, ICallbackHandler callbackHandler)
   {
      if (null != activityInstance)
      {
         // TODO line of code needed?
         // ctx.bind(DelegationBean.ACTIVITY_TO_DELEGATE, ai);

         DelegationBean delegationBean = (DelegationBean) FacesUtils.getBeanFromContext("delegationBean");
         delegationBean.setAi(activityInstance);
         delegationBean.setActivityOutData(activityOutData);
         delegationBean.setActivityContext(activityContext);
         delegationBean.setICallbackHandler(callbackHandler);
         delegationBean.openPopup();
      }
   }

   /**
    * @param activityInstances
    * @param callbackHandler
    */
   public static void openDelegateDialog(List<ActivityInstance> activityInstances, ICallbackHandler callbackHandler)
   {
      if (null != activityInstances)
      {
         DelegationBean delegationBean = (DelegationBean) FacesUtils.getBeanFromContext("delegationBean");
         delegationBean.setAis(activityInstances);
         delegationBean.setICallbackHandler(callbackHandler);
         delegationBean.openPopup();
      }
   }

   /**
    * Checks whether current Activity is Auxiliary Activity
    * 
    * @param activity
    * @return
    */
   public static boolean isAuxiliaryActivity(Activity activity)
   {
      Boolean auxiliaryAttr = null;
      Object attr = activity.getAttribute(PredefinedConstants.ACTIVITY_IS_AUXILIARY_ATT);
      if (attr instanceof Boolean)
      {
         auxiliaryAttr = (Boolean) attr;
      }
      else if (attr instanceof String && !StringUtils.isEmpty((String) attr))
      {
         auxiliaryAttr = Boolean.valueOf((String) attr);
      }
      return ActivityReportUtils.isAuxiliaryActivity(auxiliaryAttr, activity.getImplementationType());
   }

   /**
    * @param ai
    * @param context
    * @param data
    */
   public static ActivityInstance suspendToDefaultPerformer(ActivityInstance ai, String context, Map<String, ? > data)
   {
      ActivityInstance suspendedAi = null;
      try
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Suspending Activity '" + ai.getActivity().getName()
                  + "' to default performer, with out data = " + data);
         }

         if (isEmpty(context))
         {
            suspendedAi = ServiceFactoryUtils.getWorkflowService().suspendToDefaultPerformer(ai.getOID());
         }
         else
         {
            suspendedAi = ServiceFactoryUtils.getWorkflowService()
                  .suspendToDefaultPerformer(ai.getOID(), context, data);
         }

         sendActivityEvent(ai, ActivityEvent.suspended(suspendedAi));
      }
      catch (ConcurrencyException ce)
      {
         ExceptionHandler.handleException(ce,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.concurrencyError"));
      }
      catch (AccessForbiddenException af)
      {
         ExceptionHandler.handleException(af,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.acccessForbiddenError"));
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      return suspendedAi;
   }

   /**
    * @param ai
    * @param outData
    * @return
    */
   public static ActivityInstance suspend(ActivityInstance ai, ContextData outData)
   {
      ActivityInstance suspendedAi = null;

      try
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Suspending Activity '" + ai.getActivity().getName() + "' with out data = "
                  + ((null != outData) ? outData.getData() : null));
         }

         suspendedAi = ServiceFactoryUtils.getWorkflowService().suspend(ai.getOID(), outData);

         sendActivityEvent(ai, ActivityEvent.suspended(suspendedAi));
      }
      catch (ConcurrencyException ce)
      {
         ExceptionHandler.handleException(ce,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.concurrencyError"));
      }
      catch (AccessForbiddenException af)
      {
         ExceptionHandler.handleException(af,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.acccessForbiddenError"));
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      return suspendedAi;
   }

   /**
    * @param ai
    * @param context
    * @param data
    */
   public static ActivityInstance suspendToUserWorklist(ActivityInstance ai, String context, Map<String, ? > data)
   {
      ActivityInstance suspendedAi = null;
      try
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Suspending Activity '" + ai.getActivity().getName() + "' to User Worklist, with out data = "
                  + data);
         }

         if (isEmpty(context))
         {
            suspendedAi = ServiceFactoryUtils.getWorkflowService().suspendToUser(ai.getOID());
         }
         else
         {
            suspendedAi = ServiceFactoryUtils.getWorkflowService().suspendToUser(ai.getOID(), context, data);
         }

         sendActivityEvent(ai, ActivityEvent.suspended(suspendedAi));
      }
      catch (ConcurrencyException ce)
      {
         ExceptionHandler.handleException(ce,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.concurrencyError"));
      }
      catch (AccessForbiddenException af)
      {
         ExceptionHandler.handleException(af,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.acccessForbiddenError"));
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      return suspendedAi;
   }

   /**
    * @param ai
    */
   public static ActivityInstance activate(ActivityInstance ai)
   {
      ActivityInstance activatedAi = null;
      try
      {
         activatedAi = ServiceFactoryUtils.getWorkflowService().activate(ai.getOID());

         sendActivityEvent(ai, ActivityEvent.activated(activatedAi));
      }
      catch (ConcurrencyException ce)
      {
         ExceptionHandler.handleException(ce,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.concurrencyError"));
      }
      catch (AccessForbiddenException af)
      {
         ExceptionHandler.handleException(af,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.acccessForbiddenError"));
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      return activatedAi;
   }

   /**
    * @param oldAi
    * @param activityEvent
    */
   public static void sendActivityEvent(ActivityInstance oldAi, ActivityEvent activityEvent)
   {
      ParticipantWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      ProcessWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      SpecialWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      ClientContextBean.getCurrentInstance().getClientContext().sendActivityEvent(activityEvent);
   }

   /**
    * @param instance
    * @return localized activity name with OID appended
    */
   public static String getActivityLabel(ActivityInstance instance)
   {
      if (null != instance)
      {
         StringBuilder processLabel = new StringBuilder(I18nUtils.getActivityName(instance.getActivity()));
         processLabel.append(" (").append("#").append(instance.getOID()).append(")");
         return processLabel.toString();
      }
      return "";
   }
   
   /**
    * @param ai
    * @return Localized activity state name
    */
   public static String getActivityStateLabel(ActivityInstance ai)
   {
      return MessagesViewsCommonBean.getInstance().getString(STATUS_PREFIX + ai.getState().getName().toLowerCase());
   }
   
   /**
    * @return AbortActivityScope from user preferences
    */
   public static AbortScope getAbortActivityScope()
   {
      UserPreferencesHelper userPrefHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);
      String abortProcessScope = userPrefHelper.getSingleString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_ACTIVITY_ABORT_SCOPE);

      if (abortProcessScope.equals(AbortScope.ROOT_HIERARCHY))
      {
         return AbortScope.RootHierarchy;
      }
      else if (abortProcessScope.equals(AbortScope.SUB_HIERARCHY))
      {
         return AbortScope.SubHierarchy;
      }
      return null;
   }

   /**
    * to check Activity of type is Default Case Activity
    * 
    * @param ai
    * @return
    */
   public static boolean isDefaultCaseActivity(ActivityInstance ai)
   {
      if (null != ai && PredefinedConstants.DEFAULT_CASE_ACTIVITY_ID.equals(ai.getActivity().getId()))
      {
         return true;
      }
      return false;
   }
   
   /**
    * to check Activities of type is Default Case Activity
    * 
    * @param ai
    * @return
    */
   public static boolean isContainsCaseActivity(List<ActivityInstance> ais)
   {
      if (CollectionUtils.isNotEmpty(ais))
      {
         for (ActivityInstance ai : ais)
         {
            // if any of activity is case activity then return true
            if (ai.getProcessInstance().isCaseProcessInstance())
            {
               return true;
            }
         }
         return false;
      }
      // if collection is empty then return false
      else
      {
         return false;
      }
   }
   
   /**
    * to check Activities of type is Default Case Activity
    * 
    * @param ai
    * @return
    */
   public static boolean isDefaultCaseActivities(List<ActivityInstance> ais)
   {
      if (CollectionUtils.isNotEmpty(ais))
      {
         for (ActivityInstance ai : ais)
         {
            // if any of activity is non case activity then return false
            if (!ai.getProcessInstance().isCaseProcessInstance())
            {
               return false;
            }
         }
         return true;
      }
      // if collection is empty then return false
      else
      {
         return false;
      }
   }
   
   /**
    * get Case Name from activityInstance
    * 
    * @param processInstance
    * @return
    */
   public static String getCaseName(ActivityInstance activityInstance)
   {
      return ProcessInstanceUtils.getCaseName(activityInstance.getProcessInstance());
   }
   
   /**
    * Reads all Activity States and returns a List of Activity States
    */
   public static List<SelectItem> getAllActivityStates()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      List<SelectItem> allStatusList = new ArrayList<SelectItem>();

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.ABORTED), propsBean
            .getString("views.activityTable.statusFilter.aborted")));

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.ABORTING), propsBean
            .getString("views.activityTable.statusFilter.aborting")));

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.APPLICATION), propsBean
            .getString("views.activityTable.statusFilter.application")));

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.COMPLETED), propsBean
            .getString("views.activityTable.statusFilter.completed")));

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.CREATED), propsBean
            .getString("views.activityTable.statusFilter.created")));

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.HIBERNATED), propsBean
            .getString("views.activityTable.statusFilter.hibernated")));

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.INTERRUPTED), propsBean
            .getString("views.activityTable.statusFilter.interrupted")));

      allStatusList.add(new SelectItem(Integer.toString(ActivityInstanceState.SUSPENDED), propsBean
            .getString("views.activityTable.statusFilter.suspended")));
      return allStatusList;
   }
   
   /**
    * @param formatType
    * @return
    */
   public static String getIconPath(String formatType)
   {
      String fileName = getIconMap().get(formatType);
      if (!fileName.contains("/"))
      {
         return (IMAGE_BASE_PATH + fileName);
      }
      return fileName;
   }
   
   /**
    * @param type
    * @param strict
    * @return
    */
   public static String getActivityType(Activity activity, boolean strict)
   {
      String type = activity.getImplementationType().getName() + "Activity";
      String orgType = type;

      if ("ActivityActive".equals(type) || "Resubmission".equals(type) || "ActivitySuspended".equals(type)
            || "ActivityInterrupted".equals(type) || "ActivityAborted".equals(type) || "AbortingActivity".equals(type))
      {
         type = "StateChange";
      }

      if ("RouteActivity".equals(type) || "SubprocessActivity".equals(type))
      {
         type = "Auxiliary";
      }

      if (activity.isInteractive())
      {
         // If Application Activity is interactive then it's Manual
         type = "ManualActivity";
      }

      if (ActivityInstanceUtils.isAuxiliaryActivity(activity))
      {
         type = "Auxiliary";
      }
      else if (!"ApplicationActivity".equals(type))
      {
         if ("SubprocessActivity".equals(orgType))
         {
            if (strict)
            {
               type = orgType;
            }
            else
            {
               type = "ApplicationActivity";
            }
         }
         else
         {
            // All non Auxiliary activities are Manual if not Application Activity
            type = "ManualActivity";
         }
      }
      return type;
   }

   public static Map<String, String> getIconMap()
   {
      return iconMap;
   }
}
