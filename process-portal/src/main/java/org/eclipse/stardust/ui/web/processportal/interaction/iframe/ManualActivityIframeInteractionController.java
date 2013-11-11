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

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;
import org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController;
import org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityUi;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

/**
 * @author Subodh.Godbole
 *
 */
public class ManualActivityIframeInteractionController implements IActivityInteractionController, ViewEventAwareInteractionController
{
   public static final String PANEL_URI = "/plugins/processportal/manualActivityPanel.html";

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#providePanelUri(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public String providePanelUri(ActivityInstance ai)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
      
      String contextUri = "${request.scheme}://${request.serverName}:${request.serverPort}/${request.contextPath}";
      contextUri = contextUri.replace("${request.scheme}", req.getScheme());
      contextUri = contextUri.replace("${request.serverName}", req.getServerName());
      contextUri = contextUri.replace("${request.serverPort}", Integer.toString(req.getServerPort()));
      contextUri = contextUri.replace("/${request.contextPath}", req.getContextPath());
      
      contextUri = contextUri + PANEL_URI + "?interactionId=" + Interaction.getInteractionId(ai);
      
      return contextUri;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#initializePanel(org.eclipse.stardust.engine.api.runtime.ActivityInstance, java.util.Map)
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void initializePanel(ActivityInstance ai, Map inData)
   {
      if (ai == null)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("ActivityInstance ai"));
      }

      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(InteractionRegistry.BEAN_ID);
      if (null != registry)
      {
         ModelCache modelCache = ModelCache.findModelCache();
         SessionContext ippSessionContext = SessionContext.findSessionContext();

         Interaction interaction = new Interaction(modelCache.getModel(ai.getModelOID()), ai, getContextId(ai),
               ippSessionContext.getServiceFactory());

         ManualActivityUi manualActivityUi = new ManualActivityUi(ai, ai.getActivity().getApplicationContext("default"));
         interaction.setManualActivityPath(manualActivityUi.getManualActivityPath());
         interaction.setInDataValues(inData);

         registry.registerInteraction(interaction);
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#closePanel(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario)
    */
   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      if ((ClosePanelScenario.SUSPEND == scenario) || (ClosePanelScenario.ABORT == scenario))
      {
         InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(InteractionRegistry.BEAN_ID);
         if (null != registry)
         {
            // destroy interaction resource
            registry.unregisterInteraction(Interaction.getInteractionId(ai));
         }
      }

      return true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#getContextId(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public String getContextId(ActivityInstance ai)
   {
      return PredefinedConstants.DEFAULT_CONTEXT;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#getPanelIntegrationStrategy(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return PanelIntegrationStrategy.EMBEDDED_IFRAME;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#getOutDataValues(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public Map getOutDataValues(ActivityInstance ai)
   {
      return null;
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController#handleEvent(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ActivityInstance ai, ViewEvent event)
   {      
   }
}
