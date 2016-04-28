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
package org.eclipse.stardust.ui.web.rest.dto.builder;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.rest.component.cachemanager.UserAttributesCacheManager;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentTypeDTO;
import org.eclipse.stardust.ui.web.rest.dto.request.DetailLevelDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;

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
      return build(document, dms, null);
   }

   /**
    * @param document
    * @param dms
    * @param detailLevelDTO
    * @param userService
    * @return
    */
   public static DocumentDTO build(Document document, DocumentManagementService dms,
         DetailLevelDTO detailLevelDTO)
   {
      if (document != null)
      {
         DocumentDTO documentDTO = DTOBuilder.build(document, DocumentDTO.class);
         documentDTO.description = document.getDescription();
         DocumentTypeDTO documentTypeDTO = DocumentTypeDTOBuilder.build(document.getDocumentType());
         documentDTO.documentType = documentTypeDTO;
         if (dms != null)
         {
            documentDTO.downloadToken = dms.requestDocumentContentDownload(document.getId());
         }

         documentDTO.properties = new HashMap<String, Object>();
         documentDTO.properties.putAll(document.getProperties());

         // set optional details
         setDocumentData(detailLevelDTO, documentDTO, document);

         return documentDTO;
      }
      return null;
   }

   /**
    * @param detailLevelDTO
    * @param documentDTO
    * @param document
    */
   private static void setDocumentData(DetailLevelDTO detailLevelDTO, DocumentDTO documentDTO, Document document)
   {
      if (detailLevelDTO != null && detailLevelDTO.DocumentDataDetailsLevel != null)
      {
         documentDTO.documentData = TypedDocumentsUtil.getMetadataAsList(document, true);
         if (detailLevelDTO.DocumentDataDetailsLevel.equals("minimal"))
         {
            if (documentDTO.documentData.size() > 5) // Requirement is to only show
                                                     // first 5 entries
            {
               documentDTO.documentData = documentDTO.documentData.subList(0, 5);
            }
         }// else full details
      }
   }

   /**
    * @param documents
    * @param dms
    * @return
    */
   public static List<DocumentDTO> build(List<Document> documents, DocumentManagementService dms)
   {
      return build(documents, dms, null, null);
   }

   /**
    * @param documents
    * @param dms
    * @return
    */
   public static List<DocumentDTO> build(List<Document> documents, DocumentManagementService dms,
         DetailLevelDTO detailLevelDTO, UserAttributesCacheManager userCache)
   {
      return build(documents, new Comparator<DocumentDTO>()
      {
         @Override
         public int compare(DocumentDTO documentDTO1, DocumentDTO documentDTO2)
         {
            return documentDTO1.name.compareTo(documentDTO2.name);
         }
      }, dms, detailLevelDTO, userCache);
   }
   
   /**
    * to support custom sorting or turn off default sorting which is based on name
    * 
    * @param documents
    * @param comparator
    * @param dms
    * @return
    */
   public static List<DocumentDTO> build(List<Document> documents, Comparator<DocumentDTO> comparator,
         DocumentManagementService dms)
   {
      return build(documents, comparator, dms, null, null);
   }

   /**
    * @param documents
    * @param comparator
    * @param dms
    * @param detailLevelDTO
    * @param userService
    * @return
    */
   public static List<DocumentDTO> build(List<Document> documents, Comparator<DocumentDTO> comparator,
         DocumentManagementService dms, DetailLevelDTO detailLevelDTO, UserAttributesCacheManager userCache)
   {
      List<DocumentDTO> documentDTOs = CollectionUtils.newArrayList();

      for (Document document : documents)
      {
         documentDTOs.add(build(document, dms, detailLevelDTO));
      }

      if (comparator != null)
      {
         Collections.sort(documentDTOs, comparator);
      }

      // Optional Details
      // set user details
      if (StringUtils.isNotEmpty(detailLevelDTO.userDetailsLevel))
      {
         setModifierDetails(documentDTOs, userCache);
      }
      return documentDTOs;
   }

   /**
    * @param documentDTOs
    * @param userCache
    */
   public static void setModifierDetails(List<DocumentDTO> documentDTOs,
         UserAttributesCacheManager userCache)
   {
      // set Owner details
      {
         for (DocumentDTO documentDTO : documentDTOs)
         {
            documentDTO.ownerDetails = userCache.getUserAttributes(documentDTO.owner, false);
         }
      }
   }

   /**
    * Prevent instantiation
    */
   private DocumentDTOBuilder()
   {}
}
