package org.eclipse.stardust.ui.web.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.WorkflowOverviewService;
import org.eclipse.stardust.ui.web.rest.service.dto.WorkflowOverviewCountsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/workflow-overview")
public class WorkflowOverviewResource
{
   @Autowired
   private WorkflowOverviewService workflowOverviewService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/counts")
   public Response getOverviewCounts()
   {
      String directUserWorkCount = workflowOverviewService.getDirectUserWorkCount();
      String criticalActivitiesCount = workflowOverviewService.getCriticalActivitiesCount();
      String assignedActivitiesCount = workflowOverviewService.getAllAssignedActivitiesCount();
      
     
      WorkflowOverviewCountsDTO workflowOverviewCountsDTO = new WorkflowOverviewCountsDTO();
      
      workflowOverviewCountsDTO.directUserWorkCount = directUserWorkCount;
      workflowOverviewCountsDTO.assignedActivitiesCount = assignedActivitiesCount;
      workflowOverviewCountsDTO.criticalActivitiesCount = criticalActivitiesCount;
      
      return Response.ok(GsonUtils.toJson(workflowOverviewCountsDTO), MediaType.APPLICATION_JSON).build();
   }

}
