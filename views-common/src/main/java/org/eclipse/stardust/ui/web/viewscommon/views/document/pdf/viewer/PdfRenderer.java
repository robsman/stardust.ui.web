/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ICEsoft Technologies Canada, Corp. - initial API and implementation
 *    SunGard CSA LLC                    - additional modifications
 *******************************************************************************/

// Note: This file is derived from http://anonsvn.icefaces.org/repo/icepdf/tags/icepdf-3.0.0/icepdf/examples/icefaces/src/org/icepdf/examples/jsf/viewer/servlet/PdfRenderer.java (r18941)

package org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.viewer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;

public class PdfRenderer extends HttpServlet
{
   private static final long serialVersionUID = 1L;

   /**
    * renders pdf
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      try
      {
         String documentId = request.getParameter("documentId");
         SessionSharedObjectsMap sessionSharedObjectsMap = (SessionSharedObjectsMap) request.getSession().getAttribute(
               "sessionSharedObjectsMap");
         PdfDocumentHandler pdfDocumentHandler = (PdfDocumentHandler) sessionSharedObjectsMap.getObject(documentId);
         PdfDocumentState documentState = pdfDocumentHandler.getCurrentDocumentState();

         if (null != documentState)
         {
            BufferedImage bufferedImage = (BufferedImage) documentState.getPageImage();
            if (null != bufferedImage)
            {
               response.setContentType("image/png");
               OutputStream outputStream = response.getOutputStream();
               ImageIO.write(bufferedImage, "png", outputStream);
               outputStream.close();
               bufferedImage.flush();
            }
         }
      }
      catch (Exception e)
      {
      }
   }
}