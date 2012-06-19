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
package org.eclipse.stardust.ui.web.viewscommon.core;

import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Yogesh.Manware
 *
 */
public class ProcessInstanceDetailConfigurationBean extends UIComponentBean
      implements UserPreferencesEntries, PortalConfigurationListener, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private List<FilterToolbarItem> filterToolbarItems;
   private ConfirmationDialog processDetailConfirmationDialog;

   /**
    * 
    */
   public ProcessInstanceDetailConfigurationBean()
   {
      super("processInstanceDetailsView");
      initialize();
      retrieveConfiguration();

      PortalConfiguration.getInstance().addListener(this);
   }

   @Override
   public void initialize()
   {
      int i = 0;
      filterToolbarItems = new ArrayList<FilterToolbarItem>();

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("nonInteractiveActivities"),
            "processHistory.activityTable.showApplicationActivity",
            "processHistory.activityTable.hideApplicationActivity", "activity_application.png",
            Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("interactiveActivities"),
            "processHistory.activityTable.showManualActivity", "processHistory.activityTable.hideManualActivity",
            "activity_manual.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("auxilliaryActivities"),
            "processHistory.activityTable.showAuxiliaryActivity", "processHistory.activityTable.hideAuxiliaryActivity",
            "activity_auxiliary.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("delegationEvents"),
            "processHistory.activityTable.showDelegate", "processHistory.activityTable.hideDelegate", "delegate.png",
            Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("exceptions"),
            "processHistory.activityTable.showException", "processHistory.activityTable.hideException",
            "exception.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("terminatedEvents"),
            "processHistory.activityTable.showEventsCompleted", "processHistory.activityTable.hideEventsCompleted",
            "activity_completed.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("stateChangeEvents"),
            "processHistory.activityTable.showStateChange", "processHistory.activityTable.hideStateChange",
            "activity_state.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      filterToolbarItems.add(new FilterToolbarItem("" + i++, this.getMessages().getString("auxiliaryProcesses"),
            "processHistory.processTable.showAuxiliaryProcess", "processHistory.processTable.hideAuxiliaryProcess",
            "process_auxiliary.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));
   }

   public List<FilterToolbarItem> getFilterToolbarItems()
   {
      return filterToolbarItems;
   }

   public void setFilterToolbarItems(List<FilterToolbarItem> filterToolbarItems)
   {
      this.filterToolbarItems = filterToolbarItems;
   }

   public void toggleFilter(ActionEvent ae)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String name = (String) context.getExternalContext().getRequestParameterMap().get("name");

      if (name == null || name.length() == 0)
      {
         name = (String) ae.getComponent().getAttributes().get("name");
      }
      for (FilterToolbarItem fTI : filterToolbarItems)
      {
         if (fTI.getName().equals(name))
         {
            fTI.setActive(fTI.isActive() ? false : true);
         }
      }

   }

   /**
    * save
    */
   public void save()
   {
      setApplicationActivityFilter(String.valueOf(filterToolbarItems.get(0).isActive()));
      setManualActivityFilter(String.valueOf(filterToolbarItems.get(1).isActive()));
      setAuxiliaryActivityFilter(String.valueOf(filterToolbarItems.get(2).isActive()));
      setDelegateFilter(String.valueOf(filterToolbarItems.get(3).isActive()));
      setExceptionFilter(String.valueOf(filterToolbarItems.get(4).isActive()));
      setActivityCompletedFilter(String.valueOf(filterToolbarItems.get(5).isActive()));
      setStateChangedFilter(String.valueOf(filterToolbarItems.get(6).isActive()));
      setAuxiliaryProcessFilter(String.valueOf(filterToolbarItems.get(7).isActive()));

      MessageDialog.addInfoMessage(this.getMessages().getString("successConfigurationMsg"));
   }

   /**
    * reset
    */
   public void reset()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_APPLICATION_ACTIVITY);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_MANUAL_ACTIVITY);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_AUXILIARY);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_DELEGATE);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_EXCEPTION);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_ACTIVITY_COMPLETED);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_STATE_CHANGED);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_AUXILIARY_PROCESS);

      retrieveConfiguration();
      MessageDialog.addInfoMessage(this.getMessages().getString("resetConfigurationMsg"));
   }

   /*
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      processDetailConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      processDetailConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      processDetailConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagesViewsCommonBean.getInstance().getString("views.processInstanceDetailsView.labelTitle")));
      processDetailConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      reset();
      processDetailConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      processDetailConfirmationDialog = null;
      return true;
   }

   /**
    * retrieve configurations
    */
   public void retrieveConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();
      
      filterToolbarItems.get(0).setActive(isApplicationActivityFilterOn(userPrefsHelper));
      filterToolbarItems.get(1).setActive(isManualActivityFilterOn(userPrefsHelper));
      filterToolbarItems.get(2).setActive(isAuxiliaryActivityFilterOn(userPrefsHelper));
      filterToolbarItems.get(3).setActive(isDelegateFilterOn(userPrefsHelper));
      filterToolbarItems.get(4).setActive(isExceptionFilterOn(userPrefsHelper));
      filterToolbarItems.get(5).setActive(isActivityCompletedFilterOn(userPrefsHelper));
      filterToolbarItems.get(6).setActive(isStateChangedFilterOn(userPrefsHelper));
      filterToolbarItems.get(7).setActive(isAuxiliaryProcessFilterOn(userPrefsHelper));
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanged
    * (org.eclipse.stardust.engine.core.compatibility.ui.preferences.PreferenceScope)
    */
   public void preferencesScopeChanged(PreferenceScope scope)
   {
      retrieveConfiguration();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanging(org.eclipse.stardust.engine.core.compatibility.ui.preferences.PreferenceScope)
    */
   public boolean preferencesScopeChanging(PreferenceScope scope)
   {
      return true;
   }

   // public getters
   public static Boolean isApplicationActivityFilterOn()
   {
      return isApplicationActivityFilterOn(getUserPrefenceHelperForUser());
   }

   public static Boolean isManualActivityFilterOn()
   {
      return isManualActivityFilterOn(getUserPrefenceHelperForUser());
   }

   public static Boolean isAuxiliaryActivityFilterOn()
   {
      return isAuxiliaryActivityFilterOn(getUserPrefenceHelperForUser());
   }

   public static Boolean isDelegateFilterOn()
   {
      return isDelegateFilterOn(getUserPrefenceHelperForUser());
   }

   public static Boolean isExceptionFilterOn()
   {
      return isExceptionFilterOn(getUserPrefenceHelperForUser());
   }

   public static Boolean isActivityCompletedFilterOn()
   {
      return isActivityCompletedFilterOn(getUserPrefenceHelperForUser());
   }

   public static Boolean isStateChangedFilterOn()
   {
      return isStateChangedFilterOn(getUserPrefenceHelperForUser());
   }

   public static Boolean isAuxiliaryProcessFilterOn()
   {
      return isAuxiliaryProcessFilterOn(getUserPrefenceHelperForUser());
   }

   // setters
   private static void setApplicationActivityFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_APPLICATION_ACTIVITY, value);
   }

   private static void setManualActivityFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_MANUAL_ACTIVITY, value);
   }

   private static void setAuxiliaryActivityFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_AUXILIARY, value);
   }

   private static void setDelegateFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_DELEGATE, value);
   }

   private static void setExceptionFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_EXCEPTION, value);
   }

   private static void setActivityCompletedFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_ACTIVITY_COMPLETED, value);
   }

   private static void setStateChangedFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_STATE_CHANGED, value);
   }

   private static void setAuxiliaryProcessFilter(String value)
   {
      getUserPrefenceHelper().setString(V_PORTAL_CONFIG, F_AUXILIARY_PROCESS, value);
   }

   private static UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(M_VIEWS_COMMON, PortalConfiguration.getInstance().getPrefScopesHelper()
            .getSelectedPreferenceScope());
   }
   
   private static UserPreferencesHelper getUserPrefenceHelperForUser()
   {
      return UserPreferencesHelper.getInstance(M_VIEWS_COMMON, PreferenceScope.USER);
   }
   
   private static Boolean getBooleanUserPreferencesValue(UserPreferencesHelper userPrefsHelper, String featureId,
         boolean defaultValue)
   {
      String value = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, featureId);
      return StringUtils.isEmpty(value) ? defaultValue : Boolean.valueOf(value);
   }
   
   private static Boolean isApplicationActivityFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_APPLICATION_ACTIVITY, true);
   }

   private static Boolean isManualActivityFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_MANUAL_ACTIVITY, true);
   }

   private static Boolean isAuxiliaryActivityFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_AUXILIARY, true);
   }

   private static Boolean isDelegateFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_DELEGATE, true);
   }

   private static Boolean isExceptionFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_EXCEPTION, true);
   }

   private static Boolean isActivityCompletedFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_ACTIVITY_COMPLETED, true);
   }

   private static Boolean isStateChangedFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_STATE_CHANGED, true);
   }

   private static Boolean isAuxiliaryProcessFilterOn(UserPreferencesHelper userPrefsHelper)
   {
      return getBooleanUserPreferencesValue(userPrefsHelper, F_AUXILIARY_PROCESS, true);
   }

   public ConfirmationDialog getProcessDetailConfirmationDialog()
   {
      return processDetailConfirmationDialog;
   }
   
   

}
