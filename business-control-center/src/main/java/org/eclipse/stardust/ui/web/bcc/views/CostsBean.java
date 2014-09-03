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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalCostPerExecutionPolicy;
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
import org.eclipse.stardust.ui.web.bcc.jsf.CostsPerProcess;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
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
 * @author Giridhara.G
 * @version
 */
public class CostsBean extends UIComponentBean implements ResourcePaths,ViewEventHandler,ITableDataFilterListener
{
   private static final long serialVersionUID = 1L;
   
   protected final static String MODEL_PARTICIPANT = "carnotBcCosts/selectedModelParticipant";
   
   private final static int MAX_FRACTION_DIGITS = 2;

   private ModelParticipantInfo selectedModelParticipant;
   
   private SortableTable<CostTableEntry> costTable;

   private List<SelectItem> tempAllParticipantsList;

   private String currencyCode = null;

   private MessagesBCCBean propsBean;

   private SelectItem[] roleSelectItem;

   private String selectedComponent;
   
   private List<ColumnPreference> selFixedCols;
   
   private List<ColumnPreference> userSelectableCols;
   
   private List<ColumnPreference> partitionSelectableCols;
   
   private Map<String, Object> columnDefinitionMap;
   
   private Map<String, DateRange> customColumnDateRange;
   
   private UserPreferencesHelper userPrefsHelper;
   
   private int index;
   /**
    * Constructor CostsBean
    */
   public CostsBean()
   {
      super(V_costsView);
   }
   
   private void createTable(){
      
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      List<ColumnPreference> selCols = new ArrayList<ColumnPreference>();
      
      ColumnPreference colProcessDefinition = new ColumnPreference("ProcessDefinition",
            "processDefinition", ColumnDataType.STRING, propsBean
                  .getString("views.resourcePerformance.column.processDefinition"),
            new TableDataFilterPopup(new TableDataFilterSearch()));

      ColumnPreference colTodayCostGrp = new ColumnPreference("CostToday", this
            .getMessages().getString("column.today"));

      ColumnPreference colTodayCost = new ColumnPreference("TodayCost", "todayCosts",
            ColumnDataType.STRING, this.getMessages().getString("column.cost"));
      colTodayCost.setColumnAlignment(ColumnAlignment.RIGHT);

      ColumnPreference colTodayStatus = new ColumnPreference("TodayStatus", "todayStatusLabel", this
            .getMessages().getString("column.status"), V_costControllingColumns, true,
            false);
      colTodayStatus.setColumnAlignment(ColumnAlignment.CENTER);

      colTodayCostGrp.addChildren(colTodayCost);
      colTodayCostGrp.addChildren(colTodayStatus);

      ColumnPreference colLastWeekGrp = new ColumnPreference("LastWeek", this
            .getMessages().getString("column.lastWeek"));

      ColumnPreference colWeekCost = new ColumnPreference("WeekCost", "lastWeekCosts",
            ColumnDataType.STRING, this.getMessages().getString("column.cost"));
      colWeekCost.setColumnAlignment(ColumnAlignment.RIGHT);

      ColumnPreference colLastWeekStatus = new ColumnPreference("LastWeekStatus", "lastWeekStatusLabel",
            this.getMessages().getString("column.status"), V_costControllingColumns,
            true, false);
      colLastWeekStatus.setColumnAlignment(ColumnAlignment.CENTER);

      colLastWeekGrp.addChildren(colWeekCost);
      colLastWeekGrp.addChildren(colLastWeekStatus);

      ColumnPreference colLastMonthGrp = new ColumnPreference("LastMonth", this
            .getMessages().getString("column.lastMonth"));

      ColumnPreference colMonthCost = new ColumnPreference("MonthCost", "lastMonthCosts",
            ColumnDataType.STRING, this.getMessages().getString("column.cost"));
      colMonthCost.setColumnAlignment(ColumnAlignment.RIGHT);

      ColumnPreference colLastMonthStatus = new ColumnPreference("LastMonthStatus", "lastMonthStatusLabel",
            this.getMessages().getString("column.status"), V_costControllingColumns,
            true, false);
      colLastMonthStatus.setColumnAlignment(ColumnAlignment.CENTER);

      colLastMonthGrp.addChildren(colMonthCost);
      colLastMonthGrp.addChildren(colLastMonthStatus);

      fixedCols.add(colProcessDefinition);
      selFixedCols.add(colTodayCostGrp);
      selFixedCols.add(colLastWeekGrp);
      selFixedCols.add(colLastMonthGrp);
      
      // Store user Selectable columns[fixed + custom columns], read while ColumnSelector toggle
      userSelectableCols = addCustomColsFromPreference(PreferenceScope.USER, null);
      
      List<String> custPartitionReadOnlyCols = CollectionUtils.newArrayList(); 
      UserPreferencesHelper prefsHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_BCC, PreferenceScope.USER);
      List<String> viewColumns = prefsHelper.getSelectedColumns(UserPreferencesEntries.V_COST_AND_CONTROLLING);
      for (String cols : viewColumns)
      {
         if (!cols.equals(colTodayCostGrp.getColumnName()) && !cols.equals(colLastMonthGrp.getColumnName())
               && !cols.equals(colLastWeekGrp.getColumnName()))
         {
            //Custom Columns available in PARTITION, should be visible as read-only to others 
            if (!columnDefinitionMap.containsKey(cols))
            {
               custPartitionReadOnlyCols.add(cols);
            }
         }
      } 
      partitionSelectableCols = addCustomColsFromPreference(PreferenceScope.PARTITION, custPartitionReadOnlyCols);
      
