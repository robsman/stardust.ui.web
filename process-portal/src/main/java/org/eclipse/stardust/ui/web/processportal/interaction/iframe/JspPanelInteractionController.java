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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.JspActivityInteractionController;

import com.icesoft.faces.context.effects.JavascriptContext;



/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public class JspPanelInteractionController extends JspActivityInteractionController implements ViewEventAwareInteractionController
{
   private static final Logger trace = LogManager.getLogger(JspPanelInteractionController.class);

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController#handleEvent(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ActivityInstance activityInstance, ViewEvent event)
   {
      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         String uri = provideIframePanelUri(activityInstance);

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
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController#getEventScript(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public String getEventScript(ActivityInstance activityInstance, ViewEvent event)
   {
      String eventScript = "";
     
      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         String uri = provideIframePanelUri(activityInstance);

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

   /**
    * @param ai
    * @return
    */
   private String provideIframePanelUri(ActivityInstance ai)
   {
      String returnUri = null;

      try
      {
         String panelUri = providePanelUri(ai);
         if (panelUri.startsWith("/") 
               || panelUri.toLowerCase().startsWith("http://")
               || panelUri.toLowerCase().startsWith("https://"))
         {
            returnUri = panelUri;
         }
         else
         {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            returnUri = facesContext.getExternalContext().getRequestContextPath() + "/" + panelUri;
         }

         return returnUri;
      }
      catch (Exception e)
      {
         trace.warn("Failed determining context root.", e);
      }

      return returnUri;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.jsf.processportal.spi.IActivityInteractionController#getPanelIntegrationStrategy(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return PanelIntegrationStrategy.EMBEDDED_IFRAME;
   }
}
