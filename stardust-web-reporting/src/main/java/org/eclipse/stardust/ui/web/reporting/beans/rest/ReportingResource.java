package org.eclipse.stardust.ui.web.reporting.beans.rest;

import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.reporting.rt.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.reporting.beans.spring.ReportingServiceBean;
import org.eclipse.stardust.ui.web.reporting.common.LanguageUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 *
 * @author Marc.Gille
 * @author Yogesh.Manware
 *
 */
@Path("/")
public class ReportingResource
{
   private static final Logger trace = LogManager.getLogger(ReportingResource.class);
   private final JsonMarshaller jsonIo = new JsonMarshaller();
   private final Gson prettyPrinter = new GsonBuilder().setPrettyPrinting().create();

   @Resource
   private ReportingServiceBean reportingService;

   @Context
   private HttpServletRequest httpRequest;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("model-data")
   public Response getModelData()
   {
      try
      {
         return Response.ok(reportingService.getModelData(new Locale(reportingService.getLanguage(httpRequest))).toString(), MediaType.APPLICATION_JSON_TYPE).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("preference-data")
   public Response getPreferenceData()
   {
      try
      {
         return Response.ok(reportingService.getPreferenceData().toString(), MediaType.APPLICATION_JSON_TYPE).build();
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

         return Response.ok(reportingService.getReportData(postedData, httpRequest), MediaType.APPLICATION_JSON_TYPE)
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
   @Path("report-data")
   public Response getReportData()
   {
      try
      {
         trace.debug("GET report-data");
         String[] reportPathObj = (String[]) httpRequest.getParameterMap().get("reportPath");
         String reportJson = reportingService.loadReportDefinition(reportPathObj[0]).toString();
         return Response.ok(reportingService.getReportData(reportJson, httpRequest), MediaType.APPLICATION_JSON_TYPE)
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
            String updatedReportPath = reportingService
                  .renameReportDefinition(json.get("path").getAsString(), json.get("name").getAsString());

            return Response.ok(updatedReportPath, MediaType.TEXT_PLAIN).build();
         }
         else if (operation.equals("save"))
         {
            JsonObject reportJson = GsonUtils.extractObject(json, "report");
            return Response.ok(reportingService.saveReportDefinition(reportJson).toString(),
                  MediaType.APPLICATION_JSON).build();
         }
         else if (operation.equals("renameAndSave"))
         {
            JsonObject reportJson = GsonUtils.extractObject(json, "report");
            return Response.ok(reportingService.renameAndSaveReportDefinition(reportJson).toString(),
                  MediaType.APPLICATION_JSON).build();
            
         }
         return null;
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
   @Path("report-instance")
   public Response saveReportInstance(String postedData)
   {
      try
      {
         trace.debug("Save report instance: " + prettyPrinter.toJson(postedData));

         JsonObject reportInstancejson = jsonIo.readJsonObject(postedData);

         return Response.ok(reportingService.saveReportInstance(reportInstancejson).toString(), MediaType.APPLICATION_JSON).build();
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
   @Path("favorite")
   public Response addToFavorites(String postedData)
   {
      try
      {
         trace.debug("Add to Favorites: " + prettyPrinter.toJson(postedData));

         JsonObject reportInstancejson = jsonIo.readJsonObject(postedData);

         reportingService.addToFavorites(reportInstancejson);

         return Response.ok("OK", MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @DELETE
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.TEXT_PLAIN)
   @Path("favorite")
   public Response removeFromFavorites(@QueryParam("reportId") String reportId)
   {
      try
      {
         reportingService.removeFromFavorites(new String(Base64.decode(reportId.getBytes())));
         return Response.ok("OK", MediaType.APPLICATION_JSON).build();
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
      return Response.ok(reportingService.getLanguage(httpRequest), MediaType.TEXT_PLAIN_TYPE).build();
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

   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.TEXT_PLAIN)
   @Path("nextExecutionDate")
   public Response getNextExecutionDate(@QueryParam("schedulingJSON") String schedulingInfo)
   {
      try
      {
          trace.debug("Save report definitions: " + prettyPrinter.toJson(schedulingInfo));

         JsonObject json = jsonIo.readJsonObject(schedulingInfo);

         return Response.ok(reportingService.getNextExecutionDate(json), MediaType.TEXT_PLAIN).build();

      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Path("search/{serviceName}/{searchValue}")
   public Response search(@PathParam("serviceName") String serviceName, @PathParam("searchValue") String searchValue)
   {
      if (StringUtils.isNotEmpty(serviceName) && StringUtils.isNotEmpty(searchValue))
      {
         try
         {
            String result = reportingService.searchData(serviceName, searchValue);
            return Response.ok(result, MediaType.TEXT_PLAIN_TYPE).build();
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

   /**
    * @param reportId
    * @param reportName
    * @return
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   @Path("report-definition/download/{reportPath:.*}")
   public Response downloadReportDefinition(@javax.ws.rs.PathParam("reportPath") String reportPath)
   {
      try
      {
         String fileName = "";
         if (reportPath.contains("/"))
         {
            fileName = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(reportPath, "/");
            reportPath = "/" + reportPath;
         }
         else
         {
            fileName = org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(reportPath, "\\");
            reportPath = "\\" + reportPath;
         }
         
         return Response.ok(reportingService.downloadReportDefinition(reportPath), MediaType.APPLICATION_OCTET_STREAM)
               .header("content-disposition", "attachment; filename = \"" + URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20") + "\"").build();
      }
      catch (Exception e)
      {
         trace.debug(e);
      }
      return Response.serverError().build();
   }
   
   @GET
   @Consumes(MediaType.TEXT_PLAIN)
   @Produces(MediaType.TEXT_PLAIN)
   @Path("report-definition/upload")
   public Response uploadReport(@QueryParam("uuid") String uuid)
   {
      try
      {
         return Response.ok(reportingService.uploadReport(uuid).toString(),
               MediaType.APPLICATION_JSON).build();

      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
   
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.TEXT_PLAIN)
   @Path("executionDates")
   public Response getNextExecutionDates(@QueryParam("schedulingJSON") 
         String schedulingInfo, @QueryParam("startDate") String startDate,
         @QueryParam("endDate") String endDate)
   {
      try
      {
          trace.debug("Save report definitions: " + prettyPrinter.toJson(schedulingInfo));

         JsonObject json = jsonIo.readJsonObject(schedulingInfo);

         return Response.ok(reportingService.getNextExecutionDates(json, startDate, 
               endDate).toString(), MediaType.TEXT_PLAIN).build();

      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
}
