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

import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;

/**
 * @author Yogesh.Manware
 * 
 */
public class GenericDataFilterOnOff extends TableDataFilterOnOff
{
   private static final long serialVersionUID = 1L;
   private static final String STYLE_CLASS_DISABLED = "iceCmdBtn-dis";
   // absolute image path
   private String imagePath = "";
   private String offTitle = "";

   public GenericDataFilterOnOff(String name, String onTitle, String offTitle, boolean visible, boolean on, String imagePath)
   {
      super(name, onTitle, visible, on);
      this.imagePath = imagePath;
      this.offTitle = offTitle;
   }

   public String getImagePath()
   {
      return imagePath;
   }

   public void setImagePath(String imagePath)
   {
      this.imagePath = imagePath;
   }
   
   public String getStyleClass()
   {
      if (!isOn())
      {
         return STYLE_CLASS_DISABLED;
      }
      else
      {
         return "";
      }
   }
   
   @Override
   public String getTitle()
   {
      if (isOn())
      {
         return offTitle;
      }
      else
      {
         return super.getTitle();
      }
   }
}
