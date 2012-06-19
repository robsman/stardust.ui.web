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
package org.eclipse.stardust.ui.web.admin.views.model;

import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.LocalizerKey;

/**
 * @author Vikas.Mishra Toolbar item class for Model Management View
 */
public class FilterToolbarItem
{
   private static final String SHOW = "show.";
   private static final String HIDE = "hide.";
   private static final String ACTIVE_STYLE = "vertical-align: bottom;border: none;";
   private static final String IN_ACTIVE_STYLE = "vertical-align: bottom;opacity: 0.3;filter: alpha(opacity=30);";
   private String id;
   private String image;
   private String messsageKey;
   private String name;
   private boolean active;
   private boolean neverVisible;
   private boolean visible;

   /**
    * @param id
    * @param name
    * @param messsageKey
    * @param image
    */
   public FilterToolbarItem(String id, String name, String messsageKey, String image)
   {
      this.id = id;
      this.name = name;
      this.messsageKey = messsageKey;
      this.image = image;

      this.active = true;
      this.visible = true; // Default Show
      this.neverVisible = false;
   }

   /**
    * @return
    */
   public String getDisplayText()
   {
      return Localizer.getString(new LocalizerKey(messsageKey));
   }

   // **************************** DEFAULT GETTER SETTER METHODS
   // ****************************
   public String getId()
   {
      return id;
   }

   public String getImage()
   {
      return image;
   }

   public String getMesssageKey()
   {
      return (active ? HIDE:SHOW)  + messsageKey;
   }

   public String getName()
   {
      return name;
   }

   public String getStyle()
   {
      return active ? ACTIVE_STYLE : IN_ACTIVE_STYLE;
   }

   public boolean isActive()
   {
      return active;
   }

   public boolean isNeverVisible()
   {
      return neverVisible;
   }

   public boolean isVisible()
   {
      return visible;
   }

   public void setActive(boolean active)
   {
      this.active = active;
   }

   public void setVisible(boolean visible)
   {
      if (!isNeverVisible())
      {
         this.visible = visible;
      }
   }

   public boolean toggle()
   {
      return active = !active;
   }
}
