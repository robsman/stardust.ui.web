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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalProcessingTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.DateRange;
import org.eclipse.stardust.engine.core.query.statistics.api.StatisticsDateRangePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.WorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.bcc.jsf.ProcessingTimePerProcess;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterListener;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

import com.google.gson.JsonObject;

/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ResourcePerformanceBean extends UIComponentBean implements ResourcePaths, ViewEventHandler, ITableDataFilterListener
{
   private static final long serialVersionUID = 1L;
   private final static String CUSTOM_COL_PREFIX = "customColumns.";
   private ModelParticipantInfo selectedModelParticipant;
   private List<ProcessingTimeTableEntry> userStatistics;
   private SortableTable<ProcessingTimeTableEntry> statisticsTable;
   private SelectItem[] roleSelectItem;
   private List<RoleItem> allRoles;
   private List<ColumnPreference> selFixedCols;
   private String selectedComponent;
   private Map<String, Object> columnDefinitionMap;
   private Map<String, DateRange> customColumnDateRange;
   private List<ColumnPreference> userSelectableCols;
   private List<ColumnPreference> partitionSelectableCols;
   private UserPreferencesHelper userPrefsHelper;
   int index;

   /**
    * 
    */
   public ResourcePerformanceBean()
   {
      super(V_resourcePerformanceView);

   }

   private void createTable()
   {
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      List<ColumnPreference> selCols = new ArrayList<ColumnPreference>();
      ColumnPreference pDefinitionCol = new ColumnPreference("ProcessDefinition", "processDefinitionId",
            ColumnDataType.STRING, this.getMessages().getString("column.processDefinition"), new TableDataFilterPopup(
                  new TableDataFilterSearch()));

      fixedCols.add(pDefinitionCol);

      ColumnPreference colToday = new ColumnPreference("Today", this.getMessages().getString("column.today"));
      ColumnPreference todayTimeCol = new ColumnPreference("TodayTime", "averageTimeToday", ColumnDataType.STRING, this
            .getMessages().getString("column.processingTime"), null, true, false);
      todayTimeCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference todayStatusCol = new ColumnPreference("TodayStatus", "todayStatusLabel", this.getMessages()
            .getString("column.status"), V_resourcePerformanceColumns, true, false);
      todayStatusCol.setColumnAlignment(ColumnAlignment.CENTER);

      colToday.addChildren(todayTimeCol);
      colToday.addChildren(todayStatusCol);
      selFixedCols.add(colToday);

      ColumnPreference colWeek = new ColumnPreference("Week", this.getMessages().getString("column.lastWeek"));
      ColumnPreference weekTimeCol = new ColumnPreference("WeekTime", "averageTimeLastWeek", ColumnDataType.STRING,
            this.getMessages().getString("column.processingTime"), null, true, false);
      weekTimeCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference weekStatusCol = new ColumnPreference("WeekStatus", "lastWeekStatusLabel", this.getMessages()
            .getString("column.status"), V_resourcePerformanceColumns, true, false);
      weekStatusCol.setColumnAlignment(ColumnAlignment.CENTER);

      colWeek.addChildren(weekTimeCol);
      colWeek.addChildren(weekStatusCol);
      selFixedCols.add(colWeek);

      ColumnPreference colMonth = new ColumnPreference("Month", this.getMessages().getString("column.lastMonth"));
      ColumnPreference monthTimeCol = new ColumnPreference("MonthTime", "averageTimeLastMonth", ColumnDataType.STRING,
            this.getMessages().getString("column.processingTime"), null, true, false);
      monthTimeCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference monthStatusCol = new ColumnPreference("MonthStatus", "lastMonthStatusLabel", this.getMessages()
            .getString("column.status"), V_resourcePerformanceColumns, true, false);
      monthStatusCol.setColumnAlignment(ColumnAlignment.CENTER);

      colMonth.addChildren(monthTimeCol);
      colMonth.addChildren(monthStatusCol);

      selFixedCols.add(colMonth);
      // Store user Selectable columns[fixed + custom columns], read while ColumnSelector toggle 
      userSelectableCols = addCustomColsFromPreference(PreferenceScope.USER, null);
      
      List<String> custPartitionReadOnlyCols = CollectionUtils.newArrayList();
      // Read 'selectedColumn' pref key to find columns available to user, but created by
      // Admin for ALL[at PARTITION]
      UserPreferencesHelper prefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_BCC, PreferenceScope.USER);
      List<String> viewColumns = prefsHelper.getSelectedColumns(UserPreferencesEntries.V_RESOURCE_PERFORMANCE);
      
      // By default nothing is stored in selectedColumns pref key for this view
      if(!CollectionUtils.isEmpty(viewColumns))
      {
         for (String cols : viewColumns)
         {
            if (!cols.equals(colToday.getColumnName()) && !cols.equals(colMonth.getColumnName())
                  && !cols.equals(colWeek.getColumnName()))
            {
               // Custom Columns created by admin at PARTITION, should be visible as
               // read-only to others
               if (!columnDefinitionMap.containsKey(cols))
               {
                  custPartitionReadOnlyCols.add(cols);
               }
            }
         }    
      }
      
      partitionSelectableCols = addCustomColsFromPreference(PreferenceScope.PARTITION, custPartitionReadOnlyCols);
      
      selCols.addAll(userSelectableCols);
      
      IColumnModel bccPerformanceColumnModel = new ResourcePerformanceColumnModel(selCols, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_RESOURCE_PERFORMANCE);

      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(bccPerformanceColumnModel);
      statisticsTable = new SortableTable<ProcessingTimeTableEntry>(colSelecpopup, null,
            new SortableTableComparator<ProcessingTimeTableEntry>("processDefinitionId", true));
      // Add filter for all custom columns
      this.setCustomColumnFilters(bccPerformanceColumnModel, columnDefinitionMap, null);
      userStatistics = createUserStatistics();
      statisticsTable.setList(userStatistics);
      statisticsTable.initialize();
   }

   /**
    * Returns a list of Fixed Cols and custom columns from preference store
    * 
    * @param prefScope
    * @param customReadOnlyCols
    * @return
    */
   private List<ColumnPreference> addCustomColsFromPreference(PreferenceScope prefScope, List<String> customReadOnlyCols)
   {
      UserPreferencesHelper prefHelper = UserPreferencesHelper.getInstance(
            UserPreferencesEntries.M_BCC, prefScope);
      
      List<String> viewColumns = prefHelper.getSelectedColumns(UserPreferencesEntries.V_RESOURCE_PERFORMANCE);
      List<String> allCols = getCustomColumnsPreference(prefHelper);
      List<ColumnPreference> selectableCols = CollectionUtils.newArrayList();
      if(CollectionUtils.isEmpty(viewColumns))
      {
         selectableCols.addAll(selFixedCols);
      }
      else
      {
         for (ColumnPreference column : selFixedCols)
         {
            if(viewColumns.contains(column.getColumnName()))
            {
               column.setVisible(true);
            }
            else
            {
               column.setVisible(false);
            }
            selectableCols.add(column);
         }   
      }
      
      if(!CollectionUtils.isEmpty(allCols))
      {
         for (String col : allCols)
         {
            if (col.contains("#{"))
            {
               String[] columnDef = col.split("#");
               JsonObject columnDefinition = GsonUtils.readJsonObject(columnDef[1]);
               columnDefinition.addProperty("readOnly", false);
               ColumnPreference customColumn = createColumnPreferenceFromJSON(columnDefinition);
               // For ResourcePerformace view, default no preference are stored, added
               // check if prefCols list[viewColumns] is not empty
               if (!CollectionUtils.isEmpty(viewColumns) && !viewColumns.contains(customColumn.getColumnName()))
               {
                  customColumn.setVisible(false);
               }
               Long userOID = columnDefinition.get("userOID").getAsLong();
               if (userOID != SessionContext.findSessionContext().getUser().getOID())
               {
                  columnDefinition.addProperty("readOnly", true);
               }
               else
               {
                  columnDefinition.addProperty("readOnly", false);
               }
               if (customReadOnlyCols != null && customReadOnlyCols.contains(columnDef[0]))
               {
                  // Custom columns available in Partition, are shown in User Column
                  // Selector as READ-ONLY
                  userSelectableCols.add(customColumn);
               }
               selectableCols.add(customColumn);
            }
         }   
      }
      
      return selectableCols;
   }
   
   public void addNewColumn()
   {
      JsonObject columnDefinition = null;
      List<ColumnPreference> selCols = statisticsTable.getColumnModel().getSelectableColumns();
      if (index == 0)
      {
         index++;
      }
      String columnTitle = MessagesBCCBean.getInstance().get("views.costs.column.customColumn.label") + index;
      long userOrPartitionOID = statisticsTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER
            ? SessionContext.findSessionContext().getUser().getOID()
            : SessionContext.findSessionContext().getUser().getPartitionOID();
      String columnId = MessagesBCCBean.getInstance().get("views.costs.column.customColumn.name") + userOrPartitionOID
            + index++;
      // Creates JSON object storing columnDefinition with values(columnId,columnName,duration..)
      columnDefinition = CustomColumnUtils.updateCustomColumnJson(columnId, columnTitle, 0, CustomColumnUtils.DAY_TYPE, 0, CustomColumnUtils.DAY_TYPE, columnDefinition,
            customColumnDateRange);
      columnDefinition.addProperty("userOID", SessionContext.findSessionContext().getUser().getOID());
      columnDefinition.addProperty("readOnly", false);
      // Create column preference using json
      ColumnPreference customColumn = createColumnPreferenceFromJSON(columnDefinition);
      selCols.add(customColumn);
      // Update the User and Partition column list- used while columnSelector toggle
      if(statisticsTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER)
      {
         userSelectableCols =  selCols;
      }
      else
      {
         partitionSelectableCols =  selCols;
      }
      // Update table
      IColumnModel model = statisticsTable.getColumnModel();
      model.setSelectableColumns(selCols);

      TableColumnSelectorPopup columnSelectorPopup = statisticsTable.getColumnSelectorPopup() != null ? statisticsTable
            .getColumnSelectorPopup() : new TableColumnSelectorPopup(model);
      
      List<String> allColumns = getCustomColumnsPreference();
      if(CollectionUtils.isEmpty(allColumns))
      {
         allColumns = CollectionUtils.newArrayList();
      }
      allColumns.add(columnId + "#" + columnDefinition.toString());
      // Update preference store 'allColumns' with new columnDef JSON [columnId#colDefJSON]
      userPrefsHelper.setString(UserPreferencesEntries.V_RESOURCE_PERFORMANCE,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS, allColumns);
      // Save selectable columns with columnId added to fixed columns
      model.saveSelectableColumns(columnSelectorPopup.getSelectedPreferenceScope());
      
      TableDataFilterPopup filterPopup = this.setCustomColumnFilters(model, columnDefinitionMap, columnId);
      model.setDefaultSelectableColumns(selCols);
      update();
      filterPopup.openPopup();
      PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView;");

   }
   
   /**
    * 
    * @param columnDefinition
    * @return
    */
   private ColumnPreference createColumnPreferenceFromJSON(JsonObject columnDefinition)
   {
      ColumnPreference customColumn = null;
      String columnId = GsonUtils.extractString(columnDefinition, "columnId");
      String columnTitle = GsonUtils.extractString(columnDefinition, "columnTitle");
      customColumn = new ColumnPreference(columnId, CUSTOM_COL_PREFIX + columnId, ColumnDataType.STRING, columnTitle,
            true, false);
      customColumn.setColumnAlignment(ColumnAlignment.RIGHT);

      ColumnPreference colTime = new ColumnPreference(CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX, CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX, this.getMessages().getString("column.processingTime"),
            V_resourcePerformanceColumns, true, true);
      colTime.setColumnAlignment(ColumnAlignment.RIGHT);

      ColumnPreference colStatus = new ColumnPreference(CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX, CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX, this.getMessages().getString("column.status"),
            V_resourcePerformanceColumns, true, false);
      colStatus.setColumnAlignment(ColumnAlignment.CENTER);

      customColumn.addChildren(colTime);
      customColumn.addChildren(colStatus);

      columnDefinitionMap.put(columnId, columnDefinition);
      index = Integer.valueOf(columnId.substring(columnId.length() - 1)) + 1;

      CustomColumnUtils.updateCustomColumnDateRange(columnDefinition, customColumnDateRange);

      return customColumn;
   }
   
   /**
    * 
    * @param columnModel
    * @param customColumns
    * @param currentColumn
    * @return
    */
   public TableDataFilterPopup setCustomColumnFilters(IColumnModel columnModel, Map<String, Object> customColumns, String currentColumn)
   {
      TableDataFilterPopup dataFilterPopup = null;
      for (ColumnPreference colPref : columnModel.getSelectableColumns())
      {
         if (colPref.getColumnProperty().startsWith(CUSTOM_COL_PREFIX))
         {
            String property = colPref.getColumnProperty();
            if (property.indexOf(CUSTOM_COL_PREFIX) != -1)
            {
               String columnId = property.substring(property.indexOf(".") + 1);
               if(customColumns.get(columnId)!=null)
               {
                  if(currentColumn == null || (!StringUtils.isEmpty(currentColumn) && currentColumn.equals(currentColumn)))
                  {
                     Boolean readOnly =  ((JsonObject)customColumns.get(columnId)).get("readOnly").getAsBoolean();
                     if(!readOnly)
                     {
                        ResourcePerformanceDataFilter filter = new ResourcePerformanceDataFilter("", "", "", false);
                        filter.updateFilterFields(customColumns.get(columnId));
                        dataFilterPopup = new TableDataFilterPopup(MessagesBCCBean.getInstance().get("views.costs.column.customColumn.editLabel"),
                              new TableDataFilters(filter), this);
                        dataFilterPopup.setResetTitle(MessagePropertiesBean.getInstance().get("common.filterPopup.delete"));
                        colPref.setColumnDataFilterPopup(dataFilterPopup);   
                     }
                  }
               }
            }
         }
      }
      return dataFilterPopup;
      
   }


   public void applyFilter(TableDataFilters tableDataFilters)
   {
      ResourcePerformanceDataFilter filter = (ResourcePerformanceDataFilter) tableDataFilters.getList().get(0);
      if (filter == null || filter.getColumnId() == null)
      {
         return;
      }
      String columnId = filter.getColumnId();
      String columnTitle = filter.getColumnTitle();
      String startDateNumDays = filter.getStartDateNumDays();
      Integer startDateType = Integer.valueOf(filter.getStartDateType());
      String durationNumDays = filter.getDurationNumDays();
      Integer durationType = Integer.valueOf(filter.getDurationType());
      // Update Column JSON with new values from filterPopup
      JsonObject columnDefinition = (JsonObject) columnDefinitionMap.get(filter.getColumnId());
      columnDefinition = CustomColumnUtils.updateCustomColumnJson(columnId, columnTitle, Integer.valueOf(startDateNumDays),
            startDateType, Integer.valueOf(durationNumDays), durationType, columnDefinition, customColumnDateRange);
      
      List<String> allColumns = getCustomColumnsPreference();
      if(CollectionUtils.isEmpty(allColumns) && columnDefinitionMap.get(filter.getColumnId()) !=null)
      {
         allColumns = new ArrayList<String>();
         String newValue = filter.getColumnId() + "#" + columnDefinition;
         allColumns.add(newValue);
      }
      else
      {
         Iterator<String> colIterator = allColumns.iterator();
         
         while (colIterator.hasNext())
         {
            String cols = colIterator.next();
            //Update the custom column preference with new JSON
            if (cols.startsWith(columnId))
            {
               String newValue = columnId + "#" + columnDefinition;
               allColumns.set(allColumns.indexOf(cols), newValue);
               columnDefinitionMap.put(columnId, columnDefinition);
               break;
            }
         }
      }
      
      // save 'allColumns' preferenceStore
      userPrefsHelper.setString(UserPreferencesEntries.V_RESOURCE_PERFORMANCE,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS, allColumns);
      // Update the column Title
      List<ColumnPreference> selCols = statisticsTable.getColumnModel().getSelectableColumns();
      Iterator<ColumnPreference> iCol = selCols.iterator();
      while (iCol.hasNext())
      {
         ColumnPreference col = iCol.next();
         if (col.getColumnName().equals(columnId))
         {
            col.setColumnTitle(columnTitle);
            break;
         }
      }
      //Update user and partition columns list, read while column selector toggle
      if(statisticsTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER)
      {
         userSelectableCols = selCols;
      }
      else
      {
         partitionSelectableCols = selCols;
      }
      update();
      PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView;");
      
   }
   
   /**
    * 
    * @param resourceDataFilter
    */
   public void deleteFilter(ResourcePerformanceDataFilter resourceDataFilter)
   {
      List<String> allColumns = getCustomColumnsPreference();
      Iterator<String> colIterator = allColumns.iterator();
      
      String columnId = resourceDataFilter.getColumnId();
      while (colIterator.hasNext())
      {
         String cols = colIterator.next();
         if (cols.startsWith(columnId))
         {
            allColumns.remove(allColumns.indexOf(cols));
            break;
         }
      }
      userPrefsHelper.setString(UserPreferencesEntries.V_RESOURCE_PERFORMANCE,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS, allColumns);
      
      List<ColumnPreference> selCols = statisticsTable.getColumnModel().getSelectableColumns();
      Iterator<ColumnPreference> iCol = selCols.iterator();

      while (iCol.hasNext())
      {
         ColumnPreference col = iCol.next();
         if (col.getColumnName().equals(columnId))
         {
            selCols.remove(col);
            break;
         }
      }
      if(statisticsTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER)
      {
         userSelectableCols = selCols;
      }
      else
      {
         partitionSelectableCols = selCols;
      }
      columnDefinitionMap.remove(columnId);
      customColumnDateRange.remove(columnId);
      statisticsTable.getColumnModel().setDefaultSelectableColumns(selCols);
      statisticsTable.getColumnModel().saveSelectableColumns(statisticsTable.getColumnSelectorPopup().getSelectedPreferenceScope());
      statisticsTable.initialize();
      PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView;");
   }
   
   /**
    * Read custom column 'allColumns' key from Preference Store
    * 
    * @return
    */
   private List<String> getCustomColumnsPreference()
   {
      TableColumnSelectorPopup columnSelPopup = statisticsTable != null
            ? statisticsTable.getColumnSelectorPopup()
            : null;
      if (UserUtils.isUserAdmin(SessionContext.findSessionContext().getUser()))
      {
         PreferenceScope currentPrefScope = columnSelPopup != null
               ? columnSelPopup.getSelectedPreferenceScope()
               : PreferenceScope.USER;
         userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_BCC, currentPrefScope);
      }
      else
      {
         userPrefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_BCC, PreferenceScope.USER);
      }
      return getCustomColumnsPreference(userPrefsHelper);
   }

   private List<String> getCustomColumnsPreference(UserPreferencesHelper userPreferenceHelper)
   {
      return userPreferenceHelper.getString(UserPreferencesEntries.V_RESOURCE_PERFORMANCE,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS);
   }
   
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         selectedComponent = null;
         allRoles = WorkflowFacade.getWorkflowFacade().getAllRolesExceptCasePerformer();
         selectedModelParticipant = allRoles.isEmpty() ? null : allRoles.get(0).getRole();
         initAllParticipants();
         initialize();

      }
   }

   /**
    * Refresh the table
    */
   public void update()
   {
      userStatistics = createUserStatistics();
      statisticsTable.setList(userStatistics);
      statisticsTable.initialize();
   }

   /**
    * Updates UserStatistics as per selected Participant
    * 
    * @param evt
    *           which fires ValueChangeListener event
    * 
    */
   public void updateUserStatistics(ValueChangeEvent evt)
   {
      if (!evt.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         evt.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         evt.queue();
         return;
      }

      if (evt.getNewValue() != null)
      {
         String selectedItem = evt.getNewValue().toString();
         if (selectedItem != null && getRoleSelectItem() != null)
         {
            for (RoleItem item : allRoles)
            {
               String key = ParticipantUtils.getParticipantUniqueKey(item.getRole());
               if (key.equals(selectedItem))
               {
                  setSelectedModelParticipant(item.getRole());
                  break;
               }
            }
            userStatistics = createUserStatistics();
            statisticsTable.setList(userStatistics);
         }

      }

   }

   public UserWorktimeStatistics getWorktimeStatistics()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserWorktimeStatisticsQuery wsQuery = UserWorktimeStatisticsQuery.forAllUsers();
      wsQuery.setPolicy(CriticalProcessingTimePolicy.criticalityByDuration(BusinessControlCenterConstants
            .getProcessingTimeThreshold(BusinessControlCenterConstants.YELLOW_THRESHOLD, 1.0f),
            BusinessControlCenterConstants.getProcessingTimeThreshold(BusinessControlCenterConstants.RED_THRESHOLD,
                  1.0f)));

      List<DateRange> dateRange = CollectionUtils.newArrayList();
      dateRange.add(DateRange.TODAY);
      dateRange.add(DateRange.LAST_WEEK);
      dateRange.add(DateRange.LAST_MONTH);
      for (Map.Entry<String, DateRange> custCols : customColumnDateRange.entrySet())
      {
         String key = custCols.getKey();
         DateRange range = custCols.getValue();
         dateRange.add(range);
      }
      if (!dateRange.isEmpty())
      {
         wsQuery.setPolicy(new StatisticsDateRangePolicy(dateRange));
      }
      UserWorktimeStatistics stat = (UserWorktimeStatistics) facade.getAllUsers(wsQuery);
      return stat;
   }

   /**
    * returns the list of User statistics
    * 
    * @return list
    */
   private List<ProcessingTimeTableEntry> createUserStatistics()
   {
      UserWorktimeStatistics stat = getWorktimeStatistics();

      userStatistics = new ArrayList<ProcessingTimeTableEntry>();
      List<ProcessingTimePerProcess> tableData = new ArrayList<ProcessingTimePerProcess>();
      Iterator<ProcessDefinition> pIter = ProcessDefinitionUtils.getAllBusinessRelevantProcesses().iterator();
      ProcessingTimePerProcess ptp = null;
      while (pIter.hasNext())
      {
         ProcessDefinition pd = pIter.next();
         ptp = new ProcessingTimePerProcess(pd, columnDefinitionMap);
         tableData.add(ptp);
      }

      if (stat != null && selectedModelParticipant != null)
      {
         Set<Long> ids = stat.getAvailableUserOids();
         for (Long userOID : ids)
         {

            WorktimeStatistics wStat = stat.getWorktimeStatistics(userOID);
            if (wStat != null)
            {
               for (ProcessingTimePerProcess cpp : tableData)
               {
                  Contribution con = wStat.findContribution(cpp.getProcessDefinition().getQualifiedId(),
                        selectedModelParticipant);
                  cpp.addContribution(con, customColumnDateRange);

               }
            }
         }
      }
      for (ProcessingTimePerProcess cpp : tableData)
      {
         userStatistics.add(new ProcessingTimeTableEntry(I18nUtils.getProcessName(cpp.getProcessDefinition()), cpp
               .getAverageTimeToday(), cpp.getAverageTimeLastWeek(), cpp.getAverageTimeLastMonth(),
               cpp.getTodayState(), cpp.getLastMonthState(), cpp.getLastMonthState(), cpp.getCustomColumns()));

      }

      return userStatistics;
   }

   /**
    * 
    */
   private void initAllParticipants()
   {
      roleSelectItem = new SelectItem[allRoles.size()];

      Collections.sort(allRoles, new Comparator<RoleItem>()
      {
         public int compare(RoleItem r1, RoleItem r2)
         {
            return r1.getRoleName().compareTo(r2.getRoleName());
         }

      });

      RoleItem roleItem = null;
      for (int j = 0; j < allRoles.size(); j++)
      {
         roleItem = (RoleItem) allRoles.get(j);
         roleSelectItem[j] = new SelectItem(ParticipantUtils.getParticipantUniqueKey(roleItem.getRole()),
               roleItem.getRoleName());
      }
   }
   
   /**
    * @return AllModelParticipants
    */
   public SelectItem[] getAllModelParticipants()
   {
      return roleSelectItem;
   }

   // **************** Modified setter method***********
   public void setSelectedModelParticipant(ModelParticipantInfo selectedModelParticipant)
   {
      // object reference check is required here instead of equals()
      if (selectedModelParticipant == null
            || (selectedModelParticipant != null && selectedModelParticipant != this.selectedModelParticipant))
      {
         this.selectedModelParticipant = selectedModelParticipant;
      }

   }

   // ********** Default getter & setter methods *******

   public List<ProcessingTimeTableEntry> getUserStatistics()
   {
      return userStatistics;
   }

   public void setUserStatistics(List<ProcessingTimeTableEntry> userStatistics)
   {
      this.userStatistics = userStatistics;
   }

   public DataTable<ProcessingTimeTableEntry> getStatisticsTable()
   {
      return statisticsTable;
   }

   public ModelParticipantInfo getSelectedModelParticipant()
   {
      return selectedModelParticipant;
   }

   @Override
   public void initialize()
   {
      selFixedCols = CollectionUtils.newArrayList();
      columnDefinitionMap = CollectionUtils.newHashMap();
      customColumnDateRange = CollectionUtils.newHashMap();
      createTable();
   }

   public SelectItem[] getRoleSelectItem()
   {
      return roleSelectItem;
   }

   public void setRoleSelectItem(SelectItem[] roleSelectItem)
   {
      this.roleSelectItem = roleSelectItem;
   }

   public String getSelectedComponent()
   {
      return selectedComponent;
   }

   public void setSelectedComponent(String selectedComponent)
   {
      this.selectedComponent = selectedComponent;
   }
   
   class ResourcePerformanceColumnModel extends DefaultColumnModel
   {

      private static final long serialVersionUID = 3257355871815472018L;
      
      /**
       * @param columns
       * @param fixedBeforeColumns
       * @param fixedAfterColumns
       * @param moduleId
       * @param viewId
       */
      public ResourcePerformanceColumnModel(List<ColumnPreference> columns,
            List<ColumnPreference> fixedBeforeColumns,
            List<ColumnPreference> fixedAfterColumns, String moduleId, String viewId)
      {
         super(columns, fixedBeforeColumns, fixedAfterColumns, moduleId, viewId, null);
      }
      
      @Override
      public List<ColumnPreference> getSelectableColumnsForPreferenceScope(PreferenceScope prefScope)
      {
         List<ColumnPreference> selectableCols = CollectionUtils.newArrayList();
         UserPreferencesHelper prefHelper = UserPreferencesHelper.getInstance(
               UserPreferencesEntries.M_BCC, prefScope);
         List<String> viewColumns = prefHelper.getSelectedColumns(UserPreferencesEntries.V_RESOURCE_PERFORMANCE);
         // By default nothing is stored for key 'selectedColumns' at pref store for
         // current view 
         if (!CollectionUtils.isEmpty(viewColumns))
         {
            for (ColumnPreference column : prefScope == PreferenceScope.USER
                  ? userSelectableCols
                  : partitionSelectableCols)
            {
               if (viewColumns.contains(column.getColumnName()))
               {
                  column.setVisible(true);
               }
               else
               {
                  column.setVisible(false);
               }
            }
         }
         
         if(prefScope == PreferenceScope.USER)
         {
            selectableCols = userSelectableCols;
         }
         else
         {
            selectableCols = partitionSelectableCols;            
         }
         setDefaultSelectableColumns(selectableCols);
         setCustomColumnFilters(this, columnDefinitionMap,null);
         return selectableCols;
      }
   }
}
