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

import org.eclipse.stardust.ui.web.common.util.FacesUtils;


public class TIFFCustomSaveDialog
{
   private static final String BEAN_NAME = "tiffCustomSaveDialog";
   
   private boolean tiffDocument;
   private boolean showSaveBookmarks;
   private boolean showSavePageOrder;
   private boolean showSavePageRotation;
   private boolean showSaveAnnotations;
   private boolean showSaveDocumentData;
   private boolean saveBookmarks;
   private boolean savePageOrder;
   private boolean savePageRotation;
   private boolean saveAnnotations;
   private boolean saveDocumentData;
   private boolean fileSystemDocument;
   
   public static TIFFCustomSaveDialog getCurrent()
   {
      return (TIFFCustomSaveDialog) FacesUtils.getBeanFromContext(BEAN_NAME);
   }
   
   public void initialize()
   {
      tiffDocument = false;
      showSaveBookmarks = false;
      showSavePageOrder = false;
      showSavePageRotation = false;
      showSaveAnnotations = false;
      showSaveDocumentData = false;
      saveBookmarks = false;
      savePageOrder = false;
      savePageRotation = false;
      saveAnnotations = false;
      saveDocumentData = false;
   }
   
   public boolean isTiffDocument()
   {
      return tiffDocument;
   }

   public void setTiffDocument(boolean tiffDocument)
   {
      this.tiffDocument = tiffDocument;
   }

   public boolean isShowSaveBookmarks()
   {
      return showSaveBookmarks;
   }

   public void setShowSaveBookmarks(boolean showSaveBookmarks)
   {
      this.showSaveBookmarks = showSaveBookmarks;
   }

   public boolean isShowSavePageOrder()
   {
      return showSavePageOrder;
   }

   public void setShowSavePageOrder(boolean showSavePageOrder)
   {
      this.showSavePageOrder = showSavePageOrder;
   }

   public boolean isShowSavePageRotation()
   {
      return showSavePageRotation;
   }

   public void setShowSavePageRotation(boolean showSavePageRotation)
   {
      this.showSavePageRotation = showSavePageRotation;
   }

   public boolean isShowSaveAnnotations()
   {
      return showSaveAnnotations;
   }

   public void setShowSaveAnnotations(boolean showSaveAnnotations)
   {
      this.showSaveAnnotations = showSaveAnnotations;
   }

   public boolean isSaveBookmarks()
   {
      return saveBookmarks;
   }

   public void setSaveBookmarks(boolean saveBookmarks)
   {
      this.saveBookmarks = saveBookmarks;
   }

   public boolean isSavePageOrder()
   {
      return savePageOrder;
   }

   public void setSavePageOrder(boolean savePageOrder)
   {
      this.savePageOrder = savePageOrder;
   }

   public boolean isSavePageRotation()
   {
      return savePageRotation;
   }

   public void setSavePageRotation(boolean savePageRotation)
   {
      this.savePageRotation = savePageRotation;
   }

   public boolean isSaveAnnotations()
   {
      return saveAnnotations;
   }

   public void setSaveAnnotations(boolean saveAnnotations)
   {
      this.saveAnnotations = saveAnnotations;
   }

   public boolean isShowSaveDocumentData()
   {
      return showSaveDocumentData;
   }

   public void setShowSaveDocumentData(boolean showSaveDocumentData)
   {
      this.showSaveDocumentData = showSaveDocumentData;
   }
   
   public boolean isTiffSaveNone()
   {
      return !((!tiffDocument) || saveAnnotations || saveBookmarks || savePageOrder || savePageRotation || saveDocumentData);
   }

   public boolean isSaveDocumentData()
   {
      return saveDocumentData;
   }

   public void setSaveDocumentData(boolean saveDocumentData)
   {
      this.saveDocumentData = saveDocumentData;
   }

   public boolean isFileSystemDocument()
   {
      return fileSystemDocument;
   }

   public void setFileSystemDocument(boolean fileSystemDocument)
   {
      this.fileSystemDocument = fileSystemDocument;
   }
}
