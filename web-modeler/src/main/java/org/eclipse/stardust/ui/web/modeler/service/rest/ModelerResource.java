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

package org.eclipse.stardust.ui.web.modeler.service.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.core.interactions.Interaction;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.modeler.common.LanguageUtil;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ClientModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.rest.drl.DrlParser;

@Path("/modeler/{randomPostFix}")
public class ModelerResource
{
   private static final Logger trace = LogManager.getLogger(ModelerResource.class);

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   private ModelService modelService;

   // TODO static because of issues with session binding
   private static JsonObject interactionDataObject;

   @Context
   private HttpServletRequest httpRequest;

   // TODO to join session, concurrent hashmap
   @PathParam("modellingSession")
   private String sessionId;

   private long id = System.currentTimeMillis();

   @Context
   private ServletContext servletContext;

   public ModelService getModelService()
   {
      ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

      return (ModelService) context.getBean("modelService");
   }

   public void setModelService(ModelService modelService)
   {
      this.modelService = modelService;
   }

   private synchronized String getNextId()
   {
      return "a" + id++ ;
   }

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path("uniqueid")
   public Response getUniqueId()
   {
      try
      {
         return Response.ok(getNextId(), MediaType.TEXT_PLAIN_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   /**
    * @deprecated
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("users/submitChatMessage")
   public Response submitChatMessage(String postedData)
   {
      return Response.ok(postedData, MediaType.APPLICATION_JSON_TYPE).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("preferences")
   public Response getPreferences()
   {
      try
      {
         return Response.ok(getModelService().getPreferences().toString(),
               MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models")
   public Response getAllModels(@QueryParam("reload") @DefaultValue("true") boolean reload)
   {
      try
      {
         // TODO - currently always forces a reload - getAllModels(true)
         // we may need to make it conditional
         String result = getModelService().getAllModels(reload);
         return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   @Path("models/{modelId}/download")
   public Response downloadModel(@PathParam("modelId") String modelId)
   {
      byte[] docStream = getModelService().getModelFile(modelId);

      return Response.ok(docStream, MediaType.APPLICATION_OCTET_STREAM)
            .header(
                  "content-disposition",
                  "attachment; filename = \""
                        + getModelService().getModelFileName(modelId) + "\"")
            .build();
   }

   /**
    * Used to push models loaded on the client (e.g. from Orion) into a server-side cache.
    * Switches the model management strategy to <code>clientModelManagementStrategy</code>
    * .
    *
    * @param modelId
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.TEXT_PLAIN)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/upload/{fileName}")
   public Response uploadModel(@PathParam("fileName") String fileName, String postedData)
   {
      try
      {
         // Ensures that the Model Management Strategy is clientModelManagementStrategy

         ClientModelManagementStrategy modelManagementStrategy = getClientModelManagementStrategy();

         JsonObject modelDescriptorJson = modelManagementStrategy.addModelFile(fileName,
               postedData);

         getModelService().setModelManagementStrategy(modelManagementStrategy);

         return Response.ok(modelDescriptorJson.toString(), APPLICATION_JSON_TYPE)
               .build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   /**
    * Temporary, auxiliary service to load a text file. Used to provide access to Orion
    * files from this URL domain.
    *
    * @param path
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.TEXT_PLAIN)
   @Path("file")
   public Response loadFile(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(readFile(json.get("path").getAsString()),
               MediaType.TEXT_PLAIN).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   /**
    * Used by the auxiliary method above only.
    *
    * @param path
    * @return
    */
   private static String readFile(String path)
   {
      try
      {
         String content = new Scanner(new File(path)).useDelimiter("\\Z").next();

         return content;
      }
      catch (FileNotFoundException e)
      {
         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/process/{processId}/loadModel")
   public Response loadModel(@PathParam("modelId") String modelId,
         @PathParam("processId") String processId)
   {
      try
      {
         String result = getModelService().loadProcessDiagram(modelId, processId);
         return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/processes/{processId}/diagrams/{diagramId}")
   public Response updateDiagram(@PathParam("modelId") String modelId,
         @PathParam("processId") String processId,
         @PathParam("diagramId") String diagramId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         String result = getModelService().updateProcessDiagram(modelId, processId,
               diagramId, json);
         return Response.ok(result, APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/processes/{processId}/activities/{activityId}/rename")
   public Response renameActivity(@PathParam("modelId") String modelId,
         @PathParam("processId") String processId,
         @PathParam("activityId") String activityId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         String result = getModelService().renameActivity(modelId, processId, activityId,
               json);

         return Response.ok(result, APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Path("models/{modelId}")
   public Response saveModel(@PathParam("modelId") String modelId)
   {
      try
      {
         getModelService().saveModel(modelId);

         ResponseBuilder response = Response.ok("Saved");

         return response.build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/whoAmI")
   public Response whoAmI()
   {
      try
      {
         String result = getModelService().getLoggedInUser(servletContext);
         return Response.ok(result, APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("users/updateOwner")
   public Response updateOwner(String postedData)
   {
      try
      {
         JsonObject userJson = jsonIo.readJsonObject(postedData);
         String sessionID = userJson.getAsJsonObject("oldObject")
               .get("sessionId")
               .getAsString();
         String result = getModelService().getSessionOwner(sessionID);
         return Response.ok(result, APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("users/getAllProspects")
   public Response getAllProspects(String postedData)
   {
      try
      {
         JsonObject userJson = jsonIo.readJsonObject(postedData);
         String result = getModelService().getAllProspects(
               userJson.getAsJsonObject("oldObject").get("account").getAsString());
         return Response.ok(result, APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("users/getAllCollaborators")
   public Response getAllCollaborators(String postedData)
   {
      try
      {
         JsonObject userJson = new JsonMarshaller().readJsonObject(postedData);
         // utlity methode gson utils
         String result = getModelService().getAllCollaborators(
               userJson.getAsJsonObject("oldObject").get("account").getAsString());

         return Response.ok(result, APPLICATION_JSON_TYPE).build();

      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @GET
   @Path("models/save")
   public Response saveAllModels()
   {
      try
      {
         getModelService().saveAllModels();

         ResponseBuilder response = Response.ok("Saved");

         return response.build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/processes/{processId}/dataSymbols")
   public Response dropDataSymbol(@PathParam("modelId") String modelId,
         @PathParam("processId") String processId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         String result = getModelService().dropDataSymbol(modelId, processId, json);
         return Response.ok(result, APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/getModelingHelp")
   public Response getModelingHelp(String postedData)
   {
      String result = jsonIo.writeJsonObject(new JsonObject());
      return Response.ok(result, APPLICATION_JSON_TYPE).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("typeDeclarations/loadFromUrl")
   public Response loadStructuredDataTypeFromUrl(@PathParam("modelId") String modelId,
         String postedData)
   {
      try
      {
         return Response.ok(
               getModelService().getXsdStructure(jsonIo.readJsonObject(postedData))
                     .toString(), APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.TEXT_HTML)
   @Path("models/{modelId}/embeddedWebApplication/{applicationId}")
   public Response getProblems(@PathParam("modelId") String modelId,
         @PathParam("applicationId") String applicationId)
   {
      try
      {
         return Response.ok(
               getModelService().retrieveEmbeddedExternalWebApplicationMarkup(modelId,
                     applicationId), MediaType.TEXT_HTML_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{id}/processes/createWrapperProcess")
   public Response createWrapperProcess(@PathParam("id") String modelId, String postedData)
   {
      try
      {
         getModelService().createWrapperProcess(modelId,
               jsonIo.readJsonObject(postedData));
         return Response.ok("", APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{id}/processes/createProcessInterfaceTestWrapperProcess")
   public Response createProcessInterfaceTestWrapperProcess(@PathParam("id") String modelId, String postedData)
   {
      try
      {
         getModelService().createProcessInterfaceTestWrapperProcess(modelId,
               jsonIo.readJsonObject(postedData));
         return Response.ok("", APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/problems")
   public Response getProblems(@PathParam("modelId") String modelId)
   {
      try
      {
         return Response.ok(getModelService().validateModel(modelId).toString(),
               APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/configurationVariables")
   public Response getConfigurationVariables(@PathParam("modelId") String modelId)
   {
      try
      {
         return Response.ok(
               getModelService().getConfigurationVariables(modelId).toString(),
               APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/configurationVariables/{variableName}")
   public Response updateConfigurationVariable(@PathParam("modelId") String modelId,
         String postedData)
   {
      try
      {
         return Response.ok(
               getModelService().updateConfigurationVariable(modelId,
                     jsonIo.readJsonObject(postedData)).toString(), APPLICATION_JSON_TYPE)
               .build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/configurationVariables/{variableName}")
   public Response deleteConfigurationVariable(@PathParam("modelId") String modelId,
         @PathParam("variableName") String variableName, String postedData)
   {
      try
      {
         System.out.println("Delete parameter: " + postedData);

         variableName = URLDecoder.decode(variableName);
         JsonObject json = jsonIo.readJsonObject(postedData);
         getModelService().deleteConfigurationVariable(modelId, variableName, json);

         return Response.ok(
               getModelService().getConfigurationVariables(modelId).toString(),
               APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("models/{modelId}/processes/{processId}/decorations/{decorationId}")
   public Response getDecoration(@PathParam("modelId") String modelId,
         @PathParam("processId") String processId,
         @PathParam("decorationId") String decorationId, String postedData)
   {
      try
      {
         trace.debug("Retrieve decoration " + decorationId);

         JsonObject decorations = new JsonObject();

         final JsonObject defaultDecoration = new JsonObject();
         decorations.add("default", defaultDecoration);

         JsonObject processInstanceProgressDecoration = new JsonObject();
         decorations.add("processInstanceProgress", processInstanceProgressDecoration);

         JsonArray elements = new JsonArray();
         processInstanceProgressDecoration.add("elements", elements);

         // Event

         JsonObject element = new JsonObject();
         elements.add(element);

         element.addProperty("oid", 23);

         JsonObject graphicsDecoration = new JsonObject();
         element.add("graphicsDecoration", graphicsDecoration);

         JsonObject primitive = new JsonObject();
         graphicsDecoration.add("circle", primitive);

         primitive.addProperty("stroke", "green");
         primitive.addProperty("stroke-width", 2.0);

         // Connection

         element = new JsonObject();
         elements.add(element);

         element.addProperty("oid", 24);

         graphicsDecoration = new JsonObject();
         element.add("graphicsDecoration", graphicsDecoration);

         primitive = new JsonObject();

         graphicsDecoration.add("path", primitive);

         primitive.addProperty("stroke", "green");
         primitive.addProperty("stroke-width", 2.0);

         // Activity

         element = new JsonObject();
         elements.add(element);

         element.addProperty("id", "ACTIVITY_1");

         graphicsDecoration = new JsonObject();
         element.add("graphicsDecoration", graphicsDecoration);

         primitive = new JsonObject();
         graphicsDecoration.add("rectangle", primitive);

         primitive.addProperty("stroke", "green");
         primitive.addProperty("stroke-width", 2.0);

         // Connection

         element = new JsonObject();
         elements.add(element);

         element.addProperty("oid", 28);

         graphicsDecoration = new JsonObject();
         element.add("graphicsDecoration", graphicsDecoration);

         primitive = new JsonObject();
         graphicsDecoration.add("path", primitive);

         primitive.addProperty("stroke", "green");
         primitive.addProperty("stroke-width", 2.0);

         // Activity

         element = new JsonObject();
         elements.add(element);

         element.addProperty("id", "ACTIVITY_2");

         graphicsDecoration = new JsonObject();
         element.add("graphicsDecoration", graphicsDecoration);

         primitive = new JsonObject();
         graphicsDecoration.add("rectangle", primitive);

         primitive.addProperty("stroke", "yellow");
         primitive.addProperty("stroke-width", 2.0);

         // Decoration KPI

         JsonObject dashboardDecoration = new JsonObject();
         decorations.add("processInstanceDashboard", dashboardDecoration);

         elements = new JsonArray();
         dashboardDecoration.add("elements", elements);

         // Activity 1

         element = new JsonObject();
         elements.add(element);

         element.addProperty("id", "ACTIVITY_1");

         JsonArray dashboardContent = new JsonArray();
         element.add("dashboardContent", dashboardContent);

         JsonObject contentItem = new JsonObject();
         dashboardContent.add(contentItem);

         contentItem.addProperty("type", "valueList");
         contentItem.addProperty("title", "Basic Performance Indicators");

         final JsonObject attributes = new JsonObject();
         contentItem.add("attributes", attributes);

         attributes.addProperty("Average Execution Time", "10.1 Min");
         attributes.addProperty("Execution Time Deviation", "1.0 Min");
         attributes.addProperty("Rejected in QA", "22");

         // Activity 1

         element = new JsonObject();
         elements.add(element);

         element.addProperty("id", "ACTIVITY_2");

         dashboardContent = new JsonArray();
         element.add("dashboardContent", dashboardContent);

         contentItem = new JsonObject();
         dashboardContent.add(contentItem);

         contentItem.addProperty("type", "plot");
         contentItem.addProperty("title", "Monthly Development");

         final JsonArray data = new JsonArray();
         contentItem.add("data", data);

         int[][] values = {
               {0, 2}, {5, 6}, {10, 10}, {15, 20}, {20, 17}, {25, 5}, {30, 30}, {35, 40},
               {40, 45}, {45, 48}, {50, 50}};

         for (int n = 0; n < values.length; ++n)
         {
            final JsonArray point = new JsonArray();
            data.add(point);

            point.add(new JsonPrimitive(values[n][0]));
            point.add(new JsonPrimitive(values[n][1]));
         }

         return Response.ok(decorations.get(decorationId).toString(),
               APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("webServices/structure")
   public Response getWebServiceOptions(String postedData)
   {
      try
      {
         return Response.ok(
               getModelService().getWebServiceStructure(jsonIo.readJsonObject(postedData))
                     .toString(), APPLICATION_JSON_TYPE)
               .build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   /**
    *
    * @return
    */
   private void clearInteractionDataObject()
   {
      interactionDataObject = new JsonObject();
   }

   /**
    *
    * @return
    */
   private JsonObject getInteractionDataObject()
   {
      if (interactionDataObject == null)
      {
         interactionDataObject = new JsonObject();
      }

      return interactionDataObject;
   }

   // Callbacks for Interaction REST API

   // @Path("interactions/{interactionId}/definition")
   // @GET
   // @Produces(MediaType.APPLICATION_XML)
   // public InteractionDefinition getDefinition()
   // {
   // }

   // @Path("interactions/{interactionId}/inData/{parameterId}")
   // @GET
   // @Produces( {
   // MediaType.TEXT_PLAIN, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
   // public Response getInDataValueRepresentation(
   // @PathParam("interactionId") String interactionId,
   // @PathParam("parameterId") String parameterId)
   // {
   // try
   // {
   // System.out.println("Retrieving interaction input parameter " + parameterId);
   //
   // if (getInteractionDataObject().get("input").getAsJsonObject().has(parameterId))
   // {
   // System.out.println(getInteractionDataObject().get("input").getAsJsonObject().get(parameterId));
   //
   // return
   // Response.ok(getInteractionDataObject().get("input").getAsJsonObject().get(parameterId).toString(),
   // APPLICATION_JSON_TYPE)
   // .build();
   // }
   // else
   // {
   // return Response.ok(null, APPLICATION_JSON_TYPE)
   // .build();
   // }
   // }
   // catch (Exception e)
   // {
   // e.printStackTrace();
   //
   // throw new RuntimeException(e);
   // }
   // }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("interactions/{interactionId}/inData")
   public Response getInteractionInputData(
         @PathParam("interactionId") String interactionId)
   {
      try
      {
         return Response.ok(getInteractionDataObject().toString(), APPLICATION_JSON_TYPE)
               .build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("interactions/{interactionId}/inData")
   /**
    * This method is not part of the Interaction Protocol, but used to prepare the server-side state to test
    * the protocol.
    */
   public Response setInteractionInputData(
         @PathParam("interactionId") String interactionId, String postedData)
   {
      try
      {
         clearInteractionDataObject();

         JsonObject postedObject = jsonIo.readJsonObject(postedData);

         if (postedObject != null)
         {
            for (Map.Entry<String, ? > entry : postedObject.entrySet())
            {
               String key = entry.getKey();
               JsonElement value = postedObject.get(key);

               getInteractionDataObject().add(key, value);
            }
         }

         return Response.ok(getInteractionDataObject().toString(), APPLICATION_JSON_TYPE)
               .build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("interactions/{interactionId}/outData")
   public Response setInteractionOutputData(
         @PathParam("interactionId") String interactionId, String postedData)
   {
      try
      {
         JsonObject postedObject = jsonIo.readJsonObject(postedData);

         getInteractionDataObject().add("output", postedObject);

         return Response.ok(getInteractionDataObject().toString(), APPLICATION_JSON_TYPE)
               .build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("interactions/{interactionId}/outData")
   /**
    * This method is not part of the Interaction Protocol, but used to retrieve output data submitted via the protocol from the
    * server-side state to display those.
    */
   public Response getInteractionOutputData(
         @PathParam("interactionId") String interactionId)
   {
      try
      {
         System.out.println("Retrieving interaction output:");
         System.out.println(getInteractionDataObject());

         return Response.ok(getInteractionDataObject().get("output").toString(),
               APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path("/language")
   public Response getLanguage()
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-language"),
            ",");
      if (tok.hasMoreTokens())
      {
         return Response.ok(LanguageUtil.getLocale(tok.nextToken()),
               MediaType.TEXT_PLAIN_TYPE).build();
      }
      return Response.ok("en", MediaType.TEXT_PLAIN_TYPE).build();
   }

   /**
    * @param bundleName
    * @param locale
    * @return
    */
   @GET
   @Path("/{bundleName}/{locale}")
   public Response getRetrieve(@PathParam("bundleName") String bundleName,
         @PathParam("locale") String locale)
   {
      final String POST_FIX = "client-messages";

      if (StringUtils.isNotEmpty(bundleName) && bundleName.endsWith(POST_FIX))
      {
         try
         {
            StringBuffer bundleData = new StringBuffer();
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName,
                  LanguageUtil.getLocaleObject(locale));

            String key;
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements())
            {
               key = keys.nextElement();
               bundleData.append(key)
                     .append("=")
                     .append(bundle.getString(key))
                     .append("\n");
            }

            return Response.ok(bundleData.toString(), MediaType.TEXT_PLAIN_TYPE).build();
         }
         catch (MissingResourceException mre)
         {
            return Response.status(Status.NOT_FOUND).build();
         }
         catch (Exception e)
         {
            return Response.status(Status.BAD_REQUEST).build();
         }
      }
      else
      {
         return Response.status(Status.FORBIDDEN).build();
      }
   }

   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("upload")
   /**
    * Temporary - used as an upload bridge for Rules Manager
    */
   public Response uploadFile(String postedData)
   {
      trace.debug("Posted Data:");
      trace.debug(postedData);

      BufferedReader reader = new BufferedReader(new StringReader(postedData));

      try
      {
         // Read header

         reader.readLine(); // ------WebKitFormBoundary ...
         reader.readLine(); // Content-Disposition: ...
         reader.readLine(); // Content-Type: ...

         String sectionsLine = reader.readLine();

         while ((sectionsLine = reader.readLine()) != null)
         {
            if (sectionsLine.trim().length() != 0)
            {
               break;
            }
         }

         String factsLine = reader.readLine();
         String propertiesLine = reader.readLine();

         trace.debug("Sections Line" + sectionsLine);
         trace.debug("Facts Line" + factsLine);
         trace.debug("Properties Line" + propertiesLine);

         String[] sections = sectionsLine.split(";");
         String[] facts = factsLine.split(";");
         String[] properties = propertiesLine.split(";");
         List<String> sectionNames = new ArrayList<String>();
         List<String> factNames = new ArrayList<String>();
         List<String> propertyNames = new ArrayList<String>();
         String currentSectionName = null;
         String currentFactName = null;
         JsonObject returnJson = new JsonObject();
         JsonArray masterFactConditions = new JsonArray();

         returnJson.add("masterFactConditions", masterFactConditions);

         JsonArray masterFactActions = new JsonArray();

         returnJson.add("masterFactActions", masterFactActions);

         JsonObject masterFactCondition = null;
         JsonObject masterFactAction = null;
         JsonArray container = null;

         for (int n = 0; n < properties.length; ++n)
         {
            trace.debug("Property: " + properties[n]);

            if (n < sections.length && sections[n] != null && !sections[n].isEmpty())
            {
               currentSectionName = sections[n];
            }

            sectionNames.add(currentSectionName);

            if (n < facts.length && facts[n] != null && !facts[n].isEmpty())
            {
               currentFactName = facts[n];

               if (currentSectionName.toLowerCase().startsWith("condition"))
               {
                  masterFactCondition = new JsonObject();

                  masterFactConditions.add(masterFactCondition);
                  masterFactCondition.addProperty("fact", currentFactName);
                  masterFactCondition.add("propertyConditions",
                        container = new JsonArray());
               }
               else
               {
                  masterFactAction = new JsonObject();

                  masterFactActions.add(masterFactAction);
                  masterFactAction.addProperty("fact", currentFactName);
                  masterFactAction.add("propertyActions", container = new JsonArray());
               }
            }

            if (container != null)
            {
               JsonObject masterProperty = new JsonObject();

               container.add(masterProperty);
               masterProperty.addProperty("property", properties[n]);
            }
            else
            {
               System.err.println("No container set. Wrong CSV structure.");
            }

            factNames.add(currentFactName);
            propertyNames.add(properties[n]);
         }

         JsonArray rulesJson = new JsonArray();

         returnJson.add("rules", rulesJson);

         String ruleLine = null;
         int ruleIndex = 0;

         while ((ruleLine = reader.readLine()) != null)
         {
            if (ruleLine.startsWith("------") || ruleLine.isEmpty())
            {
               break;
            }

            ++ruleIndex;

            trace.debug("Processing Line: " + ruleLine);

            JsonObject ruleJson = new JsonObject();

            rulesJson.add(ruleJson);
            ruleJson.addProperty("name", "Rule " + ruleIndex);

            JsonArray factConditionsJson = new JsonArray();

            ruleJson.add("conditions", factConditionsJson);

            JsonArray factActionsJson = new JsonArray();

            ruleJson.add("actions", factActionsJson);

            String[] values = ruleLine.split(";");
            String factName = null;
            JsonObject factConditionJson = null;
            JsonArray propertyConditionsJson = null;
            JsonObject factActionJson = null;
            JsonArray propertyActionsJson = null;

            for (int m = 0; m < values.length; ++m)
            {
               if (sectionNames.get(m).toLowerCase().startsWith("condition"))
               {
                  if ( !factNames.get(m).equals(factName))
                  {
                     factName = factNames.get(m);
                     factConditionJson = new JsonObject();

                     factConditionsJson.add(factConditionJson);
                     factConditionJson.addProperty("fact", factName);

                     propertyConditionsJson = new JsonArray();

                     factConditionJson.add("propertyConditions", propertyConditionsJson);
                  }

                  JsonObject propertyConditionJson = new JsonObject();

                  propertyConditionsJson.add(propertyConditionJson);
                  propertyConditionJson.addProperty("property", propertyNames.get(m));
                  propertyConditionJson.addProperty("operator", "=");
                  propertyConditionJson.addProperty("value", values[m]);
               }
               else
               {
                  if ( !factNames.get(m).equals(factName))
                  {
                     factName = factNames.get(m);
                     factActionJson = new JsonObject();

                     factActionsJson.add(factActionJson);
                     factActionJson.addProperty("fact", factName);

                     propertyActionsJson = new JsonArray();

                     factActionJson.add("propertyActions", propertyActionsJson);
                  }

                  JsonObject propertyActionJson = new JsonObject();

                  propertyActionsJson.add(propertyActionJson);
                  propertyActionJson.addProperty("property", propertyNames.get(m));
                  propertyActionJson.addProperty("operator", "=");
                  propertyActionJson.addProperty("value", values[m]);
               }
            }
         }

         return Response.ok(returnJson.toString(), APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.TEXT_PLAIN)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/parseDrl")
   /**
    *
    */
   public Response parseDrlFile(String fileContent)
   {
      try
      {
         trace.debug("Posted Data:");
         trace.debug(fileContent);

         DrlParser parser = new DrlParser();

         JsonObject ruleSetJson = parser.parseDrl(fileContent);

         return Response.ok(ruleSetJson.toString(), APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         return Response.serverError().build();
      }
   }

   /**
    * @return
    */
   private ClientModelManagementStrategy getClientModelManagementStrategy()
   {
      ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

      return (ClientModelManagementStrategy) context.getBean("clientModelManagementStrategy");
   }
}
