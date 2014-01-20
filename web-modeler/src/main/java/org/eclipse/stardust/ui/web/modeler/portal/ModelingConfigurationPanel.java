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

package org.eclipse.stardust.ui.web.modeler.portal;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener;
import org.eclipse.stardust.ui.web.modeler.portal.messages.Messages;
import org.eclipse.stardust.ui.web.modeler.ui.UserPreferencesEntries;

/**
 * 
 * @author Marc.Gille
 * @author Subodh.Godbole
 */
public class ModelingConfigurationPanel implements UserPreferencesEntries, PortalConfigurationListener
{
   private String defaultProfile;
   private boolean showTechnologyPreview;

   private ConfirmationDialog confirmationDialog;

   /**
    * 
    */
   public ModelingConfigurationPanel()
   {
      PortalConfiguration.getInstance().addListener(this);
      initialize();
   }

   /**
    * @return
    */
   public static String getProfile()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_MODULE);
      return userPrefsHelper.getSingleString(V_MODELER, F_DEFAULT_PROFILE, PROFILE_BA);
   }

   /**
    * @return
    */
   public static boolean isShowTechologyPreview()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_MODULE);
      return userPrefsHelper.getBoolean(V_MODELER, F_TECH_PREVIEW, false);
   }

   /**
    * 
    */
   private void initialize()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();
      
      defaultProfile = userPrefsHelper.getSingleString(V_MODELER, F_DEFAULT_PROFILE, PROFILE_BA);
      showTechnologyPreview = userPrefsHelper.getBoolean(V_MODELER, F_TECH_PREVIEW, false);
   }

   /**
    * 
    */
   public void saveConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.setString(V_MODELER, F_DEFAULT_PROFILE, defaultProfile);
      userPrefsHelper.setString(V_MODELER, F_TECH_PREVIEW, String.valueOf(showTechnologyPreview));

      updateJSWorld();
      
      MessageDialog.addInfoMessage(Messages.getInstance().getString("configuremodeling.panel.saveSuccessful"));
   }
   
   /**
    * 
    */
   public void resetConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.resetValue(V_MODELER, F_DEFAULT_PROFILE);
      userPrefsHelper.resetValue(V_MODELER, F_TECH_PREVIEW);

      updateJSWorld();

      FacesUtils.clearFacesTreeValues();
      initialize();
      MessageDialog.addInfoMessage(Messages.getInstance().getString("configuremodeling.panel.resetSuccessful"));
   }

   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      confirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, new ConfirmationDialogHandler()
            {
               @Override
               public boolean cancel()
               {
                  confirmationDialog = null;
                  return true;
               }
               
               @Override
               public boolean accept()
               {
                  resetConfiguration();
                  confirmationDialog = null;
                  return true;
               }
            });

      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      confirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      confirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            Messages.getInstance().getString("perspectives.ippBpmModeler.label")));
      confirmationDialog.openPopup();
   }

   @Override
   public void preferencesScopeChanged(PreferenceScope arg0)
   {
      initialize();
   }

   @Override
   public boolean preferencesScopeChanging(PreferenceScope arg0)
   {
      return true;
   }

   /**
    * 
    */
   private void updateJSWorld()
   {
      PortalApplication.getInstance().addEventScript("if(window.top.modelingSession){window.top.modelingSession.initialize();}");
   }
   
   /**
    * @return
    */
   private UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(M_MODULE, PortalConfiguration.getInstance()
            .getPrefScopesHelper().getSelectedPreferenceScope());
   }

   public String getDefaultProfile()
   {
      return defaultProfile;
   }

   public void setDefaultProfile(String defaultProfile)
   {
      this.defaultProfile = defaultProfile;
   }
  
   public boolean isShowTechnologyPreview()
   {
      return showTechnologyPreview;
   }

   public void setShowTechnologyPreview(boolean showTechnologyPreview)
   {
      this.showTechnologyPreview = showTechnologyPreview;
   }

   public ConfirmationDialog getConfirmationDialog()
   {
      return confirmationDialog;
   }
}
