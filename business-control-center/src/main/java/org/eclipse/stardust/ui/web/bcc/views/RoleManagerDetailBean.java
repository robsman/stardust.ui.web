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

import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.RawQueryResult;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.InvalidServiceException;
import org.eclipse.stardust.ui.web.bcc.jsf.PageMessage;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.ISortHandler;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQueryResult;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

import com.icesoft.faces.component.paneltabset.TabChangeEvent;


/**
 * @author Giridhara.G
 * @version $Revision: $
 */

public class RoleManagerDetailBean extends UIComponentBean
      implements ICallbackHandler, ResourcePaths, ViewEventHandler,IUserObjectBuilder<RoleManagerDetailUserObject>
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(RoleManagerDetailBean.class);

   public final static String BEAN_ID = "roleManagerDetail";

   private boolean opened;

   private String roleName;

   private String roleId;

   private String items;

   private String account;

   private String itemsPerUser;

   private boolean roleModifiable;

   private boolean onlyLoggedInUserVisibleOnAssigned;

   private boolean onlyLoggedInUserVisibleOnAssignable;

   private MessagesBCCBean propsBean;

   private RoleItem roleItem;

   private List<UserItem> assignedUserList;

   private PaginatorDataTable<RoleManagerDetailUserObject,RoleManagerDetailUserObject> userAssignedTable;

   private PaginatorDataTable<RoleManagerDetailUserObject,RoleManagerDetailUserObject> userAssignableTable;

   private boolean selectAllAssignedUsers = false;

   private boolean selectAllAssignableUsers = false;

   private boolean disableAddUser;

   private boolean disableRemoveUser;

   private boolean disableDelegate = true;

   private WorkflowFacade facade;   

   private int selectedTabIndex = 0;


   private QualifiedModelParticipantInfo modelParticipantInfo;
   private View thisView;

   private ActivityTableHelper activityHelper;
   private Map<Long, ProcessInstance> processInstances;

   /**
    * Constructor
    */
   public RoleManagerDetailBean()
   {
      super(V_roleManagerDetailView);
   }
   
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())      
      {
         this.thisView = event.getView();
         propsBean = MessagesBCCBean.getInstance();
         facade = WorkflowFacade.getWorkflowFacade(); 
         createAssignedUsersTable();
         createAssignableUsersTable();
      }
      else if(ViewEventType.ACTIVATED == event.getType())
      {
         String roleId = thisView.getParamValue("roleId");
         String departmentOid = thisView.getParamValue("departmentOid");
         
         
         ModelParticipant participant = (ModelParticipant) ModelCache.findModelCache().getParticipant(roleId);
         Department department = facade.getAdministrationService().getDepartment(Long.parseLong(departmentOid));
         modelParticipantInfo =(QualifiedModelParticipantInfo)( (department == null) ? participant : department.getScopedParticipant(participant));
         roleItem = facade.getRoleItem(modelParticipantInfo);

         onlyLoggedInUserVisibleOnAssigned = true;
         onlyLoggedInUserVisibleOnAssignable = false; 
         initialize();        
      }
   }

   /**
    * Used to Initialize the Role Assigned and Assignable Table
    */
   private void refreshRoleTables()
   {
      userAssignedTable.refresh(true);
      userAssignableTable.refresh(true);
   }
   /**
    * @return
    */
   public static RoleManagerDetailBean getInstance()
   {
      return (RoleManagerDetailBean) FacesUtils.getBeanFromContext(BEAN_ID);
   }
   
   private PaginatorDataTable<RoleManagerDetailUserObject,RoleManagerDetailUserObject> creatRolesTable( boolean assignableSearch)
   {
      
      RoleManagerDetailSearchHandler searchHandler=new RoleManagerDetailSearchHandler();
      searchHandler.setAssignableSearch(assignableSearch);
      
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colName = new ColumnPreference("Name", "userName", ColumnDataType.STRING, this.getMessages()
            .getString("column.name"));

      ColumnPreference colRoleItems = new ColumnPreference("RoleCount", "roleCount", ColumnDataType.STRING, propsBean
            .getString("views.resourceAvailabilityView.accountTable.column.rolesCount"));
      colRoleItems.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colDirectItemCount = new ColumnPreference("DirectItemCount", "directItemCount",
            ColumnDataType.STRING, propsBean
                  .getString("views.resourceAvailabilityView.accountTable.column.directItems"));
      colDirectItemCount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colInDirectItemCount = new ColumnPreference("InDirectItemCount", "inDirectItemCount",
            ColumnDataType.STRING, propsBean
                  .getString("views.resourceAvailabilityView.accountTable.column.indirectItems"));
      colInDirectItemCount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colTotalItemCount = new ColumnPreference("TotalItemCount", "totalItemCount",
            ColumnDataType.STRING, propsBean
                  .getString("views.resourceAvailabilityView.accountTable.column.totalItemsCount"));
      colTotalItemCount.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colLoggedIn = new ColumnPreference("LoggedIn", "loggedIn", ColumnDataType.STRING, propsBean
            .getString("views.resourceAvailabilityView.accountTable.column.loggedIn"));
      colLoggedIn.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colName);
      cols.add(colRoleItems);
      cols.add(colDirectItemCount);
      cols.add(colInDirectItemCount);
      cols.add(colTotalItemCount);
      cols.add(colLoggedIn);
      DefaultColumnModelEventHandler columnModelEventHandler = new DefaultColumnModelEventHandler();
     
      String viewId = assignableSearch
            ? UserPreferencesEntries.V_USER_ASSIGNABLE
            : UserPreferencesEntries.V_USER_ASSIGNED;
      
      IColumnModel columnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_BCC, viewId,
            columnModelEventHandler);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);
      ISortHandler sortHandler = new RoleManagerDetailSortHandler();

      PaginatorDataTable<RoleManagerDetailUserObject, RoleManagerDetailUserObject> table = new PaginatorDataTable<RoleManagerDetailUserObject, RoleManagerDetailUserObject>(
            colSelecpopup, searchHandler, null, sortHandler, this, new DataTableSortModel<RoleManagerDetailUserObject>(
                  "userName", false));
      table.setISearchHandler(searchHandler);
      table.setISortHandler(sortHandler);
      table.setRowSelector(new DataTableRowSelector("select", true));

      columnModelEventHandler.setNeedRefresh(false);
      table.initialize();
      columnModelEventHandler.setNeedRefresh(true);
      return table;
   }

   /**
    * Create AssignedRoles Table
    */
   public void createAssignedUsersTable()
   {
      userAssignedTable = creatRolesTable(false);
   }

   /**
    * Create AssignableRoles Table
    */
   public void createAssignableUsersTable()
   {
      userAssignableTable = creatRolesTable(true);
   }

   /**
    * 
    */
   public void createActivityTable()
   {
      if (null == activityHelper)
      {
         activityHelper = new ActivityTableHelper();
         activityHelper.setCallbackHandler(this);
         activityHelper.initActivityTable();
         activityHelper.setStrandedActivityView(false);
         activityHelper.getActivityTable().initialize();
         
         ParticipantActivitySearchHandler participantActivitySearchHandler = new ParticipantActivitySearchHandler();
         activityHelper.getActivityTable().setISearchHandler(participantActivitySearchHandler);
         activityHelper.getActivityTable().refresh(true);
         
      }

   }

   /**
    * Refreshes the bean
    */
   public void update()
   {
      initialize();
   }

   /**
    * 
    */
   @Override
   public void initialize()
   {
      try
      {
         this.roleName = roleItem.getRoleName().toString() != "" ? roleItem.getRoleName().toString() : roleName;
         this.roleId = roleItem.getRole().getId() != "" ? roleItem.getRole().getId() : roleId;
         this.items = Long.toString(roleItem.getWorklistCount()) != ""
               ? Long.toString(roleItem.getWorklistCount())
               : items;
         this.account = Long.toString(roleItem.getUserCount()) != "" ? Long.toString(roleItem.getUserCount()) : account;
         this.itemsPerUser = Long.toString(roleItem.getEntriesPerUser()) != null ? Long.toString(roleItem
               .getEntriesPerUser()) : itemsPerUser;
         setRoleModifiable(canUserModifyRole());
      }
      catch (Exception e)
      {
        trace.error(e);
      }
      refreshRoleTables();
      
      if (null != activityHelper)
      {
         activityHelper.refreshActivityTable();
      }
   }

   /**
    * @return AssignedUser List
    */
   private List<UserItem> getAssignedUser()
   {
      try
      {
         UserQuery query = UserQuery.findAll();
         query.getFilter().add(ParticipantAssociationFilter.forParticipant(roleItem.getRole(), false));
         query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
         assignedUserList = facade.getAllUsersAsUserItems(query);
         return filterUserList(assignedUserList, onlyLoggedInUserVisibleOnAssigned);
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }

   /**
    * @return AssignableUser List
    */
   private List<UserItem> getAssignableUser()
   {
      try
      {
         if (null == assignedUserList)
         {
            assignedUserList = getAssignedUser();
         }       
         
         UserQuery query = UserQuery.findActive();
         FilterAndTerm filter = query.getFilter();
         
         if (CollectionUtils.isNotEmpty(assignedUserList))
         {
            Iterator<UserItem> euIter = assignedUserList.iterator();
            while (euIter.hasNext())
            {
               UserItem userItem = euIter.next();
               filter.add(UserQuery.OID.notEqual(userItem.getUser().getOID()));
            }
         }
         query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
         return filterUserList(facade.getAllUsersAsUserItems(query), onlyLoggedInUserVisibleOnAssignable);
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
      return null;
   }

   /**
    * @param users
    * @param onlyLoggedInUserVisible
    * @return
    */
   private List<UserItem> filterUserList(List<UserItem>users, boolean onlyLoggedInUserVisible)
   {
      if (users != null && onlyLoggedInUserVisible)
      {
         List<UserItem> filteredUser = CollectionUtils.newArrayList();
         for (Iterator<UserItem> userIter = users.iterator(); userIter.hasNext();)
         {
            UserItem user = userIter.next();
            if (user.isLoggedIn())
            {
               filteredUser.add(user);
            }
         }
         return filteredUser;
      }
      return users;
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

   /**
    * 
    */
   private List<RoleManagerDetailUserObject> getAssignedUsersTableValue()
   {
      List usersAssigned = getAssignedUser();
      UserItem userItem;
      List<RoleManagerDetailUserObject> userAssignedList = new ArrayList<RoleManagerDetailUserObject>();

      if (usersAssigned != null && usersAssigned.size() > 0)
      {
         for (int i = 0; i < usersAssigned.size(); i++)
         {
            userItem = (UserItem) usersAssigned.get(i);
            if (selectAllAssignedUsers)
            {
               userAssignedList.add(new RoleManagerDetailUserObject(userItem.getUserName(), Long.toString(userItem
                     .getUser().getOID()), Long.toString(userItem.getDirectItemCount()), Long.toString(userItem
                     .getIndirectItemCount()), Long.toString(userItem.getIndirectItemCount()
                     + userItem.getDirectItemCount()), userItem.isLoggedIn(), Long.toString(userItem.getRoleCount()),
                     userItem, true));
            }
            else
            {
               userAssignedList.add(new RoleManagerDetailUserObject(userItem.getUserName(), Long.toString(userItem
                     .getUser().getOID()), Long.toString(userItem.getDirectItemCount()), Long.toString(userItem
                     .getIndirectItemCount()), Long.toString(userItem.getIndirectItemCount()
                     + userItem.getDirectItemCount()), userItem.isLoggedIn(), Long.toString(userItem.getRoleCount()),
                     userItem, false));
            }

         }
      }
      return userAssignedList;       
   }

   /**
    * 
    */
   private List<RoleManagerDetailUserObject> getAssignableUsersTableValue()
   {
      List usersAssignable = getAssignableUser();
      List<RoleManagerDetailUserObject> userAssignableList = new ArrayList<RoleManagerDetailUserObject>();
      UserItem userItem;
      if (usersAssignable != null && usersAssignable.size() > 0)
      {
         for (int i = 0; i < usersAssignable.size(); i++)
         {
            userItem = (UserItem) usersAssignable.get(i);
            if (selectAllAssignableUsers)
            {
               userAssignableList.add(new RoleManagerDetailUserObject(userItem.getUserName(), Long.toString(userItem
                     .getUser().getOID()), Long.toString(userItem.getDirectItemCount()), Long.toString(userItem
                     .getIndirectItemCount()), Long.toString(userItem.getIndirectItemCount()
                     + userItem.getDirectItemCount()), userItem.isLoggedIn(), Long.toString(userItem.getRoleCount()),
                     userItem, true));

            }
            else
            {
               userAssignableList.add(new RoleManagerDetailUserObject(userItem.getUserName(), Long.toString(userItem
                     .getUser().getOID()), Long.toString(userItem.getDirectItemCount()), Long.toString(userItem
                     .getIndirectItemCount()), Long.toString(userItem.getIndirectItemCount()
                     + userItem.getDirectItemCount()), userItem.isLoggedIn(), Long.toString(userItem.getRoleCount()),
                     userItem, false));
            }
         }
      }     
      return userAssignableList;
   }

   /**
    * @param event
    */
   public void showAllUserAssignedChangeListener(ValueChangeEvent event)
   {
      if (event.getNewValue() != null)
      {
         if (event.getNewValue().toString().equals("false"))
         {
            onlyLoggedInUserVisibleOnAssigned = false;
         }
         else
         {
            onlyLoggedInUserVisibleOnAssigned = true;
         }
         initialize();
      }
   }

   /**
    * @param event
    */
   public void showAllUserAssignableChangeListener(ValueChangeEvent event)
   {
      if (event.getNewValue() != null)
      {
         if (event.getNewValue().toString().equals("false"))
         {
            onlyLoggedInUserVisibleOnAssignable = false;
         }
         else
         {
            onlyLoggedInUserVisibleOnAssignable = true;
         }
         initialize();
      }
   }

   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getAddUser()
   {
      int count = 0;
      List<RoleManagerDetailUserObject> rolesList = userAssignableTable.getList();
      for (RoleManagerDetailUserObject roleManagerDetailUserObject : rolesList)
      {
         if (roleManagerDetailUserObject.isSelect())
            count++;
      }
      return count;
   }

   /**
    * adds selected users to role
    */
   public void addUserToRole()
   {
      List<UserItem> users = CollectionUtils.newArrayList();
      List<RoleManagerDetailUserObject> rolesList = userAssignableTable.getList();

      for (Iterator<RoleManagerDetailUserObject> iterator = rolesList.iterator(); iterator.hasNext();)
      {
         RoleManagerDetailUserObject roleManagerDetailUserObject = (RoleManagerDetailUserObject) iterator.next();
         if (roleManagerDetailUserObject.isSelect())
         {
            users.add(facade.getUserItem(Long.parseLong(roleManagerDetailUserObject.getUserOid().toString())));
         }

      }
      try
      {
         if (facade.addUserToRole(roleItem, users) > 0)
         {
            items = Integer.toString(getAssignedUser().size());
            initialize();
         }
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
   }

   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getRemoveUser()
   {
      int count = 0;
      List<RoleManagerDetailUserObject> rolesList = userAssignedTable.getList();
      for (RoleManagerDetailUserObject roleManagerDetailUserObject : rolesList)
      {
         if (roleManagerDetailUserObject.isSelect())
            count++;
      }
      return count;
   }

   /**
    * 
    */
   public void removeUserFromRole()
   {
      List<UserItem> users = CollectionUtils.newArrayList();
      UserItem userItem = null;
      List<RoleManagerDetailUserObject> rolesList = userAssignedTable.getList();

      for (Iterator<RoleManagerDetailUserObject> iterator = rolesList.iterator(); iterator.hasNext();)
      {
         RoleManagerDetailUserObject roleManagerDetailUserObject = (RoleManagerDetailUserObject) iterator.next();
         if (roleManagerDetailUserObject.isSelect())
         {

            if (facade.getUserItem(Long.parseLong(roleManagerDetailUserObject.getUserOid().toString())).getUser()
                  .equals(facade.getLoginUser()))
            {
               userItem = facade.getUserItem(facade.getLoginUser());
            }
            else
            {
               users.add(facade.getUserItem(Long.parseLong(roleManagerDetailUserObject.getUserOid().toString())));
            }
         }

      }

      if (userItem != null)
      {
         users.add(userItem);
      }

      try
      {
         if (facade.removeUserFromRole(roleItem, users) > 0)
         {
            items = Integer.toString(getAssignedUser().size());
            initialize();
         }
      }
      catch (InvalidServiceException e)
      {
         PageMessage.setMessage(e);
      }
   }
 
   public boolean isOpened()
   {
      return opened;
   }

   public void setOpened(boolean opened)
   {
      this.opened = opened;
   }

   /**
    * 
    */
   public void open()
   {
      opened = true;
   }

   /**
    * 
    */
   public void close()
   {
      opened = false;
      ProcessResourceMgmtBean.getCurrent().initialize();
   }

   /**
    * @param event
    */
  /* public void activitiesSelectedChangeListener(ValueChangeEvent event)
   {
      String activityOid = (String) event.getComponent().getAttributes().get("activityOid");
      if (event.getNewValue() != null)
      {
         if (activityOid != null)
         {
            if (activitiesSelectedList != null)
            {
               activitiesSelectedList.put(activityOid, event.getNewValue().toString());
            }
         }
      }
   }*/   

   public PaginatorDataTable<RoleManagerDetailUserObject, RoleManagerDetailUserObject> getUserAssignedTable()
   {
      return userAssignedTable;
   }

   public PaginatorDataTable<RoleManagerDetailUserObject, RoleManagerDetailUserObject> getUserAssignableTable()
   {
      return userAssignableTable;
   }

   public boolean isOnlyLoggedInUserVisibleOnAssigned()
   {
      return onlyLoggedInUserVisibleOnAssigned;
   }

   public void setOnlyLoggedInUserVisibleOnAssigned(boolean option)
   {
      onlyLoggedInUserVisibleOnAssigned = option;
   }

   public boolean isOnlyLoggedInUserVisibleOnAssignable()
   {
      return onlyLoggedInUserVisibleOnAssignable;
   }

   public void setOnlyLoggedInUserVisibleOnAssignable(boolean option)
   {
      onlyLoggedInUserVisibleOnAssignable = option;
   }

   /**
    * @author Subodh.Godbole
    * 
    */
   public class ParticipantActivitySearchHandler extends IppSearchHandler<ActivityInstance>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
               ActivityInstanceState.Application, ActivityInstanceState.Created, ActivityInstanceState.Hibernated,
               ActivityInstanceState.Interrupted, ActivityInstanceState.Suspended});
         query.getFilter().add(PerformingParticipantFilter.forParticipant(modelParticipantInfo, true));
         return query;
      }

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

  

   public String getItems()
   {
      return items;
   }

   public void setItems(String items)
   {
      this.items = items;
   }

   public String getAccount()
   {
      return account;
   }

   public void setAccount(String account)
   {
      this.account = account;
   }

   public String getItemsPerUser()
   {
      return itemsPerUser;
   }

   public void setItemsPerUser(String itemsPerUser)
   {
      this.itemsPerUser = itemsPerUser;
   }

   public boolean isRoleModifiable()
   {
      return roleModifiable;
   }

   public void setRoleModifiable(boolean roleModifiable)
   {
      this.roleModifiable = roleModifiable;
   }

   public String getRoleName()
   {
      return roleName;
   }

   public void setRoleName(String roleName)
   {
      this.roleName = roleName;
   }

   public String getRoleId()
   {
      return roleId;
   }

   public void setRoleId(String roleId)
   {
      this.roleId = roleId;
   }
   
   public ActivityTableHelper getActivityHelper()
   {
      return activityHelper;
   }

   /**
    * @returns the currently selected tab index (User Assignment / Activity List)
    */
   public int getSelectedTabIndex()
   {
      return selectedTabIndex;
   }
   

   public void setSelectedTabIndex(int selectedTabIndex)
   {
      this.selectedTabIndex = selectedTabIndex;
   }

   public boolean isDisableAddUser()
   {
      if (getAssignableUser().size() > 0)
         disableAddUser = false;
      else
         disableAddUser = true;
      return disableAddUser;
   }

   public boolean isDisableRemoveUser()
   {
      if (getAssignedUser().size() > 0)
         disableRemoveUser = false;
      else
         disableRemoveUser = true;
      return disableRemoveUser;
   }

   public boolean isDisableDelegate()
   {
      return disableDelegate;
   }

   /**
    * After delegation, handles refreshing current page
    */
   public void handleEvent(EventType eventType)
   {
      if (eventType == EventType.APPLY)
      {
         initialize();
      }
      else if (eventType == EventType.CANCEL)
      {

      }
   }

   /**
    * Checks the declarative security
    * 
    * @return
    */
   private boolean canUserModifyRole()
   {
      // Does logged-in user have "Manage Authorization" declarative security?
      if (AuthorizationUtils.canManageAuthorization())
      {
         // return UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()) ? true :
         // (isParticipantPartofTeam() ? true : false);

         // If logged-in user has at least 1 non-team lead grant, then all participants
         // are modifiable
         if (UserUtils.hasNonTeamLeadGrant(facade.getLoginUser()))
         {
            return true;
         }
         else
         // Else logged-in user is a team lead
         {
            // Only "team participants" are modifiable
            if (isParticipantPartofTeam())
            {
               return true;
            }
            else
            {
               return false;
            }
         }
      }
      // Logged-in user does not have "Manage Authorization" declarative security
      else
      {
         return false;
      }
   }

   /**
    * Returns true if Participant is part of Team
    * 
    * @param user
    * @param participantList
    * @return
    */
   private boolean isParticipantPartofTeam()
   {
      List<QualifiedModelParticipantInfo> participantList = new ArrayList<QualifiedModelParticipantInfo>();
      participantList.add(roleItem.getRole());
      return (UserUtils.isParticipantPartofTeam(facade.getLoginUser(), participantList));
   }

   public RoleManagerDetailUserObject createUserObject(Object resultRow)
   {     
      return (RoleManagerDetailUserObject)resultRow;
   }
   
   
   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   private class RoleManagerDetailSearchHandler extends IppSearchHandler<RoleManagerDetailUserObject>
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
      public QueryResult<RoleManagerDetailUserObject> performSearch(Query query)
      {
         return null;
      }

      /**
       * 
       */
      @Override
      public IQueryResult<RoleManagerDetailUserObject> performSearch(IQuery iQuery, int startRow, int pageSize)
      {
         List<RoleManagerDetailUserObject> result = assignableSearch
               ? getAssignableUsersTableValue()
               : getAssignedUsersTableValue();

         applySorting(result);

         RawQueryResult<RoleManagerDetailUserObject> queryResult = new RawQueryResult<RoleManagerDetailUserObject>(
               result, null, false, Long.valueOf(result.size()));

         return new IppQueryResult<RoleManagerDetailUserObject>(queryResult);
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
      private void applySorting(List<RoleManagerDetailUserObject> result)
      {
         ISortHandler sortHandler = assignableSearch ? userAssignableTable.getISortHandler() : userAssignedTable
               .getISortHandler();
         RoleManagerDetailSortHandler roleAssignmentSortHandler = (RoleManagerDetailSortHandler) sortHandler;

         if (null != roleAssignmentSortHandler.getSortCriterion())
         {
            SortCriterion sortCriterion = roleAssignmentSortHandler.getSortCriterion();
            Comparator<RoleManagerDetailUserObject> comparator = new SortableTableComparator<RoleManagerDetailUserObject>(
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
   private class RoleManagerDetailSortHandler extends IppSortHandler
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
   

}
