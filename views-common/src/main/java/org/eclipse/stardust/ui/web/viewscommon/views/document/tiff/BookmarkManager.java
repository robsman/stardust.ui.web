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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.validator.ValidatorException;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageBookmark;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotations;
import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PrintDocumentAnnotationsImpl;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.IDataTable;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.table.RowDeselectionListener;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.core.SessionSharedObjectsMap;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentMgmtUtility;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.document.IDocumentContentInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.ImageViewerConfigurationBean;
import org.eclipse.stardust.ui.web.viewscommon.views.document.JCRDocument;
import org.eclipse.stardust.ui.web.viewscommon.views.document.TIFFDocumentWrapper;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.bookmark.BookmarkEntry;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.extract.ExtractPageDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.extract.TiffImageInfo;
import org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.extract.TiffImageInfo.BookmarkPageRange;



/**
 * @author Shrikant.Gangal
 * 
 */
public class BookmarkManager implements RowDeselectionListener
{
   private IDataTable<BookmarkEntry> bookmarkEntryTable;

   private TIFFDocumentHolder tiffDocHolder;
   
   private IDocumentContentInfo tiffDocInfo;
   
   private TIFFDocumentWrapper tiffDocWrapper;

   private final Set<PageBookmark> pgBookmarks; 
   
   private final MessagesViewsCommonBean MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   
   private boolean documentEditable;
   private boolean extractEnable;
   
