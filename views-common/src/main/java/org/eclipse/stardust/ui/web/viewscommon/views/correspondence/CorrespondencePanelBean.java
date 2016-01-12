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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.CorrespondencePanelPreferenceUtils;

/**
 * @author Aditya.Gaikwad
 * 
 */
public class CorrespondencePanelBean extends PopupUIComponentBean implements ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "correspondencePanelBean";

   private String numberFormat;
   private String providerSuffix;

   private String selectedDefaultType;

   private User user;

   private ICallbackHandler callbackHandler;

   private ConfirmationDialog correspondenceConfirmationDlg;

   /**
    * 
    */
   public CorrespondencePanelBean()
   {
      user = SessionContext.findSessionContext().getUser();
      initializeView();
   }

   /**
    * @return
    */
   public static CorrespondencePanelBean getInstance()
   {
      return (CorrespondencePanelBean) org.eclipse.stardust.ui.web.common.util.FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * saves userBean
    */
   public void onApply()
   {
      CorrespondencePanelPreferenceUtils.savePreference(UserPreferencesEntries.F_CORRESPONDENCE_DEFAULT_TYPE,
            selectedDefaultType);
      CorrespondencePanelPreferenceUtils.savePreference(UserPreferencesEntries.F_CORRESPONDENCE_NUMBER_FORMAT,
            numberFormat);
      CorrespondencePanelPreferenceUtils.savePreference(UserPreferencesEntries.F_CORRESPONDENCE_PROVIDER_SUFFIX,
            providerSuffix);

      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      MessageDialog.addInfoMessage(propsBean.getString("common.configuration.saveConfirmation"));
      closePopup();

      if (callbackHandler != null)
         callbackHandler.handleEvent(EventType.APPLY);

   }

   /**
    * 
    */
   private void initializeView()
   {
      selectedDefaultType = CorrespondencePanelPreferenceUtils.getCorrespondencePreferenceForUser(user,
            UserPreferencesEntries.F_CORRESPONDENCE_DEFAULT_TYPE);
      numberFormat = CorrespondencePanelPreferenceUtils.getCorrespondencePreferenceForUser(user,
            UserPreferencesEntries.F_CORRESPONDENCE_NUMBER_FORMAT);
      providerSuffix = CorrespondencePanelPreferenceUtils.getCorrespondencePreferenceForUser(user,
            UserPreferencesEntries.F_CORRESPONDENCE_PROVIDER_SUFFIX);
   }

   /**
    * 
    */
   public void resetConfiguration()
   {
      FacesUtils.clearFacesTreeValues();
      initializeView();
      MessageDialog.addInfoMessage(MessagesViewsCommonBean.getInstance().getString("views.common.config.reset"));
   }

   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      correspondenceConfirmationDlg = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      correspondenceConfirmationDlg.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      correspondenceConfirmationDlg.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagesViewsCommonBean.getInstance().getString("views.userProfileView.labelTitle")));
      correspondenceConfirmationDlg.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      resetConfiguration();
      correspondenceConfirmationDlg = null;
      return false;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      correspondenceConfirmationDlg = null;
      return false;
   }

   // ***************** Default Getter and Setter Methods ***********************

   public User getUser()
   {
      return user;
   }

   public void setICallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public String getNumberFormat()
   {
      return numberFormat;
   }

   public void setNumberFormat(String numberFormat)
   {
      this.numberFormat = numberFormat;
   }

   public String getProviderSuffix()
   {
      return providerSuffix;
   }

   public void setProviderSuffix(String providerSuffix)
   {
      this.providerSuffix = providerSuffix;
   }

   public String getSelectedDefaultType()
   {
      return selectedDefaultType;
   }

   public void setSelectedDefaultType(String selectedDefaultType)
   {
      this.selectedDefaultType = selectedDefaultType;
   }

   @Override
   public void initialize()
   {}

   public ConfirmationDialog getCorrespondenceConfirmationDlg()
   {
      return correspondenceConfirmationDlg;
   }

}