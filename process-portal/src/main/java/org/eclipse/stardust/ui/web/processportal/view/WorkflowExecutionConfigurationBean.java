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

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
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
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;



/**
 * @author Shrikant.Gangal
 * 
 */
public class WorkflowExecutionConfigurationBean extends UIComponentBean implements PortalConfigurationListener, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 8370173246308943L;
   private static final String BEAN_NAME = "ippWorkflowExecutionConfigurationBean";
   private static final String ABORT_PROCESS_SCOPE_PREFIX = "views.workflowExecution.config.abortProcess.scope.";
   private static final String ABORT_ACTIVITY_SCOPE_PREFIX = "views.workflowExecution.config.abortActivity.scope.";
   private static final String PROMPT = "prompt";
   private static final String ROOT = "root";
   private static final String SUB = "sub";

   private boolean suppressBlankDescriptors;
   private boolean propagatePriority;
   //abort process 
   private  SelectItem[] abortProcessScopes;
   private String selectedAbortProcessScope;
   
   //abort activity
   private  SelectItem[] abortActivityScopes;
   private String selectedAbortActivityScope;
   
   private MessagePropertiesBean messageBean;
   private UserProvider userProvider;
   private ConfirmationDialog workflowConfirmationDialog;

   /**
    * 
    */
   public WorkflowExecutionConfigurationBean()
   {
      PortalConfiguration.getInstance().addListener(this);
      initialize();
   }

   /**
    * @return
    */
   public static WorkflowExecutionConfigurationBean getInstance()
   {
      return (WorkflowExecutionConfigurationBean) FacesContext.getCurrentInstance().getApplication()
            .getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(), BEAN_NAME);
   }

   @Override
   public void initialize()
   {
      messageBean = MessagePropertiesBean.getInstance();
      UserPreferencesHelper userPrefHelper = getUserPrefenceHelper();

      suppressBlankDescriptors = userPrefHelper.getBoolean(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_SUPPRESS_BLANK_DESCRIPTORS, true);
      propagatePriority = userPrefHelper.getBoolean(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_PROPAGATE_PRIORITY, false);
      
      //initialize abort process scopes
      MessagePropertiesBean propertiesBean = MessagePropertiesBean.getInstance();
      abortProcessScopes = new SelectItem[3];
      abortProcessScopes[0] = new SelectItem("", propertiesBean.getString(ABORT_PROCESS_SCOPE_PREFIX + PROMPT));
      abortProcessScopes[1] = new SelectItem(AbortScope.SUB_HIERARCHY, propertiesBean
            .getString(ABORT_PROCESS_SCOPE_PREFIX + SUB));
      abortProcessScopes[2] = new SelectItem(AbortScope.ROOT_HIERARCHY, propertiesBean
            .getString(ABORT_PROCESS_SCOPE_PREFIX + ROOT));
      
      selectedAbortProcessScope = userPrefHelper.getSingleString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_PROCESS_ABORT_SCOPE);

      // initialize abort Activity scopes
      abortActivityScopes = new SelectItem[3];
      abortActivityScopes[0] = new SelectItem("", propertiesBean.getString(ABORT_ACTIVITY_SCOPE_PREFIX + PROMPT));
      abortActivityScopes[1] = new SelectItem(AbortScope.SUB_HIERARCHY, propertiesBean
            .getString(ABORT_ACTIVITY_SCOPE_PREFIX + SUB));
      abortActivityScopes[2] = new SelectItem(AbortScope.ROOT_HIERARCHY, propertiesBean
            .getString(ABORT_ACTIVITY_SCOPE_PREFIX + ROOT));
      
      selectedAbortActivityScope = userPrefHelper.getSingleString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_ACTIVITY_ABORT_SCOPE); 
   }

   /**
    * 
    */
   public void saveConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.setString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_SUPPRESS_BLANK_DESCRIPTORS, String.valueOf(isSuppressBlankDescriptors()));
      
      userPrefsHelper.setString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_PROPAGATE_PRIORITY, String.valueOf(isPropagatePriority()));
      
      if (userProvider.getUser().isAdministrator())
      {
         userPrefsHelper.setString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
               UserPreferencesEntries.F_PROCESS_ABORT_SCOPE, selectedAbortProcessScope);
         
         userPrefsHelper.setString(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
               UserPreferencesEntries.F_ACTIVITY_ABORT_SCOPE, selectedAbortActivityScope);
      }

      MessageDialog.addInfoMessage(messageBean.getString("views.workflowExecution.config.saveSuccessful"));
   }

   /**
    * 
    */
   public void resetConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.resetValue(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_SUPPRESS_BLANK_DESCRIPTORS);
      
      userPrefsHelper.resetValue(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_PROPAGATE_PRIORITY);

      if (userProvider.getUser().isAdministrator())
      {
         userPrefsHelper.resetValue(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
               UserPreferencesEntries.F_PROCESS_ABORT_SCOPE);
         
         userPrefsHelper.resetValue(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
               UserPreferencesEntries.F_ACTIVITY_ABORT_SCOPE);
      }

      FacesUtils.clearFacesTreeValues();

      initialize();
      MessageDialog.addInfoMessage(messageBean.getString("views.workflowExecution.config.resetSuccessful"));
      FacesUtils.refreshPage();
   }
   
   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      workflowConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean propsBean = org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean
            .getInstance();
      workflowConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      workflowConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagePropertiesBean.getInstance().getString("perspectives.WorkflowExecution.label")));
      workflowConfirmationDialog.openPopup();
   }

   /**
    * 
    */
   public boolean accept()
   {
      resetConfiguration();
      workflowConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      workflowConfirmationDialog = null;
      return true;
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanged
    * (org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public void preferencesScopeChanged(PreferenceScope scope)
   {
      initialize();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanging(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public boolean preferencesScopeChanging(PreferenceScope scope)
   {
      return false;
   }

   /**
    * @return
    */
   public boolean isSuppressBlankDescriptors()
   {
      return suppressBlankDescriptors;
   }

   /**
    * @param suppressBlankDescriptors
    */
   public void setSuppressBlankDescriptors(boolean suppressBlankDescriptors)
   {
      this.suppressBlankDescriptors = suppressBlankDescriptors;
   }
   public boolean isPropagatePriority()
   {
      return propagatePriority;
   }

   public void setPropagatePriority(boolean propagatePriority)
   {
      this.propagatePriority = propagatePriority;
   }

   private UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON, PortalConfiguration.getInstance()
            .getPrefScopesHelper().getSelectedPreferenceScope());
   }

   public String getSelectedAbortProcessScope()
   {
      return selectedAbortProcessScope;
   }

   public void setSelectedAbortProcessScope(String selectedAbortProcessScope)
   {
      this.selectedAbortProcessScope = selectedAbortProcessScope;
   }

   public SelectItem[] getAbortProcessScopes()
   {
      return abortProcessScopes;
   }
   
   public SelectItem[] getAbortActivityScopes()
   {
      return abortActivityScopes;
   }

   public String getSelectedAbortActivityScope()
   {
      return selectedAbortActivityScope;
   }

   public void setSelectedAbortActivityScope(String selectedAbortActivityScope)
   {
      this.selectedAbortActivityScope = selectedAbortActivityScope;
   }

   public void setUserProvider(UserProvider userProvider)
   {
      this.userProvider = userProvider;
   }

   public ConfirmationDialog getWorkflowConfirmationDialog()
   {
      return workflowConfirmationDialog;
   }
   
   
}