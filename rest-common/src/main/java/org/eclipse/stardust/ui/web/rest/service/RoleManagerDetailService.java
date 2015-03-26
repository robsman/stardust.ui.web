package org.eclipse.stardust.ui.web.rest.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.RoleManagerDetailsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserAuthorizationStatusDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.service.utils.RoleManagerDetailUtils;
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
      boolean UserAuthorization = roleManagerDetailUtils.removeUserFromRole(userIds, roleId, departmentOid);
      UserAuthorizationStatusDTO userAuthorizationStatus = new UserAuthorizationStatusDTO();
      userAuthorizationStatus.userAuthorization = UserAuthorization;
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
      boolean UserAuthorization = roleManagerDetailUtils.addUserToRole(userIds, roleId, departmentOid);
      UserAuthorizationStatusDTO userAuthorizationStatus = new UserAuthorizationStatusDTO();
      userAuthorizationStatus.userAuthorization = UserAuthorization;
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
      return ActivityTableUtils.buildTableResult(queryResult,MODE.ACTIVITY_TABLE);
   }
}
