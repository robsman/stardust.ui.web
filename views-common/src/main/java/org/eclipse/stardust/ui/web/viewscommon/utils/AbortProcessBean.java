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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationItem;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationMessage;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Yogesh.Manware
 * 
 */
public class AbortProcessBean extends PopupUIComponentBean
{
   private static final long serialVersionUID = 1L;
   public static final Logger trace = LogManager.getLogger(AbortProcessBean.class);

   public static enum DialogType {
      ABORT_OPTIONS, CONFIRMATION
   }

   private static enum AbortProcessScopes {
      root, sub
   }

   private static final String ABORT_SCOPE_PREFIX = "views.common.process.abortProcess.scope.";

   private List<ProcessInstance> processesToBeAborted;
   private ICallbackHandler callbackHandler;
   private String selectedAbortScope;
   private SelectItem[] abortScopes;
   private AbortScope abortScopeConfig;
   private List<String> headerMsgLines = null;
   private DialogType dialogType = DialogType.ABORT_OPTIONS;
   private String acceptLabel;
   private String cancelLabel;

   /**
    * @return
    */
   public static AbortProcessBean getInstance()
   {
      return (AbortProcessBean) FacesUtils.getBeanFromContext("abortProcessBean");
   }

   /**
    * abort Process
    * 
    * @param pi
    */
   public void abortProcess(ProcessInstance pi)
   {
      processesToBeAborted = new ArrayList<ProcessInstance>(1);
      processesToBeAborted.add(pi);
      openAbortProcessDialog();
   }

   /**
    * abort processes
    * @param processes
    */
   public void abortProcesses(List<ProcessInstance> processes)
   {
      processesToBeAborted = processes;
      openAbortProcessDialog();
   }

