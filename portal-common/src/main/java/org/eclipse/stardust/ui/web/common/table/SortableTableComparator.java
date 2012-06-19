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

import org.eclipse.stardust.ui.web.common.column.ColumnModel;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

/**
 * @author Subodh.Godbole
 *
 * @param <T>
 */
public class SortableTableComparator<T> extends DataTableSortModel<T>
{
   private static final long serialVersionUID = 219326549385549215L;
   private static final Logger trace = LogManager.getLogger(SortableTableComparator.class);

   /**
    * @param sortColumnProperty
    * @param ascending
    * @param caseInsensitive
    * @param columnModel
    */
   public SortableTableComparator(String sortColumnProperty, boolean ascending, 
         boolean caseInsensitive)
   {
      super(sortColumnProperty,ascending, caseInsensitive);
   }

   /**
    * Default: Case Insensitive Comparisons only for String values
    * @param sortColumnProperty
    * @param ascending
    */
   public SortableTableComparator(String sortColumnProperty, boolean ascending)
   {
      this(sortColumnProperty, ascending, true);
   }

   /* (non-Javadoc)
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   public int compare(T o1, T o2)
   {
      try
      {
         Object value1 = ColumnModel.resolvePropertyAndInvokeGetter(o1, sortColumnProperty);
         Object value2 = ColumnModel.resolvePropertyAndInvokeGetter(o2, sortColumnProperty);
         
         if(ascending)
            return compareObjects(value1, value2);
         else
            return compareObjects(value2, value1);
      }
      catch(Exception e)
      {
         trace.error(e);
      }

      return 0;
   }
   
   /**
    * @param value1
    * @param value2
    * @return
    */
   protected int compareObjects(Object value1, Object value2)
   {
      if(value1 != null && value2 != null)
      {
         if(value1 instanceof Comparable)
         {
            if(value1 instanceof String && value2 instanceof String && isCaseInsensitive())
            {
               return ((String)value1).compareToIgnoreCase((String)value2);
            }
            else
            {
               Comparable comp = (Comparable)value1;
               return comp.compareTo(value2);
            }
         }
         else
         {
            String str = "SortableTableComparator: Value is not an instance of Comparable, " + 
               value1.getClass().getName() + ", Please write your own comparator";
            trace.debug(str);
            throw new IllegalArgumentException(str);
         }
      } 
      else if (null == value1)
      {
         return -1;
      }
      else if (null == value2)
      {
         return 1;
      }
      return 0;
   }
}
