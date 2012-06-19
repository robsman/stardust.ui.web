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
package org.eclipse.stardust.ui.web.common.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.table.IRowModel;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class TableDataFilters implements Serializable
{
   private static final Logger trace = LogManager.getLogger(TableDataFilters.class);

   private static final long serialVersionUID = 328878105221449051L;

   private Map<String, ITableDataFilter> filters;
   private List<ITableDataFilter> orderedFilters;
   
   /**
    * 
    */
   public TableDataFilters()
   {
      filters = new HashMap<String, ITableDataFilter>();
      orderedFilters = new ArrayList<ITableDataFilter>();
   }

   /**
    * 
    */
   public TableDataFilters(ITableDataFilter tableDataFilter)
   {
      this();
      addDataFilter(tableDataFilter);
   }
   
   /**
    * @param tableDataFilter
    */
   public void addDataFilter(ITableDataFilter tableDataFilter)
   {
      filters.put(tableDataFilter.getName(), tableDataFilter);
      orderedFilters.add(tableDataFilter);
   }
   
   /**
    * @param tableDataFilter
    */
   public void addDataFilters(TableDataFilters tableDataFilters)
   {
      for (ITableDataFilter tableDataFilter : tableDataFilters.getList())
      {
         addDataFilter(tableDataFilter);
      }
   }

   /**
    * @param name
    * @return
    */
   public ITableDataFilter getDataFilter(String name)
   {
      if(filters.containsKey("")) // For Column Data Filters Name is set Later, So recalculate
      {
         filters.clear();
         for (ITableDataFilter tableDataFilter : orderedFilters)
         {
            filters.put(tableDataFilter.getName(), tableDataFilter);
         }
      }

      return filters.get(name);
   }

   /**
    * @return
    */
   public int getCount()
   {
      return orderedFilters.size();
   }

   /**
    * @return
    */
   public boolean isFiltersSet()
   {
      for (ITableDataFilter colFilter : orderedFilters)
      {
         if( colFilter.isFilterSet() )
            return true;
      }  
      return false;
   }

   /**
    * @param show
    */
   public void setAllFiltersVisible(boolean visible)
   {
      for (ITableDataFilter tableDataFilter : orderedFilters)
      {
         setFilterVisible(tableDataFilter.getName(), visible);
      }
   }

   /**
    * @param name
    * @param visible
    */
   public void setFilterVisible(String name, boolean visible)
   {
      ITableDataFilter tableDataFilter = filters.get(name);
      if(tableDataFilter != null)
      {
         if(!visible) // Before Hiding Filter Clear it
         {
            tableDataFilter.resetFilter();
         }

         tableDataFilter.setVisible(visible);
      }
   }

   /**
    * @return
    */
   public String getFilterSummaryTitle()
   {
      StringBuffer summary = new StringBuffer();
      String str;
      for (ITableDataFilter tableDataFilter : orderedFilters)
      {
         if(tableDataFilter.isFilterSet())
         {
            str = tableDataFilter.getFilterSummaryTitle();
            if(!StringUtils.isEmpty(str))
            {
               if(summary.length() > 0)
                  summary.append("\n");
   
               summary.append(str);
            }
         }
      }
      
      return summary.toString();
   }

   /**
    * @param rowData
    * @return
    * @throws Exception
    */
   public boolean isFilterOut(IRowModel rowData)
   {
      for (ITableDataFilter tableDataFilter : orderedFilters)
      {
         if(tableDataFilter.isFilterOut(rowData))
            return true;
      }
      
      return false;
   }

   /**
    * @param rowData
    * @param filterNames
    * @return
    * @throws Exception
    */
   public boolean isFilterOut(IRowModel rowData, String... filterNames) throws Exception
   {
      ITableDataFilter tableDataFilter;

      for (String filterName : filterNames)
      {
         tableDataFilter = filters.get(filterName);
         if(tableDataFilter != null && tableDataFilter.isFilterOut(rowData))
            return true;

      }
      
      return false;

   }

   /**
    * @return
    */
   public List<ITableDataFilter> getSetFilters()
   {
      List<ITableDataFilter> setFilters = new ArrayList<ITableDataFilter>();
      for (ITableDataFilter tableDataFilter : getList())
      {
         if(tableDataFilter.isFilterSet())
            setFilters.add(tableDataFilter);
      }
      
      return setFilters;
   }

   /**
    * 
    */
   public void print()
   {
      for (ITableDataFilter colFilter : orderedFilters)
      {
         trace.debug(colFilter);
      } 
   }

   /**
    * 
    */
   public void resetFilters()
   {
      for (ITableDataFilter colFilter : orderedFilters)
      {
         colFilter.resetFilter();
      }
   }

   /**
    * @return
    */
   public TableDataFilters getClone()
   {
      TableDataFilters clone = new TableDataFilters();
      
      for (ITableDataFilter colFilter : orderedFilters)
      {
         clone.addDataFilter(colFilter.getClone());
      } 
      
      return clone;
   }
   
   /**
    * @param dataFiltersToCopy
    */
   public void copy(TableDataFilters dataFiltersToCopy)
   {
      ITableDataFilter tableDataFilter;
      for (ITableDataFilter dataFilterToCopy : dataFiltersToCopy.getList())
      {
         tableDataFilter = getDataFilter(dataFilterToCopy.getName());
         
         if(tableDataFilter != null)
         {
            tableDataFilter.copyValues(dataFilterToCopy);
         }
      }
   }
   
   /**
    * @return
    */
   public ArrayList<String> getValidationMessages()
   {
      ArrayList<String> messags = new ArrayList<String>();

      String msg;
      for (ITableDataFilter tableDataFilter : getList())
      {
         if(tableDataFilter.isFilterSet())
         {
            msg = tableDataFilter.getValidationMessage();
            if(StringUtils.isNotEmpty(msg))
            {
               messags.add(msg);
            }
         }
      }
      
      return messags;
   }
   
   public List<ITableDataFilter> getList()
   {
      return orderedFilters;
   }

   @Override
   public String toString()
   {
      return filters.toString();
   }
}
