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


/**
 * @author Shrikant.Gangal
 *
 */
public class TIFFDocumentWrapper
{
   private IDocumentContentInfo docInfo;
   private boolean annotationChanged;
   private boolean rotationChanged;
   private boolean bookmarkChanged;
   private boolean pageSequenceChanged;
   private boolean docDetailsChanged;

   /**
    * @param document
    */
   public TIFFDocumentWrapper(IDocumentContentInfo docInfo)
   {
      this.docInfo = docInfo;
      initialize();
   }

   /**
    * 
    */
   public void initialize()
   {
      setAnnotationChanged(false);
      setBookmarkChanged(false);
      setDocDetailsChanged(false);
      setPageSequenceChanged(false);
      setRotationChanged(false);
   }

   public boolean isAnnotationChanged()
   {
      return annotationChanged;
   }

   public void setAnnotationChanged(boolean annotationChanged)
   {
      this.annotationChanged = annotationChanged;
   }

   public boolean isRotationChanged()
   {
      return rotationChanged;
   }

   public void setRotationChanged(boolean rotationChanged)
   {
      this.rotationChanged = rotationChanged;
   }

   public boolean isBookmarkChanged()
   {
      return bookmarkChanged;
   }

   public void setBookmarkChanged(boolean bookmarkChanged)
   {
      this.bookmarkChanged = bookmarkChanged;
   }

   public boolean isPageSequenceChanged()
   {
      return pageSequenceChanged;
   }

   public void setPageSequenceChanged(boolean pageSequenceChanged)
   {
      this.pageSequenceChanged = pageSequenceChanged;
   }

   public boolean isDocDetailsChanged()
   {
      return docDetailsChanged;
   }

   public void setDocDetailsChanged(boolean docDetailsChanged)
   {
      this.docDetailsChanged = docDetailsChanged;
   }

   public IDocumentContentInfo getDocInfo()
   {
      return docInfo;
   }

   public void setDocInfo(IDocumentContentInfo docInfo)
   {
      this.docInfo = docInfo;
   }
}
