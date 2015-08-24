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
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataHandler;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.ui.web.rest.service.dto.FileInfoDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DocumentInfoDTO;
import org.eclipse.stardust.ui.web.viewscommon.core.CommonProperties;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class FileUploadUtils
{
   public static String DOCUMENT_TYPE_ID = "documentTypeId";
   public static String MODEL_ID = "modelId";
   public static String PARENT_FOLDER_PATH = "parentFolderPath";

   /**
    * @param attachments
    * @return
    * @throws Exception
    */
   public static List<DocumentInfoDTO> parseAttachments(List<Attachment> attachments) throws Exception
   {
      List<DocumentInfoDTO> documents = new ArrayList<DocumentInfoDTO>();
      DocumentInfoDTO documentInfoDTO = null;
      String modelId = null;
      for (Attachment attachment : attachments)
      {
         DataHandler dataHandler = attachment.getDataHandler();
         InputStream inputStream = dataHandler.getInputStream();

         System.out.println("Name: " + dataHandler.getName());
         int i1 = 1;
         System.out.println(i1++ + "content: " + attachment.getContentId());
         System.out.println(i1++ + "content: " + attachment.getContentDisposition());
         System.out.println(i1++ + "content: " + attachment.getContentType());
         if (isFile(attachment))
         {
            documentInfoDTO = new DocumentInfoDTO();
            documentInfoDTO.name = dataHandler.getName();
            documentInfoDTO.contentType = dataHandler.getContentType();
            documentInfoDTO.content = readEntryData(inputStream);
            documents.add(documentInfoDTO);
         }
         else if (inputStream instanceof ByteArrayInputStream)
         {
            if (CommonProperties.DESCRIPTION.equals(dataHandler.getName()))
            {
               documentInfoDTO.description = inputStream.toString();
            }
            else if (CommonProperties.COMMENTS.equals(dataHandler.getName()))
            {
               documentInfoDTO.comments = inputStream.toString();
            }
            else if (PARENT_FOLDER_PATH.equals(dataHandler.getName()))
            {
               documentInfoDTO.parentFolderPath = inputStream.toString();
            }
            else if (DOCUMENT_TYPE_ID.equals(dataHandler.getName()))
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
            else if (MODEL_ID.equals(dataHandler.getName()))
            {
               modelId = inputStream.toString();
            }
            else
            {
               if (documentInfoDTO.properties == null)
               {
                  documentInfoDTO.properties = new HashMap<String, Serializable>();
               }
               documentInfoDTO.properties.put(dataHandler.getName(), inputStream.toString());
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
   private static boolean isFile(Attachment attachment)
   {
      // could not find other way to detect if the attachment is file
      if (attachment.getContentDisposition() != null
            && attachment.getContentDisposition().getParameter("filename") != null)
      {
         return true;
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