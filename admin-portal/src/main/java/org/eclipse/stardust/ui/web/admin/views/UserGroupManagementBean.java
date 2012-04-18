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
import java.util.Iterator;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserGroupQuery;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationItem;
import org.eclipse.stardust.ui.web.viewscommon.common.notification.NotificationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class UserGroupManagementBean extends UIComponentBean
      implements IUserObjectBuilder<UserGroupsTableEntry>,
      ICallbackHandler,ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger trace = LogManager
   .getLogger(UserGroupManagementBean.class);

   private WorkflowFacade workflowFacade;

   private AdminMessagesPropertiesBean propsBean;

   private PaginatorDataTable<UserGroupsTableEntry, UserGroup> userGroupsTable;
   


   /**
    * 
    */
   public UserGroupManagementBean()
   {
      super(ResourcePaths.V_userGroupMgmt);
      
   }

   /**
    * @return
    */
   public static UserGroupManagementBean getCurrent()
   {
      return (UserGroupManagementBean) FacesUtils.getBeanFromContext("userGroupMgmtBean");
   }

   /**
    * Updates the changes
    */
   public void update()
   {
      workflowFacade.reset();
      initialize();
   }

   /**
    * Returns selected items count
    * 
    * @return
    */
   public int getSelectedItemCount()
   {
      int count = 0;
      List<UserGroupsTableEntry> groupsList = userGroupsTable.getCurrentList();
      for (UserGroupsTableEntry userGroupsTableEntry : groupsList)
      {
         if (userGroupsTableEntry.isSelectedRow())
            count++;

      }
      return count;
   }

   /**
    * Invalidates selected user group
    * 
    * @param ae
    * @throws PortalException
    */
   public void invalidateUserGroup(ActionEvent ae) throws PortalException
   {
      List<UserGroup> invalidatedUserGroups = new ArrayList<UserGroup>();
      List<UserGroup> skippedUserGroups = new ArrayList<UserGroup>();
      List<NotificationItem> successItemsList = new ArrayList<NotificationItem>();
      List<NotificationItem> failureItemsList = new ArrayList<NotificationItem>();
      
      List<UserGroupsTableEntry> groupsList = userGroupsTable.getCurrentList();

      UserService service = workflowFacade.getServiceFactory().getUserService();

      for (Iterator<UserGroupsTableEntry> iterator = groupsList.iterator(); iterator
            .hasNext();)
      {
         UserGroupsTableEntry userGroupsTableEntry = (UserGroupsTableEntry) iterator
               .next();
         if (userGroupsTableEntry.isSelectedRow())
         {
            UserGroup userGroup = userGroupsTableEntry.getUserGroup();
            if (userGroup != null && userGroup.getValidTo() == null)
            {
               service.invalidateUserGroup(userGroup.getOID());
               invalidatedUserGroups.add(userGroup);
            }
            else
            {
               skippedUserGroups.add(userGroup);
            }
         }

      }
      
      if (invalidatedUserGroups != null && !invalidatedUserGroups.isEmpty())
      {
         for (Iterator<UserGroup> iterator = invalidatedUserGroups.iterator(); iterator.hasNext();)
         {
            UserGroup userGroup = (UserGroup) iterator.next();

            successItemsList.add(new NotificationItem(I18nUtils.getUserGroupLabel(userGroup), this.getMessages()
                  .getString("notifyUserGroupInvalidate")));
         }
      }

      if (skippedUserGroups != null && !skippedUserGroups.isEmpty())
      {
         for (Iterator<UserGroup> iterator = skippedUserGroups.iterator(); iterator.hasNext();)
         {
            UserGroup userGroup = (UserGroup) iterator.next();
            failureItemsList.add(new NotificationItem(I18nUtils.getUserGroupLabel(userGroup), this.getMessages()
                  .getString("notifyUserGroupNotValidateMsg")));
         }
      }
      NotificationMessageBean.showNotifications(successItemsList, this.getMessages().getString("notifySuccessMsg"),
            failureItemsList, this.getMessages().getString("notifyNonValidateMsg"));
      initialize();
   }

   /**
    * handles return event and refreshes page
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

   @Override
   public void initialize()
   {
      userGroupsTable.refresh(true);
   }
   public void handleEvent(ViewEvent event)
   {
       if (ViewEventType.CREATED == event.getType())
       {
          workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
                AdminportalConstants.WORKFLOW_FACADE);
          propsBean = AdminMessagesPropertiesBean.getInstance();
          initUserGroupColumns();
         
           
     
       }
       else  if (ViewEventType.ACTIVATED == event.getType())
       {
          initialize();
       }
   }
   /**
    * Initializes User Group Columns
    */
   private void initUserGroupColumns()
   {
      List<ColumnPreference> userGroupMgmtFixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference nameCol = new ColumnPreference("Name", "name", propsBean
            .getString("views.participantMgmt.column.name"),
            ResourcePaths.V_USERGROUPCOLUMNS_VIEW, true, true);

      ColumnPreference oidCol = new ColumnPreference("OID", "oid", ColumnDataType.NUMBER,
            propsBean.getString("views.participantMgmt.column.oid"), true, true);

      ColumnPreference idCol = new ColumnPreference("Id", "id", ColumnDataType.STRING,
            propsBean.getString("views.userGroupMgmt.userGroupTable.column.id"), true,
            true);

      ColumnPreference validFromCol = new ColumnPreference("ValidFrom", "validFrom",
            ColumnDataType.DATE, propsBean.getString("views.participantMgmt.column.validFrom"),
            true, false);
      validFromCol.setNoWrap(true);

      ColumnPreference validToCol = new ColumnPreference("ValidTo", "validTo",
            ColumnDataType.DATE, propsBean.getString("views.participantMgmt.column.validTo"),
            true, false);
      validToCol.setNoWrap(true);

      List<ColumnPreference> userGroupMgmtCols = new ArrayList<ColumnPreference>();
      userGroupMgmtCols.add(nameCol);
      userGroupMgmtCols.add(oidCol);
      userGroupMgmtCols.add(idCol);
      userGroupMgmtCols.add(validFromCol);
      userGroupMgmtCols.add(validToCol);

      IColumnModel userGroupMgmtColumnModel = new DefaultColumnModel(userGroupMgmtCols,
            userGroupMgmtFixedCols, null, UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_USER_GROUP_MGT);
      TableColumnSelectorPopup colSelecPopup = new TableColumnSelectorPopup(
            userGroupMgmtColumnModel);

      userGroupsTable = new PaginatorDataTable<UserGroupsTableEntry, UserGroup>(colSelecPopup,
            new UserGroupMgmtSearchHandler(), null, new UserGroupMgmtSortHandler(), this,
            new DataTableSortModel<UserGroupsTableEntry>("oid", false));
      userGroupsTable.setRowSelector(new DataTableRowSelector("selectedRow",true));
      userGroupsTable.initialize();

   }

   /**
    * Returns user Group table entry user object
    */
   public UserGroupsTableEntry createUserObject(Object resultRow)
   {
      UserGroup uGroup = null;
      if (resultRow instanceof UserGroup)
      {
         uGroup = (UserGroup) resultRow;
         try
         {
            return new UserGroupsTableEntry(uGroup, uGroup.getName(), uGroup.getOID(), uGroup.getId(), uGroup
                  .getValidFrom(), uGroup.getValidTo(), false);
         }
         catch (Exception e)
         {
            trace.error(e);
            UserGroupsTableEntry userGroupsTableEntry = new UserGroupsTableEntry();
            userGroupsTableEntry.setCause(e);
            userGroupsTableEntry.setLoaded(false);
            return userGroupsTableEntry;
         }

      }
      return null;
   }

   /**
    * returns total user Group count
    * 
    * @return
    * @throws PortalException
    */
   public long getTotalUserGroupsCount() throws PortalException
   {
      if (workflowFacade.getTotalUserGroupsCount() != 0)
         return workflowFacade.getTotalUserGroupsCount();
      else
         return 0;
   }

   /**
    * Returns active user group count
    * 
    * @return
    * @throws PortalException
    */
   public long getActiveUserGroupsCount() throws PortalException
   {
      if (workflowFacade.getActiveUserGroupsCount() != 0)
         return workflowFacade.getActiveUserGroupsCount();
      else
         return 0;
   }
   
   // ********************************* Default Getter and Setter Methods
   // ***********************
   public PaginatorDataTable<UserGroupsTableEntry, UserGroup> getUserGroupsTable()
   {
      return userGroupsTable;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class UserGroupMgmtSearchHandler extends IppSearchHandler<UserGroup>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         return UserGroupQuery.findAll();
      }

      @Override
      public QueryResult<UserGroup> performSearch(Query query)
      {
         try
         {
            return workflowFacade.getAllUserGroups((UserGroupQuery) query);
         }
         catch (AccessForbiddenException e)
         {
            ExceptionHandler.handleException(ExceptionHandler.CLIENT_ID_NONE, e);
            return null;
         }
      }
   }   
   
   /**
    * @author Subodh.Godbole
    *
    */
   public class UserGroupMgmtSortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = 1L;

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriteriaList)
      {
         Iterator< ? > iterator = sortCriteriaList.iterator();

         if (iterator.hasNext())
         {
            SortCriterion sortCriterion = (SortCriterion) iterator.next();
            if ("name".equals(sortCriterion.getProperty()))
            {
               query.orderBy(UserGroupQuery.NAME, sortCriterion.isAscending());
            }
            else if ("oid".equals(sortCriterion.getProperty()))
            {
               query.orderBy(UserGroupQuery.OID, sortCriterion.isAscending());

            }
            else if ("id".equals(sortCriterion.getProperty()))
            {
               query.orderBy(UserGroupQuery.ID, sortCriterion.isAscending());

            }
         }

      }
   }
}
