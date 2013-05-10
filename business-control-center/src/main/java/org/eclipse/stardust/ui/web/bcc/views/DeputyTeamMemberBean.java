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
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

public class DeputyTeamMemberBean extends UIComponentBean implements ViewEventHandler
{

   /**
    *
    */
   private static final long serialVersionUID = 1L;

   private boolean teamLead;

   private User currentUser;

   private WorkflowFacade facade;

   private SortableTable<DeputyTableEntry> myTeamMembersTable;

   private SortableTable<DeputyTableEntry> myTeamMemberDeputiesTable;

   private DeputyTableEntry selectedTeamMember;

   private List<DeputyTableEntry> myTeamMemberList;

   private boolean showAllTeamMembers = true;

   private Map<String, List<DeputyTableEntry>> deputiesTeamMemberCache;

   public DeputyTeamMemberBean()
   {
      super("deputyTeamMemberView");
      currentUser = ServiceFactoryUtils.getServiceFactory().getUserService().getUser();
      facade = WorkflowFacade.getWorkflowFacade();
      teamLead = facade.isTeamLead();

      initialize();
      load(true);
   }

   public void handleEvent(ViewEvent event)
   {
      switch (event.getType())
      {
      case CREATED:
         currentUser = ServiceFactoryUtils.getServiceFactory().getUserService().getUser();
         facade = WorkflowFacade.getWorkflowFacade();
         teamLead = facade.isTeamLead();

         initialize();
         load(true);
         break;
      }

   }

   /**
    * @param resetMyTeamsTable
    */
   private void load(boolean resetMyTeamsTable)
   {
      deputiesTeamMemberCache = new HashMap<String, List<DeputyTableEntry>>();

      loadMyTeamMembersTable();

      if (resetMyTeamsTable)
      {
         selectedTeamMember = null;
         loadMyTeamMemberDeputiesTable();
      }
   }

   @Override
   public void initialize()
   {
      initializeMyTeamMembersTable();
      initializeMyTeamMemberDeputiesTable();

   }

   /**
    *
    */
   private void initializeMyTeamMembersTable()
   {
      ColumnPreference deputyCol = new ColumnPreference("TeamMember", "userDisplayName",
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

      myTeamMembersTable = new SortableTable<DeputyTableEntry>(deputyColumnModel, null,
            new SortableTableComparator<DeputyTableEntry>("userDisplayName", true));
      myTeamMembersTable.setRowSelector(new DataTableRowSelector("selected",
            new TeamMemberRowEventListener(), false));
      myTeamMembersTable.initialize();
   }

   /**
    *
    */
   private void initializeMyTeamMemberDeputiesTable()
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

      myTeamMemberDeputiesTable = new SortableTable<DeputyTableEntry>(deputyColumnModel,
            null, new SortableTableComparator<DeputyTableEntry>("userDisplayName", true));
      myTeamMemberDeputiesTable.initialize();
   }

   /**
    *
    */
   private void loadMyTeamMembersTable()
   {
      myTeamMemberList = new ArrayList<DeputyTableEntry>();

      UserQuery userQuery = facade.getTeamQuery(false);
      userQuery.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Full));
      userQuery.orderBy(UserQuery.LAST_NAME)
            .and(UserQuery.FIRST_NAME)
            .and(UserQuery.ACCOUNT);

      UserService userService = ServiceFactoryUtils.getUserService();

      Users memberUsers = ServiceFactoryUtils.getQueryService().getAllUsers(userQuery);
      for (User memberUser : memberUsers)
      {
         if ( !memberUser.getAccount().equals(currentUser.getAccount())) // Skip the
                                                                         // Current User
         {
            myTeamMemberList.add(new DeputyTableEntry(memberUser,
                  CollectionUtils.isNotEmpty(fetchDeputies(userService, memberUser))));
         }
      }

      myTeamMembersTable.setList(myTeamMemberList);
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
      if ( !deputiesTeamMemberCache.containsKey(user.getAccount()))
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

         deputiesTeamMemberCache.put(user.getAccount(), deputyList);
      }
      return deputiesTeamMemberCache.get(user.getAccount());
   }

   /**
    * @param
    */
   private void loadMyTeamMemberDeputiesTable()
   {
      if (null != selectedTeamMember)
      {
         loadDeputiesTable(myTeamMemberDeputiesTable, selectedTeamMember.getUser());
      }
      else
      {
         myTeamMemberDeputiesTable.setList(null);
      }
   }

   /**
    *
    */
   public void addMyTeamMemberDeputy()
   {
      try
      {
         CreateOrModifyDeputyPopupBean.getInstance().openPopup(null,
               selectedTeamMember.getUser(), new DeputyCallbackHandler());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void editMyTeamMemberDeputy(ActionEvent event)
   {
      try
      {
         DeputyTableEntry deputyTableEntry = (DeputyTableEntry) event.getComponent().getAttributes().get("deputy");
         CreateOrModifyDeputyPopupBean.getInstance().openPopup(deputyTableEntry.getClone(),
               selectedTeamMember.getUser(), new DeputyCallbackHandler());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void removeMyTeamMemberDeputy(ActionEvent event)
   {
      try
      {
         DeputyTableEntry deputyTableEntry = (DeputyTableEntry) event.getComponent().getAttributes().get("deputy");
         ServiceFactoryUtils.getUserService().removeDeputy(selectedTeamMember.getUser(), deputyTableEntry.getUser());

         myTeamMemberDeputiesTable.getList().remove(deputyTableEntry);
         selectedTeamMember.setHasDeputies(myTeamMemberDeputiesTable.getList().size() > 0);
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
         selectedTeamMember.setHasDeputies(true);
         SortableTable<DeputyTableEntry> deputyTable = myTeamMemberDeputiesTable;
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
   public void toggleMyTeamMembers()
   {
      try
      {
         showAllTeamMembers = !showAllTeamMembers;

         if (showAllTeamMembers)
         {
            myTeamMembersTable.setList(myTeamMemberList);
         }
         else
         {
            List<DeputyTableEntry> list = new ArrayList<DeputyTableEntry>();
            for (DeputyTableEntry entry : myTeamMemberList)
            {
               if (entry.isHasDeputies())
               {
                  list.add(entry);
               }
            }
            myTeamMembersTable.setList(list);
         }

         selectedTeamMember = null;
         loadMyTeamMemberDeputiesTable();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   public boolean isTeamMemberSelected()
   {
      return null != selectedTeamMember;
   }

   public SortableTable<DeputyTableEntry> getMyTeamMembersTable()
   {
      return myTeamMembersTable;
   }

   public SortableTable<DeputyTableEntry> getMyTeamMemberDeputiesTable()
   {
      return myTeamMemberDeputiesTable;
   }

   public boolean isTeamLead()
   {
      return teamLead;
   }

   public boolean isShowAllTeamMembers()
   {
      return showAllTeamMembers;
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
         selectedTeamMember = myTeamMembersTable.getList().get(event.getRow());
         loadMyTeamMemberDeputiesTable();
      }
   }

}
