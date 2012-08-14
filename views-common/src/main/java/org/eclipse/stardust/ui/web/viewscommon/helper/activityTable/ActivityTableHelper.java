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
package org.eclipse.stardust.ui.web.viewscommon.helper.activityTable;

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
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DataOrder;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
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
import org.eclipse.stardust.ui.web.viewscommon.common.ProcessActivityDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.PriorityAutoCompleteItem;
import org.eclipse.stardust.ui.web.viewscommon.common.PriorityAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityAutocompleteItem;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.AbortActivityBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.CallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.JoinProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PanelConfirmation;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.SwitchProcessDialogBean;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.user.ParticipantWrapper;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ActivityTableHelper implements ICallbackHandler , IUserObjectBuilder<ActivityInstanceWithPrioTableEntry>
{
   private static final Logger trace = LogManager.getLogger(ActivityTableHelper.class);

   private static final String COL_ACTIVITY_NAME = "ActivityName";

   private static final String COL_ASSIGNED_TO = "AssignedTo";

   private static final String COL_COMPLETED_BY = "CompletedBy";

   private static final String DESCRIPTOR_COL_NAME = "Descriptors";

   private ICallbackHandler callbackHandler;

   private PaginatorDataTable<ActivityInstanceWithPrioTableEntry, ActivityInstance> activityTable;

   private MessagesViewsCommonBean propsBean;

   private ArrayList<SelectItem> allPriorities;

   private ActivityTableFilterHandler filterHandler;
   
   private ActivityTableSortHandler sortHandler;
   
   private List<SelectItem> allStatusList = new ArrayList<SelectItem>();
   
   private boolean strandedActivityView = false;
   
   private Map<String, DataPath> allDescriptors = CollectionUtils.newMap();
   
   private boolean switchPanelDisplayOn;
   
   private Map<Long, ProcessInstance> processInstanceMap; 
   
   private ColumnModelListener columnModelListener;
   
   private Set<String> visibleDescriptorsIds;
   
   private boolean fetchAllDescriptors;
   
   private boolean hasJoinProcessPermission;
   
   private boolean hasSwitchProcessPermission;

   public ActivityTableHelper()
   {
      super();
      propsBean = MessagesViewsCommonBean.getInstance();

      allPriorities = new ArrayList<SelectItem>();
      allPriorities.add(new SelectItem(new Integer(1), propsBean.getString("views.processTable.priorities.high")));
      allPriorities.add(new SelectItem(new Integer(0), propsBean.getString("views.processTable.priorities.normal")));
      allPriorities.add(new SelectItem(new Integer(-1), propsBean.getString("views.processTable.priorities.low")));
      
      allStatusList = ActivityInstanceUtils.getAllActivityStates();
      
      columnModelListener = new ColumnModelListener();
      
      hasJoinProcessPermission = AuthorizationUtils.hasAbortAndJoinProcessInstancePermission();

      hasSwitchProcessPermission = AuthorizationUtils.hasAbortAndStartProcessInstancePermission();
   }
   
   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getSelectedItemCount()
   {
      int count = 0;

      if(null != activityTable)
      {
         List<ActivityInstanceWithPrioTableEntry> activityList = activityTable
               .getCurrentList();
         for (Iterator iterator = activityList.iterator(); iterator.hasNext();)
         {
            ActivityInstanceWithPrioTableEntry activityInstanceWithPrioTableEntry = (ActivityInstanceWithPrioTableEntry) iterator
                  .next();
            if (activityInstanceWithPrioTableEntry.isCheckSelection())
            {
               count++;
            }
         }
      }
      return count;

   }
   
   /**
    * @param activityInstances
    * @param callbackHandler
    */
   public void openDelegateDialog(List<ActivityInstance> activityInstances,
         ICallbackHandler callbackHandler)
   {
      if (null != activityInstances)
      {
         DelegationBean delegationBean = (DelegationBean) FacesUtils
               .getBeanFromContext("delegationBean");
         delegationBean.setAis(activityInstances);
         delegationBean.setICallbackHandler(callbackHandler);
         delegationBean.openPopup();
      }
   }

   /**
    * @param ae
    */
   public void openDelegateDialog(ActionEvent ae)
   {
      ActivityInstance ai = (ActivityInstance) ae.getComponent().getAttributes().get(
            "activityInstance");
      List<ActivityInstance> ais = new ArrayList<ActivityInstance>();
      if (ai != null)
      {
         ais.add(ai);
      }
      else
      {
         List<ActivityInstanceWithPrioTableEntry> ait = activityTable.getCurrentList();
         for (Iterator iterator = ait.iterator(); iterator.hasNext();)
         {
            ActivityInstanceWithPrioTableEntry at = (ActivityInstanceWithPrioTableEntry) iterator
                  .next();
            if (at.isCheckSelection())
            {
               ActivityInstance a = at.getActivityInstance();
               ais.add(a);
            }
         }
      }
      if (null != ais)
      {
         DelegationBean delegationBean = (DelegationBean) FacesUtils
               .getBeanFromContext("delegationBean");
            delegationBean.setAis(ais);
            delegationBean.setICallbackHandler(callbackHandler);
            delegationBean.openPopup();
         }
      }   
   
   /**
    * 
    */
   public void refreshActivityTable()
   {
      activityTable.refresh(new DataTableSortModel<ActivityInstanceWithPrioTableEntry>(
            "startTime", false));
   }

   /**
    * Save Process Priorities
    * 
    * @param event
    */
   public void applyChanges(ActionEvent event)
   {
      try
      {
         ProcessInstanceUtils.updatePriorities(getChangedProcesses());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      finally
      {
         if (null != callbackHandler)
         {
            callbackHandler.handleEvent(EventType.APPLY);
         }
         activityTable.refresh(true);
      }
   }

   /**
    * @param activityInstances
    * @param callbackHandler
    */
   public void openAbortDialog(List<ActivityInstance> activityInstances, ICallbackHandler callbackHandler)
   {
      if (CollectionUtils.isNotEmpty(activityInstances))
      {
         AbortActivityBean abortActivity = AbortActivityBean.getInstance();
         abortActivity.setCallbackHandler(callbackHandler);
         abortActivity.abortActivities(activityInstances);
      }
   }

   /**
    * @param ae
    */
   public void openAbortDialog(ActionEvent ae)
   {
      ActivityInstance ai = (ActivityInstance) ae.getComponent().getAttributes().get("activityInstance");

      List<ActivityInstance> ais = new ArrayList<ActivityInstance>();
      if (ai != null)
      {
         ais.add(ai);
      }
      else
      {
         List<ActivityInstanceWithPrioTableEntry> aits = activityTable.getCurrentList();
         for (ActivityInstanceWithPrioTableEntry ActivityInstanceWithPrioTableEntry : aits)
         {
            if (ActivityInstanceWithPrioTableEntry.isCheckSelection())
            {
               ais.add(ActivityInstanceWithPrioTableEntry.getActivityInstance());
            }
         }
      }
      openAbortDialog(ais, this);
   }

   /**
    * Opens notes dialog
    * 
    * @param ae
    */
   public static void openNotes(ActionEvent ae)
   {
      ActivityInstance ai = (ActivityInstance) ae.getComponent().getAttributes().get("activityInstance");
      if (ai != null)
      {
         ProcessInstanceUtils.openNotes(ai.getProcessInstance());
      }
   }
   
   /**
    * Delegates the selected activity to default performer
    * 
    * @param ae
    */
   public void performDefaultDelegation(ActionEvent ae)
   {
      ActivityInstance ai = (ActivityInstance) ae.getComponent().getAttributes().get("activityInstance");
      if (ai != null)
      {
         // create callback handler
         CallbackHandler callbackHandler = new CallbackHandler(ai)
         {
            public void handleEvent(EventType eventType)
            {
               performDefaultDelegation(getPayload());
            }
         };
         // create panelpopup
         PanelConfirmation panelConfirmation = PanelConfirmation.getInstance(true);
         panelConfirmation.setCallbackHandler(callbackHandler);
         panelConfirmation.setMessage(MessagesViewsCommonBean.getInstance().getString(
               "views.strandedActivities.confirmDefaultDelegate"));
         panelConfirmation.openPopup();

      }
      else
      {

         performDefaultDelegation(ai);
         return;
      }
   }
   
   /**
    * @param query
    */
   public void applyDescriptorPolicy(Query query)
   {
      if (isFetchAllDescriptors())
      {
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
      }
      else if (CollectionUtils.isEmpty(getVisibleDescriptorsIds()))
      {
         query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
      }
      else
      {
         query.setPolicy(DescriptorPolicy.withIds(getVisibleDescriptorsIds()));
      }
   }

   /**
    * Initialize Activity table
    */
   public void initActivityTable()
   {
      initActivityTable(UserPreferencesEntries.M_VIEWS_COMMON, UserPreferencesEntries.V_ACTIVITY_WITH_PRIO);
   }

   /**
    * Initializes Activity table
    */
   public void initActivityTable(String moduleId, String viewId)
   {
      ColumnPreference activityNameCol = new ColumnPreference(
            COL_ACTIVITY_NAME,
            "activityName",
            propsBean
                  .getString("views.activityTable.column.activityName"),
            ResourcePaths.V_ACTIVITY_TABLE_COLUMNS, true, true);

      activityNameCol.setColumnDataFilterPopup(new TableDataFilterPopup(new ProcessActivityDataFilter(
            ResourcePaths.V_PROCESS_ACTIVITY_FILTER, true)));

      ColumnPreference aOIDCol = new ColumnPreference("ActivityOID", "activityOID",
              ColumnDataType.NUMBER, propsBean
                    .getString("views.activityTable.activityTable.column.aOID"),
              new TableDataFilterPopup(new TableDataFilterNumber("ActivityOID", "",
                    DataType.LONG, true, null, null)), true, true);

      ColumnPreference prioCol = new ColumnPreference("Priority", "priority", propsBean
            .getString("views.activityTable.processTable.column.priority"),
            ResourcePaths.V_ACTIVITY_TABLE_COLUMNS, true, true);
      prioCol.setColumnAlignment(ColumnAlignment.CENTER);
      prioCol.setColumnDataFilterPopup(new TableDataFilterPopup(new PriorityAutocompleteTableDataFilter()));
      
      ColumnPreference criticalityCol = new ColumnPreference("Criticality", "criticality",
            propsBean.getString("views.activityTable.processTable.column.criticality"),
            ResourcePaths.V_ACTIVITY_TABLE_COLUMNS, true, true);
      criticalityCol.setColumnAlignment(ColumnAlignment.CENTER);
      criticalityCol.setColumnDataFilterPopup(new TableDataFilterPopup(new CriticalityAutocompleteTableDataFilter()));
            
      ColumnPreference descriptorsCol = new ColumnPreference(DESCRIPTOR_COL_NAME,
            "processDescriptorsList", propsBean
                  .getString("views.activityTable.descriptors.label"),
                  ResourcePaths.V_ACTIVITY_TABLE_COLUMNS, true, false);
      descriptorsCol.setNoWrap(true);

      ColumnPreference startTimeCol = new ColumnPreference("StartTime", "startTime",
            ColumnDataType.DATE, propsBean
                  .getString("views.activityTable.processTable.column.startTime"),
            new TableDataFilterPopup(new TableDataFilterDate(DataType.DATE)), true,
            true);
      startTimeCol.setNoWrap(true);

      ColumnPreference lastModifiedCol = new ColumnPreference(
            "EndTime",
            "lastModified",
            ColumnDataType.DATE,
            propsBean
                  .getString("views.activityTable.activityTable.column.lastModified"),
            new TableDataFilterPopup(new TableDataFilterDate(DataType.DATE)), true,
            true);
      lastModifiedCol.setNoWrap(true);

      ColumnPreference durationCol = new ColumnPreference("Duration", "duration",
            ColumnDataType.STRING, propsBean
                  .getString("views.activityTable.activityTable.column.duration"),
            null, true, false);
      durationCol.setColumnAlignment(ColumnAlignment.CENTER);
      durationCol.setNoWrap(true);

      ColumnPreference statusCol = new ColumnPreference("Status", "status",
            ColumnDataType.STRING, propsBean
                  .getString("views.activityTable.priorityTable.column.status"),
            new TableDataFilterPopup(new TableDataFilterPickList(
                  FilterCriteria.SELECT_MANY, allStatusList, RenderType.LIST, 3, null)),
            false, true);

      ColumnPreference assignedToCol = new ColumnPreference(COL_ASSIGNED_TO,
            "currentPerformer", ColumnDataType.STRING, propsBean
                  .getString("views.activityTable.activityTable.column.assignedTo"),
            true, false);
      assignedToCol.setColumnDataFilterPopup(new TableDataFilterPopup(new ParticipantAutocompleteTableDataFilter()));
      
      ColumnPreference completedByCol = new ColumnPreference(COL_COMPLETED_BY,
            "performedBy", ColumnDataType.STRING, propsBean
                  .getString("views.activityTable.activityTable.column.completedBy"),
            false, false);

      completedByCol.setColumnDataFilterPopup(new TableDataFilterPopup(new UserAutocompleteTableDataFilter()));

      List<ColumnPreference> activityFixedCols2 = new ArrayList<ColumnPreference>();
      ColumnPreference col = new ColumnPreference("Actions", "", propsBean
            .getString("views.common.column.actions"),
            ResourcePaths.V_ACTIVITY_TABLE_COLUMNS, true, false);
      col.setColumnAlignment(ColumnAlignment.RIGHT);
      col.setExportable(false);
      activityFixedCols2.add(col);

      ColumnPreference pIDCol = new ColumnPreference(
            "ProcessId",
            "processID",
            ColumnDataType.STRING,
            propsBean
                  .getString("views.activityTable.processTable.column.processName"),
            null, false, false);
      ColumnPreference pOIDCol = new ColumnPreference("ProcessOID", "processOID",
            ColumnDataType.NUMBER, propsBean
                  .getString("views.activityTable.processTable.column.pOID"),
            new TableDataFilterPopup(new TableDataFilterNumber(DataType.LONG)), false,
            true);

      ColumnPreference participantPerformerCol = new ColumnPreference(
            "ParticipantPerformer",
            "participantPerformer",
            ColumnDataType.STRING,
            propsBean
                  .getString("views.activityTable.activityTable.column.participantPerformer"),
            false, false);

      //set descriptors list and map<dataId, dataPath>
      allDescriptors = CommonDescriptorUtils.getAllDescriptors(false);

      List<ColumnPreference> activityCols = new ArrayList<ColumnPreference>();
      activityCols.add(activityNameCol);
      activityCols.add(aOIDCol);
      activityCols.add(prioCol);
      activityCols.add(criticalityCol);
      activityCols.add(descriptorsCol);
      activityCols.add(startTimeCol);
      activityCols.add(lastModifiedCol);
      activityCols.add(durationCol);
      activityCols.add(statusCol);
      activityCols.add(assignedToCol);
      activityCols.add(completedByCol);
      activityCols.add(pIDCol);
      activityCols.add(pOIDCol);
      activityCols.add(participantPerformerCol);

      // Adding Descriptor Columns
      List<ColumnPreference> descriptorColumns = DescriptorColumnUtils.createDescriptorColumns(activityTable, allDescriptors);
      activityCols.addAll(descriptorColumns);

      IColumnModel activityColumnModel = new DefaultColumnModel(activityCols, null, activityFixedCols2, moduleId,
            viewId, columnModelListener);
      TableColumnSelectorPopup activitySelecpopup = new TableColumnSelectorPopup(
            activityColumnModel);
      
      DescriptorColumnUtils.setDescriptorColumnFilters(activityColumnModel, allDescriptors);
      
      filterHandler = new ActivityTableFilterHandler(allDescriptors);
      sortHandler = new ActivityTableSortHandler(allDescriptors);
      
      activityTable = new PaginatorDataTable<ActivityInstanceWithPrioTableEntry, ActivityInstance>(
            activitySelecpopup,
            null,
            filterHandler,
            sortHandler,
            this,
            new DataTableSortModel<ActivityInstanceWithPrioTableEntry>("startTime", false));
      
      activityTable.setDataTableExportHandler(new ActivityTableExportHandler());
      activityTable.setRowSelector(new DataTableRowSelector("checkSelection", true));
   }

   public void handleEvent(EventType eventType)
   {
     refreshActivityTable();
      if (null != callbackHandler)
      {
         callbackHandler.handleEvent(EventType.APPLY);
      }
   }
   
   /**
    * @return
    */
   public boolean isPriorityChanged()
   {
      boolean activityPrioChange = false;
      if (null != activityTable)
      {
         List<ActivityInstanceWithPrioTableEntry> activityList = activityTable.getCurrentList();
         for (ActivityInstanceWithPrioTableEntry activityInstanceWithPrioTableEntry : activityList)
         {           
            if (activityInstanceWithPrioTableEntry.getOldPriority() != activityInstanceWithPrioTableEntry.getPriority())
            {
               activityPrioChange = true;
               break;
            }
         }
      }
      return activityPrioChange;
   }

   private void setProcessPriority(long processInstanceOID, int priority)
   {
      try
      {
         ProcessInstanceUtils.setProcessPriority(processInstanceOID, priority);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   /**
    * @return processes whose priority is changed
    */
   private Map<Object, Integer> getChangedProcesses()
   {
      List<ActivityInstanceWithPrioTableEntry> activityList = activityTable.getCurrentList();
      Map<Object, Integer> changedProcesses = new HashMap<Object, Integer>();
      if (CollectionUtils.isNotEmpty(activityList))
      {
         for (ActivityInstanceWithPrioTableEntry activityInstanceEntry : activityList)
         {
            if (activityInstanceEntry.isPriorityChanged())
            {
               changedProcesses.put(activityInstanceEntry.getActivityInstance(),
                     activityInstanceEntry.getPriority());
            }
         }
      }
      return changedProcesses;
   }

   //****************** Default Getter & Setter methods ******************
   public PaginatorDataTable<ActivityInstanceWithPrioTableEntry, ActivityInstance> getActivityTable()
   {
      return activityTable;
   }

   public ArrayList<SelectItem> getAllPriorities()
   {
      return allPriorities;
   }

   public ICallbackHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }
   
   public boolean isStrandedActivityView()
   {
      return strandedActivityView;
   }

   public void setStrandedActivityView(boolean strandedActivityView)
   {
      this.strandedActivityView = strandedActivityView;
   }

   /**
    * 
    */
   private void initializeSelectiveDescriptorFetchProperties()
   {
      List<ColumnPreference> colPrefs = activityTable.getColumnModel().getSelectableColumns();
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
   public class ActivityTableFilterHandler extends IppFilterHandler
   {
      private static final long serialVersionUID = 1L;
      private Map<String, DataPath> allDescriptors;
      
      public ActivityTableFilterHandler(Map<String, DataPath> allDescriptors)
      {
         super();
         this.allDescriptors = allDescriptors;
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
               if ("ActivityOID".equals(dataId))
               {
                  Long start = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Long end = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (start != null)
                  {
                     filter.and(ActivityInstanceQuery.OID.greaterOrEqual(start));
                  }
                  if (end != null)
                  {
                     filter.and(ActivityInstanceQuery.OID.lessOrEqual(end));
                  }
               }

               else if (COL_ACTIVITY_NAME.equals(dataId))
               {
                  ProcessActivityDataFilter pfilter = (ProcessActivityDataFilter) tableDataFilter;
                  List<Activity> selectedActivities = pfilter.getSelectedActivityDefs();
                  
                  if (CollectionUtils.isEmpty(selectedActivities))
                  {
                     filter.add(ActivityFilter.forAnyProcess("-1"));
                  }
                  else
                  {
                     FilterOrTerm or = filter.addOrTerm();

                     for (Activity activity : selectedActivities)
                     {
                        or.add(ActivityInstanceQuery.ACTIVITY_OID.isEqual(activity.getRuntimeElementOID()));
                     }
                  } // for each
               }
               else if ("ProcessOID".equals(dataId))
               {
                  Long start = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Long end = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (start != null)
                  {
                     filter.and(ActivityInstanceQuery.PROCESS_INSTANCE_OID.greaterOrEqual(start));
                  }
                  if (end != null)
                  {
                     filter.and(ActivityInstanceQuery.PROCESS_INSTANCE_OID.lessOrEqual(end));
                  }
               }
               else if ("EndTime".equals(dataId))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                     filter.and(ActivityInstanceQuery.START_TIME.greaterOrEqual(startTime
                           .getTime()));

                  if (endTime != null)
                     filter.and(ActivityInstanceQuery.START_TIME.lessOrEqual(endTime
                           .getTime()));
               }
               else if ("Criticality".equals(dataId))
               {
                  CriticalityAutocompleteTableDataFilter criticalityfilter = (CriticalityAutocompleteTableDataFilter) tableDataFilter;
                  List<CriticalityAutocompleteItem> criticalityItems = criticalityfilter.getCriticalitySelector().getSelectedValues();

                  FilterOrTerm or = filter.addOrTerm();
                  for (CriticalityAutocompleteItem criticality : criticalityItems)
                  {
                     or.or(ActivityInstanceQuery.CRITICALITY.between(criticality.getRangeFromDouble(), criticality.getRangeToDouble()));
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
                     if (query instanceof ActivityInstanceQuery)
                     {
                        or.or(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(priority.getPriority()));
                     }
                  }
               }
               else if (COL_ASSIGNED_TO.equals(dataId))
               {
                  ParticipantAutocompleteTableDataFilter participantfilter = (ParticipantAutocompleteTableDataFilter) tableDataFilter;
                  List<ParticipantWrapper> participants = participantfilter.getParticipantSelector().getSelectedValues();

                  FilterOrTerm or = filter.addOrTerm();
                  for (ParticipantWrapper participant : participants)
                  {  
                     if (participant.isUser())
                     {
                        or.add(new org.eclipse.stardust.engine.api.query.PerformingUserFilter(participant.getOID()));
                     }
                     else if (participant.isRole() || participant.isOrganization())
                     {
                        or.add(org.eclipse.stardust.engine.api.query.PerformingParticipantFilter.forParticipant(participant.getParticipantInfo()));                        
                     }
                     else if (participant.isDepartment())
                     {
                        or.add(org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter.forDepartment(participant.getDeparment()));
                     }
                  }
               }
               else if (COL_COMPLETED_BY.equals(dataId))
               {
                  UserAutocompleteTableDataFilter userFilter = (UserAutocompleteTableDataFilter) tableDataFilter;
                  List<UserWrapper> users = userFilter.getUserSelector().getSelectedValues();

                  FilterOrTerm or = filter.addOrTerm();
                  for (UserWrapper user : users)
                  {
                     or.add(new org.eclipse.stardust.engine.api.query.PerformedByUserFilter(user.getUser().getOID()));
                  }
               }
               else if ("StartTime".equals(dataId))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                     filter.and(ActivityInstanceQuery.START_TIME.greaterOrEqual(startTime
                           .getTime()));

                  if (endTime != null)
                     filter.and(ActivityInstanceQuery.START_TIME.lessOrEqual(endTime
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
                           orTerm.add(ActivityInstanceQuery.STATE
                                 .isEqual(Long.parseLong(((ITableDataFilterPickList) tableDataFilter)
                                       .getSelected().get(i).toString())));
                        }
                     }
                  }
               }
               else if (this.allDescriptors.containsKey(dataId))
               {
                  applyDescriptorPolicy(query);

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
                     trace.debug("ActivityTableFilterAndSortHandler.applyFiltering(): Filtering not implemented for " + tableDataFilter);
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
   public class ActivityTableSortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = 1L;
      private Map<String, DataPath> allDescriptors;
      
      public ActivityTableSortHandler(Map<String, DataPath> allDescriptors)
      {
         super();
         this.allDescriptors = allDescriptors;
      }

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriteriaList)
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
                  applyDescriptorPolicy(query);
                  String columnName = getDescriptorColumnName(descriptorName, allDescriptors);
                  if (CommonDescriptorUtils.isStructuredData(allDescriptors.get(descriptorName)))
                  {
                     query.orderBy(new DataOrder(columnName, getXpathName(descriptorName, allDescriptors), sortCriterion.isAscending()));
                  }
                  else
                  {
                     query.orderBy(new DataOrder(columnName, sortCriterion.isAscending()));
                  }
               }
            }
            else if ("activityOID".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ActivityInstanceQuery.OID, sortCriterion.isAscending());
            }
            else if ("processOID".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ActivityInstanceQuery.PROCESS_INSTANCE_OID, sortCriterion
                     .isAscending());
            }
            else if ("criticality".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ActivityInstanceQuery.CRITICALITY, sortCriterion
                     .isAscending());
            }
            else if ("priority".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY,
                     sortCriterion.isAscending());
            }
            else if ("startTime".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ActivityInstanceQuery.START_TIME, sortCriterion
                     .isAscending());
            }
            else if ("lastModified".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ActivityInstanceQuery.LAST_MODIFICATION_TIME, sortCriterion
                     .isAscending());
            }
            else if ("activityName".equals(sortCriterion.getProperty()))
            {
               CustomOrderCriterion o = ActivityInstanceQuery.ACTIVITY_NAME
                     .ascendig(sortCriterion.isAscending());
               query.orderBy(o);
            }
            else if ("status".equals(sortCriterion.getProperty()))
            {
               query.orderBy(ActivityInstanceQuery.STATE,
                     sortCriterion.isAscending());
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
    * @param activityInstanceObj
    */
   private void performDefaultDelegation(Object activityInstanceObj)
   {
      ActivityInstance ai = (ActivityInstance) activityInstanceObj;
      List<ActivityInstance> ais = new ArrayList<ActivityInstance>();
      if (null != ai)
      {
         ais.add(ai);
      }
      else
      {
         List<ActivityInstanceWithPrioTableEntry> aisTableEntries = activityTable.getCurrentList();
         for (ActivityInstanceWithPrioTableEntry activityInstanceWithPrioTableEntry : aisTableEntries)
         {
            if (activityInstanceWithPrioTableEntry.isCheckSelection())
            {
               ais.add(activityInstanceWithPrioTableEntry.getActivityInstance());
            }
         }
      }
      if (CollectionUtils.isNotEmpty(ais))
      {
         boolean isCaseActivities = ActivityInstanceUtils.isContainsCaseActivity(ais);
         if (isCaseActivities)
         {
            MessageDialog.addErrorMessage(MessagesViewsCommonBean.getInstance().getString(
                  "views.switchProcessDialog.caseAbort.message"));
            return;
         }
         
         ActivityInstanceUtils.delegateToDefaultPerformer(ais);
         getCallbackHandler().handleEvent(EventType.APPLY);
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
            FacesUtils.refreshPage();
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
            getActivityTable().refresh(true);
         }
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   private class ActivityTableExportHandler implements DataTableExportHandler<ActivityInstanceWithPrioTableEntry>
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object, java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column,
            ActivityInstanceWithPrioTableEntry row, Object value)
      {
         if (COL_ACTIVITY_NAME.equals(column.getColumnName()))
         {
            return row.getActivityName();
         }
         else if ("Priority".equals(column.getColumnName()))
         {
            return ProcessInstanceUtils.getPriorityLabel(row.getPriority());
         }
         else if (DESCRIPTOR_COL_NAME.equals(column.getColumnName()))
         {
            return DescriptorColumnUtils.exportDescriptors(row.getProcessDescriptorsList(),
                  ExportType.EXCEL == exportType ? "\n" : ", ");
         }
         else if ("Criticality".equals(column.getColumnName()))
         {
            return CriticalityConfigurationUtil.getCriticalityDisplayLabel(row.getCriticalityValue(), row.getCriticality());
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

   private static String getDescriptorColumnName(String descriptorName,
         Map<String, DataPath> descriptorNameAndDataPathMap)
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
   public void toggleSwitchProcessPanel()
   {
      switchPanelDisplayOn = !switchPanelDisplayOn;
   }

   public boolean isSwitchPanelDisplayOn()
   {
      return switchPanelDisplayOn;
   }

   public void setSwitchPanelDisplayOn(boolean switchPanelDisplayOn)
   {
      this.switchPanelDisplayOn = switchPanelDisplayOn;
   }
   
   /**
    * action listener to open Switch process
    */
   public void openSwitchProcess(ActionEvent event)
   {
      try
      {
         // For mandatory single row select i.e Activity Table Actions Column
         ActivityInstance activityInstance = (ActivityInstance) event.getComponent().getAttributes()
               .get("activityInstance");
         SwitchProcessDialogBean dialog = SwitchProcessDialogBean.getInstance();

         if (null != activityInstance)
         {
            List<ProcessInstance> sourceList = CollectionUtils.newArrayList();
            sourceList.add(activityInstance.getProcessInstance());
            dialog.setSourceProcessInstances(sourceList);
         }
         else
         {
            // For multiple row select i.e Activity table toolbar
            List<ProcessInstance> processInstanceList = getSelectedProcesses();
            dialog.setSourceProcessInstances(processInstanceList);
         }
         dialog.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }
   
   /**
    * 
    * @return
    */
   public boolean isEnableJoinProcess()
   {
      if (hasJoinProcessPermission && !ActivityInstanceUtils.isContainsCaseActivity(getSelectedActivities()))
      {
         return true;
      }
      return false;
   }

   /**
    * 
    * @return
    */
   public boolean isEnableSwitchProcess()
   {
      if (hasSwitchProcessPermission && !ActivityInstanceUtils.isContainsCaseActivity(getSelectedActivities()))
      {
         return true;
      }
      return false;
   }
   
   /**
    * action listener to open Join process
    */
   public void openJoinProcess(ActionEvent event)
   {
      try
      {
         // For mandatory single row select i.e Activity Table Actions Column
         ActivityInstance activityInstance = (ActivityInstance) event.getComponent().getAttributes()
               .get("activityInstance");
         JoinProcessDialogBean dialog = JoinProcessDialogBean.getInstance();
         ProcessInstance processInstance = null;
         if (null == activityInstance)
         {
            processInstance = getSelectedProcesses().get(0);
         }
         else
         {
            processInstance = activityInstance.getProcessInstance();
         }
         if (null != processInstance)
         {
            dialog.setSourceProcessInstance(processInstance);
            dialog.openPopup();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * 
    * @return
    */
   private List<ProcessInstance> getSelectedProcesses()
   {
      List<ProcessInstance> processInstanceList = CollectionUtils.newArrayList();
      List<ActivityInstanceWithPrioTableEntry> ait = activityTable.getCurrentList();
      for (ActivityInstanceWithPrioTableEntry at : ait)
      {
         if (at.isCheckSelection())
         {
            ProcessInstance procInst = at.getActivityInstance().getProcessInstance();
            processInstanceList.add(procInst);
         }
      }
      return processInstanceList;
   }
   
   /**
    * 
    * @return
    */
   private List<ActivityInstance> getSelectedActivities()
   {
      List<ActivityInstance> processInstanceList = CollectionUtils.newArrayList();
      List<ActivityInstanceWithPrioTableEntry> ait = activityTable.getCurrentList();
      for (ActivityInstanceWithPrioTableEntry at:ait)
      {         
         if (at.isCheckSelection())
         {
            ActivityInstance ai = at.getActivityInstance();
            processInstanceList.add(ai);
         }
      }
      return processInstanceList;
   }

   public ActivityInstanceWithPrioTableEntry createUserObject(Object resultRow)
   {
      try
      {
         ActivityInstanceWithPrio row = null;
         if (resultRow instanceof ActivityInstanceWithPrio)
         {
            row = (ActivityInstanceWithPrio) resultRow;
         }
         else if (resultRow instanceof ActivityInstance)
         {
            ActivityInstance ai = (ActivityInstance) resultRow;
            ProcessInstance processInstance = null;

            if (CollectionUtils.isNotEmpty(processInstanceMap))
            {
               processInstance = processInstanceMap.get(ai.getProcessInstanceOID());
            }

            row = null != processInstance
                  ? new ActivityInstanceWithPrio(ai, processInstance)
                  : new ActivityInstanceWithPrio(ai);

         }

         return new ActivityInstanceWithPrioTableEntry(row, true);
      }
      catch (Exception e)
      {
         trace.error(e);
         ActivityInstanceWithPrioTableEntry activityInstanceWithPrioTableEntry = new ActivityInstanceWithPrioTableEntry();
         activityInstanceWithPrioTableEntry.setLoaded(false);
         activityInstanceWithPrioTableEntry.setCause(e);
         return activityInstanceWithPrioTableEntry;
      }
   }

   public Map<Long, ProcessInstance> getProcessInstanceMap()
   {
      return processInstanceMap;
   }

   public void setProcessInstanceMap(Map<Long, ProcessInstance> processInstanceMap)
   {
      this.processInstanceMap = processInstanceMap;
   }

   public ColumnModelListener getColumnModelListener()
   {
      return columnModelListener;
   }  
   
   
   
}
