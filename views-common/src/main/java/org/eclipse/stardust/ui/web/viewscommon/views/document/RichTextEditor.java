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

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class RichTextEditor extends RichTextViewer implements IDocumentEditor
{
   private final String contentUrl = "/plugins/views-common/views/document/richTextEditor.xhtml";
   private final MIMEType[] mimeTypes = {MimeTypesHelper.HTML};
   private String originalContent = "";

   private String addedContent;

   // Editor specific initialization
   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      super.initialize(documentContentInfo, view);
      originalContent = super.getContent();
   }

   public String getContentUrl()
   {
      return this.contentUrl;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentViewer#getMimeTypes()
    */
   public MIMEType[] getMimeTypes()
   {
      return mimeTypes;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.document.RichTextViewer#getContent()
    */
   public String getContent()
   {
      if (StringUtils.isNotEmpty(addedContent) && !addedContent.equals(super.getContent()))
      {
         return addedContent;
      }
      else
      {
         addedContent = "";
      }
      return super.getContent();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentEditor#addContent(java
    * .lang.String,
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentEditor.DocumentEditingPolicy
    * )
    */
   public void addContent(String content, DocumentEditingPolicy policy)
   {
      if (DocumentEditingPolicy.ADD_AT_TOP == policy)
      {
         this.addedContent = content + super.getContent();
      }
      else if (DocumentEditingPolicy.ADD_AT_BOTTOM == policy)
      {
         this.addedContent = super.getContent() + content;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentEditor#isContentChanged
    * ()
    */
   public boolean isContentChanged()
   {
      if (!originalContent.equals(getContent()))
      {
         return true;
      }
      return false;
   }
}