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

import java.util.List;

import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ActivityEventObserver;
import org.eclipse.stardust.ui.event.ModelEvent;
import org.eclipse.stardust.ui.event.ModelEventObserver;
import org.eclipse.stardust.ui.event.ProcessEvent;
import org.eclipse.stardust.ui.event.ProcessEventObserver;
import org.eclipse.stardust.ui.event.RefreshEvent;
import org.eclipse.stardust.ui.event.RefreshEventObserver;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionObserver;


/**
 * @author sauer
 * @version $Revision: $
 */
public class JsfBpmEventsController implements BpmEventsController
{
   
   public static final String BEAN_ID = "infinityBpmEventsController";
   
   private BpmEventsController parentBpmEventsController;
   
   // TODO use Observers class instead
   private List<WorklistSelectionObserver> worklistSelectionObservers;

   // TODO use Observers class instead
   private List<ProcessEventObserver> processEventObservers;

   // TODO use Observers class instead
   private List<ActivityEventObserver> activityEventObservers;

   // TODO use Observers class instead
   private List<ModelEventObserver> modelEventObservers;

   // TODO use Observers class instead
   private List<RefreshEventObserver> refreshEventObservers;

//   private UIComponent ctrlBpmEvents;
//   
//   private UIComponent ctrlModelEvent;
//   
//   private UIComponent ctrlWorklistSelectionEvent;
//   
//   private UIComponent ctrlProcessEvent;
//   
//   private UIComponent ctrlActivityEvent;
//   
//   private UIComponent ctrlRefreshEvent;
   
//   public void broadcastBpmEvent(AbstractEvent<? extends AbstractEventObserver> event)
//   {
//      if (null != ctrlBpmEvents)
//      {
//         RequestContext trContext = RequestContext.getCurrentInstance();
//         
//         trContext.partialUpdateNotify(ctrlBpmEvents);
//      }
//   }

   public void broadcastWorklistSelectionEvent(WorklistSelectionEvent event)
   {
 //     WorklistSelectionEvent.EventType eventType = event.getType();  
 //     broadcastBpmEvent(event);
      
//      if (null != ctrlWorklistSelectionEvent)
//      {
//         RequestContext trContext = RequestContext.getCurrentInstance();
//         
//         trContext.partialUpdateNotify(ctrlWorklistSelectionEvent);
//         
//         UIComponent ctrlSpecificEvent = null;
//         if (WorklistSelectionEvent.SELECTED == eventType)
//         {
//            ctrlSpecificEvent = ctrlWorklistSelectionEvent.findComponent("selected");
//         }
//         else if (WorklistSelectionEvent.INSTANCE_SELECTED == eventType)
//         {
//            ctrlSpecificEvent = ctrlWorklistSelectionEvent.findComponent("instance_selected");
//         }
//         
//         if (null != ctrlSpecificEvent)
//         {
//            trContext.partialUpdateNotify(ctrlSpecificEvent);
//         }
//      }
      
      if (null != worklistSelectionObservers)
      {
         // TODO use Observers class instead
         for (int i = 0; i < worklistSelectionObservers.size(); ++i)
         {
            WorklistSelectionObserver observer = worklistSelectionObservers.get(i);
            observer.handleEvent(event);
         }
      }
      
      if (null != parentBpmEventsController)
      {
         parentBpmEventsController.broadcastWorklistSelectionEvent(event);
      }
   }

   public void broadcastModelEvent(ModelEvent.EventType eventType)
   {
      // TODO bind real model
      broadcastModelEvent(new ModelEvent(eventType, null));
   }

   public void broadcastModelEvent(ModelEvent event)
   {
//      broadcastBpmEvent(event);
//      
//      if (null != ctrlModelEvent)
//      {
//         RequestContext trContext = RequestContext.getCurrentInstance();
//         
//         trContext.partialUpdateNotify(ctrlModelEvent);
//         
//         UIComponent ctrlSpecificEvent = null;
//         if (ModelEvent.DEPLOYED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlModelEvent.findComponent("deployed");
//         }
//         else if (ModelEvent.UPDATED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlModelEvent.findComponent("updated");
//         }
//         else if (ModelEvent.NEW_ACTIVE_MODEL == event.getType())
//         {
//            ctrlSpecificEvent = ctrlModelEvent.findComponent("new_active_model");
//         }
//         else if (ModelEvent.REMOVED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlModelEvent.findComponent("removed");
//         }
//         
//         if (null != ctrlSpecificEvent)
//         {
//            trContext.partialUpdateNotify(ctrlSpecificEvent);
//         }
//      }
      
      if (null != modelEventObservers)
      {
         // TODO use Observers class instead
         for (int i = 0; i < modelEventObservers.size(); ++i)
         {
            ModelEventObserver observer = modelEventObservers.get(i);
            observer.handleEvent(event);
         }
      }
      
      if (null != parentBpmEventsController)
      {
         parentBpmEventsController.broadcastModelEvent(event);
      }
   }
   
