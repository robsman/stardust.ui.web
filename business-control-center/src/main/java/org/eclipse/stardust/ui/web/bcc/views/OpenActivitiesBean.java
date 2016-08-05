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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.ActivitySearchHandler;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.OpenActivitiesCalculator;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;



/**
 * @author Giridhara.G
 * @version
 */

public class OpenActivitiesBean extends UIComponentBean implements ResourcePaths,ViewEventHandler,ICallbackHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(OpenActivitiesBean.class);
   
   private static final int COLUMN_SIZE = 5;

   protected static final int TODAY_COL_OFFSET = 0;

   protected static final int YESTERDAY_COL_OFFSET = 1;

   protected static final int DAY_AVG_COL_OFFSET = 2;
   
   protected static final int HIBERNATED_COL_OFFSET = 3;
   
   public static final String BEAN_ID ="openActivitiesBean";

   private SortableTable<OpenActivitiesUserObject> pendingActTable;

   private WorkflowFacade facade;

   private MessagesBCCBean propsBean;
   
   private ActivityTableHelper activityTableHelper;
   private boolean activityTableVisible;
   private ActivitySearchHandler handler = null;
   
   
   /**
    * OpenActivitiesBean Constructor
    */
   public OpenActivitiesBean()
   {
      super(V_pendingActivitiesView);
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {        
         facade = WorkflowFacade.getWorkflowFacade();
         propsBean = MessagesBCCBean.getInstance();
         initialize();
         activityTableVisible = false;
         activityTableHelper = new ActivityTableHelper();
         handler = new ActivitySearchHandler();
         if (activityTableHelper != null)
         {
            activityTableHelper.setShowResubmissionTime(true);
            activityTableHelper.initActivityTable();
            activityTableHelper.setCallbackHandler(this);
            activityTableHelper.setStrandedActivityView(false);
            activityTableHelper.getActivityTable().initialize();
            activityTableHelper.getActivityTable().setISearchHandler(handler);
         }

      }
   }
   /**
    * Refresh the table
    */
   public void update()
   {
      initialize();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {
      OpenActivitiesStatisticsQuery query = OpenActivitiesStatisticsQuery
            .forAllProcesses();
      query.setPolicy(new CriticalExecutionTimePolicy(Constants
            .getCriticalDurationThreshold(ProcessInstancePriority.LOW, 1.0f), Constants
            .getCriticalDurationThreshold(ProcessInstancePriority.NORMAL, 1.0f),
            Constants.getCriticalDurationThreshold(ProcessInstancePriority.HIGH, 1.0f)));
      OpenActivitiesStatistics openActivityStatistics = (OpenActivitiesStatistics) facade
            .getAllActivityInstances(query);

      Collection<ProcessDefinition> processDefinition =facade.getAllProcessDefinitions();

      OpenActivitiesCalculator openActivitiesCalculator = new OpenActivitiesCalculator(
            processDefinition, openActivityStatistics);

      List<String> overviewList = new ArrayList<String>();
      overviewList.add(this.getMessages().getString("column.totalOpenActivity"));
      overviewList.add(this.getMessages().getString("column.criticalOpenActivity"));

      String[] participantname = null;
      List<OpenActivitiesUserObject> pendingActList = new ArrayList<OpenActivitiesUserObject>();
      List< RoleItem > participantList = facade.getAllRolesExceptCasePerformer();

      List<Object[]> data = new ArrayList<Object[]>();

      if (participantList != null)
      {
         participantname = new String[participantList.size()];
      }
      
      if (participantList != null)
      {
         RoleItem roleItem;
         ModelParticipantInfo roledetails;     
         Map totalOpenActivities;
         Map criticalOpenActivities;
         List<OpenActivitiesDynamicUserObject> pendingActDynamicList = new ArrayList<OpenActivitiesDynamicUserObject>();
         List<OpenActivitiesDynamicUserObject> pendingCriticalActDynamicList = new ArrayList<OpenActivitiesDynamicUserObject>();
         for (int i = 0; i < participantList.size(); i++)
         {
            roleItem =  participantList.get(i);
            roledetails = roleItem.getRole();
            participantname[i] = roleItem.getRoleName();

            totalOpenActivities = openActivitiesCalculator
                  .getTotalOpenActivities(roledetails);
            criticalOpenActivities = openActivitiesCalculator
                  .getCriticalOpenActivities(roledetails);
            
            Long openActivitiesToday = new Long(((Double) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY)).longValue());
            Long openActivitiesYesterday = new Long(((Double) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY)).longValue());
            Double openActivitiesAvg = (Double) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_AVG);
            Long hibernatedActivitiesCount = (Long) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATED);
            Set<Long> openActivitiesOids = (Set<Long>) totalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY_OIDS);
            Set<Long> openActivitiesYesterdayOids = (Set<Long>) totalOpenActivities
            .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY_OIDS);
            Set<Long> openActivityHibernateOids = (Set<Long>) totalOpenActivities
            .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATE_OIDS);
            
            OpenActivitiesDynamicUserObject dyna = new OpenActivitiesDynamicUserObject(
                  openActivitiesToday, openActivitiesYesterday, openActivitiesAvg, hibernatedActivitiesCount);
            dyna.setOpenActivitiesTodayOids(openActivitiesOids);
            dyna.setOpenActivitiesYesterdayOids(openActivitiesYesterdayOids);
            dyna.setOpenActivitiesHibernateOids(openActivityHibernateOids);
            pendingActDynamicList.add(dyna);
            
            openActivitiesToday = new Long(
                  ((Double) criticalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY)).longValue());
            openActivitiesYesterday = new Long(
                  ((Double) criticalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY)).longValue());
            openActivitiesAvg = (Double) criticalOpenActivities.get(OpenActivitiesCalculator.OPEN_ACTIVITIES_AVG);
            hibernatedActivitiesCount = (Long) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATED);
            openActivitiesOids = (Set<Long>) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY_OIDS);
            openActivitiesYesterdayOids = (Set<Long>) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY_OIDS);
            openActivityHibernateOids = (Set<Long>) criticalOpenActivities
                  .get(OpenActivitiesCalculator.OPEN_ACTIVITY_HIBERNATE_OIDS);
            
            dyna = new OpenActivitiesDynamicUserObject(
                  openActivitiesToday, openActivitiesYesterday, openActivitiesAvg, hibernatedActivitiesCount);
            dyna.setOpenActivitiesTodayOids(openActivitiesOids);
            dyna.setOpenActivitiesYesterdayOids(openActivitiesYesterdayOids);
            dyna.setOpenActivitiesHibernateOids(openActivityHibernateOids);            
            pendingCriticalActDynamicList.add(dyna);
         }
         pendingActList.add(new OpenActivitiesUserObject(overviewList.get(0)
               .toString(), pendingActDynamicList));
         pendingActList.add(new OpenActivitiesUserObject(overviewList.get(1)
               .toString(), pendingCriticalActDynamicList));
         
      }

      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      fixedCols.add(new ColumnPreference("Overview", "overviewLabel", this.getMessages()
            .getString("column.overView"), V_pendingActivitiesColumns,
            null, true, true));
      
      // Add Columns as per Data
      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();
      if (pendingActList.size() > 0)
      {
         List<OpenActivitiesDynamicUserObject> roleList = pendingActList.get(0)
               .getParticipantList();
         int i = 0;
         boolean visible = true;
         ColumnPreference dynaCol;
         ColumnPreference colToday;
         ColumnPreference colYesterday;
         ColumnPreference colMonth;
         ColumnPreference colHibernated;
         for (OpenActivitiesDynamicUserObject re : roleList)
         {
            dynaCol = new ColumnPreference("participant" + i, participantname[i]);
            
            visible = i >= COLUMN_SIZE ? false : true;
            
            dynaCol.setVisible(visible);
            
            colToday = new ColumnPreference("today" + i, "participantList[" + i
                  + "].today", this.getMessages().getString(
                  "column.today"), V_pendingActivitiesColumns, null, true, false);
            colToday.setColumnAlignment(ColumnAlignment.CENTER);

            colYesterday = new ColumnPreference("yesterday" + i, "participantList[" + i
                  + "].yesterday", this.getMessages().getString(
                  "column.yesterday"), V_pendingActivitiesColumns, true, false);

            colYesterday.setColumnAlignment(ColumnAlignment.CENTER);

            colMonth = new ColumnPreference("month" + i, "participantList[" + i
                  + "].month", ColumnDataType.NUMBER, this.getMessages().getString(
                  "column.dayMonth"), true, false);

            colMonth.setColumnAlignment(ColumnAlignment.CENTER);

            colHibernated = new ColumnPreference("hibernated" + i, "participantList[" + i
                  + "].hibernated", this.getMessages().getString(
                  "column.hibernated"), V_pendingActivitiesColumns, null, true, false);
            colHibernated.setColumnAlignment(ColumnAlignment.CENTER);
            
            dynaCol.addChildren(colToday);
            dynaCol.addChildren(colYesterday);
            dynaCol.addChildren(colMonth);
            dynaCol.addChildren(colHibernated);
            i++;
            selectableCols.add(dynaCol);
         }
         
      }
      IColumnModel columnModel = new DefaultColumnModel(selectableCols, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_PENDING_ACTIVITIES);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      pendingActTable = new SortableTable<OpenActivitiesUserObject>(colSelecpopup, null,
            new SortableTableComparator<OpenActivitiesUserObject>("overviewLabel", true));
      pendingActTable.setList(pendingActList);
      pendingActTable.initialize();

   }

   public void fetchActivityAndRefresh(Set<Long> oids)
   {
      activityTableVisible = true;
      handler.setOids(oids);
      activityTableHelper.getActivityTable().refresh(true);
   }
   
   
   public SortableTable<OpenActivitiesUserObject> getPendingActTable()
   {
      return pendingActTable;
   }
   
   public ActivityTableHelper getActivityTableHelper()
   {
      return activityTableHelper;
   }

   public void setActivityTableHelper(ActivityTableHelper activityTableHelper)
   {
      this.activityTableHelper = activityTableHelper;
   }

   public boolean isActivityTableVisible()
   {
      return activityTableVisible;
   }

   public void handleEvent(EventType eventType)
   {
      if (eventType == EventType.APPLY)
      {
         initialize();
      }
   }
   
}
