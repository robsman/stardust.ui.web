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
package org.eclipse.stardust.ui.web.viewscommon.processContextExplorer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.LinkDirection;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.JoinProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SwitchProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.participantspanel.ParticipantsPanelBean;
import org.eclipse.stardust.ui.web.viewscommon.process.history.ProcessHistoryTable;
import org.eclipse.stardust.ui.web.viewscommon.utils.AbortProcessBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean.RepositoryMode;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ProcessInstanceDetailsBean extends PopupUIComponentBean
      implements ViewEventHandler,ICallbackHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(ProcessInstanceDetailsBean.class);
   private static final String STATE_PREFIX = "views.processTable.statusFilter.";
   private static final String VIEW_ID = "processInstanceDetailsView";

   private ProcessInstance processInstance;
   private Long processInstanceOID;
   private boolean hideCompletedActivities;
   private SortableTable<DescriptorItemTableEntry> descriptorTable;
   private ProcessTableHelper processHelper;
   private String duration;
   private String activityNotes;
   private boolean supportsProcessAttachments;
   private boolean abortProcess;
   private String state;

   private boolean overviewPanelExpanded = true;
   private boolean descriptorPanelExpanded = false;
   private boolean linkedProcessPanelExpanded = false;
   private boolean processHistoryPanelExpanded = false;
   private boolean processDocumentsPanelExpanded = false;
   private boolean participantsPanelExpanded = false;

   private boolean processHistoryTreeInitialized;
   private boolean genericRepositoryTreeInitialized = false;
   private boolean genericRepositoryTreeExpanded = false;
   private boolean descriptorsPanelInitialized = false;
   private boolean participantsPanelInitialized = false;
   private boolean linkedProcessPanelInitialized = false;
   private boolean hasSpawnProcessPermission;
   private boolean hasSwitchProcessPermission;
   private boolean hasJoinProcessPermission;
   private boolean disableSpawnProcess = false;

   /**
    * 
    */
   public ProcessInstanceDetailsBean()
   {
      super("processInstanceDetailsView");
      hideCompletedActivities = true;
   }

   @Override
   public void initialize()
   {
      trace.debug("-----------> ProcessInstanceDetailsBean Initialize");
      if (processInstanceOID == null)
      {
         return;
      }

      processInstance = ProcessInstanceUtils.getProcessInstance(processInstanceOID, true, true);
     
      duration=ProcessInstanceUtils.getDuration(processInstance);
      // Only initialize in refresh/update, for 1st time this will be initialized in
      // expand methods
      if (processHistoryTreeInitialized)
      {
         initializeProcessHistoryTree();
      }

      if (genericRepositoryTreeInitialized)
      {
         initializeGenericRepositoryTree();
      }

      if (descriptorsPanelInitialized)
      {
         initializeDescriptorsPanel();
      }
      
      if (linkedProcessPanelInitialized)
      {
         initializeLinkedProcessPanel();
      }

      if (participantsPanelInitialized)
      {
         initializeParticipantsPanel();
      }
      
      setSupportsProcessAttachments(DMSHelper.existsProcessAttachmentsDataPath(processInstance));
      setAbortProcess(ProcessInstanceUtils.isAbortable(processInstance));
      List<ProcessInstance> sourceList = CollectionUtils.newArrayList();
      sourceList.add(processInstance);
      setDisableSpawnProcess(ProcessInstanceUtils.isTerminatedProcessInstances(sourceList));
      state = MessagesViewsCommonBean.getInstance().getString(
            STATE_PREFIX + processInstance.getState().getName().toLowerCase());
   
      trace.debug("<----------- ProcessInstanceDetailsBean Initialize");
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         View focusView = event.getView();
         if (focusView != null && processInstance == null) // Only when first time created
         {
            String pOID = focusView.getParamValue("processInstanceOID");
            ProcessInstance pi = ProcessInstanceUtils.getProcessInstance(Long.valueOf(pOID));
            ProcessDefinition processDef = ProcessDefinitionUtils.getProcessDefinition(pi.getModelOID(),
                  pi.getProcessID());
            focusView.getViewParams().put("processInstanceName", I18nUtils.getProcessName(processDef));
            
            if (!StringUtils.isEmpty(pOID))
            {
               try
               {
                  processInstanceOID = Long.parseLong(pOID);
                  initialize();
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
      if (ViewEventType.POST_OPEN_LIFECYCLE == event.getType())
      {
         View focusView = event.getView();
         if (focusView != null && processInstance != null)
         {
            setProcessDescription(processInstance, focusView);
         }
      }
   }
  
   /**
    * 
    * @param processInstance
    * @param focusView
    * @return
    */
   private void setProcessDescription(ProcessInstance processInstance, View focusView)
   {
      String tooltip = null;
      ProcessDefinition processDef = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      String description = I18nUtils.getDescriptionAsHtml(processDef, processDef.getDescription());
      if (StringUtils.isNotEmpty(description))
      {
         tooltip = MessagesViewsCommonBean.getInstance().getParamString("views.processInstanceDetailsView.tooltip",
               focusView.getTooltip(), description);
         focusView.setTooltip(tooltip);
      }
   }

   private void initializePermissions()
   {
      hasSpawnProcessPermission = AuthorizationUtils.hasSpawnProcessPermission();
      hasSwitchProcessPermission = AuthorizationUtils.hasAbortAndStartProcessInstancePermission();
      hasJoinProcessPermission = AuthorizationUtils.hasAbortAndJoinProcessInstancePermission();
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
    * @return
    */
   public String getStartTime()
   {
      return DateUtils.formatDateTime(processInstance.getStartTime());
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
    * action listener to open Switch process dialog
    */
   public void openSwitchProcess(ActionEvent event)
   {
      SwitchProcessDialogBean dialog = SwitchProcessDialogBean.getInstance();
      List<ProcessInstance> sourceList = CollectionUtils.newArrayList();
      sourceList.add(processInstance);
      dialog.setSourceProcessInstances(sourceList);
      dialog.openPopup();
   }
   
   /**
    * action listener to open Join process
    */
   public void openJoinProcess(ActionEvent event)
   {

      JoinProcessDialogBean dialog = JoinProcessDialogBean.getInstance();
      dialog.setSourceProcessInstance(processInstance);
      dialog.openPopup();

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
            MessageDialog.addErrorMessage(this.getMessages().getString(
                  "authorization.msg"), e);
         }
      }
      initialize();
   }

   /**
    * Gets the Activity Instance from context and gets the Note data for resubmission
    * activity
    * 
    * @param ae
    */
   public void prepareActivityNotesDialog(ActionEvent ae)
   {
      Map< ? , ? > param = FacesContext.getCurrentInstance().getExternalContext()
            .getRequestParameterMap();
      Object obj = param.get("selectedActivityInstance");

      Long oid = new Long(obj.toString());
      ActivityInstance activityInstance = null;
      ActivityInstanceQuery aiq = ActivityInstanceQuery.findAll();
      aiq.getFilter().add(ActivityInstanceQuery.OID.isEqual(oid.longValue()));

      try
      {
         QueryService queryService = ServiceFactoryUtils.getQueryService(); 
         Iterator<ActivityInstance> aIter = queryService.getAllActivityInstances(aiq).iterator();
         while (aIter.hasNext())
         {
            activityInstance = aIter.next();
         }
         activityNotes = getNotes(activityInstance);
         openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * Gets the DescriptorItem object
    * 
    * @param dp
    * @param dataValue
    * @return
    */
   protected DescriptorItem getDescriptorItem(DataPath dp, Object dataValue)
   {
      Object value = dataValue;
      if ("PROCESS_PRIORITY".equals(dp.getData()) && dataValue instanceof Number)
      {
         value = ProcessInstanceUtils.getPriorityLabel(((Number) dataValue).intValue());
      }
      if (value != null)
      {
         try
         {
            value.toString();
         }
         catch (Exception e)
         {
            value = Localizer.getString(LocalizerKey.ERROR_WHILE_GETTING_VALUE);
         }
      }
      return new DescriptorItem(processInstance, I18nUtils.getDataPathName(dp), value);
   }
   
   /**
    * Retrieves activity Notes
    * @param activityInstance
    * @return
    */
   private String getNotes(ActivityInstance activityInstance)
   {
      if (activityInstance != null
            && activityInstance.isScopeProcessInstanceNoteAvailable())
      {
         try
         {
            WorkflowService ws = ServiceFactoryUtils.getWorkflowService();
            ProcessInstance pi = ws.getProcessInstance(activityInstance
                  .getProcessInstanceOID());
            if (pi.getOID() != pi.getScopeProcessInstanceOID())
            {
               pi = ws.getProcessInstance(pi.getScopeProcessInstanceOID());
            }
            ProcessInstanceAttributes pia = pi.getAttributes();
            List<Note> notes = pia.getNotes();
            if (notes.isEmpty())
            {
               return "";
            }
            else
            {
               Note note = notes.get(notes.size() - 1);
               return note.getText();
            }
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
      return null;
   }

   /**
    * When initialing this in View event observed issues with view scoping beans Hence do
    * this in expand handler methods for 1st time
    */
   private void initializeProcessHistoryTree()
   {
      trace.debug("-----------> ProcessHistoryTree Initialize");

      ProcessHistoryTable processHistoryTable = ProcessHistoryTable.getCurrent();
      processHistoryTable.setCurrentProcessInstance(processInstance);
      processHistoryTable.setEmbedded(true);
      processHistoryTable.initialize();
      processHistoryTreeInitialized = true;

      trace.debug("<----------- ProcessHistoryTree Initialize");
   }

   /**
    * load participant panel
    */
   private void initializeParticipantsPanel()
   {
      trace.debug("-----------> Participant Panel Initialize");
      if (!processHistoryTreeInitialized)
      {
         initializeProcessHistoryTree();
      }
      ParticipantsPanelBean participantsList = ParticipantsPanelBean.getCurrent();
      participantsList.setCurrentProcessInstance(processInstance);
      participantsList.setEmbedded(true);
      participantsList.setShowTitle(false);

      participantsList.setActivityTreeRoot(ProcessHistoryTable.getCurrent().getActivityTreeTable().getActivityTableRoot());

      participantsList.initialize();
      participantsPanelInitialized = true;
      trace.debug("-----------> Participant Panel Initialize");
   }

   /**
    * 
    */
   private void initializeGenericRepositoryTree()
   {
      trace.debug("-----------> GenericRepositoryTree Initialize");
      GenericRepositoryTreeViewBean genericRepositoryTree = GenericRepositoryTreeViewBean.getInstance();
      if (genericRepositoryTree.isEditingModeOff())
      {
         genericRepositoryTree.setRepositoryMode(RepositoryMode.PROCESS_DOCUMENTS);
         genericRepositoryTree.setProcessInstance(processInstance);
         genericRepositoryTree.initialize();
         genericRepositoryTreeInitialized = true;
      }
      trace.debug("-----------> GenericRepositoryTree Initialize");
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

         List<DescriptorItemTableEntry> descriptorList = convertToTableEntries(CommonDescriptorUtils.createProcessDescriptors(processInstance));
         
         descriptorTable.setList(descriptorList);
         descriptorTable.initialize();
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
            ColumnDataType.STRING, this.getMessages().getString("column.name"), true,
            true);
      ColumnPreference valueCol = new ColumnPreference("Value", "value",
            ColumnDataType.STRING, this.getMessages().getString("column.value"), true,
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

   }

   /**
    * 
    */
   private void initializeProcessTable()
   {
      trace.debug("-----------> Linked Process Table Initialize");

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

      trace.debug("<----------- Linked Process Table Initialized");
   }

   /**
    * 
    */
   private void initializeLinkedProcessPanel()
   {
      trace.debug("-----------> Linked Process Panel Initialize");
      if (!linkedProcessPanelInitialized)
      {
         initializeProcessTable();
      }
      processHelper.getProcessTable().refresh(true);
      trace.debug("-----------> Linked Process Panel Initialized");
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
         PortalApplication.getInstance().openViewById("correspondenceView",
               "DocumentID=" + processInstance.getOID(), params, null, true);
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
         PortalApplication.getInstance().openViewById("chatView",
               "processInstanceOID=" + processInstance.getOID(), params, null, true);
      }
   }
   
   
   
   // **************Modified Setter method*****************
   public void setHideCompletedActivities(boolean hideCompletedActivities)
   {
      if (hideCompletedActivities != this.hideCompletedActivities)
      {
         this.hideCompletedActivities = hideCompletedActivities;
         initialize();
      }
   }

   /**
    * @param descriptorPanelExpanded
    */
   public void setDescriptorPanelExpanded(boolean descriptorPanelExpanded)
   {
      this.descriptorPanelExpanded = descriptorPanelExpanded;
      if (!descriptorsPanelInitialized)
      {
         initializeDescriptorsPanel();
      }
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

   /**
    * @param processHistoryPanelExpanded
    */
   public void setProcessHistoryPanelExpanded(boolean processHistoryPanelExpanded)
   {
      this.processHistoryPanelExpanded = processHistoryPanelExpanded;
      if (!processHistoryTreeInitialized)
      {
         initializeProcessHistoryTree();
      }
   }

   /**
    * @param genericRepositoryTreeExpanded
    */
   public void setGenericRepositoryTreeExpanded(boolean genericRepositoryTreeExpanded)
   {
      this.genericRepositoryTreeExpanded = genericRepositoryTreeExpanded;
      if (!genericRepositoryTreeInitialized)
      {
         initializeGenericRepositoryTree();
      }
   }

   /**
    * @param participantsPanelExpanded
    */
   public void setParticipantsPanelExpanded(boolean participantsPanelExpanded)
   {
      this.participantsPanelExpanded = participantsPanelExpanded;
      if (!participantsPanelInitialized)
      {
         initializeParticipantsPanel();
      }
   }

   public void handleEvent(EventType eventType)
   {}   
   
   /**
    * 
    * @return
    */
   public boolean isCanCreateCase()
   {
      return AuthorizationUtils.canCreateCase();
   }

   /**
    * 
    */
   public void openCreateCase(ActionEvent event)
   {
      List<ProcessInstance> selectedProcesses = CollectionUtils.newArrayList();
      selectedProcesses.add(processInstance);
      ProcessInstanceUtils.openCreateCase(selectedProcesses);
   }
   
   /**
    * 
    */
   public void attachToCase(ActionEvent event)
   {
      List<ProcessInstance> selectedProcesses = CollectionUtils.newArrayList();
      selectedProcesses.add(this.processInstance);
      ProcessInstanceUtils.openAttachToCase(selectedProcesses);
   }
   
   // ***************Default Getter & Setter Methods************
   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   public boolean isHideCompletedActivities()
   {
      return hideCompletedActivities;
   }

   public SortableTable<DescriptorItemTableEntry> getDescriptorTable()
   {
      return descriptorTable;
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

   public TimeZone getTimeZone()
   {
      return java.util.TimeZone.getDefault();
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

   
   public boolean isLinkedProcessPanelExpanded()
   {
      return linkedProcessPanelExpanded;
   }

   public boolean isProcessHistoryPanelExpanded()
   {
      return processHistoryPanelExpanded;
   }
   
   public boolean isGenericRepositoryTreeExpanded()
   {
      return genericRepositoryTreeExpanded;
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


   public boolean isAbortProcess()
   {
      return abortProcess;
   }

   public void setAbortProcess(boolean abortProcess)
   {
      this.abortProcess = abortProcess;
   }

   public boolean isSupportsProcessAttachments()
   {
      return supportsProcessAttachments;
   }

   public void setSupportsProcessAttachments(boolean supportsPA)
   {
      this.supportsProcessAttachments = supportsPA;
   }

   public String getProcessName()
   {
      if (null == processInstance) 
      {
         return "";
      }
      ProcessDefinition pd = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      return I18nUtils.getProcessName(pd);
   }
   
   public String getState()
   {
      return state; 
   }

   public ProcessTableHelper getProcessHelper()
   {
      return processHelper;
   }

   public void setProcessHelper(ProcessTableHelper processHelper)
   {
      this.processHelper = processHelper;
   }

   public boolean isLinkedProcessPanelInitialized()
   {
      return linkedProcessPanelInitialized;
   }

   public void setLinkedProcessPanelInitialized(boolean linkedProcessPanelInitialized)
   {
      this.linkedProcessPanelInitialized = linkedProcessPanelInitialized;
   }

   
   public Long getProcessInstanceOID()
   {
      return processInstanceOID;
   }

   public String getStartingUser()
   {
      return UserUtils.getUserDisplayLabel(getProcessInstance().getStartingUser());
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
         ProcessInstanceQuery query = ProcessInstanceQuery.findLinked(processInstanceOID, LinkDirection.TO_FROM,
               PredefinedProcessInstanceLinkTypes.JOIN, PredefinedProcessInstanceLinkTypes.SWITCH,
               PredefinedProcessInstanceLinkTypes.UPGRADE);
         ProcessInstanceDetailsPolicy processInstanceDetailsPolicy = new ProcessInstanceDetailsPolicy(
               ProcessInstanceDetailsLevel.Default);
         processInstanceDetailsPolicy.getOptions().add(ProcessInstanceDetailsOptions.WITH_LINK_INFO);
         query.setPolicy(processInstanceDetailsPolicy);
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

   /**
    * check authorization for SpawnProcess
    * 
    * @return
    */
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

   public boolean isDisableSpawnProcess()
   {
      return disableSpawnProcess;
   }

   public void setDisableSpawnProcess(boolean disableSpawnProcess)
   {
      this.disableSpawnProcess = disableSpawnProcess;
   }  
   
   
}