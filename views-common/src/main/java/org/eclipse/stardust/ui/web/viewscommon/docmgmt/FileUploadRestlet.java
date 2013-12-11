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

package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.activation.DataHandler;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.ui.web.html5.rest.RestControllerUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;

/**
 * 
 * @author Yogesh.Manware
 * 
 */

@Path("/ippfileupload")
public class FileUploadRestlet
{
   private static final String DOC_PATH = "../../plugins/views-common/images/icons/";

   @Context
   protected ServletContext servletContext;

   @POST
   @Path("upload")
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public Response uploadFile(List<Attachment> attachments,
         @Context HttpServletRequest request)
   {
      JsonObject response = null;
      for (Attachment attr : attachments)
      {
         DataHandler handler = attr.getDataHandler();
         try
         {
            InputStream stream = handler.getInputStream();
            MultivaluedMap<String, String> map = attr.getHeaders();
            FileInfo fileInfo = getFileName(map);
            
            FileStorage fileStorage = (FileStorage) RestControllerUtils.resolveSpringBean("fileStorage", servletContext);
            
            // file path
            // TODO : generate unique ids
            String filePath = fileStorage.getStoragePath(servletContext) + fileInfo.getName();
            File f = new File(filePath);

            OutputStream out = new FileOutputStream(f);

            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = stream.read(bytes)) != -1)
            {
               out.write(bytes, 0, read);
            }
            stream.close();
            out.flush();
            out.close();
           
 
            //Store path in session
            String uuid = fileStorage.pushPath(filePath);

            // document icon
            MimeTypesHelper mimeTypesHelper = (MimeTypesHelper) RestControllerUtils.resolveSpringBean(
                  "ippMimeTypesHelper", servletContext);
            
            MIMEType mType = mimeTypesHelper.detectMimeTypeI(fileInfo.getName(), fileInfo.getContentType());

            String docIcon = DOC_PATH + "mime-types/" + mType.getIconPath();
            
            response = new JsonObject();
            response.add("uuid", new JsonPrimitive(uuid));
            response.add("contentType", new JsonPrimitive(fileInfo.getContentType()));
            response.add("fileName", new JsonPrimitive(fileInfo.getName()));
            response.add("docIcon", new JsonPrimitive(docIcon));
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      if (response != null)
      {
         return Response.ok(response.toString(), MediaType.APPLICATION_JSON_TYPE).build();
      }
      else
      {
         return Response.serverError().build();
      }
   }

   /**
    * @param header
    * @return
    */
   private FileInfo getFileName(MultivaluedMap<String, String> header)
   {
      String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

      FileInfo fileInfo = new FileInfo();
      for (String filename : contentDisposition)
      {
         if ((filename.trim().startsWith("filename")))
         {
            String[] name = filename.split("=");
            String finalFileName = name[1].trim().replaceAll("\"", "");
            fileInfo.setName(finalFileName);
         }
         else if ((filename.trim().startsWith("Content-Type")))
         {
            String[] contentType = filename.split("=");
            String finalContentType = contentType[1].trim().replaceAll("\"", "");
            fileInfo.setContentType(finalContentType);
         }
      }
      fileInfo.setContentType(header.getFirst("Content-Type"));

      return fileInfo;
   }

   private class FileInfo
   {
      String name;

      String contentType;

      public FileInfo()
      {
         name = "unknown";
         contentType = "unknown";
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getContentType()
      {
         return contentType;
      }

      public void setContentType(String contentType)
      {
         this.contentType = contentType;
      }
   }
}