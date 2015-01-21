/**
 * 
 */
package org.eclipse.stardust.ui.web.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.UserGroupService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TestDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserGroupDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * @author Aditya.Gaikwad
 *
 */
@Component
@Path("/user-group")
public class UserGroupResource {

	public static final Logger trace = LogManager
			.getLogger(UserGroupResource.class);

	@Autowired
	private UserGroupService userGroupService;

	public UserGroupService getUserGroupService() {
		return userGroupService;
	}

	public void setUserGroupService(UserGroupService userGroupService) {
		this.userGroupService = userGroupService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/all")
	public Response getAllUserGroups() {
		List<UserGroupDTO> allUserGroupsDTO = getUserGroupService()
				.getAllUserGroups();
		return Response.ok(AbstractDTO.toJson(allUserGroupsDTO),
				MediaType.APPLICATION_JSON).build();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public Response getUserGroup(@PathParam("id") String id) {
		try {
			UserGroupDTO userGroupDTO = getUserGroupService().getUserGroup(id);

			return Response.ok(userGroupDTO.toJson(),
					MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@PUT
	@Consumes({ "application/xml", "application/json",
			"application/x-www-form-urlencoded" })
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/modify")
	public Response modifyUserGroup(UserGroupDTO userGroupDTO) {
		UserGroupDTO modifiedUserGroup = getUserGroupService().modifyUserGroup(
				userGroupDTO);
		return Response.ok(modifiedUserGroup, MediaType.APPLICATION_JSON)
				.build();
	}

/*	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/delete/{id}")
	public Response deleteUserGroup(@PathParam("id") String id) {
		UserGroupDTO deleteUserGroup = getUserGroupService()
				.deleteUserGroup(id);
		return Response
				.ok(deleteUserGroup.toJson(), MediaType.APPLICATION_JSON)
				.build();
	}*/

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/create")
	public Response createUserGroup(@QueryParam("id") String id,
			@QueryParam("name") String name,
			@QueryParam("description") String description,
			@QueryParam("validFrom") Date validFrom,
			@QueryParam("validTo") Date validTo) {
		UserGroupDTO createUserGroup = getUserGroupService().createUserGroup(
				id, name, description, validFrom, validTo);
		return Response
				.ok(createUserGroup.toJson(), MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Test method for Create and modify case
	 * 
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/test/test")
	public Response test() {
		String tempId = "Test_User_Group_"
				+ Calendar.getInstance().getTimeInMillis();
		UserGroupDTO userGroupDTO = getUserGroupService().createUserGroup(
				tempId, tempId, tempId, null, null);

		userGroupDTO.setDescription("Desc Modified "
				+ Calendar.getInstance().getTimeInMillis());

		UserGroupDTO modifyUserGroup = getUserGroupService().modifyUserGroup(
				userGroupDTO);

		return Response.ok(modifyUserGroup, MediaType.APPLICATION_JSON).build();
	}

}
