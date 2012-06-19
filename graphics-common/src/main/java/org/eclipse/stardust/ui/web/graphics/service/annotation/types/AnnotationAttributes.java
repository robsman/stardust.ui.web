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
package org.eclipse.stardust.ui.web.graphics.service.annotation.types;

/**
 * @author Shrikant.Gangal
 *
 */
public class AnnotationAttributes
{
   private String colour;
   
   private float opacity;
   
   private int rotationfactor;
   
   private String fontweight;
   
   private String fontstyle;
   
   private String textdecoration;
   
   private int fontsize;

   public String getColour()
   {
      return colour;
   }

   public void setColour(String colour)
   {
      this.colour = colour;
   }

   public float getOpacity()
   {
      return opacity;
   }

   public void setOpacity(float opacity)
   {
      this.opacity = opacity;
   }

   public int getRotationfactor()
   {
      return rotationfactor;
   }

   public void setRotationfactor(int rotationfactor)
   {
      this.rotationfactor = rotationfactor;
   }

   public String getFontweight()
   {
      return fontweight;
   }

   public void setFontweight(String fontweight)
   {
      this.fontweight = fontweight;
   }

   public String getFontstyle()
   {
      return fontstyle;
   }

   public void setFontstyle(String fontstyle)
   {
      this.fontstyle = fontstyle;
   }

   public String getTextdecoration()
   {
      return textdecoration;
   }

   public void setTextdecoration(String textdecoration)
   {
      this.textdecoration = textdecoration;
   }

   public int getFontsize()
   {
      return fontsize;
   }

   public void setFontsize(int fontsize)
   {
      this.fontsize = fontsize;
   }
}
