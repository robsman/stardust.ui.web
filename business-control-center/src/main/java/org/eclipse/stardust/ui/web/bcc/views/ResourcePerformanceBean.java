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
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalProcessingTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.WorktimeStatistics;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.bcc.jsf.ProcessingTimePerProcess;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
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
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ResourcePerformanceBean extends UIComponentBean implements ResourcePaths, ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private ModelParticipantInfo selectedModelParticipant;
   private List<ProcessingTimeTableEntry> userStatistics;
   private SortableTable<ProcessingTimeTableEntry> statisticsTable;
   private SelectItem[] roleSelectItem;
   private List<RoleItem> allRoles;
   private String selectedComponent;

   /**
    * 
    */
   public ResourcePerformanceBean()
   {
      super(V_resourcePerformanceView);

   }

   private void createTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      ColumnPreference pDefinitionCol = new ColumnPreference("ProcessDefinition", "processDefinitionId",
            ColumnDataType.STRING, this.getMessages().getString("column.processDefinition"), new TableDataFilterPopup(
                  new TableDataFilterSearch()));

      cols.add(pDefinitionCol);

      ColumnPreference colToday = new ColumnPreference("Today", this.getMessages().getString("column.today"));
      ColumnPreference todayTimeCol = new ColumnPreference("TodayTime", "averageTimeToday", ColumnDataType.STRING, this
            .getMessages().getString("column.processingTime"), null, true, false);
      todayTimeCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference todayStatusCol = new ColumnPreference("TodayStatus", "todayStatusLabel", this
            .getMessages().getString("column.status"), V_resourcePerformanceColumns,
            true, false);
      todayStatusCol.setColumnAlignment(ColumnAlignment.CENTER);

      colToday.addChildren(todayTimeCol);
      colToday.addChildren(todayStatusCol);
      cols.add(colToday);

      ColumnPreference colWeek = new ColumnPreference("Week", this.getMessages().getString("column.lastWeek"));
      ColumnPreference weekTimeCol = new ColumnPreference("WeekTime", "averageTimeLastWeek", ColumnDataType.STRING,
            this.getMessages().getString("column.processingTime"), null, true, false);
      weekTimeCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference weekStatusCol = new ColumnPreference("WeekStatus", "lastWeekStatusLabel", this
            .getMessages().getString("column.status"), V_resourcePerformanceColumns,
            true, false);
      weekStatusCol.setColumnAlignment(ColumnAlignment.CENTER);

      colWeek.addChildren(weekTimeCol);
      colWeek.addChildren(weekStatusCol);
      cols.add(colWeek);

      ColumnPreference colMonth = new ColumnPreference("Month", this.getMessages().getString("column.lastMonth"));
      ColumnPreference monthTimeCol = new ColumnPreference("MonthTime", "averageTimeLastMonth", ColumnDataType.STRING,
            this.getMessages().getString("column.processingTime"), null, true, false);
      monthTimeCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference monthStatusCol = new ColumnPreference("MonthStatus", "lastMonthStatusLabel", this
            .getMessages().getString("column.status"), V_resourcePerformanceColumns,
            true, false);
      monthStatusCol.setColumnAlignment(ColumnAlignment.CENTER);

      colMonth.addChildren(monthTimeCol);
      colMonth.addChildren(monthStatusCol);

      cols.add(colMonth);

      IColumnModel bccPerformanceColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_RESOURCE_PERFORMANCE);

      userStatistics = createUserStatistics();
      statisticsTable = new SortableTable<ProcessingTimeTableEntry>(userStatistics, bccPerformanceColumnModel, null,
            new SortableTableComparator<ProcessingTimeTableEntry>("processDefinitionId", true));
      statisticsTable.initialize();
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
    	 selectedComponent=null;
         allRoles = WorkflowFacade.getWorkflowFacade().getAllRoles();
         selectedModelParticipant=allRoles.isEmpty()?null:allRoles.get(0).getRole();
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
      if (evt.getNewValue() != null)
      {
         String selectedItem = evt.getNewValue().toString();
         if (selectedItem != null && getRoleSelectItem() != null)
         {
            for (RoleItem item : allRoles)
            {
               if (item.getRole().getQualifiedId().equals(selectedItem))
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

   

   /**
    * returns the list of User statistics
    * 
    * @return list
    */
   private List<ProcessingTimeTableEntry> createUserStatistics()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserWorktimeStatisticsQuery wsQuery = UserWorktimeStatisticsQuery.forAllUsers();
      wsQuery.setPolicy(CriticalProcessingTimePolicy.criticalityByDuration(BusinessControlCenterConstants
            .getProcessingTimeThreshold(BusinessControlCenterConstants.YELLOW_THRESHOLD, 1.0f),
            BusinessControlCenterConstants.getProcessingTimeThreshold(BusinessControlCenterConstants.RED_THRESHOLD,
                  1.0f)));
      UserWorktimeStatistics stat = (UserWorktimeStatistics) facade.getAllUsers(wsQuery);

      userStatistics = new ArrayList<ProcessingTimeTableEntry>();
      List<ProcessingTimePerProcess> tableData = new ArrayList<ProcessingTimePerProcess>();
      Iterator<ProcessDefinition> pIter = ProcessDefinitionUtils.getAllBusinessRelevantProcesses().iterator();
      ProcessingTimePerProcess ptp = null;
      while (pIter.hasNext())
      {
         ProcessDefinition pd = pIter.next();
         ptp = new ProcessingTimePerProcess(pd);
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
                  cpp.addContribution(con);

               }
            }
         }
      }
      for (ProcessingTimePerProcess cpp : tableData)
      {
         userStatistics.add(new ProcessingTimeTableEntry(I18nUtils.getProcessName(cpp.getProcessDefinition()), cpp
               .getAverageTimeToday(), cpp.getAverageTimeLastWeek(), cpp.getAverageTimeLastMonth(),
               cpp.getTodayState(), cpp.getLastMonthState(), cpp.getLastMonthState()));

      }

      return userStatistics;
   }

   /**
    * @return AllModelParticipants
    */
   public SelectItem[] getAllModelParticipants()
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
         roleSelectItem[j] = new SelectItem(((QualifiedModelParticipantInfo) roleItem.getRole()).getQualifiedId(),
               roleItem.getRoleName());
      }
      return roleSelectItem;

   }

   // **************** Modified setter method***********
   public void setSelectedModelParticipant(ModelParticipantInfo selectedModelParticipant)
   {
      if (selectedModelParticipant == null
            || (selectedModelParticipant != null && !selectedModelParticipant.equals(this.selectedModelParticipant)))
      {
         this.selectedModelParticipant = selectedModelParticipant;
         //sessionCtx.bind("selectedModelParticipant", selectedModelParticipant);

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

}
