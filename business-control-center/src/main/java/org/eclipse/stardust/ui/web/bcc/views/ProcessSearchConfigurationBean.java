/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.bcc.views;

import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Subodh.Godbole
 *
 */
public class ProcessSearchConfigurationBean extends UIComponentBean
      implements InitializingBean, UserPreferencesEntries, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;

   private String archiveAuditTrailUrl;

   private ConfirmationDialog processSearchConfirmationDialog;

   /**
    * 
    */
   public ProcessSearchConfigurationBean()
   {
      super(ResourcePaths.V_processSearch);      
   }

   /**
    * @return
    */
   public static String getArchiveAuditTrailURL()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();
      String url = userPrefsHelper.getSingleString(V_PROCESS_SEARCH, F_ARCHIVE_AUDIT_TRAIL_URL);
      if (StringUtils.isNotEmpty(url) && !url.endsWith("/"))
      {
         url += "/";
      }
      return url;
   }

   @Override
   public void afterPropertiesSet() throws Exception
   {
      initialize();
   }

   @Override
   public void initialize()
   {
      archiveAuditTrailUrl = getArchiveAuditTrailURL();
   }

   /**
    * 
    */
   public void saveConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.setString(V_PROCESS_SEARCH, F_ARCHIVE_AUDIT_TRAIL_URL, String.valueOf(archiveAuditTrailUrl));

      MessageDialog.addInfoMessage(getMessages().getString("config.saveSuccessful"));
   }

   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      processSearchConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean propsBean = org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean
            .getInstance();
      processSearchConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      processSearchConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagesBCCBean.getInstance().getString("views.processSearchView.labelTitle")));
      processSearchConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public void resetConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();
      userPrefsHelper.resetValue(V_PROCESS_SEARCH, F_ARCHIVE_AUDIT_TRAIL_URL);
      initialize();
   }

   @Override
   public boolean accept()
   {
      resetConfiguration();
      processSearchConfirmationDialog = null;
      return true;
   }

   @Override
   public boolean cancel()
   {
      processSearchConfirmationDialog = null;
      return true;
   }

   /**
    * @return
    */
   private static UserPreferencesHelper getUserPrefenceHelper()
   {
      // Always use PARTITION scope
      return UserPreferencesHelper.getInstance(UserPreferencesEntries.M_BCC, PreferenceScope.PARTITION);
   }

   public String getArchiveAuditTrailUrl()
   {
      return archiveAuditTrailUrl;
   }

   public void setArchiveAuditTrailUrl(String archiveAuditTrailUrl)
   {
      this.archiveAuditTrailUrl = archiveAuditTrailUrl;
   }

   public ConfirmationDialog getProcessSearchConfirmationDialog()
   {
      return processSearchConfirmationDialog;
   }
}
