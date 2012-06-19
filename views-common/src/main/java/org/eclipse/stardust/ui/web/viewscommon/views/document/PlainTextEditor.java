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
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class PlainTextEditor extends PlainTextViewer implements IDocumentEditor
{
   private final String contentUrl = "/plugins/views-common/views/document/plainTextEditor.xhtml";
   private final MIMEType[] mimeTypes = {MimeTypesHelper.TXT, MimeTypesHelper.XML, MimeTypesHelper.CSS};
   private String originalContent = "";

   /**
    * Viewer specific initialization
    */
   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      super.initialize(documentContentInfo, view);
      originalContent = super.getContent();
   }

   public String getContentUrl()
   {
      return contentUrl;
   }

   public void addContent(String content, DocumentEditingPolicy policy)
   {}

   public MIMEType[] getMimeTypes()
   {
      return mimeTypes;
   }

   public boolean isContentChanged()
   {
      if (!originalContent.equals(getContent()))
      {
         return true;
      }
      return false;
   }
}