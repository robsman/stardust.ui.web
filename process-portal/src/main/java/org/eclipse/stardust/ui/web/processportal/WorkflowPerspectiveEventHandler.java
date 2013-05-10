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

import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler;
import org.eclipse.stardust.ui.web.processportal.common.WorkflowTimerHandler;
import org.eclipse.stardust.ui.web.processportal.launchpad.WorklistsBean;

/**
 *
 * @author Sidharth.Singh
 *
 */
public class WorkflowPerspectiveEventHandler implements PerspectiveEventHandler
{
   private boolean initialized;

   /**
    *
    */
   public void handleEvent(PerspectiveEvent event)
   {
      switch (event.getType())
      {
      case ACTIVATED:
         if (!initialized)
         {
            // Start the timer on Activation
            WorkflowTimerHandler.getInstance().startTimer();
            initialized = true;
         }
         // On Re-activation the update should trigger , when Auto-refresh is configured
         if (WorklistsBean.getInstance().isNeedUpdateForWorklist())
         {
            WorklistsBean.getInstance().update();
            WorklistsBean.getInstance().setNeedUpdateForWorklist(false);
         }
         break;
      }
   }

}
