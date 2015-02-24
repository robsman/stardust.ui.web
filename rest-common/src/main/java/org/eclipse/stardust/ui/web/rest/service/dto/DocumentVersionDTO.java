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
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;



/**
 * @author Abhay.Thappan
 * @version $Revision: $
 */
public class DocumentVersionDTO
{
   private static final long serialVersionUID = 1L;
   private String comments;
   private float versionNo;
   private String revisionId;
   //private User user;
   private String documentName;
   private String author = "";
   private String modifiedDate;
   private String documentOwner;
   /**
    * @param version
    * @param document
    */
   public DocumentVersionDTO(int version, Document document)
   {
      this.comments = RepositoryUtility.getVersionComment(document);
      this.versionNo = version;
      this.revisionId = document.getRevisionId();
      this.documentName = document.getName();
      
      User user = DocumentMgmtUtility.getOwnerOfDocument(document);
      this.documentOwner = document.getOwner();
      if (null != user)
      {
         author = FormatterUtils.getUserLabel(user);
      }
      else if (StringUtils.isNotEmpty(document.getOwner()))
      {
         author = document.getOwner();
      }
      this.modifiedDate = DateUtils.formatDateTime(document.getDateLastModified());
   }

   
   public String getAuthor()
   {
      return author;
   }

  /* public String getModifiedDate()
   {
      return DateUtils.formatDateTime(document.getDateLastModified());
   }*/

   public String getComments()
   {
      return comments;
   }

   public String getDocumentName()
   {
      return documentName;
   }
   
   
   public void setDocumentName(String documentName)
   {
      this.documentName = documentName;
   }

   public String getRevisionId()
   {
      return revisionId;
   }

}
