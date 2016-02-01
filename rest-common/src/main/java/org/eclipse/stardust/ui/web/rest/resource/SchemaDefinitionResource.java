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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.Base64;
import org.eclipse.stardust.ui.web.rest.service.SchemaDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;

@Path("/schema-definition")
public class SchemaDefinitionResource
{

   @Autowired
   private SchemaDefinitionService typeDeclarationService;

   @GET
   @Produces(MediaType.APPLICATION_OCTET_STREAM)
   public Response getSchemaDefinition(@QueryParam("modelOid") long modelOid,
         @QueryParam("typeDeclarationId") String typeDeclarationId)
   {
      byte[] contents = typeDeclarationService.getSchemaDefinition(modelOid, typeDeclarationId);
      String schemaDefinition = new String(Base64.encode(contents));
      return Response.ok(schemaDefinition, MediaType.APPLICATION_JSON).build();
   }
}
