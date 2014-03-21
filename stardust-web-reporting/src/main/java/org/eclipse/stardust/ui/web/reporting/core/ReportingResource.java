package org.eclipse.stardust.ui.web.reporting.core;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.reporting.common.LanguageUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 *
 * @author Marc.Gille
 *
 */
@Path("/")
public class ReportingResource
{
   private static final Logger trace = LogManager.getLogger(ReportingResource.class);
   private final JsonMarshaller jsonIo = new JsonMarshaller();
   private final Gson prettyPrinter = new GsonBuilder().setPrettyPrinting().create();

   @Resource
   private ReportingService reportingService;

   @Context
   private HttpServletRequest httpRequest;

   @Context
   private ServletContext servletContext;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("model-data")
   public Response getModelData()
   {
      try
      {
         return Response.ok(reportingService.getModelData().toString(), MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("report-data")
   public Response getReportData(String postedData)
   {
      try
      {
         trace.debug("report-data");
         trace.debug(postedData);

         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(reportingService.getReportData(json).toString(), MediaType.APPLICATION_JSON_TYPE)
               .build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("report-definitions")
   public Response loadReportDefinitions()
   {
      try
      {
         return Response.ok(reportingService.loadReportDefinitions().toString(), MediaType.APPLICATION_JSON)
               .build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("report-definition/{reportPath:.*}")
   public Response loadReportDefinition(@PathParam("reportPath") String path)
   {
      try
      {
         return Response.ok(reportingService.loadReportDefinition("/" + path).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    *
    * @param postedData
    * @return
    *
    * @deprecated Use GET instead
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("report-definition")
   public Response loadReportDefinitionAsJson(String postedData)
   {
      try
      {
         trace.debug("Load report definition: " + postedData);

         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(reportingService.loadReportDefinition(json.get("path").getAsString()).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.TEXT_PLAIN)
   @Path("report-definition")
   public Response saveReportDefinition(String postedData)
   {
      try
      {
         trace.debug("Save report definition: " + prettyPrinter.toJson(postedData));

         JsonObject json = jsonIo.readJsonObject(postedData);

         String operation = json.get("operation").getAsString();

         if (operation.equals("rename"))
         {
            reportingService
                  .renameReportDefinition(json.get("path").getAsString(), json.get("name").getAsString());

            return Response.ok("", MediaType.TEXT_PLAIN).build();
         }
         else
         {
            JsonObject reportJson = GsonUtils.extractObject(json, "report");
            return Response.ok(reportingService.saveReportDefinition(reportJson).toString(),
                  MediaType.APPLICATION_JSON).build();
         }
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.TEXT_PLAIN)
   @Path("report-definitions")
   public Response saveReportDefinitions(String postedData)
   {
      try
      {
         trace.debug("Save report definitions: " + prettyPrinter.toJson(postedData));

         JsonObject json = jsonIo.readJsonObject(postedData);

         reportingService.saveReportDefinitions(json);

         return Response.ok("", MediaType.TEXT_PLAIN).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("report-definition/{reportPath:.*}")
   public Response deleteReportDefinition(@PathParam("reportPath") String path)
   {
      try
      {
         return Response.ok(reportingService.deleteReportDefinition("/" + path).toString(), MediaType.APPLICATION_JSON)
               .build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.TEXT_PLAIN)
   @Path("/language")
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
}