      selCols = userSelectableCols;
   
      IColumnModel bccPerformanceColumnModel = new CostColumnModel(selCols, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_COST_AND_CONTROLLING);
      this.setCustomColumnFilters(bccPerformanceColumnModel, columnDefinitionMap,null);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(bccPerformanceColumnModel);
      costTable = new SortableTable<CostTableEntry>(colSelecpopup, null, new SortableTableComparator<CostTableEntry>(
            "processDefinition", true));
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
      
      List<String> viewColumns = prefHelper.getSelectedColumns(UserPreferencesEntries.V_COST_AND_CONTROLLING);
      List<String> allCols = getCustomColumnPreference(prefHelper);
      List<ColumnPreference> selectableCols = CollectionUtils.newArrayList();
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
               if(!viewColumns.contains(customColumn.getColumnName()))
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
               if(!CollectionUtils.isEmpty(customReadOnlyCols) && customReadOnlyCols.contains(columnDef[0]))
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
   
   /**
    * 
    */
   public void addNewColumn()
   {
      JsonObject columnDefinition = null;
      List<ColumnPreference> selCols = costTable.getColumnModel().getSelectableColumns();
      if (index == 0)
      {
         index++;
      }
      String columnTitle = propsBean.get("views.customColumn.label") + index;
      long userOrPartitionOID = costTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER
            ? SessionContext.findSessionContext().getUser().getOID()
            : SessionContext.findSessionContext().getUser().getPartitionOID();
      String columnId = propsBean.get("views.customColumn.property") + userOrPartitionOID + index++ ;
      // Creates JSON object storing columnDefinition with values(columnId,columnName,duration..)
      columnDefinition = CustomColumnUtils.updateCustomColumnJson(columnId, columnTitle, 0, CustomColumnUtils.DAY_TYPE, 0,
            CustomColumnUtils.DAY_TYPE, columnDefinition, customColumnDateRange);
      columnDefinition.addProperty("userOID", SessionContext.findSessionContext().getUser().getOID());
      columnDefinition.addProperty("readOnly", false); //for user created column readOnly false
      
      ColumnPreference customColumn = createColumnPreferenceFromJSON(columnDefinition);
      selCols.add(customColumn);
      // Update the User and Partition column list- used while columnSelector toggle
      if(costTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER)
      {
         userSelectableCols =  selCols;
      }
      else
      {
         partitionSelectableCols =  selCols;
      }
      //Update table
      IColumnModel model = costTable.getColumnModel();
      model.setSelectableColumns(selCols);

      TableColumnSelectorPopup columnSelectorPopup = costTable.getColumnSelectorPopup() != null ? costTable
            .getColumnSelectorPopup() : new TableColumnSelectorPopup(model);
      
      List<String> allColumns = getCustomColumnPreference();
      if(CollectionUtils.isEmpty(allColumns))
      {
         allColumns = CollectionUtils.newArrayList();
      }
      allColumns.add(columnId + "#" + columnDefinition.toString());
      // Save 'allColumns' prefKey with new column definition appended
      userPrefsHelper.setString(UserPreferencesEntries.V_COST_AND_CONTROLLING,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS, allColumns);
      model.saveSelectableColumns(columnSelectorPopup.getSelectedPreferenceScope());
      
      TableDataFilterPopup filterPopup = this.setCustomColumnFilters(model, columnDefinitionMap, columnId);
      model.setDefaultSelectableColumns(selCols);
      initialize();
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
      customColumn = new ColumnPreference(columnId, CustomColumnUtils.CUSTOM_COL_PREFIX + columnId,
            ColumnDataType.STRING, columnTitle, true, false);
      customColumn.setColumnAlignment(ColumnAlignment.RIGHT);

      ColumnPreference colCost = new ColumnPreference(CustomColumnUtils.CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_COST_SUFFIX, CustomColumnUtils.CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_COST_SUFFIX, this.getMessages().getString("column.cost"),
            V_costControllingColumns, true, true);
      colCost.setColumnAlignment(ColumnAlignment.RIGHT);

      ColumnPreference colStatus = new ColumnPreference(CustomColumnUtils.CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX, CustomColumnUtils.CUSTOM_COL_PREFIX + columnId
            + CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX, this.getMessages().getString("column.status"),
            V_costControllingColumns, true, false);
      colStatus.setColumnAlignment(ColumnAlignment.CENTER);

      customColumn.addChildren(colCost);
      customColumn.addChildren(colStatus);

      columnDefinitionMap.put(columnId, columnDefinition);
      index = Integer.valueOf(columnId.substring(columnId.length() - 1)) + 1;

      CustomColumnUtils.updateCustomColumnDateRange(columnDefinition, customColumnDateRange);

      return customColumn;
   }

