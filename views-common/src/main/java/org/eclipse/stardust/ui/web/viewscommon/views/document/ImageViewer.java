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
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */

public class ImageViewer implements IDocumentViewer
{
   private final String contentUrl = "/plugins/views-common/views/document/imageViewer.xhtml";
   private final MIMEType[] mimeTypes = {
         MimeTypesHelper.GIF, MimeTypesHelper.JPG, MimeTypesHelper.PNG, MimeTypesHelper.PJPG, MimeTypesHelper.XPNG};
   private String content = "";
   private String downloadToken;

   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      downloadToken = documentContentInfo.getURL();
      if (StringUtils.isEmpty(downloadToken))
      {
         MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
         content = propsBean.getString("views.documentView.message.urlNotAvailablemsg");
      }
   }

   public String getDownloadToken()
   {
      return downloadToken;
   }

   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   public String getContentUrl()
   {
      return contentUrl;
   }

   public MIMEType[] getMimeTypes()
   {
      return mimeTypes;
   }

   public String getToolbarUrl()
   {
      return null;
   }

   public void closeDocument()
   {}

   @Override
   public boolean isHideToolbar()
   {
      return false;
   }
}
