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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.plugin.support.ServiceLoaderUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.views.correspondence.Attachment;


/**
 * @author Yogesh.Manware
 * 
 */
public class PDFConverterHelper implements Serializable
{
   private static final long serialVersionUID = -7021679894798906663L;
   public static final Logger logger = LogManager.getLogger(PDFConverterHelper.class);
   private transient IPdfConverter pdfConverter;

   public PDFConverterHelper()
   {
      super();
      pdfConverter = getConverter();
   }

   public boolean isPDFConverterAvailable()
   {
      return null != pdfConverter ? true : false;
   }

   public byte[] convertAttachmentstoPDF(List<Attachment> attachments, PrintingPreferences printingPreferences)
         throws Exception
   {
      ArrayList<InputStream> pdfList = new ArrayList<InputStream>();
      Iterator<Attachment> attachmentsItr = attachments.iterator();
      while (attachmentsItr.hasNext())
      {
         Attachment attachment = attachmentsItr.next();
         byte[] pdf = pdfConverter.createPdf(attachment.getContent(), attachment.getContentType());
         if (null != pdf)
         {
            pdfList.add(new ByteArrayInputStream(pdf));
         }
      }
      return pdfConverter.concatPdfs(pdfList, printingPreferences);
   }

   /**
    * @param content
    * @param mimeType
    * @return
    * @throws Exception
    */
   public byte[] converttoPDF(byte[] content, MIMEType mimeType) throws Exception
   {
      return pdfConverter.createPdf(content, mimeType.getType());
   }

   /**
    * @param part1
    * @param part2
    * @return
    * @throws Exception
    */
   public byte[] concatinatePDFs(byte[] part1, byte[] part2) throws Exception
   {
      return pdfConverter.concat2Pdfs(part1, part2);
   }

   /**
    * @param out
    * @throws IOException
    */
   private void writeObject(ObjectOutputStream out) throws IOException
   {
      out.defaultWriteObject();
   }

   /**
    * @param in
    * @throws IOException
    * @throws ClassNotFoundException
    */
   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      in.defaultReadObject();
      pdfConverter = getConverter();
   }
   
   /**
    * @return
    */
   private static IPdfConverter getConverter()
   {
      Iterator<IPdfConverter.Factory> serviceProviders = ServiceLoaderUtils
            .searchProviders(IPdfConverter.Factory.class);

      IPdfConverter.Factory converterFactory = null;

      if (null != serviceProviders)
      {
         while (serviceProviders.hasNext())
         {
            converterFactory = serviceProviders.next();
            if (null != converterFactory)
            {
               return converterFactory.getConverter();
            }
         }
      }
      return null;
   }
}
