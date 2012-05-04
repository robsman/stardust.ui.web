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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.PreferenceManagerBean.VIEW_TYPE;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;

public class CreateOrModifyPreferenceBean extends PopupUIComponentBean
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private boolean modifyMode;

   private PreferenceBean preferenceBean;
   private String selectedView;
   private final SelectItem[] viewSelection = new SelectItem[2];
   private AdminMessagesPropertiesBean propsBean;

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
      Object obj = source.getAttributes().get("editRow");
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
               this.preferenceBean = new PreferenceBean(row.getScope(), row.getModuleId(), row.getPreferenceId(),
                     row.getPreferenceName(), row.getPreferenceValue(), null);
            }
         }
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
      }

      viewSelection[0] = new SelectItem(VIEW_TYPE.TENENT.name(),
            propsBean.getString("views.prefManagerBean.modifyPreference.tenant.label"));
      viewSelection[1] = new SelectItem(VIEW_TYPE.USER.name(),
            propsBean.getString("views.prefManagerBean.modifyPreference.user.label"));
      selectedView = VIEW_TYPE.TENENT.name();
      super.openPopup();
   }

   public void apply()
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      Map<String, Serializable> preferenceMap = new HashMap<String, Serializable>();
      Preferences prefs = null;
      preferenceMap.put(getPreferenceName(), getPreferenceValue());
      if (VIEW_TYPE.TENENT.name().equals(selectedView))
      {
         if (modifyMode)
         {
            prefs = adminService.getPreferences(PreferenceScope.PARTITION, getModuleId(), getPreferenceId());
            prefs.getPreferences().put(getPreferenceName(), getPreferenceValue());
         }
         else
         {
            prefs = new Preferences(PreferenceScope.PARTITION, getModuleId(), getPreferenceId(), preferenceMap);
         }

      }
      else
      {
         if (modifyMode)
         {
            prefs = adminService.getPreferences(PreferenceScope.USER, getModuleId(), getPreferenceId());
            prefs.getPreferences().put(getPreferenceName(), getPreferenceValue());
         }
         else
         {
            UserWrapper userWrapper = getUserSelector().getSelectedValue();
            if (userWrapper != null)
            {
               User u = userWrapper.getUser();
               prefs = new Preferences(PreferenceScope.USER, getModuleId(), getPreferenceId(), preferenceMap);
               prefs.setUserId(u.getId());
               prefs.setRealmId(u.getRealm().getId());
            }
         }
      }
      adminService.savePreferences(prefs);
      PreferenceManagerBean.getCurrent().update();
      closePopup();
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

   public String getModuleId()
   {
      return preferenceBean.getModuleId();
   }

   public void setModuleId(String moduleId)
   {
      preferenceBean.setModuleId(moduleId);
   }

   public String getPreferenceId()
   {
      return preferenceBean.getPreferenceId();
   }

   public void setPreferenceId(String preferenceId)
   {
      preferenceBean.setPreferenceId(preferenceId);
   }

   public String getPreferenceName()
   {
      return preferenceBean.getPreferenceName();
   }

   public void setPreferenceName(String preferenceName)
   {
      preferenceBean.setPreferenceName(preferenceName);
   }

   public String getPreferenceValue()
   {
      return preferenceBean.getPreferenceValue();
   }

   public void setPreferenceValue(String preferenceValue)
   {
      preferenceBean.setPreferenceValue(preferenceValue);
   }

   public UserAutocompleteMultiSelector getUserSelector()
   {
      return preferenceBean.getUserSelector();
   }

   public SelectItem[] getViewSelection()
   {
      return viewSelection;
   }

   public AdminMessagesPropertiesBean getPropsBean()
   {
      return propsBean;
   }

}
