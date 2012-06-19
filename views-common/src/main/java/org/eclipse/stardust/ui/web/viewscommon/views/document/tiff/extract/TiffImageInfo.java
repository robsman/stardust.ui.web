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
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.extract;

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;



public class TiffImageInfo
{

   private final String docId;
   private final Document document;
   private final byte[] documentContent;
   private final ProcessInstance processInstance;
   private final int maxPages;
   private List<BookmarkPageRange> bookmarkPageRange;

   /**
    * 
    * @param docId
    * @param processInstance
    * @param documentContent
    * @param bookmarkPageRange
    * @param maxPages
    * @throws Exception
    */
   public TiffImageInfo(String docId, ProcessInstance processInstance, byte[] documentContent,
         List<BookmarkPageRange> bookmarkPageRange, int maxPages) throws Exception
   {
      this.docId = docId;
      this.documentContent = documentContent;
      this.maxPages = maxPages;
      document = DocumentMgmtUtility.getDocument(docId);
      this.processInstance = processInstance;
      if (CollectionUtils.isNotEmpty(bookmarkPageRange))
      {
         setBookmarkPageRange(bookmarkPageRange);
      }
   }
   
   public boolean isProcessAvailable()
   {
      return processInstance != null ? true : false;
   }

   public String getDocId()
   {
      return docId;
   }

   public Document getDocument()
   {
      return document;
   }

   public byte[] getDocumentContent()
   {
      return documentContent;
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public int getMaxPages()
   {
      return maxPages;
   }
   
   public List<BookmarkPageRange> getBookmarkPageRange()
   {
      return bookmarkPageRange;
   }

   public void setBookmarkPageRange(List<BookmarkPageRange> bookmarkPageRange)
   {
      this.bookmarkPageRange = bookmarkPageRange;
   }

   /**
    * 
    * @author Sidharth.Singh
    * @since 7.0
    */
   public static class BookmarkPageRange
   {
      private int fromPage;
      private int toPage;

      /**
       * 
       * @param fromPage
       * @param toPage
       */
      public BookmarkPageRange(int fromPage, int toPage)
      {
         super();
         this.fromPage = fromPage;
         this.toPage = toPage;
      }

      public int getFromPage()
      {
         return fromPage;
      }

      public int getToPage()
      {
         return toPage;
      }

   }

}