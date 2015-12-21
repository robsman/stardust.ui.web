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
package org.eclipse.stardust.ui.web.rest.service.dto.builder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;

/**
 * @author Anoop.Nair
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentDTOBuilder
{
   /**
    * @param document
    * @param dms
    * @return
    */
   public static DocumentDTO build(Document document, DocumentManagementService dms)
   {
      if (document != null)
      {
         DocumentDTO documentDTO = DTOBuilder.build(document, DocumentDTO.class);
         DocumentTypeDTO documentTypeDTO = DocumentTypeDTOBuilder.build(document.getDocumentType());
         documentDTO.documentType = documentTypeDTO;
         if (dms != null)
         {
            documentDTO.downloadToken = dms.requestDocumentContentDownload(document.getId());
         }
         return documentDTO;
      }
      return null;
   }

   /**
    * @param documents
    * @param dms
    * @return
    */
   public static List<DocumentDTO> build(List<Document> documents, DocumentManagementService dms)
   {
      return build(documents, new Comparator<DocumentDTO>()
      {
         @Override
         public int compare(DocumentDTO documentDTO1, DocumentDTO documentDTO2)
         {
            return documentDTO1.name.compareTo(documentDTO2.name);
         }
      }, dms);
   }

   /**
    * to support custom sorting or turn off default sorting which is based on name
    * @param documents
    * @param comparator
    * @param dms
    * @return
    */
   public static List<DocumentDTO> build(List<Document> documents, Comparator<DocumentDTO> comparator,
         DocumentManagementService dms)
   {
      List<DocumentDTO> documentDTOs = CollectionUtils.newArrayList();

      for (Document document : documents)
      {
         documentDTOs.add(build(document, dms));
      }

      if (comparator != null)
      {
         Collections.sort(documentDTOs, comparator);
      }

      return documentDTOs;
   }

   /**
    * Prevent instantiation
    */
   private DocumentDTOBuilder()
   {}
}
