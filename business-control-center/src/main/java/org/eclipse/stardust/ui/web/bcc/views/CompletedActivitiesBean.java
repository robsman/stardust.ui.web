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
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.PerformanceStatistics;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
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
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Giridhara.G
 * @version
 */
public class CompletedActivitiesBean extends UIComponentBean implements ResourcePaths, ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(CompletedActivitiesBean.class);

   private final static String QUERY_EXTENDER = "carnotBcCompletedActivities/queryExtender";

   private IQueryExtender queryExtender;

   private SessionContext sessionCtx;

   private WorkflowFacade facade;

   private SortableTable<CompletedActivityUserObject> completedActivitiesTable;

   private UserPerformanceStatistics userStatistics;

   private MessagesBCCBean propsBean;

   private static final int DEFAULT_NUMBER_OF_SELECTED_COLUMNS = 5;

   /**
    * Constructor
    */
   public CompletedActivitiesBean()
   {
      super(V_completedActivitiesView);
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
      Query query = createQuery();
      facade = WorkflowFacade.getWorkflowFacade();
      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }

      Users users = facade.getAllUsers((UserQuery) query);
      Iterator<UserItem> userIter = facade.getAllUsersAsUserItems(users).iterator();
      Collection/* <RoleItem> */participants = facade.getAllRolesExceptCasePerformer();
      List<CompletedActivityUserObject> completedActivitiesProcessList = new ArrayList<CompletedActivityUserObject>();
      List<CompletedActivityDynamicUserObject> complActiDyna;
      UserItem userItem;
      List<ProcessDefinition> processes;

      ProcessDefinition process;
      PerformanceStatistics pStatistics;
      Contribution con = null;

      RoleItem roleItem;
      processes = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();
      while (userIter.hasNext())
      {
         userItem = (UserItem) userIter.next();

         complActiDyna = new ArrayList<CompletedActivityDynamicUserObject>();
         if (processes != null)
         {
            for (int i = 0; i < processes.size(); i++)
            {
               process = (ProcessDefinition) processes.get(i);
               pStatistics = userStatistics != null ? userStatistics.getStatisticsForUserAndProcess(userItem.getUser()
                     .getOID(), process.getQualifiedId()) : null;
              
               int nAisCompletedToday = 0;
               int nAisCompletedWeek = 0;
               int nAisCompletedMonth = 0;
               
               if (pStatistics != null)
               {
                  for (Iterator<Participant> iter = participants.iterator(); iter.hasNext();)
                  {
                     roleItem = (RoleItem) iter.next();
                     con = pStatistics.findContribution(roleItem.getRole());
                     nAisCompletedToday += con.performanceToday.nAisCompleted;
                     nAisCompletedWeek += con.performanceThisWeek.nAisCompleted;
                     nAisCompletedMonth += con.performanceThisMonth.nAisCompleted;
                  }

               }
               complActiDyna.add(new CompletedActivityDynamicUserObject(I18nUtils.getProcessName(process), Integer
                     .toString(nAisCompletedToday), Integer.toString(nAisCompletedWeek), Integer
                     .toString(nAisCompletedMonth)));
            }
            completedActivitiesProcessList.add(new CompletedActivityUserObject(userItem, userItem.getUser().getId(),
                  Long.toString(userItem.getUser().getOID()), complActiDyna));
         }
      }

      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();

      ColumnPreference colTeamMember = new ColumnPreference("TeamMember", "name", propsBean
            .getString("views.common.column.teamMember"), V_completedActivityUserManagerColumns,
            new TableDataFilterPopup(new TableDataFilterSearch()), true, true);

      fixedCols.add(colTeamMember);

      List<ColumnPreference> selCols = new ArrayList<ColumnPreference>();

      if (completedActivitiesProcessList.size() > 0)
      {
         List<CompletedActivityDynamicUserObject> sampleCompletedActivityList = completedActivitiesProcessList.get(0)
               .getCompletedActivitiesList();
         int i = 0;
         for (CompletedActivityDynamicUserObject com : sampleCompletedActivityList)
         {
            ColumnPreference processActivityCol = new ColumnPreference("p" + i, com.getProcessId());
            
            processActivityCol.setVisible((i < DEFAULT_NUMBER_OF_SELECTED_COLUMNS) ? true : false);

            ColumnPreference colToday = new ColumnPreference("Today" + i, "completedActivitiesList[" + i + "].day",
                  ColumnDataType.STRING, this.getMessages().getString("column.today"));
            colToday.setColumnAlignment(ColumnAlignment.CENTER);

            ColumnPreference colWeek = new ColumnPreference("Week" + i, "completedActivitiesList[" + i + "].week",
                  ColumnDataType.STRING, this.getMessages().getString("column.week"));
            colWeek.setColumnAlignment(ColumnAlignment.CENTER);

            ColumnPreference colMonth = new ColumnPreference("Month" + i, "completedActivitiesList[" + i + "].month",
                  ColumnDataType.STRING, this.getMessages().getString("column.month"));
            colMonth.setColumnAlignment(ColumnAlignment.CENTER);

            processActivityCol.addChildren(colToday);
            processActivityCol.addChildren(colWeek);
            processActivityCol.addChildren(colMonth);

            selCols.add(processActivityCol);

            i++;
         }
      }

      IColumnModel columnModel = new DefaultColumnModel(selCols, fixedCols, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_COMPLETED_ACTIVITY);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      completedActivitiesTable = new SortableTable<CompletedActivityUserObject>(colSelecpopup, null,
            new SortableTableComparator<CompletedActivityUserObject>("name", true));

      completedActivitiesTable.setList(completedActivitiesProcessList);
      completedActivitiesTable.initialize();

   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         sessionCtx = SessionContext.findSessionContext();
         queryExtender = getQueryExtender();
         facade = WorkflowFacade.getWorkflowFacade();
         propsBean = MessagesBCCBean.getInstance();

         try
         {
            initialize();
         }
         catch (Exception e)
         {
            ExceptionHandler.handleException(e);
         }
      }
   }

   /**
    * @return Query
    */
   public Query createQuery()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserQuery query = WorkflowFacade.getWorkflowFacade().getTeamQuery(true);
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
      userStatistics = (UserPerformanceStatistics) facade.getAllUsers(UserPerformanceStatisticsQuery.forAllUsers());

      if (queryExtender != null)
      {
         queryExtender.extendQuery(query);
      }
      return query;
   }

   /**
    * @return IQueryExtender
    */
   public IQueryExtender getQueryExtender()
   {
      if (queryExtender == null)
      {
         return (IQueryExtender) sessionCtx.lookup(QUERY_EXTENDER);
      }
      return queryExtender;
   }

   /**
    * @return SortableTable<CompletedActivityUserObject>
    */
   public SortableTable<CompletedActivityUserObject> getCompletedActivitiesTable()
   {
      return completedActivitiesTable;
   }
}
