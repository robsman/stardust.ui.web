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
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
public class DocumentTypeDTOBuilder
{
   public static DocumentTypeDTO build(DocumentType documentType)
   {
      DocumentTypeDTO documentTypeDTO = new DocumentTypeDTO();

      if (documentType != null)
      {
         documentTypeDTO.setDocumentTypeId(documentType.getDocumentTypeId());

         // TODO: Get "name" using name =
         // model.getTypeDeclaration(documentType).getName();
         String documentTypeName = documentType.getDocumentTypeId().substring(
               documentType.getDocumentTypeId().lastIndexOf('}') + 1);
         documentTypeDTO.setName(documentTypeName);

         documentTypeDTO.setSchemaLocation(documentType.getSchemaLocation());
      }

      return documentTypeDTO;
   }

   /**
    * @param documentTypes
    * @return
    */
   public static List<DocumentTypeDTO> build(List<DocumentType> documentTypes)
   {
      List<DocumentTypeDTO> documentTypesDTO = CollectionUtils.newArrayList();

      for (DocumentType documentType : documentTypes)
      {
         documentTypesDTO.add(build(documentType));
      }

      return documentTypesDTO;
   }

   /**
    * Prevent instantiation
    */
   private DocumentTypeDTOBuilder()
   {

   }

}
