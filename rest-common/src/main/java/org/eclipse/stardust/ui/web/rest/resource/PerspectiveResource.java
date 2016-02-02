/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.PerspectiveService;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.PerspectiveDTO;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 *
 */
@Path("/perspectives")
public class PerspectiveResource
{
   private static final Logger trace = LogManager.getLogger(PerspectiveResource.class);

   @Autowired
   private PerspectiveService perspectiveService;

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response getPerspectiveInfo()
   {
      try
      {
         List<PerspectiveDTO> perspectives = perspectiveService.getPerspectives();
         return Response.ok(AbstractDTO.toJson(perspectives), MediaType.APPLICATION_JSON).build();
      }
      catch (Throwable t)
      {
         trace.error("Unexpected Error while fetching list of perspectives", t);
         return Response.ok("[]", MediaType.APPLICATION_JSON).build();
      }
   }
}
