/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityInstanceWithPrio;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityInstanceWithPrioTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class StrandedActivitiesBean extends UIViewComponentBean
      implements  ICallbackHandler,ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   protected static final Logger trace = LogManager.getLogger(StrandedActivitiesBean.class);
   
   private List<ActivityInstance> nonAbortableAis;

   private List<ActivityInstance> abortableAis;

   private boolean priorityChanged = false;

   private ActivityTableHelper activityTableHelper;
   
   private Map<Long, ProcessInstance> processInstances;

   public StrandedActivitiesBean()
   {
      super(ResourcePaths.V_strandedActivitiesView);  
     
   }

   @Override
   public void initialize()
   {
      activityTableHelper =  new ActivityTableHelper();
      if (activityTableHelper != null)
      {
         activityTableHelper.initActivityTable();
         activityTableHelper.setCallbackHandler(this);
         activityTableHelper.setStrandedActivityView(true);
         activityTableHelper.getActivityTable().initialize();
         activityTableHelper.getActivityTable().setISearchHandler(new StrandedActivitiesSearchHandler());
         refreshActivityTable();
      }
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {         
         initialize();
      }
   }
   
   /**
    * Updates activity table
    */
   public void refreshActivityTable()
   {
      activityTableHelper.getActivityTable().refresh(
            new DataTableSortModel<ActivityInstanceWithPrioTableEntry>("startTime", false));
   }

   /**
    * Handles call back event
    */
   public void handleEvent(EventType eventType)
   {
      if (EventType.APPLY.equals(eventType))
      {
         refreshActivityTable();
      }
   }
   
   public ActivityTableHelper getActivityTableHelper()
   {
      return activityTableHelper;
   }   

   /**
    * 
    * @author Ankita.Patel
    * @version $Revision: $
    */
   public class StrandedActivitiesSearchHandler extends IppSearchHandler<ActivityInstance>
   {
      private QueryService queryService = ServiceFactoryUtils.getQueryService();

      @Override
      public Query createQuery()
      {
         ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
         
         // Retrieve invalidated users
         UserQuery userQuery = UserQuery.findAll();
         userQuery.getFilter().addAndTerm().add(UserQuery.VALID_TO.lessThan(System.currentTimeMillis()))
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
         return aiQuery;
      }

      @Override
      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         QueryResult<ActivityInstance> result = queryService.getAllActivityInstances((ActivityInstanceQuery) query);
         processInstances = ProcessInstanceUtils.getProcessInstancesAsMap(result, true);
         activityTableHelper.setProcessInstanceMap(processInstances);
         return result;
      }
   }

}
