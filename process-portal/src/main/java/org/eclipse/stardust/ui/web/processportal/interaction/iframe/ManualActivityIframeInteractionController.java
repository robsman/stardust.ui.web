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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.event.ViewDataEvent;
import org.eclipse.stardust.ui.web.common.event.ViewDataEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;
import org.eclipse.stardust.ui.web.processportal.interaction.iframe.ManualActivityDocumentController.DOCUMENT;
import org.eclipse.stardust.ui.web.processportal.service.rest.DataException;
import org.eclipse.stardust.ui.web.processportal.service.rest.InteractionDataUtils;
import org.eclipse.stardust.ui.web.processportal.view.ActivityPanelConfigurationBean;
import org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController;
import org.eclipse.stardust.ui.web.processportal.view.manual.ManualActivityUi;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.ActivityInteractionControllerUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.controller.RemoteControlActivityStateChangeHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Subodh.Godbole
 *
 */
public class ManualActivityIframeInteractionController implements IActivityInteractionController, ViewEventAwareInteractionController, RemoteControlActivityStateChangeHandler
{
   private static final Logger trace = LogManager.getLogger(ManualActivityIframeInteractionController.class);
   
   public static final String PANEL_URI = "/plugins/processportal/manualActivityPanel.html";
   
   public static final String CHECKLIST_FACET = "checklist";
   public static final String CHECKLIST_FACET_URI = "/plugins/simple-modeler/checklistPanel.html";

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#providePanelUri(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public String providePanelUri(ActivityInstance ai)
   {
      HttpServletRequest req = FacesUtils.getHttpRequest();
      
      String contextUri = "/${request.contextPath}";
      contextUri = contextUri.replace("/${request.contextPath}", req.getContextPath());
      
      String panelUri = PANEL_URI;
      String manualActivityFacet = (String) ai.getActivity().getAttribute("stardust:model:manualActivityFacet");
      if(CHECKLIST_FACET.equals(manualActivityFacet))
      {
         panelUri = CHECKLIST_FACET_URI;
      }

      contextUri = contextUri + panelUri + "?interactionId=" + Interaction.getInteractionId(ai);
      
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
         
         Map<String, Serializable> configuration = new HashMap<String, Serializable>();
         configuration.put("layoutColumns", ActivityPanelConfigurationBean.getAutoNoOfColumnsInColumnLayout());
         configuration.put("tableColumns", ActivityPanelConfigurationBean.getAutoNoOfColumnsInTable());
         interaction.setConfiguration(configuration);

         registry.registerInteraction(interaction);
         
         //initialize Document Controllers
         DocumentHelper.initializeDocumentControllers(interaction, inData);
      }
   }



   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController#closePanel(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario)
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario, Object parameters)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();

      if (ActivityInteractionControllerUtils.isExternalWebAppInterventionRequired(scenario))
      {
         trace.info("Triggering asynchronous close of activity panel ...");

         InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(facesContext,
               InteractionRegistry.BEAN_ID);

         Interaction interaction = registry.getInteraction(Interaction.getInteractionId(ai));
         if ((null != interaction) && (Interaction.Status.Complete == interaction.getStatus()))
         {
            // out data mapping was already performed
            // Now validate data
            try
            {
               if (null != interaction.getOutDataValues())
               {
                  Map<String, Serializable> data = InteractionDataUtils.unmarshalData(interaction.getModel(),
                        interaction.getDefinition(), (Map)interaction.getOutDataValues(), interaction, null);
                  interaction.setOutDataValues(data);
               }
               return true;
            }
            catch (DataException e)
            {
               interaction.setStatus(Interaction.Status.Active);

               StringBuilder errors = new StringBuilder();
               String msg;
               for (Entry<String, Throwable> entry : e.getErrors().entrySet())
               {
                  trace.error(entry.getKey(), entry.getValue());

                  msg = entry.getValue().getMessage();
                  if (null == msg)
                  {
                     msg = entry.getValue().toString();
                  }
                  errors.append(entry.getKey()).append(" : ").append(msg).append("\n");
               }
               
               MessageDialog.addErrorMessage(errors.toString());
               return false;
            }
         }

         String paramsJson = "";
         if (null != parameters)
         {
            paramsJson = ", " + GsonUtils.stringify(parameters);
         }
         
         PortalApplication.getInstance().addEventScript(
               "parent.InfinityBpm.ProcessPortal.sendCloseCommandToExternalWebApp('" + getContentFrameId(ai) + "', '"
                     + scenario.getId() + "', false" + paramsJson + ");");

         // close panel asynchronously after iFrame page responds via JavaScript
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
   @SuppressWarnings("rawtypes")
   public Map getOutDataValues(ActivityInstance ai)
   {
      if (ai == null)
      {
         throw new InvalidArgumentException(
               BpmRuntimeError.BPMRT_NULL_ARGUMENT.raise("ActivityInstance ai"));
      }

      Map<String, Serializable> outData = null;
      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(InteractionRegistry.BEAN_ID);
      if (null != registry)
      {
         Interaction interaction = registry.getInteraction(getInteractionId(ai));
         if (null != interaction)
         {
            //convert Raw document to jcr document 
            trace.debug("converting file system documents to JCR documents. - started");
            DocumentHelper.transformDocuments(interaction);
            trace.debug("converting file system documents to JCR documents. - finished");
            outData = interaction.getOutDataValues();
         }
      }
      return outData;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController#getEventScript(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public String getEventScript(final ActivityInstance activityInstance, ViewEvent event)
   {
      String eventScript = "";
     
      switch (event.getType())
      {
      case CREATED:
         PortalApplication.getInstance().registerViewDataEventHandler(event.getView(), new ViewDataEventHandler()
         {
            public void handleEvent(ViewDataEvent event)
            {
               Interaction interaction = getInteraction(activityInstance);
               switch (event.getType())
               {
               case DATA_MODIFIED:

                  if (event.getPayload() instanceof Map && interaction != null)
                  {
                     @SuppressWarnings("unchecked")
                     Map<String, Object> result = (Map<String, Object>) event.getPayload();
                     DocumentHelper.updateDocuments((String) result.get(DOCUMENT.DOC_INTERACTION_ID),
                           (AbstractDocumentContentInfo) result.get("document"), interaction);
                  }
                  break;

               case VIEW_STATE_CHANGED:
                  Map<String, Object> result = (Map<String, Object>) event.getPayload();

                  if (event.getViewEvent() == null)
                  {
                     break;
                  }

                  Boolean opened = null;

                  if (ViewEventType.CLOSED.equals(event.getViewEvent().getType()))
                  {
                     opened = false;
                  }
                  else if (ViewEventType.TO_BE_ACTIVATED.equals(event.getViewEvent().getType()))
                  {
                     opened = true;
                  }
                  if (opened != null)
                  {
                     DocumentHelper.updateDocumentState((String) result.get(DOCUMENT.DOC_INTERACTION_ID), opened,
                           interaction);
                  }
                  break;

               default:
                  break;
               }
            }
         });
         break;
      case TO_BE_ACTIVATED:
         String uri = providePanelUri(activityInstance);
         
         if (null != event.getView().getParamValue("query")) {
            uri += "&activatedFromWorklist=true";
         }
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
         
      case POST_OPEN_LIFECYCLE:
         DocumentHelper.openMappedDocuments(activityInstance, event.getView());
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
    * 
    * @param activityInstance
    * @return
    */
   private Interaction getInteraction(ActivityInstance activityInstance)
   {
      FacesContext facesContext = FacesContext.getCurrentInstance();
      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(facesContext,
            InteractionRegistry.BEAN_ID);

      return registry.getInteraction(Interaction.getInteractionId(activityInstance));
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.processportal.view.ViewEventAwareInteractionController#handleEvent(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ActivityInstance ai, ViewEvent event)
   {      
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

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController
    * #isTypedDocumentOpen(org.eclipse.stardust.engine.api.runtime.ActivityInstance)
    */
   public boolean isTypedDocumentOpen(ActivityInstance activityInstance)
   {
      return DocumentHelper.isTypedDocumentOpen(getInteraction(activityInstance));
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.controller.RemoteControlActivityStateChangeHandler#handleCommand(org.eclipse.stardust.engine.api.runtime.ActivityInstance, org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario)
    */
   public void handleScenario(ActivityInstance ai, ClosePanelScenario scenario)
   {
      if (ActivityInteractionControllerUtils.isExternalWebAppInterventionRequired(scenario))
      {
         org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry registry = 
            (org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry) ManagedBeanUtils
               .getManagedBean(InteractionRegistry.BEAN_ID);

         org.eclipse.stardust.ui.web.processportal.interaction.Interaction interaction = registry
               .getInteraction(org.eclipse.stardust.ui.web.processportal.interaction.Interaction
                     .getInteractionId(ai));

         if (null != interaction)
         {
            interaction.setStatus(org.eclipse.stardust.ui.web.processportal.interaction.Interaction.Status.Complete);
         }
      }
   }
}
