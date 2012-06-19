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

import java.io.Serializable;
import java.util.Comparator;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;


/**
 * @author Subodh.Godbole
 *
 * @param <T>
 */
public class DataTableSortModel<T> implements Comparator<T>, Serializable
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(DataTableSortModel.class);

   protected String sortColumnProperty;
   protected boolean ascending;
   private String  oldSortColumnProperty;
   private boolean oldAscending;

   private boolean caseInsensitive;

   /**
    * @param sortColumnProperty
    * @param ascending
    * @param caseInsensitive
    * @param columnModel
    */
   public DataTableSortModel(String sortColumnProperty, boolean ascending, 
         boolean caseInsensitive)
   {
      this.sortColumnProperty = sortColumnProperty;
      this.ascending = ascending;
      this.caseInsensitive = caseInsensitive;
      
      initializeSortModel();
   }

   /**
    * Default: Case Insensitive Comparisons only for String values
    * @param sortColumnProperty
    * @param ascending
    */
   public DataTableSortModel(String sortColumnProperty, boolean ascending)
   {
      this(sortColumnProperty, ascending, true);
   }
   
   /**
    * method will reset old value so that sorting can be applied again
    */
   public void initializeSortModel()
   {
       oldSortColumnProperty = sortColumnProperty;
       oldAscending = !ascending; // To make sure Sort happens on first render 
   }

   /**
    * This resets the Sorting Model i.e. Old/New Column Name and Ascending
    * This is called after sorting is done
    */
   public void resetSortModel()
   {
      oldSortColumnProperty = sortColumnProperty;
      oldAscending = ascending;
   }

   /* (non-Javadoc)
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   public int compare(T o1, T o2)
   {
      throw new UnsupportedOperationException(
            "compare(T, T). This class is used just to hold the Sort Model. Create a subclass instead or use SortableTableComparator");
   }
   
   /**
    * @param sortColumnProperty
    */
   public void setSortColumnProperty(String sortColumnProperty)
   {
      oldSortColumnProperty = this.sortColumnProperty;
      this.sortColumnProperty = sortColumnProperty;
   }
   
   /**
    * @return
    */
   public boolean isSortCriteriaModified()
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("Sort: Old ->" + getOldSortColumnProperty() + ":" + isOldAscending());
         trace.debug("Sort: New ->" + getSortColumnProperty() + ":" + isAscending());
      }
      
      if (!getOldSortColumnProperty().equals(getSortColumnProperty())
         || isOldAscending() != isAscending())
      {
         return true;
      }
      
      return false;
   }

   @Override
   public String toString()
   {
      return sortColumnProperty + ":" + ascending;
   }

   public String getSortColumnProperty()
   {
      return sortColumnProperty;
   }

   public boolean isAscending()
   {
      return ascending;
   }

   public void setAscending(boolean ascending)
   {
      this.ascending = ascending;
   }

   public String getOldSortColumnProperty()
   {
      return oldSortColumnProperty;
   }

   public boolean isOldAscending()
   {
      return oldAscending;
   }

   public boolean isCaseInsensitive()
   {
      return caseInsensitive;
   }

   public void setCaseInsensitive(boolean caseInsensitive)
   {
      this.caseInsensitive = caseInsensitive;
   }
}
