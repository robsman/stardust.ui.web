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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.security.InvalidPasswordException;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceAdminServiceFacade;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.QualifiedModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.QualifiedOrganizationInfo;
import org.eclipse.stardust.engine.api.model.RoleInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserExistsException;
import org.eclipse.stardust.engine.api.runtime.UserRealm;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.runtime.beans.removethis.SecurityProperties;
import org.eclipse.stardust.ui.web.common.util.PortalTimestampProvider;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.exception.ExceptionHelper;
import org.eclipse.stardust.ui.web.rest.exception.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.service.dto.InvalidateUserStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMessageDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserProfileStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
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
   private static final Logger trace = LogManager.getLogger(ParticipantManagementUtils.class);

   private static final String PREFERENCE_ID = "preference";

   public static enum ParticipantType {

      USER(1), ORGANIZATON_SCOPED_EXPLICIT(2), ORGANIZATON_SCOPED_IMPLICIT(3), ORGANIZATION_UNSCOPED(4), ROLE_SCOPED(5), ROLE_UNSCOPED(
            6), DEPARTMENT_DEFAULT(7), DEPARTMENT(8), USERGROUP(9);

      private int order = 0;

      ParticipantType(int order)
      {
         this.order = order;
      }

      public int getOrder()
      {
         return order;
      }
      
      /**
       * @param name
       * @return
       */
      public static int getOrder(String name)
      {
         for (ParticipantType participantType : values())
         {
            if (participantType.name().equals(name))
            {
               return participantType.order;
            }
         }
         return 0;
      }
   }

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   @Resource
   private ExceptionHelper exceptionHelper;

   public QueryResultDTO getAllUsers(Boolean hideInvalidatedUsers, Options options)
   {
      UserQuery query = (UserQuery) createQuery(hideInvalidatedUsers);

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      query.setPolicy(subsetPolicy);

      applySorting(query, options);

      applyFiltering(query, options);

      QueryResult<User> users = performSearch(query);

      return buildAllUsersResult(users);
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

   /**
    * 
    * @param query
    * @return
    */
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
    * 
    * @param users
    * @return
    */
   private QueryResultDTO buildAllUsersResult(QueryResult<User> users)
   {
      List<UserDTO> userDTOList = new ArrayList<UserDTO>();

      for (User user : users)
      {
         UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
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
         userDTO.displayName = UserUtils.getUserDisplayLabel(user);
         userDTOList.add(userDTO);
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = userDTOList;
      resultDTO.totalCount = users.getTotalCount();
      return resultDTO;
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
         query.getFilter().addOrTerm().or(UserQuery.VALID_TO.greaterThan(PortalTimestampProvider.getTimeStampValue()))
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
      boolean passwordChanged = false;
      if (null != userDTO && null != userService)
      {
         User userToModify = UserUtils.getUser(userDTO.oid, UserDetailsLevel.Full);
         SessionContext ctx = SessionContext.findSessionContext();
         User loggedInUser = ctx != null ? ctx.getUser() : null;
         if(!userDTO.isInternalAuthentication)
         {
            userToModify.setAccount(userDTO.account);
         }
         else
         {
            passwordChanged = userDTO.changePassword;
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

   /**
    * 
    * @param userOids
    * @return
    */
   public InvalidateUserStatusDTO invalidateUser(List<Long> userOids)
   {
      List<User> invalidatedUsers = new ArrayList<User>();
      List<Long> invalidatedUserOids = new ArrayList<Long>();
      List<User> skippedUsers = new ArrayList<User>();
      UserService service = UserUtils.getUserService();
      for (Long oid : userOids)
      {

         User user = service.getUser(oid);
         if (user != null && !user.getAccount().equals("motu") && user.getValidTo() == null)
         {
            User u = service.invalidateUser(user.getRealm().getId(), user.getAccount());
            invalidatedUsers.add(u);
            invalidatedUserOids.add(u.getOID());
         }
         else
         {
            skippedUsers.add(user);
         }

      }
      ActivityInstances activityInstances = prepareActivitiesforInvalideUsers(invalidatedUserOids);
      List<Long> activityInstanceOidList = new ArrayList<Long>();
      if (activityInstances.getSize() > 0)
      {
         for (ActivityInstance activityInstance : activityInstances)
         {
            activityInstanceOidList.add(activityInstance.getOID());
         }
      }

      NotificationMap notificationMap = createNotificationMap(invalidatedUsers, skippedUsers);

      InvalidateUserStatusDTO invalidateUserStatusDTO = new InvalidateUserStatusDTO();
      invalidateUserStatusDTO.activityInstances = activityInstanceOidList;
      invalidateUserStatusDTO.notificationMap = notificationMap;
      return invalidateUserStatusDTO;
   }

   /**
    * Retrieves activities assigned to invalidated users
    * 
    * @param invalidatedUserOids
    * @return
    */
   private ActivityInstances prepareActivitiesforInvalideUsers(List<Long> invalidatedUserOids)
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      FilterTerm filter = aiQuery.getFilter().addOrTerm();
      if (CollectionUtils.isNotEmpty(invalidatedUserOids))
      {
         for (Long userOid : invalidatedUserOids)
         {
            filter.add(new PerformingUserFilter(userOid));
         }
      }
      else
      {
         filter.add(ActivityInstanceQuery.OID.isEqual(0));
      }

      QueryService queryService = serviceFactoryUtils.getQueryService();
      return queryService.getAllActivityInstances(aiQuery);
   }

   private NotificationMap createNotificationMap(List<User> invalidatedUsers, List<User> skippedUsers)
   {
      NotificationMap notificationMap = new NotificationMap();
      if (invalidatedUsers != null && !invalidatedUsers.isEmpty())
      {
         for (Iterator<User> iterator = invalidatedUsers.iterator(); iterator.hasNext();)
         {
            User user = (User) iterator.next();

            notificationMap.addSuccess(new NotificationDTO(user.getOID(), user.getAccount(), restCommonClientMessages
                  .getString("views.participantMgmt.notifyUserInvalidate")));
         }
      }

      if (skippedUsers != null && !skippedUsers.isEmpty())
      {
         for (Iterator<User> iterator = skippedUsers.iterator(); iterator.hasNext();)
         {
            User user = (User) iterator.next();
            if (user.getAccount().equals("motu"))
            {
               notificationMap.addFailure(new NotificationDTO(user.getOID(), user.getAccount(),
                     restCommonClientMessages.getString("views.participantMgmt.notifyMotuNotValidateMsg")));
            }
            else
            {
               notificationMap.addFailure(new NotificationDTO(user.getOID(), user.getAccount(),
                     restCommonClientMessages.getString("views.participantMgmt.notifyUserCannotBeInvalidatedMsg")));
            }
         }
      }
      return notificationMap;
   }

   /**
    * Delegates to default performer
    * 
    * @param ae
    */
   public NotificationMessageDTO delegateToDefaultPerformer(List<Long> activityInstanceOids, List<Long> userOids)
   {
      NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO();
      List<ActivityInstance> ais = new ArrayList<ActivityInstance>();
      if (CollectionUtils.isNotEmpty(activityInstanceOids))
      {
         for (Long activityOid : activityInstanceOids)
         {
            ActivityInstance ai = activityInstanceUtils.getActivityInstance(activityOid);
            if (!ActivityInstanceUtils.isDefaultCaseActivity(ai))
            {
               ais.add(ai);
            }
         }
      }
      activityInstanceUtils.delegateToDefaultPerformer(ais);
      ActivityInstances activityInstances = prepareActivitiesforInvalideUsers(userOids);
      if (activityInstances.size() > 0)
      {
         notificationMessageDTO.success = false;
      }
      else
      {
         notificationMessageDTO.success = true;
      }
      return notificationMessageDTO;
   }

   /**
    * @param organization
    * @param departmentId
    * @return
    */
   public DepartmentInfo getDepartment(QualifiedOrganizationInfo organization, String departmentId)
   {
      DepartmentInfo departmentInfo = null;
      List<Department> deptList = serviceFactoryUtils.getQueryService().findAllDepartments(
            organization.getDepartment(), organization);

      for (Department department2 : deptList)
      {
         String deps = getDepartmentsHierarchy(department2, "");
         if (deps.equals(departmentId.trim()))
         {
            departmentInfo = department2;
            break;
         }
      }
      return departmentInfo;
   }

   /**
    * @param department2
    * @param departmentName
    * @return
    */
   public static String getDepartmentsHierarchy(Department department2, String departmentName)
   {
      if (department2 == null)
      {
         return departmentName;
      }

      departmentName = department2.getId() + "/" + departmentName;

      if (department2.getParentDepartment() != null)
      {
         return getDepartmentsHierarchy(department2.getParentDepartment(), departmentName);
      }

      return departmentName.substring(0, departmentName.length() - 1);
   }

   /**
    * @param input
    * @return
    */
   public static String parseParentDepartmentId(String input)
   {
      //javascript -> ^\[([^\]]*)\]
      return getMatchingString(input, "^\\[([^]]*)\\]");
   }

   /**
    * @param input
    * @return
    */
   public static String parseDepartmentId(String input)
   {
      //javascript -> \[([^\]]*)\]$
      return getMatchingString(input, "\\[([^]]*)\\]$");
   }

   /**
    * @param input
    * @return
    */
   public static String parseParticipantQId(String input)   
   {
      if (PredefinedConstants.ADMINISTRATOR_ROLE.equals(input))
      {
         return input;
      }
      
      //javascript -> (\{[^\[]*)
      return getMatchingString(input, "(\\{([^\\[]*))");
   }

   /**
    * @param input
    * @param pattern
    * @return
    */
   public static String getMatchingString(String input, String pattern)
   {
      Matcher m = Pattern.compile(pattern).matcher(input);

      if (m.find())
      {
         return m.group(1);
      }

      return null;
   }

   /**
    * @param modelParticipantInfo
    * @return
    */
   public static ParticipantType getParticipantType(QualifiedModelParticipantInfo modelParticipantInfo)
   {
      ParticipantType participantType = null;
      if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.definesDepartmentScope())
      {
         participantType = ParticipantType.ORGANIZATON_SCOPED_EXPLICIT;
      }
      else if ((modelParticipantInfo instanceof OrganizationInfo) && modelParticipantInfo.isDepartmentScoped()
            && !modelParticipantInfo.definesDepartmentScope())
      {
         participantType = ParticipantType.ORGANIZATON_SCOPED_IMPLICIT;
      }
      else if ((modelParticipantInfo instanceof RoleInfo) && modelParticipantInfo.isDepartmentScoped())
      {
         participantType = ParticipantType.ROLE_SCOPED;
      }
      else if (modelParticipantInfo instanceof OrganizationInfo)
      {
         participantType = ParticipantType.ORGANIZATION_UNSCOPED;
      }
      else if (modelParticipantInfo instanceof RoleInfo)
      {
         participantType = ParticipantType.ROLE_UNSCOPED;
      }
      return participantType;
   }
}
