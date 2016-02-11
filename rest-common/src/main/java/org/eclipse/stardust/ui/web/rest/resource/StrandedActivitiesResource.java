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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.resource;

import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.component.service.StrandedActivitiesService;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/strandedActivities")
public class StrandedActivitiesResource
{
   private static final Logger trace = LogManager.getLogger(StrandedActivitiesResource.class);

   @Resource
   private StrandedActivitiesService strandedActivitiesService;

   @Autowired
   ProcessDefinitionService processDefService;

   /**
    * 
    * @param skip
    * @param pageSize
    * @param orderBy
    * @param orderByDir
    * @param postData
    * @return
    */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   public Response getStrandedActivities(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("14") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("activityOID") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = strandedActivitiesService.getStrandedActivities(options);

         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
    * 
    * @param options
    * @param postData
    */

   private void populatePostData(DataTableOptionsDTO options, String postData)
   {
      List<DescriptorColumnDTO> availableDescriptors = processDefService.getDescriptorColumns(true);
      ActivityTableUtils.populatePostData(options, postData, availableDescriptors);
   }
}
