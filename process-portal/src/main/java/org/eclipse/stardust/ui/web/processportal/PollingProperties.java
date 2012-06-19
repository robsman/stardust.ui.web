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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;



/**
 * @author Subodh.Godbole
 *
 */
public class PollingProperties
{
   private static final Logger trace = LogManager.getLogger(PollingProperties.class);
   private static final String BEAN_NAME = "pollingProperties";

   private boolean eagerActivation;
   private int pollIterations;
   private int pollDelay;

   /**
    * 
    */
   public PollingProperties()
   {
      Parameters parameters = Parameters.instance();
      eagerActivation = parameters.getBoolean("GUI.eagerNextActivityActivation", true);
      pollIterations = parameters.getInteger("GUI.nextActivityPollIterations", 3);
      pollDelay = parameters.getInteger("GUI.nextActivityPollDelay", 300);
   }

   /**
    * @return
    */
   public static PollingProperties getInstance()
   {
      return (PollingProperties)FacesUtils.getBeanFromContext(BEAN_NAME);
   }
   
   /**
    * @param activator
    * @return
    */
   public ActivityInstance poll(Activator activator)
   {
      if (eagerActivation)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Trying to retrieve the next activity " + pollIterations + " times.");
         }

         for (int i = 0; i < pollIterations; i++)
         {
            try
            {
               Thread.sleep(Math.max(10, pollDelay));
               ActivityInstance instance = activator.activateNext();
               if (null != instance)
               {
                  return instance;
               }
            }
            catch (InterruptedException e)
            {
               // just exit
            }

            if (trace.isDebugEnabled() && (i + 1) < pollIterations)
            {
               trace.debug("Retrying " + (pollIterations - i - 1) + " more times.");
            }
         }
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Eager Activation is not enabled.");
         }
      }
      return null;
   }

   public boolean isEagerActivation()
   {
      return eagerActivation;
   }

   public int getPollIterations()
   {
      return pollIterations;
   }

   public int getPollDelay()
   {
      return pollDelay;
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public static interface Activator
   {
      ActivityInstance activateNext();
   }
}
