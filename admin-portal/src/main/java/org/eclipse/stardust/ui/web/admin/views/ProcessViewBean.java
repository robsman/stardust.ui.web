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

import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
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
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessTableHelper;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ProcessViewBean extends UIViewComponentBean
      implements ICallbackHandler,ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private WorkflowFacade workflowFacade;

   private List<ProcessInstance> processInstanceList;

   private ProcessTableHelper processTableHelper;

   /**
    *
    */
   public ProcessViewBean()
   {
      super(ResourcePaths.V_processView);
   }

   @Override
   public void initialize()
   {
      processTableHelper.getProcessTable().refresh(true);
   }

   /**
    * Handles view events
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
               AdminportalConstants.WORKFLOW_FACADE);
         initProcessColumns();
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
    * Handles call back events
    */
   public void handleEvent(EventType eventType)
   {
      update();
   }

   private void initProcessColumns()
   {
      processTableHelper = new ProcessTableHelper();
      if (processTableHelper != null)
      {
         processTableHelper.setCallbackHandler(this);
         processTableHelper.initializeProcessTable();
         processTableHelper.getProcessTable().initialize();
         processTableHelper.getProcessTable().setISearchHandler(new ProcessTableSearchHandler());
      }
   }

   // ******************** Modified Getter and Setter Methods *********************
   public long getTotalProcessInstancesCount() throws PortalException
   {
      if (workflowFacade.getTotalProcessInstancesCount() != 0)
         return workflowFacade.getTotalProcessInstancesCount();
      else
         return 0;
   }

   public long getActiveProcessInstancesCount() throws PortalException
   {
      if (workflowFacade.getActiveProcessInstancesCount() != 0)
         return workflowFacade.getActiveProcessInstancesCount();
      else
         return 0;
   }

   public long getInterruptedProcessInstancesCount() throws PortalException
   {
      if (workflowFacade.getInterruptedProcessInstancesCount() != 0)
         return workflowFacade.getInterruptedProcessInstancesCount();
      else
         return 0;
   }

   public long getCompletedProcessInstancesCount() throws PortalException
   {
      if (workflowFacade.getCompletedProcessInstancesCount() != 0)
         return workflowFacade.getCompletedProcessInstancesCount();
      else
         return 0;
   }

   public long getAbortedProcessInstancesCount() throws PortalException
   {
      if (workflowFacade.getAbortedProcessInstancesCount() != 0)
         return workflowFacade.getAbortedProcessInstancesCount();
      else
         return 0;
   }

   // ************************ Default Getter and Setter Methods **********************

   public List<ProcessInstance> getProcessInstanceList()
   {
      return processInstanceList;
   }

   public ProcessTableHelper getProcessTableHelper()
   {
      return processTableHelper;
   }

   public void setProcessInstanceList(List<ProcessInstance> processInstanceList)
   {
      this.processInstanceList = processInstanceList;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class ProcessTableSearchHandler extends IppSearchHandler<ProcessInstance>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
         processTableHelper.applyDescriptorPolicy(query);
         return query;
      }

      @Override
      public QueryResult<ProcessInstance> performSearch(Query query)
      {
         return workflowFacade.getAllProcessInstances((ProcessInstanceQuery) query);
      }
   }
}
