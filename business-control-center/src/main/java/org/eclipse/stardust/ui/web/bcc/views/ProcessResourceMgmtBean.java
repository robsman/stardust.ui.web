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
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.ISortHandler;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQueryResult;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;





/**
 * @author Giridhara.G
 * @version
 */
public class ProcessResourceMgmtBean extends UIComponentBean implements ResourcePaths, ViewEventHandler, IUserObjectBuilder
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(ProcessResourceMgmtBean.class);

   private final static String QUERY_EXTENDER = "carnotBcProcessResourceMgmt/queryExtender";

   private IQueryExtender queryExtender;

   private SessionContext sessionCtx;

   private PaginatorDataTable<ProcessResourceMgmtRoleTableEntry, ProcessResourceMgmtRoleTableEntry> processResourceRoleMgmtTable;

   private PaginatorDataTable<ProcessResourceMgmtUserTableEntry,ProcessResourceMgmtUserTableEntry> processResourceUserMgmtTable;

   private List<ProcessResourceMgmtUserTableEntry> processResourceUserList = null;

   private List<ProcessResourceMgmtRoleTableEntry> processResourceRoleList = null;
   
   private MessagesBCCBean propsBean;
   
   private String roleFilterNamePattern;
   private String userFilterNamePattern;

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
      processResourceRoleMgmtTable.refresh(true);
      processResourceUserMgmtTable.refresh(true);
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
      else if (ViewEventType.ACTIVATED == event.getType())
      {
         initialize();   
      }
   }

   /**
    * Refresh the table
    */
   public void update()
   {
      WorkflowFacade.getWorkflowFacade().initVars();
      initialize();
   }

   private void getProcessResourceUsrMgmtTable()
   {
      List<ColumnPreference> userCols = new ArrayList<ColumnPreference>();
      
      ProcessResourceMgmtSearchHandler processResourceMgmtSearchHandler = new ProcessResourceMgmtSearchHandler(
            getQueryExtender(), true);
      Query query = createQuery();
      processResourceUserList = getProcessResourceUserMgmt(query);

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
      IppFilterHandler filterHandler = new ProcessResourceMgmtFilterHandler();
      ISortHandler sortHandler = new ProcessResourceMgmtSortHandler();
      processResourceUserMgmtTable = new PaginatorDataTable<ProcessResourceMgmtUserTableEntry, ProcessResourceMgmtUserTableEntry>(
            userColSelecpopup, processResourceMgmtSearchHandler, filterHandler, null, this,
            new DataTableSortModel<ProcessResourceMgmtUserTableEntry>("userName", true));
      processResourceUserMgmtTable.setISearchHandler(processResourceMgmtSearchHandler);
      processResourceUserMgmtTable.setISortHandler(sortHandler);
      processResourceUserMgmtTable.setIFilterHandler(filterHandler);
      columnModelListener.setNeedRefresh(false);
      processResourceUserMgmtTable.initialize();      
      columnModelListener.setNeedRefresh(true);

   }

   private void getProcessResourceRolMgmtTable()
   {
      List<ColumnPreference> roleCols = new ArrayList<ColumnPreference>();
      
      ProcessResourceMgmtSearchHandler processResourceMgmtSearchHandler = new ProcessResourceMgmtSearchHandler(null,
            false);

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
      IppFilterHandler filterHandler = new ProcessResourceMgmtFilterHandler();
      ISortHandler sortHandler = new ProcessResourceMgmtSortHandler();
      processResourceRoleMgmtTable = new PaginatorDataTable<ProcessResourceMgmtRoleTableEntry, ProcessResourceMgmtRoleTableEntry>(
            roleColSelecpopup, processResourceMgmtSearchHandler, filterHandler, null, this,
            new DataTableSortModel<ProcessResourceMgmtRoleTableEntry>("name", true));
      processResourceRoleMgmtTable.setISearchHandler(processResourceMgmtSearchHandler);
      processResourceRoleMgmtTable.setISortHandler(sortHandler);
      processResourceRoleMgmtTable.setIFilterHandler(filterHandler);
      columnModelListener.setNeedRefresh(false);
      processResourceRoleMgmtTable.initialize();
      columnModelListener.setNeedRefresh(true);
   }

   /**
    * Used to set the ProcessResourceRoleMgmtTable
    */
   public List<ProcessResourceMgmtRoleTableEntry> getProcessResourceRoleMgmt()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      List<ProcessResourceMgmtRoleTableEntry> processResourceRoleList = new ArrayList<ProcessResourceMgmtRoleTableEntry>();
      List<RoleItem> roleItemList = facade.getAllRolesExceptCasePerformer();      
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

      }
      catch (Exception e)
      {
        trace.error(e);
      }
      return processResourceRoleList;
   }

   /**
    * Used to set the processResourceUserMgmtTable
    */
   public List<ProcessResourceMgmtUserTableEntry> getProcessResourceUserMgmt(Query query)
   {
      List<ProcessResourceMgmtUserTableEntry> processResourceUserList = new ArrayList<ProcessResourceMgmtUserTableEntry>();

      processResourceUserList = new ArrayList<ProcessResourceMgmtUserTableEntry>();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }
      Users users = facade.getAllUsers((UserQuery) query);
      List<UserItem> userItems = facade.getAllUsersAsUserItems(users);
      if (!userItems.isEmpty())
      {
         String userFullName;
         for (UserItem userItem : userItems)
         {
            userFullName = I18nUtils.getUserLabel(userItem.getUser());
            processResourceUserList.add(new ProcessResourceMgmtUserTableEntry(userItem.getUserName(), userItem,
                  userItem.getUser().getOID(), userItem.getUser().getId(), userFullName, userItem.getUser()
                        .getAccount(), userItem.getUser().getEMail(), userItem.getRoleCount(), userItem
                        .getDirectItemCount(), userItem.getIndirectItemCount(), userItem.getItemCount(), userItem
                        .isLoggedIn()));

         }
      }

      return processResourceUserList;
   }

   public PaginatorDataTable<ProcessResourceMgmtRoleTableEntry, ProcessResourceMgmtRoleTableEntry> getProcessResourceRoleMgmtTable()
   {
      return processResourceRoleMgmtTable;
   }

   public PaginatorDataTable<ProcessResourceMgmtUserTableEntry, ProcessResourceMgmtUserTableEntry> getProcessResourceUserMgmtTable()
   {
      return processResourceUserMgmtTable;
   }
   
   /**
    * Creates the query to get User Details
    * 
    * @return query
    */
   public Query createQuery()
   {
      UserQuery query = UserQuery.findActive();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);

      if (queryExtender != null)
      {
         queryExtender.extendQuery(query);
      }
      return query;
   }

   /**
    * 
    * @param list
    * @param filterNamePattern
    * @return
    */
   private List filterResult(List list, String filterNamePattern)
   {
      List filteredList = CollectionUtils.newArrayList();
      String regex = filterNamePattern.replaceAll("\\*", ".*") + ".*";
      Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
      Iterator iterator = list.iterator();

      while (iterator.hasNext())
      {
         Object instanceVar = iterator.next();
         if (instanceVar instanceof ProcessResourceMgmtUserTableEntry)
         {
            ProcessResourceMgmtUserTableEntry var = (ProcessResourceMgmtUserTableEntry) instanceVar;
            if (pattern.matcher(var.getUserName()).matches())
            {
               filteredList.add(var);
            }

         }
         else
         {
            ProcessResourceMgmtRoleTableEntry var = (ProcessResourceMgmtRoleTableEntry) instanceVar;
            if (pattern.matcher(var.getName()).matches())
            {
               filteredList.add(var);
            }
         }
      }
      return filteredList;
   }
   
   /**
    * 
    * @author Sidharth.Singh
    *
    */
   public class ProcessResourceMgmtFilterHandler extends IppFilterHandler
   {
      private static final long serialVersionUID = -2173022668039757090L;

      @Override
      public void applyFiltering(Query query, List<ITableDataFilter> filters)
      {
         roleFilterNamePattern = null;
         userFilterNamePattern = null;
         for (ITableDataFilter tableDataFilter : filters)
         {
            String filterName = tableDataFilter.getName();
            if ("Name".equals(filterName))
            {
               roleFilterNamePattern = ((TableDataFilterSearch) tableDataFilter).getValue();
            }
            if ("UserName".equals(filterName))
            {
               userFilterNamePattern = ((TableDataFilterSearch) tableDataFilter).getValue();
            }
         }
      }
   }
   
   /**
    * 
    * @author Sidharth.Singh
    *
    */
   private class ProcessResourceMgmtSearchHandler extends IppSearchHandler
   {
      
      private static final long serialVersionUID = 1L;

      private IQueryExtender queryExtender;
      private boolean userSearch;

      /**
       * @param queryExtender
       */
      public ProcessResourceMgmtSearchHandler(IQueryExtender queryExtender , boolean userSearch)
      {
         this.queryExtender = queryExtender;
         this.userSearch = userSearch;
      }

      /**
       * 
       */
      public Query createQuery()
      {
         return null;// No query for engine call
      }

     /**
      * 
      */
      @Override
      public QueryResult performSearch(Query query)
      {
         return null;
      }
      
      /**
       * 
       */
      @Override
      public IQueryResult performSearch(IQuery iQuery, int startRow, int pageSize)
      {
         if (userSearch)
         {
            if (CollectionUtils.isNotEmpty(processResourceUserList))
            {
               List<ProcessResourceMgmtUserTableEntry> resultList = StringUtils.isEmpty(userFilterNamePattern)
                     ? processResourceUserList
                     : filterResult(processResourceUserList, userFilterNamePattern);
               applySorting(resultList);
               RawQueryResult<ProcessResourceMgmtUserTableEntry> queryResult = new RawQueryResult<ProcessResourceMgmtUserTableEntry>(
                     resultList, null, false, Long.valueOf(resultList.size()));

               return (IQueryResult) new IppQueryResult(queryResult);
            }
            else
               return null;
         }
         else
         {
            processResourceRoleList = getProcessResourceRoleMgmt();
            List<ProcessResourceMgmtRoleTableEntry> resultList = StringUtils.isEmpty(roleFilterNamePattern)
                  ? processResourceRoleList
                  : filterResult(processResourceRoleList, roleFilterNamePattern);
            applySorting(resultList);
            RawQueryResult<ProcessResourceMgmtRoleTableEntry> queryResult = new RawQueryResult<ProcessResourceMgmtRoleTableEntry>(
                  resultList, null, false, Long.valueOf(resultList.size()));
            return (IQueryResult) new IppQueryResult(queryResult);
         }
      }


      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.jsf.common.ISortHandler#applySorting(org.eclipse.
       * stardust.engine.api.query.Query, java.util.List)
       */
      public void applySorting(List result)
      {
         if (!CollectionUtils.isEmpty(result))
         {
            ISortHandler sortHandler = (result.get(0) instanceof ProcessResourceMgmtUserTableEntry)
                  ? processResourceUserMgmtTable.getISortHandler()
                  : processResourceRoleMgmtTable.getISortHandler();
            ProcessResourceMgmtSortHandler resourceMgmtSortHandler = (ProcessResourceMgmtSortHandler) sortHandler;
            if (null != resourceMgmtSortHandler && null != resourceMgmtSortHandler.getSortCriterion())
            {
               SortCriterion sortCriterion = resourceMgmtSortHandler.getSortCriterion();
               if (null != resourceMgmtSortHandler.getSortCriterion())
               {
                  Comparator comparator = new SortableTableComparator(sortCriterion.getProperty(),
                        sortCriterion.isAscending());
                  Collections.sort(result, comparator);
               }
            }
         }

      }
      
   }
   
   /**
    * 
    * @author Sidharth.Singh
    * @version $Revision: $
    */
   private class ProcessResourceMgmtSortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = -2562964400250132610L;

      private SortCriterion sortCriterion;

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriterias)
      {}

      public void applySorting(IQuery iQuery, List<SortCriterion> sortCriterias)
      {

         if (CollectionUtils.isNotEmpty(sortCriterias))
         {
            sortCriterion = sortCriterias.get(0);
         }
         else
         {
            sortCriterion = null;
         }

      }

      public SortCriterion getSortCriterion()
      {
         return sortCriterion;
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

   public Object createUserObject(Object resultRow)
   {
      if (resultRow instanceof ProcessResourceMgmtRoleTableEntry)
         return (ProcessResourceMgmtRoleTableEntry) resultRow;
      else
         return (ProcessResourceMgmtUserTableEntry) resultRow;
   }

   public List<ProcessResourceMgmtUserTableEntry> getProcessResourceUserList()
   {
      return processResourceUserList;
   }

   public List<ProcessResourceMgmtRoleTableEntry> getProcessResourceRoleList()
   {
      return processResourceRoleList;
   }
}

