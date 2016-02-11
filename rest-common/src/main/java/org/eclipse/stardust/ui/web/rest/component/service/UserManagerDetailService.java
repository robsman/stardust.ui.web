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
package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.component.util.UserManagerDetailUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserAuthorizationStatusDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserManagerDetailsDTO;
import org.springframework.stereotype.Component;

@Component
public class UserManagerDetailService
{
   @Resource
   private UserManagerDetailUtils userManagerDetailUtils;

   /**
    * 
    * @param userOid
    * @return
    */
   public UserManagerDetailsDTO getUserManagerDetails(String userOid)
   {
      return userManagerDetailUtils.getUserManagerDetails(userOid);
   }

   /**
    * 
    * @param roleIds
    * @param userOid
    * @return
    */
   public UserAuthorizationStatusDTO addRoleToUser(List<String> roleIds, String userOid)
   {
      boolean userAuthorization = userManagerDetailUtils.addRoleToUser(roleIds, userOid);
      UserAuthorizationStatusDTO userAuthorizationStatus = new UserAuthorizationStatusDTO();
      userAuthorizationStatus.userAuthorization = userAuthorization;
      return userAuthorizationStatus;
   }

   /**
    * 
    * @param roleIds
    * @param userOid
    * @return
    */
   public UserAuthorizationStatusDTO removeRoleFromUser(List<String> roleIds, String userOid)
   {
      boolean userAuthorization = userManagerDetailUtils.removeRoleFromUser(roleIds, userOid);
      UserAuthorizationStatusDTO userAuthorizationStatus = new UserAuthorizationStatusDTO();
      userAuthorizationStatus.userAuthorization = userAuthorization;
      return userAuthorizationStatus;
   }

   /**
    * 
    * @param userOid
    * @param options
    * @return
    */
   public QueryResultDTO getAllActivitiesForUser(String userOid, DataTableOptionsDTO options)
   {
      QueryResult<ActivityInstance> queryResult = userManagerDetailUtils.getAllActivitiesForUser(userOid, options);
      if(CollectionUtils.isNotEmpty(options.extraColumns))
      {
         return ActivityTableUtils.buildTableResult(queryResult, MODE.ACTIVITY_TABLE, null, options.extraColumns);
      }
      return ActivityTableUtils.buildTableResult(queryResult,MODE.ACTIVITY_TABLE);
   }
}
