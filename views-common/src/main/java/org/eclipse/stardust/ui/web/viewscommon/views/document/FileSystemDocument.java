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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.engine.extensions.dms.data.DocumentType;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.FileSystemDocumentServlet;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;


/**
 * Represents server side document
 * 
 * @author Yogesh.Manware
 * 
 */
public class FileSystemDocument extends AbstractDocumentContentInfo
{
   private static final long serialVersionUID = -2797207925644726903L;
   protected File file;

   public FileSystemDocument(String resourcePath, DocumentType documentType, boolean editable)
   {
      file = new File(resourcePath);
      this.documentType = documentType;
      this.name = file.getName();
      mimeType = MimeTypesHelper.detectMimeType(this.name, "");
      this.contentEditable = editable;
      init();
   }
   
   public FileSystemDocument(FileSystemDocumentAttributes attributes)
   {
      this.metaDataEditable = false;
      this.showDetails = false;
      
      file = new File(attributes.getResourcePath());
      this.documentType = attributes.getDocumentType();
      this.name = file.getName();
      mimeType = attributes.getMimeType();
      this.contentEditable = attributes.isEditable();
      author = attributes.getDefaultAuthor();
      id = file.getPath();
      idLabel = attributes.getDefaultIdLabel();
      properties = new HashMap<String, Object>();
   }

   private void init()
   {
      this.metaDataEditable = false;
      this.showDetails = false;
      MessagesViewsCommonBean viewBean = MessagesViewsCommonBean.getInstance();
      author = viewBean.getString("views.documentView.properties.author.default");
      id = file.getPath();
      idLabel = viewBean.getString("views.documentView.properties.id.default");
      properties = new HashMap<String, Object>();
   }

   private void initURL()
   {
      try
      {
         UserService service = ServiceFactoryUtils.getUserService();
         User user = service.getUser();
         
         ExternalContext ectx = FacesContext.getCurrentInstance().getExternalContext();
         HttpSession session = (HttpSession) ectx.getSession(false);
         String sessionID = session.getId();
         
         String downloadtoken = FileSystemDocumentServlet.encodeFSDServletToken(file.getPath(), user.getOID(), getMimeType().getType(),sessionID);

         url = "/fsd-content/" + downloadtoken;
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
         url = "";
      }
   }

   public String getURL()
   {
      if (null == url)
      {
         initURL();
      }
      return url;
   }

   public byte[] retrieveContent()
   {
      if (null == content)
      {
         content = DocumentMgmtUtility.getFileSystemDocumentContent(file.getPath());
      }
      return content;
   }

   public IDocumentContentInfo save(byte[] contentBytes)
   {
      FileOutputStream fos;
      try
      {
         fos = new FileOutputStream(file);
         fos.write(contentBytes);
         fos.close();
      }
      catch (IOException e)
      {
         ExceptionHandler.handleException(e);
      }
      return new FileSystemDocument(getId(), documentType, isContentEditable());
   }

   public boolean delete()
   {
      return this.file.delete();
   }
   
   public Date getDateLastModified()
   {
      return new Date(file.lastModified());
   }

   public Date getDateCreated()
   {
      return new Date(file.lastModified());
   }

   public long getSize()
   {
      return file.length();
   }

   public IDocumentContentInfo reset()
   {
      return this;
   }
   
   public static class FileSystemDocumentAttributes
   {
      private String resourcePath;

      private DocumentType documentType;

      private boolean editable;

      private MIMEType mimeType;

      private String defaultAuthor;

      private String defaultIdLabel;
      
      public String getResourcePath()
      {
         return resourcePath;
      }

      public void setResourcePath(String resourcePath)
      {
         this.resourcePath = resourcePath;
      }

      public DocumentType getDocumentType()
      {
         return documentType;
      }

      public void setDocumentType(DocumentType documentType)
      {
         this.documentType = documentType;
      }

      public boolean isEditable()
      {
         return editable;
      }

      public void setEditable(boolean editable)
      {
         this.editable = editable;
      }

      public MIMEType getMimeType()
      {
         return mimeType;
      }

      public void setMimeType(MIMEType mimeType)
      {
         this.mimeType = mimeType;
      }

      public String getDefaultAuthor()
      {
         return defaultAuthor;
      }

      public void setDefaultAuthor(String defaultAuthor)
      {
         this.defaultAuthor = defaultAuthor;
      }

      public String getDefaultIdLabel()
      {
         return defaultIdLabel;
      }

      public void setDefaultIdLabel(String defaultIdLabel)
      {
         this.defaultIdLabel = defaultIdLabel;
      }
   }
}
