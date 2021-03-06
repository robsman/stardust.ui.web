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

package org.eclipse.stardust.ui.web.processportal.service.rest;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelElementUtils;

/**
 * @author Subodh.Godbole
 *
 */
@Path("/manualActivity/{interactionId}")
public class ManualActivityRestlet
{
   private static final Logger trace = LogManager.getLogger(ManualActivityRestlet.class);

   @PathParam("interactionId")
   protected String interactionId;

   @Context
   protected ServletContext servletContext;

   /**
    * @return
    */
   @Produces(MediaType.APPLICATION_JSON)
   @Path("dataMappings")
   @GET
   public Response dataMappings()
   {
      Interaction interaction = getInteraction();

      String dataMappingsJson = interaction.getManualActivityPath().toJsonString();

      return Response.ok(dataMappingsJson, MediaType.APPLICATION_JSON_TYPE).build();
   }

   /**
    * @return
    */
   @Produces(MediaType.APPLICATION_JSON)
   @Path("inData")
   @GET
   public Response inData()
   {
      Interaction interaction = getInteraction();

      if (interaction != null)
      {
         Map<String, ? extends Serializable> inData = interaction.getInDataValues();
         
         JsonObject root = InteractionDataUtils.marshalData(interaction, inData,
               servletContext, false);
         
         return Response.ok(root.toString(), MediaType.APPLICATION_JSON_TYPE).build();
      }
      else
      {
         trace.error("Interaction is null for interaction Id: " + interactionId);
      }

      return Response.ok("[]", MediaType.APPLICATION_JSON_TYPE).build();
   }

   /**
    * @return
    */
   @Produces(MediaType.APPLICATION_JSON)
   @Path("inData/{parameterId}")
   @GET
   public Response inData(@PathParam("parameterId") String parameterId)
   {
      Interaction interaction = getInteraction();

      if (interaction != null)
      {
         Map<String, ? extends Serializable> inData = interaction.getInDataValues();
         Map<String, Serializable> filteredInData = CollectionUtils.newHashMap();
         filteredInData.put(parameterId, inData.get(parameterId));
         
         JsonObject root = InteractionDataUtils.marshalData(interaction, filteredInData,
               servletContext, true);
         
         return Response.ok(root.toString(), MediaType.APPLICATION_JSON_TYPE).build();
      }
      else
      {
         trace.error("Interaction is null for interaction Id: " + interactionId);
      }

      return Response.ok("[]", MediaType.APPLICATION_JSON_TYPE).build();
   }
   
   
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("outData")
   @POST
   public Response outData(String json)
   {
      Interaction interaction = getInteraction();

      JsonObject ret = new JsonObject();

      try
      {
         JsonObject jsonElem = (JsonObject)new JsonParser().parse(json);
         Map<String, Object> jsonData = new JsonHelper().toObject(jsonElem);

         Map<String, Serializable> data = new HashMap<String, Serializable>();
         for (Entry<String, Object> entry : jsonData.entrySet())
         {
            if (entry.getValue() instanceof Serializable || entry.getValue() == null)
            {
               data.put(entry.getKey(), (Serializable)entry.getValue());
            }
         }

         interaction.setOutDataValues(data);
      }
      catch (Exception e)
      {
         JsonObject errors = new JsonObject();
         ret.add("errors", errors);

         String msg = e.getMessage();
         if (null == msg)
         {
            msg = e.toString();
         }
         errors.add("", new JsonPrimitive(msg));
      }
      
      return Response.ok(ret.toString(), MediaType.APPLICATION_JSON_TYPE).build();
   }

   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("outData/{parameterId}")
   @POST
   public Response outData(@PathParam("parameterId") String parameterId, String json)
   {
      Interaction interaction = getInteraction();

      JsonObject jsonElem = (JsonObject) new JsonParser().parse(json);

      JsonObject ret = new JsonObject();

      try
      {
         Map<String, Object> jsonData = new JsonHelper().toObject(jsonElem);

         for (Entry<String, Object> entry : jsonData.entrySet())
         {
            if (null == interaction.getOutDataValues())
            {
               interaction.setOutDataValues(new HashMap<String, Serializable>());
            }

            interaction.getOutDataValues().put(entry.getKey(), (Serializable) entry.getValue());
         }
      }
      catch (Exception e)
      {
         JsonObject errors = new JsonObject();
         ret.add("errors", errors);

         String msg = e.getMessage();
         if (null == msg)
         {
            msg = e.toString();
         }
         errors.add("", new JsonPrimitive(msg));
      }

      return Response.ok(ret.toString(), MediaType.APPLICATION_JSON_TYPE).build();
   }

