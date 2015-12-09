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
 * 
 * @author Johnson.Quadras
 *
 */
package org.eclipse.stardust.ui.web.rest.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.rest.service.dto.PreferenceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SaveFavoriteStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ViewFavoriteService
{
   private static final Logger trace = LogManager.getLogger(ViewFavoriteService.class);

   private static final String FAVORITE = "FAVORITE";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private PreferenceService preferenceService;

   public SaveFavoriteStatusDTO addFavorite(String preferenceId, String preferenceName, String values)
   {
      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
      Preferences preferences = adminService.getPreferences(PreferenceScope.USER, FAVORITE, preferenceId);
      Map<String, Serializable> prefMap = preferences.getPreferences();
      if (prefMap.get(preferenceName) != null)
      {
         trace.info("Dashboard Already present");
         SaveFavoriteStatusDTO saveFavStatusDTO = new SaveFavoriteStatusDTO();
         saveFavStatusDTO.success = false;
         saveFavStatusDTO.errorMsg = MessagesViewsCommonBean.getInstance().getParamString("views.favorite.preexist",
               preferenceName.toString());
         return saveFavStatusDTO;
      }
      else
      {
         prefMap.put(preferenceName, values);
         Preferences newPreferences = new Preferences(PreferenceScope.USER, FAVORITE, preferenceId, prefMap);
         adminService.savePreferences(newPreferences);
         SaveFavoriteStatusDTO saveFavStatusDTO = new SaveFavoriteStatusDTO();
         saveFavStatusDTO.success = true;
         return saveFavStatusDTO;
      }
   }

   public List<PreferenceDTO> getAllFavorite(String preferenceId)
   {
      List<Preferences> prefs = new ArrayList<Preferences>();

      if (preferenceId == null)
      {
         preferenceId = "*";
      }

      prefs = serviceFactoryUtils.getQueryService().getAllPreferences(
            PreferenceQuery.findPreferences(PreferenceScope.USER, FAVORITE, preferenceId));

      List<PreferenceDTO> prefList = preferenceService.buildPreferenceList(prefs);

      return prefList;
   }

   public void updateFavorite(String preferenceId, String preferenceName, String values)
   {
      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
      Preferences preferences = adminService.getPreferences(PreferenceScope.USER, FAVORITE, preferenceId);
      Map<String, Serializable> prefMap = preferences.getPreferences();
     /* if (CollectionUtils.isNotEmpty(prefMap))
      {
         prefMap.remove(preferenceName);
        
      }     */ 
      prefMap.put(preferenceName, values);
      Preferences newPreferences = new Preferences(PreferenceScope.USER, FAVORITE, preferenceId, prefMap);
      adminService.savePreferences(newPreferences);
   }

   public void deleteFavorite(String preferenceId, String preferenceName)
   {
      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
      Preferences preferences = adminService.getPreferences(PreferenceScope.USER, FAVORITE, preferenceId);
      Map<String, Serializable> prefMap = preferences.getPreferences();
      if (CollectionUtils.isNotEmpty(prefMap))
      {
         prefMap.remove(preferenceName);
         Preferences newPreferences = new Preferences(PreferenceScope.USER, FAVORITE, preferenceId, prefMap);
         adminService.savePreferences(newPreferences);
      }
   }

}
