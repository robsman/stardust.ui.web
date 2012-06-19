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
package org.eclipse.stardust.ui.web.common.table;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.DataModel;

import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.configuration.ConfigurationConstants;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.springframework.util.CollectionUtils;

import com.icesoft.faces.component.datapaginator.DataPaginator;

/**
 * @author Subodh.Godbole
 * @param <T>
 */
public class PaginatorDataTable<T extends IRowModel, E> extends DataTable<T>
      implements UserPreferencesEntries, ConfigurationConstants
{
   private static final long serialVersionUID = 1L;
 
   private static final Logger trace = LogManager.getLogger(PaginatorDataTable.class);

   protected ISearchHandler<E> iSearchHandler;
   protected IFilterHandler iFilterHandler;

   protected ISortHandler iSortHandler;
   protected DataTableSortModel<T> sortModel;
   
   private IUserObjectBuilder<T> userObjectBuilder;
   
   private boolean supportFastStep = true; // TODO Move Default to Property File
   private int pageSize;
   private int paginatorMaxPages;
   private int paginatorFastStep;
   
   private DataPaginator dataPaginator;

   private PagedListDataModel<T> onePageDataModel;
   protected IQuery orgQuery;
   protected IQuery query;

   private boolean refreshed = false;
   private boolean totalCountSuported;
  
   /**
    * @param columnModel
    * @param iSearchHandler
    * @param iFilterHandler
    * @param userObjectBuilder
    */
   public PaginatorDataTable(IColumnModel columnModel, ISearchHandler<E> iSearchHandler,
         IFilterHandler iFilterHandler, IUserObjectBuilder<T> userObjectBuilder)
   {
      super(columnModel, null);
      this.iSearchHandler = iSearchHandler;
      this.iFilterHandler = iFilterHandler;
      this.userObjectBuilder = userObjectBuilder;
   }

   /**
    * @param columnSelectorPopup
    * @param iSearchHandler
    * @param iFilterHandler
    * @param userObjectBuilder
    */
   public PaginatorDataTable(TableColumnSelectorPopup columnSelectorPopup,
         ISearchHandler<E> iSearchHandler, IFilterHandler iFilterHandler,
         IUserObjectBuilder<T> userObjectBuilder)
   {
      super(columnSelectorPopup, (TableDataFilters)null);
      this.iSearchHandler = iSearchHandler;
      this.iFilterHandler = iFilterHandler;
      this.userObjectBuilder = userObjectBuilder;
   }
   
   /**
    * @param columnModel
    * @param searchHandler
    * @param filterHandler
    * @param iSortHandler
    * @param userObjectBuilder
    * @param DataTableSortModel
    */
   public PaginatorDataTable(IColumnModel columnModel, ISearchHandler<E> searchHandler,
         IFilterHandler filterHandler, ISortHandler iSortHandler,
         IUserObjectBuilder<T> userObjectBuilder, DataTableSortModel<T> sortModel)
   {
      this(columnModel, searchHandler, filterHandler, userObjectBuilder);
      this.iSortHandler = iSortHandler;
      this.sortModel = sortModel;
   }

   /**
    * @param columnSelectorPopup
    * @param searchHandler
    * @param filterHandler
    * @param iSortHandler
    * @param userObjectBuilder
    * @param DataTableSortModel
    */
   public PaginatorDataTable(TableColumnSelectorPopup columnSelectorPopup,
         ISearchHandler<E> searchHandler, IFilterHandler filterHandler,
         ISortHandler iSortHandler, IUserObjectBuilder<T> userObjectBuilder,
         DataTableSortModel<T> sortModel)
   {
      this(columnSelectorPopup, searchHandler, filterHandler, userObjectBuilder);
      this.iSortHandler = iSortHandler;
      this.sortModel = sortModel;
   }
   
   /**
    * Creates the Query again and refreshes the UI
    * And keeps the paginator on current Page if keepPageIndex = true 
    */
   public void refresh(boolean keepPageIndex)
   {
      orgQuery = null; // Refresh deserves refreshing fully, even building query again
      applyFilteringAndSorting(true, keepPageIndex);
      allRowsSelected = false;
   }
   
   /**
    * Creates the Query again and refreshes the UI
    * And resets the paginator to 1st Page
    */
   public void refresh()
   {
      refresh(false);
   }

   /**
    * @param sortModel
    */
   public void refresh(DataTableSortModel<T> sortModel)
   {
      // Reset Filters and Sort Model
      this.sortModel = sortModel;
      this.sortModel.resetSortModel(); // With PaginatorTable, Believe that sort has happened while rendering first time
      
      refresh();
   }

   /**
    * Only refreshes the UI with previous Query.
    * Doesn't create the query again.
    * And keeps the paginator on same page if keepPageIndex = true
    */
   @SuppressWarnings("unchecked")
   public void refreshUI(boolean keepPageIndex)
   {
      if(!keepPageIndex)
      {
         gotoFirstPage();
      }

      refreshed = true;
      ((PagedListDataModel)getDataModel()).setDirtyData();
      
      allRowsSelected = false;
   }

   /**
    * Only refreshes the UI with previous Query.
    * Doesn't create the query again.
    * And resets the paginator to 1st page
    */
   public void refreshUI()
   {
      refreshUI(false);
   }

   @Override
   public void initialize()
   {
      super.initialize();

      if(sortModel != null)
         sortModel.resetSortModel(); // With PaginatorTable, Believe that sort has happened while rendering first time
      
      UserPreferencesHelper prefHelper = UserPreferencesHelper.getInstance(M_PORTAL);
      pageSize = prefHelper.getInteger(V_PORTAL_CONFIG, F_PAGINATOR_PAGE_SIZE,
            DEFAULT_PAGE_SIZE);
      paginatorMaxPages = prefHelper.getInteger(V_PORTAL_CONFIG, F_PAGINATOR_MAX_PAGES,
            DEFAULT_MAX_PAGES);
      paginatorFastStep = prefHelper.getInteger(V_PORTAL_CONFIG, F_PAGINATOR_FAST_STEP,
            DEFAULT_FAST_STEP);
   }

   @Override
   public List<T> getList()
   {
      // throw new UnsupportedOperationException("getList");
      return getCurrentList();
   }

   @Override
   public void setList(List<T> list)
   {
      throw new UnsupportedOperationException("setList");
   }

   @Override
   public int getRowCount()
   {
      return getDataModel().getRowCount();
   }
   
   @Override
   public void applyFilter(TableDataFilters tableDataFilters)
   {
      try
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Paginator Table:applyFilter() = " + dataFilters);
         }
         
         if(query != null)
         {
            applyFilteringAndSorting(false, false);
         }
         else
         {
            trace.debug("Paginator Table:applyFilter(): Query is not yet fired");
         }
      }
      catch(Exception e)
      {
         trace.error(e);
      }
   }
   
   /**
    * @return
    */
   public List<T> getCurrentList()
   {
      List<T> data = new ArrayList<T>();
      if(onePageDataModel != null && onePageDataModel.getDataPage() != null)
         data  = onePageDataModel.getDataPage().getData();

      return data;
   }

   /**
    * @return
    */
   public DataModel getDataModel()
   {
      if (onePageDataModel == null)
      {
         onePageDataModel = new LocalDataModel(pageSize);
      }

      checkForSortCriteria();

      return onePageDataModel;
   }

   /**
    * 
    */
   public void gotoFirstPage()
   {
      gotoPage(0);
   }

   /**
    * @param page
    */
   public void gotoPage(int page)
   {
      if (getDataPaginator() != null)
      {
         if (getDataPaginator().getUIData() != null)
         {
            getDataPaginator().getUIData().setFirst(page);
         }
      }
   }
   
   /**
    * 
    */
   private void checkForSortCriteria()
   {
      if (sortModel != null && sortModel.isSortCriteriaModified())
      {
         trace.debug("Sorting ...");
         sortModel.resetSortModel();
         applyFilteringAndSorting(false, false);
      }
   }
   
   /**
    * @param forceRefresh
    * @param keepPageIndex
    */
   private void applyFilteringAndSorting(boolean forceRefresh, boolean keepPageIndex)
   {
      boolean applied = false;

      if (null == orgQuery)
      {
         orgQuery = getISearchHandler().buildQuery();
      }

      query = orgQuery.getClone();

      // Apply Filtering
      if(iFilterHandler != null && dataFilters != null)
      {
         iFilterHandler.applyFiltering(query, dataFilters.getSetFilters());
         applied = true;
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Paginator Table:applyFiltering() - Not applying Filters : " + iFilterHandler + ":" + dataFilters);
         }
      }

      // Apply Sorting
      if(iSortHandler != null)
      {
         List<SortCriterion> sortCriteria = new ArrayList<SortCriterion>();
         sortCriteria.add(new SortCriterion(sortModel.getSortColumnProperty(), sortModel.isAscending()));
         iSortHandler.applySorting(query, sortCriteria);
         applied = true;
      }
      else
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Paginator Table:applySorting() - Not applying Sorting Sort Handler is Null: " + iSortHandler);
         }
      }

      if(forceRefresh || applied)
         refreshUI(keepPageIndex);
   }
   

   public int getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

   public int getPaginatorMaxPages()
   {
      return paginatorMaxPages;
   }

   public void setPaginatorMaxPages(int paginatorMaxPages)
   {
      this.paginatorMaxPages = paginatorMaxPages;
   }

   public int getPaginatorFastStep()
   {
      return paginatorFastStep;
   }

   public void setPaginatorFastStep(int paginatorFastStep)
   {
      this.paginatorFastStep = paginatorFastStep;
   }

   public boolean isSupportFastStep()
   {
      return supportFastStep;
   }

   public void setSupportFastStep(boolean supportFastStep)
   {
      this.supportFastStep = supportFastStep;
   }
   
   public ISearchHandler<E> getISearchHandler()
   {
      return iSearchHandler;
   }

   public void setISearchHandler(ISearchHandler<E> searchHandler)
   {
      // Set this to null so that buildQuery() gets called again. This is expected because ISearchHandler has changed
      orgQuery = null;
      iSearchHandler = searchHandler;
   }

   public IFilterHandler getIFilterHandler()
   {
      return iFilterHandler;
   }

   public void setIFilterHandler(IFilterHandler filterHandler)
   {
      iFilterHandler = filterHandler;
   }
   
   public ISortHandler getISortHandler()
   {
      return iSortHandler;
   }

   public void setISortHandler(ISortHandler sortHandler)
   {
      iSortHandler = sortHandler;
   }

   public DataTableSortModel<T> getSortModel()
   {
      return sortModel;
   }

   public void setSortModel(DataTableSortModel<T> sortModel)
   {
      this.sortModel = sortModel;
   }

   public DataPaginator getDataPaginator()
   {
      return dataPaginator;
   }

   public void setDataPaginator(DataPaginator dataPaginator)
   {
      this.dataPaginator = dataPaginator;
   } 

   public boolean isTotalCountSuported()
   {
      return totalCountSuported;
   }

   public void setTotalCountSuported(boolean totalCountSuported)
   {
      this.totalCountSuported = totalCountSuported;
   }

   /**
    * @author Subodh.Godbole
    */
   private class LocalDataModel extends PagedListDataModel<T>
   {
      private static final long serialVersionUID = 1L;

      public LocalDataModel(int pageSize)
      {
         super(pageSize);
      }

      public DataPage<T> fetchPage(int startRow, int pageSize)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("fetchPage = " + startRow + ":" + pageSize);
         }
         
         ArrayList<T> pageData = new ArrayList<T>();

         try
         {
            if(refreshed)
            {
               // GET DATA
               IQueryResult<E> queryResult = iSearchHandler.performSearch(query, startRow, pageSize);
               
               int totalCount = 0;
               if (queryResult != null)
               {
                  try
                  {
                     long queryCount = queryResult.getTotalCount();
                     queryCount = Long.MAX_VALUE;
                     if (Long.MAX_VALUE != queryCount)
                     {
                        totalCount = (int)queryCount;
                        setTotalCountSuported(true);
                     }
                     else
                     {
                        if (trace.isDebugEnabled())
                        {
                           trace.debug("getTotalCount() not suported! Table will not display Full Pagnation.");
                        }
                        totalCount = Integer.MAX_VALUE;
                        setTotalCountSuported(false);
                     }
                  }
                  catch (UnsupportedOperationException uoe)
                  {
                     if (trace.isDebugEnabled())
                     {
                        trace.debug("getTotalCount() not suported! Table will not display Full Pagnation.");
                     }
                     totalCount = Integer.MAX_VALUE;
                     setTotalCountSuported(false);
                  }
               }
               else
               {
                  trace.error("QueryResult received is NULL");
               }
   
               if (trace.isDebugEnabled())
               {
                  trace.debug("totalCount = " + totalCount);
               }
               
               if (null == userObjectBuilder)
               {
                  throw new IllegalStateException("IUserObjectBuilder is Null");
               }
               
               if (queryResult != null)
               {
                  List<E> data = queryResult.getData();
                  if (!isTotalCountSuported())
                  {
                     if (CollectionUtils.isEmpty(data))
                     {
                        totalCount = startRow;
                     }
                     else if (data.size() != pageSize)
                     {
                        totalCount = startRow + data.size();
                     }
                     
                     // Limitation: If Last page data is exactly equal to pageSize then
                     // there is no way to identify end of records situation, and -
                     // - Still next page action will be active, and upon clicking, it will display next page with no data
                     // - And on that page next page action will be disabled  
                  }
                  
                  for (Object result : queryResult.getData())
                  {
                     trace.debug("Adding Record");
                     pageData.add(userObjectBuilder.createUserObject(result));
                  }
               }
               
               return new DataPage<T>(totalCount, startRow, pageData);
            }
         }
         catch(Exception e)
         {
            MessageDialog.addErrorMessage(MessagePropertiesBean.getInstance().getString("common.unknownError"), e);
         }
         
         return new DataPage<T>(0, 0, pageData);
      }
   }
}
