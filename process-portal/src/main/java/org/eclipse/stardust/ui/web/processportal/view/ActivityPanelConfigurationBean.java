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

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

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
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.views.PortalConfiguration;
import org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;



/**
 * @author Subodh.Godbole
 *
 */
public class ActivityPanelConfigurationBean extends UIComponentBean implements UserPreferencesEntries, PortalConfigurationListener, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;

   public static final String DISPLAY_DOCUMENTS_OLDEST = "OLDEST";
   public static final String DISPLAY_DOCUMENTS_ALL = "ALL";
   
   public static final String PIN_ACTIVITY_VIEW_VERTICALLY = "VERTICAL";
   public static final String PIN_ACTIVITY_VIEW_HORIZONTALLY = "HPRIZONTAL";
   
   private boolean displayDocuments;
   private String displayDocumentType;
   private boolean displayMappedDocuments;
   private boolean displayNotes;
   private boolean displayProcessDetails;
   private boolean minimizeLaunchPanels;
   private boolean maximizeView;
   private boolean closeRelatedViews;
   private String documentDisplayMode;
   private boolean pinActivityView;
   private String pinActivityViewType;
   private boolean showMappedDocumentWarning;
   private ConfirmationDialog activityPnlConfirmationDialog;

   private List<SelectItem> availableNoOfColumnsInColumnLayout;
   private int noOfColumnsInColumnLayout;
   private List<SelectItem> availableNoOfColumnsInTable;
   private int noOfColumnsInTable;

   /**
    * 
    */
   public ActivityPanelConfigurationBean()
   {
      super("activityPanel");
      PortalConfiguration.getInstance().addListener(this);
      
      // Columns in Column Layout
      noOfColumnsInColumnLayout = 3;
      availableNoOfColumnsInColumnLayout = new ArrayList<SelectItem>();
      for (int i = 1; i <= 7; i++)
      {
         availableNoOfColumnsInColumnLayout.add(new SelectItem(i, String.valueOf(i)));
      }

      // Columns in Table
      noOfColumnsInTable = 0;
      availableNoOfColumnsInTable = new ArrayList<SelectItem>();
      for (int i = 1; i <= 9; i++)
      {
         availableNoOfColumnsInTable.add(new SelectItem(i, String.valueOf(i)));
      }
      availableNoOfColumnsInTable.add(new SelectItem(0, getMessages().getString(
            "config.autoGeneration.noOfColumnsInTable.option.all")));

      initialize();
   }

   /**
    * @return
    */
   public static boolean isAutoDisplayDocuments()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS, true);
   }
   
   /**
    * @return
    */
   public static String getAutoDisplayDocumentsType()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getSingleString(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS_TYPE, DISPLAY_DOCUMENTS_OLDEST);
   }

   /**
    * @return
    */
   public static String getAutoDocumentsDisplayMode()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getSingleString(V_ACTIVITY_PANEL, F_DOCUMENTS_DISPLAY_MODE,
            RepositoryUtility.DOCUMENT_DISPLAY_MODE_PORTAL);
   }
   
   /**
    * @return
    */
   public static boolean isAutoDisplayNotes()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_NOTES, false);
   }

   /**
    * @return
    */
   public static boolean isAutoDisplayMappedDocuments()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_MAPPED_DOCS, true);
   }

   /**
    * @return
    */
   public static boolean isAutoDisplayProcessDetails()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_PROCESS_DETAILS, false);
   }

   /**
    * @return
    */
   public static boolean isAutoMinimizeLaunchPanels()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_MINIMIZE_LAUNCH_PANELS, false);
   }

   /**
    * @return
    */
   public static boolean isAutoMaximizeView()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_MAXIMIZE_VIEW, false);
   }

   /**
    * @return
    */
   public static boolean isAutoPinActivityView()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW, false);
   }
   
   /**
    * @return
    */
   public static String isAutoPinActivityViewType()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getSingleString(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW_TYPE, PIN_ACTIVITY_VIEW_VERTICALLY);
   }
   
   /**
    * @return
    */
   public static boolean isAutoCloseRelatedViews()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_CLOSE_RELATED_VIEWS, true);
   }

   /**
    * @return
    */
   public static boolean isAutoShowMappedDocumentWarning()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_SHOW_MAPPED_DOC_WARNING, true);
   }

   /**
    * @param flag
    */
   public static void setAutoShowMappedDocumentWarning(boolean flag)
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_SHOW_MAPPED_DOC_WARNING, String.valueOf(flag));
   }

   /**
    * @return
    */
   public static int getAutoNoOfColumnsInColumnLayout()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getInteger(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_COLUMN_LAYOUT, 3);
   }

   /**
    * @return
    */
   public static int getAutoNoOfColumnsInTable()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_WORKFLOW);
      return userPrefsHelper.getInteger(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_TABLE, 0);
   }

   @Override
   public void initialize()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      displayDocuments = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS, true);
      displayDocumentType = userPrefsHelper.getSingleString(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS_TYPE, DISPLAY_DOCUMENTS_OLDEST);
      displayMappedDocuments = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_MAPPED_DOCS, true);
      displayNotes = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_NOTES, false);
      displayProcessDetails = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_DISPLAY_PROCESS_DETAILS, false);
      minimizeLaunchPanels = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_MINIMIZE_LAUNCH_PANELS, false);
      maximizeView = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_MAXIMIZE_VIEW, false);
      closeRelatedViews = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_CLOSE_RELATED_VIEWS, true);
      documentDisplayMode = userPrefsHelper.getSingleString(V_ACTIVITY_PANEL, F_DOCUMENTS_DISPLAY_MODE, RepositoryUtility.DOCUMENT_DISPLAY_MODE_PORTAL);
      pinActivityView = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW, false);
      pinActivityViewType = userPrefsHelper.getSingleString(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW_TYPE, PIN_ACTIVITY_VIEW_VERTICALLY);
      showMappedDocumentWarning = userPrefsHelper.getBoolean(V_ACTIVITY_PANEL, F_SHOW_MAPPED_DOC_WARNING, true);
      noOfColumnsInColumnLayout = userPrefsHelper.getInteger(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_COLUMN_LAYOUT, 3);
      noOfColumnsInTable = userPrefsHelper.getInteger(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_TABLE, 0);
   }

   /**
    * 
    */
   public void saveConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS, String.valueOf(displayDocuments));
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS_TYPE, displayDocumentType);
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_DISPLAY_MAPPED_DOCS, String.valueOf(displayMappedDocuments));
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_DISPLAY_NOTES, String.valueOf(displayNotes));
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_DISPLAY_PROCESS_DETAILS, String.valueOf(displayProcessDetails));
      //userPrefsHelper.setString(V_ACTIVITY_PANEL, F_MINIMIZE_LAUNCH_PANELS, String.valueOf(minimizeLaunchPanels));
      //userPrefsHelper.setString(V_ACTIVITY_PANEL, F_MAXIMIZE_VIEW, String.valueOf(maximizeView));
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_CLOSE_RELATED_VIEWS, String.valueOf(closeRelatedViews));
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_DOCUMENTS_DISPLAY_MODE, documentDisplayMode);
      //userPrefsHelper.setString(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW, String.valueOf(pinActivityView));
      //userPrefsHelper.setString(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW_TYPE, pinActivityViewType);
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_SHOW_MAPPED_DOC_WARNING, String.valueOf(showMappedDocumentWarning));
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_COLUMN_LAYOUT, String.valueOf(noOfColumnsInColumnLayout));
      userPrefsHelper.setString(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_TABLE, String.valueOf(noOfColumnsInTable));

      MessageDialog.addInfoMessage(getMessages().getString("config.saveSuccessful"));
   }

   /**
    * 
    */
   public void resetConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = getUserPrefenceHelper();

      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_DISPLAY_DOCUMENTS_TYPE);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_DISPLAY_NOTES);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_DISPLAY_PROCESS_DETAILS);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_MINIMIZE_LAUNCH_PANELS);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_MAXIMIZE_VIEW);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_CLOSE_RELATED_VIEWS);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_DOCUMENTS_DISPLAY_MODE);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_PIN_ACTIVITY_VIEW_TYPE);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_COLUMN_LAYOUT);
      userPrefsHelper.resetValue(V_ACTIVITY_PANEL, F_NO_OF_COLUMNS_IN_TABLE);

      FacesUtils.clearFacesTreeValues();
      initialize();
      MessageDialog.addInfoMessage(getMessages().getString("config.resetSuccessful"));
   }

   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      activityPnlConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean propsBean = org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean
            .getInstance();
      activityPnlConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      activityPnlConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            MessagePropertiesBean.getInstance().getString("views.activityPanel.labelTitle")));
      activityPnlConfirmationDialog.openPopup();
   }
   
   /**
    * 
    */
   public boolean accept()
   {
      resetConfiguration();
      activityPnlConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      activityPnlConfirmationDialog = null;
      return true;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanging(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public boolean preferencesScopeChanging(PreferenceScope pScope)
   {
      return true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanged(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public void preferencesScopeChanged(PreferenceScope pScope)
   {
      initialize();
   }

   /**
    * @return
    */
   private UserPreferencesHelper getUserPrefenceHelper()
   {
      return UserPreferencesHelper.getInstance(UserPreferencesEntries.M_WORKFLOW, PortalConfiguration.getInstance()
            .getPrefScopesHelper().getSelectedPreferenceScope());
   }

   public boolean isDisplayDocuments()
   {
      return displayDocuments;
   }

   public void setDisplayDocuments(boolean displayDocuments)
   {
      this.displayDocuments = displayDocuments;
   }

   public String getDisplayDocumentType()
   {
      return displayDocumentType;
   }

   public void setDisplayDocumentType(String displayDocumentType)
   {
      this.displayDocumentType = displayDocumentType;
   }

   public boolean isDisplayMappedDocuments()
   {
      return displayMappedDocuments;
   }

   public void setDisplayMappedDocuments(boolean displayMappedDocuments)
   {
      this.displayMappedDocuments = displayMappedDocuments;
   }

   public boolean isDisplayNotes()
   {
      return displayNotes;
   }

   public void setDisplayNotes(boolean displayNotes)
   {
      this.displayNotes = displayNotes;
   }

   public boolean isDisplayProcessDetails()
   {
      return displayProcessDetails;
   }

   public void setDisplayProcessDetails(boolean displayProcessDetails)
   {
      this.displayProcessDetails = displayProcessDetails;
   }

   public boolean isMinimizeLaunchPanels()
   {
      return minimizeLaunchPanels;
   }

   public void setMinimizeLaunchPanels(boolean minimizeLaunchPanels)
   {
      this.minimizeLaunchPanels = minimizeLaunchPanels;
   }

   public boolean isMaximizeView()
   {
      return maximizeView;
   }

   public void setMaximizeView(boolean maximizeView)
   {
      this.maximizeView = maximizeView;
   }

   public boolean isCloseRelatedViews()
   {
      return closeRelatedViews;
   }

   public void setCloseRelatedViews(boolean closeRelatedViews)
   {
      this.closeRelatedViews = closeRelatedViews;
   }

   public String getDocumentDisplayMode()
   {
      return documentDisplayMode;
   }

   public void setDocumentDisplayMode(String documentDisplayMode)
   {
      this.documentDisplayMode = documentDisplayMode;
   }

   public boolean isPinActivityView()
   {
      return pinActivityView;
   }

   public void setPinActivityView(boolean pinActivityView)
   {
      this.pinActivityView = pinActivityView;
   }

   public String getPinActivityViewType()
   {
      return pinActivityViewType;
   }

   public void setPinActivityViewType(String pinActivityViewType)
   {
      this.pinActivityViewType = pinActivityViewType;
   }

   public boolean isShowMappedDocumentWarning()
   {
      return showMappedDocumentWarning;
   }

   public void setShowMappedDocumentWarning(boolean showMappedDocumentWarning)
   {
      this.showMappedDocumentWarning = showMappedDocumentWarning;
   }

   public ConfirmationDialog getActivityPnlConfirmationDialog()
   {
      return activityPnlConfirmationDialog;
   }

   public int getNoOfColumnsInColumnLayout()
   {
      return noOfColumnsInColumnLayout;
   }

   public void setNoOfColumnsInColumnLayout(int noOfColumnsInColumnLayout)
   {
      this.noOfColumnsInColumnLayout = noOfColumnsInColumnLayout;
   }

   public int getNoOfColumnsInTable()
   {
      return noOfColumnsInTable;
   }

   public void setNoOfColumnsInTable(int noOfColumnsInTable)
   {
      this.noOfColumnsInTable = noOfColumnsInTable;
   }

   public List<SelectItem> getAvailableNoOfColumnsInColumnLayout()
   {
      return availableNoOfColumnsInColumnLayout;
   }

   public List<SelectItem> getAvailableNoOfColumnsInTable()
   {
      return availableNoOfColumnsInTable;
   }
}