   @Produces(MediaType.TEXT_PLAIN)
   @Path("i18n")
   @GET
   public Response i18n(@Context HttpServletRequest request)
   {
      StringBuffer data = new StringBuffer();

      Model model = getInteraction().getModel();
      String bundleName = ModelElementUtils.getBundleName(model);
      if (StringUtils.isNotEmpty(bundleName))
      {
         try
         {
            // Client Messages Bundle
            readBundle(ResourceBundle.getBundle("processportal-client", request.getLocale()), data);
            
            // Get required values from other bundle
            ResourceBundle rb = ResourceBundle.getBundle("views-common-messages", request.getLocale());
            data.append("common.process.priority.options.1").append("=")
                  .append(getStringFromResourceBundle(rb, "common.process.priority.options.1")).append("\n");
            data.append("common.process.priority.options.0").append("=")
                  .append(getStringFromResourceBundle(rb, "common.process.priority.options.0")).append("\n");
            data.append("common.process.priority.options.-1").append("=")
                  .append(getStringFromResourceBundle(rb, "common.process.priority.options.-1")).append("\n");

            // Model Bundle
            readBundle(ResourceBundle.getBundle(bundleName, request.getLocale()), data);
            }
         catch (Exception e)
         {
            if (trace.isDebugEnabled())
            {               
               trace.debug("No resource bundle found for model with ID '" + model.getId() + "'.");
            }
         }
      }

      return Response.ok(data.toString(), MediaType.TEXT_PLAIN_TYPE).build();
   }

   @Produces(MediaType.APPLICATION_JSON)
   @Path("dateFormats")
   @GET
   public Response dateFormats(@Context HttpServletRequest request)
   {
      ResourceBundle rb = ResourceBundle.getBundle("portal-common-messages", request.getLocale());
      JsonObject dates = new JsonObject();
      dates.add("dateFormat", new JsonPrimitive(getStringFromResourceBundle(rb, "portalFramework.formats.defaultDateFormat")));
      dates.add("dateTimeFormat", new JsonPrimitive(getStringFromResourceBundle(rb, "portalFramework.formats.defaultDateTimeFormat")));
      dates.add("timeFormat", new JsonPrimitive(getStringFromResourceBundle(rb, "portalFramework.formats.defaultTimeFormat")));

      return Response.ok(dates.toString(), MediaType.APPLICATION_JSON_TYPE).build();
   }

   @Produces(MediaType.APPLICATION_JSON)
   @Path("configuration")
   @GET
   public Response configuration()
   {
      Interaction interaction = getInteraction();

      JsonObject conf = new JsonObject();
      new JsonHelper().toJson(interaction.getConfiguration(), conf);
      
      return Response.ok(conf.toString(), MediaType.APPLICATION_JSON_TYPE).build();
   }

   /**
    * @param bundle
    * @param data
    */
   private void readBundle(ResourceBundle bundle, StringBuffer data)
   {
      String key;
      Enumeration<String> keys = bundle.getKeys();
      while (keys.hasMoreElements())
      {
         key = keys.nextElement();
         data.append(key).append("=").append(bundle.getString(key)).append("\n");
      }
   }

   /**
    * @param bundle
    * @param data
    * 
    * TODO - this can move to some utility class
    */
   private String getStringFromResourceBundle(ResourceBundle bundle, String key)
   {
      try
      {
         return bundle.getString((String) key);
      }
      catch (Exception x)
      {
         return "%" + key + "%";
      }
   }
   
   /**
    * @return
    */
   private Interaction getInteraction()
   {
      InteractionRegistry registry = getInteractionRegistry();
      Interaction interaction = registry.getInteraction(interactionId);
      return interaction;
   }

   /**
    * @return
    */
   private InteractionRegistry getInteractionRegistry()
   {
      InteractionRegistry registry = RestControllerUtils.resolveSpringBean(InteractionRegistry.class, servletContext);

      if (null == registry)
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return registry;
   }
}
