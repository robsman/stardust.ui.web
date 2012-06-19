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
package org.eclipse.stardust.ui.web.processportal.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ActivityEventObserver;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionObserver;
import org.eclipse.stardust.ui.web.processportal.EventController;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.BpmEventsDispatcher;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.JsfBpmEventsController;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class BpmEventsBridge implements ActivityEventObserver, WorklistSelectionObserver, Serializable
{
   private static final long serialVersionUID = 1L;

   public static final String BEAN_NAME = "ippRequestScopedBpmEventBridge";

   private EventController eventController;
   
   private JsfBpmEventsController jsfBpmEventsController;

   public void setEventController(EventController eventController)
   {
      this.eventController = eventController;
   }

   public void handleEvent(ActivityEvent activityEvent)
   {
      eventController.handleEvent(activityEvent);
   }

   public void handleEvent(WorklistSelectionEvent participantWorklistSelectionEvent)
   {
      eventController.handleEvent(participantWorklistSelectionEvent);
   }
   
   public String getKeepAlive()
   {
      return "";
   }

   public void init()
   {
      jsfBpmEventsController = (JsfBpmEventsController) BpmEventsDispatcher.getEventsController();
      List<ActivityEventObserver> activityEventObservers = jsfBpmEventsController.getActivityEventObservers();
      if (activityEventObservers == null)
      {
         activityEventObservers = new ArrayList<ActivityEventObserver>();
         jsfBpmEventsController.setActivityEventObservers(activityEventObservers);
      }
      activityEventObservers.add(this);

      List<WorklistSelectionObserver> worklistSelectionObservers = jsfBpmEventsController.getWorklistSelectionObservers();
      if (worklistSelectionObservers == null)
      {
         worklistSelectionObservers = new ArrayList<WorklistSelectionObserver>();
         jsfBpmEventsController.setWorklistSelectionObservers(worklistSelectionObservers);
      }
      worklistSelectionObservers.add(this);
   }

   public void destroy()
   {
      List<ActivityEventObserver> activityEventObservers = jsfBpmEventsController.getActivityEventObservers();
      if (activityEventObservers != null)
      {

         activityEventObservers.remove(this);
      }

      List<WorklistSelectionObserver> worklistSelectionObservers = jsfBpmEventsController.getWorklistSelectionObservers();
      if (worklistSelectionObservers != null)
      {

         worklistSelectionObservers.remove(this);
      }
   }

}
