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

import java.io.Serializable;
import java.text.DateFormat;
import java.util.Date;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.ui.web.viewscommon.utils.MIMEType;
import org.eclipse.stardust.ui.web.viewscommon.utils.MimeTypesHelper;



/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ChatMessage implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String timeStamp;
   private String user;
   private int userIndex;
   private String message;
   private Document attachedFile;
   private boolean fileTransfr = false;
   private String documentIcon;

   /**
    * @param dt
    * @param user
    * @param message
    */
   public ChatMessage(String user, int userIndex, String message)
   {
      super();
      DateFormat format = DateFormat.getTimeInstance();
      this.timeStamp = format.format(new Date(System.currentTimeMillis()));
      this.user = user;
      this.userIndex = userIndex;
      this.message = message;
   }

   /**
    * @param dt
    * @param user
    * @param message
    */
   public ChatMessage(String user, int userIndex, Document doc)
   {
      super();
      DateFormat format = DateFormat.getTimeInstance();
      this.timeStamp = format.format(new Date(System.currentTimeMillis()));
      this.user = user;
      this.userIndex = userIndex;
      this.attachedFile = doc;
      this.fileTransfr = true;
      this.documentIcon = "/plugins/views-common/images/icons/mime-types/"
            + MimeTypesHelper.detectMimeType(attachedFile.getName(), attachedFile.getContentType()).getIconPath();
   }

   /**
    * @return
    */
   public boolean isUserNotBlank()
   {
      if (StringUtils.isNotEmpty(user))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public String getTimeStamp()
   {
      return timeStamp;
   }

   public void setTimeStamp(String dt)
   {
      this.timeStamp = dt;
   }

   public String getUser()
   {
      return user;
   }

   public void setUser(String user)
   {
      this.user = user;
   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public Document getAttachedFile()
   {
      return attachedFile;
   }

   public void setAttachedFile(Document attachedFile)
   {
      this.attachedFile = attachedFile;
   }

   public boolean isFileTransfr()
   {
      return fileTransfr;
   }

   public String getDocumentIcon()
   {
      return documentIcon;
   }
   
   public int getUserIndex()
   {
      return userIndex;
   }
}
