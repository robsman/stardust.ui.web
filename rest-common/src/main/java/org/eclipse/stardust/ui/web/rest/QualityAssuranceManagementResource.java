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
package org.eclipse.stardust.ui.web.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.QualityAssuranceManagementService;
import org.eclipse.stardust.ui.web.rest.service.dto.QualityAssuranceActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QualityAssuranceDepartmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.request.QualityAssuranceRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
/**
 * 
 * @author Johnson.Quadras
 *
 */
@Component
@Path("/qualityAssuranceManagement")
public class QualityAssuranceManagementResource
{

   private static final Logger trace = LogManager.getLogger(QualityAssuranceManagementResource.class);

   @Context
   private HttpServletRequest httpRequest;

   @Autowired
   private QualityAssuranceManagementService qualityAssuranceManagementService;

   /**
    * 
    * @return
    */
   @GET
   @Path("/activities")
   public Response getActivities(
         @QueryParam("showObsoleteActivities") @DefaultValue("true") String showObsoleteActivities)
   {
      try
      {
         List<QualityAssuranceActivityDTO> result = qualityAssuranceManagementService.getQaActivities(Boolean
               .valueOf(showObsoleteActivities));
         return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @POST
   @Path("/departments")
   public Response getDepartments(String postData)
   {
      try
      {
         JsonObject json = GsonUtils.readJsonObject(postData);
         String activityQId = GsonUtils.extractString(json, "activityQId");
         String processQId = GsonUtils.extractString(json, "processQId");

         List<QualityAssuranceDepartmentDTO> result = qualityAssuranceManagementService.getDepartments(processQId,
               activityQId);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @POST
   @Path("/updateQaProbabilities")
   public Response updateQaProbability(String postData)
   {
      try
      {
         QualityAssuranceRequestDTO request = DTOBuilder.buildFromJSON(postData, QualityAssuranceRequestDTO.class,
               QualityAssuranceRequestDTO.getCustomTokens());
         qualityAssuranceManagementService.updateQaProbabilities(request);
         return Response.ok(null, MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

}
