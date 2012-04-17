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
package org.eclipse.stardust.ui.web.viewscommon.views.correspondence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;


import com.icesoft.faces.component.inputfile.FileInfo;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class Attachment
{
   private Document document;
   private DocumentManagementService documentManagementService;
   private FileInfo fileInfo;
   private boolean containsDocument = false;
   private String documentKey;
   private String description;

   /**
    * @param document
    */
   public Attachment(Document document, DocumentManagementService documentManagementService)
   {
      super();
      this.document = document;
      containsDocument = true;
      documentKey = document.getId();
      this.documentManagementService = documentManagementService;
   }

   /**
    * @param fileInfo
    */
   public Attachment(FileInfo fileInfo)
   {
      super();
      this.fileInfo = fileInfo;
      documentKey = DocumentMgmtUtility.generateUniqueId("ATT-");
   }

   @Override
   public boolean equals(Object obj)
   {
      if (!(obj instanceof Attachment))
      {
         return false;
      }
      else
      {
         Attachment attach1 = (Attachment) obj;
         return attach1.getDocumentKey().equals(this.getDocumentKey());
      }
   }

   public byte[] getContent() throws FileNotFoundException, IOException
   {
      if (isContainsDocument())
      {
         return documentManagementService.retrieveDocumentContent(documentKey);
      }
      else
      {
         return DocumentMgmtUtility.getFileContent(new FileInputStream(fileInfo.getPhysicalPath()));
      }
   }

   public FileInfo getFileInfo()
   {
      return fileInfo;
   }

   public Document getDocument()
   {
      return document;
   }

   public String getName()
   {
      if (isContainsDocument())
      {
         return document.getName();
      }
      else
      {
         return fileInfo.getFileName();
      }
   }

   public boolean isContainsDocument()
   {
      return containsDocument;
   }

   public String getDocumentKey()
   {
      return documentKey;
   }

   public String getContentType()
   {
      if (isContainsDocument())
      {
         return document.getContentType();
      }
      else
      {
         return fileInfo.getContentType();
      }
   }

   public String getDescription()
   {
      return description;
   }
}
