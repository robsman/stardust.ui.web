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
package org.eclipse.stardust.ui.web.viewscommon.helper.processTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceLink;
import org.eclipse.stardust.engine.api.runtime.ProcessInstanceState;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModelListener;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterPickList;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterDate;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterNumber;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPickList;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.DataType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.FilterCriteria;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterPickList.RenderType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.PriorityAutoCompleteItem;
import org.eclipse.stardust.ui.web.viewscommon.common.PriorityAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.JoinProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SpawnProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SwitchProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.AbortProcessBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;




/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class ProcessTableHelper implements IUserObjectBuilder<ProcessInstanceTableEntry>
{
   protected final static String PROCESS_DEFINITION_MODEL = "carnotBcProcessInstanceFilter/processDefinitionModel";


   private static final Logger trace = LogManager.getLogger(ProcessTableHelper.class);
   private static final String DESCRIPTOR_COL_NAME = "Descriptors";

   private List<SelectItem> allStatusList;

   private PaginatorDataTable<ProcessInstanceTableEntry, ProcessInstance> processTable;

   private ArrayList<SelectItem> allPriorities;

   private ICallbackHandler callbackHandler;

   private MessagesViewsCommonBean propsBean;

   private ProcessTableFilterHandler filterHandler;
   
   private ProcessTableSortHandler sortHandler;

   //holds <DataId, DataPath>
   private Map<String, DataPath> allDescriptors = CollectionUtils.newMap();
   
   private boolean displayLinkInfo=false;
   
   private boolean canCreateCase;
   
   private ProcessInstance processInstance;
   
   private ColumnModelListener columnModelListener;
   
   private Set<String> visibleDescriptorsIds;
   
   private boolean fetchAllDescriptors;

   public ProcessTableHelper()
   {
      super();
      propsBean = MessagesViewsCommonBean.getInstance();
      allPriorities = new ArrayList<SelectItem>();
      allPriorities.add(new SelectItem(new Integer(1), propsBean.getString("views.processTable.priorities.high")));
      allPriorities.add(new SelectItem(new Integer(0), propsBean.getString("views.processTable.priorities.normal")));
      allPriorities.add(new SelectItem(new Integer(-1), propsBean.getString("views.processTable.priorities.low")));

      allStatusList = new ArrayList<SelectItem>();
      allStatusList.add(new SelectItem(Integer.toString(ProcessInstanceState.CREATED), propsBean
            .getString("views.processTable.statusFilter.created")));
      allStatusList.add(new SelectItem(Integer.toString(ProcessInstanceState.ACTIVE), propsBean
            .getString("views.processTable.statusFilter.active")));
      allStatusList.add(new SelectItem(Integer.toString(ProcessInstanceState.INTERRUPTED), propsBean
            .getString("views.processTable.statusFilter.interrupted")));
      allStatusList.add(new SelectItem(Integer.toString(ProcessInstanceState.ABORTED), propsBean
            .getString("views.processTable.statusFilter.aborted")));
      allStatusList.add(new SelectItem(Integer.toString(ProcessInstanceState.COMPLETED), propsBean
            .getString("views.processTable.statusFilter.completed")));
      allStatusList.add(new SelectItem(Integer.toString(ProcessInstanceState.ABORTING), propsBean
            .getString("views.processTable.statusFilter.aborting")));     
      
      canCreateCase = AuthorizationUtils.canCreateCase();
      
      columnModelListener = new ColumnModelListener();
   }   
   
   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getSelectedItemCount()
   {
      int count = 0;
      if (null != processTable)
      {
         List<ProcessInstanceTableEntry> tempProcessList = processTable.getCurrentList();
         for (ProcessInstanceTableEntry pwt : tempProcessList)
         {
            if (pwt.isCheckSelection())
            {
               count++;
            }
         }
      }
      return count;

   }
   
   /**
    * Terminates Process
    * 
    * @param ae
    */
   public void terminateProcess(ActionEvent ae)
   {
      ProcessInstance pi = (ProcessInstance) ae.getComponent().getAttributes().get("processInstance");
      List<ProcessInstance> selectedProcesses = null;
      if (pi != null)
      {
         selectedProcesses = CollectionUtils.newArrayList();
         selectedProcesses.add(pi);
      }
      else
      {
         selectedProcesses = getSelectedProcessList();
      }
      if (CollectionUtils.isNotEmpty(selectedProcesses))
      {
         AbortProcessBean abortProcessHelper = AbortProcessBean.getInstance();
         abortProcessHelper.setCallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               processTable.refresh(true);
            }
         });
         abortProcessHelper.abortProcesses(selectedProcesses);
      }
   }
      
   
   /**
    * <p>
    * Jsf ActionLisnener method to open Spawn Process Dialog
    * only one process selection is allowed to open Spawn Process Dialog. 
    * <p>
    * @param ae
    */
   public void openSpawnProcess(ActionEvent ae)
   {
      try
      {
         List<Long> processOids = getSelectedProcesses();
         if(CollectionUtils.isNotEmpty(processOids) && processOids.size()>1 )//more then one selected
         {
            MessageDialog.addWarningMessage(MessagesViewsCommonBean.getInstance().getString("common.multipleProcessSelection.warning"));
         }
         else if(CollectionUtils.isNotEmpty(processOids) )//single selection
         {
            SpawnProcessDialogBean.getInstance().openPopup(processOids);
         }
        
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   /**
    * action listener to open Switch process
    */
   public void openSwitchProcess(ActionEvent event)
   {
      try
      {
         // For mandatory single row select i.e ProcessTable Actions Column
         ProcessInstance selProcessInstance = (ProcessInstance) event.getComponent().getAttributes()
               .get("processInstance");
         SwitchProcessDialogBean dialog = SwitchProcessDialogBean.getInstance();
         if (null != selProcessInstance)
         {
            List<ProcessInstance> sourceList = CollectionUtils.newArrayList();
            sourceList.add(selProcessInstance);
            dialog.setSourceProcessInstances(sourceList);
         }
         else
         {
            // For multiple row select i.e Process table toolbar
            List<ProcessInstance> processList = getSelectedProcessList();
            dialog.setSourceProcessInstances(processList);
         }
         dialog.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   /**
    * action listener to open Join process
    */
   public void openJoinProcess(ActionEvent event)
   {
      try
      {
         ProcessInstance selProcessInstance = (ProcessInstance) event.getComponent().getAttributes()
               .get("processInstance");
         JoinProcessDialogBean dialog = JoinProcessDialogBean.getInstance();
         if (null == selProcessInstance)
         {
            selProcessInstance = getSelectedProcessList().get(0);
         }
         if (null != selProcessInstance)
         {
            dialog.setSourceProcessInstance(selProcessInstance);
            dialog.openPopup();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
/**
 * method return selected process ids
 * @return
 */
   private List<Long> getSelectedProcesses()
   {

      List<Long> processOids = new ArrayList<Long>();

      List<ProcessInstanceTableEntry> tempProcessList = processTable.getCurrentList();
      for (ProcessInstanceTableEntry pwt : tempProcessList)
      {
         if (pwt.isCheckSelection())
         {
            ProcessInstance processInstance = pwt.getProcessInstance();
            if (processInstance != null)
            {
               processOids.add(processInstance.getOID());
            }
         }
      }
      return processOids;
   }
   
   /**
    * method return selected process ids
    * 
    * @return
    */
   private List<ProcessInstance> getSelectedProcessList()
   {
      List<ProcessInstance> processList = CollectionUtils.newArrayList();

      List<ProcessInstanceTableEntry> tempProcessList = processTable.getCurrentList();
      for (ProcessInstanceTableEntry pwt : tempProcessList)
      {
         if (pwt.isCheckSelection())
         {
            ProcessInstance processInstance = pwt.getProcessInstance();
            processList.add(processInstance);
         }
      }
      return processList;
   }

   /**
    * Recovers Process
    * 
    * @param ae
    */
   public void recoverProcess(ActionEvent ae)
   {
      ProcessInstance pi = (ProcessInstance) ae.getComponent().getAttributes().get(
            "processInstance");
      List<Long> processOids = null;
      try
      {
         if (pi != null)
         {
            processOids = new ArrayList<Long>();
            processOids.add(pi.getOID());
         }
         else
         {
            processOids = getSelectedProcesses();
         }
         
         if(CollectionUtils.isNotEmpty(processOids))
         {
            ProcessInstanceUtils.recoverProcessInstance(processOids);
            callbackHandler.handleEvent(ICallbackHandler.EventType.APPLY);
         }
      }
      catch (AccessForbiddenException e)
      {
         MessageDialog.addErrorMessage(propsBean.getString("common.authorization.msg"), e);

      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * Opens notes dialog
    * 
    * @param ae
    */
   public void openNotes(ActionEvent ae)
   {
      ProcessInstance pi = (ProcessInstance) ae.getComponent().getAttributes().get("processInstance");
      ProcessInstanceUtils.openNotes(pi);
      getCallbackHandler().handleEvent(ICallbackHandler.EventType.APPLY);
   }

   /**
    * Saves process priorities
    * 
    * @param event
    */
   public void applyChanges(ActionEvent event)
   {
      try
      {
         ProcessInstanceUtils.updatePriorities(getChangedProcesses());
         // initialize process table
         processTable.initialize();
         callbackHandler.handleEvent(EventType.APPLY);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * Initializes Process Table
    */
   public void initializeProcessTable()
   {    
      initializeProcessTable(UserPreferencesEntries.M_VIEWS_COMMON, UserPreferencesEntries.V_PROCESS_TABLE);
   }

   /**
    * Initializes Process Table columns, explicit ModuleId, View Id passed from
    * CaseDetailsBean, ProcessInstanceDetailsBean for Linked Process Table
    */
   public void initializeProcessTable(
         String moduleId, String viewId)
   {      
      ColumnPreference processNameCol = new ColumnPreference(
            "ProcessName",
            "processInstanceName",
            propsBean
                  .getString("views.processTable.column.processName"),
            ResourcePaths.V_PROCESS_TABLE_COLUMNS, true, true);

      processNameCol.setColumnDataFilterPopup(new TableDataFilterPopup(
            new TableDataFilterPickList(FilterCriteria.SELECT_MANY,
                  ProcessDefinitionUtils.getAllUniqueProcessDefinitionItems(), RenderType.LIST, 5, null)));

      ColumnPreference prioCol = new ColumnPreference("Priority", "priority", propsBean
            .getString("views.processTable.column.priority"),
            ResourcePaths.V_PROCESS_TABLE_COLUMNS, true, true);
      prioCol.setColumnAlignment(ColumnAlignment.CENTER);
      prioCol.setColumnDataFilterPopup(new TableDataFilterPopup(new PriorityAutocompleteTableDataFilter()));

      ColumnPreference descriptorsCol = new ColumnPreference(DESCRIPTOR_COL_NAME,
            "processDescriptorsList", propsBean
                  .getString("views.processTable.column.descriptors"),
            ResourcePaths.V_PROCESS_TABLE_COLUMNS, true, false);
      descriptorsCol.setNoWrap(true);

      ColumnPreference userCol = new ColumnPreference("StartingUser", "startingUser",
            ColumnDataType.STRING, propsBean
                  .getString("views.processTable.column.startingUser"), null, true,
            true);
      userCol.setColumnDataFilterPopup(new TableDataFilterPopup(new UserAutocompleteTableDataFilter()));
      
      ColumnPreference startTimeCol = new ColumnPreference("StartTime", "startTime",
            ColumnDataType.DATE, propsBean
                  .getString("views.processTable.column.startTime"),
            new TableDataFilterPopup(new TableDataFilterDate(DataType.DATE)), true,
            true);
      startTimeCol.setNoWrap(true);

      ColumnPreference durationCol = new ColumnPreference("Duration", "duration",
            ColumnDataType.STRING, propsBean
                  .getString("views.processTable.column.duration"),
            null, true, false);
      durationCol.setNoWrap(true);
      durationCol.setColumnAlignment(ColumnAlignment.CENTER);

      List<ColumnPreference> processFixedCols2 = new ArrayList<ColumnPreference>();
      ColumnPreference actionsCol = new ColumnPreference("Actions", "", propsBean
            .getString("views.common.column.actions"),
            ResourcePaths.V_PROCESS_TABLE_COLUMNS, true, false);
      actionsCol.setColumnAlignment(ColumnAlignment.RIGHT);
      actionsCol.setExportable(false);
      processFixedCols2.add(actionsCol);

      ColumnPreference statusCol = new ColumnPreference("Status", "status",
            ColumnDataType.STRING, propsBean
                  .getString("views.processTable.column.status"),
            null, false, false);

      statusCol.setColumnDataFilterPopup(new TableDataFilterPopup(
            new TableDataFilterPickList(FilterCriteria.SELECT_MANY, allStatusList,
                  RenderType.LIST, 3, null)));

      ColumnPreference pRootOIDCol = new ColumnPreference("RootPOID", "processInstanceRootOID", ColumnDataType.NUMBER,
            propsBean.getString("views.processTable.column.rootPOID"), new TableDataFilterPopup(
                  new TableDataFilterNumber(DataType.LONG)), false, true);

      ColumnPreference pOIDCol = new ColumnPreference("ProcessOID", "processInstanceOID",
            ColumnDataType.NUMBER, propsBean
                  .getString("views.processTable.column.pOID"),
            new TableDataFilterPopup(new TableDataFilterNumber(DataType.LONG)), true,
            true);

      ColumnPreference endTimeCol = new ColumnPreference("EndTime", "endTime", ColumnDataType.DATE,
            propsBean.getString("views.processTable.column.endTime"), new TableDataFilterPopup(new TableDataFilterDate(DataType.DATE)), false, true);

      ColumnPreference linkTypeCol = new ColumnPreference("LinkType", "linkType", ColumnDataType.STRING,
            propsBean.getString("views.processTable.column.linkType"), null, true, false);

      ColumnPreference linkCreateTimeCol = new ColumnPreference("LinkCreateTime", "createDate", ColumnDataType.DATE,
            propsBean.getString("views.processTable.column.linkCreateTime"), null, true, false);

      ColumnPreference linkCreateUserCol = new ColumnPreference("LinkCreateUser", "createUser", ColumnDataType.STRING,
            propsBean.getString("views.processTable.column.linkCreateUser"), null, true, false);

      ColumnPreference linkCommentCol = new ColumnPreference("LinkComment", "comment",
            propsBean.getString("views.processTable.column.linkComment"), ResourcePaths.V_PROCESS_TABLE_COLUMNS, true,
            false);

      ColumnPreference caseOwnerCol = new ColumnPreference("CaseOwner", "caseOwner", ColumnDataType.STRING,
            propsBean.getString("overview.CaseOwner"), null, false, false);
      
      //set descriptors list and map<dataId, dataPath>
      allDescriptors = CommonDescriptorUtils.getAllDescriptors(false);

      List<ColumnPreference> procCols = new ArrayList<ColumnPreference>();
      procCols.add(processNameCol);
      procCols.add(pOIDCol);
      procCols.add(prioCol);
      procCols.add(descriptorsCol);
      procCols.add(userCol);
      procCols.add(startTimeCol);
      procCols.add(durationCol);
      procCols.add(statusCol);
      procCols.add(pRootOIDCol);
      procCols.add(endTimeCol);
      procCols.add(caseOwnerCol);

      // Set for Linked Process Table in ProcessInstanceDetailView      
      if (displayLinkInfo)
      {
         pOIDCol.setVisible(false);
         prioCol.setVisible(false);
         userCol.setVisible(false);
         startTimeCol.setVisible(false);
         durationCol.setVisible(false);
         pRootOIDCol.setVisible(false);
         endTimeCol.setVisible(false);
         statusCol.setVisible(true);
         procCols.add(linkTypeCol);
         procCols.add(linkCreateTimeCol);
         procCols.add(linkCreateUserCol);
         procCols.add(linkCommentCol);
      }
      
      // Adding Descriptor Columns
      List<ColumnPreference> descriptorColumns = DescriptorColumnUtils.createDescriptorColumns(processTable, allDescriptors);
      procCols.addAll(descriptorColumns);

      IColumnModel procInstanceColumnModel = new DefaultColumnModel(procCols, null, processFixedCols2, moduleId,
            viewId, columnModelListener);
      
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(
            procInstanceColumnModel);

      filterHandler = new ProcessTableFilterHandler(allDescriptors);
      sortHandler = new ProcessTableSortHandler(allDescriptors);
     
      DescriptorColumnUtils.setDescriptorColumnFilters(procInstanceColumnModel, allDescriptors);
      
      processTable = new PaginatorDataTable<ProcessInstanceTableEntry, ProcessInstance>(
            colSelecpopup, null, filterHandler, sortHandler, this,
            new DataTableSortModel<ProcessInstanceTableEntry>("startTime", false));
      
      processTable.setRowSelector(new DataTableRowSelector("checkSelection", true));
      processTable.setDataTableExportHandler(new ProcessTableExportHandler());
   }
   
   /**
    * Checks whether priority is changed or not for selected process
    * @return
    */
   public boolean isPriorityChanged()
   {
      if (null != processTable)
      {
         List<ProcessInstanceTableEntry> processList = processTable.getCurrentList();
         for (ProcessInstanceTableEntry processInstanceWithPrioTableEntry : processList)
         {
            if (processInstanceWithPrioTableEntry.getOldPriority() != processInstanceWithPrioTableEntry.getPriority())
            {
               return true;
            }
         }
      }
      return false;
   }
   
   /**
    * @return processes whose priority is changed
    */
   private Map<Object, Integer> getChangedProcesses()
   {
      List<ProcessInstanceTableEntry> processList = processTable.getCurrentList();
      Map<Object, Integer> changedProcesses = new HashMap<Object, Integer>();
      if (CollectionUtils.isNotEmpty(processList))
      {
         for (ProcessInstanceTableEntry processInstanceEntry : processList)
         {
            if (processInstanceEntry.isPriorityChanged())
            {
               changedProcesses.put(processInstanceEntry.getProcessInstance(), processInstanceEntry.getPriority());
            }
         }
      }
      return changedProcesses;
   }
   
   // ********************* Default Getter & Setter methods ************************
   public PaginatorDataTable<ProcessInstanceTableEntry, ProcessInstance> getProcessTable()
   {
      return processTable;
   }

   public ArrayList<SelectItem> getAllPriorities()
   {
      return allPriorities;
   }

   public void setProcessTable(
         PaginatorDataTable<ProcessInstanceTableEntry, ProcessInstance> processTable)
   {
      this.processTable = processTable;
   }

   public ICallbackHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   
   public boolean isDisplayLinkInfo()
   {
      return displayLinkInfo;
   }

   public void setDisplayLinkInfo(boolean displayLinkInfo)
   {
      this.displayLinkInfo = displayLinkInfo;
   }
   
   private void initializeSelectiveDescriptorFetchProperties()
   {
      List<ColumnPreference> colPrefs = processTable.getColumnModel().getSelectableColumns();
      visibleDescriptorsIds = new HashSet<String>();
      fetchAllDescriptors = false;
      for (ColumnPreference colPref : colPrefs)
      {
         if (DESCRIPTOR_COL_NAME.equals(colPref.getColumnName()) && colPref.isVisible())
         {
            fetchAllDescriptors = true;
         }
         else if (isDescriptorColumn(colPref) && colPref.isVisible())
         {
            visibleDescriptorsIds.add(colPref.getColumnName());
         }
      }
   }
   
   /**
    * @return
    */
   public boolean isFetchAllDescriptors()
   {
      if (null == visibleDescriptorsIds)
      {
         initializeSelectiveDescriptorFetchProperties();
      }
      return fetchAllDescriptors;
   }

   /**
    * Returns a set descriptor IDs of the descriptor columns that are visible.
    * 
    * @return
    */
   public Set<String> getVisibleDescriptorsIds()
   {
      if (null == visibleDescriptorsIds)
      {
         initializeSelectiveDescriptorFetchProperties();
      }
      return visibleDescriptorsIds;
   }
   
   /**
    * @param colPref
    * @return
    */
   private boolean isDescriptorColumn(ColumnPreference colPref)
   {
      return allDescriptors.keySet().contains(colPref.getColumnName());
   }
   
   /**
    * @author Subodh.Godbole
    * 
    */
   public static class ProcessTableFilterHandler extends IppFilterHandler
   {
      private static final long serialVersionUID = 1L;
      private Map<String, DataPath> allDescriptors;

      public ProcessTableFilterHandler(Map<String, DataPath> descriptorNameAndDataPathMap)
      {
         super();
         this.allDescriptors = descriptorNameAndDataPathMap;
      }

      @Override
      public void applyFiltering(Query query, List<ITableDataFilter> filters)
      {
         GenericDescriptorFilterModel filterModel = null;
         FilterAndTerm filter = query.getFilter().addAndTerm();
         
         for (ITableDataFilter tableDataFilter : filters)
         {
            if (tableDataFilter.isFilterSet())
            {
               String dataId = tableDataFilter.getName();
               if ("RootPOID".equals(dataId))
               {
                  Long start = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Long end = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (start != null)
                  {
                     filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.greaterOrEqual(start));
                  }
                  if (end != null)
                  {
                     filter.and(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID.lessOrEqual(end));
                  }
               }
               else if ("ProcessOID".equals(dataId))
               {
                  Long start = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Long end = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (start != null)
                  {
                     filter.and(ProcessInstanceQuery.OID.greaterOrEqual(start));
                  }
                  if (end != null)
                  {
                     filter.and(ProcessInstanceQuery.OID.lessOrEqual(end));
                  }
               }
               else if ("StartTime".equals(dataId))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                     filter.and(ProcessInstanceQuery.START_TIME.greaterOrEqual(startTime
                           .getTime()));

                  if (endTime != null)
                     filter.and(ProcessInstanceQuery.START_TIME.lessOrEqual(endTime
                           .getTime()));
               }
               else if ("EndTime".equals(dataId))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                     filter.and(ProcessInstanceQuery.TERMINATION_TIME.greaterOrEqual(startTime
                           .getTime()));

                  if (endTime != null)
                     filter.and(ProcessInstanceQuery.TERMINATION_TIME.lessOrEqual(endTime
                           .getTime()));
               }
               else if ("Status".equals(dataId))
               {
                  if (((ITableDataFilterPickList) tableDataFilter).getSelected() != null)
                  {
                     FilterTerm orTerm = filter.addOrTerm();
                     if (((ITableDataFilterPickList) tableDataFilter).getSelected()
                           .size() > 0)
                     {
                        for (int i = 0; i < ((ITableDataFilterPickList) tableDataFilter)
                              .getSelected().size(); i++)
                        {
                           orTerm.add(ProcessInstanceQuery.STATE
                                 .isEqual(Long.parseLong(((ITableDataFilterPickList) tableDataFilter)
                                       .getSelected().get(i).toString())));
                        }
                     }
                  }
               }

               else if ("ProcessName".equals(dataId))
               {
                  if (((ITableDataFilterPickList) tableDataFilter).getSelected() != null)
                  {
                     FilterOrTerm or = filter.addOrTerm();
                     if (((ITableDataFilterPickList) tableDataFilter).getSelected()
                           .size() > 0)
                     {
                        for (int i = 0; i < ((ITableDataFilterPickList) tableDataFilter)
                              .getSelected().size(); i++)
                        {
                           or.add(new ProcessDefinitionFilter(
                                 ((ITableDataFilterPickList) tableDataFilter)
                                       .getSelected().get(i).toString(), false));
                        }
                     }
                  }
               }
               else if ("Priority".equals(dataId))
               {
                  PriorityAutocompleteTableDataFilter priorityfilter = (PriorityAutocompleteTableDataFilter) tableDataFilter;
                  List<PriorityAutoCompleteItem> priorityItems = priorityfilter.getPriorityAutocompleteSelector()
                        .getSelectedValues();

                  FilterOrTerm or = filter.addOrTerm();
                  for (PriorityAutoCompleteItem priority : priorityItems)
                  {
                     or.or(ProcessInstanceQuery.PRIORITY.isEqual(priority.getPriority()));
                  }
               }
               else if ("StartingUser".equals(dataId))
               {
                  UserAutocompleteTableDataFilter userFilter = (UserAutocompleteTableDataFilter) tableDataFilter;
                  List<UserWrapper> users = userFilter.getUserSelector().getSelectedValues();

                  FilterOrTerm or = filter.addOrTerm();
                  for (UserWrapper user : users)
                  {
                     or.add(new org.eclipse.stardust.engine.api.query.StartingUserFilter(user.getUser().getOID()));
                  }
               }
               else if (this.allDescriptors.containsKey(dataId))
               {
                  query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

                  if (null == filterModel)
                  {
                     filterModel = GenericDescriptorFilterModel.create(allDescriptors.values());
                     filterModel.setFilterEnabled(true);
                  }
                  filterModel.setFilterValue(dataId,
                        DescriptorColumnUtils.getFilterValue(tableDataFilter, allDescriptors.get(dataId)));
               }
               else
               {
                  if (trace.isDebugEnabled())
                  {
                     trace.debug("ProcessTableFilterAndSortHandler.applyFiltering() : Filtering not implemented for " + tableDataFilter);
                  }
               }
            }
         }
         if (null != filterModel)
         {
            DescriptorFilterUtils.applyFilters(query, filterModel);
         }
      }
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   public static class ProcessTableSortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = 1L;
      private Map<String, DataPath> allDescriptors;

      public ProcessTableSortHandler(Map<String, DataPath> descriptorNameAndDataPathMap)
      {
         super();
         this.allDescriptors = descriptorNameAndDataPathMap;
      }

      @Override
      public void applySorting(Query query, List sortCriteriaList)
      {
         Iterator< ? > iterator = sortCriteriaList.iterator();

         // As per current Architecture, this list will hold only one item
         if (iterator.hasNext())
         {
            SortCriterion sortCriterion = (SortCriterion) iterator.next();
            if (trace.isDebugEnabled())
            {
               trace.debug("sortCriterion = " + sortCriterion);
            }

            if (sortCriterion.getProperty().startsWith("descriptorValues."))
            {
               String[] descriptorNames = sortCriterion.getProperty().split("\\.");
               String descriptorName = descriptorNames[1];

               if (allDescriptors.containsKey(descriptorName))
               {
                  query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
                  String columnName = getDescriptorColumnName(descriptorName, allDescriptors);
                  if (CommonDescriptorUtils.isStructuredData(allDescriptors.get(descriptorName)))
                  {
                     query.orderBy(new DataOrder(columnName,
                           getXpathName(descriptorName, allDescriptors), sortCriterion.isAscending()));
                  }
                  else
                  {
                     query.orderBy(new DataOrder(columnName, sortCriterion.isAscending()));
                  }
               }
            }
            else if ("processInstanceRootOID".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ProcessInstanceQuery.ROOT_PROCESS_INSTANCE_OID,
                     sortCriterion.isAscending());
            }
            else if ("processInstanceOID".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ProcessInstanceQuery.OID, sortCriterion.isAscending());
            }
            else if ("priority".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ProcessInstanceQuery.PRIORITY, sortCriterion.isAscending());
            }
            else if ("startTime".equals(sortCriterion.getProperty()))
            {
               query
                     .orderBy(ProcessInstanceQuery.START_TIME, sortCriterion
                           .isAscending());
            }
            else if ("endTime".equals(sortCriterion.getProperty()))
            {
               query
                     .orderBy(ProcessInstanceQuery.TERMINATION_TIME, sortCriterion
                           .isAscending());
            }
            else if ("processInstanceName".equals(sortCriterion.getProperty()))
            {
               CustomOrderCriterion o = ProcessInstanceQuery.PROC_DEF_NAME
                     .ascendig(sortCriterion.isAscending());
               query.orderBy(o);
            }
            else if ("startingUser".equals(sortCriterion.getProperty()))
            {

               query.orderBy(ProcessInstanceQuery.STARTING_USER_OID, sortCriterion
                     .isAscending());
            }

            else
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("ProcessTableFilterAndSortHandler.applySorting(): Sorting not implemented for " + sortCriterion);
               }
            }
         }
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class ColumnModelListener implements IColumnModelListener
   {
      private boolean needRefresh;
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.column.IColumnModelListener#columnsRearranged(org.eclipse.stardust.ui.web.common.column.IColumnModel)
       */
      public void columnsRearranged(IColumnModel columnModel)
      {
         handleNewlyAddedDescriptorColumns(columnModel);
        
         if (needRefresh)
         {
            FacesUtils.refreshPage();// this is required where we have multiple tables on same page.
         }
      }
      
      public void setNeedRefresh(boolean needRefresh)
      {
         this.needRefresh = needRefresh;
      }
      
      /**
       * Refresh the table if a descriptor column or the "descriptors" column is newly added.
       * Doesn't refresh if any descriptor columns are newly added but the "descriptors" column was already visible.
       *   
       * @param columnModel
       */
      private void handleNewlyAddedDescriptorColumns(IColumnModel columnModel)
      {
         initializeSelectiveDescriptorFetchProperties();
         boolean descriptorsColWasVisibleBefore = false;
         boolean hasNewlyAddedDescColumns = false;
         List<ColumnPreference> colPrefs = columnModel.getSelectableColumns();
         for (ColumnPreference colPref : colPrefs)
         {
            if (allDescriptors.containsKey(colPref.getColumnName()) && colPref.isNewlyVisible())
            {
               hasNewlyAddedDescColumns = true;
            }
            
            if (DESCRIPTOR_COL_NAME.equals(colPref.getColumnName()) && colPref.isVisible())
            {
               if (colPref.isNewlyVisible())
               {
                  hasNewlyAddedDescColumns = true;
               }
               else
               {
                  descriptorsColWasVisibleBefore = true;
               }
            }
         }

         if (!descriptorsColWasVisibleBefore && hasNewlyAddedDescColumns)
         {
            getProcessTable().refresh(true);
         }
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   private class ProcessTableExportHandler implements DataTableExportHandler<ProcessInstanceTableEntry>
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object, java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column,
            ProcessInstanceTableEntry row, Object value)
      {
         if ("Priority".equals(column.getColumnName()))
         {
            return ProcessInstanceUtils.getPriorityLabel(row.getPriority());
         }
         else if (DESCRIPTOR_COL_NAME.equals(column.getColumnName()))
         {
            return DescriptorColumnUtils.exportDescriptors(row.getProcessDescriptorsList(),
                  ExportType.EXCEL == exportType ? "\n" : ", ");
         }
         else
         {
            return value;
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleHeaderCellExport(org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.String)
       */
      public String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text)
      {
         return text;
      }
   }

   private static String getXpathName(String descriptorName, Map<String, DataPath> descriptorNameAndDataPathMap)
   {
      if (descriptorNameAndDataPathMap.containsKey(descriptorName))
      {
         DataPath columnNameDataPath = (DataPath) descriptorNameAndDataPathMap.get(descriptorName);

         return columnNameDataPath.getAccessPath();
      }
      else
         return null;
   }

   private static String getDescriptorColumnName(String descriptorName, Map<String, DataPath> descriptorNameAndDataPathMap)
   {
      if (descriptorNameAndDataPathMap.containsKey(descriptorName))
      {
         DataPath columnNameDataPath = (DataPath) descriptorNameAndDataPathMap.get(descriptorName);

         return columnNameDataPath.getData();
      }
      else
         return null;
   }

     
   /**
    * 
    */
   public void openCreateCase(ActionEvent event)
   {
      List<ProcessInstance> selectedProcesses = getSelectedProcessList();
      ProcessInstanceUtils.openCreateCase(selectedProcesses);
   }

   /**
    * 
    * @return
    */
   public boolean isCanCreateCase()
   {
      return canCreateCase;
   }

   /**
    * 
    */
   public void attachToCase(ActionEvent event)
   {
      List<ProcessInstance> selectedProcesses = getSelectedProcessList();
      ProcessInstanceUtils.openAttachToCase(selectedProcesses);
   }  
  
   /**
    * 
    */
   public ProcessInstanceTableEntry createUserObject(Object resultRow)
   {
      ProcessInstanceTableEntry tableEntry = null;
      ProcessInstance processInstance = null;
      try
      {
         if (resultRow instanceof ProcessInstanceTableEntry)
         {
            tableEntry = (ProcessInstanceTableEntry) resultRow;
            processInstance = tableEntry.getProcessInstance();
         }
         else if (resultRow instanceof ProcessInstance)
         {
            processInstance = (ProcessInstance) resultRow;
         }

         if (processInstance != null)
         {
            // for Case Link Info Table
            if (displayLinkInfo)
            {
               List<ProcessInstanceLink> linkedProcess = processInstance.getLinkedProcessInstances();

               if (CollectionUtils.isNotEmpty(linkedProcess))
               {
                  ProcessInstanceLink processInstanceLink = linkedProcess.get(0);
                  if (resultRow instanceof ProcessInstance)
                  {
                     tableEntry = new ProcessInstanceTableEntry(processInstance, processInstanceLink);
                  }
                  else if (null != tableEntry)
                  {
                     tableEntry.initProcessInstanceLink(processInstanceLink);
                  }
               }
            }
            else if (resultRow instanceof ProcessInstance)
            {
               tableEntry = new ProcessInstanceTableEntry(processInstance);               
            }
         }
      }
      catch (Exception e)
      {
         trace.error(e);
         ProcessInstanceTableEntry processInstanceWithPrioTableEntry = new ProcessInstanceTableEntry();
         processInstanceWithPrioTableEntry.setLoaded(false);
         processInstanceWithPrioTableEntry.setCause(e);
         tableEntry = processInstanceWithPrioTableEntry;
      }
      return tableEntry;
   }  

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   public ColumnModelListener getColumnModelListener()
   {
      return columnModelListener;
   }  
   
   
   
}
