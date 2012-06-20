/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload;

import java.io.IOException;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementServiceException;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.upload.AbstractDocumentUploadHelper.DocumentUploadCallbackHandler.DocumentUploadEventType;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadCallbackHandler;

/**
 * @author Yogesh.Manware
 * 
 */
public class DocumentDragDropHelper extends AbstractDocumentUploadHelper
{
   private static final long serialVersionUID = 1300859849953775864L;

   /**
    * This method is invoked directly while drag drop operation
    * 
    * @param existingDocument
    * @param draggedDocument
    */
   public void updateDocument(final Document existingDocument, final Document draggedDocument)
   {
      
      //if check if the version can be created
      if (!handleFileAlreadyExistInFolder(null, existingDocument.getName()))
      {
         displayFileAlreadyExistError(existingDocument.getName());
         return;
      }
      
      initializeSaveVersionConfirmationDialog(existingDocument);

      if (!StringUtils.areEqual(existingDocument.getDocumentType(), draggedDocument.getDocumentType()))
      {
         fileUploadHelperDialog.getAttributes().setMessage(
               msgBean.getString("views.genericRepositoryView.fileDragged.documentTypeChanged"));
         fileUploadHelperDialog.getAttributes().setDocumentType(draggedDocument.getDocumentType());
      }

      fileUploadHelperDialog.setCallbackHandler(new FileUploadCallbackHandler()
      {
         public void handleEvent(FileUploadEvent eventType)
         {
            if (eventType == FileUploadEvent.SAVE_CONFIRMED)
            {
               try
               {
                  saveVersion(existingDocument, getFileWrapper(), draggedDocument);
               }
               catch (Exception e)
               {
                  ExceptionHandler.handleException(e);
                  informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
               }
            }
            else
            {
               informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
            }
         }
      });

      fileUploadHelperDialog.openPopup();
   }

   /**
    * @param existingDocument
    * @param fileWrapper
    * @param draggedDocument
    * @throws DocumentManagementServiceException
    * @throws IOException
    */
   private void saveVersion(Document existingDocument, final FileWrapper fileWrapper, final Document draggedDocument)
         throws DocumentManagementServiceException, IOException
   {
      if (checkModifyPrivilege(existingDocument))
      {
         existingDocument.setOwner(draggedDocument.getOwner());
         existingDocument.setContentType(draggedDocument.getContentType());
         existingDocument.setDocumentAnnotations(draggedDocument.getDocumentAnnotations());

         // For document Type evaluation, please refer CRNT-23295
         if (!StringUtils.areEqual(existingDocument.getDocumentType(), draggedDocument.getDocumentType()))
         {
            existingDocument.setDocumentType(draggedDocument.getDocumentType());
            existingDocument.getProperties().clear();
         }

         Document updatedDocument = DocumentMgmtUtility.updateDocument(existingDocument, DocumentMgmtUtility
               .getDocumentManagementService().retrieveDocumentContent(draggedDocument.getId()), fileWrapper
               .getDescription(), fileWrapper.getComments());

         informInitiator(DocumentUploadEventType.VERSION_SAVED, updatedDocument);

         if (fileWrapper.isOpenDocument())
         {
            openDocument(updatedDocument);
         }
      }
      else
      {
         informInitiator(DocumentUploadEventType.UPLOAD_FAILED, null);
      }
   }
}
