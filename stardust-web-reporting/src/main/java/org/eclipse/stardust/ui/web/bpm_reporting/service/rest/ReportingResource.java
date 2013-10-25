package org.eclipse.stardust.ui.web.bpm_reporting.service.rest;

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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.bpm_reporting.service.ReportingService;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * 
 * @author Marc.Gille
 * 
 */
@Path("/")
public class ReportingResource {
	private static final Logger trace = LogManager
			.getLogger(ReportingResource.class);
	private final JsonMarshaller jsonIo = new JsonMarshaller();
	private final Gson prettyPrinter = new GsonBuilder().setPrettyPrinting()
			.create();
	private ReportingService reportingService;

	@Context
	private HttpServletRequest httpRequest;

	@Context
	private ServletContext servletContext;

	/**
	 * 
	 * @return
	 */
	public ReportingService getReportingService() {
		return reportingService;
	}

	/**
	 * 
	 * @param reportingService
	 */
	public void setReportingService(ReportingService reportingService) {
		this.reportingService = reportingService;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("model-data")
	public Response getModelData() {
		try {
			return Response.ok(getReportingService().getModelData().toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("report-data")
	public Response getReportData(String postedData) {
		try {
			trace.debug("report-data");
			trace.debug(postedData);

			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getReportingService().getReportData(json).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("report-definitions")
	public Response loadReportDefinitions() {
		try {
			return Response.ok(
					getReportingService().loadReportDefinitions().toString(),
					MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("report-definition")
	public Response loadReportDefinition(@PathParam("path") String path) {
		try {
			return Response
					.ok(getReportingService().loadReportDefinition(path)
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
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
	public Response loadReportDefinitionAsJson(String postedData) {
		try {
			trace.debug("Load report definition: " + postedData);

			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getReportingService().loadReportDefinition(
							json.get("path").getAsString()).toString(),
					MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("report-definition")
	public Response saveReportDefinition(String postedData) {
		try {
			trace.debug("Save report definition: "
					+ prettyPrinter.toJson(postedData));

			JsonObject json = jsonIo.readJsonObject(postedData);

			String operation = json.get("operation").getAsString();

			if (operation.equals("rename")) {
				getReportingService().renameReportDefinition(
						json.get("path").getAsString(),
						json.get("name").getAsString());

				return Response.ok("", MediaType.TEXT_PLAIN).build();
			} else {
				return Response.ok(
						getReportingService().saveReportDefinition(json)
								.toString(), MediaType.APPLICATION_JSON)
						.build();
			}
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("report-definitions")
	public Response saveReportDefinitions(String postedData) {
		try {
			trace.debug("Save report definitions: "
					+ prettyPrinter.toJson(postedData));

			JsonObject json = jsonIo.readJsonObject(postedData);

			getReportingService().saveReportDefinitions(json);

			return Response.ok("", MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("report-definition")
	public Response deleteReportDefinition(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getReportingService().deleteReportDefinition(json)
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("test-external-data")
	public Response testExternalDataRetrieval() {
		try {
			return Response.ok(
					getReportingService().testExternalDataRetrieval()
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}
}
