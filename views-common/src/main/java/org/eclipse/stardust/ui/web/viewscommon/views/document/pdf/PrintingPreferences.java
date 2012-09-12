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
package org.eclipse.stardust.ui.web.viewscommon.views.document.pdf;

import java.io.Serializable;

import javax.faces.model.SelectItem;

/**
 * @author Yogesh.Manware
 * 
 */
public class PrintingPreferences implements Serializable
{
   private static final long serialVersionUID = -5008324954880775898L;
   private String pageSize = "letter";
   private String orientation = "portrait";
   private float right = 1;
   private float left = 1;
   private float top = 1;
   private float bottom = 1;

   private SelectItem[] margin = {
         new SelectItem("1.00"), new SelectItem("0.00"), new SelectItem(".50"), new SelectItem(".60"),
         new SelectItem(".70"), new SelectItem(".75"), new SelectItem(".80"), new SelectItem(".90"),
         new SelectItem("1.10"), new SelectItem("1.20"), new SelectItem("1.25"), new SelectItem("1.30"),
         new SelectItem("1.40"), new SelectItem("1.50"), new SelectItem("1.70"), new SelectItem("1.75"),
         new SelectItem("1.80"), new SelectItem("1.90"), new SelectItem("2.00"), new SelectItem("2.25"),
         new SelectItem("2.50")};

   public String getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(String pageSize)
   {
      this.pageSize = pageSize;
   }

   public String getOrientation()
   {
      return orientation;
   }

   public void setOrientation(String orientation)
   {
      this.orientation = orientation;
   }

   public float getBottom()
   {
      return bottom * 72;
   }

   public void setBottom(float bottom)
   {
      this.bottom = bottom;
   }

   public float getTop()
   {
      return top * 72;
   }

   public void setTop(float top)
   {
      this.top = top;
   }

   public float getRight()
   {
      return right * 72;
   }

   public void setRight(float right)
   {
      this.right = right;
   }

   public float getLeft()
   {
      return left * 72;
   }

   public void setLeft(float left)
   {
      this.left = left;
   }

   public SelectItem[] getMargin()
   {
      return margin;
   }

   public void setMargin(SelectItem[] margin)
   {
      this.margin = margin;
   }
}
