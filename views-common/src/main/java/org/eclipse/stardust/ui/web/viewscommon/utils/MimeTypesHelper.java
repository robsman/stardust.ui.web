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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.io.Serializable;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;



/**
 * @author Yogesh.Manware
 *
 */
public class MimeTypesHelper implements Serializable
{
   private static final long serialVersionUID = 1L;
   public static final String BEAN_NAME = "ippMimeTypesHelper";
   private Set<MIMEType> allMimeTypes = new HashSet<MIMEType>();

   public static final MIMEType XHTML = new MIMEType("text/xhtml", new String[]{"xhtml", "htm"}, "html.png", "XHTML");
   public static final MIMEType HTML = new MIMEType("text/html", new String[]{"html", "htm"}, "html.png", "HTML");
   public static final MIMEType JPG = new MIMEType("image/jpeg", new String[]{"jpg", "jpeg"}, "document-image.png", "JPEG");
   public static final MIMEType PJPG = new MIMEType("image/pjpeg", "jpg", "document-image.png", "PJPEG");
   public static final MIMEType XPNG = new MIMEType("image/x-png", "jpg", "document-image.png", "X-PNG");
   public static final MIMEType GIF = new MIMEType("image/gif", "gif", "gif.gif", "GIF");
	public static final MIMEType TIFF = new MIMEType("image/tiff", new String[] { "tif", "tiff" }, "images.png", "Tiff");
   public static final MIMEType PDF = new MIMEType("application/pdf", "pdf", "pdf.gif", "PDF");
   public static final MIMEType RTF = new MIMEType("text/rtf", "rtf", "ms-word.gif", "");
   public static final MIMEType DOC = new MIMEType("application/msword", "doc", "ms-word.gif", "");
   public static final MIMEType MOV = new MIMEType("video/quicktime", "mov", "quicktime.gif", "Video");
   public static final MIMEType WMF = new MIMEType("video/x-ms-wmv", "wmf", "windows-media.gif", "Video");
   public static final MIMEType AVI = new MIMEType("video/x-msvideo", "avi", "windows-media.gif", "Video");
   public static final MIMEType SWF = new MIMEType("application/x-shockwave-flash", "swf", "swf.png", "Shockwave Flash");
   public static final MIMEType WMA = new MIMEType("audio/x-ms-wma", "wma", "audio.gif", "Audio");
   public static final MIMEType MP3 = new MIMEType("audio/mpeg", "mp3", "audio.gif", "Audio");
   public static final MIMEType ZIP = new MIMEType("application/zip", "zip", "folder-zip.png", "");
   public static final MIMEType TXT = new MIMEType("text/plain", "txt", "document-text.png", "Text");
   public static final MIMEType XML = new MIMEType("text/xml", "xml", "xml.gif", "XML");
   public static final MIMEType PPT = new MIMEType("application/vnd.ms-powerpoint", "ppt", "ms-power-point.gif", "");
   public static final MIMEType XLS = new MIMEType("application/vnd.ms-excel", "xls", "ms-excel.gif", "");
   public static final MIMEType PNG = new MIMEType("image/png", "png", "document-image.png", "PNG");
   public static final MIMEType DEFAULT = new MIMEType("application/octet-stream", "", "tree_document.gif", "");
   public static final MIMEType MS2007_DOC = new MIMEType(
         "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx", "ms-word.gif", "");
   public static final MIMEType MS2007_XLS = new MIMEType(
         "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx", "ms-excel.gif", "");
   public static final MIMEType MS2007_PPT = new MIMEType(
         "application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx", "ms-power-point.gif", "");

   public static final MIMEType RPT_DESIGN = new MIMEType("application/rptdesign", "rptdesign", "report.png", "Reports");
   public static final MIMEType CSS = new MIMEType("text/css", "css", "document-text.png", "CSS");

   public static final MIMEType X_MPEG = new MIMEType("audio/x-mpeg", "mp3", "audio.gif", "Audio");

   public MimeTypesHelper()
   {
      super();
      registerAllKnownTypes();
   }