   /**
    * 
    */
   public BookmarkManager(TIFFDocumentHolder tiffDocHolder)
   {
      this.tiffDocHolder = tiffDocHolder;
      initDocument();
      PrintDocumentAnnotations annots = ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations());
      if (annots != null)
      {
         pgBookmarks = ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).getBookmarks();
      }
      else
      {
         pgBookmarks = new HashSet<PageBookmark>();
      }
      initialize();
      extractEnable = canExtract();
   }

   /**
    * 
    */
   public void initialize()
   {
      createTable();
   }

   /**
    * @return
    */
   public IDataTable<BookmarkEntry> getBookmarksTable()
   {
      return bookmarkEntryTable;
   }

   /**
    * @param event
    */
   public void addNewBookmark(ActionEvent event)
   {
      if (exitEditMode())
      {
         addNewBookmark(tiffDocHolder.getSelectedPageNumber());
      }
   }
   
   /**
    * @param pageNo
    */
   public void addNewBookmark(int pageNo)
   {
      BookmarkEntry b = new BookmarkEntry();
      b.setRowDeselectionListener(this);
      b.setBookmarkText(MESSAGE_BEAN.getString("views.tiffViewer.bookmarks.newBookmark.text") + " " + (pgBookmarks.size() + 1));
      b.setFromPageNo(String.valueOf(pageNo));
      b.setToPageNo(String.valueOf(pageNo));
      b.setHierarchyLevel(0);
      b.setHasChildren(false);
      b.setSelected(true);
      b.setEditable(true);
      int selectedBmIndex = getSelectedBookmarkIndex();
      
      if (selectedBmIndex != -1)
      {
         ((BookmarkEntry) bookmarkEntryTable.getList().get(selectedBmIndex)).setSelected(false);
         bookmarkEntryTable.getList().add(selectedBmIndex + 1, b);
      }
      else
      {
         bookmarkEntryTable.getList().add(b);
      }
      
      if (null == (PrintDocumentAnnotations) tiffDocInfo.getAnnotations())
      {
         tiffDocInfo.setAnnotations(new PrintDocumentAnnotationsImpl());
      }
      refreshPageBookmarks();
      
      tiffDocWrapper.setBookmarkChanged(true);
   }

   /**
    * @param event
    */
   public void deleteSelectedBookmarks(ActionEvent event)
   {
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();

      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isSelected())
         {
            ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).removeBookmark(be.getBookmarkText());
            iter.remove();
         }
      }
      tiffDocWrapper.setBookmarkChanged(true);
   }

   /**
    * @param event
    */
   public void enterEditMode(ActionEvent event)
   {
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();
      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isSelected())
         {
            be.setEditable(true);
         }
         else
         {
            be.setEditable(false);
         }
      }
   }
   
   /**
    * @param event
    */
   public void extractPageBookmark(ActionEvent event)
   {
      ExtractPageDialog dialog = ExtractPageDialog.getCurrent();
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();
      List<BookmarkPageRange> bookmarkList = CollectionUtils.newArrayList();
      try
      {
         while (iter.hasNext())
         {
            BookmarkEntry be = iter.next();
            if (be.isSelected())
            {
               int fromIndex = Integer.valueOf(be.getFromPageNo());
               int toIndex = Integer.valueOf(be.getToPageNo());
               bookmarkList.add(new BookmarkPageRange(fromIndex, toIndex));
            }
         }
         TiffImageInfo imageInfo = new TiffImageInfo(tiffDocHolder.getDocId(), tiffDocHolder.getProcessInstance(),
               tiffDocHolder.getDocumentContent(), bookmarkList, tiffDocHolder.getMaxPageIndex());
         dialog.setImageInfo(imageInfo);
         dialog.openPopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
   
   /**
    * @param event
    */
   public void exitEditMode(ActionEvent event)
   {
      exitEditMode();      
      refreshPageBookmarks();
      tiffDocWrapper.setBookmarkChanged(true);
   }
   
   /**
    * @param event
    */
   public boolean exitEditMode()
   {
      boolean valid = true;
      
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();
      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isEditable())
         {
            if (arePageNumbersValid(be))
            {
               be.setEditable(false);
            }
            else
            {
               valid = false;
            }
         }    
      }
      
      return valid;
   }

   /**
    * @param event
    */
   public void toggleDefaultBookMark(ActionEvent event)
   {
      //Set existing bookmark to null
      ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).setDefaultBookmark(null);
      
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();
      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isSelected())
         {            
            be.toggleDefaultBookmark();
            if (be.isDefaultBookmark())
            {
               ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).setDefaultBookmark(be.getBookmarkText());
            }
            else
            {
               // It would be better to have the API enhanced to reset the default
               // bookmark rather than setting it to empty string.
               ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).setDefaultBookmark("");
            }
         }
         else
         {
            be.setDefaultBookmark(false);
         }
      }
      tiffDocWrapper.setBookmarkChanged(true);
   }

   /**
    * 
    */
   public void moveUp(ActionEvent event)
   {
      List<Integer> selIndexes = getAllSelectedBookmarkIndex();
      List<BookmarkEntry> bmEntries = bookmarkEntryTable.getList();
      for (int i : selIndexes)
      {
         BookmarkEntry be = bmEntries.remove(i);
         bmEntries.add(i - 1, be);
      }
      refreshPageBookmarks();
      tiffDocWrapper.setBookmarkChanged(true);
   }

   /**
    * 
    */
   public void moveDown(ActionEvent event)
   {
      List<Integer> selIndexes = getAllSelectedBookmarkIndex();
      Collections.reverse(selIndexes);
      List<BookmarkEntry> bmEntries = bookmarkEntryTable.getList();
      for (int i : selIndexes)
      {
         BookmarkEntry be = bmEntries.remove(i);
         bmEntries.add(i + 1, be);
      }
      refreshPageBookmarks();
      tiffDocWrapper.setBookmarkChanged(true);
   }

   /**
    * Returns default page if present, else returns first page as default.
    * 
    * @return
    */
   public String getDefaultPage()
   {
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();

      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isDefaultBookmark())
         {
            return String.valueOf(be.getFromPageNo());
         }
      }

      return "1";
   }

   /**
    * @return
    */
   public boolean isExactlyOneRowSelected()
   {
      if (getNumberOfSelectedRows() == 1)
      {
         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public boolean isAtLeastOneRowSelected()
   {
      if (getNumberOfSelectedRows() >= 1)
      {
         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public boolean isInEditMode()
   {
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();

      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isEditable())
         {
            return true;
         }
      }

      return false;
   }

   /**
    * @return
    */
   public boolean isMoreThanOneRowSelected()
   {
      if (getNumberOfSelectedRows() > 1)
      {
         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public boolean isFirstBMSelected()
   {
      List<BookmarkEntry> bmList = bookmarkEntryTable.getList();
      if (bmList.size() > 0 && bmList.get(0).isSelected())
      {
         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public boolean isLastBMSelected()
   {
      List<BookmarkEntry> bmList = bookmarkEntryTable.getList();
      if (bmList.size() > 0 && bmList.get(bmList.size() - 1).isSelected())
      {

         return true;
      }

      return false;
   }

   /**
    * @return
    */
   public int getNumberOfSelectedRows()
   {
      int count = 0;
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();

      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isSelected())
         {
            count++;
         }
      }

      return count;
   }

   /**
    * @return
    */
   public int getSelectedRowsIndex()
   {
      int count = 0;
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();

      while (iter.hasNext())
      {
         BookmarkEntry be = iter.next();
         if (be.isSelected())
         {
            count++;
         }
      }

      return count;
   }

   /**
    * 
    */
   private void createTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colBookText = new ColumnPreference("BookmarkText", "bookmarkText", ColumnDataType.STRING,
    		  MESSAGE_BEAN.getString("views.tiffViewer.bookmarks.columnHeader.bookmark"), true, false);
      colBookText.setColumnAlignment(ColumnAlignment.LEFT);
      colBookText.setColumnContentUrl("/plugins/views-common/views/document/tiffBookmarkColumns.xhtml");
      ColumnPreference colFromPg = new ColumnPreference("FromPageNo", "fromPageNo", ColumnDataType.STRING,
    		  MESSAGE_BEAN.getString("views.tiffViewer.bookmarks.columnHeader.fromPage"), true, false);
      colFromPg.setColumnAlignment(ColumnAlignment.CENTER);
      colFromPg.setColumnContentUrl("/plugins/views-common/views/document/tiffBookmarkColumns.xhtml");
      ColumnPreference colToPg = new ColumnPreference("ToPageNo", "toPageNo", ColumnDataType.STRING,
    		  MESSAGE_BEAN.getString("views.tiffViewer.bookmarks.columnHeader.toPage"), true, false);
      colToPg.setColumnContentUrl("/plugins/views-common/views/document/tiffBookmarkColumns.xhtml");

      colToPg.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colBookText);
      cols.add(colFromPg);
      cols.add(colToPg);

     
      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();

      

      IColumnModel bookmarkColumnModel = new DefaultColumnModel(cols, null, fixedAfterColumns,
            UserPreferencesEntries.M_VIEWS_COMMON, ResourcePaths.V_TIFF_BOOKMARKS_VIEW);

      // without column selector
      bookmarkEntryTable = new DataTable<BookmarkEntry>(null, bookmarkColumnModel, null);

      bookmarkEntryTable.setList(getStoredBookmarks());
      bookmarkEntryTable.setRowSelector(new DataTableRowSelector("selected",true));
      bookmarkEntryTable.initialize();
   }

   private PageBookmark getPageBookmarkFrom(String id, Integer startPage, Integer endPage)
   {
      PageBookmark bm = new PageBookmark();
      bm.setId(id);
      bm.setStartPage(startPage);
      bm.setEndPage(endPage);

      return bm;
   }
   
   /**
    * Dummy method - should retrieve from repo in real life scenario.
    * 
    * @return
    */
   private List<BookmarkEntry> getStoredBookmarks()
   {
      // TODO for now return empty string
      // replace with code to retrieve existing book-marks.
      List<BookmarkEntry> bookMarks = new ArrayList<BookmarkEntry>(pgBookmarks.size());
      //Set default book-mark if present
      PageBookmark defaultPb =((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).getDefaultBookmark();
      for (PageBookmark pb : pgBookmarks)
      {
         BookmarkEntry be = new BookmarkEntry();
         be.setRowDeselectionListener(this);
         be.setPageBookmark(pb);
         be.setBookmarkText(pb.getId());
         be.setFromPageNo(pb.getStartPage().toString());
         be.setToPageNo(pb.getEndPage().toString());
         bookMarks.add(pb.getOrder(), be);
         if (pb == defaultPb)
         {
            be.setDefaultBookmark(true);
         }
      }

      return bookMarks;
   }

   /**
    * @return
    */
   private int getSelectedBookmarkIndex()
   {
      List<BookmarkEntry> beList = bookmarkEntryTable.getList();
      Iterator<BookmarkEntry> iter = beList.iterator();
      int beIndex = -1;
      while (iter.hasNext())
      {
         beIndex++;
         if (iter.next().isSelected())
         {
            break;
         }
      }

      return beIndex;
   }

   /**
    * @return
    */
   private List<Integer> getAllSelectedBookmarkIndex()
   {
      Iterator<BookmarkEntry> iter = bookmarkEntryTable.getList().iterator();

      List<Integer> selBMs = new ArrayList<Integer>();
      int index = -1;
      while (iter.hasNext())
      {
         index++;
         if (iter.next().isSelected())
         {
            selBMs.add(index);
         }
      }

      return selBMs;
   }

   /**
    * 
    */
   private boolean arePageNumbersValid(BookmarkEntry bookmark) throws ValidatorException
   {
      if (bookmark.isEditable())
      {
         try
         {
            int fromPgNo = Integer.parseInt(bookmark.getFromPageNo());
            int toPgNo = Integer.parseInt(bookmark.getToPageNo());
            if (fromPgNo < tiffDocHolder.getMinPageIndex())
            {
               FacesUtils.addErrorMessage("", MESSAGE_BEAN.getParamString(
                     "views.tiffViewer.bookmarks.errorMsg.fromPgInvalid", String.valueOf(tiffDocHolder
                           .getMinPageIndex())));
               return false;
            }
            if (toPgNo > tiffDocHolder.getMaxPageIndex())
            {
               FacesUtils.addErrorMessage("", MESSAGE_BEAN
                     .getParamString("views.tiffViewer.bookmarks.errorMsg.toPgInvalid", String.valueOf(tiffDocHolder
                           .getMaxPageIndex())));
               return false;
            }
            if (fromPgNo > toPgNo)
            {
               FacesUtils.addErrorMessage("", MESSAGE_BEAN
                     .getString("views.tiffViewer.bookmarks.errorMsg.fromToInvalid"));
               return false;
            }
         }
         catch (Exception e)
         {
            FacesUtils.addErrorMessage("", MESSAGE_BEAN.getString("views.tiffViewer.bookmarks.errorMsg.nan"));
            return false;
         }
      }

      return true;
   }
   
   private void initDocument()
   {
      if (null == tiffDocInfo)
      {
         tiffDocWrapper = ((Map<String, TIFFDocumentWrapper>) SessionSharedObjectsMap.getCurrent().getObject("DOC_ID_VS_DOC_MAP")).get(tiffDocHolder.getDocId());
         tiffDocInfo = tiffDocWrapper.getDocInfo();
      }
   }

   
   /**
    * Sub-classed to add the setOrder method. 
    *
    */
   public class TIFFPageBookmark extends PageBookmark
   {
      public void setOrder(int order)
      {
         this.order = order;
      }
   }
   
   /**
    * 
    */
   private void refreshPageBookmarks()
   {
      List<BookmarkEntry> bmEntries = bookmarkEntryTable.getList();
      ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).removeAllBookmarks();
      for (BookmarkEntry bm : bmEntries)
      {
         PageBookmark pb = getPageBookmarkFrom(bm.getBookmarkText(), Integer.valueOf(bm.getFromPageNo()), Integer.valueOf(bm.getToPageNo()));
         try
         {
            ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).addBookmark(pb);
         }
         catch(Exception e)
         {
            FacesUtils.addErrorMessage("", MESSAGE_BEAN.getParamString(
                  "views.tiffViewer.bookmarks.errorMsg.duplicateBookmark", bm.getBookmarkText()));
            bmEntries.remove(bm);
            break;
         }
         bm.setPageBookmark(pb);
         if (bm.isDefaultBookmark())
         {
            ((PrintDocumentAnnotations) tiffDocInfo.getAnnotations()).setDefaultBookmark(bm.getBookmarkText());
         }
      }
   }

   /**
    * This is a workaround to get around an icefaces bug.
    * Icefaces bug: Programatically selected rows / moved rows are not de-selected by icefaces when another row is
    * selected.
    * 
    * Workaround: Whenever <<rowEntry>>#setSelected(boolean) is called with a "false" value, de-select all rows.
    * This requires the table bean to implement RowDeselectionListene.
    * In RowDeselectionListener#rowDeselected() deselect all rows.
    * This method needs to be invoked whenever <<rowEntry>>#setSelected is called with a "false" value.
    */
   public void rowDeselected()
   {
      for (BookmarkEntry be : bookmarkEntryTable.getList())
      {
         be.resetSelection();
      }
   }

   /**
    * @return
    */
   public boolean isDocumentEditable()
   {
      return documentEditable;
   }

   /**
    * @param documentEditable
    */
   public void setDocumentEditable(boolean documentEditable)
   {
      this.documentEditable = documentEditable;
   }
   
   private boolean canExtract()
   {
      if (!ImageViewerConfigurationBean.isExtractPagesEnable())
      {
         return false;
      }
      if (null != tiffDocHolder.getProcessInstance() && tiffDocHolder.getProcessInstance().isCaseProcessInstance())
      {
         return false;
      }

      if (null != tiffDocInfo && null == tiffDocHolder.getProcessInstance() && tiffDocInfo instanceof JCRDocument)
      {
         Document document = ((JCRDocument) tiffDocInfo).getDocument();
         ProcessInstances processInstances = DocumentMgmtUtility.findProcessesHavingDocument(document);

         if (null != processInstances && processInstances.size() == 1)
         {
            ProcessInstance pi = processInstances.get(0);
            if (pi.isCaseProcessInstance())
            {
               return false;
            }
         }
      }

      if (tiffDocInfo != null && tiffDocInfo instanceof JCRDocument)
      {
         return true;
      }     
      
      return false;
   }
   
   public boolean isExtractEnable()
   {
      return extractEnable;
   }
}