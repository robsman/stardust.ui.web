package org.eclipse.stardust.ui.mobile.rest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.mobile.service.MobileWorkflowService;

import com.google.gson.JsonObject;

/**
 * 
 * @author Marc.Gille
 * 
 */
@Path("/")
public class MobileWorkflowResource {
	private MobileWorkflowService mobileWorkflowService;
	private final JsonMarshaller jsonIo = new JsonMarshaller();

	@Context
	private HttpServletRequest httpRequest;

	@Context
	private ServletContext servletContext;

	/**
	 * 
	 * @return
	 */
	public MobileWorkflowService getMobileWorkflowService() {
		return mobileWorkflowService;
	}

	/**
	 * 
	 * @param mobileWorkflowService
	 */
	public void setMobileWorkflowService(
			MobileWorkflowService mobileWorkflowService) {
		this.mobileWorkflowService = mobileWorkflowService;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("login")
	public Response login(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getMobileWorkflowService().login(json).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("startable-processes")
	public Response getStartableProcesses() {
		try {
			return Response.ok(
					getMobileWorkflowService().getStartableProcesses()
							.toString(), MediaType.APPLICATION_JSON_TYPE)
					.build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("worklist")
	public Response getWorklist() {
		try {
			return Response.ok(
					getMobileWorkflowService().getWorklist().toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("activity/activate")
	public Response activateActivity(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getMobileWorkflowService().activateActivity(json)
							.toString(), MediaType.APPLICATION_JSON_TYPE)
					.build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid}")
	public Response getProcessInstance(@PathParam("oid") String processOid) {
		try {
			return Response.ok(
					getMobileWorkflowService().getProcessInstance(
							Long.parseLong(processOid)).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid}/notes")
	public Response getNotes(@PathParam("oid") String processOid) {
		try {
			return Response.ok(
					getMobileWorkflowService().getNotes(
							Long.parseLong(processOid)).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("process-instances/{oid}/notes/create")
	public Response createNote(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getMobileWorkflowService().createNote(json).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("folders/{id}")
	public Response getFolders(@PathParam("id") String folderId) {
		try {
			return Response.ok(
					getMobileWorkflowService().getFolders(folderId).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}
}
