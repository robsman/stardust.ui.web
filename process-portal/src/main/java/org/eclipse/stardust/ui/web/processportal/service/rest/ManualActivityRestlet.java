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
import java.util.Map;

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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.processportal.interaction.Interaction;
import org.eclipse.stardust.ui.web.processportal.interaction.InteractionRegistry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
      new JsonHelper().toJson(inData, root);
      
      return Response.ok(root.toString(), MediaType.APPLICATION_JSON_TYPE).build();
   }

   @Consumes(MediaType.APPLICATION_JSON)
   @Path("outData")
   @POST
   public void outData(String json)
   {
      InteractionRegistry registry = getInteractionRegistry();
      Interaction interaction = registry.getInteraction(interactionId);

      JsonObject jsonElem = (JsonObject)new JsonParser().parse(json);

      Map<String, Serializable> data = InteractionDataUtils.unmarshalData(interaction.getModel(),
            interaction.getDefinition(), new JsonHelper().toObject(jsonElem));

      interaction.setOutDataValues(data);
   }

   @Consumes(MediaType.APPLICATION_JSON)
   @Path("outData/{parameterId}")
   @POST
   public void outData(@PathParam("parameterId") String parameterId, String value)
   {
      trace.info("ParameterId: " + parameterId + " : " + value);
   }
}
