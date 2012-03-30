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
package org.eclipse.stardust.ui.web.viewscommon.views.document;

import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.pdf.viewer.PdfDocumentHandler;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */

public class PdfViewer implements IDocumentViewer
{
   private final String contentUrl = "/plugins/views-common/views/document/pdfViewer.xhtml";
   private final MIMEType[] mimeTypes = {MimeTypesHelper.PDF};
   private PdfDocumentHandler pdfDocumentHandler;

   /**
    * Viewer specific initialization
    */
   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      String documentId = documentContentInfo.getId();
      pdfDocumentHandler = new PdfDocumentHandler(documentContentInfo);
      SessionSharedObjectsMap sharedObjectsMap = SessionSharedObjectsMap.getCurrent();
      sharedObjectsMap.removeObject(documentId);
      sharedObjectsMap.setObject(documentId, pdfDocumentHandler);
   }

   public String getContent()
   {
      return "";
   }

   public String getContentUrl()
   {
      return contentUrl;
   }

   public void setContent(String content)
   {}

   public MIMEType[] getMimeTypes()
   {
      return mimeTypes;
   }

   public PdfDocumentHandler getPdfDocumentHandler()
   {
      return pdfDocumentHandler;
   }

   public String getToolbarUrl()
   {
      return null;
   }

   public void closeDocument()
   {
      if (null != pdfDocumentHandler.getCurrentDocumentState())
      {
         pdfDocumentHandler.getCurrentDocumentState().closeDocument();
      }
   }
}