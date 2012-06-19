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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.io.Serializable;
import java.util.List;

import org.eclipse.stardust.common.config.ExtensionProviderUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ImplementationType;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.viewscommon.common.AbstractProcessExecutionPortal;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.CasabacActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.ExternalWebAppActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.JsfActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.JspActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.ManualActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.ActivityInteractionHandler2Adapter;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionHandler2;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionHandler.Factory;


/**
 * @author rsauer
 * @version $Revision$
 */
public class SpiUtils
{
   private static final Logger trace = LogManager.getLogger(SpiUtils.class);

   public static final IActivityInteractionController DEFAULT_JSF_ACTIVITY_CONTROLLER = new JsfActivityInteractionController();

   public static final IActivityInteractionController DEFAULT_JSP_ACTIVITY_CONTROLLER = new JspActivityInteractionController();

   public static final IActivityInteractionController DEFAULT_CAD_ACTIVITY_CONTROLLER = new CasabacActivityInteractionController();

   public static final IActivityInteractionController DEFAULT_EXTERNAL_WEB_APP_CONTROLLER = new ExternalWebAppActivityInteractionController();

   public static final IActivityInteractionController DEFAULT_MANUAL_ACTIVITY_CONTROLLER = new ManualActivityInteractionController();

   // TODO private static final String RESOLVED_INTERACTION_HANDLER = SpiUtils.class.getName() + ".ResolvedActivityInteractionHandler";
   
   // TODO private static final String RESOLVED_INTERACTION_CONTROLLER = SpiUtils.class.getName() + ".ResolvedActivityInteractionController";
   
   private static final IActivityInteractionHandler NONE = new UnknownActivityInteractionHandler();
   
   public static IActivityInteractionHandler getInteractionHandler(Activity activity)
   {
      IActivityInteractionHandler strategy = null; // TODO (IActivityInteractionHandler) activity.getAttribute(RESOLVED_INTERACTION_HANDLER);

      if (null == strategy)
      {
         List/*<Factory>*/ factories = ExtensionProviderUtils.getExtensionProviders(IActivityInteractionHandler.Factory.class);
         
         for (int i = 0; i < factories.size(); ++i)
         {
            final Factory aihFactory = (Factory) factories.get(i);
            
            strategy = aihFactory.getInteractionHandler(activity);
            if (null != strategy)
            {
               break;
            }
         }

         //TODO activity.setAttribute(RESOLVED_INTERACTION_HANDLER, (null != strategy) ? strategy : NONE);
      }
      
      return (NONE == strategy) ? null : strategy;
   }
   
   public static IActivityInteractionController getInteractionController(Activity activity)
   {
      IActivityInteractionController controller = null; // TODO (IActivityInteractionHandler) activity.getAttribute(RESOLVED_INTERACTION_CONTROLLER);

      if (null == controller)
      {
         // backwards compatibility
         List/*<IActivityInteractionHandler.Factory>*/ aihFactories = ExtensionProviderUtils.getExtensionProviders(IActivityInteractionHandler.Factory.class);
         for (int i = 0; i < aihFactories.size(); ++i)
         {
            final IActivityInteractionHandler.Factory aihFactory = (IActivityInteractionHandler.Factory) aihFactories.get(i);
            
            IActivityInteractionHandler aih = aihFactory.getInteractionHandler(activity);
            if (aih instanceof IActivityInteractionHandler2)
            {
               trace.warn("Use of the deprecated interface "
                     + IActivityInteractionHandler2.class.getName() + " is discouraged: "
                     + aih.getClass().getName());
               
               controller = new ActivityInteractionHandler2Adapter((IActivityInteractionHandler2) aih);
               break;
            }
         }

         if (null == controller)
         {
            List/*<IActivityInteractionController.Factory>*/ factories = ExtensionProviderUtils.getExtensionProviders(IActivityInteractionController.Factory.class);
            for (int i = 0; i < factories.size(); ++i)
            {
               final IActivityInteractionController.Factory aicFactory = (IActivityInteractionController.Factory) factories.get(i);
               
               controller = aicFactory.getInteractionController(activity);
               if (null != controller)
               {
                  break;
               }
            }
         }
         
         if (null == controller)
         {
            // finally try to resolve to default implementations
            controller = getDefaultInteractionController(activity);

            if (null == controller)
            {
               // TODO raise exception?
            }
         }

         //TODO activity.setAttribute(RESOLVED_INTERACTION_CONTROLLER, (null != strategy) ? controller : NONE);
      }
      
      return (NONE == controller) ? null : controller;
   }
   
   private SpiUtils()
   {
      // utility class
   }

   private static IActivityInteractionController getDefaultInteractionController(
         Activity activity)
   {
      if (null != activity.getApplicationContext(PredefinedConstants.JSF_CONTEXT))
      {
         return DEFAULT_JSF_ACTIVITY_CONTROLLER;
      }
      else if (null != activity.getApplicationContext(ExternalWebAppActivityInteractionController.EXT_WEB_APP_CONTEXT_ID))
      {
         return DEFAULT_EXTERNAL_WEB_APP_CONTROLLER;
      }
      else if (null != activity.getApplicationContext(CasabacActivityInteractionController.CASABAC_CONTEXT))
      {
         return DEFAULT_CAD_ACTIVITY_CONTROLLER;
      }
      else if (null != activity.getApplicationContext(PredefinedConstants.JSP_CONTEXT))
      {
         return DEFAULT_JSP_ACTIVITY_CONTROLLER;
      }
      else if (ImplementationType.Manual.equals(activity.getImplementationType())
            || (null != activity.getApplicationContext(PredefinedConstants.DEFAULT_CONTEXT)))
      {
         return DEFAULT_MANUAL_ACTIVITY_CONTROLLER;
      }
      
      return null;
   }

   private static final class UnknownActivityInteractionHandler
         implements IActivityInteractionHandler, Serializable
   {

      static final long serialVersionUID = 1L;

      public String launchWorkshopActivity(ActivityInstance activityInstance,
            AbstractProcessExecutionPortal portal)
      {
         raiseUnsupportedOperationException();

         return null;
      }

      private void raiseUnsupportedOperationException()
      {
         throw new UnsupportedOperationException(
               "Methods on marker instance must never be invoked.");
      }
   }
}
