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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetails;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterDate;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.DataType;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationItem;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationMessage;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.user.TableDataFilterUserName;
import org.eclipse.stardust.ui.web.viewscommon.user.UserProfileBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author ankita.patel
 * @version $Revision: $
 */
public class UserManagementBean extends PopupUIComponentBean
      implements IUserObjectBuilder<UserDetailsTableEntry>,
      ICallbackHandler, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager
   .getLogger(UserManagementBean.class);
   public static final String SHOW_INVALIDATED_USERS = "showInvalidatedUsers"; 

   private WorkflowFacade workflowFacade;

   private PaginatorDataTable<UserDetailsTableEntry, User> userDetailsTable;

   private ActivityInstances activityInstances;
   
   private List<User> invalidatedUsers;
   
   private List<User> skippedUsers;
   
   private IParametricCallbackHandler parametricCallbackHandler;
   
   private List<FilterToolbarItem> userMgmtFilterToolbarItems;
   
   private ConfirmationDialog userMgmtConfirmationDialog;
   
   /**
    * 
    */
   public UserManagementBean()
   {
      super(ResourcePaths.V_participantMgmt);
      workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
            AdminportalConstants.WORKFLOW_FACADE);

      List<ColumnPreference> userMgmtFixedCols = new ArrayList<ColumnPreference>();

      ColumnPreference nameCol = new ColumnPreference("Name", "name", this.getMessages()
            .getString("column.name"), ResourcePaths.V_USERCOLUMNS_VIEW, true, false);

      nameCol.setColumnDataFilterPopup(new TableDataFilterPopup(new TableDataFilterUserName()));

      ColumnPreference oidCol = new ColumnPreference("OID", "OID", ColumnDataType.NUMBER,
            this.getMessages().getString("column.oid"), true, true);

      ColumnPreference accountCol = new ColumnPreference("Account", "account",
            ColumnDataType.STRING, this.getMessages().getString("column.account"),
            new TableDataFilterPopup(new TableDataFilterSearch()), true, true);

      ColumnPreference realmCol = new ColumnPreference("Realm", "realm",
            ColumnDataType.STRING, this.getMessages().getString("column.realm"),
            new TableDataFilterPopup(new TableDataFilterSearch()), true, false);

      ColumnPreference validFromCol = new ColumnPreference("ValidFrom", "validFrom",
            ColumnDataType.DATE, this.getMessages().getString("column.validFrom"),
            new TableDataFilterPopup(new TableDataFilterDate(DataType.DATE)), true,
            false);
      validFromCol.setNoWrap(true);

      ColumnPreference validToCol = new ColumnPreference("ValidTo", "validTo",
            ColumnDataType.DATE, this.getMessages().getString("column.validTo"),
            new TableDataFilterPopup(new TableDataFilterDate(DataType.DATE)), true,
            false);
      validToCol.setNoWrap(true);

      ColumnPreference emailCol = new ColumnPreference("Email", "email",
            ColumnDataType.STRING, this.getMessages().getString("column.email"), true,
            false);

      List<ColumnPreference> userMgmtCols = new ArrayList<ColumnPreference>();
      userMgmtCols.add(nameCol);
      userMgmtCols.add(oidCol);
      userMgmtCols.add(accountCol);
      userMgmtCols.add(realmCol);
      userMgmtCols.add(validFromCol);
      userMgmtCols.add(validToCol);
      userMgmtCols.add(emailCol);

      IColumnModel userMgmtColumnModel = new DefaultColumnModel(userMgmtCols,
            userMgmtFixedCols, null, UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_USER_MGT);
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(
            userMgmtColumnModel);
      
      userDetailsTable = new PaginatorDataTable<UserDetailsTableEntry, User>(colSelecPopup,
            new UserMgmtSearchHandler(), new UserMgmtFilterHandler(), new UserMgmtSortHandler(), this,
            new DataTableSortModel<UserDetailsTableEntry>("OID", false));
      
      userDetailsTable.setRowSelector(new DataTableRowSelector("selectedRow",true));
      userDetailsTable.setDataTableExportHandler(new UserTableExportHandler());
      
      createFilterToolbar();
      userDetailsTable.initialize();
      initialize();
   }

   /**
    * @return
    */
   public static UserManagementBean getCurrent()
   {
      return (UserManagementBean) FacesUtils.getBeanFromContext("userMgmtBean");
   }

   @Override
   public void initialize()
   { 
      userDetailsTable.refresh(true);
   }

   /**
    * Opens Create User of Modify User dialog and passes event parameter
    * 
    * @param ae
    */
   public void openCreateUserDialog(ActionEvent ae)
   {
      UserProfileBean userProfileBean = UserProfileBean.getInstance();
      userProfileBean.setICallbackHandler(this);
      userProfileBean.openCreateUserDialog();
   }

   /**
    * Opens Create User of Modify User dialog and passes event parameter
    * 
    * @param ae
    */
   public void openModifyUserDialog(ActionEvent ae)
   {
      UserProfileBean userProfileBean = UserProfileBean.getInstance();
      userProfileBean.setICallbackHandler(this);
      userProfileBean.openModifyUserDialog(ae);
   }
   
   public void copyUser()
   {
      try
      {
         List<UserDetailsTableEntry> usersList = userDetailsTable.getCurrentList();
         User user = null;
         for (UserDetailsTableEntry userDetailsTableEntry : usersList)
         {
            if (userDetailsTableEntry.isSelectedRow())
            {
               user = userDetailsTableEntry.getUser();
               break;
            }
         }

         if (user != null)
         {
            user = getUserService().getUser(user.getOID());//latest latest user to get latest roles
            UserProfileBean userProfileBean = UserProfileBean.getInstance();
            userProfileBean.setICallbackHandler(new ICallbackHandler()
            {
               public void handleEvent(EventType eventType)
               {
                  initialize();
                  ParticipantManagementBean.getInstance().refreshParticipantTree();
               }
            });
            userProfileBean.openCopyUserDialog(user);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   
   
   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getSelectedItemCount()
   {
      int count = 0;
      List<UserDetailsTableEntry> usersList = userDetailsTable.getCurrentList();
      for (UserDetailsTableEntry userDetailsTableEntry : usersList)
      {
         if (userDetailsTableEntry.isSelectedRow())
            count++;
      }
      return count;
   }

   /**
    * Invalidates selected user
    * 
    * @param ae
    * @throws PortalException
    */
   public void invalidateUser(ActionEvent ae) throws PortalException
   {
      try
      {
         invalidatedUsers = new ArrayList<User>();
         skippedUsers = new ArrayList<User>();
         UserService service = getUserService();

         List<UserDetailsTableEntry> usersList = userDetailsTable.getCurrentList();
         for (Iterator<UserDetailsTableEntry> iterator = usersList.iterator(); iterator
               .hasNext();)
         {
            UserDetailsTableEntry userDetailsTableEntry = (UserDetailsTableEntry) iterator
                  .next();
            if (userDetailsTableEntry.isSelectedRow())
            {
               User user = userDetailsTableEntry.getUser();
               if (user != null && !user.getAccount().equals("motu")
                     && userDetailsTableEntry.getValidTo() == null)
               {                 
                  User u= service.invalidateUser(user.getRealm().getId(),user.getAccount());
                  invalidatedUsers.add(u);
               }
               else
               {
                  skippedUsers.add(user);
               }
            }
         }
         prepareActivitiesforInvalideUsers();
         
         if(activityInstances.getSize() > 0)
         {
            AdminMessagesPropertiesBean propsBean = AdminMessagesPropertiesBean.getInstance();
            // Confirmation Dialog for default delegation of activities
            userMgmtConfirmationDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO, null,
                  DialogStyle.COMPACT, this);
            userMgmtConfirmationDialog.setTitle(propsBean.getString("common.defaultDelegateDialog.title"));
            userMgmtConfirmationDialog.setMessage(propsBean.getString("common.ConfirmDelegate"));
            userMgmtConfirmationDialog.openPopup();
         }
         else
         {
            showNotificationDialog();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * Delegates to default performer
    * @param ae
    */
   public void delegateToDefaultPerformer()
   {
      List<ActivityInstance> ais = new ArrayList<ActivityInstance>();
      if(null != activityInstances)
      {
         for (ActivityInstance ai : activityInstances)
         {
            ais.add(ai);
         }
      }
      ActivityInstanceUtils.delegateToDefaultPerformer(ais);
      prepareActivitiesforInvalideUsers();
      if(activityInstances.size() > 0)
      {
         openStrandedActivitiesAlert();
      }
      else
      {
         showNotificationDialog();
      }
   }
   
   /**
    * Opens stranded activities alert dialog
    */
   public void openStrandedActivitiesAlert()
   {
      AdminMessagesPropertiesBean propsBean = AdminMessagesPropertiesBean.getInstance();
      userMgmtConfirmationDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.OK_CANCEL, DialogType.ACCEPT_ONLY,
            DialogStyle.COMPACT, this);
      userMgmtConfirmationDialog.setTitle(propsBean.getString("common.strandedActivitiesAlert.title"));
      userMgmtConfirmationDialog.setMessage(propsBean.getString("common.strandedActivitiesAlert.message.label"));
      userMgmtConfirmationDialog.openPopup();
   }
   
   /**
    * closes dialog and opens notification dialog
    */
   public void closeStrandedActivitiesAlert()
   {
      showNotificationDialog();
   }
   
   /**
    * 
    */
   public boolean accept()
   {
      if (userMgmtConfirmationDialog.getDialogType() == DialogType.ACCEPT_ONLY)
      {
         closeStrandedActivitiesAlert();
      }
      else
      {
         delegateToDefaultPerformer();
      }
      userMgmtConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      userMgmtConfirmationDialog = null;
      openStrandedActivitiesAlert();
      return true;
   }
   
   /**
    * Returns user details table entry object
    */
   public UserDetailsTableEntry createUserObject(Object resultRow)
   {
      if (resultRow instanceof UserDetails)
      {
         UserDetails ud = (UserDetails) resultRow;
         try
         {
            UserDetailsTableEntry userDetailsTableEntry = new UserDetailsTableEntry(ud, I18nUtils.getUserLabel(ud),
                  ud.getOID(), ud.getAccount(), ud.getRealmId(), ud.getValidFrom(), ud.getValidTo(), ud.getEMail(),
                  false);
            userDetailsTableEntry.setParametricCallbackHandler(parametricCallbackHandler);
            return userDetailsTableEntry;
         }
         catch (Exception e)
         {
            trace.error(e);
            UserDetailsTableEntry userDetailsTableEntry = new UserDetailsTableEntry();
            userDetailsTableEntry.setLoaded(false);
            userDetailsTableEntry.setCause(e);
            return userDetailsTableEntry;
         }
      }
      return null;
   }
   
   /**
    * @param event
    */
   public void applyFilter(ActionEvent event)
   {
      UICommand commandObject = (UICommand) event.getComponent();
      Map<String, Object> attributesMap = commandObject.getAttributes();

      String filterName = (String) attributesMap.get("name");

      // Update Filter on UI
      FilterToolbarItem filterToolbarItem = getFilterToolbarItem(filterName);
      filterToolbarItem.toggle();

      initialize();
   }
   
   /**
    * create table level filter 
    */
   private void createFilterToolbar()
   {
      userMgmtFilterToolbarItems = new ArrayList<FilterToolbarItem>();
      FilterToolbarItem filterToolbarItem = new FilterToolbarItem("0", SHOW_INVALIDATED_USERS,
            "views.participantMgmt.filters.invalidatedUsers.showUser",
            "views.participantMgmt.filters.invalidatedUsers.hideUser",
            "/plugins/admin-portal/images/icons/user-invalidated.png");
      filterToolbarItem.setActive(true);
      userMgmtFilterToolbarItems.add(filterToolbarItem);
   }

   /**
    * @param name
    * @return FilterToolbarItem
    */
   private FilterToolbarItem getFilterToolbarItem(String name)
   {
      for (FilterToolbarItem filterToolbarItem : userMgmtFilterToolbarItems)
      {
         if (filterToolbarItem.getName().equals(name))
         {
            return filterToolbarItem;
         }
      }
      return null;
   }

   /**
    * Apply table level filters
    * 
    * @param UserQuery
    */
   private void applyTableLevelFilters(UserQuery query)
   {
      for (FilterToolbarItem filter : getUserMgmtFilterToolbarItems())
      {
         // Invalidated users filter
         if (filter.getName().equals(UserManagementBean.SHOW_INVALIDATED_USERS))
         {
            if (!filter.isActive())
            {
               query.getFilter().addOrTerm().or(UserQuery.VALID_TO.greaterThan(System.currentTimeMillis()))
                     .or(UserQuery.VALID_TO.isEqual(0));
            }
         }
      }
   }

   /**
    * shows notification dialog
    */
   private void showNotificationDialog()
   {
      NotificationMessageBean nb = NotificationMessageBean.getCurrent();
      NotificationMessage notificationMessage = new NotificationMessage();
      List<NotificationItem> itemsList = new ArrayList<NotificationItem>();
      if (invalidatedUsers != null && !invalidatedUsers.isEmpty())
      {
         notificationMessage.setMessage(this.getMessages().getString(
               "notifySuccessMsg"));
         for (Iterator<User> iterator = invalidatedUsers.iterator(); iterator
               .hasNext();)
         {
            User user = (User) iterator.next();

            itemsList.add(new NotificationItem(user.getAccount(), this.getMessages()
                  .getString("notifyUserInvalidate")));
         }
         notificationMessage.setNotificationItem(itemsList);
         nb.add(notificationMessage);
      }
      notificationMessage = new NotificationMessage();
      itemsList = new ArrayList<NotificationItem>();

      if (skippedUsers != null && !skippedUsers.isEmpty())
      {
         notificationMessage.setMessage(this.getMessages().getString(
               "notifyNonValidateMsg"));
         for (Iterator<User> iterator = skippedUsers.iterator(); iterator.hasNext();)
         {
            User user = (User) iterator.next();
            if (user.getAccount().equals("motu"))
               itemsList.add(new NotificationItem(user.getAccount(), this.getMessages().getString(
                     "notifyMotuNotValidateMsg")));
            else
               itemsList.add(new NotificationItem(user.getAccount(), this.getMessages().getString(
                     "notifyUserCannotBeInvalidatedMsg")));
         }
         notificationMessage.setNotificationItem(itemsList);
         nb.add(notificationMessage);
      }
      nb.openPopup();
      initialize();
      
   }

   /**
    * Retrieves activities assigned to invalidated users
    */
   private void prepareActivitiesforInvalideUsers()
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      FilterTerm filter = aiQuery.getFilter().addOrTerm();
      if(CollectionUtils.isNotEmpty(invalidatedUsers))
      {
         for (User user : invalidatedUsers)
         {
            filter.add(new PerformingUserFilter(user.getOID()));
         }
      }
      else
      {
         filter.add(ActivityInstanceQuery.OID.isEqual(0));
      }
      
      QueryService queryService = ServiceFactoryUtils.getQueryService();
      activityInstances = queryService.getAllActivityInstances(aiQuery);
   }


   /**
    * Returns totalUsersCount
    * 
    * @return long
    * @throws PortalException
    */
   public long getTotalUsersCount() throws PortalException
   {
      if (workflowFacade.getTotalUsersCount() != 0)
         return workflowFacade.getTotalUsersCount();
      else
         return 0;
   }

   /**
    * Returns activeUsersCount
    * 
    * @return long
    * @throws PortalException
    */
   public long getActiveUsersCount() throws PortalException
   {
      if (workflowFacade.getActiveUsersCount() != 0)
         return workflowFacade.getActiveUsersCount();
      else
         return 0;
   }

   // ********************** Modified Getter & Setter Methods **************
   public PaginatorDataTable<UserDetailsTableEntry, User> getUserDetailsTable()
   {
      return userDetailsTable;
   }

   public void setUserDetailsTable(
         PaginatorDataTable<UserDetailsTableEntry, User> userDetailsTable)
   {
      this.userDetailsTable = userDetailsTable;
   }

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
    * @author Subodh.Godbole
    *
    */
   public class UserMgmtSearchHandler extends IppSearchHandler<User>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         UserQuery query = UserQuery.findAll(); 
         applyTableLevelFilters(query);
         return query;
      }

      @Override
      public QueryResult<User> performSearch(Query query)
      {
         try
         {
            return workflowFacade.getAllUsers((UserQuery) query);
         }
         catch (AccessForbiddenException e)
         {
            return null;
         }
      }
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   public class UserMgmtFilterHandler extends IppFilterHandler
   {
      private static final long serialVersionUID = 1L;

      @Override
      public void applyFiltering(Query query, List<ITableDataFilter> filters)
      {
         FilterAndTerm filter = query.getFilter().addAndTerm();

         for (ITableDataFilter tableDataFilter : filters)
         {
            if (tableDataFilter.isFilterSet())
            {
               if ("ValidFrom".equals(tableDataFilter.getName()))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                     filter.and(UserQuery.VALID_FROM.greaterOrEqual(startTime.getTime()));

                  if (endTime != null)
                     filter.and(UserQuery.VALID_FROM.lessOrEqual(endTime.getTime()));
               }
               else if ("ValidTo".equals(tableDataFilter.getName()))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                     filter.and(UserQuery.VALID_TO.greaterOrEqual(startTime.getTime()));

                  if (endTime != null)
                     filter.and(UserQuery.VALID_TO.lessOrEqual(endTime.getTime()));
               }
               else if ("Realm".equals(tableDataFilter.getName()))
               {
                  String filterByValue = ((TableDataFilterSearch) tableDataFilter).getValue();
                  if (filterByValue != null)
                  {
                     if (filterByValue.contains("*"))
                     {
                        filterByValue = filterByValue.replace('*', '%');
                     }
                     filter.and(UserQuery.REALM_ID.like(filterByValue));
                  }
               }
               else if ("Account".equals(tableDataFilter.getName()))
               {
                  String filterByValue = ((TableDataFilterSearch) tableDataFilter).getValue();
                  if (filterByValue != null)
                  {
                     if (filterByValue.contains("*"))
                     {
                        filterByValue = filterByValue.replace('*', '%');
                     }
                     filter.and(UserQuery.ACCOUNT.like(filterByValue));
                  }
               }
               else if ("Name".equals(tableDataFilter.getName()))
               {
                  String fn = ((TableDataFilterUserName) tableDataFilter).getFirstName();
                  String ln = ((TableDataFilterUserName) tableDataFilter).getLastName();
                  if (StringUtils.isNotEmpty(fn) && StringUtils.isNotEmpty(ln))
                  {
                     FilterAndTerm nameAnd = filter.addAndTerm();
                     
                     FilterOrTerm fnOr = nameAnd.addOrTerm();
                     fnOr.add(UserQuery.FIRST_NAME.like(getLikeFilterString(fn)));
                     fnOr.add(UserQuery.FIRST_NAME.like(getLikeFilterStringAltCase(fn)));

                     FilterOrTerm lnOr = nameAnd.addOrTerm();
                     lnOr.add(UserQuery.LAST_NAME.like(getLikeFilterString(ln)));
                     lnOr.add(UserQuery.LAST_NAME.like(getLikeFilterStringAltCase(ln)));

                  }
                  else if (StringUtils.isNotEmpty(fn))
                  {
                     FilterOrTerm or = filter.addOrTerm();
                     or.add(UserQuery.FIRST_NAME.like(getLikeFilterString(fn)));
                     or.add(UserQuery.FIRST_NAME.like(getLikeFilterStringAltCase(fn)));
                  }
                  else if (StringUtils.isNotEmpty(ln))
                  {
                     FilterOrTerm or = filter.addOrTerm();
                     or.add(UserQuery.LAST_NAME.like(getLikeFilterString(ln)));
                     or.add(UserQuery.LAST_NAME.like(getLikeFilterStringAltCase(ln)));
                  }
               }
            }
         }

      }
   }
   
   private String getLikeFilterString(String searchString)
   {
      return "%" + searchString.replace('*', '%') + "%";
   }

   private String getLikeFilterStringAltCase(String searchString)
   {
      return getLikeFilterString(StringUtils.alternateFirstLetterCase(searchString));
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   public class UserMgmtSortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = 1L;

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriteriaList)
      {
         Iterator< ? > iterator = sortCriteriaList.iterator();

         if (iterator.hasNext())
         {
            SortCriterion sortCriterion = (SortCriterion) iterator.next();
            if ("OID".equals(sortCriterion.getProperty()))
            {
               query.orderBy(UserQuery.OID, sortCriterion.isAscending());
            }
            else if ("account".equals(sortCriterion.getProperty()))
            {

               query.orderBy(UserQuery.ACCOUNT, sortCriterion.isAscending());
            }
         }

      }
   }
   
   /**
    * @return
    */
   private UserService getUserService()
   {
      return workflowFacade.getServiceFactory().getUserService();
   }

   public void setParametricCallbackHandler(IParametricCallbackHandler parametricCallbackHandler)
   {
      this.parametricCallbackHandler = parametricCallbackHandler;
   }

   /**
    * @return the userMgmtFilterToolbarItems
    */
   public List<FilterToolbarItem> getUserMgmtFilterToolbarItems()
   {
      return userMgmtFilterToolbarItems;
   }
   
   public ConfirmationDialog getUserMgmtConfirmationDialog()
   {
      return userMgmtConfirmationDialog;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   private class UserTableExportHandler implements DataTableExportHandler<UserDetailsTableEntry>
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object, java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column, UserDetailsTableEntry row,
            Object value)
      {
         if ("Name".equals(column.getColumnName()))
         {
            return row.getName();
         }
         else
         {
            return value;
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleHeaderCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.String)
       */
      public String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text)
      {
         return text;
      }
   }

}
