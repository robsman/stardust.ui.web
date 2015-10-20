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
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.spi.dms.RepositoryIdUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.eclipse.stardust.ui.web.viewscommon.common.DocumentToolTip;
import org.eclipse.stardust.ui.web.viewscommon.common.ToolTip;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.TypedDocumentsUtil;

@DTOClass
public class DocumentSearchResultDTO extends AbstractDTO
{
   public Long createDate = null;

   public Long modificationDate = null;

   public String author = "";

   public String containingText;

   public String fileType;

   public String documentType;

   public List<Pair<String, String>> metadata;

   public String documentId;

   public String documentName;

   public String documentPath;

   public String repositoryId;

   public Long userOID;

   public long fileSize;

   public ToolTip documentToolTip;

   // ~ Constructor
   // ================================================================================================

   public DocumentSearchResultDTO(Document doc)
   {
      this.documentId = doc.getId();
      this.documentName = doc.getName();
      this.fileType = doc.getContentType();
      this.documentType = TypedDocumentsUtil.getDocumentTypeLabel(doc.getDocumentType());
      this.createDate = doc.getDateCreated().getTime();
      this.modificationDate = doc.getDateLastModified().getTime();
      this.fileSize = doc.getSize();
      this.documentPath = getFolderFromFullPath(doc.getPath());
      this.repositoryId = RepositoryIdUtils.extractRepositoryId(doc);
      
      User user = DocumentMgmtUtility.getOwnerOfDocument(doc);
      
      if (null != user)
      {  userOID = user.getOID();
         author = FormatterUtils.getUserLabel(user);
      }
      else if (StringUtils.isNotEmpty(doc.getOwner()))
      {
         author = doc.getOwner();
      }

      documentToolTip = new DocumentToolTip(null, doc);

      metadata = TypedDocumentsUtil.getMetadataAsList(doc, true);
      if (metadata.size() > 5) // Requirement is to only show first 5 entries
      {
         metadata = metadata.subList(0, 5);
      }

   }

   public DocumentSearchResultDTO()
   {
      // TODO Auto-generated constructor stub
   }

   /**
    * method returns folder path from full document path.
    * 
    * @param fullPath
    * @return
    */
   private static String getFolderFromFullPath(String fullPath)
   {
      if (StringUtils.isNotEmpty(fullPath))
      {
         int lastSeperator = fullPath.lastIndexOf("/");
         return fullPath.substring(0, lastSeperator);
      }
      return fullPath;
   }
}
