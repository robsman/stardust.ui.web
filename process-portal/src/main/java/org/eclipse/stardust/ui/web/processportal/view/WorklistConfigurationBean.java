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
package org.eclipse.stardust.ui.web.processportal.view;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration.ParticipantWorklistColumnConfigurationBean;
import org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration.ProcessWorklistColumnConfigurationBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.FilterProviderUtil;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Subodh.Godbole
 * 
 */
public class WorklistConfigurationBean implements InitializingBean, ConfirmationDialogHandler
{
   public static final String BEAN_NAME = "worklistConfigurationBean";

   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(WorklistConfigurationBean.class);
   private String configFilterProviders;
   private ConfirmationDialog worklistConfirmationDialog;
   private ParticipantWorklistColumnConfigurationBean participantWorklistConfBean;
   private ProcessWorklistColumnConfigurationBean processWorklistConfBean;

   public WorklistConfigurationBean()
   {     
   }

   /**
    * @return
    */
   public static WorklistConfigurationBean getInstance()
   {
      return (WorklistConfigurationBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * TODO temp method 
    */
   public void reload()
   {
      initializeWorklistConf();
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      configFilterProviders = FilterProviderUtil.getInstance().getFilterProviderPreferences();     
      initializeWorklistConf();
   }

   /**
    * 
    */
   public void save()
   {
      setFilterProviderPreferences(configFilterProviders);
      FilterProviderUtil.getInstance().initializeFilterProviders();
      
      saveWorklistColumnConf();
      
      MessageDialog.addInfoMessage(MessagePropertiesBean.getInstance().getString(
            "views.worklistPanelConfiguration.saveSuccessful"));
   }

   private void saveWorklistColumnConf()
   {
      participantWorklistConfBean.save();
      processWorklistConfBean.save();
      initializeWorklistConf();
   }
   
   private void initializeWorklistConf()
   {
      participantWorklistConfBean = new ParticipantWorklistColumnConfigurationBean();
      processWorklistConfBean = new ProcessWorklistColumnConfigurationBean();
   }

   public void reset()
   {
      getUserPreferencesHelper().resetValue(UserPreferencesEntries.V_WORKLIST, UserPreferencesEntries.F_PROVIDERS);
      participantWorklistConfBean.reset();
      processWorklistConfBean.reset();
      FacesUtils.clearFacesTreeValues();
      configFilterProviders = FilterProviderUtil.getInstance().getFilterProviderPreferences();
   }

   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      worklistConfirmationDialog =new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean propsBean = org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean.getInstance();
      worklistConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      worklistConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagePropertiesBean.getInstance().getString("views.worklistPanel.labelTitle")));
      worklistConfirmationDialog.openPopup();
   }
   
   /**
    * 
    */
   public boolean accept()
   {
      reset();
      worklistConfirmationDialog=null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      worklistConfirmationDialog=null;
      return true;
   }
   
   /**
    * @param value
    */
   private void setFilterProviderPreferences(String value)
   {
      getUserPreferencesHelper()
            .setString(UserPreferencesEntries.V_WORKLIST, UserPreferencesEntries.F_PROVIDERS, value);
   }

   /**
    * @return
    */
   private UserPreferencesHelper getUserPreferencesHelper()
   {
      // Filter Providers are always saved in PARTITION and never at USER
      // scope
      return UserPreferencesHelper.getInstance(UserPreferencesEntries.M_WORKFLOW, PreferenceScope.PARTITION);
   }

   public String getConfigFilterProviders()
   {
      return configFilterProviders;
   }

   public void setConfigFilterProviders(String configFilterProviders)
   {
      this.configFilterProviders = configFilterProviders;
   }

   public ConfirmationDialog getWorklistConfirmationDialog()
   {
      return worklistConfirmationDialog;
   }

   public ParticipantWorklistColumnConfigurationBean getParticipantWorklistConfBean()
   {
      return participantWorklistConfBean;
   }

   public ProcessWorklistColumnConfigurationBean getProcessWorklistConfBean()
   {
      return processWorklistConfBean;
   }
}