   /**
    * Abort Process after confirmation
    */
   public void abortProcessAction()
   {
      if (null != this.abortScopeConfig)
      {
         abortProcess(this.abortScopeConfig);
      }
      else if (selectedAbortScope.contains(AbortProcessScopes.root.name()))
      {
         abortProcess(AbortScope.RootHierarchy);
      }
      else
      {
         abortProcess(AbortScope.SubHierarchy);
      }
      closePopup();
   }

   
   /**
    * open abort process dialog
    */
   private void openAbortProcessDialog()
   {
      // Set Dialog Type
      this.abortScopeConfig = ProcessInstanceUtils.getAbortProcessScope();
      if (null == this.abortScopeConfig)
      {
         dialogType = DialogType.ABORT_OPTIONS;
      }
      else
      {
         dialogType = DialogType.CONFIRMATION;
      }

      // initialize the dialog'
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
         String[] keys = {AbortProcessScopes.sub.name(), AbortProcessScopes.root.name()};
         abortScopes = IceComponentUtil.buildSelectItemArray(ABORT_SCOPE_PREFIX, keys, msgBean);
         selectedAbortScope = abortScopes[0].getValue().toString();
         acceptLabel = msgBean.getString("common.ok");
         cancelLabel = msgBean.getString("common.cancel");
      }
      openPopup();
   }

   
   /**
    * Shows abort notification dialog
    * 
    * @param abortedProcesses
    * @param skippedProcesses
    */
   private static void showProcessAbortNotification(List<ProcessInstance> abortedProcesses,
         Map<ProcessInstance, String> skippedProcesses)
   {
      MessagesViewsCommonBean msgBean = MessagesViewsCommonBean.getInstance();
      if ((CollectionUtils.isNotEmpty(abortedProcesses)) || (CollectionUtils.isNotEmpty(skippedProcesses)))
      {
         NotificationMessageBean notificationMB = NotificationMessageBean.getCurrent();
         WorkflowService ws = ServiceFactoryUtils.getWorkflowService();

         // Aborted processes
         NotificationMessage notificationMessage = new NotificationMessage();
         List<NotificationItem> itemsList = new ArrayList<NotificationItem>();
         if (CollectionUtils.isNotEmpty(abortedProcesses))
         {
            notificationMessage.setMessage(msgBean.getString("views.common.process.abortProcess.success"));

            for (ProcessInstance pi : abortedProcesses)
            {
               pi = ws.getProcessInstance(pi.getOID());
               itemsList.add(new NotificationItem(ProcessInstanceUtils.getProcessLabel(pi), ProcessInstanceUtils
                     .getProcessStateLabel(pi)));
            }
            notificationMessage.setNotificationItem(itemsList);
            notificationMB.add(notificationMessage);
         }

         // Skipped Processes
         notificationMessage = new NotificationMessage();
         itemsList = new ArrayList<NotificationItem>();

         if (CollectionUtils.isNotEmpty(skippedProcesses))
         {
            notificationMessage.setMessage(msgBean.getString("views.common.process.abortProcess.failure"));
            for (Entry<ProcessInstance, String> skippedProcess : skippedProcesses.entrySet())
            {
               ProcessInstance pi = ws.getProcessInstance(skippedProcess.getKey().getOID());
               itemsList.add(new NotificationItem(ProcessInstanceUtils.getProcessLabel(pi), skippedProcess.getValue()));
            }
            notificationMessage.setNotificationItem(itemsList);
            notificationMB.add(notificationMessage);
         }
         notificationMB.openPopup();
      }
   }

   /**
    * Abort Process based on user preference
    * 
    * @param abortScope
    */
   private void abortProcess(AbortScope abortScope)
   {
      if (CollectionUtils.isNotEmpty(processesToBeAborted))
      {
         List<ProcessInstance> abortedProcesses = new ArrayList<ProcessInstance>();
         Map<ProcessInstance, String> skippedProcesses = new HashMap<ProcessInstance, String>();
         for (ProcessInstance processInstance : processesToBeAborted)
         {
            if (processInstance != null)
            {
               processInstance = ProcessInstanceUtils.getProcessInstance(processInstance.getOID());
               if (ProcessInstanceUtils.isAbortable(processInstance))
               {
                  try
                  {
                     
                     if(processInstance.isCaseProcessInstance())
                     {
                        skippedProcesses.put(processInstance, MessagesViewsCommonBean.getInstance().getParamString(
                              "views.switchProcessDialog.caseAbort.message",
                              ProcessInstanceUtils.getProcessStateLabel(processInstance)));
                     }
                     else
                     {
                        ProcessInstanceUtils.abortProcess(processInstance, abortScope);
                        abortedProcesses.add(processInstance);   
                     }
                     
                  }
                  catch (Exception e)
                  {
                     // It is very to rare, any exception would occur here
                     trace.error(e);
                     skippedProcesses.put(processInstance, MessagesViewsCommonBean.getInstance().getParamString(
                           "views.common.process.abortProcess.failureMsg2", ExceptionHandler.getExceptionMessage(e)));
                  }
               }
               else
               {
                  if (ProcessInstanceState.Aborted.equals(processInstance.getState())
                        || ProcessInstanceState.Completed.equals(processInstance.getState()))
                  {
                     skippedProcesses.put(processInstance, MessagesViewsCommonBean.getInstance().getParamString(
                           "views.common.process.abortProcess.failureMsg3",
                           ProcessInstanceUtils.getProcessStateLabel(processInstance)));
                  }
                  else
                  {
                     skippedProcesses.put(processInstance, MessagesViewsCommonBean.getInstance().getString(
                           "views.common.process.abortProcess.failureMsg1"));
                  }
               }
            }
         }
         showProcessAbortNotification(abortedProcesses, skippedProcesses);
         if (null != callbackHandler)
         {
            callbackHandler.handleEvent(EventType.APPLY);
         }
      }
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
         headerMsg = MessagesViewsCommonBean.getInstance().getString("views.common.process.abortProcess.headerMsg.sub");
      }
      else
      {
         headerMsg = MessagesViewsCommonBean.getInstance().getString(
               "views.common.process.abortProcess.headerMsg.prompt");
      }
      return headerMsg;
   }

   public boolean isConfirmationDialog()
   {
      return DialogType.CONFIRMATION.equals(this.dialogType);
   }

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

   @Override
   public void initialize()
   {}

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
