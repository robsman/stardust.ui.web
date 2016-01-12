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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
@Path("/session")
public class SessionResource
{
   
   @Autowired
   private HttpServletRequest httpRequest;
   
   @GET
   @Path("/ping")
   public Response getActivityInstance()
   {
      return Response.ok().build();
   }
   
   @DELETE
   @Path("/invalidate")
   public Response invalidate()
   {
      HttpSession session = httpRequest.getSession();
      session.invalidate();
      return Response.ok().build();
   }
}
