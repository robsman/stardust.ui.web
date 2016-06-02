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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.service.DocumentTypeService;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.DocumentTypeDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Path("/document-types")
public class DocumentTypeResource
{
   private static final Logger trace = LogManager
         .getLogger(DocumentTypeResource.class);

   @Autowired
   private DocumentTypeService documentTypeService;

   // private final JsonMarshaller jsonIo = new JsonMarshaller();

   /**
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @ResponseDescription("The response will contain list of DocumentTypeDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.DocumentTypeDTO")
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
