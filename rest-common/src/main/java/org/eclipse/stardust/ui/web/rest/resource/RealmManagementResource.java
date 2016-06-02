package org.eclipse.stardust.ui.web.rest.resource;

import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.service.RealmManagementService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.RealmDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@Path("/realm")
public class RealmManagementResource
{

   private static final Logger trace = LogManager.getLogger(RealmManagementResource.class);

   @Autowired
   private RealmManagementService realmManagementService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/fetch")
   @ResponseDescription("The response will have List of UserRealm")
   public Response getUserRealms()
   {
      try
      {
         return Response.ok(GsonUtils.toJsonHTMLSafeString(realmManagementService.getUserRealms()), MediaType.APPLICATION_JSON)
               .build();
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
   @DTODescription(request="org.eclipse.stardust.ui.web.rest.dto.RealmDTO")
   public Response save(String postData)
   {
      RealmDTO realmDTO = GsonUtils.fromJson(postData, RealmDTO.class);
      try
      {
         return Response.ok(GsonUtils.toJsonHTMLSafeString(realmManagementService.createRealm(realmDTO.id, realmDTO.name, realmDTO.description)),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         return Response.status(417).entity(e.getMessage()).build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/delete")
   public Response delete(String postData)
   {
      JsonObject json = GsonUtils.readJsonObject(postData);
      
      Type listType = new TypeToken<List<String>>()
            {
            }.getType();
            
      @SuppressWarnings("unchecked")
      List<String> realmIds = (List<String>) GsonUtils.extractList(GsonUtils.extractJsonArray(json, "ids"), listType);

      try
      {
         realmManagementService.deleteRealms(realmIds);
         return Response.ok("", MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         return Response.status(417).entity(e.getMessage()).build();
      }
   }
}
