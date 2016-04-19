package org.eclipse.stardust.ui.web.rest.resource;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.service.CriticalityConfigService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.dto.CriticalityConfigDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@Path("/criticality-config")
public class CriticalityConfigResource
{
   private static final Logger trace = LogManager.getLogger(CriticalityConfigResource.class);
   
   @Autowired
   private CriticalityConfigService criticalityConfigService;
   
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/fetch")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.CriticalityConfigDTO")
   public Response getCriticalityConfig()
   {
      try
      {
         return Response.ok(GsonUtils.toJsonHTMLSafeString(criticalityConfigService.getCriticalityConfig()), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/save")
   @DTODescription(request="org.eclipse.stardust.ui.web.rest.dto.CriticalityConfigDTO")
   public Response save(String postData) {
      CriticalityConfigDTO criticalityConfigDTO = GsonUtils.fromJson(postData, CriticalityConfigDTO.class);
      try
      {
         criticalityConfigService.save(criticalityConfigDTO);
         
         return Response.ok("",
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         return Response.status(417).entity(e.getMessage()).build();
      }
   }
   
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/import")
   
   public Response importCriticalityConfig(String postData) {
      JsonObject json = GsonUtils.readJsonObject(postData);
      String uuid = GsonUtils.extractString(json, "uuid");
      try
      {
         criticalityConfigService.importCriticalityConfig(uuid);
         
         return Response.ok("",
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         return Response.status(417).entity(e.getMessage()).build();
      }
   }
   
   @GET
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   @Path("/export")
   public Response exportCriticalityConfig() {
      try
      {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         try
         {
            criticalityConfigService.exportCriticalityConfig(output);
         }
         catch (Exception e)
         {
            trace.error(e, e);
         }
         byte[] bytes = output.toByteArray();
         
         Map<String, Object> res = new HashMap<String, Object>();
         res.put("data", bytes);
         
         return Response.ok(GsonUtils.toJsonHTMLSafeString(res), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         return Response.status(417).entity(e.getMessage()).build();
      }
   }
}
