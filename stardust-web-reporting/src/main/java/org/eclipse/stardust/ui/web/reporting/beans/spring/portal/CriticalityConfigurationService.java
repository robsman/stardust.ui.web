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
package org.eclipse.stardust.ui.web.reporting.beans.spring.portal;

import java.io.Serializable;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Yogesh.Manware
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CriticalityConfigurationService
{

   /**
    * 
    */
   @Resource
   private SessionContext sessionContext;

   /**
    * @return
    */
   public Map<String, Serializable> readCriticalityCategoryPrefsMap()
   {
      Map<String, Serializable> prefMap = null;
      Preferences prefs = getPreferences(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
            UserPreferencesEntries.P_ACTIVITY_CRITICALITY_CONFIG);
      if (null != prefs)
      {
         prefMap = prefs.getPreferences();
      }

      return prefMap;
   }

   /**
    * @param preferences
    */
   public static void saveCriticalityCategories(Map<String, Serializable> preferences)
   {
      savePreferenceMap(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
            UserPreferencesEntries.P_ACTIVITY_CRITICALITY_CONFIG, preferences);
   }

   /**
    * @param scope
    * @param moduleId
    * @param preferenceId
    * @param preferences
    */
   private static void savePreferenceMap(PreferenceScope scope, String moduleId, String preferenceId,
         Map<String, Serializable> preferences)
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      Preferences prefs = new Preferences(scope, moduleId, preferenceId, preferences);
      adminService.savePreferences(prefs);
   }

   /**
    * @param scope
    * @param moduleId
    * @param preferenceId
    * @return
    */
   private Preferences getPreferences(PreferenceScope scope, String moduleId, String preferenceId)
   {
      AdministrationService adminService = getServiceFactory().getAdministrationService();
      return adminService.getPreferences(scope, moduleId, preferenceId);
   }

   private ServiceFactory getServiceFactory()
   {
      return sessionContext.getServiceFactory();
   }
}