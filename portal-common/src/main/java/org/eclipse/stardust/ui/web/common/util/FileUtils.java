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
package org.eclipse.stardust.ui.web.common.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Shrikant.Gangal
 * 
 */
public class FileUtils
{
   public static final String ZIP_FILE = ".zip";
   public static String MIME_TEXT_XML = "text/xml";
   public final static String XPDL_FILE = ".xpdl";

   /**
    * Closes the Closeable resource (input/output stream) if it's not null. Ignores any
    * exception thrown while closing the stream.
    * 
    * @see Closeable
    * @param resource
    */
   public static void close(Closeable resource)
   {
      if (resource != null)
      {
         try
         {
            resource.close();
         }
         catch (Exception e)
         {
            // e.printStackTrace();
         }
      }
   }

   /**
    * Deletes the file at the given file path.
    */
   public static boolean deleteFile(final String filePath)
   {
      try
      {
         File file = new File(filePath);

         return file.delete();
      }
      catch (Exception e)
      {
         // Catch-all to avoid any issues on account of any random run-time exception
         return false;
      }
   }

   /**
    * method return byte array for given file name.
    * 
    * @param fileName
    * @param filePath
    * @return
    * @throws Exception
    */
   public static byte[] fileBytesFromZip(final String fileName, final String filePath) throws Exception
   {
      byte[] documentContent = null;
      ZipInputStream zipStream = null;

      try
      {
         zipStream = getZipInputStream(filePath);

         // now iterate through each item in the stream. The get next
         // entry call will return a ZipEntry for each file in the
         // stream
         ZipEntry entry = null;

         while ((entry = zipStream.getNextEntry()) != null)
         {
            if (entry.getName().equals(fileName))
            {
               documentContent = readEntryData(zipStream);

               break;
            }
         }
      }
      finally
      {
         // we must always close the stream.
         // closing the one which was the last to be constructed in chain.
         close(zipStream);
      }

      return documentContent;
   }

   /**
    * Reads the given file and returns a byte array.
    * 
    * @param fileName
    *           - file to be read
    * @return - byte array of the file read.
    * 
    * @throws IOException
    *            - in case the file is too large (greater than Integer.MAX_VALUE number of
    *            bytes) or some other IOException occurs.
    */
   public static byte[] fileToBytes(final String fileName) throws IOException
   {
      FileInputStream ipStream = null;

      try
      {
         File file = new File(fileName);
         long fileLength = file.length();

         if (fileLength > Integer.MAX_VALUE)
         {
            throw new IOException("File too large. Please select a smaller file.");
         }

         ipStream = new FileInputStream(file);

         byte[] bytes = new byte[(int) file.length()];

         int bytesRead = ipStream.read(bytes);

         if (bytesRead < fileLength)
         {
            throw new IOException("File not read correctly.");
         }

         return bytes;
      }
      catch (IOException e)
      {
         throw e;
      }
      finally
      {
         close(ipStream);
      }
   }

   /**
    * method returns list of file names contain in zip file.
    * 
    * @param fileName
    * @return
    * @throws Exception
    */
   public static List<String> getFileNamesFromZip(final String filePath) throws Exception
   {
      List<String> fileNames = new ArrayList<String>();
      ZipInputStream zipStream = null;

      try
      {
         zipStream = getZipInputStream(filePath);

         ZipEntry entry = null;

         while ((entry = zipStream.getNextEntry()) != null)
         {
            fileNames.add(entry.getName());
         }
      }
      finally
      {
         // we must always close the stream.
         // closing the one which was the last to be constructed in chain.
         close(zipStream);
      }

      return fileNames;
   }

   /**
    * method returns list of byte[] ,as zip may contain list of files.
    * 
    * @param inputStream
    * @return
    * @throws Exception
    */
   public static Map<String, byte[]> zipToBytesMap(final String filePath) throws Exception
   {
      Map<String, byte[]> zipData = new HashMap<String, byte[]>();

      ZipInputStream zipStream = null;

      try
      {
         zipStream = getZipInputStream(filePath);

         // now iterate through each item in the stream. The get next
         // entry call will return a ZipEntry for each file in the
         // stream
         ZipEntry entry = null;

         while ((entry = zipStream.getNextEntry()) != null)
         {
            byte[] documentContent = readEntryData(zipStream);
            zipData.put(entry.getName(), documentContent);
         }
      }
      finally
      {
         // Gently close stream.
         // closing the one which was the last to be constructed in chain.
         close(zipStream);
      }

      return zipData;
   }

   private static ZipInputStream getZipInputStream(final String filePath) throws IOException
   {
      FileInputStream fileStream = null;
      ZipInputStream zipStream = null;

      File file = new File(filePath);
      long fileLength = file.length();

      if (fileLength > Integer.MAX_VALUE)
      {
         throw new IOException("File too large. Please select a smaller file.");
      }

      fileStream = new FileInputStream(file);
      // open the zip file stream
      zipStream = new ZipInputStream(fileStream);

      return zipStream;
   }

   /**
    * method returns byte array from input stream
    * 
    * @param stream
    * @return
    * @throws Exception
    */
   private static byte[] readEntryData(ZipInputStream stream) throws Exception
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
         // Gently close stream.
         close(output);
      }
   }

   /**
    * method takes parameter as Map contain file name ,file content and make zip file
    * 
    * @param filesMap
    * @return
    */

   public static byte[] doZip(Map<String, String> filesMap)
   {
      byte[] buffer = null;
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ZipOutputStream out = new ZipOutputStream(outputStream);
      try
      {
         // Now we set the compression level. This step is optional.
         // If we don't set compression levels, default values will be used.
         // I am just setting them so that you know how to do it.
         out.setMethod(ZipOutputStream.DEFLATED);
         out.setLevel(9);
         
         for (String fileName : filesMap.keySet())
         {
            String content = (String) filesMap.get(fileName);
            ZipEntry zipEntry = new ZipEntry(fileName);

            zipEntry.setSize(content.length());
            zipEntry.setTime(System.currentTimeMillis());
            zipEntry.setComment(MIME_TEXT_XML);
            out.putNextEntry(zipEntry);
            out.write(content.getBytes());
            // Complete the entry
            out.closeEntry();

         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         close(outputStream);
         close(out);
      }
      buffer = outputStream.toByteArray();
      return buffer;
   }
   /**
    * 
    * @return
    */
   public static String getDocumentPath(String fullPath)
   {
      int sep = fullPath.lastIndexOf("/");
      return fullPath.substring(0, sep);
   }

}
