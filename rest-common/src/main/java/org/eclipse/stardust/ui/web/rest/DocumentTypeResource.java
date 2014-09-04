/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.DocumentTypeService;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
@Path("/portal/document-types")
public class DocumentTypeResource
{
   private static final Logger trace = LogManager
         .getLogger(ActivityInstanceResource.class);

   @Autowired
   private DocumentTypeService documentTypeService;

   // private final JsonMarshaller jsonIo = new JsonMarshaller();

   /**
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Response getDocumentTypes()
   {
      try
      {
         Gson gson = new Gson();

         List<DocumentTypeDTO> documentTypesDTO = getDocumentTypeService()
               .getDocumentTypes();
         String jsonOutput = gson.toJson(documentTypesDTO);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @return
    */
   public DocumentTypeService getDocumentTypeService()
   {
      return documentTypeService;
   }

   /**
    * @param DocumentTypeService
    */
   public void setDocumentTypeService(DocumentTypeService documentTypeService)
   {
      this.documentTypeService = documentTypeService;
   }
}
