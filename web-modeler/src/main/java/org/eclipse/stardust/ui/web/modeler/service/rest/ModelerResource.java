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

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.model.xpdl.builder.strategy.ModelManagementStrategy;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.modeler.common.LanguageUtil;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.portal.ViewUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Path("/modeler/{randomPostFix}")
public class ModelerResource
{
   private final JsonMarshaller jsonIo = new JsonMarshaller();

   private ModelService modelService;

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

   /**
    * @deprecated
    * @param modelId
    * @param processId
    * @param servletContext
    * @param req
    * @param resp
    * @return
    */
   @POST
   @Path("models/{modelId}/processes/{processId}/openview")
   public Response openProcessView(@PathParam("modelId") String modelId,
         @PathParam("processId") String processId,
         @Context ServletContext servletContext, @Context HttpServletRequest req,
         @Context HttpServletResponse resp)
   {
      try
      {
         ViewUtils.openView(modelId, processId, servletContext, req, resp);
         return Response.ok().build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("preferences")
   public Response getPreferences()
   {
      try
      {
         return Response.ok(getModelService().getPreferences().toString(), MediaType.APPLICATION_JSON_TYPE).build();
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
   public Response getAllModels()
   {
      try
      {
         //TODO - currently always forces a reload - getAllModels(true)
         //we may need to make it conditional
         String result = getModelService().getAllModels(true);
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
   @Produces (MediaType.APPLICATION_JSON)
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

   @HEAD
   @Path("users/getOfflineInvites")
   public Response getOfflineInvites()
   {
      try
      {
         PortalApplication app = WebApplicationContextUtils.getWebApplicationContext(servletContext).getBean(PortalApplication.class);
         org.eclipse.stardust.ui.web.common.spi.user.User currentUser = app.getLoggedInUser();
         getModelService().getOfflineInvites(currentUser.getLoginName());
         return Response.ok().build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes (MediaType.APPLICATION_JSON)
   @Produces (MediaType.APPLICATION_JSON)
   @Path("users/updateOwner")
   public Response updateOwner(String postedData)
   {
      try
      {
         JsonObject userJson = jsonIo.readJsonObject(postedData);
         String sessionID = userJson.getAsJsonObject("oldObject").get("sessionId").getAsString();
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
   @Consumes (MediaType.APPLICATION_JSON)
   @Produces (MediaType.APPLICATION_JSON)
   @Path("users/getAllProspects")
   public Response getAllProspects(String postedData)
   {
      try
      {
         JsonObject userJson = jsonIo.readJsonObject(postedData);
         String result = getModelService().getAllProspects(userJson.getAsJsonObject("oldObject").get("account").getAsString());
         return Response.ok(result, APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException(e);
      }
   }

   @POST
   @Consumes (MediaType.APPLICATION_JSON)
   @Produces (MediaType.APPLICATION_JSON)
   @Path("users/getAllCollaborators")
   public Response getAllCollaborators(String postedData)
   {
      try
      {
         JsonObject userJson = new JsonMarshaller().readJsonObject(postedData);
         //utlity methode gson utils
         String result = getModelService().getAllCollaborators(userJson.getAsJsonObject("oldObject").get("account").getAsString());

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
         return Response.ok(getModelService().getXsdStructure(jsonIo.readJsonObject(postedData)).toString(),
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
   @Path("models/{id}/processes/createWrapperProcess")
   public Response createWrapperProcess(@PathParam("id") String modelId, String postedData)
   {
      try
      {
         String result = getModelService().createWrapperProcess(modelId,
               jsonIo.readJsonObject(postedData));
         return Response.ok(result, APPLICATION_JSON_TYPE).build();
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
         System.out.println("Retrieve decoration " + decorationId);

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

         element.addProperty("id", "Activity1");

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

         element.addProperty("id", "Activity2");

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

         element.addProperty("id", "Activity1");

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

         element.addProperty("id", "Activity2");

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
         return Response.ok(getModelService().getWebServiceStructure(jsonIo.readJsonObject(postedData)).toString(),
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
   @Path ("/language")
   public Response getLanguage()
   {
      StringTokenizer tok = new StringTokenizer(httpRequest.getHeader("Accept-language"), ",");
      if (tok.hasMoreTokens())
      {
         return Response.ok(LanguageUtil.getLocale(tok.nextToken()), MediaType.TEXT_PLAIN_TYPE).build();
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
   public Response getRetrieve(@PathParam("bundleName") String bundleName, @PathParam("locale") String locale)
   {
      final String POST_FIX = "client-messages";

      if (StringUtils.isNotEmpty(bundleName) && bundleName.endsWith(POST_FIX))
      {
         try
         {
            StringBuffer bundleData = new StringBuffer();
            ResourceBundle bundle = ResourceBundle.getBundle(bundleName, LanguageUtil.getLocaleObject(locale));

            String key;
            Enumeration<String> keys = bundle.getKeys();
            while (keys.hasMoreElements())
            {
               key = keys.nextElement();
               bundleData.append(key).append("=").append(bundle.getString(key)).append("\n");
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
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("model/management/strategy")
   public Response setModelManagementStrategy(String postedData)
   {
      try
      {
         getModelService().setModelManagementStrategy(getUriModelManagementStrategy());
         return Response.ok(getModelService().getWebServiceStructure(jsonIo.readJsonObject(postedData)).toString(),
               APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         e.printStackTrace();

         throw new RuntimeException(e);
      }
   }
   
   /**
    * @return
    */
   private ModelManagementStrategy getUriModelManagementStrategy()
   {
      ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(servletContext);

      return (ModelManagementStrategy) context.getBean("uriModelManagementStrategy");
   }
}
