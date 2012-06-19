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
package org.eclipse.stardust.ui.web.viewscommon.common;

import org.eclipse.stardust.common.StringUtils;


/**
 * @author Subodh.Godbole
 * 
 */
public class FilterToolbarItem
{
   private static final String STYLE_CLASS_DISABLED = "iceCmdBtn-dis";
   private String id;
   private String name;
   private String messsageKey;
   private String image;
   private boolean visible;
   private boolean active;
   private boolean neverVisible;
   private String baseImagePath;
   private String msgKeyActive;
   private String msgKeyInactive;

   /**
    * The variable MsgKeyInactive : message to show when Icon is disabled, The variable
    * msgKeyActive : message to show when Icon is enabled,
    * 
    * @param id
    * @param name
    * @param inActiveMsgKey
    * @param activeMsgKey
    * @param image
    * @param baseImagePath
    */
   public FilterToolbarItem(String id, String name, String msgKeyInactive, String msgKeyActive, String image,
         String baseImagePath)
   {
      super();
      this.id = id;
      this.name = name;
      this.msgKeyInactive = msgKeyInactive;
      this.msgKeyActive = msgKeyActive;
      this.image = image;

      this.active = true;
      this.visible = true; // Default Show
      this.neverVisible = false;
      this.baseImagePath = baseImagePath;
   }
   
   /**
    * use this method if you are passing absolute path of image
    * 
    * @param id
    * @param name
    * @param messsageKey
    * @param image
    */
   public FilterToolbarItem(String id, String name, String msgKeyInactive, String msgKeyActive, String image)
   {
      this(id, name, msgKeyInactive,msgKeyActive, image, null);
   }
   
   
   public String getStyleClass()
   {
      if (!active)
      {
         return STYLE_CLASS_DISABLED;
      }
      else
      {
         return "";
      }
   }

   /**
    * @return
    */
   public String getImage()
   {
      if (StringUtils.isNotEmpty(baseImagePath))
      {
         return baseImagePath + image;
      }
      return image;
   }

   /**
    * @return
    */
   public String getDisplayText()
   {
      return Localizer.getString(new LocalizerKey(messsageKey));
   }

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public boolean isActive()
   {
      return active;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

   public boolean toggle()
   {
      return active = !active;
   }

   public boolean isVisible()
   {
      return visible;
   }

   public void setVisible(boolean visible)
   {
      if (!isNeverVisible())
      {
         this.visible = visible;
      }
   }

   public String getMesssageKey()
   {
      if (!active)
      {
         return msgKeyInactive;
      }
      else
      {
         return msgKeyActive;
      }
   }

   public boolean isNeverVisible()
   {
      return neverVisible;
   }

   public String getMsgKeyActive()
   {
      return msgKeyActive;
   }

   public String getMsgKeyInactive()
   {
      return msgKeyInactive;
   }
   
   
}
