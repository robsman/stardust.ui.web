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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
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
               servletContext);
         
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

      JsonObject jsonElem = (JsonObject)new JsonParser().parse(json);

      JsonObject ret = new JsonObject();

      try
      {
         Map<String, Serializable> data = InteractionDataUtils.unmarshalData(interaction.getModel(),
               interaction.getDefinition(), new JsonHelper().toObject(jsonElem), getInteraction(), servletContext);
         interaction.setOutDataValues(data);
      }
      catch (DataException e)
      {
         JsonObject errors = new JsonObject();
         ret.add("errors", errors);

         for (Entry<String, Throwable> entry : e.getErrors().entrySet())
         {
            errors.add(entry.getKey(), new JsonPrimitive(entry.getValue().getMessage()));
         }
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

      JsonObject jsonElem = (JsonObject)new JsonParser().parse(json);

      JsonObject ret = new JsonObject();

      try
      {
      Map<String, Serializable> data = InteractionDataUtils.unmarshalData(interaction.getModel(),
            interaction.getDefinition(), new JsonHelper().toObject(jsonElem), getInteraction(), servletContext);
   
      // This will have only one value, so loop will execute once only
      for (Entry<String, Serializable> entry : data.entrySet())
      {
         if(null == interaction.getOutDataValues())
         {
            interaction.setOutDataValues(new HashMap<String, Serializable>());
         }
         
         interaction.getOutDataValues().put(entry.getKey(), entry.getValue());
      }
   }
      catch (DataException e)
      {
         JsonObject errors = new JsonObject();
         ret.add("errors", errors);

         for (Entry<String, Throwable> entry : e.getErrors().entrySet())
         {
            errors.add(entry.getKey(), new JsonPrimitive(entry.getValue().getMessage()));
         }
      }

      return Response.ok(ret.toString(), MediaType.APPLICATION_JSON_TYPE).build();
   }

   @Produces(MediaType.TEXT_PLAIN)
   @Path("i18n")
   @GET
   public Response i18n()
   {
      StringBuffer data = new StringBuffer();

      MessagePropertiesBean messageBean = (MessagePropertiesBean) RestControllerUtils.resolveSpringBean(
            MessagePropertiesBean.class, servletContext);
      
      Model model = getInteraction().getModel();
      String bundleName = ModelElementUtils.getBundleName(model);
      if (StringUtils.isNotEmpty(bundleName))
      {
         try
         {
            // Client Messages Bundle
            readBundle(ResourceBundle.getBundle("processportal-client", messageBean.getLocaleObject()), data);
            
            // Get required values from other bundle
            ResourceBundle rb = ResourceBundle.getBundle("views-common-messages", messageBean.getLocaleObject());
            data.append("common.process.priority.options.1").append("=")
                  .append(rb.getString("common.process.priority.options.1")).append("\n");
            data.append("common.process.priority.options.0").append("=")
                  .append(rb.getString("common.process.priority.options.0")).append("\n");
            data.append("common.process.priority.options.-1").append("=")
                  .append(rb.getString("common.process.priority.options.-1")).append("\n");

            // Model Bundle
            readBundle(ResourceBundle.getBundle(bundleName, messageBean.getLocaleObject()), data);
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
   public Response dateFormats()
   {
      MessagePropertiesBean messageBean = (MessagePropertiesBean) RestControllerUtils.resolveSpringBean(
            MessagePropertiesBean.class, servletContext);

      JsonObject dates = new JsonObject();
      dates.add("dateFormat", new JsonPrimitive(messageBean.getString("portalFramework.formats.defaultDateFormat")));
      dates.add("dateTimeFormat", new JsonPrimitive(messageBean.getString("portalFramework.formats.defaultDateTimeFormat")));
      dates.add("timeFormat", new JsonPrimitive(messageBean.getString("portalFramework.formats.defaultTimeFormat")));

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
