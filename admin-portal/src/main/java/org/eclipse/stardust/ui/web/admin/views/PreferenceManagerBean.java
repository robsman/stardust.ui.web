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

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class PreferenceManagerBean extends UIComponentBean implements ViewEventHandler, ConfirmationDialogHandler
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final SelectItem[] viewSelection = new SelectItem[2];
   private String selectedView;
   private UserAutocompleteMultiSelector userSelector;
   private String prefSearchTxt;
   private SortableTable<PreferenceManagerTableEntry> prefManagerTable;
   private List<PreferenceManagerTableEntry> prefList;
   private ConfirmationDialog prefMngrConfirmationDialog;
   private PreferenceManagerTableEntry selectedPrefMngrObj;

   public PreferenceManagerBean()
   {
      super("prefManagerBean");
   }

   /**
    * @return
    */
   public static PreferenceManagerBean getCurrent()
   {
      return (PreferenceManagerBean) FacesUtils.getBeanFromContext("prefManagerBean");
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         initialize();
      }

   }

   @Override
   public void initialize()
   {
      userSelector = new UserAutocompleteMultiSelector(false, true);
      userSelector.setShowOnlineIndicator(false);
      viewSelection[0] = new SelectItem(VIEW_TYPE.TENENT.name(), getMessages().getString("tenant.label"));
      viewSelection[1] = new SelectItem(VIEW_TYPE.USER.name(), getMessages().getString("user.label"));
      selectedView = VIEW_TYPE.TENENT.name();
      prefList = CollectionUtils.newArrayList();
      createTable();
      update();
   }

   public void update()
   {
      fetchPreferences();
      updatePrefStoreTable(prefList);
   }

   public void updatePrefStoreTable(List<PreferenceManagerTableEntry> prefTableElements)
   {
      prefManagerTable.setList(prefTableElements);
      prefManagerTable.initialize();
   }

   /**
    * 
    */
   private void fetchPreferences()
   {

      QueryService qService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      List<Preferences> prefs = new ArrayList<Preferences>();
      prefList.clear();
      String userFullName = null;
      if (VIEW_TYPE.TENENT.name().equals(selectedView))
      {
         // fetch all the Partition preferences
         prefs = qService.getAllPreferences(PreferenceQuery.findAll(PreferenceScope.PARTITION));
      }
      else
      {
         UserWrapper userWrapper = userSelector.getSelectedValue();
         if (userWrapper != null)
         {
            User u = userWrapper.getUser();
            userFullName = userWrapper.getFullName();
            // fetch all preference store entries for User, the moduleId and PreferenceId
            // can be passed as '*'
            prefs = qService.getAllPreferences(PreferenceQuery.findPreferencesForUsers(u.getRealm().getId(), u.getId(),
                  "*", "*"));
         }

      }
      for (Preferences pref : prefs)
      {
         Map<String, Serializable> pref11 = pref.getPreferences();

         for (Map.Entry<String, Serializable> entry : pref11.entrySet())
         {
            prefList
                  .add(new PreferenceManagerTableEntry(pref.getScope().name(), pref.getModuleId(), pref
                        .getPreferencesId(), entry.getKey(), entry.getValue().toString(), pref.getPartitionId(),
                        userFullName));
         }
      }

   }

   /**
    * 
    */
   private void createTable()
   {
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference scopeCol = new ColumnPreference("Scope", "scope", ColumnDataType.STRING, getMessages()
            .getString("scope.label"), null, true, false);
      fixedCols.add(scopeCol);

      ColumnPreference moduleIdCol = new ColumnPreference("ModuleId", "moduleId", ColumnDataType.STRING, getMessages()
            .getString("moduleId.label"), null, true, true);
      fixedCols.add(moduleIdCol);

      ColumnPreference preferenceIdCol = new ColumnPreference("PreferenceId", "preferenceId", ColumnDataType.STRING,
            getMessages().getString("preferenceId.label"), null, true, true);
      fixedCols.add(preferenceIdCol);

      ColumnPreference preferenceNameCol = new ColumnPreference("PreferenceName", "preferenceName",
            ColumnDataType.STRING, getMessages().getString("preferenceName.label"), null, true, true);
      fixedCols.add(preferenceNameCol);

      ColumnPreference preferenceValueCol = new ColumnPreference("PreferenceValue", "preferenceValue",
            ColumnDataType.STRING, getMessages().getString("preferenceValue.label"), null, true, true);
      fixedCols.add(preferenceValueCol);

      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();

      IColumnModel columnModel = new DefaultColumnModel(selectableCols, fixedCols, null,
            UserPreferencesEntries.M_ADMIN, "prefManagerBean");
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      prefManagerTable = new SortableTable<PreferenceManagerTableEntry>(colSelecpopup, null,
            new SortableTableComparator<PreferenceManagerTableEntry>("moduleId", true));
      prefManagerTable.setRowSelector(new DataTableRowSelector("selected"));
   }

   /**
    * 
    * @param event
    */
   public void viewSelectionListener(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();

         return;
      }
      else
      {
         update();
      }
   }

   /**
    * 
    * @param event
    */
   public void performSearch()
   {
      if (StringUtils.isNotEmpty(prefSearchTxt))
      {
         if (CollectionUtils.isEmpty(prefList))
            fetchPreferences();
         updatePrefStoreTable(polulateSearchResult());
      }
      else
         update();
   }

   /**
    * 
    * @param event
    */
   public void removePreference(ActionEvent event)
   {
      for (Iterator<PreferenceManagerTableEntry> it = prefManagerTable.getList().iterator(); it.hasNext();)
      {
         PreferenceManagerTableEntry row = it.next();

         if (row.isSelected())
         {
            selectedPrefMngrObj = row;
            // Confirmation Dialog on Delete action
            prefMngrConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING,
                  DialogActionType.CONTINUE_CANCEL, null, DialogStyle.COMPACT, this);
            prefMngrConfirmationDialog.setMessage(getMessages().getString("modifyPreference.confirmDelPref.warn"));
            prefMngrConfirmationDialog.openPopup();
         }
      }
      if (null == selectedPrefMngrObj)
      {
         MessageDialog.addMessage(MessageType.ERROR,
               AdminMessagesPropertiesBean.getInstance().getString("views.common.error.label"), getMessages()
                     .getString("modifyPreference.confirmDelPref.error"));
         return;
      }
   }

   /**
    * Search the retrieved List for search text entered and return matching
    * PreferenceManagerTableEntry objects
    * 
    * @return
    */
   private List<PreferenceManagerTableEntry> polulateSearchResult()
   {
      List<PreferenceManagerTableEntry> prefTableList = CollectionUtils.newArrayList();
      try
      {
         for (PreferenceManagerTableEntry prefTabList : prefList)
         {
            Field[] inputFields = prefTabList.getClass().getDeclaredFields();
            for (Field field : inputFields)
            {

               String value = ReflectionUtils.invokeGetterMethod((Object) prefTabList, field.getName()).toString();
               if (value.toLowerCase().contains(prefSearchTxt.toLowerCase()))
               {
                  prefTableList.add(prefTabList);
                  break;
               }

            }
         }
      }
      catch (NoSuchMethodException e)
      {
      }
      catch (Exception e)
      {
      }
      return prefTableList;
   }

   public boolean accept()
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      Preferences selPreference = null;
      if (VIEW_TYPE.TENENT.name().equals(selectedView))
      {
         selPreference = adminService.getPreferences(PreferenceScope.PARTITION, selectedPrefMngrObj.getModuleId(),
               selectedPrefMngrObj.getPreferenceId());
      }
      else
         selPreference = adminService.getPreferences(PreferenceScope.USER, selectedPrefMngrObj.getModuleId(),
               selectedPrefMngrObj.getPreferenceId());
      selPreference.getPreferences().remove(selectedPrefMngrObj.getPreferenceName());
      adminService.savePreferences(selPreference);
      update();
      prefMngrConfirmationDialog = null;
      return true;
   }

   public boolean cancel()
   {
      prefMngrConfirmationDialog = null;
      return true;
   }

   /**
   *
   */
   public static enum VIEW_TYPE {
      TENENT, USER;
   }

   public SelectItem[] getViewSelection()
   {
      return viewSelection;
   }

   public String getSelectedView()
   {
      return selectedView;
   }

   public void setSelectedView(String selectedView)
   {
      this.selectedView = selectedView;
   }

   public UserAutocompleteMultiSelector getUserSelector()
   {
      return userSelector;
   }

   public String getPrefSearchTxt()
   {
      return prefSearchTxt;
   }

   public void setPrefSearchTxt(String prefSearchTxt)
   {
      this.prefSearchTxt = prefSearchTxt;
   }

   public SortableTable<PreferenceManagerTableEntry> getPrefManagerTable()
   {
      return prefManagerTable;
   }

   public List<PreferenceManagerTableEntry> getPrefList()
   {
      return prefList;
   }

   public ConfirmationDialog getPrefMngrConfirmationDialog()
   {
      return prefMngrConfirmationDialog;
   }

   public PreferenceManagerTableEntry getSelectedPrefMngrObj()
   {
      return selectedPrefMngrObj;
   }

   public void setSelectedPrefMngrObj(PreferenceManagerTableEntry selectedPrefMngrObj)
   {
      this.selectedPrefMngrObj = selectedPrefMngrObj;
   }

}
