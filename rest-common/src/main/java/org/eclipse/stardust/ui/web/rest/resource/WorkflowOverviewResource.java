package org.eclipse.stardust.ui.web.rest.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.service.WorkflowOverviewService;
import org.eclipse.stardust.ui.web.rest.dto.WorkflowOverviewCountsDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

@Path("/workflow-overview")
public class WorkflowOverviewResource
{
   public static final Logger trace = LogManager.getLogger(WorkflowOverviewResource.class);
   
   @Autowired
   private WorkflowOverviewService workflowOverviewService;

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/resetCache")
   public Response resetCache( )
   {
      trace.debug("Reseting Participant and Special worklist cache.");
      
      ParticipantWorklistCacheManager.getInstance().reset();
      SpecialWorklistCacheManager.getInstance().reset();
      return Response.ok().build();
   }
   
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
