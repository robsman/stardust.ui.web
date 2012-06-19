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
package org.eclipse.stardust.ui.web.viewscommon.common.controller;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ModelEvent;
import org.eclipse.stardust.ui.event.ProcessEvent;
import org.eclipse.stardust.ui.event.RefreshEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;



/**
 * @author sauer
 * @version $Revision: $
 */
public class BpmEventsDispatcher
{
   
   private static final Logger trace = LogManager.getLogger(BpmEventsDispatcher.class);
   
   private static final BpmEventsController NOOP_EVENTS_CONTROLLER = new AbstractBpmEventsController()
   {
   };

   public static BpmEventsController getEventsController()
   {
      BpmEventsController eventsController = (BpmEventsController) ManagedBeanUtils.getManagedBean(JsfBpmEventsController.BEAN_ID);
      if (null == eventsController)
      {
         trace.warn("No events controller is available, please check your configuration.");
         eventsController = NOOP_EVENTS_CONTROLLER;
      }
      
      return eventsController;
   }
   
 /*  public static void sendModelEvent(ModelEvent event)
   {
      getEventsController().broadcastModelEvent(event);
   }
   
   @SuppressWarnings("deprecation")
   public static void sendModelEvent(ModelEvent.EventType eventType)
   {
      getEventsController().broadcastModelEvent(eventType);
   }
   
   @SuppressWarnings("deprecation")
   public static void sendProcessEvent(ProcessEvent.EventType eventType)
   {
      getEventsController().broadcastProcessEvent(eventType);
   }

   @SuppressWarnings("deprecation")
   public static void sendActivityEvent(ActivityEvent.EventType eventType)
   {
      getEventsController().broadcastActivityEvent(eventType);
   }  
   
   @SuppressWarnings("deprecation")
   public static void sendRefreshEvent(RefreshEvent.EventType eventType)
   {
      getEventsController().broadcastRefreshEvent(eventType);
   }*/
   public static void sendWorklistSelectionEvent(WorklistSelectionEvent event)
   {
      getEventsController().broadcastWorklistSelectionEvent(event);
   }
}
