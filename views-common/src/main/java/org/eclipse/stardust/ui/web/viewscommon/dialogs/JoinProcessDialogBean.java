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

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.search.RelatedProcessSearchHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.search.RelatedProcessTableEntry;



/**
 * 
 * @author vikas.mishra
 * @since 7.0
 * 
 *        bean for Join process dialog
 * 
 */
public class JoinProcessDialogBean extends PopupUIComponentBean implements ICallbackHandler, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "joinProcessDialogBean";
   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   private final RelatedProcessSearchHelper relatedProcessSearchHelper = new RelatedProcessSearchHelper();

   private JoinProcessPage currentPage;
   private String linkComment;
   private Long processOid;
   private ProcessInstance sourceProcessInstance;
   private ProcessInstance targetProcessInstance;
   private ICallbackHandler iCallbackHandler;
   private boolean targetProcessSupportProcessAttachment;
   private Scope scope;
   private ConfirmationDialog joinProcessConfirmationDialog;

   /**
    * 
    */
   public static enum JoinProcessPage 
   {
      SEARCH, ADVANCE, NOTIFICATION;
   }

   /**
    * 
    * @author Sidharth.Singh
    * @version $Revision: $
    */
   public enum Scope {
      Case, Process;
   }
   
   /**
    * method to get SwitchProcessDialogBean instance
    * 
    * @return SwitchProcessDialogBean object
    */
   public static JoinProcessDialogBean getInstance()
   {
      return (JoinProcessDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * reset instance values
    */
   public void reset()
   {
      relatedProcessSearchHelper.reset();
      linkComment = null;
      processOid = null;
      sourceProcessInstance = null;
      targetProcessInstance = null;
      iCallbackHandler = null;
      currentPage = null;
   }

   private void resolveScope()
   {
      scope = getSourceProcessInstance().isCaseProcessInstance() ? Scope.Case : Scope.Process;
   }
   
   /**
    * initialize instance values
    */
   @Override
   public void initialize()
   {
      // join process should always execute on Root process instance of source process
      // instance
      sourceProcessInstance = ProcessInstanceUtils.getRootProcessInstance(sourceProcessInstance, true);
      currentPage = JoinProcessPage.SEARCH; // always first page
      List<ProcessInstance> list = CollectionUtils.newArrayList();
      list.add(sourceProcessInstance);
      relatedProcessSearchHelper.setSourceProcessInstances(list);
      relatedProcessSearchHelper.initialize();
   }

   /**
    * JSF action method open Join Process Dialog
    */
   @Override
   public void openPopup()
   {
      try
      {
         resolveScope();
         getRelatedProcessSearchHelper().setSearchCases(Scope.Case.equals(scope) ? true : false);
         // join process should always execute on Root process instance of source process
         // instance
         sourceProcessInstance = ProcessInstanceUtils.getRootProcessInstance(sourceProcessInstance, true);
         if (showNotificationForAbortedSource())
         {
            return;
         }
         initialize();
         super.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * prepare MessageDialog error message if source process is already aborted.
    * 
    */
   private boolean showNotificationForAbortedSource()
   {
      MessagePropertiesBean messageBean = MessagePropertiesBean.getInstance();
      if (sourceProcessInstance.isCaseProcessInstance()
            && !AuthorizationUtils.hasManageCasePermission(sourceProcessInstance))
      {
         MessageDialog.addMessage(MessageType.ERROR, messageBean.getString("common.error"),
               COMMON_MESSAGE_BEAN.getString("common.authorization.msg"));
         return true;
      }
      else if (!ProcessInstanceUtils.isAbortableState(sourceProcessInstance))
      {
         MessageDialog.addMessage(MessageType.ERROR, messageBean.getString("common.error"),
               COMMON_MESSAGE_BEAN.getString("common.notifyProcessAlreadyAborted"));
         return true;
      }
      return false;
   }

   /**
    * method to validate selected process OID if input process OID is alreay aborted or
    * completed then shows error message if input process OID not exist in shows error
    * message
    * 
    * @param event
    */
   private void validateProcess()
   {

      if (null == processOid)
      {
         if (Scope.Process.equals(scope))
         {
         FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.joinProcessDialog.inputProcess.message"));
         }
         else
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.joinCaseDialog.inputProcess.message"));
         }
         return;
      }
      else
      {
         targetProcessInstance = ProcessInstanceUtils.getProcessInstance(processOid);
      }

      if (null == targetProcessInstance)
      {
         if (Scope.Process.equals(scope))
         {
         FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.common.process.invalidProcess.message"));
         }
         else
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.inputIsProcess.message"));
         }
      }
      else if (targetProcessInstance.getState().getValue() == ProcessInstanceState.ABORTED)
      {
         if (Scope.Process.equals(scope))
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("common.notifyProcessAlreadyAborted"));
         }
         else
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.specifyActiveCase"));
         }
         targetProcessInstance = null;
      }
      else if (targetProcessInstance.getState().getValue() == ProcessInstanceState.COMPLETED)
      {
         if (Scope.Process.equals(scope))
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("common.notifyProcessAlreadyCompleted"));
         }
         else
         {
            FacesUtils.addErrorMessage("", COMMON_MESSAGE_BEAN.getString("views.attachToCase.specifyActiveCase"));
         }
         targetProcessInstance = null;
      }
      else if (targetProcessInstance.getOID() == sourceProcessInstance.getOID())
      {
         if (Scope.Process.equals(scope))
         {
         FacesUtils.addErrorMessage("",
               COMMON_MESSAGE_BEAN.getString("views.common.process.invalidTargetProcess.message"));
         }
         else
         {
            FacesUtils.addErrorMessage("",
                  COMMON_MESSAGE_BEAN.getString("views.joinCaseDialog.invalidCase.message"));
         }
         targetProcessInstance = null;
      }
      else if(Scope.Process.equals(scope)  & ProcessDefinitionUtils.isCaseProcess(targetProcessInstance.getProcessID()))
      {
         FacesUtils.addErrorMessage("",
               COMMON_MESSAGE_BEAN.getString("views.common.process.invalidProcess.message"));
         targetProcessInstance = null;
      }
      else if(Scope.Case.equals(scope)  & !(ProcessDefinitionUtils.isCaseProcess(targetProcessInstance.getProcessID())))
      {
         FacesUtils.addErrorMessage("",
               COMMON_MESSAGE_BEAN.getString("views.joinCaseDialog.invalidCase.message1"));
         targetProcessInstance = null;
      }

   }

   
   /**
    * jsf action method to toggle page toggle Search to Advanced or Advanced to Search
    * page
    */
   public void toggleView()
   {
      if (JoinProcessPage.SEARCH.equals(currentPage))
      {
         currentPage = JoinProcessPage.ADVANCE;
      }
      else
      {
         currentPage = JoinProcessPage.SEARCH;
      }
      targetProcessInstance = null;
      processOid = null;
      linkComment = null;
      RelatedProcessTableEntry tableEntry = relatedProcessSearchHelper.getSelectedProcessInstance();
      if (null != tableEntry)
      {
         tableEntry.setSelected(false);
      }
      FacesUtils.clearFacesTreeValues();
   }

   /**
    * change search result from Match any to Match All or vise versa.
    */
   public void toggleMatch()
   {
      boolean matchAny = relatedProcessSearchHelper.isMatchAny();
      relatedProcessSearchHelper.setMatchAny(!matchAny);
      relatedProcessSearchHelper.update();
   }

   /**
    * JSF action to abort source process instance and join with selected or given process
    * instance. * also @see WorkflowService#joinProcessInstance(long, long, String);
    */
   public void abortAndJoin()
   {
      try
      {
         if (JoinProcessPage.SEARCH.equals(currentPage))
         {
            RelatedProcessTableEntry tableEntry = relatedProcessSearchHelper.getSelectedProcessInstance();

            if (null != tableEntry)
            {
               targetProcessInstance = tableEntry.getProcessInstance();
            }
         }
         else if (JoinProcessPage.ADVANCE.equals(currentPage))
         {
            validateProcess();
         }
         if (null != targetProcessInstance)
         {
            if (Scope.Process.equals(scope))
            { 
            targetProcessInstance = ServiceFactoryUtils.getWorkflowService().joinProcessInstance(
                  sourceProcessInstance.getOID(), targetProcessInstance.getOID(), linkComment);
            }
            else
            {
               targetProcessInstance = ServiceFactoryUtils.getWorkflowService().mergeCases(
                     targetProcessInstance.getOID(), new long[] {sourceProcessInstance.getOID()},linkComment);
               CommonDescriptorUtils.reCalculateCaseDescriptors(sourceProcessInstance);
               CommonDescriptorUtils.reCalculateCaseDescriptors(targetProcessInstance);
            }
            targetProcessSupportProcessAttachment = DocumentMgmtUtility
                  .isProcessAttachmentAllowed(targetProcessInstance);

            openConfirmationDialog();
         }
      }
      catch (Exception e)
      {
         closeJoinProcessPopup();
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * Information dialog showing Aborted and Joined process information
    */
   public void openConfirmationDialog()
   {
      joinProcessConfirmationDialog = new ConfirmationDialog(DialogContentType.INFO, DialogActionType.YES_NO, null,
            DialogStyle.COMPACT, this);
      joinProcessConfirmationDialog.setIncludePath(ResourcePaths.V_JOIN_PROCESS_CONF_DLG);
      super.closePopup();
      joinProcessConfirmationDialog.openPopup();
   }
   
   /**
    * 
    */
   public boolean accept()
   {
      joinProcessConfirmationDialog = null;
      openProcess();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      joinProcessConfirmationDialog = null;
      closeJoinProcessPopup();
      return true;
   }
   
   /**
    * JSF action to open Joined process in Process Context explorer view
    */
   public void openProcess()
   {
      ProcessInstanceUtils.openProcessContextExplorer(targetProcessInstance);
      closeJoinProcessPopup();
   }
   

   
   public boolean isTargetProcessSupportProcessAttachment()
   {
      return targetProcessSupportProcessAttachment;
   }

   /**
     *
     */
   public void handleEvent(EventType eventType)
   {}

   /**
    * Method to close Join Process Popup Dialog
    */
   public void closeJoinProcessPopup()
   {
      if (null != iCallbackHandler && JoinProcessPage.NOTIFICATION.equals(currentPage))
      {
         iCallbackHandler.handleEvent(EventType.APPLY);
      }

      targetProcessInstance = null;
      reset();
      super.closePopup();
   }

   /**
    * 
    * @return
    */
   public boolean isEnableAbortAndJoin()
   {
      if (JoinProcessPage.SEARCH.equals(currentPage))
      {
         return CollectionUtils.isNotEmpty(relatedProcessSearchHelper.getRelatedProcessTable().getList())
               && relatedProcessSearchHelper.getSelectedProcessInstance() != null;
      }
      else
      {
         return true;
      }
   }

   /**
    * 
    * @return current page value possible values are (SEARCH, ADVANCE, NOTIFICATION )
    */
   public JoinProcessPage getCurrentPage()
   {
      return currentPage;
   }

   /**
    * 
    * @return
    */
   public String getLinkComment()
   {
      return linkComment;
   }

   /**
    * 
    * @param linkComment
    */
   public void setLinkComment(String linkComment)
   {
      this.linkComment = linkComment;
   }

   public Long getProcessOid()
   {
      return processOid;
   }

   public void setProcessOid(Long processOid)
   {
      this.processOid = processOid;
   }

   /**
    * 
    * @return
    */
   public String getJoinProcessMessage()
   {
      return COMMON_MESSAGE_BEAN.getParamString("views.joinProcessDialog.processJoined",
            ProcessInstanceUtils.getProcessLabel(sourceProcessInstance),
            ProcessInstanceUtils.getProcessLabel(targetProcessInstance));
   }

   /**
    * 
    * @return
    */
   public ProcessInstance getSourceProcessInstance()
   {
      return sourceProcessInstance;
   }

   /**
    * 
    * @param sourceProcessInstance
    */
   public void setSourceProcessInstance(ProcessInstance sourceProcessInstance)
   {
      this.sourceProcessInstance = sourceProcessInstance;
   }

   /**
    * 
    * @return
    */
   public ProcessInstance getTargetProcessInstance()
   {
      return targetProcessInstance;
   }
   

   /**
    * 
    * @return
    */
   public String getDialogTitle()
   {
      return COMMON_MESSAGE_BEAN.getParamString("views.joinProcessDialog.title",
            ProcessInstanceUtils.getProcessLabel(sourceProcessInstance));
   }

   /**
    * 
    * @return
    */
   public RelatedProcessSearchHelper getRelatedProcessSearchHelper()
   {
      return relatedProcessSearchHelper;
   }
   
   /**
    * 
    * @param currentPage
    */
   public void setCurrentPage(JoinProcessPage currentPage)
   {
      this.currentPage = currentPage;
   }
   /**
    * 
    * @param callbackHandler
    */
   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      iCallbackHandler = callbackHandler;
   }

   public Scope getScope()
   {
      return scope;
   }

   public void setScope(Scope scope)
   {
      this.scope = scope;
   }
   
   public ConfirmationDialog getJoinProcessConfirmationDialog()
   {
      return joinProcessConfirmationDialog;
   }

}
