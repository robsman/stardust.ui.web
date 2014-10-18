package org.eclipse.stardust.ui.web.benchmark.rest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.benchmark.service.BenchmarkService;

@Path("/")
public class BenchmarkResource {
	private static final Logger trace = LogManager
			.getLogger(BenchmarkResource.class);
	private BenchmarkService benchmarkService;
	private final JsonMarshaller jsonIo = new JsonMarshaller();

	@Context
	private HttpServletRequest httpRequest;

	@Context
	private ServletContext servletContext;

	/**
	 * 
	 * @return
	 */
	public BenchmarkService getbenchmarkService() {
		return benchmarkService;
	}

	/**
	 * 
	 * @param benchmarkService
	 */
	public void setbenchmarkService(
			BenchmarkService benchmarkService) {
		this.benchmarkService = benchmarkService;
	}

	@GET
	@Path("/businessObject.json")
	public Response getBusinessObject() {
		try {
			return Response.ok(
					getbenchmarkService().getBusinessObjects()
							.toString(), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);

			return Response.serverError().build();
		}
	}
}
