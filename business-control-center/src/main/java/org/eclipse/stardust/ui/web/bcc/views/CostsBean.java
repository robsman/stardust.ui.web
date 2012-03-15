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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalCostPerExecutionPolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.WorktimeStatistics;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.bcc.jsf.CostsPerProcess;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Giridhara.G
 * @version
 */
public class CostsBean extends UIComponentBean implements ResourcePaths,ViewEventHandler
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
   

   /**
    * Constructor CostsBean
    */
   public CostsBean()
   {
      super(V_costsView);
   }
   
   private void createTable(){
      
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

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

      cols.add(colProcessDefinition);
      cols.add(colTodayCostGrp);
      cols.add(colLastWeekGrp);
      cols.add(colLastMonthGrp);

      IColumnModel bccPerformanceColumnModel = new DefaultColumnModel(cols, null, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_COST_AND_CONTROLLING);

      costTable = new SortableTable<CostTableEntry>(bccPerformanceColumnModel, null,
            new SortableTableComparator<CostTableEntry>("processDefinition", true));
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
         initAllParticipants();
         createTable();
         initialize();
      }
   }
   /**
    * 
    */
   private void initAllParticipants()
   {
      List<RoleItem> allRoles = WorkflowFacade.getWorkflowFacade().getAllRoles();
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
      
      List<RoleItem> allRoles = WorkflowFacade.getWorkflowFacade().getAllRoles();
      
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
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserWorktimeStatisticsQuery wsQuery = UserWorktimeStatisticsQuery.forAllUsers();
      wsQuery.setPolicy(CriticalCostPerExecutionPolicy.criticalityByCost(
            BusinessControlCenterConstants.getInstanceCostThreshold(
                  BusinessControlCenterConstants.YELLOW_THRESHOLD, 1.0f),
            BusinessControlCenterConstants.getInstanceCostThreshold(
                  BusinessControlCenterConstants.RED_THRESHOLD, 1.0f)));
      UserWorktimeStatistics stat = (UserWorktimeStatistics) facade.getAllUsers(wsQuery);
      UserQuery query = UserQuery.findActive();
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
      List<CostsPerProcess> tableData = new ArrayList<CostsPerProcess>();
      Iterator pIter = ProcessDefinitionUtils.getAllBusinessRelevantProcesses().iterator();

      while (pIter.hasNext())
      {
         ProcessDefinition pd = (ProcessDefinition) pIter.next();
         tableData.add(new CostsPerProcess(pd));
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
                  cpp.addContribution(con);
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
                        .getLastMonthState(), currencyCode));

         }

      }

      return costData;
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
}
