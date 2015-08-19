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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.stardust.ui.web.rest.service.dto.FileInfoDTO;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class FileUploadUtils
{

   /**
    * @param header
    * @return
    * @throws UnsupportedEncodingException
    */
   public static FileInfoDTO getFileInfo(MultivaluedMap<String, String> header) throws UnsupportedEncodingException
   {
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
    * @param stream
    * @return
    * @throws Exception
    */
   public static byte[] readEntryData(InputStream stream) throws Exception
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
