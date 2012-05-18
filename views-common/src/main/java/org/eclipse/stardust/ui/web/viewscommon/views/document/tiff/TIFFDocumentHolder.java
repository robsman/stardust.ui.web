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

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotations;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.FileSystemDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.TIFFDocumentWrapper;


/**
 * @author Shrikant.Gangal
 * 
 */
public class TIFFDocumentHolder
{
   private static final String BEAN_NAME = "tiffDocumentHolder";
   private static final String TIFF_FORMAT_NAME = "tiff";
   private static final int MIN_PAGE_INDEX = 0;
   private static final int THUMBNAILS_TAB_INDEX = 0;
   private static final int BOOKMARKS_TAB_INDEX = 1;
   private static final String IMAGE_ENCODING = "png";
   private static final Logger trace = LogManager.getLogger(TIFFDocumentHolder.class);
   private final MessagesViewsCommonBean MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();

   private int pageIndex;
   private int maxPageIndex;
   private byte[] documentContent;
   private ImageReader reader;
   private BufferedImage currentImage;
   private List<TIFFThumbnail> pageOrder;
   private int copyPageSource;
   private int copyPageDestination;
   private boolean showHideFlag = true;
   private boolean insertEnabled = false;
   private int selectedPageNumber;
   private BookmarkManager bookmarkManager;
   private int focusIndex;
   private ProcessInstance processInstance;
   private boolean editable;
   private IDocumentContentInfo docInfo;

   /**
    * @param docId
    */
   public TIFFDocumentHolder(IDocumentContentInfo docInfo)
   {
      initialize(docInfo);
   }

   /**
    * @param docId
    */
   public void initialize(IDocumentContentInfo docInfo)
   {
      this.docInfo = docInfo;
      pageIndex = 1;

      try
      {
         documentContent = docInfo.retrieveContent();

         initTIFFReader();

         reader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(getDocumentContent())));
         maxPageIndex = reader.getNumImages(true);

