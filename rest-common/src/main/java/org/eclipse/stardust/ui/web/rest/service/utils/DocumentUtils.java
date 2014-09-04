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
package org.eclipse.stardust.ui.web.rest.service.utils;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Component
public class DocumentUtils
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public Document getDocument(String documentId)
   {
      return serviceFactoryUtils.getDocumentManagementService().getDocument(documentId);
   }

   public int getNumPages(Document document)
   {
      int numPages = 0;

      byte[] data = null;

      String contentType = document.getContentType();
      if (MimeTypesHelper.PDF.getType().equals(contentType))
      {
         data = getDocumentManagementService().retrieveDocumentContent(document.getId());
         numPages = 0; // PdfPageCapture.getNumPages(data); // TODO: Complete
      }
      else if (MimeTypesHelper.TIFF.getType().equals(contentType))
      {
         data = getDocumentManagementService().retrieveDocumentContent(document.getId());
         numPages = 0; // TiffReader.getNumPages(data); // TODO: Complete
      }

      return numPages;
   }

   /**
    * @param documentId
    */
   public DocumentType getDocumentType(String documentId)
   {
      return getDocument(documentId).getDocumentType();
   }

   /**
    * @param documentId
    * @param documentTypeId
    * @param schemaLocation
    * @return
    */
   public Document setDocumentType(String documentId, String documentTypeId,
         String schemaLocation)
   {
      DocumentType documentType = null;
      Document document = getDocumentManagementService().getDocument(documentId);

      if (StringUtils.isNotEmpty(documentTypeId))
      {
         documentType = new DocumentType(documentTypeId, schemaLocation);
      }

      document.setDocumentType(documentType);

      Document updatedDocument = getDocumentManagementService().updateDocument(document,
            false, "", "", false);

      return updatedDocument;
   }

   /**
    * @return
    */
   private DocumentManagementService getDocumentManagementService()
   {
      return serviceFactoryUtils.getDocumentManagementService();
   }

}