   public void broadcastProcessEvent(ProcessEvent.EventType eventType)
   {
      // TODO bind real process
      broadcastProcessEvent(new ProcessEvent(eventType, null));
   }

   public void broadcastProcessEvent(ProcessEvent event)
   {
//      broadcastBpmEvent(event);
//      
//      if (null != ctrlProcessEvent)
//      {
//         RequestContext trContext = RequestContext.getCurrentInstance();
//         
//         trContext.partialUpdateNotify(ctrlProcessEvent);
//         
//         UIComponent ctrlSpecificEvent = null;
//         if (ProcessEvent.STARTED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlProcessEvent.findComponent("started");
//         }
//         else if (ProcessEvent.COMPLETED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlProcessEvent.findComponent("completed");
//         }
//         
//         if (null != ctrlSpecificEvent)
//         {
//            trContext.partialUpdateNotify(ctrlSpecificEvent);
//         }
//      }
      
      if (null != processEventObservers)
      {
         // TODO use Observers class instead
         for (int i = 0; i < processEventObservers.size(); ++i)
         {
            ProcessEventObserver observer = processEventObservers.get(i);
            observer.handleEvent(event);
         }
      }
      
      if (null != parentBpmEventsController)
      {
         parentBpmEventsController.broadcastProcessEvent(event);
      }
   }

   public void broadcastActivityEvent(ActivityEvent.EventType eventType)
   {
      // TODO bind real activity
      broadcastActivityEvent(new ActivityEvent(eventType, null, false));
   }

   public void broadcastActivityEvent(ActivityEvent event)
   {
//      broadcastBpmEvent(event);
//      
//      if (null != ctrlActivityEvent)
//      {
//         RequestContext trContext = RequestContext.getCurrentInstance();
//         
//         trContext.partialUpdateNotify(ctrlActivityEvent);
//         
//         UIComponent ctrlSpecificEvent = null;
//         if (ActivityEvent.STARTED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlActivityEvent.findComponent("started");
//         }
//         else if (ActivityEvent.ACTIVATED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlActivityEvent.findComponent("activated");
//         }
//         else if (ActivityEvent.SUSPENDED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlActivityEvent.findComponent("suspended");
//         }
//         else if (ActivityEvent.COMPLETED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlActivityEvent.findComponent("completed");
//         }
//         else if (ActivityEvent.ABORTED == event.getType())
//         {
//            ctrlSpecificEvent = ctrlActivityEvent.findComponent("aborted");
//         }
//         
//         if (null != ctrlSpecificEvent)
//         {
//            trContext.partialUpdateNotify(ctrlSpecificEvent);
//         }
//      }
      
      if (null != activityEventObservers)
      {
         // TODO use Observers class instead
         for (int i = 0; i < activityEventObservers.size(); ++i)
         {
            ActivityEventObserver observer = activityEventObservers.get(i);
            observer.handleEvent(event);
         }
      }
      
      if (null != parentBpmEventsController)
      {
         parentBpmEventsController.broadcastActivityEvent(event);
      }
   }

   public void broadcastRefreshEvent(RefreshEvent.EventType eventType)
   {
      // TODO bind real model
      broadcastRefreshEvent(new RefreshEvent(eventType));
   }

