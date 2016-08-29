/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.rest.component.service.GanttChartService;
import org.eclipse.stardust.ui.web.rest.dto.GanttChartDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Path("/gantt-chart")
public class GanttChartResource
{
   @Autowired
   private GanttChartService ganttChartService;

   /**
    * @param processOid
    * @return
    * @throws Exception
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("process/{oid}")
   public Response getGanttChartInfo(@PathParam("oid") Long processOid,
         @QueryParam("findAllChildren") @DefaultValue("false") Boolean findAllChildren,
         @QueryParam("fetchRootProcess") @DefaultValue("false") Boolean fetchRootProcess) throws Exception
   {
      
      GanttChartDTO ganttChartInfo = ganttChartService.getGanttChart(processOid,
            fetchRootProcess, findAllChildren);
      return Response.ok(ganttChartInfo.toJson()).build();
   }
}
