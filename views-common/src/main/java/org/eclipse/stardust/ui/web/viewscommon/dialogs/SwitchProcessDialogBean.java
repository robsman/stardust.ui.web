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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.ProcessDefinitions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * 
 * @author vikas.mishra
 * @since 7.0
 * 
 *        bean for switch(Abort and Start) process dialog
 * 
 */
public class SwitchProcessDialogBean extends PopupUIComponentBean implements ICallbackHandler, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "switchProcessDialogBean";
   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   
   private List<SelectItem> switchableProcessItems;
   private String selectedProcessId;
   private boolean showStartProcessView = true;
   private List<SwitchProcessTableEntry> switchedProcessTable;
   private List<ProcessInstance> sourceProcessInstances;
   private String linkComment;
   private List<ProcessInstance> startedProcessInstances;
   private List<String> switchedProcessMessage;
   private ICallbackHandler iCallbackHandler;
   private List<ProcessInstance> nonAbortableProcesses;
   private boolean showAbortDailog;
   private boolean allRowAborted;
   private String notificationLabel;
   private Integer modelOID;
   private ConfirmationDialog switchProcessConfirmationDialog;

   /**
    * method to get SwitchProcessDialogBean instance
    * 
    * @return SwitchProcessDialogBean object
    */
   public static SwitchProcessDialogBean getInstance()
   {
      return (SwitchProcessDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * method to initialize instance variables value
    */
   @Override
   public void initialize()
   {    
      switchableProcessItems = findStartableProcessess(modelOID);
      if (CollectionUtils.isNotEmpty(switchableProcessItems))
      {
         Collections.sort(switchableProcessItems, new Comparator<SelectItem>()
         {
            public int compare(SelectItem s1, SelectItem s2)
            {
               return s1.getLabel().compareTo(s2.getLabel());
            }
         });
         
         selectedProcessId = (String) switchableProcessItems.get(0).getValue(); // default
                                                                                // selection
                                                                                // first
      }
   }

   /**
    * method to reset instance variables value
    */
   @Override
   public void reset()
   {
      showStartProcessView = true;
      startedProcessInstances = null;
      selectedProcessId = null;
      linkComment = null;
      showAbortDailog = false;
      allRowAborted = false;
      notificationLabel = null;
      nonAbortableProcesses = null;
      sourceProcessInstances = null;
   }

   /**
    * JSF action method open Switch Process Dialog
    * 
    */
   @Override
   public void openPopup()
   {
      try
      {
         if (CollectionUtils.isEmpty(sourceProcessInstances))
         {
            return;
         }

         modelOID = ProcessInstanceUtils.getProcessModelOID(sourceProcessInstances);

         if (null == modelOID)
         {
            showNotificationForModel();

            return;
         }

         // find and replace processes with root process instances
         sourceProcessInstances = ProcessInstanceUtils.getRootProcessInstances(sourceProcessInstances);

         // Case-1
         // check if selected process(s) are allowed to abort ,if not then show
         // table with ProcessName and Abort status message
         // also do not allow to proceed,if all selected processes are not abortable then
         // if any of selected process is allowed to abort and user select "continue"
         // then allow to proceed.
         nonAbortableProcesses = CollectionUtils.newArrayList();
         List<SwitchProcessTableEntry> tableEntry = checkAndShowAbortNotification();

         if (CollectionUtils.isNotEmpty(tableEntry))
         {
            showAbortDailog = true;
            showStartProcessView = false;
            switchedProcessTable = tableEntry;
            // Check if all the source processes are already aborted
            if (tableEntry.size() == sourceProcessInstances.size())
            {
               allRowAborted = true;
               notificationLabel = COMMON_MESSAGE_BEAN.getParamString("views.switchProcessDialog.notification.message",
                     COMMON_MESSAGE_BEAN.getString("common.confirmCannotContinue.message.label"));
            }
            else
            {
               allRowAborted = false;
               notificationLabel = COMMON_MESSAGE_BEAN.getParamString("views.switchProcessDialog.notification.message",
                     COMMON_MESSAGE_BEAN.getString("views.common.continueQuestion.label"));
            }
            openConfirmationDialog();
         }
         else if (CollectionUtils.isNotEmpty(sourceProcessInstances))
         {
         // Case-2 :if Case-1 and Case -2 is passed then allow to open
         // "Abort and Start Process"
         initialize();
         super.openPopup();
         }
         
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * JSF action method to switch new process for selected process from Switch process
    * 
    */
   public void switchProcesses()
   {
      try
      {
         if (null != selectedProcessId)
         {
            startedProcessInstances = CollectionUtils.newArrayList();
            showStartProcessView = false;

            String targetProcessId = ModelUtils.extractParticipantId(selectedProcessId);

            if (sourceProcessInstances.size() > 1)
            {
               List<SwitchProcessTableEntry> tableEntryList = CollectionUtils.newArrayList();

               if (CollectionUtils.isNotEmpty(sourceProcessInstances))
               {
                  for (ProcessInstance pi : sourceProcessInstances)
                  {
                     if (!nonAbortableProcesses.contains(pi))
                     {
                        tableEntryList.add(spawnPeerProcess(pi, targetProcessId, linkComment));
                     }
                  }

                  switchedProcessTable = tableEntryList;
               }
            }
            else
            {
               ProcessInstance processInstance = sourceProcessInstances.get(0);

               // check permission
               if (ProcessInstanceUtils.isAbortable(processInstance))
               {
                  ProcessInstance pi = ServiceFactoryUtils.getWorkflowService().spawnPeerProcessInstance(
                        processInstance.getOID(), targetProcessId, true, null, true, linkComment);
                  startedProcessInstances.add(pi);
               }
            }
         }
         openConfirmationDialog();
      }
      catch (Exception e)
      {
         closeSwitchProcessPopup();
         ExceptionHandler.handleException(e);
      }
   }

   /*
    * Confirmation Dialog showing Error Notification/Warning Info/Success Switch of
    * process details
    */
   public void openConfirmationDialog()
   {
      if (showAbortDailog)
      {
         if (allRowAborted)
         {
            switchProcessConfirmationDialog = new ConfirmationDialog(DialogContentType.ERROR,
                  DialogActionType.CONTINUE_CLOSE, DialogType.CANCEL_ONLY, DialogStyle.COMPACT, this);
         }
         else
         {
            switchProcessConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING,
                  DialogActionType.YES_NO, null, DialogStyle.COMPACT, this);
         }
      }
      else
      {
         switchProcessConfirmationDialog = new ConfirmationDialog(DialogContentType.INFO, DialogActionType.YES_NO,
               null, DialogStyle.COMPACT, this);
      }
      switchProcessConfirmationDialog.setIncludePath(ResourcePaths.V_SWITCH_PROCESS_CONF_DLG);
      super.closePopup();
      switchProcessConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      switchProcessConfirmationDialog = null;
      if (showAbortDailog)
      {
         openSwitchProcessDialog();
      }
      else
      {
         openActivities();
      }
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      switchProcessConfirmationDialog = null;
      closeSwitchProcessPopup();
      return true;
   }
   
   /**
    * method to close Switch Process Dialog
    */
   public void closeSwitchProcessPopup()
   {
      if (CollectionUtils.isNotEmpty(startedProcessInstances))
      {
         fireCallbackEvent();
      }
      reset();
      super.closePopup();
   }

   /**
    * action method to open Worklist table view for switched processes
    */
   public void openActivities()
   {
      try
      {
         if (CollectionUtils.isNotEmpty(startedProcessInstances))
         {
            Map<String, Object> params = CollectionUtils.newTreeMap();
            ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();

            FilterOrTerm orTerm = query.getFilter().addOrTerm();

            for (ProcessInstance pInstance : startedProcessInstances)
            {
               orTerm.add(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pInstance.getOID()));
            }

            params.put(Query.class.getName(), query);
            params.put("name", COMMON_MESSAGE_BEAN.getString("views.switchProcessDialog.worklist.title"));
            PortalApplication.getInstance().openViewById("worklistPanel", "id=" + new Date().getTime(), params, null,
                  false);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

      // close Switch Process Dialog
      closeSwitchProcessPopup();
   }

   /**
    * return Switched Process Message in Collection (separated by \n)
    * 
    * @return List<String>
    */
   public List<String> getSwitchedProcessMessage()
   {
      switchedProcessMessage = CollectionUtils.newArrayList();

      if (CollectionUtils.isNotEmpty(startedProcessInstances))
      {
         if (startedProcessInstances.size() > 1)
         {
            switchedProcessMessage.add(COMMON_MESSAGE_BEAN.getString("views.switchProcessDialog.operationSuccess"));
         }
         else
         {
            if (CollectionUtils.isNotEmpty(startedProcessInstances))
            {
               ProcessInstance sourceProcessInstance = sourceProcessInstances.get(0);
               ProcessInstance switchedProcessInstance = startedProcessInstances.get(0);

               String sourceParam = ProcessInstanceUtils.getProcessLabel(sourceProcessInstance);
               String targetParam = ProcessInstanceUtils.getProcessLabel(switchedProcessInstance);

               String successMsg = COMMON_MESSAGE_BEAN.getParamString("views.switchProcessDialog.processesStarted",
                     sourceParam, targetParam);
               switchedProcessMessage.add(successMsg);
            }
         }
      }
      return switchedProcessMessage;
   }

   public String getSelectedProcessId()
   {
      return selectedProcessId;
   }

   public void setSelectedProcessId(String selectedProcessId)
   {
      this.selectedProcessId = selectedProcessId;
   }

   public List<SelectItem> getSwitchableProcessItems()
   {
      return switchableProcessItems;
   }

   public void setSwitchableProcessItems(List<SelectItem> switchableProcessItems)
   {
      this.switchableProcessItems = switchableProcessItems;
   }

   public boolean isShowStartProcessView()
   {
      return showStartProcessView;
   }

   public void setShowStartProcessView(boolean showStartProcessView)
   {
      this.showStartProcessView = showStartProcessView;
   }

   public List<SwitchProcessTableEntry> getSwitchedProcessTable()
   {
      return switchedProcessTable;
   }

   public String getLinkComment()
   {
      return linkComment;
   }

   public void setLinkComment(String linkComment)
   {
      this.linkComment = linkComment;
   }

   public List<ProcessInstance> getSourceProcessInstances()
   {
      return sourceProcessInstances;
   }

   public void setSourceProcessInstances(List<ProcessInstance> sourceProcessInstances)
   {
      this.sourceProcessInstances = sourceProcessInstances;
   }

   public void setSwitchedProcessMessage(List<String> switchedProcessMessage)
   {
      this.switchedProcessMessage = switchedProcessMessage;
   }

   public List<ProcessInstance> getStartedProcessInstances()
   {
      return startedProcessInstances;
   }

   public boolean isShowAbortDailog()
   {
      return showAbortDailog;
   }

   public boolean isAllRowAborted()
   {
      return allRowAborted;
   }

   public String getNotificationLabel()
   {
      return notificationLabel;
   }

   public ConfirmationDialog getSwitchProcessConfirmationDialog()
   {
      return switchProcessConfirmationDialog;
   }

   /**
    * 
    * @param callbackHandler
    */
   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
   }

   /**
    * after given confirmation to user and user is agreed to continue this method get
    * called
    * 
    */
   public void handleEvent(EventType eventType)
   {
      if (eventType.equals(EventType.APPLY) && CollectionUtils.isNotEmpty(sourceProcessInstances))
      {
         openSwitchProcessDialog();
      }
   }

   public void openSwitchProcessDialog()
   {
      // reset abort dialog flag , when user select's 'Yes' on warning dialog
      showAbortDailog = false;
      allRowAborted = false;
      showStartProcessView = true;
      initialize();
      super.openPopup();
   }

   /**
    * prepare notification messages and open NotificationMessage Dialog if models are not
    * same
    */
   private void showNotificationForModel()
   {
      MessageDialog.addMessage(MessageType.WARNING, COMMON_MESSAGE_BEAN.getString("common.notification.title"),
            COMMON_MESSAGE_BEAN.getString("views.switchProcessDialog.pisInDiffModels"));
   }

   /**
    * return SwitchProcessTableEntry with Abort Message and Aborted SourceProcessName
    * unauthorized SourceProcessName
    * 
    * @param processInstance
    * @return
    */
   private SwitchProcessTableEntry createNotificationItem(ProcessInstance processInstance)
   {
      SwitchProcessTableEntry tableEntry = null;

      if (!AuthorizationUtils.hasAbortPermission(processInstance))
      {
         String key = ProcessInstanceUtils.getProcessLabel(processInstance);
         tableEntry = new SwitchProcessTableEntry(null, null, false,
               COMMON_MESSAGE_BEAN.getString("common.authorization.msg"), key);
      }
      else if (!ProcessInstanceUtils.isAbortable(processInstance))
      {
         String key = ProcessInstanceUtils.getProcessLabel(processInstance);
         tableEntry = new SwitchProcessTableEntry(null, null, false,
               COMMON_MESSAGE_BEAN.getString("common.notifyProcessAlreadyAborted"), key);
      }
      else if(processInstance.isCaseProcessInstance())
      {
         String key = ProcessInstanceUtils.getProcessLabel(processInstance);
         tableEntry = new SwitchProcessTableEntry(null, null, false,
               COMMON_MESSAGE_BEAN.getString("views.switchProcessDialog.caseAbort.message"), key);
      }

      return tableEntry;
   }

   /**
    * prepare List of processes already aborted or unauthorized to abort by use
    * 
    */
   private List<SwitchProcessTableEntry> checkAndShowAbortNotification()
   {
      List<SwitchProcessTableEntry> tableEntryList = CollectionUtils.newArrayList();

      for (ProcessInstance processInstance : sourceProcessInstances)
      {
         SwitchProcessTableEntry abortedProcess = createNotificationItem(processInstance);

         if (null != abortedProcess)
         {
            tableEntryList.add(abortedProcess);
            nonAbortableProcesses.add(processInstance);
         }
      }

      return tableEntryList;
   }

   /**
    * method fire CallbackEvent to CallbackHandler so handler can take appropriate action
    */
   private void fireCallbackEvent()
   {
      if (iCallbackHandler != null)
      {
         iCallbackHandler.handleEvent(EventType.APPLY);
      }
   }

   /**
    * method spawn's the new process aborting current process and returns the list of new
    * processes started
    * 
    * @param processInstance
    * @param targetProcessId
    * @param linkComment
    * @return
    */
   private SwitchProcessTableEntry spawnPeerProcess(ProcessInstance processInstance, String targetProcessId,
         String linkComment)
   {
      WorkflowService workflowService = ServiceFactoryUtils.getWorkflowService();
      SwitchProcessTableEntry tableEntry = null;

      try
      {
         ProcessInstance pi = workflowService.spawnPeerProcessInstance(processInstance.getOID(), targetProcessId, true,
               null, true, linkComment);

         startedProcessInstances.add(pi);
         tableEntry = new SwitchProcessTableEntry(processInstance, pi, true, null, null);
      }
      catch (Exception e)
      {
         tableEntry = new SwitchProcessTableEntry(processInstance, null, false,
               COMMON_MESSAGE_BEAN.getString("common.fail") + " - " + e.getLocalizedMessage(), null);
      }

      return tableEntry;
   }

   /**
    * return list of SelectItem for modelOID Excluding current in scope Process Definition
    * should not be available for selection.
    * 
    * @param modelOID
    * @return list of SelectItem for modelOID
    */
   private List<SelectItem> findStartableProcessess(long modelOID)
   {     

      List<SelectItem> items = new ArrayList<SelectItem>();

      ProcessDefinitions pds = ServiceFactoryUtils.getQueryService().getProcessDefinitions(
            ProcessDefinitionQuery.findStartable(modelOID));

      Map<String, ProcessDefinition> pdMap = CollectionUtils.newHashMap();

      for (ProcessDefinition pd : pds)
      {
         pdMap.put(pd.getId(), pd);
      }

      List<ProcessDefinition> filteredPds = new ArrayList<ProcessDefinition>(pdMap.values());
      ProcessDefinitionUtils.sort(filteredPds);

      for (ProcessDefinition pd : pdMap.values())
      {
         items.add(new SelectItem(pd.getQualifiedId(), I18nUtils.getProcessName(pd)));
      }

      pdMap = null;
      pds = null;
      filteredPds = null;

      return items;
   }

   /**
    * table entry class for Switch Process Table
    * 
    * @author vikas.mishra
    * @version $Revision: $
    */
   public static class SwitchProcessTableEntry extends DefaultRowModel
   {
      private static final long serialVersionUID = 1L;
      private final ProcessInstance switchedProcessInstance;
      private final ProcessInstance sourceProcessInstance;
      private final boolean success;
      private final String statusMessage;
      private final String abortedProcessInstance;
      private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();

      /**
       * Constructor
       * 
       * @param sourceProcessInstance
       * @param switchedProcessInstance
       * @param success
       * @param statusMessage
       */
      public SwitchProcessTableEntry(ProcessInstance sourceProcessInstance, ProcessInstance switchedProcessInstance,
            boolean success, String statusMessage, String abortedProcessInstance)
      {
         this.switchedProcessInstance = switchedProcessInstance;
         this.sourceProcessInstance = sourceProcessInstance;
         this.success = success;
         this.statusMessage = statusMessage;
         this.abortedProcessInstance = abortedProcessInstance;
      }

      /**
       * on success returns i18n "Success" otherwise returns error message.
       * 
       * @return
       */
      public String getStatusMessage()
      {
         return success ? COMMON_MESSAGE_BEAN.getString("common.success") : statusMessage;
      }

      /**
       * returns formatted started process name on success or returns empty string.
       * 
       * @return String
       */
      public String getSwitchedProcessName()
      {
         return success ? ProcessInstanceUtils.getProcessLabel(switchedProcessInstance) : "";
      }

      /**
       * returns formatted source process name
       * 
       * @return
       */
      public String getAbortedProcessName()
      {
         return ProcessInstanceUtils.getProcessLabel(sourceProcessInstance);
      }

      public boolean isSuccess()
      {
         return success;
      }

      public String getAbortedProcessInstance()
      {
         return abortedProcessInstance;
      }
   }
   
   public boolean getMultiSelected()
   {
      return CollectionUtils.isNotEmpty(getSourceProcessInstances()) && getSourceProcessInstances().size() > 1
            ? true
            : false;
   }

   public Integer getModelOID()
   {
      return modelOID;
   }

}