   /**
    * Create Filter PopUp for custom columns
    * 
    * @param columnModel
    * @param customColumns
    * @param currentColumn
    * @return
    */
   private TableDataFilterPopup setCustomColumnFilters(IColumnModel columnModel, Map<String, Object> customColumns, String currentColumn)
   {
      TableDataFilterPopup dataFilterPopup = null;
      for (ColumnPreference colPref : columnModel.getSelectableColumns())
      {
         if (colPref.getColumnProperty().startsWith(CustomColumnUtils.CUSTOM_COL_PREFIX))
         {
            String property = colPref.getColumnProperty();
            if (property.indexOf(CustomColumnUtils.CUSTOM_COL_PREFIX) != -1)
            {
               String columnId = property.substring(property.indexOf(".") + 1);
               if(customColumns.get(columnId)!=null)
               {
                  if(currentColumn == null || (!StringUtils.isEmpty(currentColumn) && currentColumn.equals(currentColumn)))
                  {
                     Boolean readOnly =  ((JsonObject)customColumns.get(columnId)).get("readOnly").getAsBoolean();
                     if(!readOnly)
                     {
                        CostTableDataFilter filter = new CostTableDataFilter("", "", "", false);
                        filter.updateFilterFields(customColumns.get(columnId));
                        dataFilterPopup = new TableDataFilterPopup(propsBean.get("views.customColumn.editLabel"),
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

   /**
    * Read preference string for 'allColumns' preference key
    * 
    * @return
    */
   private List<String> getCustomColumnPreference()
   {
      TableColumnSelectorPopup columnSelPopup = costTable != null ? costTable.getColumnSelectorPopup() : null;
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
      return getCustomColumnPreference(userPrefsHelper);
   }

   /**
    * Read preference string for 'allColumns' preference key
    * 
    * @param userPreferenceHelper
    * @return
    */
   private List<String> getCustomColumnPreference(UserPreferencesHelper userPreferenceHelper)
   {
      return userPreferenceHelper.getString(UserPreferencesEntries.V_COST_AND_CONTROLLING,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS);
   }
   
   /**
    * Call back method to handle view event
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
    	 selectedComponent=null;
         propsBean = MessagesBCCBean.getInstance();
         currencyCode = this.getMessages().getString("currencyCode");
         columnDefinitionMap = CollectionUtils.newHashMap();
         customColumnDateRange = CollectionUtils.newHashMap();
         selFixedCols = CollectionUtils.newArrayList();
         userSelectableCols = CollectionUtils.newArrayList();
         partitionSelectableCols = CollectionUtils.newArrayList();
         selFixedCols = CollectionUtils.newArrayList();
         initAllParticipants();
         costTable = null;
         createTable();
         initialize();
      }
   }
   /**
    * 
    */
   private void initAllParticipants()
   {
      List<RoleItem> allRoles = WorkflowFacade.getWorkflowFacade().getAllRolesExceptCasePerformer();
      roleSelectItem = new SelectItem[allRoles.size()];
      Collections.sort(allRoles, new Comparator<RoleItem>()
      {
         public int compare(RoleItem r1, RoleItem r2)
         {
            return r1.getRoleName().compareTo(r2.getRoleName());
         }

      });
      selectedModelParticipant = allRoles.isEmpty()?null:allRoles.get(0).getRole();
      RoleItem roleItem = null;
      for (int j = 0; j < allRoles.size(); j++)
      {
         roleItem = (RoleItem) allRoles.get(j);
         roleSelectItem[j] = new SelectItem(ParticipantUtils.getParticipantUniqueKey(roleItem.getRole()), roleItem.getRoleName());
      }

   }


   /**
    * Refresh the table
    */
   public void update()
   {
      initialize();
   }

   /**
    * To get all the participants
    * 
    * @return List
    */
   public SelectItem[] getAllParticipants()
   {
      return roleSelectItem;
   }

   /**
    * Used to update the Table values whenever value change
    * 
    * @param event
    */
   public void roleChangeListener(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      
      List<RoleItem> allRoles = WorkflowFacade.getWorkflowFacade().getAllRolesExceptCasePerformer();
      
      if (event.getNewValue() != null)
      {
         String selectedItem = event.getNewValue().toString();         
         if (selectedItem != null && getRoleSelectItem()!=null)
         {
               for (RoleItem item : allRoles)
               {                  
                  String key = ParticipantUtils.getParticipantUniqueKey(item.getRole());
                  if (selectedItem.equals(key))
                  {
                     setSelectedModelParticipant(item.getRole());
                     break;
                  }
               }
         }
         initialize();
      }
     

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {
      List<CostTableEntry> costStatistics = getCost();
      costTable.setList(costStatistics);
      costTable.initialize();
   } 

   /**
    * To get Cost value
    * 
    * @return List
    */
   public List<CostTableEntry> getCost()
   {
      UserWorktimeStatistics stat = getWorktimeStatistics();
      UserQuery query = UserQuery.findActive();
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
      List<CostsPerProcess> tableData = new ArrayList<CostsPerProcess>();
      Iterator pIter = ProcessDefinitionUtils.getAllBusinessRelevantProcesses().iterator();

      while (pIter.hasNext())
      {
         ProcessDefinition pd = (ProcessDefinition) pIter.next();
         tableData.add(new CostsPerProcess(pd, columnDefinitionMap));
      }
      CostsPerProcess cpp = null;    
      if (stat != null && selectedModelParticipant != null)
      {          
        Set<Long> ids=  stat.getAvailableUserOids();      
         WorktimeStatistics wStat;
         for (Long userOID:ids)
         {          
            wStat = stat.getWorktimeStatistics(userOID);
            if (wStat != null)
            {
               for (pIter = tableData.iterator(); pIter.hasNext();)
               {
                  cpp = (CostsPerProcess) pIter.next();
                  Contribution con = wStat.findContribution(cpp.getProcessDefinition()
                        .getQualifiedId(), selectedModelParticipant);
                  cpp.addContribution(con, customColumnDateRange);
               }
            }
         }
      }
      List<CostTableEntry> costData = new ArrayList<CostTableEntry>();
      if (tableData != null)
      {
         NumberFormat numberFormat = NumberFormat.getInstance();
         numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
         CostsPerProcess costsPerProcess;
         for (int i = 0; i < tableData.size(); i++)
         {
            costsPerProcess = (CostsPerProcess) tableData.get(i);
            costData.add(new CostTableEntry(I18nUtils.getProcessName(costsPerProcess.getProcessDefinition()),
                  numberFormat.format(costsPerProcess.getAverageCostsToday()), numberFormat.format(costsPerProcess
                        .getAverageCostsLastWeek()), numberFormat.format(costsPerProcess.getAverageCostsLastMonth()),
                  costsPerProcess.getTodayState(), costsPerProcess.getLastWeekState(), costsPerProcess
                        .getLastMonthState(), currencyCode, costsPerProcess.getCustomColumns()));

         }

      }

      return costData;
   }
   
   public UserWorktimeStatistics getWorktimeStatistics()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserWorktimeStatisticsQuery wsQuery = UserWorktimeStatisticsQuery.forAllUsers();
      wsQuery
            .setPolicy(CriticalCostPerExecutionPolicy.criticalityByCost(BusinessControlCenterConstants
                  .getInstanceCostThreshold(BusinessControlCenterConstants.YELLOW_THRESHOLD, 1.0f),
                  BusinessControlCenterConstants.getInstanceCostThreshold(BusinessControlCenterConstants.RED_THRESHOLD,
                        1.0f)));
      List<DateRange> dateRange = CollectionUtils.newArrayList();
      dateRange.add(DateRange.TODAY);
      dateRange.add(DateRange.LAST_WEEK);
      dateRange.add(DateRange.LAST_MONTH);
      for(Map.Entry<String, DateRange> custCols : customColumnDateRange.entrySet())
      {
         String key = custCols.getKey();
         DateRange range = custCols.getValue();
         dateRange.add(range);
      }
      if(!dateRange.isEmpty())
      {
         wsQuery.setPolicy(new StatisticsDateRangePolicy(dateRange));   
      }
      UserWorktimeStatistics stat = (UserWorktimeStatistics) facade.getAllUsers(wsQuery);
      return stat;
 }
   
   public void applyFilter(TableDataFilters tableDataFilters)
   {
      CostTableDataFilter filter = (CostTableDataFilter) tableDataFilters.getList().get(0);
      if (filter == null || filter.getColumnId() == null)
      {
         return;
      }
      // Update Column JSON with new values from filterPopup
      JsonObject columnDefinition = (JsonObject) columnDefinitionMap.get(filter.getColumnId());
      String columnId = filter.getColumnId();
      String columnTitle = filter.getColumnTitle(); 
      if(filter.isShowDatePicker())
      {
         Date startDate = filter.getStartDate();
         Date endDate = filter.getEndDate();
         columnDefinition = CustomColumnUtils.updateCustomColumnJson(columnId, columnTitle, startDate, endDate, columnDefinition, customColumnDateRange);
      }
      else
      {
         String startDateNumDays = filter.getStartDateNumDays();
         Integer startDateType = Integer.valueOf(filter.getStartDateType());
         String durationNumDays = filter.getDurationNumDays();
         Integer durationType = Integer.valueOf(filter.getDurationType());
         columnDefinition = CustomColumnUtils.updateCustomColumnJson(columnId, columnTitle, Integer.valueOf(startDateNumDays),
               startDateType, Integer.valueOf(durationNumDays), durationType, columnDefinition, customColumnDateRange);
      }
      
      List<String> allColumns = getCustomColumnPreference();
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
      userPrefsHelper.setString(UserPreferencesEntries.V_COST_AND_CONTROLLING,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS, allColumns);
      // Update the column Title
      List<ColumnPreference> selCols = costTable.getColumnModel().getSelectableColumns();
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
      if(costTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER)
      {
         userSelectableCols = selCols;
      }
      else
      {
         partitionSelectableCols = selCols;
      }
      initialize();
      PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView;");
   }
   
   public void deleteFilter(CostTableDataFilter costDataFilter)
   {
      List<String> allColumns = getCustomColumnPreference();
      Iterator<String> colIterator = allColumns.iterator();
      String columnId = costDataFilter.getColumnId();
      while (colIterator.hasNext())
      {
         String cols = colIterator.next();
         if (cols.startsWith(columnId))
         {
            allColumns.remove(allColumns.indexOf(cols));
            break;
         }
      }
      userPrefsHelper.setString(UserPreferencesEntries.V_COST_AND_CONTROLLING,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS, allColumns);
      
      List<ColumnPreference> selCols = costTable.getColumnModel().getSelectableColumns();
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
      if(costTable.getColumnSelectorPopup().getSelectedPreferenceScope() == PreferenceScope.USER)
      {
         userSelectableCols = selCols;
      }
      else
      {
         partitionSelectableCols = selCols;
      }
      columnDefinitionMap.remove(columnId);
      customColumnDateRange.remove(columnId);
      costTable.getColumnModel().setDefaultSelectableColumns(selCols);
      costTable.getColumnModel().saveSelectableColumns(costTable.getColumnSelectorPopup().getSelectedPreferenceScope());
      costTable.initialize();
      PortalApplication.getInstance().addEventScript("parent.BridgeUtils.View.syncActiveView;");
   }

   public void setSelectedModelParticipant(ModelParticipantInfo selectedModelParticipant)
   {
      this.selectedModelParticipant = selectedModelParticipant;
   }

   
   public ModelParticipantInfo getSelectedModelParticipant()
   {
      return selectedModelParticipant;
   }

   public List<SelectItem> getTempAllParticipantsList()
   {
      return tempAllParticipantsList;
   }

   public void setTempAllParticipantsList(List<SelectItem> tempAllParticipantsList)
   {
      this.tempAllParticipantsList = tempAllParticipantsList;
   }

   public DataTable<CostTableEntry> getCostTable()
   {
      return costTable;
   }

   public String getSelectedComponent()
   {
      return selectedComponent;
   }

   public void setSelectedComponent(String selectedComponent)
   {
      this.selectedComponent = selectedComponent;
   }

   public SelectItem[] getRoleSelectItem()
   {
      return roleSelectItem;
   }

   public void setRoleSelectItem(SelectItem[] roleSelectItem)
   {
      this.roleSelectItem = roleSelectItem;
   }

   class CostColumnModel extends DefaultColumnModel
   {

      private static final long serialVersionUID = 3257355871815472018L;
      
      /**
       * @param columns
       * @param fixedBeforeColumns
       * @param fixedAfterColumns
       * @param moduleId
       * @param viewId
       */
      public CostColumnModel(List<ColumnPreference> columns,
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
         List<String> viewColumns = prefHelper.getSelectedColumns(UserPreferencesEntries.V_COST_AND_CONTROLLING);
         for (ColumnPreference column : prefScope == PreferenceScope.USER ? userSelectableCols : partitionSelectableCols)
         {
            if(viewColumns.contains(column.getColumnName()))
            {
               column.setVisible(true);
            }
            else
            {
               column.setVisible(false);
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


