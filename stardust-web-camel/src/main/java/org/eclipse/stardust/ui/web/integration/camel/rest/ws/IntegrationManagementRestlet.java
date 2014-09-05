package org.eclipse.stardust.ui.web.integration.camel.rest.ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.stardust.engine.extensions.camel.integration.management.IntegrationManagement;

public class IntegrationManagementRestlet {

	IntegrationManagement integrationManagement;

	public IntegrationManagement getIntegrationManagement() {
		return integrationManagement;
	}

	public void setIntegrationManagement(
			IntegrationManagement integrationManagement) {
		this.integrationManagement = integrationManagement;
	}

	@GET
	@Path("/contexts")
	@Produces(MediaType.APPLICATION_JSON)
	public String contextsListService() {
		return integrationManagement.contextsList();
	}

	@GET
	@Path("/routes/{contextId}")
	@Produces(MediaType.APPLICATION_JSON)
	public String allRoutesListService(@PathParam("contextId") String contextId) {
		return integrationManagement.allRoutesList(contextId);
	}

	@GET
	@Path("/context/{contextId}/route/{routeId}/start")
	@Produces(MediaType.TEXT_PLAIN)
	public void startRouteService(@PathParam("contextId") String contextId,
			@PathParam("routeId") String routeId) {
		integrationManagement.startRouteService(contextId, routeId);
	}

	@GET
	@Path("/context/{contextId}/route/{routeId}/stop")
	@Produces(MediaType.TEXT_PLAIN)
	public void stopRouteService(@PathParam("contextId") String contextId,
			@PathParam("routeId") String routeId) {
		integrationManagement.stopRoute(contextId, routeId);

	}
	
}