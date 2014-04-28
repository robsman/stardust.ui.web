/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Sidharth.Singh
 * @author Yogesh.Manware
 * @version $Revision: $
 * 
 * Spring based copy of org.eclipse.stardust.ui.web.viewscommon.core.PartitionPreferenceCache
 */

@Component
@Scope(value =  "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PartitionPreferenceService implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = -4518297066809700481L;

   @Resource
   private SessionContext sessionContext;
   
   private Map<String, Object> preferenceMap = new HashMap<String, Object>();

   /**
    * 
    * @param key
    * @param moduleId
    * @param preferenceId
    * @param defaultValue
    * @return
    */
   public Object getObject(String key, String moduleId, String preferenceId, Object defaultValue)
   {
      Object value = preferenceMap.get(key);
      if (null == value)
      {
         // fetch the preference from DB
         Preferences retrievedPrefs = getAdminService().getPreferences(PreferenceScope.PARTITION, moduleId,
               preferenceId);
         if (null != retrievedPrefs && !CollectionUtils.isEmpty(retrievedPrefs.getPreferences()))
         {
            value = retrievedPrefs.getPreferences().get(key);
         }
         // If no preference found, but default value specified, set the same in value
         if (value == null && defaultValue != null)
         {
            value = defaultValue;
         }
         preferenceMap.put(key, value);
      }
      return value;
   }

   public void removeObject(String key)
   {
      preferenceMap.remove(key);
   }

   /**
    * Save the preference and update the Map
    * 
    * @param key
    * @param value
    * @param preferences
    */
   public void setObject(String key, Object value, String moduleId, String preferenceId)
   {
      Preferences retrievedPrefs = getAdminService().getPreferences(PreferenceScope.PARTITION, moduleId, preferenceId);

      if (null == retrievedPrefs)
      {
         Map<String, Serializable> prefMap = new HashMap<String, Serializable>();
         retrievedPrefs = new Preferences(PreferenceScope.PARTITION, moduleId, preferenceId, prefMap);
      }

      retrievedPrefs.getPreferences().put(key, (Serializable) value);
      getAdminService().savePreferences(retrievedPrefs);
      preferenceMap.put(key, value);
   }

   private AdministrationService getAdminService()
   {
      return sessionContext.getServiceFactory().getAdministrationService();
   }

}
