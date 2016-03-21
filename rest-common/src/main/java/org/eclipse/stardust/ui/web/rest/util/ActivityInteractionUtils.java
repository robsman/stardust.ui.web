/*******************************************************************************
 * Copyright (c) 2016 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.util;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpiUtils;

/**
 * @author Subodh.Godbole
 *
 */
public class ActivityInteractionUtils
{
   /**
    * This method is copied from ActivityDetailsBean#getInteractionController()
    * At the moment all Interaction Controllers are present in process portal project so using reflection
    * TODO: Reflection needs to be removed & best time would be at the time of migrating activity panel to HTML5
    * 
    * @param activity
    * @return
    */
   public static IActivityInteractionController getInteractionController(Activity activity)
   {
      String clazz = "";
      IActivityInteractionController interactionController = ActivityInstanceUtils.getInteractionController(activity);

      if (SpiUtils.DEFAULT_JSF_ACTIVITY_CONTROLLER == interactionController)
      {
         clazz = "org.eclipse.stardust.ui.web.processportal.interaction.iframe.FaceletPanelInteractionController";
      }
      else if (SpiUtils.DEFAULT_EXTERNAL_WEB_APP_CONTROLLER == interactionController)
      {
         clazz = "org.eclipse.stardust.ui.web.processportal.interaction.iframe.ExternalWebAppInteractionController";
      }
      else if (SpiUtils.DEFAULT_JSP_ACTIVITY_CONTROLLER == interactionController)
      {
         clazz = "org.eclipse.stardust.ui.web.processportal.interaction.iframe.JspPanelInteractionController";
      }
      else if (SpiUtils.DEFAULT_MANUAL_ACTIVITY_CONTROLLER == interactionController)
      {
         clazz = "org.eclipse.stardust.ui.web.processportal.interaction.iframe.ManualActivityIframeInteractionController";
      }

      if (StringUtils.isNotEmpty(clazz))
      {
         try
         {
            interactionController = (IActivityInteractionController)Class.forName(clazz).newInstance();
         }
         catch (Exception e)
         {
         }
      }

      return interactionController;
   }
}
