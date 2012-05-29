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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.PreferenceManagerBean.PREF_VIEW_TYPE;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;

public class CreateOrModifyPreferenceBean extends PopupUIComponentBean 
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private final SelectItem[] viewSelection = new SelectItem[2];
   private boolean modifyMode;
   private String selectedView;
   private String userValidationMsg;
   private AdminMessagesPropertiesBean propsBean;
   private PreferenceBean preferenceBean;
   private UserAutocompleteMultiSelector userSelector;
   private AdministrationService adminService;
   private UserWrapper userWrapperObj;

   public CreateOrModifyPreferenceBean()
   {
      super();
      propsBean = AdminMessagesPropertiesBean.getInstance();
   }
   

   /**
    * 
    * @param ae
    */
   public void openModifyPrefDialog(ActionEvent ae)
   {
      UIComponent source = ae.getComponent();
      userValidationMsg = null;
      preferenceBean = null;
      Object obj = source.getAttributes().get("editRow");
      // Get the preference selected in Preference Manager View, to populate the Scope in
      // Add Preference
      String selPrefView = (String) source.getAttributes().get("selectedView");
      selectedView = selPrefView;
      // Get the User selected in Preference Manager View, to populate the Scoped User in
      // Add Preference
      userWrapperObj = (UserWrapper) source.getAttributes().get("selectedUser");
      // If Edit preference is selected , get selected row
      if (null != obj)
      {
         this.modifyMode = true;
         List<PreferenceManagerTableEntry> prefMngrList = PreferenceManagerBean.getCurrent().getPrefManagerTable()
               .getList();
         for (Iterator<PreferenceManagerTableEntry> it = prefMngrList.iterator(); it.hasNext();)
         {
            PreferenceManagerTableEntry row = it.next();
            // Initialize the PreferenceBean with current row selected
            if (row.isSelected())
            {
               this.preferenceBean = new PreferenceBean(row.getModuleId(), row.getPreferenceId(),
                     row.getPreferenceName(), row.getPreferenceValue(), row.getUserId(), row.getRealmId());
               selectedView = row.getScope().contains(PREF_VIEW_TYPE.PARTITION.name()) ? PREF_VIEW_TYPE.PARTITION
                     .name() : PREF_VIEW_TYPE.USER.name();
            }
         }
         // If no row is selected, show error message dialog
         if (null == preferenceBean)
         {
            MessageDialog.addMessage(MessageType.ERROR, propsBean.getString("views.common.error.label"),
                  propsBean.getString("views.prefManagerBean.modifyPreference.confirmEditPref.error"));
            return;
         }
      }
      // When add preference is selected , create new Preference Bean object
      else
      {
         this.preferenceBean = new PreferenceBean();
         this.modifyMode = false;
         viewSelection[0] = new SelectItem(PREF_VIEW_TYPE.PARTITION.name(),
               propsBean.getString("views.prefManagerBean.modifyPreference.tenant.label"));
         viewSelection[1] = new SelectItem(PREF_VIEW_TYPE.USER.name(),
               propsBean.getString("views.prefManagerBean.modifyPreference.user.label"));

      }
      userSelector = new UserAutocompleteMultiSelector(false, true);
      userSelector.setShowOnlineIndicator(false);
      // Auto populate the User Select Autocomplete text
      if (null != userWrapperObj)
      {
         userSelector.setSearchValue(userWrapperObj.getFullName());
      }
      super.openPopup();
   }

   public void apply()
   {
      adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Preferences prefs = null;
      userValidationMsg = null;
      if (PREF_VIEW_TYPE.PARTITION.name().equals(selectedView))
      {
         prefs = updatePreference(PreferenceScope.PARTITION, true);
      }
      else
      {
         prefs = updatePreference(PreferenceScope.USER, false);
      }
      if (null == prefs)
         return;
      else
      {
         adminService.savePreferences(prefs);
      }
         
      PreferenceManagerBean.getCurrent().update();
      closePopup();
   }
   
   private Preferences updatePreference(PreferenceScope prefScope, boolean partitionPrefSelected)
   {
      Preferences prefs = null;
      User user = null;
      Map<String, Serializable> preferenceMap = new HashMap<String, Serializable>();
      preferenceMap.put(preferenceBean.getPreferenceName(), (Serializable) parsePreferenceValue(preferenceBean.getPreferenceValue()));
      UserWrapper userWrapper = getUserSelector().getSelectedValue();
      
      if (partitionPrefSelected)
      {
        prefs = adminService.getPreferences(prefScope, preferenceBean.getModuleId(), preferenceBean.getPreferenceId());
      }
      else
      {
         // Add Preference with User selection
         if (!modifyMode && null != userWrapper)
         {
            user = userWrapper.getUser();
            userWrapperObj = userWrapper;
         }
         else if (!modifyMode && StringUtils.isNotEmpty(getUserSelector().getSearchValue()) && null != userWrapperObj)
         {
            if(userWrapperObj.getFullName().equals(getUserSelector().getSearchValue()))
            {
               user = userWrapperObj.getUser();   
            }
            
         }
         
         if (!modifyMode && null == user)
         {
            // When no user selection is made in 'Add' for User Preference Scope , return
            // null with error msg
            userValidationMsg = propsBean.getString("views.prefManagerBean.modifyPreference.confirmAddPref.error");
            return null;
         }
         
         if (null != user)
         {
            preferenceBean.setUserId(user.getId());
            preferenceBean.setRealmId(user.getRealm().getId());
         }

         List<Preferences> userPrefList = CollectionUtils.newArrayList();
         // AdminServiceImpl does not have method to retrieve specific preference for User
         // , we use QueryService call to get preference for User selected
         userPrefList = SessionContext
               .findSessionContext()
               .getServiceFactory()
               .getQueryService()
               .getAllPreferences(
                     PreferenceQuery.findPreferencesForUsers(preferenceBean.getRealmId(), preferenceBean.getUserId(),
                           preferenceBean.getModuleId(), preferenceBean.getPreferenceId()));
         if (CollectionUtils.isNotEmpty(userPrefList))
         {
            prefs = userPrefList.get(0);
         }
      }
      if (null == prefs)
      {
         prefs = new Preferences(prefScope, preferenceBean.getModuleId(), preferenceBean.getPreferenceId(),
               preferenceMap);
      }
      else
      {
         prefs.getPreferences().put(preferenceBean.getPreferenceName(), (Serializable) parsePreferenceValue(preferenceBean.getPreferenceValue()));
      }
      prefs.setUserId(preferenceBean.getUserId());
      prefs.setRealmId(preferenceBean.getRealmId());
      return prefs;
   }
   
   private Object parsePreferenceValue(String preferenceValue)
   {
      if (null != preferenceValue)
      {
         String prefValue = preferenceValue.toString();
         Scanner numberValidation = new Scanner(prefValue.toString());
         if (numberValidation.hasNextBoolean())
         {
            return Boolean.valueOf(prefValue);
         }
         if (numberValidation.hasNextInt())
         {
            return Integer.valueOf(prefValue);
         }
         else if (numberValidation.hasNextFloat())
         {
            return Float.valueOf(prefValue);
         }
         else if (numberValidation.hasNextDouble())
         {
            return Double.valueOf(prefValue);
         }
         else
            return prefValue;
      }
      else
         return null;
   }

   @Override
   public void initialize()
   {}

   public boolean isModifyMode()
   {
      return modifyMode;
   }

   public String getSelectedView()
   {
      return selectedView;
   }

   public void setSelectedView(String selectedView)
   {
      this.selectedView = selectedView;
   }

   public PreferenceBean getPreferenceBean()
   {
      return preferenceBean;
   }

   public UserAutocompleteMultiSelector getUserSelector()
   {
      return userSelector;
   }

   public SelectItem[] getViewSelection()
   {
      return viewSelection;
   }

   public AdminMessagesPropertiesBean getPropsBean()
   {
      return propsBean;
   }

   public String getUserValidationMsg()
   {
      return userValidationMsg;
   }
}
