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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.menu.impl;

import org.eclipse.stardust.ui.web.common.spi.menu.CommonMenuItem;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;

/**
 * @author Anoop.Nair
 *
 */
public class IppCommonMenuItem implements CommonMenuItem
{
   private static final long serialVersionUID = 8116100926397961097L;

   private String id;
   private String title;
   private String URL;
   private String iconPath;
   private boolean changed;
   private String titleCache;
   
   public IppCommonMenuItem()
   {
   }
   
   /**
    * @param id         Id for the Common Menu item
    * @param title      Title for the Common Menu item
    * @param URL        URL for the Common Menu item
    * @param iconPath   Icon for the Common Menu item
    * @param changed    True if the menu item has changed since the last retrieval, false otherwise
    */
   public IppCommonMenuItem(String id, String title, String URL, String iconPath, boolean changed)
   {
      this.id = id;
      this.title = title;
      this.URL = URL;
      this.iconPath = iconPath;
      this.changed = changed;
   }
   
   public String getId()
   {
      return id;
   }

   /**
    * If title is I18N, title will be of format 'messageBean#message' split and return the
    * message
    */
   public String getTitle()
   {
      if (null == titleCache)
      {
         try
         {
            if (title.contains("#"))
            {
               String[] parts = title.split("#");
               if (null != parts)
               {
                  MessagesViewsCommonBean messBean = (MessagesViewsCommonBean) FacesUtils.getBeanFromContext(parts[0]);
                  String titleStr = messBean.getString(parts[1]);
                  titleCache = titleStr;
               }
            }
         }
         catch (Exception e)
         {
         }
         finally
         {
            if (null == titleCache)
            {
               titleCache = title;
            }
         }

      }
      return titleCache;
   }

   public String getURL()
   {
      return URL;
   }

   public String getIconPath()
   {
      return iconPath;
   }

   public boolean isChanged()
   {
      return changed;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public void setURL(String uRL)
   {
      URL = uRL;
   }

   public void setIconPath(String iconPath)
   {
      this.iconPath = iconPath;
   }
   
   public void setChanged(boolean changed)
   {
      this.changed = changed;
   }
}
