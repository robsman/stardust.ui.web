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

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.core.interactions.Interaction.getInteractionId;
import static org.eclipse.stardust.ui.web.processportal.interaction.iframe.IframePanelUtils.getContentFrameId;

import java.io.Serializable;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpiUtils;
import org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController;

import com.icesoft.faces.context.effects.JavascriptContext;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class FaceletPanelInteractionController implements IActivityInteractionController, ViewEventAwareInteractionController
{
   private static final Logger trace = LogManager.getLogger(FaceletPanelInteractionController.class);

   public static final String VIEW_ID_NON_IFACE_FACELET_CONTAINER = "/plugins/processportal/integration/trinidad/facelet-panel-container.xhtml";

   public void handleEvent(ActivityInstance activityInstance, ViewEvent event)
   {
      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         String uri = provideIframePanelUri(activityInstance, event.getView());

//         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
//               "InfinityBpm.ProcessPortal.createOrActivateContentFrame('"
//                     + getContentFrameId(activityInstance) + "', '" + uri + "');");
         break;

      case TO_BE_DEACTIVATED:
//         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
//               "InfinityBpm.ProcessPortal.deactivateContentFrame('"
//                     + getContentFrameId(activityInstance) + "');");
         break;

      case CLOSED:
//         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
//               "InfinityBpm.ProcessPortal.closeContentFrame('"
//                     + getContentFrameId(activityInstance) + "');");
         break;

      case LAUNCH_PANELS_ACTIVATED:
      case LAUNCH_PANELS_DEACTIVATED:
      case FULL_SCREENED:
      case RESTORED_TO_NORMAL:
      case PINNED:
      case PERSPECTIVE_CHANGED:
//         JavascriptContext.addJavascriptCall(FacesContext.getCurrentInstance(),
//               "InfinityBpm.ProcessPortal.resizeContentFrame('"
//                     + getContentFrameId(activityInstance) + "');");
         break;
      }
   }

   public String getEventScript(ActivityInstance activityInstance, ViewEvent event)
   {
      String eventScript = "";

      switch (event.getType())
      {
      case TO_BE_ACTIVATED:
         String uri = provideIframePanelUri(activityInstance, event.getView());

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

   public String getContextId(ActivityInstance ai)
   {
      return PredefinedConstants.JSF_CONTEXT;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return PanelIntegrationStrategy.EMBEDDED_IFRAME;
   }


   public String provideIframePanelUri(ActivityInstance ai, View view)
   {
      String uri = null;

      try
      {
         FacesContext jsfContext = FacesContext.getCurrentInstance();

         // must prefix root relative URIs with the current context root
         if (isIceFacesPanel(ai))
         {
            uri = jsfContext.getExternalContext().getRequestContextPath()
                  + providePanelUri(ai);
         }
         else
         {
            // TODO configure servlet mapping
            uri = jsfContext.getExternalContext().getRequestContextPath() + "/faces"
                  + VIEW_ID_NON_IFACE_FACELET_CONTAINER;
         }
      }
      catch (Exception e)
      {
         // not in JSF?
         trace.warn("Failed determining context root.", e);
      }

      if (StringUtils.isNotEmpty(uri))
      {
         uri += (-1 == uri.indexOf("?")) ? "?" : "&";
         uri += IframePanelConstants.QSTR_VIEW_URL + "=" + new String(Base64.encode(view.getUrl().getBytes()));
      }

      return uri;
   }

   public String providePanelUri(ActivityInstance ai)
   {
      // delegate to default controller
      return SpiUtils.DEFAULT_JSF_ACTIVITY_CONTROLLER.providePanelUri(ai);
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

         Interaction interaction = registry.getInteraction(Interaction.getInteractionId(ai));
         if (null == interaction)
         {
            interaction = new Interaction(ippSessionContext.getUser(),
                  modelCache.getModel(ai.getModelOID()), ai, getContextId(ai),
                  ippSessionContext.getServiceFactory());

            interaction.setInDataValues(inData);

            registry.registerInteraction(interaction);
         }

         Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
         sessionMap.put(IframePanelConstants.KEY_COMMAND,
               IframePanelConstants.CMD_IFRAME_PANEL_INITIALIZE);
         sessionMap.put(IframePanelConstants.KEY_INTERACTION_ID, interaction.getId());
         if ( !isIceFacesPanel(ai))
         {
            sessionMap.put(IframePanelConstants.KEY_VIEW_ID,
                  VIEW_ID_NON_IFACE_FACELET_CONTAINER);
         }
      }

      // TODO emit java script to load page into panel?
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();

      if ((ClosePanelScenario.COMPLETE == scenario)
            || (ClosePanelScenario.SUSPEND_AND_SAVE == scenario))
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
         if ( !isIceFacesPanel(ai))
         {
            sessionMap.put(IframePanelConstants.KEY_VIEW_ID,
                  VIEW_ID_NON_IFACE_FACELET_CONTAINER);
         }
         
         JavascriptContext.addJavascriptCall(facesContext,
               "parent.InfinityBpm.ProcessPortal.sendCloseCommandToExternalWebApp('"
                     + getContentFrameId(ai) + "', '" + scenario.getId() + "');");

         // close panel asynchronously after ICEfaces page responds via JavaScript
         return false;
      }
      else
      {
         // destroy interaction
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

   private static boolean isIceFacesPanel(ActivityInstance ai)
   {
      String panelUrl = SpiUtils.DEFAULT_JSF_ACTIVITY_CONTROLLER.providePanelUri(ai);

      return !isEmpty(panelUrl)
            && (panelUrl.endsWith(".iface") || panelUrl.contains(".iface?"));
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
