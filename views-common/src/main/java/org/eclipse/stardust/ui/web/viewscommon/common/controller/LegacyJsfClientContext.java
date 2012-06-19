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

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.client.common.ClientContext;
import org.eclipse.stardust.ui.client.common.spi.IClientContextLocator;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ModelEvent;
import org.eclipse.stardust.ui.event.ProcessEvent;
import org.eclipse.stardust.ui.event.RefreshEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class LegacyJsfClientContext extends ClientContext
{
   
   @Override
   public User getUser()
   {
      return legacySessionContext().getUser();
   }
   
  // @Override
   public ServiceFactory getServiceFactory()
   {
      return legacySessionContext().getServiceFactory();
   } 


   @Override
   public void close()
   {
      // TODO better ignore for the time being?
      legacySessionContext().logout();
   }

   @Override
   public void sendRefreshEvent(RefreshEvent event)
   {
      legacyBpmEventsController().broadcastRefreshEvent(event);
   }
   
   @Override
   public void sendModelEvent(ModelEvent event)
   {
      legacyBpmEventsController().broadcastModelEvent(event);
   }
   
   @Override
   public void sendProcessEvent(ProcessEvent event)
   {
      legacyBpmEventsController().broadcastProcessEvent(event);
   }
   
   @Override
   public void sendActivityEvent(ActivityEvent event)
   {
      legacyBpmEventsController().broadcastActivityEvent(event);
   }

   @Override
   public void sendWorklistSelectionEvent(WorklistSelectionEvent event)
   {
      legacyBpmEventsController().broadcastWorklistSelectionEvent(event);
   }

   private static SessionContext legacySessionContext()
   {
      return SessionContext.findSessionContext();
   }

   private static BpmEventsController legacyBpmEventsController()
   {
      return BpmEventsDispatcher.getEventsController();
   }

   public static class Factory implements IClientContextLocator.Factory
   {
      
      private static final IClientContextLocator INSTANCE = new IClientContextLocator()
      {
         private final LegacyJsfClientContext CONTEXT = new LegacyJsfClientContext();

         public ClientContext getClientContext()
         {
            return CONTEXT;
         }
      };

      public IClientContextLocator getLocator(String contextId)
      {
         return "LegacyJSF".equals(contextId) ? INSTANCE : null;
      }
      
   }
   
}
