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

import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 * 
 */
public abstract class TableDataFilterBetween extends TableDataFilter implements ITableDataFilterBetween
{
   private static final long serialVersionUID = 4276094614258630061L;

   private Object startValue;
   private Object endValue;

   /**
    * @param name
    * @param title
    * @param dataType
    * @param filterCriteria
    * @param visible
    * @param startValue
    * @param endValue
    */
   public TableDataFilterBetween(String name, String title, DataType dataType, FilterCriteria filterCriteria,
         boolean visible, Object startValue, Object endValue)
   {
      super(name, title, dataType, filterCriteria, visible);
      this.startValue = startValue;
      this.endValue = endValue;

   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#isFilterSet()
    */
   public boolean isFilterSet()
   {
      return (getReturnValue(startValue, false) != null || getReturnValue(endValue, false) != null) ? true : false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#resetFilter()
    */
   public void resetFilter()
   {
      startValue = null;
      endValue = null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween#getStartValueAsDataType
    * ()
    */
   public Object getStartValueAsDataType()
   {
      return getReturnValue(startValue, false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween#getEndValueAsDataType
    * ()
    */
   public Object getEndValueAsDataType()
   {
      return getReturnValue(endValue, false);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getFilterSummaryTitle()
    */
   public String getFilterSummaryTitle()
   {
      String str = "";
      if (isFilterSet())
      {
         String startVal = getFormatedValue(startValue);
         String endVal = getFormatedValue(endValue);

         if (!StringUtils.isEmpty(startVal))
         {
            str += startVal;
         }
         if (!StringUtils.isEmpty(endVal))
         {
            if (!StringUtils.isEmpty(str))
               str += " - ";

            str += endVal;
         }
      }

      return str;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#copyValues(org.eclipse.stardust.ui.web.common.filter.ITableDataFilter)
    */
   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      TableDataFilterBetween filterToCopy = (TableDataFilterBetween) dataFilterToCopy;
      setStartValue(filterToCopy.getStartValue());
      setEndValue(filterToCopy.getEndValue());
   }

   /**
    * @param startValue2
    * @param b
    * @return
    */
   protected abstract Object getReturnValue(Object startValue2, boolean b);

   /**
    * @param value
    * @return
    */
   protected abstract String getFormatedValue(final Object value);

   public Object getStartValue()
   {
      return startValue;
   }

   public void setStartValue(Object o)
   {
      startValue = o;
   }

   public Object getEndValue()
   {
      return endValue;
   }

   public void setEndValue(Object o)
   {
      endValue = o;
   }

   @Override
   public String toString()
   {
      return super.toString() + ":" + getStartValue() + ":" + getEndValue();
   }
}
