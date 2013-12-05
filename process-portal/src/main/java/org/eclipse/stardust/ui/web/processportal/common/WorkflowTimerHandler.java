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
package org.eclipse.stardust.ui.web.processportal.common;

import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.app.View.ViewState;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.timer.TimerEventHandler;
import org.eclipse.stardust.ui.web.common.timer.TimerManager;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.processportal.launchpad.WorklistsBean;
import org.eclipse.stardust.ui.web.processportal.view.WorklistConfigurationBean;
import org.eclipse.stardust.ui.web.processportal.view.WorklistTableBean;

/**
 * Start/Stop the timer and update worklist
 *
 * @author Sidharth.Singh
 *
 */
public class WorkflowTimerHandler implements TimerEventHandler
{
   private static final Logger trace = LogManager.getLogger(WorkflowTimerHandler.class);

   public static final String WORKLIST_BEAN = "worklistsBean";
   public static final String WORKLIST_PANEL = "worklistPanel";

   public static final String BEAN_NAME = "workflowTimer";

   public static final int TIME_MILISECONDS = 60000;

   /**
    * @return
    */
   public static WorkflowTimerHandler getInstance()
   {
      return (WorkflowTimerHandler) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    *
    */
   public void handleEvent()
   {
      updateWorklistTable();
      updateWorkflowLaunchPanel();
      PortalApplication.getInstance().renderPortalSession();
   }

   /**
    *
    */
   public void startTimer()
   {
      Integer refreshInterval = WorklistConfigurationBean.getInstance().getRefreshInterval();
      if (refreshInterval != null && refreshInterval > 0)
      {
         refreshInterval = (refreshInterval.intValue() * TIME_MILISECONDS);
         TimerManager.getInstance().startTimer(BEAN_NAME, refreshInterval, this);
         if (trace.isDebugEnabled())
         {
            trace.debug("Timer Started for" + BEAN_NAME);
         }
      }
   }

   /**
    *
    */
   public void stopTimer()
   {
      TimerManager.getInstance().stopTimer(BEAN_NAME);
      if (trace.isDebugEnabled())
      {
         trace.debug("Timer Stoped for" + BEAN_NAME);
      }
   }

   /**
    * Update the worklist table on autoRefresh trigger
    */
   private void updateWorklistTable()
   {
      try
      {
         for (View currentView : PortalApplication.getInstance().getOpenViews())
         {
            if (currentView.getName().equals(WORKLIST_PANEL))
            {
               View worklistView = currentView;

               String controllerName = worklistView.getDefinition().getController();
               if (controllerName != null)
               {
                  WorklistTableBean worklistTableBean = (WorklistTableBean) FacesUtils
                        .getBeanFromContext(controllerName);
                  // for inactive tab, set the flag for refresh on activation
                  if (currentView.getViewState().equals(ViewState.INACTIVE))
                  {
                     worklistTableBean.setNeedUpdateForActvityEvent(true);
                     if (trace.isDebugEnabled())
                     {
                        trace.debug("Worklist table refresh postponed till activation of view");
                     }
                  }
                  else
                  {
                     worklistTableBean.refresh();
                     if (trace.isDebugEnabled())
                     {
                        trace.debug("Worklist table refresh called");
                     }
                  }

               }
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Error in Updating Worklist Table", e);
      }

   }

   /**
    * Update the worklist launchPanel on autoRefresh
    */
   private void updateWorkflowLaunchPanel()
   {
      try
      {
         WorklistsBean worklist = (WorklistsBean) FacesUtils.getBeanFromContext(WORKLIST_BEAN);
         if (PortalApplication.getInstance().getPortalUiController().getPerspective().getName()
               .equals("WorkflowExecution"))
         {
            worklist.update();
            if (trace.isDebugEnabled())
            {
               trace.debug("Workflow launchpanel refresh called");
            }
         }
         else
         {
            // for inactive tab, set the flag for refresh on activation
            worklist.setNeedUpdateForWorklist(true);
            if (trace.isDebugEnabled())
            {
               trace.debug("Workflow launchpanel refresh postponed till activation of view");
            }
         }
      }
      catch (Exception e)
      {
         trace.error("Error in Updating Worklist Launch Panel", e);
      }
   }

}
