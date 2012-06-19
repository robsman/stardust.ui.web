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
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff;

import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;

public class TIFFThumbnail
{
   private int currentPageIndex;
   
   private int originalPageIndex;
   
   private long randomNo = System.currentTimeMillis();
   
   private TIFFDocumentHolder docHolder;
   
   private MessagesViewsCommonBean propsBean = null;
   
   public TIFFThumbnail(int currPgIndex, int origPgIndex, TIFFDocumentHolder docHolder)
   {
      currentPageIndex = currPgIndex;
      originalPageIndex = origPgIndex; 
      this.docHolder = docHolder;
      propsBean = MessagesViewsCommonBean.getInstance();
   }

   public int getCurrentPageIndex()
   {
      return currentPageIndex;
   }
   
   /**
    * @return tool tip text for the page
    */
   public String getPageToolTip()
   {
      return propsBean.getParamString("views.tiffViewer.thumbnails.toolTip.pageNumber", String.valueOf(getCurrentPageIndex()));
   }   

   public void setCurrentPageIndex(int currentPageIndex)
   {
      this.currentPageIndex = currentPageIndex;
      randomNo = System.currentTimeMillis();
   }

   public int getOriginalPageIndex()
   {
      return originalPageIndex;
   }

   public void setOriginalPageIndex(int originalPageIndex)
   {
      this.originalPageIndex = originalPageIndex;
      randomNo = System.currentTimeMillis();
   }

   public long getRandomNo()
   {
      return randomNo;
   }
   
   public boolean isSelected()
   {
      return currentPageIndex == docHolder.getSelectedPageNumber();
   }
   
   public String getPageClass()
   {
      if (isSelected())
      {
         return "this.className='selectedPage'";
      }
      else
      {
         return "this.className='unSelectedPage'";
      }
   }
}
