/*******************************************************************************
 * Copyright (c) 2011, 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.common.controller;

import static java.util.Collections.emptyMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.engine.core.interactions.Interaction.getInteractionId;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.stardust.common.error.InvalidArgumentException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ApplicationContext;
import org.eclipse.stardust.engine.api.model.DataMapping;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.engine.core.interactions.InteractionRegistry;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ClosePanelScenario;
import org.eclipse.stardust.ui.web.viewscommon.common.PanelIntegrationStrategy;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IActivityInteractionController;
import org.eclipse.stardust.ui.web.viewscommon.utils.ClientSideDataFlowUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ManagedBeanUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;



/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class ExternalWebAppActivityInteractionController implements IActivityInteractionController
{

   private static final Logger trace = LogManager.getLogger(ExternalWebAppActivityInteractionController.class);

   public static final String EXT_WEB_APP_CONTEXT_ID = "externalWebApp";

   public static final String PARAM_INTERACTION_RESOURCE_URI = "ippInteractionUri";

   public static final String PARAM_PORTAL_BASE_URI = "ippPortalBaseUri";

   public static final String PARAM_SERVICES_BASE_URI = "ippServicesBaseUri";

   private Map<String, String> webAppBaseUris;

   public ExternalWebAppActivityInteractionController()
   {
      try
      {
         URL cfgFileUri = getClass().getClassLoader().getResource(
               "ipp-liberation.properties");

         Properties props = new Properties();
         props.load(cfgFileUri.openStream());

         this.webAppBaseUris = new HashMap<String, String>();
         for (Object key : props.keySet())
         {
            if (key instanceof String)
            {
               webAppBaseUris.put((String) key, props.getProperty((String) key));
            }
         }

         trace.info("Resolved URIs for external Web App to " + webAppBaseUris);
      }
      catch (Exception e)
      {
         trace.debug("Failed loading URI mapping for external Web App.");

         this.webAppBaseUris = emptyMap();
      }
   }

   public String getContextId(ActivityInstance ai)
   {
      return EXT_WEB_APP_CONTEXT_ID;
   }

   public PanelIntegrationStrategy getPanelIntegrationStrategy(ActivityInstance ai)
   {
      return PanelIntegrationStrategy.EMBEDDED_IFRAME;
   }

 /*  public String launchWorkshopActivity(ActivityInstance activityInstance,
         AbstractProcessExecutionPortal portal)
   {
      return Constants.WORKFLOW_LAUNCH_ACTIVITY_IF_ANY;
   }*/

   @SuppressWarnings("unchecked")
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

         Interaction interaction = new Interaction(ippSessionContext.getUser(),
               modelCache.getModel(ai.getModelOID()), ai, getContextId(ai),
               ippSessionContext.getServiceFactory());

         // performing client side IN mappings
         Map<String, Serializable> inParams = newHashMap();
         for (DataMapping inMapping : (List<DataMapping>) interaction.getDefinition().getAllInDataMappings())
         {
            Serializable inValue = (Serializable) inData.get(inMapping.getId());
            if (null != inValue)
            {
               try
               {
                  String paramId = inMapping.getApplicationAccessPoint().getId();

                  Object inParam = ClientSideDataFlowUtils.evaluateClientSideInMapping(
                        interaction.getModel(), inParams.get(paramId), inMapping, inValue);

                  inParams.put(paramId, (Serializable) inParam);
               }
               catch (Exception e)
               {
                  trace.warn("Failed evaluating client side of IN data mapping "
                        + inMapping.getId() + " on activity instance " + ai, e);
               }
            }
         }

         interaction.setInDataValues(inParams);

         registry.registerInteraction(interaction);
      }
   }

   public String providePanelUri(ActivityInstance ai)
   {
      ApplicationContext context = ai.getActivity().getApplicationContext(
            EXT_WEB_APP_CONTEXT_ID);

      FacesContext fc = FacesContext.getCurrentInstance();
      HttpServletRequest req = (HttpServletRequest) fc.getExternalContext().getRequest();
      
      // allow base URI override via parameter
      String servicesBaseUri = fc.getExternalContext().getInitParameter("InfinityBpm.ServicesBaseUri");
      if (isEmpty(servicesBaseUri))
      {
         servicesBaseUri = "${request.scheme}://${request.serverName}:${request.serverPort}/${request.contextPath}/services/";
      }

      servicesBaseUri = servicesBaseUri.replace("${request.scheme}", req.getScheme());
      servicesBaseUri = servicesBaseUri.replace("${request.serverName}", req.getServerName());
      servicesBaseUri = servicesBaseUri.replace("${request.serverPort}", Integer.toString(req.getServerPort()));
      servicesBaseUri = servicesBaseUri.replace("/${request.contextPath}", req.getContextPath());

      String portalBaseUri = fc.getExternalContext().getInitParameter("InfinityBpm.PortalBaseUri");
      if (isEmpty(portalBaseUri))
      {
         portalBaseUri = "${request.scheme}://${request.serverName}:${request.serverPort}/${request.contextPath}";
      }

      portalBaseUri = portalBaseUri.replace("${request.scheme}", req.getScheme());
      portalBaseUri = portalBaseUri.replace("${request.serverName}", req.getServerName());
      portalBaseUri = portalBaseUri.replace("${request.serverPort}", Integer.toString(req.getServerPort()));
      portalBaseUri = portalBaseUri.replace("/${request.contextPath}", req.getContextPath());

      String uri = "";
      Boolean embedded = (Boolean) context.getAttribute("carnot:engine:ui:externalWebApp:embedded");
      if (null != embedded && embedded)
      {
         uri = "${serviceBaseUrl}rest/engine/interactions/${interactionId}/embeddedMarkup";
         uri = uri.replace("${serviceBaseUrl}", servicesBaseUri);
         uri = uri.replace("${interactionId}", getInteractionId(ai));
      }
      else
      {
         uri = (String) context.getAttribute("carnot:engine:ui:externalWebApp:uri");
         if ((null != webAppBaseUris) && webAppBaseUris.containsKey(uri))
         {
            uri = webAppBaseUris.get(uri);

            trace.info("Overriding external Web App URI to " + uri);
         }
      }
      
      // Take out Hash if any to append at the end
      String uriHash = "";
      if (uri.contains("#"))
      {
         uriHash = uri.substring(uri.indexOf("#"));
         uri = uri.substring(0, uri.indexOf("#"));
      }

      StringBuilder uriBuilder = new StringBuilder();

      uriBuilder.append(uri) //
            .append(uri.contains("?") ? "&" : "?") //
            .append(PARAM_INTERACTION_RESOURCE_URI).append("=") //
            .append(servicesBaseUri) //
            .append("rest/engine/interactions/").append(getInteractionId(ai));

      uriBuilder.append("&") //
            .append(PARAM_PORTAL_BASE_URI).append("=").append(portalBaseUri);

      uriBuilder.append("&") //
            .append(PARAM_SERVICES_BASE_URI).append("=").append(servicesBaseUri);
      
      // Append Hash
      uriBuilder.append(uriHash);

      return fc.getExternalContext().encodeResourceURL(uriBuilder.toString());
   }

   public boolean closePanel(ActivityInstance ai, ClosePanelScenario scenario)
   {
      if ((ClosePanelScenario.SUSPEND == scenario) || (ClosePanelScenario.ABORT == scenario))
      {
         // no out data will be retrieved

         InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(InteractionRegistry.BEAN_ID);
         if (null != registry)
         {
            // destroy interaction resource
            registry.unregisterInteraction(getInteractionId(ai));
         }
      }

      return true;
   }

   @SuppressWarnings("unchecked")
   public Map getOutDataValues(ActivityInstance ai)
   {
      InteractionRegistry registry = (InteractionRegistry) ManagedBeanUtils.getManagedBean(InteractionRegistry.BEAN_ID);

      Map<String, Serializable> outData = null;

      if (null != registry)
      {
         // retrieve out data
         Interaction interaction = registry.getInteraction(getInteractionId(ai));
         if (null != interaction)
         {
            Map<String, Serializable> outParams = interaction.getOutDataValues();
            if (null != outParams)
            {
               // performing client side OUT mappings
               outData = newHashMap();
               for (DataMapping outMapping : (List<DataMapping>) interaction.getDefinition().getAllOutDataMappings())
               {
                  Serializable outParam = outParams.get(outMapping.getApplicationAccessPoint().getId());
                  if (null != outParam)
                  {
                     try
                     {
                        Object outValue = ClientSideDataFlowUtils.evaluateClientSideOutMapping(
                              interaction.getModel(), outParam, outMapping);

                        outData.put(outMapping.getId(), (Serializable) outValue);
                     }
                     catch (Exception e)
                     {
                        trace.warn("Failed evaluating client side of OUT data mapping "
                              + outMapping.getId() + " on activity instance " + ai, e);
                     }
                  }
                  else
                  {
                     trace.info("Missing value for data mapping " + outMapping.getId()
                           + " on activity instance " + ai);
                  }
               }
            }

            // destroy interaction resource
            registry.unregisterInteraction(interaction.getId());
         }
         else
         {
            trace.warn("Failed resolving interaction resource for activity instance " + ai);
         }
      }

      return outData;
   }

}
