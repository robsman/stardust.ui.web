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
   public Response uploadFile(List<Attachment> attachments, @Context HttpServletRequest request)
   {
      JsonObject response = null;
      for (Attachment attachment : attachments)
      {
         DataHandler dataHandler = attachment.getDataHandler();
         try
         {
            InputStream inputStream = dataHandler.getInputStream();
            MultivaluedMap<String, String> headers = attachment.getHeaders();

            FileInfo fileInfo = getFileName(headers);

            FileStorage fileStorage = (FileStorage) RestControllerUtils
                  .resolveSpringBean("fileStorage", servletContext);

            // file path
            String filePath = fileStorage.getStoragePath(servletContext) + fileInfo.name;
            File uploadedFile = new File(filePath);

            OutputStream outputStream = new FileOutputStream(uploadedFile);

            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1)
            {
               outputStream.write(bytes, 0, read);
            }
            inputStream.close();

            outputStream.flush();
            outputStream.close();

            // Store file path in session
            String uuid = fileStorage.pushPath(filePath);

            // get document icon
            MimeTypesHelper mimeTypesHelper = (MimeTypesHelper) RestControllerUtils.resolveSpringBean(
                  "ippMimeTypesHelper", servletContext);
            MIMEType mType = mimeTypesHelper.detectMimeTypeI(fileInfo.name, fileInfo.contentType);

            String docIcon = DOC_PATH + "mime-types/" + mType.getIconPath();

            response = new JsonObject();
            response.add("uuid", new JsonPrimitive(uuid));
            response.add("contentType", new JsonPrimitive(fileInfo.contentType));
            response.add("fileName", new JsonPrimitive(fileInfo.name));
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
            fileInfo.name = name[1].trim().replaceAll("\"", "");
         }
      }

      fileInfo.contentType = header.getFirst("Content-Type");

      return fileInfo;
   }

   /**
    * 
    * @author Yogesh.Manware
    * 
    */
   private class FileInfo
   {
      String name;
      String contentType;

      public FileInfo()
      {
         name = "unknown";
         contentType = "unknown";
      }
   }
}