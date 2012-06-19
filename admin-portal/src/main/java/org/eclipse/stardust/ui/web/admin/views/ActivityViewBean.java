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
package org.eclipse.stardust.ui.web.admin.views;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;




/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ActivityViewBean extends UIViewComponentBean implements ICallbackHandler, ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   
   private static final Logger trace = LogManager.getLogger(ActivityViewBean.class);

   private WorkflowFacade workflowFacade;

   private List<ActivityInstance> selectedActivities;
   
   private ActivityTableHelper activityHelper;
   
   Map<Long, ProcessInstance> processInstances;
   
   /**
    * 
    */
   public ActivityViewBean()
   {
      super(ResourcePaths.V_activityView);
      
   }

   @Override
   public void initialize()
   {
      activityHelper.getActivityTable().refresh(true);
   }
   
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {     
         workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
               AdminportalConstants.WORKFLOW_FACADE);
         
         initActivitiesColumns();
         initialize();

      }
   }
   
   /**
    * Updates the changes
    */
   public void update()
   {
      workflowFacade.reset();
      initialize();
   }

   /**
    * Handles callback event after Abort is performed
    */
   public void handleEvent(EventType eventType)
   {
      update();
   }

   
   /**
    * Initializes activity columns
    */
   private void initActivitiesColumns()
   {
      activityHelper = new ActivityTableHelper();
      if (activityHelper != null)
      {
         activityHelper.initActivityTable();
         activityHelper.setCallbackHandler(this);
         activityHelper.setStrandedActivityView(false);
         activityHelper.getActivityTable().initialize();
         activityHelper.getActivityTable().setISearchHandler(new ActivitySearchHandler());
      }
   }

   // ****************** Modified Getter and Setter methods ********************
   public long getTotalActivityInstancesCount() throws PortalException
   {
      if (workflowFacade.getTotalActivityInstancesCount() != 0)
         return workflowFacade.getTotalActivityInstancesCount();
      else
         return 0;
   }

   public long getActiveActivityInstancesCount() throws PortalException
   {
      if (workflowFacade.getActiveActivityInstancesCount() != 0)
         return workflowFacade.getActiveActivityInstancesCount();
      else
         return 0;
   }

   public long getPendingActivityInstancesCount() throws PortalException
   {
      if (workflowFacade.getPendingActivityInstancesCount() != 0)
         return workflowFacade.getPendingActivityInstancesCount();
      else
         return 0;
   }

   public long getCompletedActivityInstancesCount() throws PortalException
   {
      if (workflowFacade.getCompletedActivityInstancesCount() != 0)
         return workflowFacade.getCompletedActivityInstancesCount();
      else
         return 0;
   }

   public long getAbortedActivityInstancesCount() throws PortalException
   {
      if (workflowFacade.getAbortedActivityInstancesCount() != 0)
         return workflowFacade.getAbortedActivityInstancesCount();
      else
         return 0;
   }

   // ************************* Default Getter and Setter methods
   // ********************************
   public List<ActivityInstance> getSelectedActivities()
   {
      return selectedActivities;
   }

   public ActivityTableHelper getActivityHelper()
   {
      return activityHelper;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class ActivitySearchHandler extends IppSearchHandler<ActivityInstance>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
         if (getActivityHelper().isFetchAllDescriptors())
         {
            query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         }
         else if (CollectionUtils.isEmpty(getActivityHelper().getVisibleDescriptorsIds()))
         {
            query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
         }
         else
         {
            query.setPolicy(DescriptorPolicy.withIds(getActivityHelper().getVisibleDescriptorsIds()));
         }
         return query;
      }

      @Override
      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         QueryResult<ActivityInstance> result = workflowFacade.getAllActivitiesEntries((ActivityInstanceQuery) query);
         processInstances = ProcessInstanceUtils.getProcessInstancesAsMap(result, true);
         activityHelper.setProcessInstanceMap(processInstances);
         return result;
      }
   }

}
