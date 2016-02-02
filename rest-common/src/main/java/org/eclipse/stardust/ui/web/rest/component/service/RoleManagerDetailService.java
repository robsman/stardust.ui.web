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
import org.eclipse.stardust.ui.web.rest.common.Options;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.component.util.RoleManagerDetailUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.RoleManagerDetailsDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserAuthorizationStatusDTO;
import org.springframework.stereotype.Component;

@Component
public class RoleManagerDetailService
{

   @Resource
   private RoleManagerDetailUtils roleManagerDetailUtils;

   /**
    * 
    * @param roleId
    * @param departmentOid
    * @return
    */
   public RoleManagerDetailsDTO getRoleManagerDetails(String roleId, String departmentOid)
   {
      return roleManagerDetailUtils.getRoleManagerDetails(roleId, departmentOid);
   }
   
   /**
    * 
    * @param userIds
    * @param roleId
    * @param departmentOid
    * @return
    */
   public UserAuthorizationStatusDTO removeUserFromRole(List<String> userIds, String roleId,
         String departmentOid){
      boolean userAuthorization = roleManagerDetailUtils.removeUserFromRole(userIds, roleId, departmentOid);
      UserAuthorizationStatusDTO userAuthorizationStatus = new UserAuthorizationStatusDTO();
      userAuthorizationStatus.userAuthorization = userAuthorization;
      return userAuthorizationStatus;
   }
   
   /**
    * 
    * @param userIds
    * @param roleId
    * @param departmentOid
    * @return
    */
   public UserAuthorizationStatusDTO addUserToRole(List<String> userIds, String roleId, String departmentOid){
      boolean userAuthorization = roleManagerDetailUtils.addUserToRole(userIds, roleId, departmentOid);
      UserAuthorizationStatusDTO userAuthorizationStatus = new UserAuthorizationStatusDTO();
      userAuthorizationStatus.userAuthorization = userAuthorization;
      return userAuthorizationStatus;
   }
   /**
    * 
    * @param roleId
    * @param departmentOid
    * @param options
    * @return
    */
   public QueryResultDTO getAllActivitiesForRole(String roleId,
         String departmentOid, Options options){
      QueryResult<ActivityInstance> queryResult = roleManagerDetailUtils.getAllActivitiesForRole(roleId, departmentOid, options);
      if(CollectionUtils.isNotEmpty(options.extraColumns))
      {
         return ActivityTableUtils.buildTableResult(queryResult, MODE.ACTIVITY_TABLE, null, options.extraColumns);
      }
      return ActivityTableUtils.buildTableResult(queryResult,MODE.ACTIVITY_TABLE);
   }
}
