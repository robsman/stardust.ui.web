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
package org.eclipse.stardust.ui.client.common;

import java.util.List;

import org.eclipse.stardust.common.config.FactoryFinder;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.client.common.spi.IClientContextLocator;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ModelEvent;
import org.eclipse.stardust.ui.event.ProcessEvent;
import org.eclipse.stardust.ui.event.RefreshEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;



/**
 * @author robert.sauer
 * @version $Revision: 31458 $
 */
public abstract class ClientContext
{
   private static final String KEY_CONTEXT_LOCATOR =
         ClientContext.class.getName() + ".ContextLocator";

   public abstract User getUser();

   public abstract ServiceFactory getServiceFactory();

   public abstract void close();

   public abstract void sendRefreshEvent(RefreshEvent event);

   public abstract void sendModelEvent(ModelEvent event);

   public abstract void sendProcessEvent(ProcessEvent event);

   public abstract void sendActivityEvent(ActivityEvent event);

   public abstract void sendWorklistSelectionEvent(WorklistSelectionEvent event);

   public static ClientContext getClientContext()
   {
      ClientContext context = null;

      Parameters params = Parameters.instance();

      IClientContextLocator locator = (IClientContextLocator)
            params.get(KEY_CONTEXT_LOCATOR);

      String clientType = params.getString("BpmClient.ClientType", "LegacyJSF");

      if (null == locator)
      {
         @SuppressWarnings("unchecked")
         List<IClientContextLocator.Factory> factories = FactoryFinder.findFactories(
               IClientContextLocator.Factory.class, null, null);

         if (factories.isEmpty())
         {
            throw new InvalidContextException("No context factories registered.", clientType);
         }

         for (int i = 0; i < factories.size(); ++i)
         {
            IClientContextLocator.Factory factory = factories.get(i);
            locator = factory.getLocator(clientType);
            if (null != locator)
            {
               params.set(KEY_CONTEXT_LOCATOR, locator);
               break;
            }
         }
      }

      if (locator == null)
      {
         throw new InvalidContextException("Unable to find a context locator for client type: " + clientType, clientType);
      }

      context = locator.getClientContext();

      if (context == null)
      {
         throw new InvalidContextException("Unable to initialize context: " + clientType, clientType);
      }
      
      return context;
   }

}
