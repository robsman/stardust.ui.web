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
package org.eclipse.stardust.ui.web.bcc;

import org.eclipse.stardust.ui.web.common.event.PerspectiveEvent;
import org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class BusinessControlCenterPerspectiveEventHandler implements PerspectiveEventHandler
{
   private boolean initialized;

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.PerspectiveEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.PerspectiveEvent)
    */
   public void handleEvent(PerspectiveEvent event)
   {
      switch (event.getType())
      {
      case ACTIVATED:
         if (!initialized)
         {
            WorkflowFacade.createWorkflowFacade(ServiceFactoryUtils.getServiceFactory());
            initialized = true;
         }
         break;
      }
   }
}
