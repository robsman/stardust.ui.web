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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;


/**
 * Mainly this is FileSystemDocument.
 * When save method is called it saves the file into JCR and 
 * Then from this point onwards it becomes JCRDocument
 * 
 * @author Subodh.Godbole
 */
public class FileSystemJCRDocument extends FileSystemDocument
{
   private static final Logger trace = LogManager.getLogger(FileSystemJCRDocument.class);
   private String jcrParentFolder;

   /**
    * @param resourcePath
    * @param documentType
    * @param jcrParentFolder
    * @param description
    * @param comments
    */
   public FileSystemJCRDocument(String resourcePath, DocumentType documentType, String jcrParentFolder,
         String description, String comments)
   {
      super(resourcePath, documentType, true);
      this.jcrParentFolder = jcrParentFolder;
      this.description = description;
      this.comments = comments;
      this.metaDataEditable = true;
   }

   /**
    * contentBytes are ignored.
    * Reads content from File and creates/saves file in JCR and returns JCRDocument
    */
   @Override
   public IDocumentContentInfo save(byte[] contentBytes)
   {
      return new JCRDocument(createDocument(contentBytes));
   }
   

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemDocument#reset()
    * 
    * The reset methods returns a fresh copy of the file system document.
    * This is needed in case of TIFF viewer as tiff viewer takes a fresh copy and applies 
    * additions made to the annotations / bookmarks "selectively" to this fresh object.
    * 
    * Changing the behaviour of reset method behaviour will affect the save functionality
    * of file system tiff documents.
    */
   @Override
   public IDocumentContentInfo reset()
   {
      /* Please read the docs for this method before making any changes. */
      return new FileSystemJCRDocument(file.getPath(), documentType, jcrParentFolder, description, comments);
   }

   /**
    * @return
    */
   private Document createDocument(byte[] contentBytes)
   {
      Folder typedDocFolder = DocumentMgmtUtility.createFolderIfNotExists(jcrParentFolder);

      Document concreteDocument = null;
      try
      {
         // CHECK if the file with same name already exist in Specific Documents folder
         Document existingDocument = DocumentMgmtUtility.getDocument(jcrParentFolder, file.getName());
         if (null != existingDocument)
         {
            // display error message
            throw new RuntimeException(MessagesViewsCommonBean.getInstance().getParamString(
                  "views.genericRepositoryView.specificDocument.reclassifyDocument.fileAlreadyExist", file.getName()));
         }

         // Create Document with Properties
         concreteDocument = DocumentMgmtUtility.createDocument(typedDocFolder.getId(), file.getName(), contentBytes,
               getDocumentType(), getMimeType().getType(), description, comments, getAnnotations(), getProperties());

         return concreteDocument;
      }
      catch (Exception e)
      {
         if (null != concreteDocument)
         {
            try
            {
               DocumentMgmtUtility.deleteDocumentWithVersions(concreteDocument);
            }
            catch (Exception ex)
            {
               trace.error("Unable to Delete Document", ex);
            }
         }
         throw new RuntimeException(MessagesViewsCommonBean.getInstance().getParamString(
               "views.documentView.createError"), e);
      }
   }
}
