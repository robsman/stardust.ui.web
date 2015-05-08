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

import javax.annotation.Resource;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.UserFilterDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;
import org.springframework.stereotype.Component;

@Component
public class ParticipantManagementUtils
{

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
}
