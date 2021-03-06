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

import static org.eclipse.stardust.engine.core.interactions.Interaction.getInteractionId;
import static org.eclipse.stardust.ui.web.processportal.interaction.iframe.IframePanelUtils.getContentFrameId;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.ActivityInteractionControllerUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpiUtils;

import com.icesoft.faces.context.BridgeFacesContext;



/**
 * @author sauer
 * @version $Revision: $
 */
public class IceFacesActivityInteractionController implements IActivityInteractionController, ViewEventAwareInteractionController
{
   private static final Logger trace = LogManager.getLogger(IceFacesActivityInteractionController.class);

   public void handleEvent(ActivityInstance activityInstance, ViewEvent event)
   {
      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         break;

      case TO_BE_DEACTIVATED:
         break;

      case CLOSED:
         break;

      case LAUNCH_PANELS_ACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
      case FULL_SCREENED:
      case RESTORED_TO_NORMAL:
      case PINNED:
      case PERSPECTIVE_CHANGED:
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
               + getContentFrameId(activityInstance) + "', '" + uri + "', {html5ViewId: '" + event.getView().getHtml5FwViewId() + "'});";
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

   public String getContextId(ActivityInstance ai)
   {
      return PredefinedConstants.JSF_CONTEXT;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return PanelIntegrationStrategy.EMBEDDED_IFRAME;
   }

   public String providePanelUri(ActivityInstance ai)
   {
      // delegate to default controller
      String uri = SpiUtils.DEFAULT_JSF_ACTIVITY_CONTROLLER.providePanelUri(ai);

      if ( !StringUtils.isEmpty(uri) && uri.startsWith("/"))
      {
         // must prefix root relative URIs with the current context root
         try
         {
            FacesContext jsfContext = FacesContext.getCurrentInstance();

            uri = jsfContext.getExternalContext().getRequestContextPath() + uri;
         }
         catch (Exception e)
         {
            // not in JSF?
            trace.warn("Failed determining context root.", e);
         }
      }

      return uri;
   }

   public void initializePanel(ActivityInstance ai, Map inData)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();

      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
            facesContext, InteractionRegistry.BEAN_ID);
      if (null != registry)
      {
         // start new interaction
         ModelCache modelCache = ModelCache.findModelCache();
         SessionContext ippSessionContext = SessionContext.findSessionContext();

         Interaction interaction = new Interaction(ippSessionContext.getUser(),
               ai, getContextId(ai), modelCache);

         interaction.setInDataValues(inData);

         registry.registerInteraction(interaction);

         Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
         sessionMap.put(IframePanelConstants.KEY_COMMAND,
               IframePanelConstants.CMD_IFRAME_PANEL_INITIALIZE);
         sessionMap.put(IframePanelConstants.KEY_INTERACTION_ID, interaction.getId());
      }

      // TODO emit java script to load page into panel?
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario, Object parameters)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();

      if (ActivityInteractionControllerUtils.isExternalWebAppInterventionRequired(scenario))
      {
         trace.info("Triggering asynchronous close of activity panel ...");

         InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
               facesContext, InteractionRegistry.BEAN_ID);

         Interaction interaction = registry.getInteraction(Interaction.getInteractionId(ai));
         if ((null != interaction)
               && (Interaction.Status.Complete == interaction.getStatus()))
         {
            // out data mapping was already performed
            return true;
         }

         Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();

         sessionMap.put(IframePanelConstants.KEY_COMMAND, scenario.getId());
         sessionMap.put(IframePanelConstants.KEY_INTERACTION_ID,
               Interaction.getInteractionId(ai));

         // trigger remote end via JavaScript

         if (facesContext instanceof BridgeFacesContext)
         {
            PortalApplication.getInstance().addEventScript(
                  "parent.InfinityBpm.ProcessPortal.sendCloseCommandToExternalWebApp('"
                        + getContentFrameId(ai) + "', '" + scenario.getId() + "');");
         }
         else
         {
            trace.error("FacesContext other than ICEfaces is not supported...", new Throwable());
         }

         // close panel asynchronously after ICEfaces page responds via JavaScript
         return false;
      }
      else
      {
         unregisterInteraction(ai);
         // synchronously close panel as no custom post processing needs to occur
         return true;
      }
   }

   public Map getOutDataValues(ActivityInstance ai)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();

      Map<String, ? extends Serializable> outData = null;

      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(
            facesContext, InteractionRegistry.BEAN_ID);

      Interaction interaction = registry.getInteraction(Interaction.getInteractionId(ai));
      if (null != interaction)
      {
         outData = interaction.getOutDataValues();
      }

      return outData;
   }

   public static class Factory implements IActivityInteractionController.Factory
   {

      private static final IceFacesActivityInteractionController INSTANCE = new IceFacesActivityInteractionController();

      public IActivityInteractionController getInteractionController(Activity activity)
      {
         if (activity.isInteractive())
         {
            ApplicationContext jsfAppContext = activity.getApplicationContext(PredefinedConstants.JSF_CONTEXT);

            if (null != jsfAppContext)
            {
               String panelUrl = (String) jsfAppContext.getAttribute("jsf:url");
               if ( !StringUtils.isEmpty(panelUrl)
                     && (panelUrl.endsWith(".iface") || ( -1 != panelUrl.indexOf(".iface?"))))
               {
                  return INSTANCE;
               }
            }
         }

         return null;
      }

   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#unregisterInteraction(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public boolean unregisterInteraction(ActivityInstance ai)
   {
      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(InteractionRegistry.BEAN_ID);
      if (registry != null)
      {
         // destroy interaction resource
         registry.unregisterInteraction(getInteractionId(ai));
         return true;
      }
      return false;
   }

   public boolean isTypedDocumentOpen(ActivityInstance activityInstance)
   {
      return false;
   }
}
