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

package org.eclipse.stardust.ui.web.modeler.rest;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.portal.ViewUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

@Path("/modeler/{randomPostFix}")
public class ModelerResource {

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

	public ModelService getModelService() {
		ApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(servletContext);
		return (ModelService) context.getBean("modelService");
	}

	public void setModelService(ModelService modelService) {
		this.modelService = modelService;
	}

	private synchronized String getNextId() {
		return "a" + id++;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("uniqueid")
	public Response getUniqueId() {
		try {
			return Response.ok(getNextId(), MediaType.TEXT_PLAIN_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("users/requestJoin")
	public Response requestJoin(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			System.out.println("requestJoin: " + postedData);

			return Response.ok(getModelService().requestJoin(json),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("users/confirmJoin")
	public Response confirmJoin(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(getModelService().confirmJoin(json),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("users/submitChatMessage")
	public Response submitChatMessage(String postedData) {
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
			@Context ServletContext servletContext,
			@Context HttpServletRequest req, @Context HttpServletResponse resp) {
		try {
			ViewUtils.openView(modelId, processId, servletContext, req, resp);
			return Response.ok().build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models")
	public Response createModel(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createModel(getNextId(), json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/createWithId/{modelId}")
	public Response createModel(@PathParam("modelId") String modelId,
			String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createModel(modelId, json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}")
	public Response deleteModel(@PathParam("modelId") String modelId,
			String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);
			String result = getModelService().deleteModel(modelId, json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}")
	public Response deleteProcess(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);
			String result = getModelService().deleteProcess(modelId, processId,
					json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/structuredDataTypes/{id}")
	public Response deleteStructuredDataType(
			@PathParam("modelId") String modelId,
			@PathParam("id") String structuredDataTypeId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);
			String result = getModelService().deleteStructuredDataType(modelId,
					structuredDataTypeId, json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/participants/{id}")
	public Response deleteParticipantRole(@PathParam("modelId") String modelId,
			@PathParam("id") String participantId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);
			String result = getModelService().deleteParticipant(modelId,
					participantId, json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/{applicationId}")
	public Response deleteApplication(@PathParam("modelId") String modelId,
			@PathParam("applicationId") String applicationId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);
			String result = getModelService().deleteApplication(modelId,
					applicationId, json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models")
	public Response getAllModels() {
		try {
			String result = getModelService().getAllModels();
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/process/{processId}/loadModel")
	public Response loadModel(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId) {
		try {
			String result = getModelService().loadProcessDiagram(modelId,
					processId);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/rename")
	public Response renameModel(@PathParam("modelId") String modelId,
			String postedData) {
		try {
			String result = getModelService().renameModel(modelId,
					jsonIo.readJsonObject(postedData));
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/rename")
	public Response renameProcess(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String command) {
		try {
			JsonObject json = jsonIo.readJsonObject(command);

			String result = getModelService().renameProcess(modelId, processId,
					json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/{applicationId}/rename")
	public Response renameApplication(@PathParam("modelId") String modelId,
			@PathParam("applicationId") String applicationId, String command) {
		try {
			JsonObject json = jsonIo.readJsonObject(command);
			String result = getModelService().renameApplication(modelId,
					applicationId, json);

			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/participants/{participantId}/rename")
	public Response renameParticipant(@PathParam("modelId") String modelId,
			@PathParam("participantId") String participantId, String command) {
		try {
			JsonObject json = jsonIo.readJsonObject(command);
			String result = getModelService().renameParticipant(modelId,
					participantId, json);

			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/structuredDataTypes/{structuredDataTypeId}/rename")
	public Response renameStructuredDataType(
			@PathParam("modelId") String modelId,
			@PathParam("structuredDataTypeId") String structuredDataTypeId,
			String command) {
		try {
			JsonObject json = jsonIo.readJsonObject(command);
			String result = getModelService().renameStructuredDataType(modelId,
					structuredDataTypeId, json);
			return Response.ok(result, MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{id}/processes")
	public Response createProcess(@PathParam("id") String modelId,
			String postedData) {
		try {
			String result = getModelService().createProcess(modelId,
					jsonIo.readJsonObject(postedData));
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
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
			@PathParam("diagramId") String diagramId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateProcessDiagram(modelId,
					processId, diagramId, json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/activities")
	public Response createActivity(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			String result = getModelService().createActivity(modelId,
					processId, jsonIo.readJsonObject(postedData));
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
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
			@PathParam("activityId") String activityId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().renameActivity(modelId,
					processId, activityId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/activities/{activityId}")
	public Response updateActivity(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("activityId") String activityId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateActivity(modelId,
					processId, activityId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/activities/{activityId}")
	public Response deleteActivity(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("activityId") String activityId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().deleteActivity(modelId,
					processId, activityId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/gateways")
	public Response createGateway(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createGateway(modelId, processId,
					json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/gateways/{gatewayId}")
	public Response updateGateway(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("gatewayId") String gatewayId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateGateway(modelId, processId,
					gatewayId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/gateways/{gatewayId}")
	public Response updateGateway(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("gatewayId") String gatewayId) {
		try {
			String result = getModelService().deleteGateway(modelId, processId,
					gatewayId);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	/*
	 * @POST
	 * 
	 * @Consumes(MediaType.APPLICATION_JSON)
	 * 
	 * @Produces(MediaType.APPLICATION_JSON)
	 * 
	 * @Path(
	 * "models/{modelId}/processes/{processId}/elementType/{elementType}/elementId/{elementId}/properties"
	 * ) public Response updateProperties(@PathParam("modelId") String modelId,
	 * 
	 * @PathParam("processId") String processId,
	 * 
	 * @PathParam("activityId") String activityId, @PathParam("elementType")
	 * String elementType,
	 * 
	 * @PathParam("elementId") String elementId, String postedData) { String
	 * result = getModelService().updateProperties(modelId, processId,
	 * elementType, elementId, postedData); return Response.ok(result,
	 * APPLICATION_JSON_TYPE).build(); }
	 */

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/connections")
	public Response createConnection(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createConnection(modelId,
					processId, json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/connections/{connectionOid}")
	public Response updateConnection(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("connectionOid") long connectionOid, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateConnection(modelId,
					processId, connectionOid, json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/connections/{connectionOid}")
	public Response deleteConnection(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("connectionOid") long connectionOid) {
		try {
			String result = getModelService().deleteConnection(modelId,
					processId, connectionOid);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/events")
	public Response createEvent(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createEvent(modelId, processId,
					json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/events/{eventId}")
	public Response updateEvent(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("eventId") String eventId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateEvent(modelId, processId,
					eventId, json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/lanes")
	public Response createLane(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createLane(modelId, processId,
					json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/lanes/{laneId}")
	public Response updateLane(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("laneId") String laneId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateLane(modelId, processId,
					laneId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Path("models/{modelId}")
	public Response saveModel(@PathParam("modelId") String modelId) {
		try {
			getModelService().saveModel(modelId);

			ResponseBuilder response = Response.ok("Saved");

			return response.build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@GET
	@Path("models/{modelId}/processes/{processId}/undo")
	public Response undo(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId) {
		try {
			String result = getModelService().undo(modelId, processId);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@GET
	@Path("models/{modelId}/processes/{processId}/redo")
	public Response redo(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId) {
		try {
			String result = getModelService().redo(modelId, processId);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/data")
	public Response createData(@PathParam("modelId") String modelId,
			String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createData(modelId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/dataSymbols")
	public Response createDataSymbol(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createDataSymbol(modelId,
					processId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/dataSymbols")
	public Response dropDataSymbol(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().dropDataSymbol(modelId,
					processId, json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/primitiveDataTypes/{datatypeId}")
	public Response updateDatatype(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("datatypeId") String datatypeId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateData(modelId, processId,
					datatypeId, json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/structuredDataTypes/{datatypeId}")
	public Response updateStructuredDatatype(
			@PathParam("modelId") String modelId,
			@PathParam("processId") String processId,
			@PathParam("datatypeId") String datatypeId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().updateData(modelId, processId,
					datatypeId, json);
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/roles")
	public Response createRole(@PathParam("modelId") String modelId,
			String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createRole(modelId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	// ======================== TODO Put in separate resource as we are not
	// going to share this with Eclipse =====================

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/getModelingHelp")
	public Response getModelingHelp(String postedData) {
		String result = jsonIo.writeJsonObject(new JsonObject());
		return Response.ok(result, APPLICATION_JSON_TYPE).build();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/createDocumentation")
	public Response createDocumentation(@PathParam("modelId") String modelId,
			String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService()
					.createDocumentation(modelId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/createDocumentation")
	public Response createDocumentation(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			String result = getModelService().createDocumentation(modelId,
					processId, json);

			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/webServiceApplications")
	public Response createWebServiceApplication(
			@PathParam("modelId") String modelId, String postedData) {

		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			json = getModelService().createWebServiceApplication(modelId, json);

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/messageTransformationApplications")
	public Response createMessageTransformationApplication(
			@PathParam("modelId") String modelId, String postedData) {

		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			json = getModelService().createMessageTransformationApplication(
					modelId, json);

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/messageTransformationApplications/{applicationId}")
	public Response updateMessageTransformationApplication(
			@PathParam("modelId") String modelId,
			@PathParam("applicationId") String applicationId, String postedData) {

		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			json = getModelService().updateMessageTransformationApplication(
					modelId, applicationId, json);

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/camelApplications")
	public Response createCamelApplication(
			@PathParam("modelId") String modelId, String postedData) {

		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			json = getModelService().createCamelApplication(modelId, json);

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/camelApplications/{applicationId}")
	public Response updateCamelApplication(
			@PathParam("modelId") String modelId,
			@PathParam("applicationId") String applicationId, String postedData) {

		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			json = getModelService().updateCamelApplication(modelId,
					applicationId, json);

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/applications/externalWebApplications")
	public Response createExternalWebApplication(
			@PathParam("modelId") String modelId, String postedData) {

		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			json = getModelService()
					.createExternalWebApplication(modelId, json);

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/structuredDataTypes")
	public Response createStructuredDataType(
			@PathParam("modelId") String modelId, String postedData) {

		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			json = getModelService().createStructuredDataType(modelId, json);

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/structuredDataTypes/loadFromUrl")
	public Response loadStructuredDataTypeFromUrl(
			@PathParam("modelId") String modelId, String postedData) {

		try {
			JsonObject json = new JsonObject();

			System.out.println("URL: " + json.get("url").getAsString());

			JsonObject jTdOrder = new JsonObject();
			json.add("ord:Order", jTdOrder);

			jTdOrder.addProperty("name", "Order");

			JsonObject jTdOrderSub = new JsonObject();
			jTdOrder.add("children", jTdOrderSub);

			JsonObject jOrderId = new JsonObject();
			jTdOrderSub.add("OrderId", jOrderId);

			jOrderId.addProperty("type", "xsd:string");
			jOrderId.addProperty("cardinality", "1");

			JsonObject jOrderDate = new JsonObject();
			jTdOrderSub.add("OrderDate", jOrderDate);

			jOrderDate.addProperty("type", "xsd:date");
			jOrderDate.addProperty("cardinality", "1");

			JsonObject jCustomer = new JsonObject();
			jTdOrderSub.add("Customer", jCustomer);

			jCustomer.addProperty("type", "per:Person");
			jCustomer.addProperty("cardinality", "1");

			JsonObject jTdPerson = new JsonObject();
			json.add("per:Person", jTdPerson);

			jTdPerson.addProperty("name", "Person");

			JsonObject jTdPersonSub = new JsonObject();
			jTdPerson.add("children", jTdPersonSub);

			JsonObject jFirstName = new JsonObject();
			jTdPersonSub.add("FirstName", jFirstName);

			jFirstName.addProperty("type", "xsd:string");
			jFirstName.addProperty("cardinality", "1");

			JsonObject jLastName = new JsonObject();
			jTdPersonSub.add("LastName", jLastName);

			jLastName.addProperty("type", "xsd:string");
			jLastName.addProperty("cardinality", "1");

			JsonObject jDob = new JsonObject();
			jTdPersonSub.add("DateOfBirth", jDob);

			jDob.addProperty("type", "xsd:date");
			jDob.addProperty("cardinality", "1");

			return Response.ok(jsonIo.writeJsonObject(json),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{id}/processes/createWrapperProcess")
	public Response createWrapperProcess(@PathParam("id") String modelId,
			String postedData) {
		try {
			String result = getModelService().createWrapperProcess(modelId,
					jsonIo.readJsonObject(postedData));
			return Response.ok(result, APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("models/{modelId}/processes/{processId}/decorations")
	public Response getDecorations(@PathParam("modelId") String modelId,
			@PathParam("processId") String processId, String postedData) {
		try {
			return Response.ok("{}", APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
			@PathParam("decorationId") String decorationId, String postedData) {
		try {
			System.out.println("Retrieve decoration " + decorationId);

			JsonObject decorations = new JsonObject();

			final JsonObject decDefault = new JsonObject();
			decorations.add("default", decDefault);

			JsonObject decProgrss = new JsonObject();
			decorations.add("progress", decProgrss);

			JsonArray elements = new JsonArray();
			decProgrss.add("elements", elements);

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

			JsonObject decKpi = new JsonObject();
			decorations.add("kpi", decKpi);

			elements = new JsonArray();
			decKpi.add("elements", elements);

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

			int[][] values = { { 0, 2 }, { 5, 6 }, { 10, 10 }, { 15, 20 },
					{ 20, 17 }, { 25, 5 }, { 30, 30 }, { 35, 40 }, { 40, 45 },
					{ 45, 48 }, { 50, 50 } };

			for (int n = 0; n < values.length; ++n) {
				final JsonArray point = new JsonArray();
				data.add(point);

				point.add(new JsonPrimitive(values[n][0]));
				point.add(new JsonPrimitive(values[n][1]));
			}

			return Response.ok(decorations.get(decorationId).toString(),
					APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
