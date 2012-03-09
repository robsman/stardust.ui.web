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
   protected File file;
   protected String id;
   
   public FileSystemDocument(String resourcePath, DocumentType documentType, boolean editable)
   {
      file = new File(resourcePath);
      this.documentType = documentType;
      this.name = file.getName();
      mimeType = MimeTypesHelper.detectMimeType(getName(), "");
      this.contentEditable = editable;
      this.metaDataEditable = false;
      init();
   }

   private void init()
   {
      MessagesViewsCommonBean viewBean = MessagesViewsCommonBean.getInstance();
      author = viewBean.getString("views.documentView.properties.author.default");
      id = viewBean.getString("views.documentView.properties.id.default");
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

   public String getId()
   {
      return id;
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
      return new FileSystemDocument(file.getPath(), documentType, isContentEditable());
   }
}
