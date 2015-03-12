package org.eclipse.stardust.ui.web.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantSearchResponseDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@Component
@Path("/user")
public class UserResource
{

   public static final Logger trace = LogManager.getLogger(UserResource.class);

   /**
    * @param serviceName
    * @param searchValue
    * @return
    */
   private String fetchUsers(String searchValue, boolean onlyActive, int maxMatches)
   {
      List<User> users = UserUtils.searchUsers(searchValue, onlyActive, maxMatches);
      List<ParticipantSearchResponseDTO> userWrappers = new ArrayList<ParticipantSearchResponseDTO>();
      for (User user : users)
      {
         UserDTO dto = new UserDTO();
         dto.setId(user.getId());
         dto.setName(UserUtils.getUserDisplayLabel(user));
         userWrappers.add(new ParticipantSearchResponseDTO((Participant) user));
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = userWrappers;
      resultDTO.totalCount = userWrappers.size();

      Gson gson = new Gson();
      return gson.toJson(resultDTO);
   }

   @GET
   @Path("/search/{searchValue}")
   public Response searchUsers(@PathParam("searchValue") String searchValue, @QueryParam("active") Boolean active,
         @QueryParam("max") Integer max)
   {
      if (StringUtils.isNotEmpty(searchValue))
      {
         try
         {
            if (active == null)
            {
               active = false;
            }
            if (max == null)
            {
               max = 20;
            }
            String result = fetchUsers(searchValue, active, max);
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
   
   @GET
   @Path("/test")
   public Response test()
   {
      return Response.ok("test ok", MediaType.TEXT_PLAIN_TYPE).build();
   }
}