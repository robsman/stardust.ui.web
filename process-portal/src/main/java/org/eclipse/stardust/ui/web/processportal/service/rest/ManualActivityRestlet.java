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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

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
   private InteractionRegistry getInteractionRegistry()
   {
      InteractionRegistry registry = RestControllerUtils.resolveSpringBean(InteractionRegistry.class, servletContext);

      if (null == registry)
      {
         throw new WebApplicationException(Status.NOT_FOUND);
      }

      return registry;
   }

   /**
    * @return
    */
   @Produces(MediaType.APPLICATION_JSON)
   @Path("dataMappings")
   @GET
   public Response dataMappings()
   {
      InteractionRegistry registry = getInteractionRegistry();
      Interaction interaction = registry.getInteraction(interactionId);

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
      InteractionRegistry registry = getInteractionRegistry();
      Interaction interaction = registry.getInteraction(interactionId);

      Map<String, ? extends Serializable> inData = interaction.getInDataValues();
      JsonObject root = new JsonObject();
      toJson(inData, root);
      
      return Response.ok(root.toString(), MediaType.APPLICATION_JSON_TYPE).build();
   }

   /*
    * 
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private void toJson(Map<String, ? extends Serializable> data, JsonObject parent)
   {
      for (Entry<String, ? extends Serializable> entry : data.entrySet())
      {
         if (null == entry.getValue())
         {
            continue;
         }
         
         if (entry.getValue() instanceof Map)
         {
            JsonObject json = new JsonObject();
            parent.add(entry.getKey(), json);
            toJson((Map)entry.getValue(), json);
         }
         else if (entry.getValue() instanceof List)
         {
            JsonArray json = new JsonArray();
            parent.add(entry.getKey(), json);
            toJson((List)entry.getValue(), json);
         }
         else // Primitive
         {
            JsonPrimitive primitive = toJson(entry.getValue());
            parent.add(entry.getKey(), primitive);
         }
      }
   }
   
   /*
    * 
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private void toJson(List<? extends Serializable> data, JsonArray parent)
   {
      for (Serializable value : data)
      {
         if (null == value)
         {
            continue;
         }

         if (value instanceof Map)
         {
            JsonObject json = new JsonObject();
            parent.add(json);
            toJson((Map)value, json);
         }
         else if (value instanceof List)
         {
            JsonArray json = new JsonArray();
            parent.add(json);
            toJson((List)value, json);
         }
         else // Primitive
         {
            JsonPrimitive primitive = toJson(value);
            if (null != primitive)
            {
               parent.add(primitive);
            }
         }
      }
   }

   /*
    * 
    */
   private JsonPrimitive toJson(Object value)
   {
      JsonPrimitive ret = null;

      try
      {
         if (value instanceof Float || value instanceof Double || value instanceof Number)
         {
            ret = new JsonPrimitive((Number)value);               
         }
         else if (value instanceof Boolean)
         {
            ret = new JsonPrimitive((Boolean)value);               
         }
         else if (value instanceof Character)
         {
            ret = new JsonPrimitive((Character)value);               
         }
         else if (value instanceof Date)
         {
            ret = new JsonPrimitive(((Date)value).toString());
         }
         else if (value instanceof Calendar)
         {
            ret = new JsonPrimitive(((Calendar)value).getTime().toString());
         }
         else
         {
            ret = new JsonPrimitive((String)value);
         }
      }
      catch (Exception e)
      {
         trace.warn("Unsupported Data Type: " + value.getClass().getName(), e);
      }
      
      return ret;
   }
}