         initPageOrder();
         bookmarkManager = new BookmarkManager(this);
         bookmarkManager.setDocumentEditable(editable);
         bookmarkManager.initialize();
         selectedPageNumber = getDefaultPageIndex();
         focusIndex = THUMBNAILS_TAB_INDEX;
         refresh();
      }
      catch (Exception e)
      {
         trace.error(e);
         throw new RuntimeException(e);
      }
   }

   /**
    * @return
    */
   public static TIFFDocumentHolder getCurrent()
   {
      return (TIFFDocumentHolder) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public String getDefaultPagePath()
   {
      String pagePath = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
      pagePath += "/plugins/views-common/views/document/tiffViewer.html?docId=" + getDocId() + "&pageNo="
      + getDefaultPageIndex() + "&noOfPages=" + getNumberOfpages() + "&postFix="
      + System.currentTimeMillis();
      
      return pagePath;
   }
   
   /**
    * @param pageIndex
    * @return
    */
   public BufferedImage getPage(int pageIndex)
   {
      int origPage = pageOrder.get(pageIndex - 1).getOriginalPageIndex();
      try
      {
         synchronized (reader)
         {
            return reader.read(origPage - 1, null);
         }
      }
      catch (IOException e)
      {
         trace.error(e);
      }

      return null;
   }
   
   /**
    * @param pageIndex
    * @return
    */
   public int getPageWidth(int pageIndex)
   {
      try
      {
         synchronized (reader)
         {
            return reader.read(getOriginalPageIndex(pageIndex), null).getWidth();
         }
      }
      catch (IOException e)
      {
         trace.error(e);
      }

      return 0;
   }
   
   /**
    * @param pageIndex
    * @return
    */
   public int getPageHeight(int pageIndex)
   {
      try
      {
         synchronized (reader)
         {
            return reader.read(getOriginalPageIndex(pageIndex), null).getHeight();
         }
      }
      catch (IOException e)
      {
         trace.error(e);
      }

      return 0;
   }
   
   /**
    * Returns the original page index for the given page index.
    * Requestede page indexe can be different from original page index
    * in case the pages are rearranged on the client side.  
    * @return
    */
   public int getOriginalPageIndex(int pageIndex)
   {
      return pageOrder.get(pageIndex - 1).getOriginalPageIndex() - 1;
   }

   /**
    * @param image
    * @return
    */
   public byte[] getPNGEnodedImageBytes(BufferedImage image)
   {
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(IMAGE_ENCODING);
      if (!writers.hasNext())
      {
         throw new RuntimeException("no image writer");
      }
      ImageWriter writer = writers.next();
      try
      {
         writer.setOutput(ImageIO.createImageOutputStream(bs));
         writer.write(image);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error in converting the image to byte array");
      }

      return bs.toByteArray();
   }

   /**
    * @param pageIndex
    * @param width
    * @param height
    * @return
    */
   public BufferedImage getPage(int pageIndex, float width, float height)
   {
      BufferedImage img = getPage(pageIndex);
      int actualWidth = img.getWidth();
      int actualHeight = img.getHeight();

      float widthScaleRatio = width / actualWidth;
      float heightScaleRatio = height / actualHeight;

      AffineTransform xform = new AffineTransform();
      xform.scale(widthScaleRatio, heightScaleRatio);
      AffineTransformOp op = new AffineTransformOp(xform, AffineTransformOp.TYPE_BILINEAR);
      BufferedImage out = op.filter(img, null);

      return out;
   }

   /**
    * @param pageIndex
    * @param width
    * @param height
    * @return
    */
   public BufferedImage getBestFitPageImage(int pageIndex, float width, float height)
   {
      BufferedImage img = getPage(pageIndex);
      int actualWidth = img.getWidth();
      int actualHeight = img.getHeight();

      float widthScaleRatio = width / actualWidth;
      float heightScaleRatio = height / actualHeight;

      float scaleRatio = Math.min(widthScaleRatio, heightScaleRatio);

      AffineTransform xform = new AffineTransform();
      xform.scale(scaleRatio, scaleRatio);
      AffineTransformOp op = new AffineTransformOp(xform, AffineTransformOp.TYPE_BILINEAR);
      BufferedImage out = op.filter(img, null);

      return out;
   }

   /**
    * @return
    */
   public byte[] getDocumentContent()
   {
      return documentContent;
   }

   /**
    * 
    */
   private void refresh()
   {
      try
      {
         currentImage = reader.read(pageIndex - 1, null);
      }
      catch (IOException e)
      {
         trace.error(e);
      }
   }

   /**
    * @param event
    */
   public void goToPage(ActionEvent event)
   {
      if (pageIndex > maxPageIndex)
      {
         pageIndex = maxPageIndex;
      }
      if (pageIndex < MIN_PAGE_INDEX)
      {
         pageIndex = MIN_PAGE_INDEX;
      }
      refresh();
   }
   
   /**
    * @param event
    */
   public void addBookmark(ActionEvent event)
   {
      int pageNo = 1;
      if (null != event.getComponent().getAttributes().get("pageNo"))
      {
         pageNo = ((Integer) event.getComponent().getAttributes().get("pageNo")).intValue();
      }
      else
      {
         pageNo = getSelectedPageNumber();
      }

      bookmarkManager.addNewBookmark(pageNo);
      focusIndex = BOOKMARKS_TAB_INDEX;
   }

   /**
    * @param event
    */
   public void movePageUp(ActionEvent event)
   {
      int pageNo = 0;
      if (null != event.getComponent().getAttributes().get("pageNo"))
      {
         pageNo = ((Integer) event.getComponent().getAttributes().get("pageNo")).intValue();
      }
      else
      {
         pageNo = getSelectedPageNumber();
      }

      movePageUp(pageNo);
      setDocumentAnnotationPageIndex();
   }

   /**
    * Scans for TIFF readers. Also, forces a re-scans of the classpath once if no reader
    * is found the first time. (This is needed as a workaround for the classloading issues
    * observed on JBoss that)
    */
   private void initTIFFReader()
   {
      final int TIFF_READER_SCAN_RETRY_COUNT_MAX = 1;
      int retryCount = 0;
      Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(TIFF_FORMAT_NAME);      
      while (!readers.hasNext())
      {
         if (retryCount < TIFF_READER_SCAN_RETRY_COUNT_MAX)
         {
            ImageIO.scanForPlugins();            
            readers = ImageIO.getImageReadersByFormatName(TIFF_FORMAT_NAME);
            retryCount++;
         }
         else
         {
            throw new RuntimeException(MESSAGE_BEAN.getString("views.tiffViewer.error.noReaderFound"));
         }
      }

      reader = (ImageReader) readers.next();
   }

   /**
    * @param pageNo
    */
   private void movePageUp(int pageNo)
   {
      final int minPageNoForPageUpAction = 2;

      if (pageNo >= minPageNoForPageUpAction)
      {
         TIFFThumbnail srcPage = pageOrder.get(pageNo - 1);
         int srcPageOrigIndex = srcPage.getOriginalPageIndex();
         TIFFThumbnail destPage = pageOrder.get(pageNo - 2);
         int destPageOrigIndex = destPage.getOriginalPageIndex();

         srcPage.setOriginalPageIndex(destPageOrigIndex);
         destPage.setOriginalPageIndex(srcPageOrigIndex);
         PortalApplication.getInstance().addEventScript(
               "window.parent.EventHub.events.publish('page_sequence_change_event', 'moveUp', " + pageNo + ");");
         selectedPageNumber--;
         ((Map<String, TIFFDocumentWrapper>) SessionSharedObjectsMap.getCurrent().getObject("DOC_ID_VS_DOC_MAP")).get(getDocId()).setPageSequenceChanged(true);
      }
   }

   /**
    * @param event
    */
   public void movePageDown(ActionEvent event)
   {
      int pageNo = 0;
      if (null != event.getComponent().getAttributes().get("pageNo"))
      {
         pageNo = ((Integer) event.getComponent().getAttributes().get("pageNo")).intValue();
      }
      else
      {
         pageNo = getSelectedPageNumber();
      }

      movePageDown(pageNo);
      setDocumentAnnotationPageIndex();
   }

   /**
    * @param pageNo
    */
   private void movePageDown(int pageNo)
   {
      if (pageNo > 0 && pageNo < getMaxPageIndex())
      {
         TIFFThumbnail srcPage = pageOrder.get(pageNo - 1);
         int srcPageOrigIndex = srcPage.getOriginalPageIndex();
         TIFFThumbnail destPage = pageOrder.get(pageNo);
         int destPageOrigIndex = destPage.getOriginalPageIndex();

         srcPage.setOriginalPageIndex(destPageOrigIndex);
         destPage.setOriginalPageIndex(srcPageOrigIndex);
         PortalApplication.getInstance().addEventScript(
               "window.parent.EventHub.events.publish('page_sequence_change_event', 'moveDown', " + pageNo + ");");
         selectedPageNumber++;
         ((Map<String, TIFFDocumentWrapper>) SessionSharedObjectsMap.getCurrent().getObject("DOC_ID_VS_DOC_MAP")).get(getDocId()).setPageSequenceChanged(true);
      }
   }

   /**
    * @param event
    */
   public void copyPage(ActionEvent event)
   {
      int pageNo = 0;
      if (null != event.getComponent().getAttributes().get("pageNo"))
      {
         pageNo = ((Integer) event.getComponent().getAttributes().get("pageNo")).intValue();
      }
      else
      {
         pageNo = getSelectedPageNumber();
      }

      copyPage(pageNo);
   }

   /**
    * @param pageNo
    */
   private void copyPage(int pageNo)
   {
      if (0 != pageNo)
      {
         copyPageSource = pageNo;
         insertEnabled = true;
      }
   }

   /**
    * @param event
    */
   public void pastePage(ActionEvent event)
   {
      int pageNo = 0;
      if (null != event.getComponent().getAttributes().get("pageNo"))
      {
         pageNo = ((Integer) event.getComponent().getAttributes().get("pageNo")).intValue();
      }
      else
      {
         pageNo = getSelectedPageNumber();
      }

      pastePage(pageNo);
      setDocumentAnnotationPageIndex();
   }
   
   /**
    * @param pageNo
    */
   private void pastePage(int pageNo)
   {
      if (0 != pageNo)
      {
         copyPageDestination = pageNo;
         TIFFThumbnail copiedPage = pageOrder.get(copyPageSource - 1);

         List<TIFFThumbnail> tmpList = new ArrayList<TIFFThumbnail>();

         if (copyPageDestination > copyPageSource)
         {
            copiedPage.setCurrentPageIndex(copyPageDestination - 1);
            for (int i = 0; i < pageOrder.size(); i++)
            {
               TIFFThumbnail thisPage = pageOrder.get(i);
               if (i == (copyPageDestination - 1))
               {
                  tmpList.add(copiedPage);
               }
               if (i != (copyPageSource - 1))
               {
                  thisPage.setCurrentPageIndex(tmpList.size() + 1);
                  tmpList.add(thisPage);
               }
            }
            pageOrder = tmpList;
         }

         if (copyPageDestination < copyPageSource)
         {
            copiedPage.setCurrentPageIndex(copyPageDestination);
            for (int i = 0; i < pageOrder.size(); i++)
            {
               TIFFThumbnail thisPage = pageOrder.get(i);
               if (i == (copyPageDestination - 1))
               {
                  tmpList.add(copiedPage);
               }
               if (i != (copyPageSource - 1))
               {
                  thisPage.setCurrentPageIndex(tmpList.size() + 1);
                  tmpList.add(thisPage);
               }
            }
            pageOrder = tmpList;
         }

         insertEnabled = false;
         PortalApplication.getInstance().addEventScript(
               "window.parent.EventHub.events.publish('page_sequence_change_event', 'paste', " + getCopyPageSource()
                     + ", " + pageNo + ");");
         selectedPageNumber = copyPageDestination;
         ((Map<String, TIFFDocumentWrapper>) SessionSharedObjectsMap.getCurrent().getObject("DOC_ID_VS_DOC_MAP")).get(getDocId()).setPageSequenceChanged(true);
      }
   }

   /**
    * @return
    */
   public boolean isInsertEnabled()
   {
      return insertEnabled;
   }

   /**
    * @param event
    */
   public void reversePageOrder(ActionEvent event)
   {
      Collections.reverse(pageOrder);
      int i = 1;
      for (TIFFThumbnail page : pageOrder)
      {
         page.setCurrentPageIndex(i++);
      }
      PortalApplication.getInstance().addEventScript(
            "window.parent.EventHub.events.publish('page_sequence_change_event', 'reverse')");
      setDocumentAnnotationPageIndex();
      ((Map<String, TIFFDocumentWrapper>) SessionSharedObjectsMap.getCurrent().getObject("DOC_ID_VS_DOC_MAP")).get(getDocId()).setPageSequenceChanged(true);
   }

   /**
    * @return
    */
   public boolean isShowHideFlag()
   {
      return showHideFlag;
   }

   /**
    * @param showHideFlag
    */
   public void setShowHideFlag(boolean showHideFlag)
   {
      this.showHideFlag = showHideFlag;
   }

   /**
    * 
    */
   public void toggleShowHideFlag()
   {
      showHideFlag = !showHideFlag;
   }

   /**
    * @return
    */
   public int getCopyPageSource()
   {
      return copyPageSource;
   }

   /**
    * @return
    */
   public int getCopyPageDestination()
   {
      return copyPageDestination;
   }

   /**
    * @return
    */
   public boolean isFirstPage()
   {
      if (pageIndex == MIN_PAGE_INDEX)
      {
         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public boolean isLastPage()
   {
      if (pageIndex == maxPageIndex)
      {
         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public String getDocId()
   {
      return (docInfo instanceof FileSystemDocument)
            ? DocumentMgmtUtility.stripOffSpecialCharacters(docInfo.getId())
            : docInfo.getId();
   }

   /**
    * @return
    */
   public int getPageIndex()
   {
      return pageIndex;
   }

   /**
    * @return
    */
   public int getDefaultPageIndex()
   {
      String pgNo = bookmarkManager.getDefaultPage();
      try
      {
         return Integer.parseInt(pgNo);
      }
      catch (Exception e)
      {
         return 1;
      }
   }

   /**
    * @param pageIndex
    */
   public void setPageIndex(int pageIndex)
   {
      if (pageIndex > maxPageIndex)
      {
         this.pageIndex = maxPageIndex;
      }
      else if (pageIndex < MIN_PAGE_INDEX)
      {
         this.pageIndex = MIN_PAGE_INDEX;
      }
      else
      {
         this.pageIndex = pageIndex;
      }
   }

   /**
    * @return
    */
   public int getMaxPageIndex()
   {
      return maxPageIndex;
   }

   /**
    * @return
    */
   public int getMinPageIndex()
   {
      return 1;
   }

   /**
    * @param maxPageIndex
    */
   public void setMaxPageIndex(int maxPageIndex)
   {
      this.maxPageIndex = maxPageIndex;
   }

   /**
    * @return
    */
   public String getUniqueReqPostFix()
   {
      return new Long(System.currentTimeMillis()).toString();
   }

   /**
    * @return
    */
   public int getPageWidth()
   {
      if (null != currentImage)
      {
         return currentImage.getWidth();
      }

      return 0;
   }

   /**
    * @return
    */
   public int getPageHeight()
   {
      if (null != currentImage)
      {
         return currentImage.getHeight();
      }

      return 0;
   }

   /**
    * @return
    */
   public int getMaxDimension()
   {
      if (null != currentImage)
      {
         return Math.max(currentImage.getHeight(), currentImage.getWidth());
      }

      return 0;
   }

   /**
    * @return
    */
   public String getPageImageURL()
   {
      return "/Samsa/IppTiffRenderer?docId=" + docInfo.getId() + "&pageNo=" + getPageIndex() + "&postFix=1291269892395";
   }

   /**
    * @return
    */
   public int getNumberOfpages()
   {
      try
      {
         return reader.getNumImages(true);
      }
      catch (IOException e)
      {
         return 0;
      }
   }

   /**
    * @return
    */
   public List<TIFFThumbnail> getPageOrder()
   {
      return pageOrder;
   }
   
   /**
    * @return
    */
   public List<Integer> getCurrentPageOrder()
   {
      List<Integer> currentPageOrder = new ArrayList<Integer>();
      List<TIFFThumbnail> pgOrder = getPageOrder();
      for (TIFFThumbnail pgo : pgOrder)
      {
         currentPageOrder.add(pgo.getOriginalPageIndex());
      }
      
      return currentPageOrder;
   }

   /**
    * @param event
    */
   public void setSelectedPageNumber(ActionEvent event)
   {
      selectedPageNumber = ((Integer) event.getComponent().getAttributes().get("pageNo")).intValue();
   }

   /**
    * @return
    */
   public BookmarkManager getBookmarkManager()
   {
      return bookmarkManager;
   }

   /**
    * @return
    */
   public int getSelectedPageNumber()
   {
      return selectedPageNumber;
   }

   public int getFocusIndex()
   {
      return focusIndex;
   }

   public void setFocusIndex(int focusIndex)
   {
      this.focusIndex = focusIndex;
   }

   /**
    * 
    */
   private void initPageOrder()
   {
      IDocumentContentInfo doc = ((Map<String, TIFFDocumentWrapper>) SessionSharedObjectsMap.getCurrent().getObject("DOC_ID_VS_DOC_MAP")).get(getDocId()).getDocInfo();
      List<Integer> pgSeq = ((PrintDocumentAnnotations) doc.getAnnotations()).getPageSequence();
      if (null != pgSeq && pgSeq.size() > 0)         
      {
         pageOrder = new ArrayList<TIFFThumbnail>(pgSeq.size());
         for (int i = 1; i <= getNumberOfpages(); i++)
         {
            pageOrder.add(new TIFFThumbnail(i, pgSeq.get(i - 1) + 1, this));
         }
      }
      else
      {
         pageOrder = new ArrayList<TIFFThumbnail>();
         for (int i = 1; i <= getNumberOfpages(); i++)
         {
            pageOrder.add(new TIFFThumbnail(i, i, this));
         }
         setDocumentAnnotationPageIndex();
      }
   }
   
   private void setDocumentAnnotationPageIndex()
   {
      List<Integer> pageSequence = new ArrayList<Integer>(pageOrder.size());
      for (TIFFThumbnail th : pageOrder)
      {
         pageSequence.add(th.getOriginalPageIndex() - 1);
      }
      IDocumentContentInfo doc = ((Map<String, TIFFDocumentWrapper>) SessionSharedObjectsMap.getCurrent().getObject("DOC_ID_VS_DOC_MAP")).get(getDocId()).getDocInfo();
      ((PrintDocumentAnnotations) doc.getAnnotations()).setPageSequence(pageSequence);
   }

   public ProcessInstance getProcessInstance()
   {
      return processInstance;
   }

   public void setProcessInstance(ProcessInstance processInstance)
   {
      this.processInstance = processInstance;
   }

   public boolean isEditable()
   {
      return editable;
   }

   public void setEditable(boolean editable)
   {
      this.editable = editable;
      bookmarkManager.setDocumentEditable(this.editable);
   }
}
