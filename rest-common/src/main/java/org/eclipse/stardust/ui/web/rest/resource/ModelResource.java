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

import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.ModelServiceBean;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ModelDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
@Path("/models")
public class ModelResource
{
   private static final Logger trace = LogManager.getLogger(ModelResource.class);

   @Autowired
   private ModelServiceBean modelService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/")
   public Response getModels(@QueryParam("allActive") @DefaultValue("false") Boolean allActive,
         @QueryParam("includePredefinedModel") @DefaultValue("false") Boolean includePredefinedModel)
   {
      try
      {
         List<ModelDTO> models = modelService.getModels(allActive, includePredefinedModel);
         return Response.ok(AbstractDTO.toJson(models), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }
}
