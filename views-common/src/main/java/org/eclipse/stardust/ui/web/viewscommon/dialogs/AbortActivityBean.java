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

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationItem;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ClientContextBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.IceComponentUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author Yogesh.Manware
 * 
 */
public class AbortActivityBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final String ABORT_SCOPE_PREFIX = "views.common.activity.abortActivity.scope.";
   public static final Logger trace = LogManager.getLogger(AbortActivityBean.class);
   private List<ActivityInstance> activitiesToBeAborted;
   private ICallbackHandler callbackHandler;
   private String selectedAbortScope;
   private SelectItem[] abortScopes;
   private AbortScope abortScopeConfig;
   private List<String> headerMsgLines = null;
   private DialogType dialogType = DialogType.ABORT_OPTIONS;
   private String acceptLabel;
   private String cancelLabel;

   private static enum DialogType {
      ABORT_OPTIONS, CONFIRMATION
   }

   private static enum AbortActivityScopes {
      root, sub
   }

   /**
    * @return
    */
   public static AbortActivityBean getInstance()
   {
      return (AbortActivityBean) FacesUtils.getBeanFromContext("abortActivityBean");
   }

   /**
    * abort Activity
    * 
    * @param pi
    */
   public void abortActivity(ActivityInstance ai)
   {
      activitiesToBeAborted = new ArrayList<ActivityInstance>(1);
      activitiesToBeAborted.add(ai);
      openAbortActivityDialog();
   }

   /**
    * abort Activities
    * 
    * @param activities
    */
   public void abortActivities(List<ActivityInstance> activities)
   {
      activitiesToBeAborted = activities;
      openAbortActivityDialog();
   }

   /**
    * Abort Activity after confirmation
    */
   public void abortActivityAction()
   {
      boolean notificationShown = false;
      if (null != this.abortScopeConfig)
      {
         notificationShown = abortActivities(this.abortScopeConfig);
      }
      else if (selectedAbortScope.contains(AbortActivityScopes.root.name()))
      {
         notificationShown = abortActivities(AbortScope.RootHierarchy);
      }
      else
      {
         notificationShown = abortActivities(AbortScope.SubHierarchy);
      }
      
      if (!notificationShown)
      {
         closePopup();
      }
   }

   /**
    * Open Abort Activity Dialog
    */
   private void openAbortActivityDialog()
   {
      // Set Dialog Type
      this.abortScopeConfig = ActivityInstanceUtils.getAbortActivityScope();
      if (null == this.abortScopeConfig)
      {
         dialogType = DialogType.ABORT_OPTIONS;
      }
      else
      {
         dialogType = DialogType.CONFIRMATION;
      }

      // initialize the dialog
      headerMsgLines = IceComponentUtil.parseMessage(getHeaderMsg());
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();

      if (isConfirmationDialog())
      {
         // create confirmation dialog
         acceptLabel = msgBean.getString("common.yes");
         cancelLabel = msgBean.getString("common.no");
      }
      else
      { // prompt dialog showing abort options
         String[] keys = {AbortActivityScopes.sub.name(), AbortActivityScopes.root.name()};
         abortScopes = IceComponentUtil.buildSelectItemArray(ABORT_SCOPE_PREFIX, keys, msgBean);
         selectedAbortScope = abortScopes[0].getValue().toString();
         acceptLabel = msgBean.getString("common.ok");
         cancelLabel = msgBean.getString("common.cancel");
      }
      openPopup();
   }

   /**
    * Aborts the selected activities
    * 
    * @param ae
    */
   private boolean abortActivities(AbortScope abortScope)
   {

      if (CollectionUtils.isNotEmpty(activitiesToBeAborted))
      {
         List<NotificationItem> successNotifications = new ArrayList<NotificationItem>();
         List<NotificationItem> failureNotifications = new ArrayList<NotificationItem>();

         WorkflowService workflowService = ServiceFactoryUtils.getWorkflowService();
         for (ActivityInstance activityInstance : activitiesToBeAborted)
         {
            if (null != activityInstance)
            {
               activityInstance = ActivityInstanceUtils.getActivityInstance(activityInstance.getOID());
               if (!ActivityInstanceUtils.isDefaultCaseActivity(activityInstance))
               {
                  try
                  {
                     workflowService.abortActivityInstance(activityInstance.getOID(), abortScope);
                     ClientContextBean.getCurrentInstance().getClientContext()
                           .sendActivityEvent(ActivityEvent.aborted(activityInstance));
                     successNotifications.add(new NotificationItem(ActivityInstanceUtils
                           .getActivityLabel(activityInstance), ActivityInstanceUtils
                           .getActivityStateLabel(activityInstance)));
                  }
                  catch (Exception e)
                  {
                     // It is very to rare that any exception would occur here
                     trace.error(e);

                     failureNotifications
                           .add(new NotificationItem(ActivityInstanceUtils.getActivityLabel(activityInstance),
                                 MessagesViewsCommonBean.getInstance().getParamString(
                                       "views.common.activity.abortActivity.failureMsg2",
                                       ExceptionHandler.getExceptionMessage(e))));
                  }
               }
               else
               {
                  if (ActivityInstanceUtils.isDefaultCaseActivity(activityInstance))
                  {
                     failureNotifications.add(new NotificationItem(ActivityInstanceUtils
                           .getActivityLabel(activityInstance), MessagesViewsCommonBean.getInstance().getString(
                           "views.switchProcessDialog.caseAbort.message")));
                  }
                  else if (ActivityInstanceState.Aborted.equals(activityInstance.getState())
                        || ActivityInstanceState.Completed.equals(activityInstance.getState()))
                  {
                     failureNotifications.add(new NotificationItem(ActivityInstanceUtils
                           .getActivityLabel(activityInstance), MessagesViewsCommonBean.getInstance().getParamString(
                           "views.common.activity.abortActivity.failureMsg3",
                           ActivityInstanceUtils.getActivityStateLabel(activityInstance))));
                  }
                  else
                  {
                     failureNotifications.add(new NotificationItem(ActivityInstanceUtils
                           .getActivityLabel(activityInstance), MessagesViewsCommonBean.getInstance().getString(
                           "views.common.activity.abortActivity.failureMsg1")));
                  }
               }
            }
         }

         // Before opening new Popup close current Popup so that ViewEvents will be fired
         // in order
         closePopup();
         NotificationMessageBean.showNotifications(successNotifications, MessagesViewsCommonBean.getInstance()
               .getString("views.common.activity.abortActivity.success"), failureNotifications, MessagesViewsCommonBean
               .getInstance().getString("views.common.activity.abortActivity.failure"), callbackHandler);

         return true;
      }

      return false;
   }

   /**
    * @return header message based on the configured abort scope
    */
   private String getHeaderMsg()
   {
      String headerMsg;
      if (AbortScope.RootHierarchy.equals(abortScopeConfig))
      {
         headerMsg = MessagesViewsCommonBean.getInstance()
               .getString("views.common.process.abortProcess.headerMsg.root");
      }
      else if (AbortScope.SubHierarchy.equals(abortScopeConfig))
      {
         headerMsg = MessagesViewsCommonBean.getInstance().getString(
               "views.common.activity.abortActivity.headerMsg.sub");
      }
      else
      {
         headerMsg = MessagesViewsCommonBean.getInstance().getString(
               "views.common.activity.abortActivity.headerMsg.prompt");
      }
      return headerMsg;
   }

   @Override
   public void initialize()
   {}
   
   public boolean isConfirmationDialog()
   {
      return DialogType.CONFIRMATION.equals(this.dialogType);
   }

   // ************* Default Getter and Setter Methods*****************
   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public String getSelectedAbortScope()
   {
      return selectedAbortScope;
   }

   public void setSelectedAbortScope(String selectedAbortScope)
   {
      this.selectedAbortScope = selectedAbortScope;
   }

   public SelectItem[] getAbortScopes()
   {
      return abortScopes;
   }

   public List<String> getHeaderMsgLines()
   {
      return headerMsgLines;
   }

   public String getAcceptLabel()
   {
      return acceptLabel;
   }

   public String getCancelLabel()
   {
      return cancelLabel;
   }
}