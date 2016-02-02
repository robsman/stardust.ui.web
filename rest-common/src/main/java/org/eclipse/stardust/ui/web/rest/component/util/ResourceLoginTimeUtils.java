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
package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatistics.LoginStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserLoginStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.ResourceLoginInfoDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

@Component
public class ResourceLoginTimeUtils
{

   public QueryResultDTO getResourceLoginTimeInfo()
   {
      List<ResourceLoginInfoDTO> resourceLoginInfoList = new ArrayList<ResourceLoginInfoDTO>();
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserQuery query = facade.getTeamQuery(true);
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);

      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }
      Users users = facade != null ? facade.getAllUsers((UserQuery) query) : null;
      Iterator userIter = facade.getAllUsersAsUserItems(users).iterator();
      UserLoginStatistics statistics = (UserLoginStatistics) facade.getAllUsers(UserLoginStatisticsQuery.forAllUsers());
      while (userIter.hasNext())
      {
         UserItem userItem = (UserItem) userIter.next();
         LoginStatistics loginStatistics = statistics.getLoginStatistics(userItem.getUser().getOID());
         ResourceLoginInfoDTO entry = null;
         if (loginStatistics != null)
         {
            entry = new ResourceLoginInfoDTO(UserUtils.getUserDisplayLabel(userItem.getUser()), userItem.getUser()
                  .getId(), userItem.getUser().getOID(), loginStatistics.timeLoggedInToday,
                  loginStatistics.timeLoggedInThisWeek, loginStatistics.timeLoggedInThisMonth);
         }
         else
         {
            entry = new ResourceLoginInfoDTO(UserUtils.getUserDisplayLabel(userItem.getUser()), userItem.getUser()
                  .getId(), userItem.getUser().getOID(), null, null, null);
         }
         resourceLoginInfoList.add(entry);
      }

      QueryResultDTO result = new QueryResultDTO();
      result.list = resourceLoginInfoList;
      result.totalCount = resourceLoginInfoList.size();
      return result;

   }

}
