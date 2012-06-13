/*
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * "The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is ICEfaces 1.5 open source software code, released
 * November 5, 2006. The Initial Developer of the Original Code is ICEsoft
 * Technologies Canada, Corp. Portions created by ICEsoft are Copyright (C)
 * 2004-2006 ICEsoft Technologies Canada, Corp. All Rights Reserved.
 *
 * Contributor(s): _____________________.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"
 * License), in which case the provisions of the LGPL License are
 * applicable instead of those above. If you wish to allow use of your
 * version of this file only under the terms of the LGPL License and not to
 * allow others to use your version of this file under the MPL, indicate
 * your decision by deleting the provisions above and replace them with
 * the notice and other provisions required by the LGPL License. If you do
 * not delete the provisions above, a recipient may use your version of
 * this file under either the MPL or the LGPL License."
 *
 */

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
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler.MessageDisplayMode;

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
         ExceptionHandler.handleException(e,
               MessagesViewsCommonBean.getInstance().getString("common.unableToPerformAction"),
               MessageDisplayMode.ONLY_CUSTOM_MSG);
      }
   }
}