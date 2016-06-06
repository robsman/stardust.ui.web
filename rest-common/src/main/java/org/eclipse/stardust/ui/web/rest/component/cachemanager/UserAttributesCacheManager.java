/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.component.cachemanager;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.LRUCache;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.UserAttributesDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Caches only oid, account, display name and user image url
 * 
 * @author Yogesh.Manware
 * @version $Revision: $
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserAttributesCacheManager
{
   public static final String PARAM_ENABLED = "Carnot.Client.Caching.UserAttributes.Enabled";

   public static final String PARAM_CACHE_SIZE = "Carnot.Client.Caching.UserAttributes.CacheSize";

   public static final String PARAM_CACHE_TTL = "Carnot.Client.Caching.UserAttributes.CacheTTL";

   private static final String UNKNOWN_USER = "<Unknown User>";

   private static Logger trace = LogManager.getLogger(UserAttributesCacheManager.class);

   private final LRUCache cache;

   private final boolean cacheEnabled;

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public UserAttributesCacheManager()
   {
      Parameters params = Parameters.instance();
      cacheEnabled = params.getBoolean(PARAM_ENABLED, true);

      if (cacheEnabled)
      {
         this.cache = new LRUCache(params.getLong(PARAM_CACHE_TTL, 120) * 1000L, params.getInteger(PARAM_CACHE_SIZE,
               100), false);
      }
      else
      {
         this.cache = null;
      }
   }

   /**
    * @param oid
    * @param forceReload
    * @return
    */
   public UserAttributesDTO getUserAttributes(long oid, boolean forceReload)
   {
      if (null == cache || oid == 0)
      {
         return null;
      }

      UserAttributesDTO UserAttributesDTO = (UserAttributesDTO) cache.get(oid);

      if (forceReload || null == UserAttributesDTO)
      {
         User user = UserUtils.getUser(oid, UserDetailsLevel.Minimal);
         UserAttributesDTO = populateUser(user);
      }
      return UserAttributesDTO;
   }

   /**
    * @param account
    * @param forceReload
    * @return
    */
   public UserAttributesDTO getUserAttributes(String account, boolean forceReload)
   {
      if (null == cache)
      {
         return null;
      }

      Collection users = cache.values();

      if (!forceReload)
      {
         for (Object userObj : users)
         {
            UserAttributesDTO user = (UserAttributesDTO) userObj;
            if (user.account.equals(account))
            {
               return user;
            }
         }
      }
      
      if(org.apache.commons.lang.StringUtils.isNotEmpty(account)) 
      {
         User user = UserUtils.getUser(account, UserDetailsLevel.Minimal);
         if(null != user)
         {
            return populateUser(user);
         }
      }
      
      return null;
   }

   /**
    * @param account
    * @param forceReload
    * @return
    */
   public UserAttributesDTO getUserAttributes(User user)
   {
      if (null == cache)
      {
         return null;
      }

      UserAttributesDTO UserAttributesDTO = (UserAttributesDTO) cache.get(user.getOID());

      if (null == UserAttributesDTO)
      {
         UserAttributesDTO = populateUser(user);
      }
      return UserAttributesDTO;
   }

   /**
    * @param user
    * @return
    */
   private UserAttributesDTO populateUser(User user)
   {
      UserAttributesDTO userDTO = new UserAttributesDTO();
      userDTO.account = user.getAccount();
      userDTO.oid = user.getOID();
      userDTO.displayName = getUserLabel(user);
      userDTO.userImageURI = MyPicturePreferenceUtils.getUsersImageURI(user);
      cache.put(user.getOID(), userDTO);
      return userDTO;
   }

   /**
    * @param user
    * @return
    */
   public String getUserLabel(User user)
   {
      String label = "";

      if (null != user)
      {
         String userDisplayFormat = null;

         // get user level preference if it is enabled through properties constant
         if (true || "PerUser".equals(Parameters.instance().getString("Carnot.Client.Ui.User.NamePattern", "Global")))
         {
            // "withproperties" gives us this preference value
            userDisplayFormat = (String) user.getProperty(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID);

            if (StringUtils.isEmpty(userDisplayFormat))
            {
               QueryService queryService = serviceFactoryUtils.getQueryService();
               List<Preferences> prefs = queryService.getAllPreferences(PreferenceQuery.findPreferencesForUsers(user
                     .getRealm().getId(), user.getId(), UserPreferencesEntries.M_ADMIN_PORTAL,
                     UserPreferencesEntries.PREFERENCE));

               for (Preferences userPref : prefs)
               {
                  if (userPref.getPreferences().get(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID) != null)
                  {
                     userDisplayFormat = (String) userPref.getPreferences().get(
                           UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID);
                     break;
                  }
               }
            }

         }

         if (StringUtils.isNotEmpty(userDisplayFormat))
         {
            label = MessageFormat.format(userDisplayFormat, user.getFirstName(), user.getLastName(), user.getAccount());
         }
         else
         {
            // default format is already cached
            label = MessageFormat.format(UserUtils.getDefaultUserNameDisplayFormat(), user.getFirstName(),
                  user.getLastName(), user.getAccount());
         }
      }

      if (StringUtils.isEmpty(label))
      {
         label = UNKNOWN_USER;
      }

      return label;
   }
}
