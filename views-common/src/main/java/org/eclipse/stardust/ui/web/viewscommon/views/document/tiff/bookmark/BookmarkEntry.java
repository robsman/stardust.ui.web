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
package org.eclipse.stardust.ui.web.viewscommon.views.document.tiff.bookmark;

import org.eclipse.stardust.engine.extensions.dms.data.annotations.printdocument.PageBookmark;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.table.RowDeselectionListener;


/**
 * @author Shrikant.Gangal
 * 
 */
public class BookmarkEntry extends DefaultRowModel
{
   private static final long serialVersionUID = -1882356679244979515L;

   private String bookmarkText;

   private String fromPageNo;

   private String toPageNo;

   private int hierarchyLevel;

   private boolean hasChildren;

   private boolean selected;

   private boolean editable;

   private boolean defaultBookmark;
   
   private PageBookmark pageBookmark;
   
   private RowDeselectionListener rowDeselectionListener;

   /**
    * 
    */
   public BookmarkEntry()
   {

   }

   public BookmarkEntry(String text, String fromPg, String toPg)
   {
      bookmarkText = text;
      fromPageNo = fromPg;
      toPageNo = toPg;
   }

   public String getBookmarkText()
   {
      return bookmarkText;
   }

   public void setBookmarkText(String bookmarkText)
   {
      this.bookmarkText = bookmarkText;
      if (null != pageBookmark)
      {
         pageBookmark.setId(this.bookmarkText);
      }
   }

   public String getFromPageNo()
   {
      return fromPageNo;
   }

   public void setFromPageNo(String fromPageNo)
   {
      this.fromPageNo = fromPageNo;
      if (null != pageBookmark)
      {
         pageBookmark.setStartPage(Integer.valueOf(this.fromPageNo));
      }
   }

   public String getToPageNo()
   {
      return toPageNo;
   }

   public void setToPageNo(String toPageNo)
   {
      this.toPageNo = toPageNo;
      if (null != pageBookmark)
      {
         pageBookmark.setEndPage(Integer.valueOf(this.toPageNo));
      }
   }

   public int getHierarchyLevel()
   {
      return hierarchyLevel;
   }

   public void setHierarchyLevel(int hierarchyLevel)
   {
      this.hierarchyLevel = hierarchyLevel;
   }

   public boolean isHasChildren()
   {
      return hasChildren;
   }

   public void setHasChildren(boolean hasChildren)
   {
      this.hasChildren = hasChildren;
   }

   /**
    * @return
    */
   public boolean isSelected()
   {
      return selected;
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
    * 
    * @param checkSelection
    */
   public void setSelected(boolean checkSelection)
   {
      if (!checkSelection && null != rowDeselectionListener)
      {
         rowDeselectionListener.rowDeselected();
      }
      this.selected = checkSelection;
   }
   
   public void resetSelection()
   {
      this.selected = false;
   }

   /**
    * @return
    */
   public boolean isEditable()
   {
      return editable;
   }

   /**
    * @param editable
    */
   public void setEditable(boolean editable)
   {
      this.editable = editable;
   }

   /**
    * @param editable
    */
   public void toggleEditable()
   {
      editable = !editable;
   }

   public boolean isDefaultBookmark()
   {
      return defaultBookmark;
   }

   public void setDefaultBookmark(boolean defaultBookmark)
   {
      this.defaultBookmark = defaultBookmark;
   }

   public void toggleDefaultBookmark()
   {
      defaultBookmark = !defaultBookmark;
   }

   public PageBookmark getPageBookmark()
   {
      return pageBookmark;
   }

   public void setPageBookmark(PageBookmark pageBookmark)
   {
      this.pageBookmark = pageBookmark;
   }

   public RowDeselectionListener getRowDeselectionListener()
   {
      return rowDeselectionListener;
   }

   public void setRowDeselectionListener(RowDeselectionListener rowDeselectionListener)
   {
      this.rowDeselectionListener = rowDeselectionListener;
   }
}
