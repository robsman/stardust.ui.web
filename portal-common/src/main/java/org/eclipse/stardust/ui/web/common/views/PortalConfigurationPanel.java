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
package org.eclipse.stardust.ui.web.common.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstants;
import org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstantsAdapter;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.spi.theme.Theme;
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.spi.user.UserProvider;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.icesoft.faces.component.menubar.MenuItem;


/**
 * @author subodh.godbole
 *
 */
public class PortalConfigurationPanel extends UIComponentBean
      implements UserPreferencesEntries, ConfigurationConstants, PortalConfigurationListener, InitializingBean ,ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;

   private static Logger trace = LogManager.getLogger(PortalConfigurationPanel.class);
   
   private Integer maxTabsDisplay;
   private Integer pageSize;
   private Integer paginatorMaxPages;
   private Integer paginatorFastStep;

   private List<SelectItem> availableSkins;
   private String selectedSkin;
   
   private UserProvider userProvider;
   private ThemeProvider themeProvider;
   private ConfirmationDialog portalConfirmationDialog;
   
   private List<SelectItem> availablePerspectives;
   private String selectedPerspective;

   /**
    * 
    */
   public PortalConfigurationPanel()
   {
      PortalConfiguration.getInstance().addListener(this);
   }

   /* (non-Javadoc)
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      initialize();
   }
   
   @Override
   public void initialize()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_PORTAL,
            PortalConfiguration.getInstance().getPrefScopesHelper()
                  .getSelectedPreferenceScope());
      // SKINS
      availableSkins = new ArrayList<SelectItem>();
      
      List<Theme> themes = themeProvider.getThemes();
      for (Theme theme : themes)
      {
         availableSkins.add(new SelectItem(theme.getThemeId(), theme.getThemeName()));
      }
      
      selectedSkin = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, F_SKIN);

      // OTHERS OPTIONS
      maxTabsDisplay = getIntUserPreferencesValue(userPrefsHelper,
            F_TABS_MAX_TABS_DISPLAY, DEFAULT_MAX_TAB_DISPLAY);
      pageSize = getIntUserPreferencesValue(userPrefsHelper, 
            F_PAGINATOR_PAGE_SIZE, DEFAULT_PAGE_SIZE);
      paginatorMaxPages = getIntUserPreferencesValue(userPrefsHelper,
            F_PAGINATOR_MAX_PAGES, DEFAULT_MAX_PAGES);
      paginatorFastStep = getIntUserPreferencesValue(userPrefsHelper,
            F_PAGINATOR_FAST_STEP, DEFAULT_FAST_STEP);
      
      // PERSPECTIVES
      selectedPerspective = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, F_DEFAULT_PERSPECTIVE);

      PortalApplication portalApp = PortalApplication.getInstance();
      boolean perspectiveAvailable = false;
      IPerspectiveDefinition xmlConfigDefaultPerspetive = null;
      IPerspectiveDefinition currPerspective;
      availablePerspectives = new ArrayList<SelectItem>();

      List<MenuItem> perspectives = portalApp.getPortalUiController().getPerspectiveItems();
      for (MenuItem item : perspectives)
      {
         availablePerspectives.add(new SelectItem(item.getId(), item.getTitle()));

         currPerspective = portalApp.getPortalUiController().getPerspective(item.getId());

         if (null == xmlConfigDefaultPerspetive && currPerspective.isDefaultPerspective())
         {
            xmlConfigDefaultPerspetive = currPerspective;
         }
         
         if (StringUtils.isEmpty(selectedPerspective) && currPerspective.isDefaultPerspective())
         {
            selectedPerspective = item.getId();
            perspectiveAvailable = true;
         }
         else if (item.getId().equals(selectedPerspective))
         {
            perspectiveAvailable = true;
         }
      }

      if(StringUtils.isNotEmpty(selectedPerspective) && !perspectiveAvailable)
      {
         trace.warn("Cannot load default Perspective, either it's not available or user is not authorized - "
               + selectedPerspective);
         
         // Fall back to available default Perspective
         if (null != xmlConfigDefaultPerspetive)
         {
            selectedPerspective = xmlConfigDefaultPerspetive.getName();
         }
      }
   }

   /**
    * 
    */
   public void saveConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_PORTAL,
            PortalConfiguration.getInstance().getPrefScopesHelper()
                  .getSelectedPreferenceScope());

      if (userProvider.getUser().isAdministrator())
      {
         userPrefsHelper.setString(V_PORTAL_CONFIG, F_SKIN, selectedSkin);
      }
      userPrefsHelper.setString(V_PORTAL_CONFIG, F_DEFAULT_PERSPECTIVE, selectedPerspective);
      userPrefsHelper.setString(V_PORTAL_CONFIG, F_TABS_MAX_TABS_DISPLAY, String.valueOf(maxTabsDisplay));
      userPrefsHelper.setString(V_PORTAL_CONFIG, F_PAGINATOR_PAGE_SIZE, String.valueOf(pageSize));
      userPrefsHelper.setString(V_PORTAL_CONFIG, F_PAGINATOR_MAX_PAGES, String.valueOf(paginatorMaxPages));
      userPrefsHelper.setString(V_PORTAL_CONFIG, F_PAGINATOR_FAST_STEP, String.valueOf(paginatorFastStep));
      
      PortalApplication.getInstance().refreshSkin();
      
      MessageDialog.addInfoMessage(MessagePropertiesBean.getInstance().getString(
            "views.configurationPanel.saveSuccessful"));
   }
   
   /**
    * 
    */
   public void resetConfiguration()
   {
      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_PORTAL,
            PortalConfiguration.getInstance().getPrefScopesHelper()
                  .getSelectedPreferenceScope());

      if (userProvider.getUser().isAdministrator())
      {
         userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_SKIN);
      }
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_DEFAULT_PERSPECTIVE);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_TABS_MAX_TABS_DISPLAY);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_PAGINATOR_PAGE_SIZE);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_PAGINATOR_MAX_PAGES);
      userPrefsHelper.resetValue(V_PORTAL_CONFIG, F_PAGINATOR_FAST_STEP);
      
      FacesUtils.clearFacesTreeValues();
      initialize();
      PortalApplication.getInstance().refreshSkin();

      MessageDialog.addInfoMessage(MessagePropertiesBean.getInstance().getString(
            "views.configurationPanel.resetSuccessful"));
   }
   
   /**
    * Confirmation Dialog before reseting Config data.
    */
   public void openConfirmationDialog()
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      Iterator<String> facesMessageIds = facesContext.getClientIdsWithMessages();
      while (facesMessageIds.hasNext())
      {
         // Clears the Error messages on the Page
         FacesUtils.clearFacesComponentValues(FacesUtils.matchComponentInHierarchy(FacesContext.getCurrentInstance()
               .getViewRoot(), facesMessageIds.next()));
      }
	   portalConfirmationDialog = new ConfirmationDialog(DialogContentType.WARNING, DialogActionType.YES_NO,
            DialogType.NORMAL, DialogStyle.COMPACT, this);
      MessagePropertiesBean propsBean = MessagePropertiesBean.getInstance();
      portalConfirmationDialog.setTitle(propsBean.getString("common.configurationPanel.confirmResetTitle"));
      portalConfirmationDialog.setMessage(propsBean.getParamString("common.configurationPanel.confirmReset",
            propsBean.getString("views.configurationTreeView.label")));
      portalConfirmationDialog.openPopup();
   }
   
   /**
    * 
    */
   public boolean accept()
   {
      resetConfiguration();
      portalConfirmationDialog = null;
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      portalConfirmationDialog = null;
      return true;
   }

   /**
    * @param userPrefsHelper
    * @param featureId
    * @param defaultValue
    * @return
    */
   private int getIntUserPreferencesValue(UserPreferencesHelper userPrefsHelper,
         String featureId, int defaultValue)
   {
      String value = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, featureId);
      return StringUtils.isEmpty(value) ? defaultValue : Integer.valueOf(value);
   }

   public Integer getMaxTabsDisplay()
   {
      return maxTabsDisplay;
   }

   public void setMaxTabsDisplay(Integer maxTabsDisplay)
   {
      this.maxTabsDisplay = maxTabsDisplay;
      if(this.maxTabsDisplay == null)
      {
         this.maxTabsDisplay = 0;
      }
   }

   public Integer getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(Integer pageSize)
   {
      this.pageSize = pageSize;
      if(this.pageSize == null)
      {
         this.pageSize = 0;
      }
   }

   public Integer getPaginatorMaxPages()
   {
      return paginatorMaxPages;
   }

   public void setPaginatorMaxPages(Integer paginatorMaxPages)
   {
      this.paginatorMaxPages = paginatorMaxPages;
      if(this.paginatorMaxPages == null)
      {
         this.paginatorMaxPages = 0;
      }
   }

   public Integer getPaginatorFastStep()
   {
      return paginatorFastStep;
   }

   public void setPaginatorFastStep(Integer paginatorFastStep)
   {
      this.paginatorFastStep = paginatorFastStep;
      if(this.paginatorFastStep == null)
      {
         this.paginatorFastStep = 0;
      }
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanged(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public void preferencesScopeChanged(PreferenceScope scope)
   {
      initialize();
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.views.PortalConfigurationListener#preferencesScopeChanging(org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope)
    */
   public boolean preferencesScopeChanging(PreferenceScope scope)
   {
      return true;
   }

   public ConfigurationConstantsAdapter getConfigurationConstants()
   {
      return ConfigurationConstantsAdapter.getInstance();
   }

   public String getSelectedSkin()
   {
      return selectedSkin;
   }

   public void setSelectedSkin(String selectedSkin)
   {
      this.selectedSkin = selectedSkin;
   }

   public List<SelectItem> getAvailableSkins()
   {
      return availableSkins;
   }
   
   public void setUserProvider(UserProvider userProvider)
   {
      this.userProvider = userProvider;
   }
   
   public void setThemeProvider(ThemeProvider themeProvider)
   {
      this.themeProvider = themeProvider;
   }

   public List<SelectItem> getAvailablePerspectives()
   {
      return availablePerspectives;
   }

   public String getSelectedPerspective()
   {
      return selectedPerspective;
   }

   public void setSelectedPerspective(String selectedPerspective)
   {
      this.selectedPerspective = selectedPerspective;
   }

   public ConfirmationDialog getPortalConfirmationDialog()
   {
      return portalConfirmationDialog;
   }
   
   

}