   public void broadcastRefreshEvent(RefreshEvent event)
   {
//      broadcastBpmEvent(event);
//      
//      if (null != ctrlRefreshEvent)
//      {
//         RequestContext trContext = RequestContext.getCurrentInstance();
//         
//         trContext.partialUpdateNotify(ctrlRefreshEvent);
//         
//         UIComponent ctrlSpecificEvent = null;
//         if (RefreshEvent.FULL == event.getType())
//         {
//            ctrlSpecificEvent = ctrlRefreshEvent.findComponent("full");
//         }
//         else if (RefreshEvent.WORKLIST_OUTLINE == event.getType())
//         {
//            ctrlSpecificEvent = ctrlRefreshEvent.findComponent("worklistOutline");
//         }
//         else if (RefreshEvent.WORKAREA == event.getType())
//         {
//            ctrlSpecificEvent = ctrlRefreshEvent.findComponent("workarea");
//         }
//         
//         if (null != ctrlSpecificEvent)
//         {
//            trContext.partialUpdateNotify(ctrlSpecificEvent);
//         }
//      }
      
      if (null != refreshEventObservers)
      {
         // TODO use Observers class instead
         for (int i = 0; i < refreshEventObservers.size(); ++i)
         {
            RefreshEventObserver observer = refreshEventObservers.get(i);
            if (null != observer)
            {
               observer.handleEvent(event);
            }
         }
      }
      
      if (null != parentBpmEventsController)
      {
         parentBpmEventsController.broadcastRefreshEvent(event);
      }
   }
   
   public BpmEventsController getParentBpmEventsController()
   {
      return parentBpmEventsController;
   }

   public void setParentBpmEventsController(BpmEventsController parentBpmEventsController)
   {
      this.parentBpmEventsController = parentBpmEventsController;
   }

   public List<WorklistSelectionObserver> getWorklistSelectionObservers()
   {
      return worklistSelectionObservers;
   }

   public void setWorklistSelectionObservers(List<WorklistSelectionObserver> observers)
   {
      this.worklistSelectionObservers = observers;
   }

   public List<ProcessEventObserver> getProcessEventObservers()
   {
      return processEventObservers;
   }

   public void setProcessEventObservers(List<ProcessEventObserver> processEventObservers)
   {
      this.processEventObservers = processEventObservers;
   }

   public List<ActivityEventObserver> getActivityEventObservers()
   {
      return activityEventObservers;
   }

   public void setActivityEventObservers(List<ActivityEventObserver> activityEventObservers)
   {
      this.activityEventObservers = activityEventObservers;
   }

   public List<ModelEventObserver> getModelEventObservers()
   {
      return modelEventObservers;
   }

   public void setModelEventObservers(List<ModelEventObserver> modelEventObservers)
   {
      this.modelEventObservers = modelEventObservers;
   }

   public List<RefreshEventObserver> getRefreshEventObservers()
   {
      return refreshEventObservers;
   }

   public void setRefreshEventObservers(List<RefreshEventObserver> refreshEventObservers)
   {
      this.refreshEventObservers = refreshEventObservers;
   }

//   public UIComponent getCtrlBpmEvents()
//   {
//      return ctrlBpmEvents;
//   }
//
//   public void setCtrlBpmEvents(UIComponent ctrlBpmEvents)
//   {
//      this.ctrlBpmEvents = ctrlBpmEvents;
//   }
//
//   public UIComponent getCtrlModelEvent()
//   {
//      return ctrlModelEvent;
//   }
//   
//   public void setCtrlModelEvent(UIComponent ctrlModelEvent)
//   {
//      this.ctrlModelEvent = ctrlModelEvent;
//   }
//   
//   public UIComponent getCtrlWorklistSelectionEvent()
//   {
//      return ctrlWorklistSelectionEvent;
//   }
//
//   public void setCtrlWorklistSelectionEvent(UIComponent ctrlWorklistSelectionEvent)
//   {
//      this.ctrlWorklistSelectionEvent = ctrlWorklistSelectionEvent;
//   }
//
//   public UIComponent getCtrlProcessEvent()
//   {
//      return ctrlProcessEvent;
//   }
//
//   public void setCtrlProcessEvent(UIComponent ctrlProcessEvent)
//   {
//      this.ctrlProcessEvent = ctrlProcessEvent;
//   }
//
//   public UIComponent getCtrlActivityEvent()
//   {
//      return ctrlActivityEvent;
//   }
//
//   public void setCtrlActivityEvent(UIComponent ctrlActivityEvent)
//   {
//      this.ctrlActivityEvent = ctrlActivityEvent;
//   }
//
//   public UIComponent getCtrlRefreshEvent()
//   {
//      return ctrlRefreshEvent;
//   }
//
//   public void setCtrlRefreshEvent(UIComponent ctrlRefreshEvent)
//   {
//      this.ctrlRefreshEvent = ctrlRefreshEvent;
//   }

}
