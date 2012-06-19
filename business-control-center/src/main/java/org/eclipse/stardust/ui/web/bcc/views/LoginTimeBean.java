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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics.LoginStatistics;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.UserDefinedQueryResult;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterListener;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.ISortHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.UserQuerySortHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class LoginTimeBean extends UIComponentBean
      implements ITableDataFilterListener, ResourcePaths,ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private final static String QUERY_EXTENDER = "carnotBcLoginTime/queryExtender";

   private List<LoginTimeTableEntry> tableEntires;

   private SortableTable<LoginTimeTableEntry> loginStatisticsTable;

   private IQueryExtender queryExtender;

   private SessionContext sessionCtx;
   
   private boolean initSuccess;

   /**
    * 
    */
   public LoginTimeBean()
   {
      super(V_resourceLogin);
      

   }

   private void createTable()
   {

      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      ColumnPreference nameCol = new ColumnPreference("UserName", "name", this.getMessages().getString(
            "column.teamMember"), V_resourceLoginViewColumns, new TableDataFilterPopup(new TableDataFilterSearch()),
            true, true);

      cols.add(nameCol);
      ColumnPreference colTotalLogedIn = new ColumnPreference("TotalLogedIn", this.getMessages().getString(
            "title"));
      ColumnPreference dayCol = new ColumnPreference("Day", "day", ColumnDataType.STRING, this.getMessages().getString(
            "column.day"), null, true, false);
      dayCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference weekCol = new ColumnPreference("Week", "week", ColumnDataType.STRING, this.getMessages()
            .getString("column.week"), null, true, false);
      weekCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference monthCol = new ColumnPreference("Month", "month", ColumnDataType.STRING, this.getMessages()
            .getString("column.month"), null, true, false);
      monthCol.setColumnAlignment(ColumnAlignment.CENTER);

      colTotalLogedIn.addChildren(dayCol);
      colTotalLogedIn.addChildren(weekCol);
      colTotalLogedIn.addChildren(monthCol);

      cols.add(colTotalLogedIn);

      IColumnModel loginTimeColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_LOGIN_TIME);
      loginStatisticsTable = new SortableTable<LoginTimeTableEntry>(null, loginTimeColumnModel, null,
            new SortableTableComparator<LoginTimeTableEntry>("name", true));

     
   }
   @Override
   public void initialize()
   {
      try
      {
         initSuccess = false;
         tableEntires = createLoginStatistics();
         loginStatisticsTable.setList(tableEntires);
         loginStatisticsTable.initialize();
         initSuccess = true;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         sessionCtx = SessionContext.findSessionContext();
         queryExtender = getQueryExtender();
         createTable();
         initialize();
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
    * Performs search and creates the list of Login Statistics as per Users
    * 
    * @return list of Login Statistics Table Entries
    */
   private List<LoginTimeTableEntry> createLoginStatistics()
   {
      LoginTimeSearchHandler ltsh = new LoginTimeSearchHandler(getQueryExtender());
      Query query = ltsh.createQuery();
      QueryResult qr = ltsh.performSearch(query);
      Iterator< ? > itr = qr.iterator();
      List<LoginTimeTableEntry> tableData = new ArrayList<LoginTimeTableEntry>();
      while (itr.hasNext())
      {
         Object obj = itr.next();
         if (obj instanceof TableEntry)
         {
            TableEntry te = (TableEntry) obj;
            tableData.add(new LoginTimeTableEntry(te.getUserItem(), te.getUserItem()
                  .getUser().getId(), Long.toString(te.getUserItem().getUser().getOID()),
                  te.getDay(), te.getWeek(), te.getMonth()));
         }
      }
      return tableData;
   }

   // Modified Getter Setter Methods
   public void attachQueryExtender(IQueryExtender queryExtender)
   {
      this.queryExtender = queryExtender;
      sessionCtx.bind(QUERY_EXTENDER, queryExtender);
   }

   public IQueryExtender getQueryExtender()
   {
      if (queryExtender == null)
      {
         return (IQueryExtender) sessionCtx.lookup(QUERY_EXTENDER);
      }
      return queryExtender;
   }

   public static class TableEntry
   {
      private UserItem user;

      private String timeLoggedInToday;

      private String timeLoggedInThisWeek;

      private String timeLoggedInThisMonth;

      public TableEntry(UserItem user, Date timeLoggedInToday, Date timeLoggedInThisWeek,
            Date timeLoggedInThisMonth)
      {
         this.timeLoggedInToday = formatDate(timeLoggedInToday);
         this.timeLoggedInThisWeek = formatDate(timeLoggedInThisWeek);
         this.timeLoggedInThisMonth = formatDate(timeLoggedInThisMonth);
         this.user = user;
      }

      private String formatDate(Date date)
      {
         double timeInMs = date != null ? date.getTime() : 0;
         int minutesPerDay = BusinessControlCenterConstants.getWorkingMinutesPerDay();
         return timeInMs == 0 ? "-" : DateUtils
               .formatDurationInHumanReadableFormat((long) timeInMs, minutesPerDay * 60);
      }

      public String getMonth()
      {
         return timeLoggedInThisMonth;
      }

      public String getWeek()
      {
         return timeLoggedInThisWeek;
      }

      public String getDay()
      {
         return timeLoggedInToday;
      }

      public User getUser()
      {
         return user.getUser();
      }

      public UserItem getUserItem()
      {
         return user;
      }

   }

   private static class LoginTimeSearchHandler implements ISearchHandler, ISortHandler
   {
      private UserLoginStatistics statistics;

      private IQueryExtender queryExtender;

      private static final ISortHandler sortHandler = new UserQuerySortHandler();

      public LoginTimeSearchHandler(IQueryExtender queryExtender)
      {
         this.queryExtender = queryExtender;
      }

      public Query createQuery()
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         statistics = (UserLoginStatistics) facade.getAllUsers(UserLoginStatisticsQuery
               .forAllUsers());
         UserQuery query = WorkflowFacade.getWorkflowFacade().getTeamQuery(true);
         query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
         if (queryExtender != null)
         {
            queryExtender.extendQuery(query);
         }
         return query;
      }

      public QueryResult<UserItem> performSearch(Query query)
      {
         WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
         if (query.getOrderCriteria().getCriteria().size() == 0)
         {
            query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(
                  UserQuery.ACCOUNT);
         }
         Users users = facade != null ? facade.getAllUsers((UserQuery) query) : null;
         List<TableEntry> data = new ArrayList<TableEntry>();
         Iterator userIter = facade.getAllUsersAsUserItems(users).iterator();
         while (userIter.hasNext())
         {
            UserItem userItem = (UserItem) userIter.next();
            LoginStatistics loginStatistics = statistics.getLoginStatistics(userItem
                  .getUser().getOID());
            TableEntry entry = null;
            if (loginStatistics != null)
            {
               entry = new TableEntry(userItem, loginStatistics.timeLoggedInToday,
                     loginStatistics.timeLoggedInThisWeek,
                     loginStatistics.timeLoggedInThisMonth);
            }
            else
            {
               entry = new TableEntry(userItem, null, null, null);
            }
            data.add(entry);
         }
         return new UserDefinedQueryResult(query, data, users.hasMore(), new Long(users
               .getTotalCount()));
      }

      public void applySorting(Query query, List sortCriteria)
      {
         sortHandler.applySorting(query, sortCriteria);
      }

      public boolean isSortableColumn(String propertyName)
      {
         return sortHandler.isSortableColumn(propertyName);
      }

   }

   public void applyFilter(TableDataFilters tableDataFilters)
   {}

   public List<LoginTimeTableEntry> getTableEntires()
   {
      return tableEntires;
   }

   public SortableTable<LoginTimeTableEntry> getLoginStatisticsTable()
   {
      return loginStatisticsTable;
   }

   public boolean isInitSuccess()
   {
      return initSuccess;
   }
}