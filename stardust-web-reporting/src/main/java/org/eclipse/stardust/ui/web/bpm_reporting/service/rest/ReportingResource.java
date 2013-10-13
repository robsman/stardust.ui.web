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
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
	private ReportingService reportingService;
	private final JsonMarshaller jsonIo = new JsonMarshaller();
	private final Gson prettyPrinter = new GsonBuilder().setPrettyPrinting().create();

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
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("report-data")
	public Response getReportData(String postedData) {
		try {
			System.out.println("report-data");
			System.out.println(postedData);

			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getReportingService().getReportData(json).toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("report-definitions")
	public Response loadReportDefinitions() {
		try {
			return Response.ok(
					getReportingService().loadReportDefinitions()
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("report-definition")
	public Response loadReportDefinition(@PathParam("path") String path) {
		try {
			return Response.ok(
					getReportingService().loadReportDefinition(path)
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
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
			System.out.println("Load report definition: " + postedData);

			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(
					getReportingService().loadReportDefinition(json.get("path").getAsString())
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("report-definition")
	public Response saveReportDefinition(String postedData) {
		try {
			System.out.println("Save report definition: " + prettyPrinter.toJson(postedData));

			JsonObject json = jsonIo.readJsonObject(postedData);

			String operation = json.get("operation").getAsString();
			
			if (operation.equals("rename"))
			{
				getReportingService().renameReportDefinition(json.get("path").getAsString(), json.get("name").getAsString());				
			}
			else
			{
				getReportingService().saveReportDefinition(json);
			}

			return Response.ok("", MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("report-definitions")
	public Response saveReportDefinitions(String postedData) {
		try {
			System.out.println("Save report definitions: " + prettyPrinter.toJson(postedData));

			JsonObject json = jsonIo.readJsonObject(postedData);
			
			getReportingService().saveReportDefinitions(json);

			return Response.ok("", MediaType.TEXT_PLAIN).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
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
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}
}
