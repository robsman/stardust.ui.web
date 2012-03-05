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
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;

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
    */
   public FileSystemJCRDocument(String resourcePath, DocumentType documentType, String jcrParentFolder)
   {
      super(resourcePath, documentType);
      this.jcrParentFolder = jcrParentFolder;
   }

   /**
    * @param resourcePath
    * @param mimeType
    * @param name
    */
   public FileSystemJCRDocument(String resourcePath, MIMEType mimeType, String name, String jcrParentFolder)
   {
      super(resourcePath, mimeType, name);
      this.jcrParentFolder = jcrParentFolder;
   }

   @Override
   public String getId()
   {
      return "unassigned";
   }

   /**
    * contentBytes are ignored.
    * Reads content from File and creates/saves file in JCR and returns JCRDocument
    */
   @Override
   public IDocumentContentInfo save(byte[] contentBytes)
   {
      return new JCRDocument(createDocument());
   }
   

   @Override
   public IDocumentContentInfo reset()
   {
      return new FileSystemJCRDocument(file.getPath(), documentType, jcrParentFolder);
   }

   /**
    * @return
    */
   private Document createDocument()
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
         concreteDocument = DocumentMgmtUtility.createDocument(typedDocFolder.getId(), file.getName(),
               DocumentMgmtUtility.getFileSystemDocumentContent(file.getAbsolutePath()), null, getMimeType().getType(),
               description, comments, null);

         // It's observed that Document Type is not there when document is just created
         // Set it again to UI to work
         concreteDocument.setDocumentType(getDocumentType());

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
         //TODO I18N
         throw new RuntimeException("Unable to create document in JCR", e);
      }
   }
}
