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
package org.eclipse.stardust.ui.web.processportal;

import java.io.Serializable;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ActivityEventObserver;
import org.eclipse.stardust.ui.event.Observers;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionObserver;



/**
 * <b>Mediator</b> for (most) common-client Events
 * 
 * @author roland.stamm
 * 
 */
public class EventController implements ActivityEventObserver, WorklistSelectionObserver, Serializable
{
   private static final long serialVersionUID = 1L;

   private static Logger log = LogManager.getLogger(EventController.class);

   transient private Observers<WorklistSelectionObserver> worklistSelectionEventObservers = new Observers<WorklistSelectionObserver>();

   transient private Observers<ActivityEventObserver> activityEventObservers = new Observers<ActivityEventObserver>();

   /**
    * @param clientContext
    */
   public EventController()
   {
   }


   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.event.ActivityEventObserver#handleEvent(org.eclipse.stardust.ui.event.ActivityEvent)
    */
   public void handleEvent(ActivityEvent event)
   {
      getActivityEventObservers().notifyObservers(event);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.event.WorklistSelectionObserver#handleEvent(org.eclipse.stardust.ui.event.WorklistSelectionEvent)
    */
   public void handleEvent(WorklistSelectionEvent event)
   {
      log.info("ParticipantWorklistSelectionEvent: " + event.getClass().getName());

      getWorklistSelectionEventObservers().notifyObservers(event);
   }

   /**
    * @param observer
    */
   public void registerObserver(WorklistSelectionObserver observer)
   {
      getWorklistSelectionEventObservers().add(observer);
   }

   /**
    * @param observer
    * @return
    */
   public boolean unregisterObserver(WorklistSelectionObserver observer)
   {
      return getWorklistSelectionEventObservers().remove(observer);
   }

   /**
    * @param observer
    */
   public void registerObserver(ActivityEventObserver observer)
   {
      getActivityEventObservers().add(observer);
   }

   /**
    * @param observer
    * @return
    */
   public boolean unregisterObserver(ActivityEventObserver observer)
   {
      return getActivityEventObservers().remove(observer);
   }
   
   /**
    * 
    */
   public void destroy()
   {
      for (ActivityEventObserver activityEventObserver : getActivityEventObservers().asList())
      {
         getActivityEventObservers().remove(activityEventObserver);
      }
      
      for (WorklistSelectionObserver worklistSelectionEventObserver : getWorklistSelectionEventObservers().asList())
      {
         getWorklistSelectionEventObservers().remove(worklistSelectionEventObserver);
      }
   }

   private Observers<WorklistSelectionObserver> getWorklistSelectionEventObservers()
   {
      if (null == worklistSelectionEventObservers)
      {
         worklistSelectionEventObservers = new Observers<WorklistSelectionObserver>();
      }
      
      return worklistSelectionEventObservers;
   }


   private Observers<ActivityEventObserver> getActivityEventObservers()
   {
      if (null == activityEventObservers)
      {
         activityEventObservers = new Observers<ActivityEventObserver>();
      }
      
      return activityEventObservers;
   }

}
