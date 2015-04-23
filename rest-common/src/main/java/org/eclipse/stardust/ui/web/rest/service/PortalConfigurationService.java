/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service;

import static org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstants.DEFAULT_FAST_STEP;
import static org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstants.DEFAULT_MAX_PAGES;
import static org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstants.DEFAULT_MAX_TAB_DISPLAY;
import static org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstants.DEFAULT_PAGE_SIZE;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.IPerspectiveDefinition;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.spi.theme.Theme;
import org.eclipse.stardust.ui.web.common.spi.theme.ThemeProvider;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.PortalConfigurationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.icesoft.faces.component.menubar.MenuItem;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PortalConfigurationService
{

   /**
    * Constants
    */
   public static final String M_PORTAL = "ipp-portal-common";

   public static final String V_PORTAL_CONFIG = "configuration";

   public static final String F_SKIN = "prefs.skin";

   public static final String F_DEFAULT_PERSPECTIVE = "prefs.defaultPerspective";

   public static final String F_TABS_MAX_TABS_DISPLAY = "prefs.maxTabsDisplay";

   public static final String F_PAGINATOR_PAGE_SIZE = "prefs.pageSize";

   public static final String F_PAGINATOR_MAX_PAGES = "prefs.paginatorMaxPages";

   public static final String F_PAGINATOR_FAST_STEP = "prefs.paginatorFastStep";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ThemeProvider themeProvider;

   private static final Logger trace = LogManager.getLogger(PortalConfigurationService.class);

   public PortalConfigurationDTO getPortalConfiguration(String scope)
   {

      PreferenceScope pScope = scope.equalsIgnoreCase(PreferenceScope.USER.toString())
            ? PreferenceScope.USER
            : PreferenceScope.PARTITION;

      UserPreferencesHelper userPrefsHelper = UserPreferencesHelper.getInstance(M_PORTAL, pScope);

      PortalConfigurationDTO config = new PortalConfigurationDTO();
      // SKINS
      config.availableSkins = new ArrayList<SelectItemDTO>();

      List<Theme> themes = themeProvider.getThemes();

      for (Theme theme : themes)
      {
         config.availableSkins.add(new SelectItemDTO(theme.getThemeId(), theme.getThemeName()));
      }

      config.selectedSkin = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, F_SKIN);

      config.maxTabsDisplay = getIntUserPreferencesValue(userPrefsHelper, F_TABS_MAX_TABS_DISPLAY,
            DEFAULT_MAX_TAB_DISPLAY);
      config.pageSize = getIntUserPreferencesValue(userPrefsHelper, F_PAGINATOR_PAGE_SIZE, DEFAULT_PAGE_SIZE);
      config.paginatorMaxPages = getIntUserPreferencesValue(userPrefsHelper, F_PAGINATOR_MAX_PAGES, DEFAULT_MAX_PAGES);
      config.paginatorFastStep = getIntUserPreferencesValue(userPrefsHelper, F_PAGINATOR_FAST_STEP, DEFAULT_FAST_STEP);

      // PERSPECTIVES
      config.selectedPerspective = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, F_DEFAULT_PERSPECTIVE);

      PortalApplication portalApp = PortalApplication.getInstance();
      boolean perspectiveAvailable = false;
      IPerspectiveDefinition xmlConfigDefaultPerspetive = null;
      IPerspectiveDefinition currPerspective;
      config.availablePerspectives = new ArrayList<SelectItemDTO>();

      List<MenuItem> perspectives = portalApp.getPortalUiController().getPerspectiveItems();
      for (MenuItem item : perspectives)
      {
         config.availablePerspectives.add(new SelectItemDTO(item.getId(), item.getTitle()));

         currPerspective = portalApp.getPortalUiController().getPerspective(item.getId());

         if (null == xmlConfigDefaultPerspetive && currPerspective.isDefaultPerspective())
         {
            xmlConfigDefaultPerspetive = currPerspective;
         }

         if (StringUtils.isEmpty(config.selectedPerspective) && currPerspective.isDefaultPerspective())
         {
            config.selectedPerspective = item.getId();
            perspectiveAvailable = true;
         }
         else if (item.getId().equals(config.selectedPerspective))
         {
            perspectiveAvailable = true;
         }
      }

      if (StringUtils.isNotEmpty(config.selectedPerspective) && !perspectiveAvailable)
      {
         trace.warn("Cannot load default Perspective, either it's not available or user is not authorized - "
               + config.selectedPerspective);

         // Fall back to available default Perspective
         if (null != xmlConfigDefaultPerspetive)
         {
            config.selectedPerspective = xmlConfigDefaultPerspetive.getName();
         }
      }

      return config;
   }

   /**
    * @param userPrefsHelper
    * @param featureId
    * @param defaultValue
    * @return
    */
   private int getIntUserPreferencesValue(UserPreferencesHelper userPrefsHelper, String featureId, int defaultValue)
   {
      String value = userPrefsHelper.getSingleString(V_PORTAL_CONFIG, featureId);
      return StringUtils.isEmpty(value) ? defaultValue : Integer.valueOf(value);
   }
}
