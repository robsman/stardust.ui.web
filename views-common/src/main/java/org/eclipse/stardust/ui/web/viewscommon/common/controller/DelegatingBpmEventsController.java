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

import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ModelEvent;
import org.eclipse.stardust.ui.event.ProcessEvent;
import org.eclipse.stardust.ui.event.RefreshEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;

/**
 * @author sauer
 * @version $Revision: $
 */
public class DelegatingBpmEventsController implements BpmEventsController//TODO: Not in user,eligible for delete
{

   private BpmEventsController delegate;

   @SuppressWarnings("deprecation")
   public void broadcastModelEvent(ModelEvent.EventType eventType)
   {
      delegate.broadcastModelEvent(eventType);
   }

   public void broadcastModelEvent(ModelEvent event)
   {
      delegate.broadcastModelEvent(event);
   }

   @SuppressWarnings("deprecation")
   public void broadcastProcessEvent(ProcessEvent.EventType eventType)
   {
      delegate.broadcastProcessEvent(eventType);
   }

   public void broadcastProcessEvent(ProcessEvent event)
   {
      delegate.broadcastProcessEvent(event);
   }

   @SuppressWarnings("deprecation")
   public void broadcastActivityEvent(ActivityEvent.EventType eventType)
   {
      delegate.broadcastActivityEvent(eventType);
   }

   public void broadcastActivityEvent(ActivityEvent event)
   {
      delegate.broadcastActivityEvent(event);
   }

   public void broadcastWorklistSelectionEvent(WorklistSelectionEvent event)
   {
      delegate.broadcastWorklistSelectionEvent(event);
   }

   @SuppressWarnings("deprecation")
   public void broadcastRefreshEvent(RefreshEvent.EventType eventType)
   {
      delegate.broadcastRefreshEvent(eventType);
   }

   public void broadcastRefreshEvent(RefreshEvent event)
   {
      delegate.broadcastRefreshEvent(event);
   }

   public BpmEventsController getDelegate()
   {
      return delegate;
   }

   public void setDelegate(BpmEventsController delegate)
   {
      this.delegate = delegate;
   }

}
