package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import com.icesoft.faces.component.ext.ClickActionEvent;
import com.icesoft.faces.component.ext.RowSelectorEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;

import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnRenderType;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector.EventListener;
import org.eclipse.stardust.ui.web.viewscommon.common.deputy.CreateOrModifyDeputyPopupBean;
import org.eclipse.stardust.ui.web.viewscommon.common.deputy.DeputyTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

public class DeputyTeamMemberBean extends UIComponentBean implements ViewEventHandler
{

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private User currentUser;

   private SortableTable<DeputyTableEntry> usersTable;

   private SortableTable<DeputyTableEntry> userDeputiesTable;

   private DeputyTableEntry selectedUser;

   private List<DeputyTableEntry> usersList;

   private boolean showAllUsers = true;

   private Map<String, List<DeputyTableEntry>> deputiesUserCache;

   public DeputyTeamMemberBean()
   {
      super("deputyTeamMemberView");
      currentUser = ServiceFactoryUtils.getServiceFactory().getUserService().getUser();

      initialize();
      load(true);
   }

   public void handleEvent(ViewEvent event)
   {
      switch (event.getType())
      {
      case CREATED:
         currentUser = ServiceFactoryUtils.getServiceFactory().getUserService().getUser();

         initialize();
         load(true);
         break;
      }

   }

   /**
    * @param resetUsersTable
    */
   private void load(boolean resetUsersTable)
   {
      deputiesUserCache = new HashMap<String, List<DeputyTableEntry>>();

      loadUsersTable();

      if (resetUsersTable)
      {
         selectedUser = null;
         loadUserDeputiesTable();
      }
   }

   @Override
   public void initialize()
   {
      initializeUsersTable();
      initializeUserDeputiesTable();

   }

   /**
    *
    */
   private void initializeUsersTable()
   {
      ColumnPreference deputyCol = new ColumnPreference("Participant", "userDisplayName",
            getMessages().getString("teamMember"),
            ResourcePaths.V_deputyManagementViewColumns, true, true);

      ColumnPreference validFromCol = new ColumnPreference("HasDeputies",
            "hasDeputiesLabel", ColumnDataType.STRING, getMessages().getString(
                  "hasDeputies"), true, false);
      validFromCol.setColumnRenderType(ColumnRenderType.READ_ONLY);

      List<ColumnPreference> myTeamsDeputiesCols = new ArrayList<ColumnPreference>();
      myTeamsDeputiesCols.add(deputyCol);
      myTeamsDeputiesCols.add(validFromCol);

      IColumnModel deputyColumnModel = new DefaultColumnModel(myTeamsDeputiesCols,
            UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_DEPUTY_TEAM_MEMBER_VIEW);

      usersTable = new SortableTable<DeputyTableEntry>(deputyColumnModel, null,
            new SortableTableComparator<DeputyTableEntry>("userDisplayName", true));
      usersTable.setRowSelector(new DataTableRowSelector("selected",
            new TeamMemberRowEventListener(), false));
      usersTable.initialize();
   }

   /**
    *
    */
   private void initializeUserDeputiesTable()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      ColumnPreference deputyCol = new ColumnPreference("Deputy", "userDisplayName",
            propsBean.getString("views.deputyManagementView.deputy"),
            ResourcePaths.V_deputyManagementViewColumns, true, true);

      ColumnPreference validFromCol = new ColumnPreference("ValidFrom", "validFrom",
            ColumnDataType.DATE, propsBean.getString("views.deputyManagementView.validFrom"), true, false);

      ColumnPreference validToCol = new ColumnPreference("ValidTo", "validTo",
            ColumnDataType.DATE, propsBean.getString("views.deputyManagementView.validTo"), true, false);

      ColumnPreference actionsCol = new ColumnPreference("TeamMemberDeputiesActions", "",
            MessagesBCCBean.getInstance().getString("views.common.column.actions"),
            ResourcePaths.V_deputyManagementViewColumns, true, false);

      List<ColumnPreference> deputyCols = new ArrayList<ColumnPreference>();
      deputyCols.add(deputyCol);
      deputyCols.add(validFromCol);
      deputyCols.add(validToCol);
      deputyCols.add(actionsCol);

      IColumnModel deputyColumnModel = new DefaultColumnModel(deputyCols,
            UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_DEPUTY_TEAM_MEMBER_VIEW);

