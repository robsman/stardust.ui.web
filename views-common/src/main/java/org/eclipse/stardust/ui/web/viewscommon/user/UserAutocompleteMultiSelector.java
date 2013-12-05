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
package org.eclipse.stardust.ui.web.viewscommon.user;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics.LoginStatistics;
import org.eclipse.stardust.ui.web.common.autocomplete.AutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;

import com.icesoft.faces.component.selectinputtext.SelectInputText;



/**
 * @author Subodh.Godbole
 *
 */
public class UserAutocompleteMultiSelector extends AutocompleteMultiSelector<UserWrapper>
{
   private static final long serialVersionUID = 1L;

   public static final int MAX_ROWS = 10; // Can be moved as a Configuration
   public static final int MIN_CHARACTERS = 1; // Can be moved as a Configuration
   
   protected boolean onlyActiveUsers;
   protected boolean showAutocompletePanel;

   protected boolean showSelectedList;
   protected SortableTable<UserWrapper> selectedUsersTable;
   
   protected boolean singleSelect;
   
   protected IAutocompleteMultiSelectorListener<UserWrapper> autocompleteMultiSelectorListener;
   
   protected boolean showOnlineIndicator;
   protected boolean showProfileImage;

   /**
    * @param maxRows
    * @param onlyActiveUsers
    */
   public UserAutocompleteMultiSelector(boolean onlyActiveUsers, boolean singleSelect)
   {
      super(MAX_ROWS, MIN_CHARACTERS);

      this.setAutocompleteContentUrl(singleSelect
            ? ResourcePaths.V_AUTOCOMPLETE_SINGLE_USER_SELECTOR
            : ResourcePaths.V_AUTOCOMPLETE_MULTI_USER_SELECTOR);
      this.setSelectedDataContentUrl(ResourcePaths.V_AUTOCOMPLETE_USER_SELECTOR_TABLE);
      this.setDataProvider(new AutocompleteUserSelectorDataProvider());
      this.onlyActiveUsers = onlyActiveUsers;
      this.singleSelect = singleSelect;
      
      showAutocompletePanel = true;
      showOnlineIndicator = true;
      showProfileImage = true;
      
      if(!singleSelect)
      {
         showSelectedList = true;
         super.setAutocompleteSelectorListener(new AutocompleteUserSelectorListener());
         initializeSelectedUsersTable();
      }
   }

   /**
    * @param maxRows
    * @param minCharacters
    * @param onlyActiveUsers
    */
   public UserAutocompleteMultiSelector(boolean onlyActiveUsers)
   {
      this(onlyActiveUsers, false);
   }
   
   @Override
   public void setAutocompleteSelectorListener(IAutocompleteSelectorListener autocompleteSelectorListener)
   {
      if(!singleSelect)
      {
         throw new IllegalAccessError("Method Not Allowed to be invoked");
      }
      
      super.setAutocompleteSelectorListener(autocompleteSelectorListener);
   }

   /**
    * @param user
    */
   public void removeSelectedUser(UserWrapper user)
   {
      validateMultiSelectMode();

      List<UserWrapper> users = selectedUsersTable.getList();
      users.remove(user);
      setSelectedValues(users);

      // Fire Event
      if(autocompleteMultiSelectorListener != null)
      {
         autocompleteMultiSelectorListener.dataRemoved(user);
      }
   }

