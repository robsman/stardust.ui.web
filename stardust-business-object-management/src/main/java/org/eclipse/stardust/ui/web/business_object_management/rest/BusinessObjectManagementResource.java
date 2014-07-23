package org.eclipse.stardust.ui.web.business_object_management.rest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.business_object_management.service.BusinessObjectManagementService;

import com.google.gson.JsonObject;

@Path("/")
public class BusinessObjectManagementResource {
	private static final Logger trace = LogManager
			.getLogger(BusinessObjectManagementResource.class);
	private BusinessObjectManagementService businessObjectManagementService;
	private final JsonMarshaller jsonIo = new JsonMarshaller();

	@Context
	private HttpServletRequest httpRequest;

	@Context
	private ServletContext servletContext;

	/**
	 * 
	 * @return
	 */
	public BusinessObjectManagementService getBusinessObjectManagementService() {
		return businessObjectManagementService;
	}

	/**
	 * 
	 * @param businessObjectManagementService
	 */
	public void setBusinessObjectManagementService(
			BusinessObjectManagementService businessObjectManagementService) {
		this.businessObjectManagementService = businessObjectManagementService;
	}

	@GET
	@Path("/businessObject.json")
	public Response getBusinessObject() {
		try {
			return Response.ok(
					getBusinessObjectManagementService().getBusinessObjects()
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@GET
	@Path("/businessObject/{modelOid}/{businessObjectId}/instance.json")
	public Response getBusinessObjectInstances(
			@PathParam("modelOid") String modelOid,
			@PathParam("businessObjectId") String businessObjectId) {
		try {
			return Response.ok(
					getBusinessObjectManagementService()
							.getBusinessObjectInstances(modelOid,
									businessObjectId).toString(),
					MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/businessObject/{modelOid}/{businessObjectId}/instance/{primaryKey}.json")
	public Response createBusinessObjectInstance(
			@PathParam("modelOid") String modelOid,
			@PathParam("businessObjectId") String businessObjectId,
			@PathParam("primaryKey") String primaryKey, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getBusinessObjectManagementService()
							.createBusinessObjectInstance(modelOid,
									businessObjectId, primaryKey, json).toString(),
					MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/businessObject/{modelOid}/{businessObjectId}/instance/{primaryKey}.json")
	public Response updateBusinessObjectInstance(
			@PathParam("modelOid") String modelOid,
			@PathParam("businessObjectId") String businessObjectId,
			@PathParam("primaryKey") String primaryKey, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getBusinessObjectManagementService()
							.updateBusinessObjectInstance(modelOid,
									businessObjectId, primaryKey, json).toString(),
					MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@GET
	@Path("/businessObject/{modelOid}/{businessObjectId}/{primaryKey}/processInstances.json")
	public Response getBusinessObjectProcessInstances(
			@PathParam("modelOid") String modelOid,
			@PathParam("businessObjectId") String businessObjectId,
			@PathParam("primaryKey") String primaryKey) {
		try {
			return Response.ok(
					getBusinessObjectManagementService()
							.getBusinessObjectProcessInstances(modelOid,
									businessObjectId, primaryKey).toString(),
					MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}
}
