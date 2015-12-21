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
package org.eclipse.stardust.ui.web.rest.service;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentTypeDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentTypeDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.DocumentUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;



/**
 * @author Anoop.Nair
 * @author Abhay.Thappan
 * @version $Revision: $
 */
@Component
public class DocumentService
{

   @Resource
   private DocumentUtils documentUtils;
   
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;
   
   private static final Logger trace = LogManager
			.getLogger(DocumentService.class);

   /**
    * @param documentId
    * @param pageNumber
    * @return
    */
   public byte[] getDocumentImage(String documentId, int pageNumber)
   {
      byte[] content = null;

      Document document = documentUtils.getDocument(documentId);

      if (document != null)
      {
      }

      return content;
   }

   /**
    * @param documentId
    * @return
    */
   public DocumentTypeDTO getDocumentType(String documentId)
   {
      DocumentType documentType = documentUtils.getDocumentType(documentId);

      DocumentTypeDTO documentTypeDTO = DocumentTypeDTOBuilder.build(documentType);

      return documentTypeDTO;
   }

   /**
    * @param documentId
    * @param documentTypeDTO
    * @return
    */
   public DocumentDTO setDocumentType(String documentId, DocumentTypeDTO documentTypeDTO)
   {
      Document updatedDocument = documentUtils.setDocumentType(documentId,
            documentTypeDTO.getDocumentTypeId(), documentTypeDTO.getSchemaLocation());

      DocumentDTO documentDTO = DocumentDTOBuilder.build(updatedDocument,
            serviceFactoryUtils.getDocumentManagementService());

      return documentDTO;
   }
   
   
	/**
	 * @param documentId
	 * @return
	 * @throws Exception
	 */
	public byte[] downloadDocumentDefinition(String documentId) throws Exception {
		try {
			DocumentManagementService documentManagementService = serviceFactoryUtils
					.getDocumentManagementService();
			return documentManagementService
					.retrieveDocumentContent(documentId);
		} catch (Exception e) {
			trace.error("Exception while Download Document Definition " + e, e);
			throw e;
		}
	}
}
