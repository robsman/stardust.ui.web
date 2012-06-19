/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    ICEsoft Technologies Canada, Corp. - initial API and implementation
 *    SunGard CSA LLC                    - additional modifications
 *******************************************************************************/

//Note: This file is derived from http://anonsvn.icefaces.org/repo/tutorials/trunk/tutorials/icefaces1.8/samples/dataTable-JPA/dataTable/src/com/icesoft/icefaces/samples/datatable/ui/PagedListDataModel.java (r27696) 

package org.eclipse.stardust.ui.web.common.table;

import java.io.Serializable;

import javax.faces.model.DataModel;

/**
 * A special type of JSF DataModel to allow a datatable and datapaginator
 * to page through a large set of data without having to hold the entire
 * set of data in memory at once.
 * Any time a managed bean wants to avoid holding an entire dataset,
 * the managed bean should declare an inner class which extends this
 * class and implements the fetchData method. This method is called
 * as needed when the table requires data that isn't available in the
 * current data page held by this object.
 * This does require the managed bean (and in general the business
 * method that the managed bean uses) to provide the data wrapped in
 * a DataPage object that provides info on the full size of the dataset.
 */
public abstract class PagedListDataModel<T> extends DataModel implements Serializable
{
   private static final long serialVersionUID = 1L;

   private int pageSize;

   private int rowIndex;

   private DataPage<T> page;

   protected boolean dirtyData = false;

   /*
    * Create a datamodel that pages through the data showing the specified
    * number of rows on each page.
    * 
    * @param pageSize
    */
   public PagedListDataModel(int pageSize)
   {
      super();
      this.pageSize = pageSize;
      this.rowIndex = -1;
      this.page = null;
   }

   /**
    * Not used in this class; data is fetched via a callback to the
    * fetchData method rather than by explicitly assigning a list.
    */
   @Override
   public void setWrappedData(Object o)
   {
      throw new UnsupportedOperationException("setWrappedData");
   }

   @Override
   public int getRowIndex()
   {
      return rowIndex;
   }

   /**
    * Specify what the "current row" within the dataset is. Note that
    * the UIData component will repeatedly call this method followed
    * by getRowData to obtain the objects to render in the table.
    */
   @Override
   public void setRowIndex(int index)
   {
      rowIndex = index;
   }

   /**
    * Return the total number of rows of data available (not just the
    * number of rows in the current page!).
    */
   @Override
   public int getRowCount()
   {
      return getPage().getDatasetSize();
   }

   /**
    * Return a DataPage object; if one is not currently available then
    * fetch one. Note that this doesn't ensure that the datapage
    * returned includes the current rowIndex row; see getRowData.
    */
   private DataPage<T> getPage()
   {
      if (page != null)
         return page;

      int rowIndex = getRowIndex();
      int startRow = rowIndex;
      if (rowIndex == -1)
      {
         // even when no row is selected, we still need a page
         // object so that we know the amount of data available.
         startRow = 0;
      }

      // invoke method on enclosing class
      page = privateFetchPage(startRow, pageSize);
      return page;
   }

   /**
    * Return the object corresponding to the current rowIndex.
    * If the DataPage object currently cached doesn't include that
    * index or the data is marked as dirty, then fetchPage is called
    * to retrieve the appropriate page.
    */
   @Override
   public Object getRowData()
   {
      if (rowIndex < 0)
      {
         throw new IllegalArgumentException(
               "Invalid rowIndex for PagedListDataModel; not within page");
      }

      // ensure page exists; if rowIndex is beyond dataset size, then
      // we should still get back a DataPage object with the dataset size
      // in it...
      if (page == null)
      {
         page = privateFetchPage(rowIndex, pageSize);
      }

      // Check if rowIndex is equal to startRow,
      // useful for dynamic sorting on pages
      if (rowIndex == page.getStartRow() && dirtyData)
      {
         page = privateFetchPage(rowIndex, pageSize);
      }

      int datasetSize = page.getDatasetSize();
      int startRow = page.getStartRow();
      int nRows = page.getData().size();
      int endRow = startRow + nRows;

      if (rowIndex >= datasetSize)
      {
         throw new IllegalArgumentException("Invalid rowIndex");
      }

      if (rowIndex < startRow)
      {
         page = privateFetchPage(rowIndex, pageSize);
         startRow = page.getStartRow();
      }
      else if (rowIndex >= endRow)
      {
         page = privateFetchPage(rowIndex, pageSize);
         startRow = page.getStartRow();
      }
      
      int index = rowIndex - startRow;
      if (index < page.getDataSize())
      {
         return page.getData().get(index);
      }
      else
      {
         return null;
      }
   }

   @Override
   public Object getWrappedData()
   {
      return page.getData();
   }

   /**
    * Return true if the rowIndex value is currently set to a
    * value that matches some element in the dataset. Note that
    * it may match a row that is not in the currently cached
    * DataPage; if so then when getRowData is called the
    * required DataPage will be fetched by calling fetchData.
    */
   @Override
   public boolean isRowAvailable()
   {
      DataPage<T> page = getPage();
      if (page == null)
         return false;

      int rowIndex = getRowIndex();
      if (rowIndex < 0)
      {
         return false;
      }
      else if (rowIndex >= page.getDatasetSize())
      {
         return false;
      }
      else
      {
         return true;
      }
   }
   
   /**
    * @return
    */
   public int getPageIndex()
   {
      int pageNo = 0;
      if(page != null)
      {
         pageNo = (int)page.getStartRow() / pageSize;
      }
      
      return pageNo;
   }

   /**
    * @param startRow
    * @param pageSize
    * @return
    */
   private DataPage<T> privateFetchPage(int startRow, int pageSize)
   {
      setDirtyData(false);
      return fetchPage(startRow, pageSize);
   }
   
   /**
    * Method which must be implemented in cooperation with the
    * managed bean class to fetch data on demand.
    * 
    * @param startRow
    * @param pageSize
    * @return
    */
   public abstract DataPage<T> fetchPage(int startRow, int pageSize);

   public boolean isDirtyData()
   {
      return dirtyData;
   }
   
   public void setDirtyData(boolean dirtyData)
   {
      if(dirtyData)
         setDirtyData();
      else
         this.dirtyData = dirtyData;
   }

   public void setDirtyData()
   {
      dirtyData = true;
      if(this.dirtyData)
         page = null;
   }

   public DataPage<T> getDataPage()
   {
      return page;
   }
}
