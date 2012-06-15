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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.io.Serializable;

import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentToolTip implements ToolTip, Serializable
{
   private static final long serialVersionUID = 1207798997426129373L;
   private static final String DOCUMENT = "document";
   private String name;
   private String documentType;
   private String fileType;
   private long size;
   private String modificationDate;
   private String description;

   /**
    * @param document
    */
   public DocumentToolTip(DocumentType documentType)
   {
      super();

   }

   /**
    * @param documentType
    * @param document
    */
   public DocumentToolTip(DocumentType documentType, Document document)
   {
      super();
      if (null == document)
      {
         this.documentType = TypedDocumentsUtil.getDocumentTypeLabel(documentType);
      }
      else
      {
         this.documentType = TypedDocumentsUtil.getDocumentTypeLabel(document.getDocumentType());
         name = document.getName();
         fileType = MimeTypesHelper.detectMimeType(name, document.getContentType()).getUserFriendlyName();

         size = document.getSize();

         modificationDate = DateUtils.formatDateTime(document.getDateLastModified());

         description = document.getDescription();
         description = description == null || description.length() < 90 ? description : description.substring(0, 89)
               + "...";
      }
   }

   public String getToolTipType()
   {
      return DOCUMENT;
   }

   public String getTitle()
   {
      return name;
   }

   public String getDocumentType()
   {
      return documentType;
   }

   public String getFileType()
   {
      return fileType;
   }

   public long getSize()
   {
      return size;
   }

   public String getModificationDate()
   {
      return modificationDate;
   }

   public String getDescription()
   {
      return description;
   }
}