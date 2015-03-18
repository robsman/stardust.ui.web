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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessResourceMgmtRoleDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessResourceMgmtUserDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.springframework.stereotype.Component;

@Component
public class ProcessResourceMgmtUtils
{
   private IQueryExtender queryExtender;

   private final static String QUERY_EXTENDER = "carnotBcProcessResourceMgmt/queryExtender";

   /**
    * Used to set the list of ProcessResourceMgmtRoleDTO
    */
   public List<ProcessResourceMgmtRoleDTO> getProcessResourceRoles()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      List<ProcessResourceMgmtRoleDTO> processResourceRoleList = new ArrayList<ProcessResourceMgmtRoleDTO>();
      List<RoleItem> roleItemList = facade.getAllRolesExceptCasePerformer();
      DepartmentInfo departmentInfo;
      long departmentOid;

      for (RoleItem roleItem : roleItemList)
      {
         departmentInfo = roleItem.getRole().getDepartment();
         departmentOid = (departmentInfo == null) ? 0 : departmentInfo.getOID();
         processResourceRoleList.add(new ProcessResourceMgmtRoleDTO(roleItem.getRole()
               .getQualifiedId(), departmentOid, roleItem.getRoleName(), roleItem
               .getWorklistCount(), roleItem.getLoggedInUserCount(), roleItem
               .getUserCount(), roleItem.getEntriesPerUser()));

      }

      return processResourceRoleList;
   }

   /**
    * Used to set the list of ProcessResourceMgmtUserDTO
    */
   public List<ProcessResourceMgmtUserDTO> getProcessResourceUsers()
   {
      List<ProcessResourceMgmtUserDTO> processResourceUserList = new ArrayList<ProcessResourceMgmtUserDTO>();

      Query query = createQuery();

      processResourceUserList = new ArrayList<ProcessResourceMgmtUserDTO>();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME)
               .and(UserQuery.ACCOUNT);
      }
      Users users = facade.getAllUsers((UserQuery) query);
      List<UserItem> userItems = facade.getAllUsersAsUserItems(users);
      if (!userItems.isEmpty())
      {
         String userFullName;
         for (UserItem userItem : userItems)
         {
            userFullName = I18nUtils.getUserLabel(userItem.getUser());
            processResourceUserList.add(new ProcessResourceMgmtUserDTO(userItem
                  .getUserName(), userItem.getUser().getOID(),
                  userItem.getUser().getId(), userFullName, userItem.getUser()
                        .getAccount(), userItem.getUser().getEMail(), userItem
                        .getRoleCount(), userItem.getDirectItemCount(), userItem
                        .getIndirectItemCount(), userItem.getItemCount(), userItem
                        .isLoggedIn()));

         }
      }

      return processResourceUserList;
   }

   /**
    * Creates the query to get Users
    * 
    * @return query
    */
   public Query createQuery()
   {
      UserQuery query = UserQuery.findActive();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);

      getQueryExtender();

      if (queryExtender != null)
      {
         queryExtender.extendQuery(query);
      }
      return query;
   }

   /**
    * @return IQueryExtender
    */
   public IQueryExtender getQueryExtender()
   {
      if (queryExtender == null)
      {
         SessionContext sessionCtx = SessionContext.findSessionContext();
         return (IQueryExtender) sessionCtx.lookup(QUERY_EXTENDER);
      }
      return queryExtender;
   }
}
