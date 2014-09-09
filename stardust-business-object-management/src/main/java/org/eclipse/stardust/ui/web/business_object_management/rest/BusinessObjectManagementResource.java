package org.eclipse.stardust.ui.web.business_object_management.rest;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import javax.activation.DataHandler;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.MultipartBody;
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
	@Path("/businessObject/{modelOid}/{businessObjectId}.json")
	public Response getBusinessObject(@PathParam("modelOid") String modelOid,
			@PathParam("businessObjectId") String businessObjectId) {
		try {
			return Response.ok(
					getBusinessObjectManagementService().getBusinessObject(
							modelOid, businessObjectId).toString(),
					MediaType.APPLICATION_JSON).build();
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
									businessObjectId, primaryKey, json)
							.toString(), MediaType.APPLICATION_JSON).build();
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
									businessObjectId, primaryKey, json)
							.toString(), MediaType.APPLICATION_JSON).build();
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

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/businessObject/{modelOid}/{businessObjectId}.form-data")
	public Response upload(MultipartBody body,
			@PathParam("modelOid") String modelOid,
			@PathParam("businessObjectId") String businessObjectId) {

		JsonObject businessObjectJson = getBusinessObjectManagementService()
				.getBusinessObject("" + modelOid, businessObjectId);
		JsonObject primaryKeyField = getPrimaryKeyField(businessObjectJson);

		try {
			Attachment attachment = body.getAttachment("file");
			DataHandler dataHandler = attachment.getDataHandler();
			InputStream inputStream = dataHandler.getInputStream();
			BufferedReader x = new BufferedReader(new InputStreamReader(
					inputStream));

			String line = null;
			String[] fields = null;
			int n = 0;

			while ((line = x.readLine()) != null) {
				if (n == 0) {
					fields = line.split(",");

					++n;

					continue;
				} else if (n > 100) {
					// Only 100 for now

					break;
				}

				String[] fieldValues = line.split(",");

				JsonObject businessObjectInstanceJson = new JsonObject();

				// TODO Only 22 fields for testing

				for (int m = 0; m < fieldValues.length && m < 19; ++m) {
					// Strip whitespaces from field identifiers

					businessObjectInstanceJson.addProperty(
							fields[m].replaceAll("\\s+", ""), fieldValues[m]);
				}

				System.out.println("Primary Key");
				System.out.println(primaryKeyField);
				System.out.println(businessObjectInstanceJson);

				try {
					getBusinessObjectManagementService()
							.createBusinessObjectInstance(
									modelOid,
									businessObjectId,
									businessObjectInstanceJson.get(
											primaryKeyField.get("id")
													.getAsString())
											.getAsString(),
									businessObjectInstanceJson);
				} catch (Exception e) {
					e.printStackTrace();
				}

				++n;
			}

			return Response.ok("{}", MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	/**
	 * 
	 * @param businessObjectJson
	 * @return
	 */
	private JsonObject getPrimaryKeyField(JsonObject businessObjectJson) {
		for (int n = 0; n < businessObjectJson.get("fields").getAsJsonArray()
				.size(); ++n) {
			if (businessObjectJson.get("fields").getAsJsonArray().get(n)
					.getAsJsonObject().get("primaryKey").getAsBoolean()) {
				return businessObjectJson.get("fields").getAsJsonArray().get(n)
						.getAsJsonObject();
			}
		}

		throw new IllegalArgumentException("Primary key field not found.");
	}
}
