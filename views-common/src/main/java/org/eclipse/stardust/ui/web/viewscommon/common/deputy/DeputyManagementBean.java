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
package org.eclipse.stardust.ui.web.viewscommon.common.deputy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.engine.api.runtime.Deputy;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class DeputyManagementBean extends UIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private User currentUser;

   private SortableTable<DeputyTableEntry> myDeputiesTable;
   private SortableTable<DeputyTableEntry> deputyOfTable;

   private Map<String, List<DeputyTableEntry>> deputiesCache;

   /**
    *
    */
   public DeputyManagementBean()
   {
      super("deputyManagementView");
      currentUser = ServiceFactoryUtils.getServiceFactory().getUserService().getUser();

      initialize();
      load(true);
   }

   /*
    * (non-Javadoc)
    *
    * @see
    * org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse
    * .stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
   }

   @Override
   public void initialize()
   {
      initializeMyDeputiesTable();
      initializeDeputyOfTable();

   }

   /**
    * @param resetMyTeamsTable
    */
   private void load(boolean resetMyTeamsTable)
   {
      deputiesCache = new HashMap<String, List<DeputyTableEntry>>();

      loadMyDeputiesTable();
      loadDeputyOfTable();

   }

   /**
    *
    */
   private void initializeMyDeputiesTable()
   {
      ColumnPreference deputyCol = new ColumnPreference("Deputy", "userDisplayName", getMessages().getString("deputy"),
            ResourcePaths.V_deputyManagementViewColumns, true, true);

      ColumnPreference validFromCol = new ColumnPreference("ValidFrom", "validFrom", ColumnDataType.DATE,
            getMessages().getString("validFrom"), true, false);

      ColumnPreference validToCol = new ColumnPreference("ValidTo", "validTo", ColumnDataType.DATE,
            getMessages().getString("validTo"), true, false);

      ColumnPreference actionsCol = new ColumnPreference("MyDeputiesActions", "", MessagesViewsCommonBean.getInstance()
            .getString("views.common.column.actions"), ResourcePaths.V_deputyManagementViewColumns, true, false);

      List<ColumnPreference> deputyCols = new ArrayList<ColumnPreference>();
      deputyCols.add(deputyCol);
      deputyCols.add(validFromCol);
      deputyCols.add(validToCol);
      deputyCols.add(actionsCol);

      IColumnModel deputyColumnModel = new DefaultColumnModel(deputyCols, UserPreferencesEntries.M_VIEWS_COMMON,
            UserPreferencesEntries.V_DEPUTY_MANAGEMENT_VIEW);

      myDeputiesTable = new SortableTable<DeputyTableEntry>(deputyColumnModel, null,
            new SortableTableComparator<DeputyTableEntry>("userDisplayName", true));
      myDeputiesTable.initialize();
   }

   /**
    *
    */
   private void initializeDeputyOfTable()
   {
      ColumnPreference deputyCol = new ColumnPreference("User", "userDisplayName", getMessages().getString("user"),
            ResourcePaths.V_deputyManagementViewColumns, true, true);

      ColumnPreference validFromCol = new ColumnPreference("ValidFrom", "validFrom", ColumnDataType.DATE,
            getMessages().getString("validFrom"), true, false);

      ColumnPreference validToCol = new ColumnPreference("ValidTo", "validTo", ColumnDataType.DATE,
            getMessages().getString("validTo"), true, false);

      List<ColumnPreference> deputyOfCols = new ArrayList<ColumnPreference>();
      deputyOfCols.add(deputyCol);
      deputyOfCols.add(validFromCol);
      deputyOfCols.add(validToCol);

      IColumnModel deputyColumnModel = new DefaultColumnModel(deputyOfCols, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_DEPUTY_MANAGEMENT_VIEW);

      deputyOfTable = new SortableTable<DeputyTableEntry>(deputyColumnModel, null,
            new SortableTableComparator<DeputyTableEntry>("userDisplayName", true));
      deputyOfTable.initialize();
   }


   /**
    *
    */
   private void loadMyDeputiesTable()
   {
      loadDeputiesTable(myDeputiesTable, currentUser);
   }

   /**
    * @param deputiesTable
    * @param forUser
    */
   private void loadDeputiesTable(SortableTable<DeputyTableEntry> deputiesTable, User forUser)
   {
      UserService userService = ServiceFactoryUtils.getUserService();
      deputiesTable.setList(fetchDeputies(userService, forUser));
   }

   /**
    *
    */
   private void loadDeputyOfTable()
   {
      List<DeputyTableEntry> deputyList = new ArrayList<DeputyTableEntry>();

      UserService userService = ServiceFactoryUtils.getUserService();
      List<Deputy> deputes = userService.getUsersBeingDeputyFor(currentUser);
      for (Deputy deputy : deputes)
      {
         // TODO To Optimise
         User user = ServiceFactoryUtils.getUserService().getUser(deputy.getUser().getId());
         deputyList.add(new DeputyTableEntry(user, deputy.getFromDate(), deputy.getUntilDate(), deputy
               .getParticipints()));
      }
      deputyOfTable.setList(deputyList);
   }

   /**
    * @param userService
    * @param user
    * @return
    */
   private List<DeputyTableEntry> fetchDeputies(UserService userService, User user)
   {
      if (!deputiesCache.containsKey(user.getAccount()))
      {
         User deputyUser;
         List<DeputyTableEntry> deputyList = new ArrayList<DeputyTableEntry>();
         for (Deputy deputy : userService.getDeputies(user))
         {
            // TODO To Optimise
            deputyUser = ServiceFactoryUtils.getUserService().getUser(deputy.getDeputyUser().getId());
            deputyList.add(new DeputyTableEntry(deputyUser, deputy.getFromDate(), deputy.getUntilDate(), deputy
                  .getParticipints()));
         }

         deputiesCache.put(user.getAccount(), deputyList);
      }
      return deputiesCache.get(user.getAccount());
   }

   /**
    *
    */
   public void addMyDeputy()
   {
      try
      {
         CreateOrModifyDeputyPopupBean.getInstance().openPopup(null, currentUser,
               new DeputyCallbackHandler());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void editMyDeputy(ActionEvent event)
   {
      try
      {
         DeputyTableEntry deputyTableEntry = (DeputyTableEntry) event.getComponent().getAttributes().get("deputy");
         CreateOrModifyDeputyPopupBean.getInstance().openPopup(deputyTableEntry.getClone(), currentUser,
               new DeputyCallbackHandler());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void removeMyDeputy(ActionEvent event)
   {
      try
      {
         DeputyTableEntry deputyTableEntry = (DeputyTableEntry) event.getComponent().getAttributes().get("deputy");
         ServiceFactoryUtils.getUserService().removeDeputy(currentUser, deputyTableEntry.getUser());

         myDeputiesTable.getList().remove(deputyTableEntry);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }


   public SortableTable<DeputyTableEntry> getMyDeputiesTable()
   {
      return myDeputiesTable;
   }

   public SortableTable<DeputyTableEntry> getDeputyOfTable()
   {
      return deputyOfTable;
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

         SortableTable<DeputyTableEntry> deputyTable = myDeputiesTable;
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

}