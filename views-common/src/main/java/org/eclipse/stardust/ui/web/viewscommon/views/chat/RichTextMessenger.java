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
package org.eclipse.stardust.ui.web.viewscommon.views.chat;

public class RichTextMessenger implements IMessenger
{
   private final String contentUrl = "/plugins/views-common/views/chat/richTextMessenger.xhtml";
   private String content = "";


   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.views.chat.IMessenger#getContentUrl()
    */
   public String getContentUrl()
   {
      return this.contentUrl;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.views.chat.IMessenger#setContent(java.lang.String
    * )
    */
   public void setContent(String content)
   {
      this.content = content;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.viewscommon.views.chat.IMessenger#getContent()
    */
   public String getContent()
   {
      return content;
   }
}