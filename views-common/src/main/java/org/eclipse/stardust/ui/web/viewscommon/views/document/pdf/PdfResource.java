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
package org.eclipse.stardust.ui.web.viewscommon.views.document.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.faces.context.ExternalContext;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.viewscommon.core.HtmlCleanerUtility;
import org.eclipse.stardust.ui.web.viewscommon.core.HtmlFormatter;
import org.eclipse.stardust.ui.web.viewscommon.core.ContextPortalEnums.PrintPageOrientation;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler.MessageDisplayMode;
import org.eclipse.stardust.ui.web.viewscommon.views.correspondence.Attachment;

import com.icesoft.faces.context.Resource;

/**
 * @author Yogesh.Manware
 * 
 */
public class PdfResource implements Resource, Serializable
{
   private static final long serialVersionUID = 1L;
   public static final Logger logger = LogManager.getLogger(PdfResource.class);
   private final Date lastModified;
   private String resourceName;
   private InputStream inputStream;
   private String content = "";
   private List<Attachment> attachments;
   private PrintingPreferences printingPreferences;
   private PDFConverterHelper pdfConverterHelper;
   /**
    * @param ec
    * @param resourceName
    */
   public PdfResource(ExternalContext ec, String resourceName)
   {
      this.resourceName = resourceName;
      this.lastModified = new Date();
   }

   /**
    * @param ec
    * @param resourceName
    * @param content
    */
   public PdfResource(ExternalContext ec, String resourceName, String content)
   {
      this(ec, resourceName);
      this.content = content;
   }

   /**
    * @param ec
    * @param resourceName
    * @param content
    * @param attachments
    * @param printPopup
    */
   public PdfResource(ExternalContext ec, String resourceName, String content, List<Attachment> attachments,
         PrintingPreferences printingPreferences, PDFConverterHelper pdfConverterHelper)
   {
      this(ec, resourceName, content);
      this.attachments = attachments;
      this.printingPreferences = printingPreferences;
      this.pdfConverterHelper = pdfConverterHelper;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.icesoft.faces.context.Resource#open()
    */
   public InputStream open() throws IOException
   {
      if (inputStream == null)
      {
         byte[] byteArray = null;
         try
         {
            if (CollectionUtils.isEmpty(attachments))
            {
               byteArray = getRichTextContent();
            }
            else
            {
               byteArray = pdfConverterHelper.concatinatePDFs(getRichTextContent(),
                     pdfConverterHelper.convertAttachmentstoPDF(attachments, printingPreferences));
            }
            if (byteArray == null)
               return new ByteArrayInputStream(" ".getBytes());
            inputStream = new ByteArrayInputStream(byteArray);
         }
         catch (Exception e)
         {
            logger.debug("Error Occured while PDF conversion");
            ExceptionHandler.handleException(e,
                  MessagesViewsCommonBean.getInstance().getString("common.unableToPerformAction"),
                  MessageDisplayMode.ONLY_CUSTOM_MSG);
         }
      }
      return inputStream;
   }

   /**
    * @return
    * @throws Exception
    */
   private byte[] getRichTextContent() throws Exception
   {
      String content = this.content;

      String cleanedupXml = cleanTheInputXml(content);
      byte[] inputXmlAsByteArray = createByteArrayFromXml(new ByteArrayOutputStream(), cleanedupXml);
      return createPdfAsByteArray(inputXmlAsByteArray);

   }

   /**
    * @param content
    * @return
    */
   private String cleanTheInputXml(String content)
   {
      HtmlFormatter cleaner = new HtmlCleanerUtility();
      return cleaner.cleanAndFormatHtml(content);
   }

   /**
    * @return
    */
   private String isLandScape()
   {
      if (this.printingPreferences.getOrientation().equals(PrintPageOrientation.landscape.toString()))
         return PrintPageOrientation.landscape.toString();
      else
         return PrintPageOrientation.portrait.toString();
   }

   private String getPageSize()
   {
      return printingPreferences.getPageSize();
   }

   /**
    * @return
    */
   private String getStyle()
   {
      return

      "@page {" + "size: " + getPageSize() + " " + isLandScape() + ";" + "margin-left: "
            + printingPreferences.getLeft() / 72 + "in;" + "margin-right: " + printingPreferences.getRight() / 72
            + "in;" + "margin-top: " + printingPreferences.getTop() / 72 + "in;" + "margin-bottom: "
            + printingPreferences.getBottom() / 72 + "in;}";
   }

   /**
    * @param inputXml
    * @return
    */
   private String getXMLWithHeaders(String inputXml)
   {
      StringBuilder build = new StringBuilder();
      final String TAG_XHTML_DOCTYPE = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">";
      final String TAG_HTML_OPEN = "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\" dir=\"ltr\">";
      final String TAG_HEAD_OPEN = "<head>";
      final String TAG_STYLE_OPEN = "<style>";
      final String TAG_STYLE_CLOSE = "</style>";
      final String TAG_HEAD_CLOSE = "</head>";
      final String TAG_HTML_CLOSE = "</html>";

      build.append(TAG_XHTML_DOCTYPE).append(TAG_HTML_OPEN);
      build.append(TAG_HEAD_OPEN).append(TAG_STYLE_OPEN);
      build.append(getStyle());
      build.append(TAG_STYLE_CLOSE).append(TAG_HEAD_CLOSE);
      build.append(inputXml);
      build.append(TAG_HTML_CLOSE);

      return build.toString();
   }

   /**
    * @param inputXml
    * @return
    * @throws Exception
    */
   private byte[] createPdfAsByteArray(byte[] inputXml) throws Exception
   {
      return pdfConverterHelper.converttoPDF(inputXml, MimeTypesHelper.XHTML);
   }

   /**
    * @param out
    * @param cleanedXml
    * @return
    */
   private byte[] createByteArrayFromXml(ByteArrayOutputStream out, String cleanedXml)
   {
      try
      {
         String cleanedXhtml = getXMLWithHeaders(cleanedXml);
         byte buf[] = cleanedXhtml.getBytes();
         out.write(buf);
         out.close();
      }
      catch (Exception e)
      {
         logger.error("Error in reading the xml file: " + e.getMessage(), e);
      }
      return out.toByteArray();

   }

   public String calculateDigest()
   {
      return resourceName;
   }

   public Date lastModified()
   {
      return lastModified;
   }

   public void withOptions(Options arg0) throws IOException
   {}
}
