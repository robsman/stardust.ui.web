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
package org.eclipse.stardust.ui.web.rest.service.dto.builder;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class DocumentDTOBuilder
{
   public static DocumentDTO build(Document document)
   {
      DocumentDTO documentDTO = new DocumentDTO();

      if (document != null)
      {
         documentDTO.setUuid(document.getId());
         documentDTO.setName(document.getName());
         documentDTO.setPath(document.getPath());
         documentDTO.setContentType(document.getContentType());

         /*
          * int numPages = documentUtils.getNumPages(document);
          * documentDTO.setNumPages(numPages);
          */

         DocumentTypeDTO documentTypeDTO = DocumentTypeDTOBuilder.build(document
               .getDocumentType());
         documentDTO.setDocumentType(documentTypeDTO);
      }

      return documentDTO;
   }

   /**
    * @param documents
    * @return
    */
   public static List<DocumentDTO> build(List<Document> documents)
   {
      List<DocumentDTO> documentsDTO = CollectionUtils.newArrayList();

      for (Document document : documents)
      {
         documentsDTO.add(build(document));
      }

      return documentsDTO;
   }

   /**
    * Prevent instantiation
    */
   private DocumentDTOBuilder()
   {

   }

}
