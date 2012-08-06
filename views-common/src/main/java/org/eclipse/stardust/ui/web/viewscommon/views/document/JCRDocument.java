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

import java.io.File;
import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrivilege;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DMSHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.FormatterUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;


/**
 * Represents JCR Document
 * 
 *  @author Yogesh.Manware
 * 
 * code to set documentID is added specially to always have latest file. Consider the case if
 * file has 2 revisions and the file gets updated at base version from non-portal
 * application.
 * 
 * Fetched along with docRevisions or with revision id -> {documentAnnotations={},
 * properties={faxEMailMessageInfo={}, comments=}, contentType=text/plain,
 * dateLastModified=Fri Dec 23 16:35:56 IST 2011,
 * revisionId={jcrRev}03f8c815-c873-47c0-bcce-ad8b1c815078, size=54,
 * id={jcrUuid}a58c0a09-e2dc-44fd-a3ef-e281cdf02ef9, name=a.txt, versionLabels=[3],
 * path=/realms/carnot/users/motu/documents/a.txt, owner=motu, dateCreated=Fri Dec 16
 * 16:56:08 IST 2011, revisionName=1.2}
 * 
 * Fetched with DocumentId -> {documentAnnotations={},
 * properties={faxEMailMessageInfo={}, comments=}, contentType=text/plain,
 * dateLastModified=Fri Dec 23 16:52:21 IST 2011,
 * revisionId={jcrRev}03f8c815-c873-47c0-bcce-ad8b1c815078, size=84,
 * id={jcrUuid}a58c0a09-e2dc-44fd-a3ef-e281cdf02ef9, name=a.txt, versionLabels=[3],
 * path=/realms/carnot/users/motu/documents/a.txt, owner=motu, dateCreated=Fri Dec 16
 * 16:56:08 IST 2011, revisionName=1.2}
 * 
 * If we add a revision through portal, base version + latest revision gets updated.
 * But if we just update the base version (from non-portal application) then latest
 * revision does not get updated which is shown in above case. check the
 * dateLastModified and file size, for latest version always get the base version copy.
 * 
 * Note: if we create a file in JCR and if it supports revisions. In JCR 2 copies of
 * file gets created 1. Base copy (pointed by Id) and 2. revision copy (pointed by
 * revisionID)
 * 
 *
 * 
 */
public class JCRDocument extends AbstractDocumentContentInfo
{
   private Document document;
   
   /**
    * @param document
    */
   public JCRDocument(Document document)
   {
      this(document, false);
   }
   
   
   /**
    * @param documentId
    * @throws ResourceNotFoundException
    */
   public JCRDocument(String documentId) throws ResourceNotFoundException
   {
      this(DocumentMgmtUtility.getDocument(documentId), false);
   }

   /**
    * @param document
    * @param readOnly
    */
   public JCRDocument(Document document, boolean readOnly)
   {
      this(document, readOnly, null);
   }

   /**
    * package scope
    * 
    * @param doc
    * @param vTracker
    */
   JCRDocument(Document doc, JCRVersionTracker vTracker)
   {
      this(doc, false, vTracker);
   }
   
   /**
    * package scope
    * 
    * @param doc
    * @param readOnly
    * @param vTracker
    */
   JCRDocument(Document doc, boolean readOnly, JCRVersionTracker vTracker)
   {
      initialize(doc, readOnly, vTracker);
   }
   
   public void initialize(Document doc, boolean readOnly, JCRVersionTracker vTracker)
   {
      this.document = doc;
      supportVersioning = true;
      if (null == vTracker)
      {
         this.versionTracker = new JCRVersionTracker(document);
      }
      else
      {
         this.versionTracker = vTracker;
      }

      name = document.getName();

      mimeType = MimeTypesHelper.detectMimeType(document.getName(), document.getContentType());
      properties = document.getProperties();

      description = RepositoryUtility.getDescription(document);
      comments = RepositoryUtility.getVersionComment(document);
      annotations = document.getDocumentAnnotations();
      documentType = document.getDocumentType();
      
      if (readOnly)
      {
         modifyPrivilege = false;
      }
      else
      {
         modifyPrivilege = DMSHelper.hasPrivilege(getDocument().getId(), DmsPrivilege.MODIFY_PRIVILEGE);
      }
      
      id = document.getId();
      if (!versionTracker.isLatestVersion())
      {
         id = document.getRevisionId();
      }
      idLabel = id;
      
      contentEditable = modifyPrivilege && null != versionTracker && versionTracker.isLatestVersion();
      metaDataEditable = contentEditable;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo#reset()
    */
   public JCRDocument reset() throws ResourceNotFoundException
   {
     initialize(DocumentMgmtUtility.getDocument(this.document.getId()), false, null);
     content = null;
     return this;
   }

   /**
    * initURL
    */
   private void initURL()
   {
      try
      {
         url = "/dms-content/"
               + DocumentMgmtUtility.getDocumentManagementService().requestDocumentContentDownload(getId());
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
         url = "";
      }
   }

   /**
    * initContent
    */
   private void initContent()
   {
      try
      {
         content = DocumentMgmtUtility.getDocumentManagementService().retrieveDocumentContent(getId());
      }
      catch (Exception e)
      {
         MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
         content = propsBean.getString("views.documentView.message.unsupportedContentMsg").getBytes();
      }
   }

   /**
    * initAuthor
    */
   private void initAuthor()
   {
      // set auther
      User user = DocumentMgmtUtility.getOwnerOfDocument(document);
      if (null != user)
      {
         author = FormatterUtils.getUserLabel(user);
      }
      else if (StringUtils.isNotEmpty(document.getOwner()))
      {
         author = document.getOwner();
      }
   }

   public byte[] retrieveContent()
   {
      if (null == content)
      {
         initContent();
      }
      return content;
   }

   public String getURL()
   {
      if (null == url)
      {
         initURL();
      }
      return url;
   }

   public Document getDocument()
   {
      return document;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo#save(byte[])
    */
   public IDocumentContentInfo save(byte[] contentByte)
   {
      if (!DocumentMgmtUtility.isDocumentVersioned(this.document))
      {
         this.document = DocumentMgmtUtility.getDocumentManagementService().versionDocument(this.document.getId(), "", null);
      }

      this.document.setProperties(getProperties());
      this.document.setDescription(description);
      this.document.setDocumentType(documentType);
      
      this.document.setDocumentAnnotations(annotations);
      this.document.setOwner(DocumentMgmtUtility.getUser().getAccount());
      
      Document document = DocumentMgmtUtility.getDocumentManagementService().updateDocument(this.document, contentByte,
            "", true, comments, null, false);
      return new JCRDocument(document, new JCRVersionTracker(document));
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.AbstractDocumentContentInfo#saveFile(java.lang.String)
    */
   public IDocumentContentInfo saveFile(String physicalPath)
   {
      document.setName(org.eclipse.stardust.ui.web.viewscommon.utils.StringUtils.substringAfterLast(physicalPath, File.separator));
      byte[] contentByte = DocumentMgmtUtility.getFileSystemDocumentContent(physicalPath);
      return save(contentByte);
   }
   
   public String getAuthor()
   {
      if (null == author)
      {
         initAuthor();
      }
      return author;
   }

   public Date getDateLastModified()
   {
      return document.getDateLastModified();
   }

   public Date getDateCreated()
   {
      return document.getDateCreated();
   }

   public long getSize()
   {
      return document.getSize();
   }
}