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
public interface BpmEventsController
{

   void broadcastWorklistSelectionEvent(WorklistSelectionEvent event);

   void broadcastProcessEvent(ProcessEvent event);

   @Deprecated
   void broadcastProcessEvent(ProcessEvent.EventType eventType);

   void broadcastActivityEvent(ActivityEvent event);

   @Deprecated
   void broadcastActivityEvent(ActivityEvent.EventType eventType);

   void broadcastModelEvent(ModelEvent event);

   @Deprecated
   void broadcastModelEvent(ModelEvent.EventType eventType);

   void broadcastRefreshEvent(RefreshEvent event);

   @Deprecated
   void broadcastRefreshEvent(RefreshEvent.EventType eventType);

}
