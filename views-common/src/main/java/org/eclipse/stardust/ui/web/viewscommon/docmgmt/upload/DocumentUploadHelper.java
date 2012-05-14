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

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.ProcessAttachmentUserObject;

/**
 * @author Yogesh.Manware
 *
 */
public class DocumentUploadHelper extends AbstractDocumentUploadHelper
{
   private static final long serialVersionUID = 6884319935030733352L;
   

   @Override
   public void initializeVersionUploadDialog(Document existingDocument)
   {
      super.initializeVersionUploadDialog(existingDocument);
      getFileUploadDialogAttributes().setHeaderMessage(
            msgBean.getParamString("views.documentView.saveDocumentDialog.uploadNewVersion.text",
                  existingDocument.getName()));
   }
   
   /*
    * Process Attachment folder may contain a files which are not displayed on Process
    * Instance Details screen. This is because in Document Reclassification, document is
    * logically moved between Process Attachment and Specific Document Folder.
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.docmgmt.AbstractDocumentUploadHelper#
    * isVersionPermissible(org.eclipse.stardust.engine.api.runtime.Folder,
    * java.lang.String)
    */
   @Override
   protected boolean handleFileAlreadyExistInFolder(Folder parentFolder, String fileName)
   {
      boolean allowVersion = true;
      if (null != repositoryResourceUserObject)
      {
         if (repositoryResourceUserObject instanceof ProcessAttachmentUserObject)
         {
            allowVersion = false;
            ProcessAttachmentUserObject attachmentUserObject = (ProcessAttachmentUserObject) repositoryResourceUserObject;
            List<Document> attachmentsList = DMSHelper.fetchProcessAttachments(attachmentUserObject
                  .getProcessInstance());
            for (Document document : attachmentsList)
            {
               if (document.getPath().equals(parentFolder.getPath() + "/" + fileName))
               {
                  allowVersion = true;
                  break;
               }
            }
         }
      }
      return allowVersion;
   }
}
