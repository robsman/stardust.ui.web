/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataHandler;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.FileInfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class FileUploadUtils
{
   private static final Logger trace = LogManager.getLogger(FileUploadUtils.class);

   /**
    * @param attachments
    * @return
    * @throws Exception
    */
   public static List<DocumentContentRequestDTO> parseAttachments(List<Attachment> attachments) throws Exception
   {
      List<DocumentContentRequestDTO> documents = new ArrayList<DocumentContentRequestDTO>();
      DocumentContentRequestDTO documentInfoDTO = null;
      String modelId = null;
      for (Attachment attachment : attachments)
      {
         DataHandler dataHandler = attachment.getDataHandler();
         InputStream inputStream = dataHandler.getInputStream();

         if (isFile(attachment.getHeaders()))
         {
            documentInfoDTO = new DocumentContentRequestDTO();
            documentInfoDTO.name = new String(dataHandler.getName().getBytes("ISO-8859-1"), "UTF-8");
            documentInfoDTO.contentType = dataHandler.getContentType();
            documentInfoDTO.contentBytes = readEntryData(inputStream);
            documents.add(documentInfoDTO);
         }
         // following properties can be added from client side as
         // for (var i in files) {
         // formData.append("file", files[i]);
         // formData.append("description", "Description for above file");
         // }
         else if (inputStream instanceof ByteArrayInputStream)
         {
            if (CommonProperties.DESCRIPTION.equals(dataHandler.getName()))
            {
               documentInfoDTO.description = inputStream.toString();
            }
            else if (CommonProperties.COMMENTS.equals(dataHandler.getName()))
            {
               documentInfoDTO.comment = inputStream.toString();
            }
            else if (CommonProperties.PARENT_FOLDER_PATH.equals(dataHandler.getName()))
            {
               documentInfoDTO.parentFolderPath = inputStream.toString();
            }
            else if (CommonProperties.UPLOAD_VERSION.equals(dataHandler.getName()))
            {
               documentInfoDTO.uploadVersion = Boolean.valueOf(inputStream.toString());
            }
            else if (CommonProperties.CREATE_VERSION.equals(dataHandler.getName()))
            {
               documentInfoDTO.createVersion = Boolean.valueOf(inputStream.toString());
            }
            else if (CommonProperties.CREATE_NEW_REVISION.equals(dataHandler.getName()))
            {
               documentInfoDTO.createNewRevision = Boolean.valueOf(inputStream.toString());
            }
            else if (CommonProperties.DOCUMENT_TYPE_ID.equals(dataHandler.getName()))
            {
               // TODO how to detect documentType, it should be always followed by
               // modelId ??
               if (StringUtils.isNotEmpty(modelId))
               {
                  Model model = ModelCache.findModelCache().getActiveModel(modelId);
                  documentInfoDTO.documentType = org.eclipse.stardust.engine.core.runtime.beans.DocumentTypeUtils
                        .getDocumentType(inputStream.toString(), model);
                  modelId = null;
               }
            }
            else if (CommonProperties.MODEL_ID.equals(dataHandler.getName()))
            {
               modelId = inputStream.toString();
            }
            else if (CommonProperties.PROPERTIES.equals(dataHandler.getName()))
            {
               if (documentInfoDTO.properties == null)
               {
                  documentInfoDTO.properties = new HashMap<String, Object>();
               }
               documentInfoDTO.properties.putAll(GsonUtils.readJsonMap(inputStream.toString()));
            }
            else
            {
               trace.warn("Uknown property : " + dataHandler.getName());
            }
         }
      }

      return documents;
   }

   /**
    * @param header
    * @return
    * @throws UnsupportedEncodingException
    * @Deprecated use dataHandler
    */
   public static FileInfoDTO getFileInfo(MultivaluedMap<String, String> header) throws UnsupportedEncodingException
   {
      if (header.getFirst("Content-Disposition") == null)
      {
         return null;
      }

      String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

      FileInfoDTO fileInfo = new FileInfoDTO();
      for (String filename : contentDisposition)
      {
         if ((filename.trim().startsWith("filename")))
         {
            String[] name = filename.split("=");
            fileInfo.name = name[1].trim().replaceAll("\"", "");

            // CXF headers are still in ISO-8859-1. So to handle file containing
            // multi-byte characters in its filename, convert it to UTF-8
            fileInfo.name = new String(fileInfo.name.getBytes("ISO-8859-1"), "UTF-8");
         }
      }

      fileInfo.contentType = header.getFirst("Content-Type");

      return fileInfo;
   }

   /**
    * @param attachment
    * @return
    */
   private static boolean isFile(MultivaluedMap<String, String> header)
   {
      // TODO: is there a better way to check if it is a file?
      String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
      for (String attributeName : contentDisposition)
      {
         if (attributeName.trim().startsWith("filename"))
         {
            return true;
         }
      }
      return false;
   }

   /**
    * @param stream
    * @return
    * @throws IOException
    */
   public static byte[] readEntryData(InputStream stream) throws IOException
   {
      // create a buffer to improve performance
      byte[] buffer = new byte[2048];

      // Once we get the entry from the stream, the stream is
      // positioned read to read the raw data, and we keep
      // reading until read returns 0 or less.
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      try
      {
         int len = 0;
         while ((len = stream.read(buffer)) > 0)
         {
            output.write(buffer, 0, len);
         }
         return output.toByteArray();
      }
      finally
      {
         // must always close the output file
         if (output != null)
         {
            output.close();
         }
      }
   }
}