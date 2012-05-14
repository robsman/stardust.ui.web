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

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.TypedDocument;

/**
 * @author Yogesh.Manware
 *
 */
public class TypedDocumentUploadHelper extends AbstractDocumentUploadHelper
{
   private static final long serialVersionUID = -1564645291758610703L;
   private TypedDocument typedDocument;

   @Override
   public void initializeDocumentUploadDialog()
   {
      super.initializeDocumentUploadDialog();
      getFileUploadDialogAttributes().setHeaderMessage(
            MessagesViewsCommonBean.getInstance().getParamString(
                  "views.genericRepositoryView.specificDocument.uploadFile", typedDocument.getName()));
      setup();
   }

   @Override
   public void initializeVersionUploadDialog(Document existingDocument)
   {
      super.initializeVersionUploadDialog(existingDocument);
      getFileUploadDialogAttributes().setHeaderMessage(
            MessagesViewsCommonBean.getInstance().getParamString(
                  "views.genericRepositoryView.specificDocument.newVersion", typedDocument.getName()));
      setup();
   }

   public void setup()
   {
      getFileUploadDialogAttributes().setDocumentType(typedDocument.getDocumentType());
      getFileUploadDialogAttributes().setOpenDocumentFlag(true);
   }

   /*
    * This method is invoked when uploading a document (not version) from activity panel
    * and the Specific Document folder already contains a file with same name
    */
   @Override
   protected boolean handleFileAlreadyExistInFolder(Folder parentFolder, String fileName)
   {
      return false;
   }

   public void setTypedDocument(TypedDocument typedDocument)
   {
      this.typedDocument = typedDocument;
   }
}