      userDeputiesTable = new SortableTable<DeputyTableEntry>(deputyColumnModel,
            null, new SortableTableComparator<DeputyTableEntry>("userDisplayName", true));
      userDeputiesTable.initialize();
   }

   /**
    *
    */
   private void loadUsersTable()
   {
      usersList = new ArrayList<DeputyTableEntry>();
      UserQuery query = null;
      UserService userService = ServiceFactoryUtils.getUserService();
      if (AuthorizationUtils.canManageDeputies())
      {
         query = UserQuery.findActive();
         query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Full));
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);

         Users memberUsers = ServiceFactoryUtils.getQueryService().getAllUsers(query);
         for (User memberUser : memberUsers)
         {
            usersList.add(new DeputyTableEntry(memberUser, CollectionUtils.isNotEmpty(fetchDeputies(userService,
                  memberUser))));
         }
      }
      else
      {
         usersList.add(new DeputyTableEntry(currentUser, CollectionUtils.isNotEmpty(fetchDeputies(userService,
               currentUser))));
      }

      usersTable.setList(usersList);
   }

   /**
    * @param deputiesTable
    * @param forUser
    */
   private void loadDeputiesTable(SortableTable<DeputyTableEntry> deputiesTable,
         User forUser)
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      deputiesTable.setList(fetchDeputies(userService, forUser));
   }

   /**
    * @param userService
    * @param user
    * @return
    */
   private List<DeputyTableEntry> fetchDeputies(UserService userService, User user)
   {
      if ( !deputiesUserCache.containsKey(user.getAccount()))
      {
         User deputyUser;
         List<DeputyTableEntry> deputyList = new ArrayList<DeputyTableEntry>();
         for (Deputy deputy : userService.getDeputies(user))
         {
            // TODO To Optimise
            deputyUser = ServiceFactoryUtils.getUserService().getUser(
                  deputy.getDeputyUser().getId());
            deputyList.add(new DeputyTableEntry(deputyUser, deputy.getFromDate(),
                  deputy.getUntilDate(), deputy.getParticipints()));
         }

         deputiesUserCache.put(user.getAccount(), deputyList);
      }
      return deputiesUserCache.get(user.getAccount());
   }

   /**
    * @param
    */
   private void loadUserDeputiesTable()
   {
      if (null != selectedUser)
      {
         loadDeputiesTable(userDeputiesTable, selectedUser.getUser());
      }
      else
      {
         userDeputiesTable.setList(null);
      }
   }

   /**
    *
    */
   public void addUserDeputy()
   {
      try
      {
         CreateOrModifyDeputyPopupBean.getInstance().openPopup(null,
               selectedUser.getUser(), new DeputyCallbackHandler());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void editUserDeputy(ActionEvent event)
   {
      try
      {
         DeputyTableEntry deputyTableEntry = (DeputyTableEntry) event.getComponent().getAttributes().get("deputy");
         CreateOrModifyDeputyPopupBean.getInstance().openPopup(deputyTableEntry.getClone(),
               selectedUser.getUser(), new DeputyCallbackHandler());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void removeUserDeputy(ActionEvent event)
   {
      try
      {
         DeputyTableEntry deputyTableEntry = (DeputyTableEntry) event.getComponent().getAttributes().get("deputy");
         ServiceFactoryUtils.getUserService().removeDeputy(selectedUser.getUser(), deputyTableEntry.getUser());

         userDeputiesTable.getList().remove(deputyTableEntry);
         selectedUser.setHasDeputies(userDeputiesTable.getList().size() > 0);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }


   /**
    * @author Subodh.Godbole
    *
    */
   private class DeputyCallbackHandler extends ParametricCallbackHandler
   {
      /*
       * (non-Javadoc)
       *
       * @see
       * org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler#handleEvent(
       * org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType)
       */
      public void handleEvent(EventType eventType)
      {
         selectedUser.setHasDeputies(true);
         SortableTable<DeputyTableEntry> deputyTable = userDeputiesTable;
         List<DeputyTableEntry> list = deputyTable.getList();

         DeputyTableEntry deputyTableEntry = ((DeputyTableEntry) getParameters().get(
               "deputyTableEntry"));
         int index = list.indexOf(deputyTableEntry);

         if (index >= 0)
         {
            deputyTable.getList().remove(index);
         }

         list.add(deputyTableEntry);
         deputyTable.setList(list);

         // This is required so that again sorting happens. This would be fixed later in
         // SortableTable
         deputyTable.getComparator().initializeSortModel();
      }
   }

   /**
    *
    */
   public void toggleShowAllUsers()
   {
      try
      {
         showAllUsers = !showAllUsers;

         if (showAllUsers)
         {
            usersTable.setList(usersList);
         }
         else
         {
            List<DeputyTableEntry> list = new ArrayList<DeputyTableEntry>();
            for (DeputyTableEntry entry : usersList)
            {
               if (entry.isHasDeputies())
               {
                  list.add(entry);
               }
            }
            usersTable.setList(list);
         }

         selectedUser = null;
         loadUserDeputiesTable();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   public boolean isUserSelected()
   {
      return null != selectedUser;
   }

   public SortableTable<DeputyTableEntry> getUsersTable()
   {
      return usersTable;
   }

   public SortableTable<DeputyTableEntry> getUserDeputiesTable()
   {
      return userDeputiesTable;
   }

   public boolean isShowAllUsers()
   {
      return showAllUsers;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   private class TeamMemberRowEventListener implements EventListener
   {
      /*
       * (non-Javadoc)
       *
       * @see org.eclipse.stardust.ui.web.common.table.DataTableRowSelector.EventListener#
       * rowClicked(com.icesoft.faces.component.ext.ClickActionEvent)
       */
      public void rowClicked(ClickActionEvent event)
      {
      }

      /*
       * (non-Javadoc)
       *
       * @see org.eclipse.stardust.ui.web.common.table.DataTableRowSelector.EventListener#
       * rowSelected(com.icesoft.faces.component.ext.RowSelectorEvent)
       */
      public void rowSelected(RowSelectorEvent event)
      {
         selectedUser = usersTable.getList().get(event.getRow());
         loadUserDeputiesTable();
      }
   }

}
