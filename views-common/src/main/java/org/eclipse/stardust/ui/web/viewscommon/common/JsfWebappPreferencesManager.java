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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.compatibility.ui.preferences.AbstractCachedPreferencesManager;
import org.eclipse.stardust.engine.core.preferences.AgeCache;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferencesManager;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



/**
 * @author sauer
 * @version $Revision: $
 */
public class JsfWebappPreferencesManager extends AbstractCachedPreferencesManager
   implements IPreferencesManager.Factory
{

   private static final String KEY_USER_PREFERENCES_CACHE = JsfWebappPreferencesManager.class.getName()
         + ".UserPreferences";
   
   private final ConcurrentHashMap partitionPrefs = new ConcurrentHashMap();
   
   private final ConcurrentHashMap realmPrefs = new ConcurrentHashMap();

   private final ConcurrentHashMap userPrefs = new ConcurrentHashMap();
   
   protected User getUser()
   {
      return SessionContext.findSessionContext().getUser();
   }

   protected ServiceFactory getServiceFactory()
   {
      return SessionContext.findSessionContext().getServiceFactory();
   }

   protected AgeCache getPartitionPreferencesCache(String partitionId)
   {
      AgeCache partitionPrefsCache = (AgeCache) partitionPrefs.get(partitionId);
      if (null == partitionPrefsCache)
      {
         partitionPrefs.putIfAbsent(partitionId, new AgeCache(null, new ConcurrentHashMap()));
         partitionPrefsCache =  (AgeCache) partitionPrefs.get(partitionId);
      }

      return partitionPrefsCache;
   }

   protected AgeCache getRealmPreferencesCache(String partitionId, String realmId)
   {
      Pair realmCacheKey = new Pair(partitionId, realmId);
      AgeCache realmPrefsCache = (AgeCache) realmPrefs.get(realmCacheKey);
      if (null == realmPrefsCache)
      {
         realmPrefs.putIfAbsent(realmCacheKey, new AgeCache(null, new ConcurrentHashMap()));
         realmPrefsCache = (AgeCache) realmPrefs.get(realmCacheKey);
      }
      
      return realmPrefsCache;
   }

   protected AgeCache getUserPreferencesCache(User user)
   {
      String userCacheKey = user.getPartitionId() + "." + user.getRealm().getId() + "."
            + user.getOID();
      AgeCache userPrefsCache = (AgeCache) userPrefs.get(userCacheKey);
      if (null == userPrefsCache)
      {
         userPrefs.putIfAbsent(userCacheKey, new AgeCache(null, new ConcurrentHashMap()));
         userPrefsCache = (AgeCache) userPrefs.get(userCacheKey);
      }
      
      return userPrefsCache;
   }

   public IPreferencesManager getPreferencesManager()
   {
      return INSTANCE;
   }
   
   private final static IPreferencesManager INSTANCE = new JsfWebappPreferencesManager();
}
