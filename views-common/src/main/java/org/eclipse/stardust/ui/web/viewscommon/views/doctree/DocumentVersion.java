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
package org.eclipse.stardust.ui.web.viewscommon.views.doctree;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class DocumentVersion extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private Document document;
   private String comments;
   private int VersionNo;
   private String revisionId;
   private User user;
   private String documentName;
   private String author = "";
   /**
    * @param version
    * @param document
    */
   public DocumentVersion(int version, Document document)
   {
      this.document = document;
      this.comments = createComments();
      this.VersionNo = version;
      this.revisionId = document.getRevisionId();
      this.documentName = document.getName();
      
      user = DocumentMgmtUtility.getOwnerOfDocument(document);
      if (null != user)
      {
         author = FormatterUtils.getUserLabel(user);
      }
      else if (StringUtils.isNotEmpty(document.getOwner()))
      {
         author = document.getOwner();
      }
   }

   public float getVersionNo()
   {
      return this.VersionNo;
   }

   public String getName()
   {
      return this.document.getName();
   }

   public final User getUser()
   {
      return user;
   }

   public String getAuthor()
   {
      return author;
   }

   public String getModifiedDate()
   {
      return DateUtils.formatDateTime(document.getDateLastModified());
   }

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

   /**
    * pre-processes the document version comments
    * 
    * @return
    */
   private String createComments()
   {
      if (null != document && null != document.getProperties())
      {
         if (document.getProperties().containsKey(CommonProperties.COMMENTS))
            return (String) document.getProperties().get(CommonProperties.COMMENTS);
      }
      return "";
   }

   public String getRevisionId()
   {
      return revisionId;
   }

   public Document getDocument()
   {
      return document;
   }

}
