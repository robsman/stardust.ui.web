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
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceAdminServiceFacade;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserExistsException;
import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserProfileStatusDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.login.util.PasswordUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class ParticipantManagementUtils
{
   private static final String PREFERENCE_ID = "preference";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public QueryResult<User> getAllUsers(Boolean hideInvalidatedUsers, Options options)
   {
      UserQuery query = (UserQuery) createQuery(hideInvalidatedUsers);

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      query.setPolicy(subsetPolicy);

      applySorting(query, options);

      applyFiltering(query, options);

      QueryResult<User> users = performSearch(query);

      return users;

   }

   public Query createQuery(Boolean hideInvalidatedUsers)
   {
      UserQuery query = UserQuery.findAll();
      applyTableLevelFilters(query, hideInvalidatedUsers);
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Full);
      userPolicy
            .setPreferenceModules(org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);
      return query;
   }

   public QueryResult<User> performSearch(Query query)
   {
      try
      {
         return serviceFactoryUtils.getQueryService().getAllUsers((UserQuery) query);
      }
      catch (AccessForbiddenException e)
      {
         return null;
      }
   }

   /**
    * Apply table level filters
    * 
    * @param UserQuery
    */
   private void applyTableLevelFilters(UserQuery query, Boolean hideInvalidatedUsers)
   {
      if (hideInvalidatedUsers)
      {
         query.getFilter().addOrTerm().or(UserQuery.VALID_TO.greaterThan(System.currentTimeMillis()))
               .or(UserQuery.VALID_TO.isEqual(0));
      }
   }

   /**
    * 
    * @param query
    * @param options
    */
   public void applyFiltering(Query query, Options options)
   {
      FilterAndTerm filter = query.getFilter().addAndTerm();

      UserFilterDTO filterDTO = (UserFilterDTO) options.filter;

      if (filterDTO == null)
      {
         return;
      }

      // valid from
      if (null != filterDTO.validFrom)
      {
         if (null != filterDTO.validFrom.from)
         {
            filter.and(UserQuery.VALID_FROM.greaterOrEqual(filterDTO.validFrom.from));
         }
         if (null != filterDTO.validFrom.to)
         {
            filter.and(UserQuery.VALID_FROM.lessOrEqual(filterDTO.validFrom.to));
         }
      }

      // valid to
      if (null != filterDTO.validTo)
      {
         if (null != filterDTO.validTo.from)
         {
            filter.and(UserQuery.VALID_TO.greaterOrEqual(filterDTO.validTo.from));
         }
         if (null != filterDTO.validTo.to)
         {
            filter.and(UserQuery.VALID_TO.lessOrEqual(filterDTO.validTo.to));
         }
      }

      // realm Filter
      if (null != filterDTO.realm)
      {
         if (StringUtils.isNotEmpty(filterDTO.realm.textSearch))
         {
            filter.and(UserQuery.REALM_ID.like(QueryUtils.getFormattedString(filterDTO.realm.textSearch)));
         }
      }
      // account filter
      if (null != filterDTO.account)
      {
         if (StringUtils.isNotEmpty(filterDTO.account.textSearch))
         {
            filter.and(UserQuery.ACCOUNT.like(QueryUtils.getFormattedString(filterDTO.account.textSearch)));
         }
      }
      // user name filter
      if (null != filterDTO.name)
      {
         String fn = filterDTO.name.firstName;
         String ln = filterDTO.name.lastName;
         if (StringUtils.isNotEmpty(fn) && StringUtils.isNotEmpty(ln))
         {
            FilterAndTerm nameAnd = filter.addAndTerm();

            FilterOrTerm fnOr = nameAnd.addOrTerm();
            fnOr.add(UserQuery.FIRST_NAME.like(getLikeFilterString(fn)));
            fnOr.add(UserQuery.FIRST_NAME.like(getLikeFilterStringAltCase(fn)));

            FilterOrTerm lnOr = nameAnd.addOrTerm();
            lnOr.add(UserQuery.LAST_NAME.like(getLikeFilterString(ln)));
            lnOr.add(UserQuery.LAST_NAME.like(getLikeFilterStringAltCase(ln)));

         }
         else if (StringUtils.isNotEmpty(fn))
         {
            FilterOrTerm or = filter.addOrTerm();
            or.add(UserQuery.FIRST_NAME.like(getLikeFilterString(fn)));
            or.add(UserQuery.FIRST_NAME.like(getLikeFilterStringAltCase(fn)));
         }
         else if (StringUtils.isNotEmpty(ln))
         {
            FilterOrTerm or = filter.addOrTerm();
            or.add(UserQuery.LAST_NAME.like(getLikeFilterString(ln)));
            or.add(UserQuery.LAST_NAME.like(getLikeFilterStringAltCase(ln)));
         }
      }
   }

   /**
    * 
    * @param searchString
    * @return
    */
   private String getLikeFilterString(String searchString)
   {
      return "%" + searchString.replace('*', '%') + "%";
   }

   /**
    * 
    * @param searchString
    * @return
    */
   private String getLikeFilterStringAltCase(String searchString)
   {
      return getLikeFilterString(StringUtils.alternateFirstLetterCase(searchString));
   }

   /**
    * 
    * @param query
    * @param options
    */
   public void applySorting(Query query, Options options)
   {
      if ("oid".equals(options.orderBy))
      {
         query.orderBy(UserQuery.OID, options.asc);
      }
      else if ("account".equals(options.orderBy))
      {

         query.orderBy(UserQuery.ACCOUNT, options.asc);
      }

   }

   public UserDTO initializeView(String mode, Long oid)
   {
      UserService userService = UserUtils.getUserService();
      UserDTO userDTO = new UserDTO();
      userDTO.isInternalAuthentication = userService.isInternalAuthentication();
      userDTO.oldPassword = "";
      userDTO.password = "";
      userDTO.confirmPassword = "";
      // validationMsg = null;
      // passwordValidationMsg = null;
      // emailValidationMsg = null;
      User user = null;
      if (oid != null && oid != -1)
      {
         user = userService.getUser(oid);
      }

      if (mode.equals("CREATE_USER") || mode.equals("COPY_USER"))
      {
         /*
          * if (isCreateMode()) { headerTitle =
          * propsBean.getString("views.createUser.title"); } else { headerTitle =
          * propsBean.getParamString("views.copyUser.title",
          * I18nUtils.getUserLabel(user)); }
          */

         userDTO.changePassword = true;
         userDTO.account = "";
         userDTO.firstName = "";
         userDTO.lastName = "";
         userDTO.eMail = "";
         userDTO.validFrom = null;
         userDTO.validTo = null;
         userDTO.description = "";
         QualityAssuranceAdminServiceFacade qualityAssuranceAdminService = serviceFactoryUtils
               .getQualityCheckAdminServiceFacade();
         userDTO.qaOverride = qualityAssuranceAdminService.getQualityAssuranceUserDefaultProbability();

         if (mode.equals("COPY_USER") && null != user)
         {

            userDTO.realmId = user.getRealm().getId();
            userDTO.qaOverride = user.getQualityAssuranceProbability();
         }
         else
         {
            userDTO.realmId = setDefaultRealm();
         }
      }
      else if (mode.equals("MODIFY_USER") && null != user)
      {
         Long userOid = user.getOID();
         if (userOid != null)
         {
            user = UserUtils.getUser(userOid.longValue(), UserDetailsLevel.Full);
         }
         // headerTitle = propsBean.getString("views.modifyUser.title");
         userDTO.changePassword = false;
         userDTO.oid = user.getOID();
         userDTO.account = user.getAccount();
         userDTO.firstName = user.getFirstName();
         userDTO.lastName = user.getLastName();
         userDTO.realmId = user.getRealm().getId();
         userDTO.eMail = user.getEMail();
         if (user.getValidFrom() != null)
         {
            userDTO.validFrom = user.getValidFrom().getTime();
         }
         else
         {
            userDTO.validFrom = null;
         }
         if (user.getValidTo() != null)
         {
            userDTO.validTo = user.getValidTo().getTime();
         }
         else
         {
            userDTO.validTo = null;
         }
         userDTO.description = user.getDescription();
         userDTO.qaOverride = user.getQualityAssuranceProbability();

         /*
          * if (isModifyProfileConfiguration()) { //TODO will be used in user profile
          * configuration myPicturePreference = new MyPicturePreferenceBean(user); }
          */
      }

      // initDisplayFormats(); this will be implemented @ client side

      userDTO.selectedDisplayFormat = initNameDisplayFormat(user, mode);
      userDTO.allRealms = getAllRealms();
      return userDTO;
   }

   /**
    * 
    * @param user
    * @param mode
    * @return
    */
   private String initNameDisplayFormat(User user, String mode)
   {
      if (mode.equals("CREATE_USER") || mode.equals("COPY_USER"))
      {
         return UserUtils.getDefaultUserNameDisplayFormat();
      }
      else
      {
         if (null != user.getProperty(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID))
         {
            return (String) user.getProperty(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID);
         }
         else
         {
            return UserUtils.getDefaultUserNameDisplayFormat();
         }
      }
   }

   /**
    * 
    */
   private List<SelectItemDTO> getAllRealms()
   {
      UserService userService = serviceFactoryUtils.getUserService();
      List<UserRealm> realms = userService.getUserRealms();
      List<SelectItemDTO> allRealms = new ArrayList<SelectItemDTO>();
      allRealms.add(0, new SelectItemDTO("", ""));
      int count = 1;
      for (UserRealm realm : realms)
      {
         allRealms.add(count, new SelectItemDTO(realm.getId(), realm.getId()));
         count++;
      }
      return allRealms;
   }

   /**
    * 
    * @return
    */
   private String setDefaultRealm()
   {
      String defaultRealm = Parameters.instance().getString(SecurityProperties.DEFAULT_REALM,
            PredefinedConstants.DEFAULT_REALM_ID);

      UserService userService = serviceFactoryUtils.getUserService();
      List<UserRealm> realms = userService.getUserRealms();

      if (defaultRealm != null && defaultRealm.trim().length() > 0)
      {
         for (UserRealm userRealm : realms)
         {
            if (userRealm.getId().compareTo(defaultRealm) == 0)
            {
               return userRealm.getId();

            }
         }
      }
      return "";
   }

   /**
    * 
    * @param userDTO
    * @param mode
    * @return
    */
   public UserProfileStatusDTO createCopyModifyUser(UserDTO userDTO, String mode)
   {
      boolean success = true;
      String passwordValidationMsg = null;
      String validationMsg = null;

      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
      try
      {
         if (mode.equals("CREATE_USER") || mode.equals("COPY_USER"))
         {
            //
            User newUser = createUser(userDTO);
            UserService userService = serviceFactoryUtils.getUserService();
            if (mode.equals("COPY_USER"))
            {
               User user = userService.getUser(userDTO.oid);
               newUser = UserUtils.copyGrantsAndUserGroups(user, newUser);// new user
            }
            updateUserDisplayFormatProperty(newUser, userDTO.selectedDisplayFormat);

            if (null != userDTO.qaOverride)
            {
               newUser.setQualityAssuranceProbability(userDTO.qaOverride);
               newUser = userService.modifyUser(newUser);
            }
         }
         else if (mode.equals("MODIFY_USER"))
         {
            User newUser = modifyUser(userDTO);
            updateUserDisplayFormatProperty(newUser, userDTO.selectedDisplayFormat);
         }
      }
      catch (InvalidPasswordException e)
      {
         success = false;
         String errMessages = PasswordUtils.decodeInvalidPasswordMessage(e, null);
         if (StringUtils.isNotEmpty(errMessages))
         {
            passwordValidationMsg = errMessages;
         }
         else
         {
            passwordValidationMsg = e.toString();
         }
      }
      catch (UserExistsException e)
      {
         success = false;
         validationMsg = propsBean.getParamString("views.createUser.userExistException", userDTO.account);
      }
      catch (PublicException e)
      {
         success = false;
         validationMsg = ExceptionHandler.getExceptionMessage(e);
      }

      UserProfileStatusDTO userProfileStatus = new UserProfileStatusDTO();
      userProfileStatus.success = success;
      userProfileStatus.passwordValidationMsg = passwordValidationMsg;
      userProfileStatus.validationMsg = validationMsg;
      return userProfileStatus;
   }

   /**
    * 
    * @param userDTO
    * @return
    */
   private User modifyUser(UserDTO userDTO)
   {
      User modifiedUser = null;
      UserService userService = UserUtils.getUserService();
      if (null != userDTO && null != userService)
      {
         User userToModify = UserUtils.getUser(userDTO.oid, UserDetailsLevel.Full);
         SessionContext ctx = SessionContext.findSessionContext();
         User loggedInUser = ctx != null ? ctx.getUser() : null;
         boolean passwordChanged = userDTO.changePassword;
         if (passwordChanged)
         {
            userToModify.setPassword(userDTO.password);
         }
         userToModify.setFirstName(userDTO.firstName);
         userToModify.setLastName(userDTO.lastName);
         userToModify.setDescription(userDTO.description);
         userToModify.setEMail(userDTO.eMail);
         if (userDTO.validFrom != null)
         {
            userToModify.setValidFrom(new Date(userDTO.validFrom));
         }
         else
         {
            userToModify.setValidFrom(null);
         }
         if (userDTO.validTo != null)
         {
            userToModify.setValidTo(new Date(userDTO.validTo));
         }
         else
         {
            userToModify.setValidTo(null);
         }

         userToModify.setQualityAssuranceProbability(userDTO.qaOverride);
         modifiedUser = userService.modifyUser(userToModify);
         if (modifiedUser != null && modifiedUser.equals(loggedInUser))
         {
            if (passwordChanged)
            {
               UserUtils.updateServiceFactory(userDTO.account, userDTO.password);
            }
            UserUtils.updateLoggedInUser();
         }
      }
      return modifiedUser;
   }

   /**
    * 
    * @param userToModify
    * @param selectedDisplayFormat
    */
   private void updateUserDisplayFormatProperty(User userToModify, String selectedDisplayFormat)
   {
      QueryService qService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      List<Preferences> prefs = qService.getAllPreferences(PreferenceQuery.findPreferencesForUsers(userToModify
            .getRealm().getId(), userToModify.getId(), UserPreferencesEntries.M_ADMIN_PORTAL, PREFERENCE_ID));
      if (CollectionUtils.isEmpty(prefs))
      {
         Map<String, Serializable> prefMap = new HashMap<String, Serializable>();
         prefMap.put(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID, selectedDisplayFormat);
         Preferences newPref = new Preferences(PreferenceScope.USER, UserPreferencesEntries.M_ADMIN_PORTAL,
               PREFERENCE_ID, prefMap);
         newPref.setRealmId(userToModify.getRealm().getId());
         newPref.setUserId(userToModify.getId());
         prefs.add(newPref);
      }
      else
      {
         for (Preferences pref : prefs)
         {
            Map<String, Serializable> pMap = pref.getPreferences();
            if (CollectionUtils.isEmpty(pMap))
            {
               pMap = new HashMap<String, Serializable>();
            }
            pMap.put(UserUtils.USER_NAME_DISPLAY_FORMAT_PREF_ID, selectedDisplayFormat);
            pref.setPreferences(pMap);
         }
      }

      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      adminService.savePreferences(prefs);
   }

   /**
    * @param userBean
    * @return
    */
   private User createUser(UserDTO userDTO)
   {
      UserService userService = UserUtils.getUserService();
      if (userService != null)
      {
         Date validFrom = null;
         Date validTo = null;
         if (userDTO.validFrom != null)
         {
            validFrom = new Date(userDTO.validFrom);
         }
         if (userDTO.validTo != null)
         {
            validTo = new Date(userDTO.validTo);
         }
         User user = userService.createUser(userDTO.realmId, userDTO.account, userDTO.firstName, userDTO.lastName,
               userDTO.description, userDTO.password, userDTO.eMail, validFrom, validTo);
         return user;
      }
      return null;
   }

}
