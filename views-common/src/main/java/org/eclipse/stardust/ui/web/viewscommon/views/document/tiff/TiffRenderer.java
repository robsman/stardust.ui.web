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
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;



/**
 * @author Shrikant.Gangal
 *
 */
public class TiffRenderer extends HttpServlet
{
   private static final long serialVersionUID = -4017920966592316009L;
   private static final String IMAGE_ENCODING = "png";
   private static final String IMAGE_CONTENT_TYPE = "image/" + IMAGE_ENCODING;
   private static final Logger trace = LogManager.getLogger(TiffRenderer.class);

   /**
    * Renders the TIFF image.
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
   {
      try
      {
         SessionSharedObjectsMap objMap = (SessionSharedObjectsMap) request.getSession().getAttribute(
               "sessionSharedObjectsMap");

         Object obj = objMap.getObject(request.getParameter("docId"));
         if (null != obj && obj instanceof TIFFDocumentHolder)
         {
            TIFFDocumentHolder tiffDocHolder = (TIFFDocumentHolder) obj;
            BufferedImage image = null;
            String pageNo = (String) request.getParameter("pageNo");
            boolean isThumbnail = Boolean.parseBoolean((String) request.getParameter("isThumbnail"));
            if (isThumbnail)
            {
               image = tiffDocHolder.getPage(new Integer(pageNo).intValue(), 120, 150);
            }
            else
            {
               image = tiffDocHolder.getPage(new Integer(pageNo).intValue());
               //image = tiffDocHolder.getBestFitPageImage(new Integer(pageNo).intValue(), 650, 750);
            }
            if (null != image)
            {
               response.setContentType(IMAGE_CONTENT_TYPE);
               OutputStream os = response.getOutputStream();               
               os.write(tiffDocHolder.getPNGEnodedImageBytes(image));
               os.close();
            }
         }

      }
      catch (Exception e)
      {
         trace.error(e);
         throw new ServletException(e);
      }
   }
}
