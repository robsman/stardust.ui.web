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
package org.eclipse.stardust.ui.web.viewscommon.views.casemanagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.LinkDirection;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLinkType;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.JoinProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.participantspanel.ParticipantsPanelBean;
import org.eclipse.stardust.ui.web.viewscommon.process.history.ProcessHistoryTable;
import org.eclipse.stardust.ui.web.viewscommon.process.history.ProcessInstanceHistoryItem;
import org.eclipse.stardust.ui.web.viewscommon.processContextExplorer.DescriptorItemTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.utils.AbortProcessBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean.RepositoryMode;



public class CaseDetailsBean extends PopupUIComponentBean
      implements ViewEventHandler, ICallbackHandler, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(CaseDetailsBean.class);

   private static final String STATE_PREFIX = "views.processTable.statusFilter.";
   private static final String VIEW_ID = "caseDetailsView"; 
   private ProcessInstance processInstance;
   private Long processInstanceOID;
   private boolean hideCompletedActivities;
   private SortableTable<DescriptorItemTableEntry> descriptorTable;
   private final MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();

   private ProcessTableHelper processHelper;
   private String duration;
   private String activityNotes;
   private boolean abortProcess;
   private String state;
   private boolean supportsProcessAttachments;

   private boolean overviewPanelExpanded = true;
   private boolean descriptorPanelExpanded = false;
   private boolean processHistoryPanelExpanded = false;
   private boolean processDocumentsPanelExpanded = false;
   private boolean participantsPanelExpanded = false;
   private boolean linkedProcessPanelExpanded = false;
   
   private boolean processHistoryTreeInitialized;
   private boolean genericRepositoryTreeInitialized = false;
   private boolean genericRepositoryTreeExpanded = false;
   private boolean descriptorsPanelInitialized = false;
   private boolean participantsPanelInitialized = false;
   private boolean linkedProcessPanelInitialized = false;
   
   private ActivityInstance activityInstance;
   private boolean editProcessName = false;
   private String processName;
   private String caseOwnerLabel;
   private ProcessInstance detachProcessInstance;
   private ConfirmationDialog detachCaseConfirmationDialog;
   private String description;   
   private boolean editDescription = false;
   
   private boolean hasCreateCasePermission;
   private boolean hasManageCasePermission;   
   private boolean hasSpawnProcessPermission;
   private boolean hasSwitchProcessPermission;
   private boolean hasJoinProcessPermission;

   public CaseDetailsBean()
   {
      super(VIEW_ID);
   }
   
   @Override
   public void initialize()
   {
      processInstance = ProcessInstanceUtils.getProcessInstance(processInstanceOID, true, true);
      duration = ProcessInstanceUtils.getDuration(processInstance);
      processName = getCaseName(processInstance);
      description= getCaseDescription(processInstance);
      activityInstance = ActivityInstanceUtils.getActivityInstance(processInstance);
      caseOwnerLabel = ProcessInstanceUtils.getCaseOwnerName(processInstance);
      state = MessagesViewsCommonBean.getInstance().getString(
            STATE_PREFIX + processInstance.getState().getName().toLowerCase());

      if (descriptorsPanelInitialized)
      {
         initializeDescriptorsPanel();
      }
      if (processHistoryTreeInitialized)
      {
         initializeProcessHistoryTree();
      }

      if (genericRepositoryTreeInitialized)
      {
         initializeGenericRepositoryTree();
      }
      if (participantsPanelInitialized)
      {
         initializeParticipantsPanel();
      }
      if (linkedProcessPanelInitialized)
      {
         initializeLinkedProcessPanel();
      }

      setSupportsProcessAttachments(DMSHelper.existsProcessAttachmentsDataPath(processInstance));
      setAbortProcess(ProcessInstanceUtils.isAbortable(processInstance));
      state = MessagesViewsCommonBean.getInstance().getString(
            STATE_PREFIX + processInstance.getState().getName().toLowerCase());

   }

   private void initializePermissions()
   {
      hasSpawnProcessPermission = AuthorizationUtils.hasSpawnProcessPermission();
      hasSwitchProcessPermission = AuthorizationUtils.hasAbortAndStartProcessInstancePermission();
      hasJoinProcessPermission = AuthorizationUtils.hasAbortAndJoinProcessInstancePermission();
      hasCreateCasePermission = AuthorizationUtils.canCreateCase();
   }
   /**
    * 
    */
   private void initializeGenericRepositoryTree()
   {
      trace.info("-----------> GenericRepositoryTree Initialize");
      GenericRepositoryTreeViewBean genericRepositoryTree = GenericRepositoryTreeViewBean.getInstance();
      if (genericRepositoryTree.isEditingModeOff())
      {
         genericRepositoryTree.setRepositoryMode(RepositoryMode.CASE_DOCUMENTS);
         genericRepositoryTree.setProcessInstance(processInstance);
         genericRepositoryTree.initialize();
         genericRepositoryTreeInitialized = true;
      }
      trace.info("-----------> GenericRepositoryTree Initialize");
   }

   /**
    * When initialing this in View event observed issues with view scoping beans Hence do
    * this in expand handler methods for 1st time
    */
   private void initializeProcessHistoryTree()
   {
      trace.info("-----------> ProcessHistoryTree Initialize");

      ProcessHistoryTable processHistoryTable = ProcessHistoryTable.getCurrent();
      processHistoryTable.setEnableCase(true);
      processHistoryTable.setCurrentProcessInstance(processInstance);
      processHistoryTable.setEmbedded(true);
      processHistoryTable.initialize();
      processHistoryTreeInitialized = true;

      trace.info("<----------- ProcessHistoryTree Initialize");
   }

   /**
    * load participant panel
    */
   private void initializeParticipantsPanel()
   {
      trace.info("-----------> Participant Panel Initialize");
      if (!processHistoryTreeInitialized)
      {
         initializeProcessHistoryTree();
      }
      ParticipantsPanelBean participantsList = ParticipantsPanelBean.getCurrent();
      participantsList.setCurrentProcessInstance(processInstance);
      participantsList.setEmbedded(true);
      participantsList.setShowTitle(false);

      participantsList.setCaseTreeRootElements(ProcessHistoryTable.getCurrent().getActivityTreeTable()
            .getCaseActivitiesRoot());

      participantsList.initialize();
      participantsPanelInitialized = true;
      trace.info("-----------> Participant Panel Initialize");
   }
  
   /**
    * 
    */
   private void initializeLinkedProcessPanel()
   {
      trace.info("-----------> Linked Process Panel Initialize");
      if (!linkedProcessPanelInitialized)
      {
         initializeProcessTable();
      }
      processHelper.getProcessTable().refresh(true);
      trace.info("-----------> Linked Process Panel Initialized");
   }
   
   /**
    * 
    */
   private void initializeProcessTable()
   {
      trace.info("-----------> Linked Process Table Initialize");

      processHelper = new ProcessTableHelper();
      processHelper.getColumnModelListener().setNeedRefresh(false);
      processHelper.setCallbackHandler(this);
      processHelper.setDisplayLinkInfo(true);
      processHelper.setProcessInstance(processInstance);
      processHelper.initializeProcessTable(UserPreferencesEntries.M_VIEWS_COMMON, VIEW_ID);
      processHelper.getProcessTable().initialize();
      processHelper.getProcessTable().setISearchHandler(new ProcessTableSearchHandler());
      linkedProcessPanelInitialized = true;
      processHelper.getColumnModelListener().setNeedRefresh(true);
      
      trace.info("<----------- Linked Process Table Initialized");
   }
   /**
    * 
    */
   public void handleEvent(ViewEvent event)
   {	   
      if (ViewEventType.CREATED == event.getType())
      {
         View focusView = event.getView();
         if (focusView != null && processInstance == null) // Only when first time created
         {
            String pOID = focusView.getParamValue("processInstanceOID");
            focusView.getViewParams().put(
                  "processInstanceName",
                  I18nUtils.getProcessName(ProcessDefinitionUtils.getProcessDefinition(ProcessInstanceUtils
                        .getProcessInstance(Long.valueOf(pOID)).getProcessID())));
            if (!StringUtils.isEmpty(pOID))
            {
               try
               {
                  processInstanceOID = Long.parseLong(pOID);
                  initialize();
                  hasManageCasePermission = AuthorizationUtils.hasManageCasePermission(processInstance);
               }
               catch (Exception ex)
               {
                  event.setVetoed(true);
                  ExceptionHandler.handleException(ex);
               }
            }
         }
         initializePermissions();        
      }
   }

   /**
    * 
    */
   public void attachToCase(ActionEvent event)
   {
      List<ProcessInstance> selectedProcesses = CollectionUtils.newArrayList();
      selectedProcesses.add(processInstance);
      ProcessInstanceUtils.openAttachToCase(selectedProcesses);
   }
   
   /**
    * 
    * @param ae
    */
   public void detachFromCase(ActionEvent ae)
   {
      try
      {
         ProcessInstance pi = (ProcessInstance) ae.getComponent().getAttributes().get("processInstance");
         if (pi.getOID() == pi.getRootProcessInstanceOID())
         {
            MessagePropertiesBean messageBean = MessagePropertiesBean.getInstance();
            MessageDialog.addMessage(MessageType.ERROR, messageBean.getString("common.error"),
                  COMMON_MESSAGE_BEAN.getString("views.attachToCase.caseDetach.rootError"));
            return;
         }

         detachProcessInstance = pi;
         ProcessInstance rootProcessInstance = ProcessInstanceUtils.getRootProcessInstance(pi, false);
         Set<Long> childPIs = countImmediateChilds(rootProcessInstance);

         if (childPIs.contains(pi.getOID()) && childPIs.size() == 1)
         {
            openConfirmationDialog();
            return;
         }
         else
         {
            detachProcess();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   /**
    * Dialog to Confirm Detach from case notification
    */
   public void openConfirmationDialog()
   {
      detachCaseConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO, null,
            DialogStyle.COMPACT, this);
      detachCaseConfirmationDialog.setIncludePath(ResourcePaths.V_DETACH_CASE_CONF_DLG);
      detachCaseConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      detachCaseConfirmationDialog = null;
      detachProcess();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      detachCaseConfirmationDialog = null;
      return true;
   }
   
   private Set<Long> countImmediateChilds(ProcessInstance rootProcessInstance)
   {
      List<ProcessInstanceHistoryItem> childPIs = ProcessHistoryTable.getCurrent().getProcessTreeTable()
            .getProcessHistoryTableRoot().getChildren();
      Set<Long> processOIDs = CollectionUtils.newHashSet();
      for (ProcessInstanceHistoryItem pi : childPIs)
      {
         processOIDs.add(pi.getOID());
      }
      return processOIDs;
   }


   public void detachProcess()
   {
      long[] pis = {detachProcessInstance.getOID()};
      ServiceFactoryUtils.getWorkflowService().leaveCase(detachProcessInstance.getRootProcessInstanceOID(), pis);
      ProcessInstance rootProcess = ProcessInstanceUtils.getProcessInstance(detachProcessInstance
            .getRootProcessInstanceOID());
      CommonDescriptorUtils.reCalculateCaseDescriptors(rootProcess);
      update();
   }

   /**
    * Creates the Descriptor table to display
    */
   private void initializeDescriptorsPanel()
   {
      try
      {
         if (null == processInstance)
         {
            return;
         }

         // If descriptor panel is already initialized then do not build the table again
         if (!descriptorsPanelInitialized)
         {
            initializeDescriptorColumns();
         }
         List<DescriptorItemTableEntry> descriptorList = convertToTableEntries(CommonDescriptorUtils
               .createCaseDescriptors(processInstance));
         descriptorTable.setList(descriptorList);         
         descriptorsPanelInitialized = true;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param processDescriptors
    * @return
    */
   private List<DescriptorItemTableEntry> convertToTableEntries(List<ProcessDescriptor> processDescriptors)
   {
      List<DescriptorItemTableEntry> descriptorsEntries = CollectionUtils.newList();
      for (ProcessDescriptor processDescriptor : processDescriptors)
      {
         descriptorsEntries.add(new DescriptorItemTableEntry(processDescriptor));
      }
      return descriptorsEntries;
   }
   
   /**
    * Initializes Descriptor columns
    */
   private void initializeDescriptorColumns()
   {
      ColumnPreference nameCol = new ColumnPreference("Name", "name",
            ColumnDataType.STRING, COMMON_MESSAGE_BEAN.getString("views.processInstanceDetailsView.column.name"), true,
            true);
      ColumnPreference valueCol = new ColumnPreference("Value", "value",
            ColumnDataType.STRING, COMMON_MESSAGE_BEAN.getString("views.processInstanceDetailsView.column.value"), true,
            true);
      valueCol.setEscape(false);

      List<ColumnPreference> descriptoCols = new ArrayList<ColumnPreference>();
      descriptoCols.add(nameCol);
      descriptoCols.add(valueCol);      

      IColumnModel descriptorColumnModel = new DefaultColumnModel(descriptoCols, null,
            null, UserPreferencesEntries.M_VIEWS_COMMON, UserPreferencesEntries.V_PROCESS_INSTANCE_DETAILS);
      descriptorTable = new SortableTable<DescriptorItemTableEntry>(
            descriptorColumnModel, null,
            new SortableTableComparator<DescriptorItemTableEntry>("name", true));      
      descriptorTable.initialize();      
   }

   /**
    * Terminates process
    */
   public void terminateProcess()
   {
      if (processInstance != null)
      {
         AbortProcessBean abortProcessHelper = AbortProcessBean.getInstance();
         abortProcessHelper.setCallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               initialize();
            }
         });
         abortProcessHelper.abortProcess(processInstance);
      }
   }

   /**
    * Recovers the process
    */
   public void recoverProcess()
   {
      List<Long> processOids = new ArrayList<Long>();
      if (processInstance != null)
      {
         try
         {
            processOids.add(processInstance.getOID());
            ProcessInstanceUtils.recoverProcessInstance(processOids);
         }
         catch (AccessForbiddenException e)
         {
            MessageDialog.addErrorMessage(this.getMessages().getString("authorization.msg"), e);
         }
      }
      initialize();
   }

   /**
    * Open new Correspondence View
    */
   public void openCorrespondence()
   {
      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("processInstanceOID", Long.toString(processInstance.getOID()));
         PortalApplication.getInstance().openViewById("correspondenceView", "DocumentID=" + processInstance.getOID(),
               params, null, true);
      }
   }

   /**
    * Open new Chat View
    */
   public void openChat()
   {
      if (null != processInstance)
      {
         Map<String, Object> params = CollectionUtils.newTreeMap();
         params.put("processInstanceOID", Long.toString(processInstance.getOID()));
         PortalApplication.getInstance().openViewById("chatView", "processInstanceOID=" + processInstance.getOID(),
               params, null, true);
      }
   }

   /**
    * Updates the view
    */
   public void update()
   {
      initialize();
      initializePermissions();
   }
   
   /**
    * Get the descriptor value which represents the CaseName
    * 
    * @param processInstance
    * @return
    */
   private String getCaseName(ProcessInstance processInstance)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
      return (String) processInstanceDetails.getDescriptors().get(PredefinedConstants.CASE_NAME_ELEMENT);
   }
   
   /**
    * Recalculate descriptors
    */
   public void updateDescriptors()
   {
      CommonDescriptorUtils.reCalculateCaseDescriptors(processInstance);
      initialize();
   }

   /**
    * 
    */
   public void enableEditProcessName()
   {
      this.editProcessName = true;
   }

   /**
   * 
   */
   public void updateProcessName()
   {
      String notEditProcessName = getCaseName(processInstance);
      String tempProcessName = processName.trim();
      if (StringUtils.isNotEmpty(tempProcessName))
      {
         if (!notEditProcessName.equals(processName))
         {
            CommonDescriptorUtils.updateCaseDescriptor(processInstance, tempProcessName, false);
            update();
         }
      }
      else
      {
         processName = notEditProcessName;
      }
      this.editProcessName = false;

   }

   /**
    * 
    */
   public void cancelEditProcessName()
   {
      this.processName = getCaseName(processInstance);
      this.editProcessName = false;
   }
   
   /**
    * Get the descriptor value which represents the CaseName
    * 
    * @param processInstance
    * @return
    */
   private String getCaseDescription(ProcessInstance processInstance)
   {
      ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
      return (String) processInstanceDetails.getDescriptors().get(PredefinedConstants.CASE_DESCRIPTION_ELEMENT);
   }
   
   /**
    * 
    */
   public void enableEditDescription()
   {
      this.editDescription = true;
   }

   /**
    * 
    */
    public void updateDescription()
    {
      this.editDescription = false;
      String notEditDescription = getCaseDescription(processInstance);

      if (StringUtils.isEmpty(description) && StringUtils.isEmpty(notEditDescription))
      {
         return;
      }
      else if (StringUtils.isNotEmpty(description) && description.equals(notEditDescription))
      {
         return;
      }
      else
      {
         String tempProcessName = processName;
         CommonDescriptorUtils.updateCaseDescriptor(processInstance, this.description.trim(), true);
         update();
         if (editProcessName)
         {
            processName = tempProcessName;
         }
        
      }

    }

   /**
    * 
    */
   public void cancelEditDescription()
   {
      this.description = getCaseDescription(processInstance);
      this.editDescription = false;
   }
   
   /**
    * 
    * @param ae
    */
   public void openDelegateDialog(ActionEvent ae)
   {
      List<ActivityInstance> ais = CollectionUtils.newArrayList();
      ais.add(activityInstance);
      DelegationBean delegationBean = (DelegationBean) FacesUtils.getBeanFromContext("delegationBean");
      delegationBean.setAis(ais);
      delegationBean.setDelegateCase(true);
      delegationBean.setICallbackHandler(this);
      delegationBean.openPopup();
   }

   /**
    * action listener to open Join Case Instance
    */
   public void openJoinProcess(ActionEvent event)
   {

      JoinProcessDialogBean dialog = JoinProcessDialogBean.getInstance();
      dialog.setSourceProcessInstance(processInstance);
      dialog.openPopup();
   }
   
   /**
    * action listener to open Switch process dialog
    */
   public void openSwitchProcess(ActionEvent event)
   {
      MessagePropertiesBean messageBean = MessagePropertiesBean.getInstance();
      MessageDialog.addMessage(MessageType.ERROR, messageBean.getString("common.error"),
            COMMON_MESSAGE_BEAN.getString("views.switchProcessDialog.caseAbort.message"));
      return;
   }
   
   public boolean isInactiveCase()
   {
      if (ProcessInstanceState.COMPLETED == processInstance.getState().getValue() || ProcessInstanceState.ABORTED == processInstance.getState().getValue())
      {
         return true;
      }
      else
      {
         return false;
      }
   }  

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   public Long getProcessInstanceOID()
   {
      return processInstanceOID;
   }

   public void setProcessInstanceOID(Long processInstanceOID)
   {
      this.processInstanceOID = processInstanceOID;
   }

   public boolean isHideCompletedActivities()
   {
      return hideCompletedActivities;
   }

   public void setHideCompletedActivities(boolean hideCompletedActivities)
   {
      this.hideCompletedActivities = hideCompletedActivities;
   }

   /**
    * @return
    */
   public String getStartingUser()
   {
      return ProcessInstanceUtils.getStartingUser(getProcessInstance());
   }
   
   public ProcessTableHelper getProcessHelper()
   {
      return processHelper;
   }

   public void setProcessHelper(ProcessTableHelper processHelper)
   {
      this.processHelper = processHelper;
   }

   public String getDuration()
   {
      return duration;
   }

   public void setDuration(String duration)
   {
      this.duration = duration;
   }

   public String getActivityNotes()
   {
      return activityNotes;
   }

   public void setActivityNotes(String activityNotes)
   {
      this.activityNotes = activityNotes;
   }

   public boolean isAbortProcess()
   {
      return abortProcess;
   }

   public void setAbortProcess(boolean abortProcess)
   {
      this.abortProcess = abortProcess;
   }

   public String getProcessName()
   {
         return processName;
   }
   
   public void setProcessName(String processName)
   {
      this.processName = processName;
   }
   
   /**
    * @return
    */
   public String getStartTime()
   {
      return DateUtils.formatDateTime(processInstance.getStartTime());
   }

   public String getState()
   {
      return state;
   }

   public void setState(String state)
   {
      this.state = state;
   }

   
   public boolean isSupportsProcessAttachments()
   {
      return supportsProcessAttachments;
   }

   public void setSupportsProcessAttachments(boolean supportsProcessAttachments)
   {
      this.supportsProcessAttachments = supportsProcessAttachments;
   }

   public boolean isOverviewPanelExpanded()
   {
      return overviewPanelExpanded;
   }

   public void setOverviewPanelExpanded(boolean overviewPanelExpanded)
   {
      this.overviewPanelExpanded = overviewPanelExpanded;
   }

   public boolean isDescriptorPanelExpanded()
   {
      return descriptorPanelExpanded;
   }

   public void setDescriptorPanelExpanded(boolean descriptorPanelExpanded)
   {
      this.descriptorPanelExpanded = descriptorPanelExpanded;
      if (!descriptorsPanelInitialized)
      {
         initializeDescriptorsPanel();
      }
   }

   public boolean isProcessHistoryPanelExpanded()
   {
      return processHistoryPanelExpanded;
   }

   public void setProcessHistoryPanelExpanded(boolean processHistoryPanelExpanded)
   {
      this.processHistoryPanelExpanded = processHistoryPanelExpanded;
      if (!processHistoryTreeInitialized)
      {
         initializeProcessHistoryTree();
      }
   }

   public boolean isProcessDocumentsPanelExpanded()
   {
      return processDocumentsPanelExpanded;
   }

   public void setProcessDocumentsPanelExpanded(boolean processDocumentsPanelExpanded)
   {
      this.processDocumentsPanelExpanded = processDocumentsPanelExpanded;
   }

   public boolean isParticipantsPanelExpanded()
   {
      return participantsPanelExpanded;
   }

   public void setParticipantsPanelExpanded(boolean participantsPanelExpanded)
   {
      this.participantsPanelExpanded = participantsPanelExpanded;
      if (!participantsPanelInitialized)
      {
         initializeParticipantsPanel();
      }
   }

   public boolean isProcessHistoryTreeInitialized()
   {
      return processHistoryTreeInitialized;
   }

   public void setProcessHistoryTreeInitialized(boolean processHistoryTreeInitialized)
   {
      this.processHistoryTreeInitialized = processHistoryTreeInitialized;
   }

   public boolean isGenericRepositoryTreeInitialized()
   {
      return genericRepositoryTreeInitialized;
   }

   public boolean isGenericRepositoryTreeExpanded()
   {
      return genericRepositoryTreeExpanded;
   }

   public void setGenericRepositoryTreeExpanded(boolean genericRepositoryTreeExpanded)
   {
      this.genericRepositoryTreeExpanded = genericRepositoryTreeExpanded;
      if (!genericRepositoryTreeInitialized)
      {
         initializeGenericRepositoryTree();
      }
   }

   public boolean isDescriptorsPanelInitialized()
   {
      return descriptorsPanelInitialized;
   }

   public void setDescriptorsPanelInitialized(boolean descriptorsPanelInitialized)
   {
      this.descriptorsPanelInitialized = descriptorsPanelInitialized;
   }

   public boolean isParticipantsPanelInitialized()
   {
      return participantsPanelInitialized;
   }

   public void setParticipantsPanelInitialized(boolean participantsPanelInitialized)
   {
      this.participantsPanelInitialized = participantsPanelInitialized;
   }

   public SortableTable<DescriptorItemTableEntry> getDescriptorTable()
   {
      return descriptorTable;
   }

   public void setDescriptorTable(SortableTable<DescriptorItemTableEntry> descriptorTable)
   {
      this.descriptorTable = descriptorTable;
   }

   public void handleEvent(EventType eventType)
   {
      if (EventType.APPLY.equals(eventType))
      {
         update();
      }
   }

   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public void setActivityInstance(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
   }

   public boolean isEditProcessName()
   {
      return editProcessName;
   }

   public void setEditProcessName(boolean editProcessName)
   {
      this.editProcessName = editProcessName;
   }
   
   public String getCaseOwnerLabel()
   {
      return caseOwnerLabel;
   }

   public void setCaseOwnerLabel(String caseOwnerLabel)
   {
      this.caseOwnerLabel = caseOwnerLabel;
   }

   public String getDescription()
   {
      return description;
   }
   
   public void setDescription(String description)
   {
      if(editDescription)
      {
         this.description = description;   
      }
   }

   /**
    * 
    * @return
    */
   public boolean isCanManageCase()
   {
      return hasManageCasePermission;
   }

   /**
    * 
    * @return
    */
   public boolean isCanCreateCase()
   {
      return hasCreateCasePermission;
   }
   
   public boolean isLinkedProcessPanelExpanded()
   {
      return linkedProcessPanelExpanded;
   }

   /**
    * 
    * @param linkedProcessPanelExpanded
    */
   public void setLinkedProcessPanelExpanded(boolean linkedProcessPanelExpanded)
   {
      this.linkedProcessPanelExpanded = linkedProcessPanelExpanded;
      // Only when pannel is expanded and if not initialized
      if (linkedProcessPanelExpanded & !linkedProcessPanelInitialized)
      {
         initializeLinkedProcessPanel();
      }
   }
   
   public boolean isEditDescription()
   {
      return editDescription;
   }

   public void setEditDescription(boolean editDescription)
   {
      this.editDescription = editDescription;
   }

   public ConfirmationDialog getDetachCaseConfirmationDialog()
   {
      return detachCaseConfirmationDialog;
   }

   /**
    * @author Sidharth.Singh
    * 
    */
   public class ProcessTableSearchHandler extends IppSearchHandler<ProcessInstance>
   {
      private static final long serialVersionUID = 1L;

      /**
       * Creates a query for retrieving the Linked ProcessInstance for current processOID
       */
      @Override
      public Query createQuery()
      {
         String qualifiedGroupId = "{" + PredefinedConstants.PREDEFINED_MODEL_ID + "}"
         + PredefinedConstants.CASE_PROCESS_ID;
         ProcessInstanceQuery query = ProcessInstanceQuery.findLinked(processInstanceOID, LinkDirection.TO_FROM,
               ProcessInstanceLinkType.JOIN);
         ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.Default);
         processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);
         query.setPolicy(processInstanceDetailsPolicy);
         query.getFilter().addAndTerm().add(new ProcessDefinitionFilter(qualifiedGroupId, false));
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         return query;
      }

      @Override
      public QueryResult<ProcessInstance> performSearch(Query query)
      {
         QueryService queryService = ServiceFactoryUtils.getQueryService();
         return queryService.getAllProcessInstances((ProcessInstanceQuery) query);
      }
   }

   public boolean isEnableSpawnProcess()
   {
      return hasSpawnProcessPermission;
   }
   public boolean isEnableSwitchProcess()
   {
      return hasSwitchProcessPermission;
   }
   public boolean isEnableJoinProcess()
   {
      return hasJoinProcessPermission;
   } 
}
