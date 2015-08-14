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

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
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
         documentDTO.uuid = document.getId();
         documentDTO.name = document.getName();
         documentDTO.path = document.getPath();
         documentDTO.contentType = document.getContentType();

         /*
          * int numPages = documentUtils.getNumPages(document);
          * documentDTO.setNumPages(numPages);
          */

         DocumentTypeDTO documentTypeDTO = DocumentTypeDTOBuilder.build(document.getDocumentType());
         documentDTO.documentType = documentTypeDTO;
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
    * @param folder
    * @return
    */
   public static DocumentDTO build(Folder folder)
   {
      DocumentDTO documentDTO = new DocumentDTO();

      if (folder != null)
      {
         documentDTO.uuid = folder.getId();
         documentDTO.name = folder.getName();
         documentDTO.path = folder.getPath();
         documentDTO.contentType = "folder";
      }

      return documentDTO;
   }

   /**
    * @param folders
    * @return
    */
   public static List<DocumentDTO> buildFolders(List<Folder> folders)
   {
      List<DocumentDTO> documentsDTO = CollectionUtils.newArrayList();

      for (Folder folder : folders)
      {
         documentsDTO.add(build(folder));
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
