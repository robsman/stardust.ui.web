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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.reporting.common.mapping.reponse.UserJson;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.core.PartitionPreferenceCache;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * @author Yogesh.Manware
 * 
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class PortalUserService implements IUserService, ISearchHandler
{
   public static final String SERVICE_NAME = "userService";

   public static final String USER_NAME_DISPLAY_FORMAT_PREF_ID = "ipp-admin-portal.userNameDisplayFormat.prefs.displayFormat";
   public static final String USER_NAME_DISPLAY_FORMAT_0 = "{1}, {0} ({2})";
   public static final String USER_NAME_DISPLAY_FORMAT_1 = "{0} {1} ({2})";
   public static final String USER_NAME_DISPLAY_FORMAT_2 = "{1} {0} ({2})";
   private static final String PREFERENCES_ID = "preference";

   @Resource
   private SessionContext sessionContext;

   @Resource
   private PartitionPreferenceCache partitionPreferenceCache;

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.reporting.beans.spring.portal.ISearchHandler#handle(
    * java.lang.String, java.lang.String)
    */
   public String handle(String serviceName, String searchVal)
   {
      if (!SERVICE_NAME.equals(serviceName))
      {
         return null;
      }
      return buildResult(serviceName, searchVal);
   }

   /**
    * @param serviceName
    * @param searchVal
    * @return
    */
   private String buildResult(String serviceName, String searchVal)
   {
      // build result - parse search value if required
      List<User> users = searchUsers(searchVal, true, 20);
      List<UserJson> userWrappers = new ArrayList<UserJson>();
      for (User user : users)
      {
         userWrappers.add(new UserJson(user, getUserDisplayLabel(user)));
      }

      Gson gson = new Gson();
      return gson.toJson(userWrappers);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.reporting.beans.spring.portal.IUserService#searchUsers
    * (java.lang.String, boolean, int)
    */
   public List<User> searchUsers(String searchValue, boolean onlyActive, int maxMatches)
   {
      UserQuery userQuery = onlyActive ? UserQuery.findActive() : UserQuery.findAll();

      String[] prefModules = {UserPreferencesEntries.M_ADMIN_PORTAL, UserPreferencesEntries.M_VIEWS_COMMON};
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(prefModules);
      userQuery.setPolicy(userPolicy);

      userQuery.setPolicy(new SubsetPolicy(maxMatches, false));
      String nameFirstLetterCaseChanged = alternateFirstLetter(searchValue);
      FilterOrTerm filter = userQuery.getFilter().addOrTerm();
      filter.or(UserQuery.FIRST_NAME.like(searchValue));
      filter.or(UserQuery.FIRST_NAME.like(nameFirstLetterCaseChanged));
      filter.or(UserQuery.LAST_NAME.like(searchValue));
      filter.or(UserQuery.LAST_NAME.like(nameFirstLetterCaseChanged));
      filter.or(UserQuery.ACCOUNT.like(searchValue));
      filter.or(UserQuery.ACCOUNT.like(nameFirstLetterCaseChanged));
      userQuery.where(filter);

      userQuery.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);

      Users users = sessionContext.getServiceFactory().getQueryService().getAllUsers(userQuery);

      return users;
   }

   /**
    * @param field
    * @return
    */
   private static String alternateFirstLetter(String field)
   {
      String firstLetter = field.substring(0, 1);
      if (firstLetter.compareTo(field.substring(0, 1).toLowerCase()) == 0)
      {
         firstLetter = firstLetter.toUpperCase();
      }
      else
      {
         firstLetter = firstLetter.toLowerCase();
      }
      return firstLetter + field.substring(1);
   }

   // prepare label
   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.reporting.beans.spring.portal.IUserService#
    * getUserDisplayLabel(org.eclipse.stardust.engine.api.runtime.User)
    */
   public String getUserDisplayLabel(User user)
   {
      String label = "";

      if (null != user)
      {
         label = formatUserName(user.getFirstName(), user.getLastName(), user.getAccount(),
               (String) user.getProperty(USER_NAME_DISPLAY_FORMAT_PREF_ID));
      }
      if (StringUtils.isEmpty(label))
      {
         label = "common.unknown"; // TODO I18n
      }
      return label;
   }

   /**
    * @param firstName
    * @param lastName
    * @param account
    * @param format
    * @return
    */
   private String formatUserName(String firstName, String lastName, String account, String format)
   {
      if (StringUtils.isNotEmpty(format))
      {
         return MessageFormat.format(format, firstName, lastName, account);
      }

      return MessageFormat.format(getDefaultUserNameDisplayFormat(), firstName, lastName, account);
   }

   /**
    * @return
    */
   private String getDefaultUserNameDisplayFormat()
   {
      String defaultdispFormat = null;
      String prefKey = getDefaultUserNameFormatPrefKey();
      // Search the preference key in cache
      defaultdispFormat = (String) partitionPreferenceCache.getObject(prefKey, UserPreferencesEntries.M_ADMIN_PORTAL,
            PREFERENCES_ID, USER_NAME_DISPLAY_FORMAT_0);

      return defaultdispFormat;
   }

   /**
    * @return
    */
   private String getDefaultUserNameFormatPrefKey()
   {
      return UserPreferencesEntries.M_ADMIN_PORTAL + "." + UserPreferencesEntries.V_USER_NAME_DISPALY_FORMAT + "."
            + UserPreferencesEntries.F_USER_NAME_DISPLAY_FORMAT_DEFAULT;
   }
}
