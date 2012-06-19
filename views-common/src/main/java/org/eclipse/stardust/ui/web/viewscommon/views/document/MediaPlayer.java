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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;


/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class MediaPlayer implements IDocumentViewer
{
   private final String contentUrl = "/plugins/views-common/views/document/mediaPlayer.xhtml";
   private final Map<String, String> playerMap = new HashMap<String, String>();
   private final MIMEType[] mimeTypes = {
         MimeTypesHelper.WMF, MimeTypesHelper.AVI, MimeTypesHelper.WMA, MimeTypesHelper.X_MPEG, MimeTypesHelper.MP3,
         MimeTypesHelper.MOV, MimeTypesHelper.SWF};
   private String content = "";
   private String player = "windows";
   private String downloadToken = "";
   
   /**
    *initialize
    */
   public void initialize(IDocumentContentInfo documentContentInfo, View view)
   {
      playerMap.put("video/x-ms-wmv", "windows");
      playerMap.put("video/x-msvideo", "windows");
      playerMap.put("audio/x-ms-wma", "windows");
      playerMap.put("audio/x-mpeg", "windows");
      playerMap.put("audio/mpeg", "windows");
      playerMap.put("video/quicktime", "quicktime");
      playerMap.put("application/x-shockwave-flash", "flash");

      this.downloadToken = documentContentInfo.getURL();
      this.player = playerMap.get(documentContentInfo.getMimeType().getType());
   }

   public String getDownloadToken()
   {
      return downloadToken;
   }

   public String getPlayer()
   {
      return player;
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
}