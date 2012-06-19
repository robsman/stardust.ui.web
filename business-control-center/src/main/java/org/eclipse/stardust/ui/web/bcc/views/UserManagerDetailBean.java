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


import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.faces.event.AbortProcessingException;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.bcc.jsf.InvalidServiceException;
import org.eclipse.stardust.ui.web.bcc.jsf.PageMessage;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.ISortHandler;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQueryResult;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;


import com.icesoft.faces.component.paneltabset.TabChangeEvent;

/**
 * @author Giridhara.G
 * @version $Revision: $
 */

public class UserManagerDetailBean extends UIComponentBean
      implements ICallbackHandler, ResourcePaths, ViewEventHandler,IUserObjectBuilder<UserManagerRoleAssignmentUserObject>
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(UserManagerDetailBean.class);

   public final static String BEAN_ID = "userManagerDetail";

   private Long userOid;

   private String userFullName;

   private String userAccount;

   private String userEmailId;

   private String directCountItem;

   private String inDirectCountItem;

   private String roleCount;

   private UserItem user;
   
   private PaginatorDataTable<UserManagerRoleAssignmentUserObject, UserManagerRoleAssignmentUserObject> roleAssignedTable;
   
   private PaginatorDataTable<UserManagerRoleAssignmentUserObject, UserManagerRoleAssignmentUserObject> roleAssignableTable;   

   private boolean disableAddRole;

   private boolean disableRemoveRole; 

   private UserItem userItem;

   private WorkflowFacade facade;   

   private int selectedTabIndex = 0;

   private View thisView;   

   private ActivityTableHelper activityHelper;
   
   private Map<Long, ProcessInstance> processInstances;
   
   private boolean canManageAuthorization = false;

   /**
    * 
    */
   public UserManagerDetailBean()
   {
      super(V_userManagerDetailView);
   }

   /**
    * 
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         this.thisView = event.getView();
         facade = WorkflowFacade.getWorkflowFacade();
         createAssignedRolesTable();
         createAssignableRolesTable();
         canManageAuthorization = AuthorizationUtils.canManageAuthorization();
      }
      else if (ViewEventType.ACTIVATED == event.getType())
      {
         String userOid = thisView.getParamValue("userOid");

         if (!StringUtils.isEmpty(userOid))
         {
            this.userOid = Long.parseLong(userOid);
         }
         initialize();         
      }
   }
   /**
    * Used to Initialize the Role Assigned and Assignable Table
    */
   private void refreshRoleTables()
   {
      roleAssignedTable.refresh(true);
      roleAssignableTable.refresh(true);
   }
   /**
    * 
    * @param assignableSearch
    * @return
    */
   public PaginatorDataTable<UserManagerRoleAssignmentUserObject, UserManagerRoleAssignmentUserObject> creatRolesTable(
         boolean assignableSearch)
   {
      UserManagerRoleAssignmentSearchHandler searchHandler = new UserManagerRoleAssignmentSearchHandler();
      searchHandler.setAssignableSearch(assignableSearch);

      String viewId = assignableSearch
            ? UserPreferencesEntries.V_ROLE_ASSIGNABLE
            : UserPreferencesEntries.V_ROLE_ASSIGNED;

      List<ColumnPreference> cols = CollectionUtils.newArrayList();

      ColumnPreference colName = new ColumnPreference("Name", "roleName", ColumnDataType.STRING, this.getMessages()
            .getString("column.name"));

      ColumnPreference colItems = new ColumnPreference("Items", "itemsCount", ColumnDataType.STRING, this.getMessages()
            .getString("items.label"));
      colItems.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colAccount = new ColumnPreference("Account", "roleAccount", ColumnDataType.STRING, this
            .getMessages().getString("account.label"));
      colAccount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colItemsPerUser = new ColumnPreference("ItemsPerUser", "itemsPerUser", ColumnDataType.STRING,
            this.getMessages().getString("itemsPerUser.label"));
      colItemsPerUser.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colName);
      cols.add(colItems);
      cols.add(colAccount);
      cols.add(colItemsPerUser);

      List<ColumnPreference> fixedCols = CollectionUtils.newArrayList();
      DefaultColumnModelEventHandler columnModelEventHandler = new DefaultColumnModelEventHandler();
      ISortHandler sortHandler = new UserManagerRoleAssignmentSortHandler();

      IColumnModel columnModel = new DefaultColumnModel(cols, fixedCols, null, UserPreferencesEntries.M_BCC, viewId,
            columnModelEventHandler);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      PaginatorDataTable<UserManagerRoleAssignmentUserObject, UserManagerRoleAssignmentUserObject> table = new PaginatorDataTable<UserManagerRoleAssignmentUserObject, UserManagerRoleAssignmentUserObject>(
            colSelecpopup, searchHandler, null, sortHandler, this,
            new DataTableSortModel<UserManagerRoleAssignmentUserObject>("roleName", true));

      table.setRowSelector(new DataTableRowSelector("select", true));
      table.setISearchHandler(searchHandler);
      table.setISortHandler(sortHandler);
      
      columnModelEventHandler.setNeedRefresh(false);
      table.initialize();
      columnModelEventHandler.setNeedRefresh(true);
      return table;
   }
   
   /**
    * Create AssignedRoles Table
    */
   public void createAssignedRolesTable()
   {
      roleAssignedTable = creatRolesTable(false);
   }

   /**
    * Create AssignableRoles Table
    */
   public void createAssignableRolesTable()
   {
      roleAssignableTable = creatRolesTable(true);
   }
   /**
    * Create Activity Table
    */
   public void createActivityTable()
   {      
      if (activityHelper == null)
      {
         activityHelper = new ActivityTableHelper();
         activityHelper.setStrandedActivityView(false);
         activityHelper.setCallbackHandler(this);
         activityHelper.initActivityTable();
         activityHelper.getColumnModelListener().setNeedRefresh(false);     
         activityHelper.getActivityTable().initialize();
         activityHelper.getColumnModelListener().setNeedRefresh(true);
         
         UserActivitySearchHandler userActivitySearchHandler = new UserActivitySearchHandler(userOid);
         activityHelper.getActivityTable().setISearchHandler(userActivitySearchHandler);
         
         refreshActivityTab();
      }
   }
   
   /**
    * 
    */
   public void refreshActivityTab()
   {
      activityHelper.getActivityTable().refresh(true);
   }

   /**
    * Refreshes the bean
    */
   public void update()
   {
      initialize();
      refreshActivityTab();
   }

   /**
    * 
    */
   @Override
   public void initialize()
   {
      try
      {
         userItem = facade.getUserItem(this.userOid);
         user = facade.getUserItem(this.userOid);         
      }
      catch (Exception e)
      {
         trace.error(e);
      }
      this.userFullName = I18nUtils.getUserLabel(userItem.getUser());
      this.userAccount = userItem.getUser().getAccount();
      this.userEmailId = userItem.getUser().getEMail();
      this.directCountItem = Long.toString(userItem.getDirectItemCount());
      this.inDirectCountItem = Long.toString(userItem.getIndirectItemCount());
      this.roleCount = StringUtils.isEmpty(roleCount) ? Long.toString(userItem.getRoleCount()) : roleCount;
      
      initUserItem(user);//TODO: check need to this statement
      
      SessionContext context = SessionContext.findSessionContext();
      if (context != null)
      {
         context.bind(BusinessControlCenterConstants.USER_DETAILS_ASSIGNABLE_ROLE_MODEL, null);
         context.bind(BusinessControlCenterConstants.USER_DETAILS_ASSIGNED_ROLE_MODEL, null);
         context.bind(BusinessControlCenterConstants.USER_DETAILS_ACTIVITIES_MODEL, null);
      }   
      refreshRoleTables();
      
      if (null != activityHelper)
      {
         activityHelper.refreshActivityTable();
      }
   } 

   /**
    * @param userItem
    */
   private void initUserItem(UserItem userItem)
   {
      if (userItem != null && !UserDetailsLevel.Full.equals(userItem.getUser().getDetailsLevel()))
      {
         UserQuery query = UserQuery.findAll();
         query.getFilter().add(UserQuery.OID.isEqual(userItem.getUser().getOID()));
         try
         {
            List<User> users = facade.getAllUsers(query);
            User u = CollectionUtils.isNotEmpty(users) ? users.get(0) : null;
            if (u != null)
            {
               userItem.setUser(u);
            }
         }
         catch (InvalidServiceException e)
         {
            trace.error(e);
         }
      }
   }

   /**
    * @return
    */
   public List<RoleItem>  getAssignedRoles(UserItem user)
   {
      try
      {
         List <RoleItem> roles =facade.getAllRoles();
         List<Grant> grants = user.getUser().getAllGrants();
         roles.retainAll(grants);
         return roles;
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }

   /**
    * @return
    */
   public List<RoleItem> getAssignableRoles()
   {
      try
      {
         List<RoleItem> roles = facade.getAllRoles();
         List<Grant> grants = user.getUser().getAllGrants();
         roles.removeAll(grants);
         return roles;
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }

   /**
    * 
    */
   public List<UserManagerRoleAssignmentUserObject> getAssignedRolesTableValues()
   {
      List<RoleItem> rolesAssigned = getAssignedRoles(user);
      List<UserManagerRoleAssignmentUserObject> roleAssignedList = CollectionUtils.newArrayList();
      if (rolesAssigned != null)
      {
         for (RoleItem roleItem : rolesAssigned)
         {
            roleAssignedList.add(new UserManagerRoleAssignmentUserObject(roleItem.getRoleName(), roleItem.getRole(),
                  roleItem.getWorklistCount(), roleItem.getUserCount(), roleItem.getEntriesPerUser(), false));
         }
      }
      return roleAssignedList;
   }

   /**
    * 
    */
   public List<UserManagerRoleAssignmentUserObject> getAssignableRolesTableValues()
   {
      List<RoleItem> rolesAssignable = getAssignableRoles();
      List<UserManagerRoleAssignmentUserObject> roleAssignableList = CollectionUtils.newArrayList();

      if (rolesAssignable != null)
      {
         for (RoleItem roleItem : rolesAssignable)
         {
            roleAssignableList.add(new UserManagerRoleAssignmentUserObject(roleItem.getRoleName(), roleItem.getRole(),
                  roleItem.getWorklistCount(), roleItem.getUserCount(), roleItem.getEntriesPerUser(), false));

         }
      }
      return roleAssignableList;
   }

   /**
    * After delegation, handles refreshing current page
    */
   public void handleEvent(EventType eventType)
   {
      if (eventType == EventType.APPLY)
      {
         initialize();
         refreshActivityTab();
      }
      else if (eventType == EventType.CANCEL)
      {

      }
   }

   /**
    * Returns selected items count for "Assignable Roles Table"
    * 
    * @return
    */
   public int getAssignableRolesTableSelectedCount()
   {
      List<UserManagerRoleAssignmentUserObject> selectedList = getSelectedRows(roleAssignableTable.getList());
      return selectedList.size();
   }
   
   /**
    * Returns selected items count for "Assigned Roles Table"
    * 
    * @return
    */
   public int getAssignedRolesTableSelectedCount()
   {
      List<UserManagerRoleAssignmentUserObject> selectedList = getSelectedRows(roleAssignedTable.getList());
      return selectedList.size();
   }
   
   /**
    * 
    * @param list
    * @return
    */
   private List<UserManagerRoleAssignmentUserObject> getSelectedRows(
         List<UserManagerRoleAssignmentUserObject> list)
   {
      List<UserManagerRoleAssignmentUserObject> selectedList = CollectionUtils.newArrayList();
      for (UserManagerRoleAssignmentUserObject userObject : list)
      {
         if (userObject.isSelect())
         {
            selectedList.add(userObject);
         }

      }
      return selectedList;
   }

   /**
    * Adds selected roles to User
    */
   public void addRoleToUser()
   {
      try
      {
         // Does logged-in user have "Manage Authorization" declarative security?
         if (isManageAuthorization())
         {
            List<RoleItem> roles = CollectionUtils.newArrayList();

            List<UserManagerRoleAssignmentUserObject> rolesList = roleAssignableTable.getList();
            for (UserManagerRoleAssignmentUserObject userObject : rolesList)
            {
               if (userObject.isSelect())
               {
                  roles.add(facade.getRoleItem(userObject.getModelParticipantInfo()));
               }

            }

            // If logged-in user has at least 1 non-team lead grant, then all participants
            // are modifiable
            if (UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()))
            {
               if (CollectionUtils.isNotEmpty(roles) && (facade.addRolesToUser(user, roles) > 0))
               {
                  roleCount = Integer.toString(getAssignedRoles(user).size());
                  initialize();
               }
            }
            else
            // Else logged-in user is a team lead
            {
               // Only "team participants" are modifiable
               List<RoleItem> rolesToAdd = getTeamsRoles(roles);
               if (CollectionUtils.isNotEmpty(rolesToAdd) && (facade.addRolesToUser(user, rolesToAdd) > 0))
               {
                  roleCount = Integer.toString(getAssignedRoles(user).size());
                  initialize();
               }
            }
         }
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
   }   

   /**
    * 
    */
   public void removeRoleFromUser()
   {
      try
      {
         // Does logged-in user have "Manage Authorization" declarative security?
         if (isManageAuthorization())
         {
            List<RoleItem> roles = CollectionUtils.newArrayList();
            List<UserManagerRoleAssignmentUserObject> rolesList = roleAssignedTable.getList();
            
            for (UserManagerRoleAssignmentUserObject userObject : rolesList)
            {
               if (userObject.isSelect())
               {
                  roles.add(facade.getRoleItem(userObject.getModelParticipantInfo()));
               }
            }

            // If logged-in user has at least 1 non-team lead grant, then all participants
            // are modifiable
            if (UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()))
            {
               if (CollectionUtils.isNotEmpty(roles) && (facade.removeRolesFromUser(user, roles) > 0))
               {
                  roleCount = Integer.toString(getAssignedRoles(user).size());
                  initialize();
               }
            }
            else
            // Else logged-in user is a team lead
            {
               // Only "team participants" are modifiable
               List<RoleItem> rolesToremove = getTeamsRoles(roles);
               if (CollectionUtils.isNotEmpty(rolesToremove) && (facade.removeRolesFromUser(user, rolesToremove) > 0))
               {
                  roleCount = Integer.toString(getAssignedRoles(user).size());
                  initialize();
               }
            }
         }
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
   }

   /**
    * Called when the table binding's tab focus changes.
    * 
    * @param tabChangeEvent
    *           used to set the tab focus.
    * @throws AbortProcessingException
    *            An exception that may be thrown by event listeners to terminate the
    *            processing of the current event.
    */
   public void processTabChange(TabChangeEvent tabChangeEvent) throws AbortProcessingException
   {
      selectedTabIndex = tabChangeEvent.getNewTabIndex();
      if (selectedTabIndex == 1)
      {
         createActivityTable();
      }
   }   

  /* public boolean isOpened()
   {
      return opened;
   }

   public void setOpened(boolean opened)
   {
      this.opened = opened;
   }

  
   
   public void open()
   {
      opened = true;
   }

   
   public void close()
   {
      opened = false;
      ProcessResourceMgmtBean.getCurrent().initialize();
   }*/
  
   /**
    * Returns true if Participant is part of Team
    * 
    * @param user
    * @param participantList
    * @return
    */
   public boolean isParticipantPartofTeam()
   {
      return (UserUtils.isParticipantPartofTeam(facade.getLoginUser(), getUserParticipantList()));
   }
   
   /**
    * retrieves user participant list
    * 
    * @return
    */
   private List<QualifiedModelParticipantInfo> getUserParticipantList()
   {
      List<QualifiedModelParticipantInfo> participantList = CollectionUtils.newArrayList();
      List<RoleItem> roles = getAssignedRoles(user);
      for (RoleItem roleItem:roles)
      {        
         participantList.add(roleItem.getRole());
      }
      return participantList;
   }

   /**
    * Returns team's role from selected roles
    * 
    * @param roles
    * @return
    */
   private List<RoleItem> getTeamsRoles(List<RoleItem> roles)
   {
      List<RoleItem> rolesToAdd = CollectionUtils.newArrayList();
      
      List<QualifiedModelParticipantInfo> tempList = null;
      for (RoleItem roleItem : roles)
      {
         QualifiedModelParticipantInfo modelParticipantInfo=roleItem.getRole();
         tempList = CollectionUtils.newArrayList();
         tempList.add(modelParticipantInfo);
         if (UserUtils.isParticipantPartofTeam(facade.getLoginUser(), tempList))
         {
            rolesToAdd.add(facade.getRoleItem(modelParticipantInfo));
         }        
      }
     
      return rolesToAdd;
   }

   public Long getUserOid()
   {
      return userOid;
   }

   public void setUserOid(Long userOid)
   {
      this.userOid = userOid;
   }

   public String getUserFullName()
   {
      return userFullName;
   }

   public void setUserFullName(String userFullName)
   {
      this.userFullName = userFullName;
   }

   public String getUserAccount()
   {
      return userAccount;
   }

   public void setUserAccount(String userAccount)
   {
      this.userAccount = userAccount;
   }

   public String getUserEmailId()
   {
      return userEmailId;
   }

   public void setUserEmailId(String userEmailId)
   {
      this.userEmailId = userEmailId;
   }

   public String getDirectCountItem()
   {
      return directCountItem;
   }

   public void setDirectCountItem(String directCountItem)
   {
      this.directCountItem = directCountItem;
   }

   public String getInDirectCountItem()
   {
      return inDirectCountItem;
   }

   public void setInDirectCountItem(String inDirectCountItem)
   {
      this.inDirectCountItem = inDirectCountItem;
   }

   public String getRoleCount()
   {
      return roleCount;
   }

   public void setRoleCount(String roleCount)
   {
      this.roleCount = roleCount;
   }

   public PaginatorDataTable<UserManagerRoleAssignmentUserObject,UserManagerRoleAssignmentUserObject> getRoleAssignedTable()
   {
      return roleAssignedTable;
   }

   public PaginatorDataTable<UserManagerRoleAssignmentUserObject,UserManagerRoleAssignmentUserObject> getRoleAssignableTable()
   {
      return roleAssignableTable;
   }   

   /**
    * @returns the currently selected tab index (Role Assignment / Activity List)
    */
   public int getSelectedTabIndex()
   {
      return selectedTabIndex;
   }
   

   public void setSelectedTabIndex(int selectedTabIndex)
   {
      this.selectedTabIndex = selectedTabIndex;
   }

   public boolean isDisableAddRole()
   {
      if (getAssignableRoles().size() > 0)
         disableAddRole = false;
      else
         disableAddRole = true;
      return disableAddRole;
   }

   public boolean isDisableRemoveRole()
   {
      if (getAssignedRoles(user).size() > 0)
         disableRemoveRole = false;
      else
         disableRemoveRole = true;
      return disableRemoveRole;
   }
   
   public ActivityTableHelper getActivityHelper()
   {
      return activityHelper;
   }

   /**
    * Returns true if current user has rights
    * 
    * @return
    */
   public boolean isManageAuthorization()
   {
      return canManageAuthorization;
   }

   public UserManagerRoleAssignmentUserObject createUserObject(Object resultRow)
   {
      return (UserManagerRoleAssignmentUserObject)resultRow;
   }
   
   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   private class UserManagerRoleAssignmentSearchHandler extends IppSearchHandler<UserManagerRoleAssignmentUserObject>
   {
      private boolean assignableSearch;

      private static final long serialVersionUID = -3543070769771871255L;

      /**
       * 
       */
      @Override
      public Query createQuery()
      {
         return null;// No query for engine call
      }

      /**
       * 
       */
      @Override
      public QueryResult<UserManagerRoleAssignmentUserObject> performSearch(Query query)
      {
         return null;
      }

      /**
       * 
       */
      @Override
      public IQueryResult<UserManagerRoleAssignmentUserObject> performSearch(IQuery iQuery, int startRow, int pageSize)
      {
         List<UserManagerRoleAssignmentUserObject> result = assignableSearch
               ? getAssignableRolesTableValues()
               : getAssignedRolesTableValues();

         applySorting(result);

         RawQueryResult<UserManagerRoleAssignmentUserObject> queryResult = new RawQueryResult<UserManagerRoleAssignmentUserObject>(
               result, null, false, Long.valueOf(result.size()));

         return new IppQueryResult<UserManagerRoleAssignmentUserObject>(queryResult);
      }

      /**
       * 
       * @param assignableSearch
       */
      public void setAssignableSearch(boolean assignableSearch)
      {
         this.assignableSearch = assignableSearch;
      }

      /**
       * 
       * @param result
       */
      private void applySorting(List<UserManagerRoleAssignmentUserObject> result)
      {
         ISortHandler sortHandler = assignableSearch ? roleAssignableTable.getISortHandler() : roleAssignedTable
               .getISortHandler();
         UserManagerRoleAssignmentSortHandler roleAssignmentSortHandler = (UserManagerRoleAssignmentSortHandler) sortHandler;

         if (null != roleAssignmentSortHandler.getSortCriterion())
         {
            SortCriterion sortCriterion = roleAssignmentSortHandler.getSortCriterion();
            Comparator<UserManagerRoleAssignmentUserObject> comparator = new SortableTableComparator<UserManagerRoleAssignmentUserObject>(
                  sortCriterion.getProperty(), sortCriterion.isAscending());
            Collections.sort(result, comparator);
         }
      }

   }
   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   private class UserManagerRoleAssignmentSortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = -2562964400250132610L;

      private SortCriterion sortCriterion;

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriterias)
      {

      }
      
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
    * @author Subodh.Godbole
    * 
    */
   private class UserActivitySearchHandler extends IppSearchHandler<ActivityInstance>
   {
      private static final long serialVersionUID = 1L;

      private long userOid;

      public UserActivitySearchHandler(long userOid)
      {
         this.userOid = userOid;
      }

      @Override
      public Query createQuery()
      {
         ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
               ActivityInstanceState.Application, ActivityInstanceState.Created, ActivityInstanceState.Hibernated,
               ActivityInstanceState.Interrupted, ActivityInstanceState.Suspended});
         query.getFilter().add(new PerformingUserFilter(userOid));
         return query;
      };

      @Override
      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         try
         {
            QueryResult<ActivityInstance> result = facade.getAllActivityInstances((ActivityInstanceQuery) query);
            processInstances = ProcessInstanceUtils.getProcessInstancesAsMap(result, true);
            activityHelper.setProcessInstanceMap(processInstances);
            return result;
         }
         catch (InvalidServiceException e)
         {
            PageMessage.setMessage(e);
         }
         return null;
      }
   }   
   
}
