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

import java.util.Collections;
import java.util.List;

import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;


/**
 * @author Subodh.Godbole
 * 
 */
public class SortableTable<T extends IRowModel> extends DataTable<T>
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(SortableTable.class);
   protected SortableTableComparator<T> comparator;

   /**
    * @param columnModel
    * @param dataFilters
    * @param comparator
    */
   public SortableTable(IColumnModel columnModel, TableDataFilters dataFilters,
         SortableTableComparator<T> comparator)
   {
      this(null, columnModel, dataFilters, comparator);
   }

   /**
    * @param list
    * @param columnModel
    * @param dataFilters
    * @param comparator
    */
   public SortableTable(List<T> list, IColumnModel columnModel,
         TableDataFilters dataFilters, SortableTableComparator<T> comparator)
   {
      super(list, columnModel, dataFilters);
      setComparator(comparator);
   }

   /**
    * @param columnSelectorPopup
    * @param dataFilterPopup
    * @param comparator
    */
   public SortableTable(TableColumnSelectorPopup columnSelectorPopup,
         TableDataFilterPopup dataFilterPopup, SortableTableComparator<T> comparator)
   {
      this(null, columnSelectorPopup, dataFilterPopup, comparator);
   }

   /**
    * @param list
    * @param columnSelectorPopup
    * @param dataFilterPopup
    * @param comparator
    */
   public SortableTable(List<T> list, TableColumnSelectorPopup columnSelectorPopup,
         TableDataFilterPopup dataFilterPopup, SortableTableComparator<T> comparator)
   {
      super(list, columnSelectorPopup, dataFilterPopup);
      setComparator(comparator);
   }

   public SortableTableComparator<T> getComparator()
   {
      return comparator;
   }

   /**
    * @return
    */
   public List<T> getList()
   {
      trace.debug("Returnung List (Sorted)");
      if (list != null && comparator != null)
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Sort: List = " + list != null ? "List Not Null" : "");
         }
         
         if(comparator.isSortCriteriaModified())
         {
            trace.debug("Sorting");
            Collections.sort(list, comparator);            
            comparator.resetSortModel();
            
            if (null != getRowSelector())
            {
               getRowSelector().resetSelectedRow(list);
            }
         }
      }

      return list;
   }

   @Override
   public void initialize()
   {
      getComparator().initializeSortModel();
      super.initialize();
   }

   /**
    * @param comparator
    */
   private void setComparator(SortableTableComparator<T> comparator)
   {
      this.comparator = comparator;
   }
}
