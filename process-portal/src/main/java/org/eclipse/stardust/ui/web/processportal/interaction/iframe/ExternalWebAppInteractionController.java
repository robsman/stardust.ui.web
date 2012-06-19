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
package org.eclipse.stardust.ui.web.processportal.interaction.iframe;

import static org.eclipse.stardust.ui.web.processportal.interaction.iframe.IframePanelUtils.getContentFrameId;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.ExternalWebAppActivityInteractionController;

import com.icesoft.faces.context.effects.JavascriptContext;



public class ExternalWebAppInteractionController
      extends ExternalWebAppActivityInteractionController
      implements ViewEventAwareInteractionController
{
   public void handleEvent(ActivityInstance activityInstance, ViewEvent event)
   {
      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         String uri = providePanelUri(activityInstance);

         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
               "InfinityBpm.ProcessPortal.createOrActivateContentFrame('"
                     + getContentFrameId(activityInstance) + "', '" + uri + "');");
         break;

      case TO_BE_DEACTIVATED:
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
               "InfinityBpm.ProcessPortal.deactivateContentFrame('"
                     + getContentFrameId(activityInstance) + "');");
         break;

      case CLOSED:
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
               "InfinityBpm.ProcessPortal.closeContentFrame('"
                     + getContentFrameId(activityInstance) + "');");
         break;
         
      case LAUNCH_PANELS_ACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
      case FULL_SCREENED:
      case RESTORED_TO_NORMAL:
      case PINNED:
      case PERSPECTIVE_CHANGED:
         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
               "InfinityBpm.ProcessPortal.resizeContentFrame('"
                     + getContentFrameId(activityInstance) + "');");
         break;
      }
   }

   public String getEventScript(ActivityInstance activityInstance, ViewEvent event)
   {
      String eventScript = "";
     
      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         String uri = providePanelUri(activityInstance);

         eventScript = "InfinityBpm.ProcessPortal.createOrActivateContentFrame('"
               + getContentFrameId(activityInstance) + "', '" + uri + "');";
         break;

      case TO_BE_DEACTIVATED:
         eventScript = "InfinityBpm.ProcessPortal.deactivateContentFrame('"
               + getContentFrameId(activityInstance) + "');";
         break;

      case CLOSED:
         eventScript = "InfinityBpm.ProcessPortal.closeContentFrame('"
               + getContentFrameId(activityInstance) + "');";
         break;
         
      case LAUNCH_PANELS_ACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
      case FULL_SCREENED:
      case RESTORED_TO_NORMAL:
      case PINNED:
      case PERSPECTIVE_CHANGED:
         eventScript = "InfinityBpm.ProcessPortal.resizeContentFrame('"
               + getContentFrameId(activityInstance) + "');";
         break;
      }

      return eventScript;
   }
}
