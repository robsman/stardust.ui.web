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

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.util.PortalTimestampProvider;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.stereotype.Component;

@Component
public class StrandedActivitiesUtils
{
   public QueryResult<ActivityInstance> getStrandedActivities(Options options)
   {
      QueryService queryService = ServiceFactoryUtils.getQueryService();
      Query query = createQuery(queryService, options);
      QueryResult<ActivityInstance> result = performSearch(query, queryService);
      return result;
   }

   private Query createQuery(QueryService queryService, Options options)
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();

      // Retrieve invalidated users
      UserQuery userQuery = UserQuery.findAll();
      userQuery.getFilter().addAndTerm().add(UserQuery.VALID_TO.lessThan(PortalTimestampProvider.getTimeStampValue()))
            .add(UserQuery.VALID_TO.notEqual(0));

      Users users = queryService.getAllUsers(userQuery);
      FilterTerm filter = aiQuery.getFilter().addOrTerm();
      if (users.getTotalCount() > 0)
      {
         // Apply non active users PerformingFilter to ActivityInstanceQuery
         for (User user : users)
         {
            filter.add(new PerformingUserFilter(user.getOID()));
         }
      }
      else
      {
         // Added dummy filter when there is no invalidated users are present,it
         // should not return any activity instances
         filter.add(ActivityInstanceQuery.ACTIVITY_OID.isEqual(0));
      }

      ActivityTableUtils.addDescriptorPolicy(options, aiQuery);

      ActivityTableUtils.addSortCriteria(aiQuery, options);

      ActivityTableUtils.addFilterCriteria(aiQuery, options);

      SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
      aiQuery.setPolicy(subsetPolicy);

      return aiQuery;
   }

   private QueryResult<ActivityInstance> performSearch(Query query, QueryService queryService)
   {
      QueryResult<ActivityInstance> result = queryService.getAllActivityInstances((ActivityInstanceQuery) query);
      return result;
   }
}
