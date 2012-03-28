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
import java.util.Map;

import org.eclipse.stardust.engine.api.model.TypeDeclaration;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;

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
         this.documentType = getDocumentTypeName(documentType);
      }
      else
      {
         name = document.getName();
         this.documentType = getDocumentTypeName(document.getDocumentType());

         fileType = MimeTypesHelper.detectMimeType(name, document.getContentType()).getUserFriendlyName();

         size = document.getSize();

         modificationDate = DateUtils.formatDateTime(document.getDateLastModified());

         @SuppressWarnings("rawtypes")
         Map properties = document.getProperties();
         description = document.getDescription();
         if (StringUtils.isEmpty(description))
         {
            description = (String) properties.get(CommonProperties.DESCRIPTION);
         }
      }
   }

   /**
    * @param docType
    * @return
    */
   private String getDocumentTypeName(DocumentType docType)
   {
      String dType = "";
      if (null != docType)
      {
         DeployedModel model = ModelUtils.getModelForDocumentType(docType);
         if (null != model)
         {
            TypeDeclaration typeDeclaration = model.getTypeDeclaration(docType);
            if (null != typeDeclaration)
            {
               dType = typeDeclaration.getName();
            }
         }
      }
      return dType;
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