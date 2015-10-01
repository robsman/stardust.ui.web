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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.DataPathDetails;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceAttributes;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsLevel;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetailsOptions;
import org.eclipse.stardust.engine.api.model.Data;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.LinkDirection;
import org.eclipse.stardust.engine.api.query.ProcessInstanceDetailsPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.HistoricalEvent;
import org.eclipse.stardust.engine.api.runtime.PredefinedProcessInstanceLinkTypes;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.ISortHandler;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalErrorClass;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.ValidationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQueryResult;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
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
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.GenericRepositoryTreeViewBean.RepositoryMode;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;

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
   private final static String COL_DESC_DETAILS = "DescDetails";   

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
   private String abortedUser;

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
   private boolean descriptorsHistoryPanelInitialized = false;
   private boolean linkedProcessPanelInitialized = false;
   private boolean hasSpawnProcessPermission;
   private boolean hasSwitchProcessPermission;
   private boolean hasJoinProcessPermission;
   private boolean disableSpawnProcess = false;
   private View thisView;
   
   private ValidationMessageBean validationMessageBean = null;
   // Store all OUT DataPaths keyed by DATAID
   Map<String, DataPathDetails> outDataPathsMap = null;
   Map<String, DataPathDetails> inDataPathsMap = null; 
   
   private String startingUserLabel = null;
   
   private int selectedTabIndex = 0;
   private List<DescriptorHistoryTableEntry> descriptorsHistoryList;
   private PaginatorDataTable<DescriptorHistoryTableEntry, DescriptorHistoryTableEntry> descriptorHistoryTable;
   private String descriptorSearchTxt;

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

      initializeParticipantsPanel(true);
      
      setSupportsProcessAttachments(DMSHelper.existsProcessAttachmentsDataPath(processInstance));
      setAbortProcess(ProcessInstanceUtils.isAbortable(processInstance));
      List<ProcessInstance> sourceList = CollectionUtils.newArrayList();
      sourceList.add(processInstance);
      setDisableSpawnProcess(ProcessInstanceUtils.isTerminatedProcessInstances(sourceList));
      state = MessagesViewsCommonBean.getInstance().getString(
            STATE_PREFIX + processInstance.getState().getName().toLowerCase());

      if(processInstance.getState().equals(ProcessInstanceState.Aborted))
      {
         setAbortedUser(processInstance);
      }

   
      trace.debug("<----------- ProcessInstanceDetailsBean Initialize");
   }
   
   /**
    *  TODO - Identify a better way to getAborting User for current PI
    * @param processInstance
    */
   private void setAbortedUser(ProcessInstance processInstance)
   {
      ProcessInstanceQuery query = new ProcessInstanceQuery();
      query.getFilter()
            .and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.isEqual(processInstance.getOID()));
      query.setPolicy(HistoricalEventPolicy.ALL_EVENTS);

      List<ProcessInstance> pis = ServiceFactoryUtils.getQueryService()
            .getAllProcessInstances(query);

      Iterator<ProcessInstance> iter = pis.iterator();

      while (iter.hasNext())
      {
         ProcessInstance pi = iter.next();
         abortedUser = ProcessInstanceUtils.getAbortedUser(pi);
      }

   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         thisView = event.getView();
         if (thisView != null && processInstance == null) // Only when first time created
         {
            String pOID = thisView.getParamValue("processInstanceOID");
            if (!StringUtils.isEmpty(pOID))
            {
               try
               {
                  processInstanceOID = Long.parseLong(pOID);
                  initialize();
                  // set process Name
                  thisView.getViewParams().put("processInstanceName", getProcessName());
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
   
   public void descriptorTabChange(TabChangeEvent tabChangeEvent) 
   {
      selectedTabIndex = tabChangeEvent.getNewTabIndex();

      if (selectedTabIndex == 1)
      { 
         if(!descriptorsHistoryPanelInitialized)
         {
            descriptorsHistoryList = CollectionUtils.newArrayList();
            initializeDescriptorHistoryTable();
         }
         descriptorsHistoryPanelInitialized = true;
         updateDescriptorHistory();
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
   private void initializeParticipantsPanel(boolean forceRefresh)
   {
      if (isParticipantsPanelExpanded())
      {
         if (!processHistoryTreeInitialized)
         {
            initializeProcessHistoryTree();
         }
      }

      trace.debug("<----------- Participant Panel Initialize");
      ParticipantsPanelBean participantPanel = ParticipantsPanelBean.getCurrent();
      
      //If it is force refresh then don't check current process
      if (!forceRefresh && (participantPanel.getCurrentProcessInstance() != null && participantPanel.getCurrentProcessInstance().getOID() == ProcessHistoryTable.getCurrent().getSelectedRow().getProcessInstance().getOID()))
      {
         return;
      }
      
      ProcessInstance pi = processInstance;
      
      if (ProcessHistoryTable.getCurrent().getSelectedRow() != null)
      {
         pi = ProcessHistoryTable.getCurrent().getSelectedRow().getProcessInstance();
      }
      
      participantPanel.initializePanel(pi, ProcessHistoryTable.getCurrent().getActivityTreeTable().getActivityTableRoot());

      trace.debug("Participant Panel Initialize ----------->");
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
            validationMessageBean = new ValidationMessageBean();
            inDataPathsMap = CollectionUtils.newHashMap();
            outDataPathsMap = CollectionUtils.newHashMap();
            validationMessageBean.setStyleClass("messagePanel");
            initializeDescriptorColumns();
         }

         validationMessageBean.reset();
         List<DescriptorItemTableEntry> descriptorList = convertToTableEntries(CommonDescriptorUtils.createProcessDescriptors(processInstance, false));
         
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
      try
      {
         GenericDataMapping mapping;
         DataMappingWrapper dmWrapper;
         // Store DataPath Map with all IN and OUT mappings
         updateDataPathMap();
         boolean suppressBlankDescriptors = CommonDescriptorUtils.isSuppressBlankDescriptorsEnabled();
         DeployedModel model = ModelCache.findModelCache().getModel(processInstance.getModelOID());
         for (ProcessDescriptor processDescriptor : processDescriptors)
         {
            DataPathDetails inDataPath = inDataPathsMap.get(processDescriptor.getId());
            // For non-filterable OUT DataPath(complex), prevent edit
            if (CollectionUtils.isNotEmpty(outDataPathsMap) && DescriptorFilterUtils.isDataFilterable(inDataPath))
            {
               // Get OUT dataPath for IN DataPath
               DataPathDetails outDataPath = fetchOutDataPath(inDataPath);
               if (null != outDataPath)
               {
                  Class dataClass = outDataPath.getMappedType();
                  mapping = new GenericDataMapping(outDataPath);
                  dmWrapper = new DataMappingWrapper(mapping, null, false);
                  String type = dmWrapper.getType();
                  Object value = DescriptorFilterUtils.convertDataPathValue(dataClass, processDescriptor.getValue());
                  if(null != value)
                  {
                     descriptorsEntries.add(new DescriptorItemTableEntry(processDescriptor.getKey(), value,  processDescriptor.getId(), type, dataClass, true));
                  }
                  else
                  {
                     descriptorsEntries.add(new DescriptorItemTableEntry(processDescriptor.getKey(), processDescriptor
                           .getValue()));   
                  }
               }
               else
               {
                  if (!suppressBlankDescriptors || (suppressBlankDescriptors
                        && (null != processDescriptor.getValue() && StringUtils.isNotEmpty(processDescriptor.getValue()))))
                  {
                     descriptorsEntries.add(new DescriptorItemTableEntry(processDescriptor.getKey(), processDescriptor
                           .getValue()));   
                  }
               }
            }
            else
            {
               if (!suppressBlankDescriptors
                     || (suppressBlankDescriptors && (null != processDescriptor.getValue() && StringUtils
                           .isNotEmpty(processDescriptor.getValue()))))
               {
                  descriptorsEntries.add(new DescriptorItemTableEntry(processDescriptor.getKey(), processDescriptor
                        .getValue()));
               }
            }
            
         }
      }
      catch(Exception e)
      {
         FacesMessage facesMsg = ExceptionHandler.getFacesMessage(
               new PortalException(PortalErrorClass.UNABLE_TO_CONVERT_DATAMAPPING_VALUE, e));

         throw new ValidatorException(facesMsg);
      }
      
      return descriptorsEntries;
   }
   
   /**
    * 
    * @param event
    */
   public void valueChange(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      else
      {
         validationMessageBean.reset();
         DescriptorItemTableEntry userObject = null;
         DataPathDetails inDataPath = null, outDataPath = null;
         try
         {
            Object newDescriptorValue = event.getNewValue();
            Object oldDescriptorValue = event.getOldValue();
            Object newValue = null;
            if (null != newDescriptorValue
                  && !newDescriptorValue.toString().equals(oldDescriptorValue))
            {
               userObject = (DescriptorItemTableEntry) event.getComponent().getAttributes().get("row");
               String type = userObject.getType();
               Class dataClass = userObject.getMappedType();
               inDataPath = inDataPathsMap.get(userObject.getId());
               outDataPath = fetchOutDataPath(inDataPath);
               if (null != outDataPath)
               {
                  newValue = DescriptorFilterUtils.convertDataPathValue(dataClass, newDescriptorValue);
                  ServiceFactoryUtils.getWorkflowService().setOutDataPath(processInstance.getOID(),
                        outDataPath.getId(), newValue);

                  userObject.setHasError(false);
                  validationMessageBean.addInfoMessage(
                        this.getMessages().getString("descriptor.save", inDataPath.getName()), "descriptorViewMsg");
               }
            }
         }
         catch (Exception e)
         {
            userObject.setHasError(true);
            FacesMessage facesMsg = ExceptionHandler.getFacesMessage(new PortalException(
                  PortalErrorClass.UNABLE_TO_CONVERT_DATAMAPPING_VALUE, e));
            validationMessageBean.addError(facesMsg.getSummary(), "descriptorViewMsg");
         }
      }
   }
   
   /**
    * 
    * @param inDataPath
    * @return
    */
   private DataPathDetails fetchOutDataPath(DataPathDetails inDataPath)
   {
      if (CollectionUtils.isNotEmpty(outDataPathsMap))
      {
         // read all OUT dataPath for given Data
         DataPathDetails outDataPath = outDataPathsMap.get(inDataPath.getId());
         if (null != outDataPath)
         {
            // Filter dataPath with same AccessPoint and on same Qualified Model
            if (outDataPath.getAccessPath().equals(inDataPath.getAccessPath()))
            {
               String data = inDataPath.getData();
               Data data1 = DescriptorFilterUtils.getData(inDataPath);
               Data data2 = DescriptorFilterUtils.getData(outDataPath);
               if (data1.equals(data2))
               {
                  return outDataPath;
               }
            }
         }
      }

      return null;
   }
   
   /**
    * 
    */
   private void updateDataPathMap()
   {
      ProcessDefinition processDef = ProcessDefinitionUtils.getProcessDefinition(processInstance.getModelOID(),
            processInstance.getProcessID());
      List<DataPathDetails> dataPaths = processDef.getAllDataPaths();
      DataPathDetails dataPathDetails;
      int size = dataPaths.size();
      for (int i = 0; i < size; i++)
      {
         dataPathDetails = (DataPathDetails) dataPaths.get(i);
         if(null != dataPathDetails.getDirection())
         {
            if(dataPathDetails.getDirection().equals(Direction.OUT))
            {
               outDataPathsMap.put(dataPathDetails.getId(), dataPathDetails);  
            }
            else
            {
               inDataPathsMap.put(dataPathDetails.getId(), dataPathDetails);
            }
         }
      }
   }
   
   /**
    * Initializes Descriptor columns
    */
   private void initializeDescriptorColumns()
   {
      ColumnPreference nameCol = new ColumnPreference("Name", "name",
            ColumnDataType.STRING, this.getMessages().getString("column.name"), true,
            true);
      ColumnPreference valueCol = new ColumnPreference("Value", "value", this.getMessages().getString("column.value"),
            ResourcePaths.V_DESC_TABLE_COLUMNS, true, true);
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
   
   private void initializeDescriptorHistoryTable()
   {

      ColumnPreference timeStampCol = new ColumnPreference("TimeStamp", "timestamp", ColumnDataType.DATE, this.getMessages().getString("descriptors.history.timeStamp"),
            true, true);
      timeStampCol.setNoWrap(true);

      ColumnPreference eventCol = new ColumnPreference("EventType", "eventType", ColumnDataType.STRING, this.getMessages().getString("descriptors.history.event"), true, false);
      eventCol.setNoWrap(true);

      ColumnPreference userCol = new ColumnPreference("User", "user", ColumnDataType.STRING, this.getMessages().getString("descriptors.history.user"), true, false);
      userCol.setNoWrap(true);

      ColumnPreference detailsCol = new ColumnPreference("DescDetails", "descDetails", ColumnDataType.STRING,
            this.getMessages().getString("descriptors.history.details"), new TableDataFilterPopup(new TableDataFilterSearch()), true, false);
      detailsCol.setNoWrap(true);

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      cols.add(timeStampCol);
      cols.add(eventCol);
      cols.add(userCol);
      cols.add(detailsCol);

      IColumnModel daemonsColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_VIEWS_COMMON,
            UserPreferencesEntries.V_PROCESS_INSTANCE_DETAILS);
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(daemonsColumnModel);
      
      IppSearchHandler<DescriptorHistoryTableEntry> searchHandler = new DescriptorHistorySearchHandler();
      IppFilterHandler filterHandler = new DescriptorHistoryFilterHandler();
      ISortHandler sortHandler = new DescriptorHistorySortHandler();
      descriptorHistoryTable = new PaginatorDataTable<DescriptorHistoryTableEntry, DescriptorHistoryTableEntry>(colSelecPopup, searchHandler, filterHandler, sortHandler, new IUserObjectBuilder<DescriptorHistoryTableEntry>()
      {

         public DescriptorHistoryTableEntry createUserObject(Object resultRow)
         {
            return (DescriptorHistoryTableEntry) resultRow;
         }
      }, new DataTableSortModel<DescriptorHistoryTableEntry>("timestamp", false));
      descriptorHistoryTable.setISortHandler(sortHandler);
      descriptorHistoryTable.initialize();
   }

   public void updateDescriptorHistory()
   {
      descriptorsHistoryList = fetchDescriptorHistory();
      
      descriptorHistoryTable.refresh(true);
   }
   
   private List<DescriptorHistoryTableEntry> fetchDescriptorHistory()
   {
      List<DescriptorHistoryTableEntry> descriptorsHistory = CollectionUtils.newArrayList();
      try
      {
         List<HistoricalEvent> events = DescriptorColumnUtils.getProcessDescriptorsHistory(this.processInstance);
         DeployedModel model = ModelCache.findModelCache().getModel(this.processInstance.getModelOID());
         if (CollectionUtils.isNotEmpty(events))
         {
            for (HistoricalEvent event : events)
            {
               String descriptorDetails = (String) event.getDetails();
               if (StringUtils.isNotEmpty(descriptorDetails))
               {
                  if (descriptorDetails.contains("'"))
                  {
                     // TODO - provide better way to split and read params
                     String[] token = descriptorDetails.split("'");
                     if (null != token && token.length == 6)
                     {
                        String dataId = token[1];
                        Data data = model.getData(dataId);
                        dataId = I18nUtils.getDataName(data);
                        String processInstanceOID = token[3];
                        String dataPathId = token[5];
                        DataPath dataPath = outDataPathsMap.get(dataPathId);
                        if (null != dataPath)
                        {
                           dataPathId = I18nUtils.getDataPathName(dataPath);
                        }
                        descriptorDetails = MessagesViewsCommonBean.getInstance().getParamString(
                              "views.processInstanceDetailsView.descriptors.history.descriptorDetails", dataId,
                              processInstanceOID, dataPathId);
                     }
                  }
                  descriptorsHistory.add(new DescriptorHistoryTableEntry(event.getEventTime(), null, event
                        .getEventType().getName(), I18nUtils.getUserLabel(event.getUser()), descriptorDetails));
               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
      }

      return descriptorsHistory;
   }
   
   /**
    * 
    */
   private void initializeProcessTable()
   {
      trace.debug("-----------> Linked Process Table Initialize");

      processHelper = new ProcessTableHelper();
      processHelper.setCallbackHandler(this);
      processHelper.setDisplayLinkInfo(true);
      processHelper.setProcessInstance(processInstance);
      processHelper.initializeProcessTable(UserPreferencesEntries.M_VIEWS_COMMON, VIEW_ID);
      processHelper.getProcessTable().initialize();
      processHelper.getProcessTable().setISearchHandler(new ProcessTableSearchHandler());
      linkedProcessPanelInitialized = true;

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
      ParticipantsPanelBean participantPanel = ParticipantsPanelBean.getCurrent();
      participantPanel.setExpanded(this.participantsPanelExpanded);
      initializeParticipantsPanel(false);
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
      if (startingUserLabel == null)
      {
         User startingUser = getProcessInstance().getStartingUser();
         UserUtils.loadDisplayPreferenceForUser(startingUser);
         startingUserLabel = UserUtils.getUserDisplayLabel(startingUser);
      }
      return startingUserLabel;
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
               PredefinedProcessInstanceLinkTypes.UPGRADE, PredefinedProcessInstanceLinkTypes.SPAWN,
               PredefinedProcessInstanceLinkTypes.RELATED);
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
    * 
    * @param list
    * @param filterNamePattern
    * @return
    */
   private List<DescriptorHistoryTableEntry> filterResult(List<DescriptorHistoryTableEntry> list, String filterNamePattern)
   {
      List<DescriptorHistoryTableEntry> filteredList = CollectionUtils.newArrayList();

      for (DescriptorHistoryTableEntry row : descriptorsHistoryList)
      {
         String descriptorDetails = row.getDescDetails();
         if (StringUtils.isNotEmpty(descriptorDetails) && descriptorDetails.contains(filterNamePattern))
         {
            filteredList.add(row);
         }
      }
      
      return filteredList;
   }
   
   private List<DescriptorHistoryTableEntry> getPaginatedSubList(List<DescriptorHistoryTableEntry> list, int startIndex,
         int pageSize)
   {
      int listSize = list.size();
      int toIndex = listSize > (startIndex + pageSize) ? startIndex + pageSize : listSize;
      List<DescriptorHistoryTableEntry> result = list.subList(startIndex, toIndex);
      return result;
   }
   
   /**
    * 
    * @author Sidharth.Singh
    * @version $Revision: $
    */
   public class DescriptorHistoryFilterHandler extends IppFilterHandler
   {
      private static final long serialVersionUID = -2173022668039757090L;

      @Override
      public void applyFiltering(Query query, List<ITableDataFilter> filters)
      {
         descriptorSearchTxt = null;
         for (ITableDataFilter tableDataFilter : filters)
         {
            String filterName = tableDataFilter.getName();
            if (COL_DESC_DETAILS.equals(filterName))
            {
               descriptorSearchTxt = ((TableDataFilterSearch) tableDataFilter).getValue();
            }
         }
      }

   }
   
   /**
    * 
    * @author Sidharth.Singh
    * @version $Revision: $
    */
   public class DescriptorHistorySearchHandler extends IppSearchHandler<DescriptorHistoryTableEntry>
   {
      private static final long serialVersionUID = -3543070769771871255L;

      @Override
      public Query createQuery()
      {
         return null;// No query for engine call
      }

      @Override
      public QueryResult<DescriptorHistoryTableEntry> performSearch(Query query)
      {
         return new RawQueryResult<DescriptorHistoryTableEntry>(descriptorsHistoryList, null, false, Long.valueOf(descriptorsHistoryList.size()));
      }

      @Override
      public IQueryResult<DescriptorHistoryTableEntry> performSearch(IQuery iQuery, int startRow, int pageSize)
      {
         List<DescriptorHistoryTableEntry> resultList = StringUtils.isEmpty(descriptorSearchTxt)
               ? descriptorsHistoryList
               : filterResult(descriptorsHistoryList, descriptorSearchTxt);
         
         
         List<DescriptorHistoryTableEntry> result = getPaginatedSubList(resultList, startRow, pageSize);
         
         RawQueryResult<DescriptorHistoryTableEntry> queryResult = new RawQueryResult<DescriptorHistoryTableEntry>(result,
               null, false, Long.valueOf(resultList.size()));

         return new IppQueryResult<DescriptorHistoryTableEntry>(queryResult);
      }
      
   }
   
   /**
    * 
    * @author Sidharth.Singh
    * @version $Revision: $
    */
   private class DescriptorHistorySortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = -2562964400250132610L;

      private SortCriterion sortCriterion;

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriterias)
      {}

      public void applySorting(IQuery iQuery, List<SortCriterion> sortCriterias)
      {
         if (CollectionUtils.isNotEmpty(sortCriterias))
         {
            sortCriterion = sortCriterias.get(0);
            Comparator<DescriptorHistoryTableEntry> comparator = new SortableTableComparator<DescriptorHistoryTableEntry>(
                  sortCriterion.getProperty(), sortCriterion.isAscending());
            Collections.sort(descriptorsHistoryList, comparator);
         }
         else
         {
            sortCriterion = null;
         }

      }

      public SortCriterion getSortCriterion()
      {
         return sortCriterion;
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

   public String getAbortedUser()
   {
      return abortedUser;
   }

   public ValidationMessageBean getValidationMessageBean()
   {
      return validationMessageBean;
   }

   public void setValidationMessageBean(ValidationMessageBean validationMessageBean)
   {
      this.validationMessageBean = validationMessageBean;
   }

   public boolean isDescriptorsHistoryPanelInitialized()
   {
      return descriptorsHistoryPanelInitialized;
   }

   public int getSelectedTabIndex()
   {
      return selectedTabIndex;
   }

   public void setSelectedTabIndex(int selectedTabIndex)
   {
      this.selectedTabIndex = selectedTabIndex;
   }  
   
   public List<DescriptorHistoryTableEntry> getDescriptorsHistoryList()
   {
      return descriptorsHistoryList;
   }

   public PaginatorDataTable<DescriptorHistoryTableEntry, DescriptorHistoryTableEntry> getDescriptorHistoryTable()
   {
      return descriptorHistoryTable;
   }

}