   protected static MimeTypesHelper getInstance()
   {
      return (MimeTypesHelper) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   /**
    * designed to be invoked only by documentHandlersRegistryBean
    * 
    * @param mimeType
    */
   public void registerMimeTypes(MIMEType[] mimeType)
   {
      for (MIMEType type : mimeType)
      {
         allMimeTypes.add(type);
      }
   }

   /**
    * @param contentType
    * @return
    */
   public static String getExtension(String contentType)
   {
      if(org.eclipse.stardust.common.StringUtils.isNotEmpty(contentType)){
         for (MIMEType mimeType : getInstance().allMimeTypes)
         {
            if (mimeType.getType().equalsIgnoreCase(contentType))
            {
               return mimeType.getFileExtension();
            }
         }
      }
      return "";
   }
   
   /**
    * @param fileName
    * @param type
    * @return
    */
   public static MIMEType detectMimeType(String fileName, String type)
   {
      MIMEType requiredMimeType = null;
      
      if (org.eclipse.stardust.common.StringUtils.isNotEmpty(fileName) || org.eclipse.stardust.common.StringUtils.isNotEmpty(type))
      {
         String extension = null;
         if (org.eclipse.stardust.common.StringUtils.isNotEmpty(fileName))
         {
            extension = StringUtils.substringAfterLast(fileName, ".");
         }
         MimeTypesHelper mimeTypeUtils = getInstance();

         // check if file extension mapping is defined
         if (org.eclipse.stardust.common.StringUtils.isNotEmpty(extension))
         {
            for (MIMEType mimeType : mimeTypeUtils.allMimeTypes)
            {
               if (mimeType.containsExtension(extension))
               {
                  requiredMimeType = mimeType;
                  break;
               }
            }
         }

         // check if there is a mapping defined for content type
         if (null == requiredMimeType)
         {
            for (MIMEType mimeType : mimeTypeUtils.allMimeTypes)
            {
               if (mimeType.getType().equalsIgnoreCase(type))
               {
                  requiredMimeType = mimeType;
                  break;
               }
            }
         }

         // If the file type is not defined, then try to get correct content type using
         // URLConnection. This condition is useful when the content type and extension of
         // the file are not defined in the application. URLConnection returns the
         // standard content type based on file extension. This content type must be
         // defined in above mapping
         if (null == requiredMimeType)
         {
            String fileType = URLConnection.guessContentTypeFromName(fileName);
            requiredMimeType = findByType(fileType);
         }
         
         if (null == requiredMimeType)
         {
            requiredMimeType = DEFAULT;
         }
      }
      return requiredMimeType;
   }

   /**
    * @param type
    * @return
    */
   public static MIMEType findByType(String type)
   {
      MimeTypesHelper mimeTypeUtils = getInstance();
      for (MIMEType mimeType : mimeTypeUtils.allMimeTypes)
      {
         if (mimeType.getType().equalsIgnoreCase(type))
         {
            return mimeType;
         }
      }
      return null;
   }

   /**
    * @return
    */
   public Set<MIMEType> getAllMimeTypes()
   {
      return CollectionUtils.copySet(allMimeTypes);
   }

   /**
    * register all known IPP Mime types
    */
   private void registerAllKnownTypes()
   {
      allMimeTypes.add(HTML);
      allMimeTypes.add(JPG);
      allMimeTypes.add(PJPG);
      allMimeTypes.add(XPNG);
      allMimeTypes.add(GIF);
      allMimeTypes.add(TIFF);
      allMimeTypes.add(PDF);
      allMimeTypes.add(RTF);
      allMimeTypes.add(DOC);
      allMimeTypes.add(MOV);
      allMimeTypes.add(WMF);
      allMimeTypes.add(AVI);
      allMimeTypes.add(SWF);
      allMimeTypes.add(WMA);
      allMimeTypes.add(MP3);
      allMimeTypes.add(ZIP);
      allMimeTypes.add(TXT);
      allMimeTypes.add(XML);
      allMimeTypes.add(PPT);
      allMimeTypes.add(XLS);
      allMimeTypes.add(PNG);
      allMimeTypes.add(DEFAULT);
      allMimeTypes.add(MS2007_DOC);
      allMimeTypes.add(MS2007_XLS);
      allMimeTypes.add(MS2007_PPT);
      allMimeTypes.add(RPT_DESIGN);
      allMimeTypes.add(CSS);
      allMimeTypes.add(X_MPEG);
   }
}
