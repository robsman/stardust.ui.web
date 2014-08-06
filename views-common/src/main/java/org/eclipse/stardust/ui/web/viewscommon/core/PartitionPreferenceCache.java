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
package org.eclipse.stardust.ui.web.viewscommon.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
public class PartitionPreferenceCache implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = -4518297066809700481L;

   private static final String BEAN_NAME = "partitionPreferenceCache";
   private Map<String, Object> preferenceMap = new HashMap<String, Object>();

   public static PartitionPreferenceCache getCurrent()
   {
      return (PartitionPreferenceCache) ManagedBeanUtils.getManagedBean(BEAN_NAME);
   }

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
      return SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
   }

}
