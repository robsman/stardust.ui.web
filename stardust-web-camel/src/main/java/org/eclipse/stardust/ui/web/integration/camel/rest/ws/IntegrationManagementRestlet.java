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
	@Path("context/{contextId}/routes")
	@Produces(MediaType.APPLICATION_JSON)
	public String allRoutesListService(@PathParam("contextId") String contextId) {
		return integrationManagement.allRoutesList(contextId);
	}
	
	@GET
	@Path("context/{contextId}/start")
	@Produces(MediaType.TEXT_PLAIN)
	public void startCamelContextService(@PathParam("contextId") String contextId) {
		integrationManagement.startCamelContext(contextId);
	}
	
	@GET
	@Path("context/{contextId}/stop")
	@Produces(MediaType.TEXT_PLAIN)
	public void stopCamelContext(@PathParam("contextId") String contextId) {
		integrationManagement.stopCamelContext(contextId);
	}

	@GET
	@Path("context/{contextId}/startAllRoutes")
	@Produces(MediaType.TEXT_PLAIN)
	public void startAllRoutesService(@PathParam("contextId") String contextId) {
		integrationManagement.startAllRoutes(contextId);
	}
	
	@GET
	@Path("context/{contextId}/stopAllRoutes")
	@Produces(MediaType.TEXT_PLAIN)
	public void stopAllRoutesService(@PathParam("contextId") String contextId) {
		integrationManagement.stopAllRoutes(contextId);
	}

	
	@GET
	@Path("context/{contextId}/routes/producers")
	@Produces(MediaType.APPLICATION_JSON)
	public String producerRoutesListService(@PathParam("contextId") String contextId) {
		return integrationManagement.getProducerRoutesList(contextId);
	}
	
	@GET
	@Path("context/{contextId}/routes/consumers")
	@Produces(MediaType.APPLICATION_JSON)
	public String consumerRoutesListService(@PathParam("contextId") String contextId) {
		return integrationManagement.getConsumerRoutesList(contextId);
	}
	
	
	@GET
	@Path("context/{contextId}/routes/consumers/trigger")
	@Produces(MediaType.APPLICATION_JSON)
	public String triggerConsumerRoutesListtService(@PathParam("contextId") String contextId) {
		return integrationManagement.getTriggerConsumerRoutesList(contextId);
	}
	
	@GET
	@Path("context/{contextId}/routes/consumers/application")
	@Produces(MediaType.APPLICATION_JSON)
	public String applicationConsumerRoutesListService(@PathParam("contextId") String contextId) {
		return integrationManagement.getApplicationConsumerRoutesList(contextId);
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