   /**
    * @param user
    */
   public void addSelectedUser(UserWrapper user)
   {
      validateMultiSelectMode();

      List<UserWrapper> users = selectedUsersTable.getList();
      if(!users.contains(user))
      {
         user.setAutocompleteUserSelector(this);
         users.add(user);
         setSelectedValues(users);
   
         // Fire Event
         if(autocompleteMultiSelectorListener != null)
         {
            autocompleteMultiSelectorListener.dataAdded(user);
         }
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#getSelectedData()
    */
   public List<UserWrapper> getSelectedValues()
   {
      validateMultiSelectMode();

      return selectedUsersTable.getList();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#setSelectedData(java.util.List)
    */
   public void setSelectedValues(List<UserWrapper> selectedValues)
   {
      validateMultiSelectMode();

      selectedUsersTable.setList(selectedValues);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#getSelectedDataAsString()
    */
   public List<String> getSelectedValuesAsString()
   {
      List<String> stringList = new ArrayList<String>();
      
      List<UserWrapper> list = getSelectedValues();
      for (UserWrapper userWrapper : list)
      {
         stringList.add(userWrapper.getFullName());
      }
      
      return stringList;
   }

   /**
    * 
    */
   private void initializeSelectedUsersTable()
   {
      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();
      ColumnPreference colProfileIcon = new ColumnPreference("ProfileIcon", "", ColumnDataType.STRING, "", true, false);
      colProfileIcon.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colUserName = new ColumnPreference("UserName", "fullName", ColumnDataType.STRING,
            propsBean.getString("views.userAutocomplete.selectedUsers.table.column.name"), true, true);
      colUserName.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colCloseIcon = new ColumnPreference("Actions", "", ColumnDataType.STRING, "", true, false);
      colCloseIcon.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colProfileIcon);
      cols.add(colUserName);
      cols.add(colCloseIcon);

      IColumnModel columnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_VIEWS_COMMON, UserPreferencesEntries.V_USER_AUTOCOMPLETE);

      selectedUsersTable = new SortableTable<UserWrapper>(columnModel, null,
            new SortableTableComparator<UserWrapper>("fullName", true));

      selectedUsersTable.setList(new ArrayList<UserWrapper>());
      selectedUsersTable.initialize();
   }

   public boolean isOnlyActiveUsers()
   {
      return onlyActiveUsers;
   }

   public void setOnlyActiveUsers(boolean onlyActiveUsers)
   {
      this.onlyActiveUsers = onlyActiveUsers;
   }
   
   public boolean isShowAutocompletePanel()
   {
      return showAutocompletePanel;
   }

   public void setShowAutocompletePanel(boolean showAutocompletePanel)
   {
      this.showAutocompletePanel = showAutocompletePanel;
   }

   public boolean isShowSelectedList()
   {
      validateMultiSelectMode();;

      return showSelectedList;
   }

   public void setShowSelectedList(boolean showSelectedList)
   {
      validateMultiSelectMode();

      this.showSelectedList = showSelectedList;
   }

   public SortableTable<UserWrapper> getSelectedUsersTable()
   {
      validateMultiSelectMode();
      return selectedUsersTable;
   }

   /**
    * @param showOnlineIndicator
    */
   public void setShowOnlineIndicator(boolean showOnlineIndicator)
   {
      this.showOnlineIndicator = showOnlineIndicator;
   }

   /**
    * @param showProfileImage
    */
   public void setShowProfileImage(boolean showProfileImage)
   {
      this.showProfileImage = showProfileImage;
      if(selectedUsersTable != null)
      {
         selectedUsersTable.getColumnModel().getColumn("ProfileIcon").setVisible(showProfileImage);
         selectedUsersTable.initialize();
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector#setAutocompleteMultiSelectorListener(org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector.IAutocompleteMultiSelectorListener)
    */
   public void setAutocompleteMultiSelectorListener(IAutocompleteMultiSelectorListener<UserWrapper> autocompleteMultiSelectorListener)
   {
      this.autocompleteMultiSelectorListener = autocompleteMultiSelectorListener;
   }

   /**
    * 
    */
   private void validateMultiSelectMode()
   {
      if(singleSelect)
      {
         throw new IllegalAccessError("Does not support Multi Selection");
      }
   }

   public boolean isSingleSelect()
   {
      return singleSelect;
   }

   public void setSingleSelect(boolean singleSelect)
   {
      this.singleSelect = singleSelect;
   }

   public boolean isShowOnlineIndicator()
   {
      return showOnlineIndicator;
   }

   public boolean isShowProfileImage()
   {
      return showProfileImage;
   }

   /**
    * @param users
    * @param selectedData
    * @param searchValue
    * @return
    */
   public static List<SelectItem> buildSearchResult(List<User> users, List<UserWrapper> selectedData, String searchValue)
   {
      List<SelectItem> userItems = new ArrayList<SelectItem>(users.size());

      if(CollectionUtils.isNotEmpty(users))
      {
         UserLoginStatistics userLoginStatistics = UserUtils.getUserLoginStatistics(users);
         
         UserWrapper userWrapper;
         if (null == selectedData)
         {
            selectedData = new ArrayList<UserWrapper>();
         }

         for (User user : users)
         {
            LoginStatistics loginStatistics = userLoginStatistics.getLoginStatistics(user.getOID());
            userWrapper = new UserWrapper(user, SessionContext.findSessionContext().getUser(), getUserLabel(user,
                  searchValue), loginStatistics != null ? loginStatistics.currentlyLoggedIn : false);
            if (!selectedData.contains(userWrapper))
            {
               userItems.add(new SelectItem(userWrapper, user.getLastName() + ", " + user.getFirstName()));
            }
         }
      }

      return userItems;
   }

   /**
    * @param user
    * @param searchValue
    * @return
    */
   public static String getUserLabel(User user, String searchValue)
   {
      return formatValue(I18nUtils.getUserLabel(user), searchValue);
   }

   /**
    * @param value
    * @param searchValue
    * @return
    */
   private static String formatValue(String value, String searchValue)
   {
      return value.replaceAll(searchValue, "<b>"+searchValue+"</b>");
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class AutocompleteUserSelectorDataProvider implements IAutocompleteDataProvider
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider#getMatchingData(java.lang.String, int)
       */
      public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
      {
         List<User> users = UserUtils.searchUsers(searchValue + "%", onlyActiveUsers, maxMatches);
         return buildSearchResult(users, !isSingleSelect() ? getSelectedValues() : new ArrayList<UserWrapper>(),
               searchValue);
      }
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   public class AutocompleteUserSelectorListener implements IAutocompleteSelectorListener
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteListener#actionPerformed(javax.faces.model.SelectItem)
       */
      public void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem)
      {
         if (selectedItem.getValue() instanceof UserWrapper) // Safety check
         {
            UserWrapper userWrapper = (UserWrapper)selectedItem.getValue();
            addSelectedUser(userWrapper);
            autoComplete.setValue(null);
         }
      }
   }
}
