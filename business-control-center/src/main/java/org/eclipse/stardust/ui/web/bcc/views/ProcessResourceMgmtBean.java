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
import java.util.List;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserDefinedQueryResult;
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
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.ISortHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.UserQuerySortHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;





/**
 * @author Giridhara.G
 * @version
 */
public class ProcessResourceMgmtBean extends UIComponentBean implements ResourcePaths, ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ProcessResourceMgmtBean.class);

   private final static String QUERY_EXTENDER = "carnotBcProcessResourceMgmt/queryExtender";

   private IQueryExtender queryExtender;

   private SessionContext sessionCtx;

   private SortableTable<ProcessResourceMgmtRoleTableEntry> processResourceRoleMgmtTable;

   private SortableTable<ProcessResourceMgmtUserTableEntry> processResourceUserMgmtTable;

   private MessagesBCCBean propsBean;

   /**
    * Constructor ProcessResourceMgmtBean
    */
   public ProcessResourceMgmtBean()
   {
      super(V_resourceAvailability);
   }

   /**
    * Used to get the currentInstance
    * 
    * @return currentInstance
    */
   public static ProcessResourceMgmtBean getCurrent()
   {
      return (ProcessResourceMgmtBean) FacesContext.getCurrentInstance().getApplication().getVariableResolver()
            .resolveVariable(FacesContext.getCurrentInstance(), "processResourceMgmtBean");
   }

   /*
    * This method is used to Initialize the processResourceAvailablilty table
    */
   public void initialize()
   {
      getProcessResourceUserMgmt();
      getProcessResourceRoleMgmt();
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         sessionCtx = SessionContext.findSessionContext();
         propsBean = MessagesBCCBean.getInstance();
         getProcessResourceUsrMgmtTable();
         getProcessResourceRolMgmtTable();
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

   private void getProcessResourceUsrMgmtTable()
   {
      List<ColumnPreference> userCols = new ArrayList<ColumnPreference>();

      ColumnPreference colUserName = new ColumnPreference("UserName", "userName", propsBean
            .getString("views.processOverviewView.priorityTable.column.name"),
            V_resourceAvailabilityUserManagerColumns, new TableDataFilterPopup(new TableDataFilterSearch()), true, true);

      ColumnPreference colRoleCount = new ColumnPreference("RoleCount", "roleCount", ColumnDataType.STRING, this
            .getMessages().getString("accountTable.column.rolesCount"));
      colRoleCount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colDirectItemCount = new ColumnPreference("DirectItemCount", "directItemCount",
            ColumnDataType.STRING, this.getMessages().getString("accountTable.column.directItems"));
      colDirectItemCount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colIndirectItemCount = new ColumnPreference("IndirectItemCount", "indirectItemCount",
            ColumnDataType.STRING, this.getMessages().getString("accountTable.column.indirectItems"));
      colIndirectItemCount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colItemCount = new ColumnPreference("ItemCount", "itemCount", ColumnDataType.STRING, this
            .getMessages().getString("accountTable.column.totalItemsCount"));
      colItemCount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colLoggedIn = new ColumnPreference("LoggedIn", "loggedIn", ColumnDataType.STRING, this
            .getMessages().getString("accountTable.column.loggedIn"));

      userCols.add(colUserName);
      userCols.add(colRoleCount);
      userCols.add(colDirectItemCount);
      userCols.add(colIndirectItemCount);
      userCols.add(colItemCount);
      userCols.add(colLoggedIn);
      
      DefaultColumnModelEventHandler columnModelListener = new DefaultColumnModelEventHandler();

      IColumnModel userColumnModel = new DefaultColumnModel(userCols, null, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_PROCESS_RESOURCE_USR_MGT, columnModelListener);
     
      TableColumnSelectorPopup userColSelecpopup = new TableColumnSelectorPopup(userColumnModel);

      processResourceUserMgmtTable = new SortableTable<ProcessResourceMgmtUserTableEntry>(userColSelecpopup, null,
            new SortableTableComparator<ProcessResourceMgmtUserTableEntry>("userName", true));
      
      columnModelListener.setNeedRefresh(false);
      processResourceUserMgmtTable.initialize();      
      columnModelListener.setNeedRefresh(true);

   }

   private void getProcessResourceRolMgmtTable()
   {
      List<ColumnPreference> roleCols = new ArrayList<ColumnPreference>();

      ColumnPreference colName = new ColumnPreference("Name", "name", propsBean
            .getString("views.processOverviewView.priorityTable.column.name"),
            V_resourceAvailabilityRoleManagerColumns, new TableDataFilterPopup(new TableDataFilterSearch()), true, true);

      ColumnPreference colItems = new ColumnPreference("Items", "items", ColumnDataType.STRING, this.getMessages()
            .getString("rolesTable.column.items"));
      colItems.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colAccount = new ColumnPreference("Account", "account", ColumnDataType.STRING, this
            .getMessages().getString("rolesTable.column.account"));
      colAccount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colItemsPerUser = new ColumnPreference("ItemsPerUser", "itemsPerUser", ColumnDataType.STRING,
            this.getMessages().getString("rolesTable.column.itemsperUser"));
      colItemsPerUser.setColumnAlignment(ColumnAlignment.CENTER);

      roleCols.add(colName);
      roleCols.add(colItems);
      roleCols.add(colAccount);
      roleCols.add(colItemsPerUser);
      
      DefaultColumnModelEventHandler columnModelListener = new DefaultColumnModelEventHandler();

      IColumnModel roleColumnModel = new DefaultColumnModel(roleCols, null, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_PROCESS_RESOURCE_ROLE_MGT, columnModelListener);
      TableColumnSelectorPopup roleColSelecpopup = new TableColumnSelectorPopup(roleColumnModel);

      processResourceRoleMgmtTable = new SortableTable<ProcessResourceMgmtRoleTableEntry>(roleColSelecpopup, null,
            new SortableTableComparator<ProcessResourceMgmtRoleTableEntry>("name", true));
      
      columnModelListener.setNeedRefresh(false);
      processResourceRoleMgmtTable.initialize();
      columnModelListener.setNeedRefresh(true);
   }

   /**
    * Used to set the ProcessResourceRoleMgmtTable
    */
   public void getProcessResourceRoleMgmt()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      List<ProcessResourceMgmtRoleTableEntry> processResourceRoleList = new ArrayList<ProcessResourceMgmtRoleTableEntry>();
      List<RoleItem> roleItemList = facade.getAllRoles();      
      DepartmentInfo departmentInfo;
      long departmentOid;

      try
      {
         for (RoleItem roleItem : roleItemList)
         {
            departmentInfo = roleItem.getRole().getDepartment();
            departmentOid = (departmentInfo == null) ? 0 : departmentInfo.getOID();
            //TODO FQID change required here
            processResourceRoleList.add(new ProcessResourceMgmtRoleTableEntry(
            roleItem.getRole().getQualifiedId(), departmentOid, roleItem.getRoleName(),
                  roleItem.getWorklistCount(), roleItem.getLoggedInUserCount(), roleItem.getUserCount(), roleItem
                        .getEntriesPerUser()));

         }

         processResourceRoleMgmtTable.setList(processResourceRoleList);       
      }
      catch (Exception e)
      {
        trace.error(e);
      }
   }

   /**
    * Used to set the processResourceUserMgmtTable
    */
   public void getProcessResourceUserMgmt()
   {
      List<ProcessResourceMgmtUserTableEntry> processResourceUserList = new ArrayList<ProcessResourceMgmtUserTableEntry>();

      ProcessResourceMgmtSearchHandler processResourceMgmtSearchHandler = new ProcessResourceMgmtSearchHandler(
            getQueryExtender());

      Query query = processResourceMgmtSearchHandler.createQuery();
      QueryResult queryResult = processResourceMgmtSearchHandler.performSearch(query);
      if (!queryResult.isEmpty())
      {
         String userFullName;
         UserItem userItem;
         for (int i = 0; i < queryResult.size(); i++)
         {
            userItem = (UserItem) queryResult.get(i);
            userFullName = I18nUtils.getUserLabel(userItem.getUser());
            processResourceUserList.add(new ProcessResourceMgmtUserTableEntry(userItem.getUserName(), userItem,
                  userItem.getUser().getOID(), userItem.getUser().getId(), userFullName, userItem.getUser()
                        .getAccount(), userItem.getUser().getEMail(), userItem.getRoleCount(), userItem
                        .getDirectItemCount(), userItem.getIndirectItemCount(), userItem.getItemCount(), userItem
                        .isLoggedIn()));

         }
      }

      processResourceUserMgmtTable.setList(processResourceUserList);
   }

   public SortableTable<ProcessResourceMgmtRoleTableEntry> getProcessResourceRoleMgmtTable()
   {
      return processResourceRoleMgmtTable;
   }

   public SortableTable<ProcessResourceMgmtUserTableEntry> getProcessResourceUserMgmtTable()
   {
      return processResourceUserMgmtTable;
   }

   private class ProcessResourceMgmtSearchHandler implements ISearchHandler, ISortHandler
   {
      
      private static final long serialVersionUID = 1L;

      private IQueryExtender queryExtender;

      private final ISortHandler sortHandler = new UserQuerySortHandler();

      /**
       * @param queryExtender
       */
      public ProcessResourceMgmtSearchHandler(IQueryExtender queryExtender)
      {
         this.queryExtender = queryExtender;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.jsf.common.ISearchHandler#createQuery()
       */
      public Query createQuery()
      {
         UserQuery query = UserQuery.findActive();
         query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));

         if (queryExtender != null)
         {
            queryExtender.extendQuery(query);
         }
         return query;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.jsf.common.ISearchHandler#performSearch(org.eclipse.stardust.engine.api.query.Query)
       */
      public QueryResult<UserItem> performSearch(Query query)
      {
         try
         {
            WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
            if (query.getOrderCriteria().getCriteria().size() == 0)
            {
               query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
            }
            Users users = facade.getAllUsers((UserQuery) query);
            List<UserItem> userItems = facade.getAllUsersAsUserItems(users);
            return new UserDefinedQueryResult(query, userItems, users.hasMore(), new Long(users.getTotalCount()));
         }
         catch (Exception e)
         {
            trace.error(e);
         }
         return null;
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.jsf.common.ISortHandler#applySorting(org.eclipse.stardust.engine.api.query.Query, java.util.List)
       */
      public void applySorting(Query query, List sortCriteria)
      {
         sortHandler.applySorting(query, sortCriteria);
      }

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.jsf.common.ISortHandler#isSortableColumn(java.lang. String)
       */
      public boolean isSortableColumn(String propertyName)
      {
         return sortHandler.isSortableColumn(propertyName);
      }
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
}

