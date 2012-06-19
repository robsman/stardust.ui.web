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
public class TableDataFilterSearch extends TableDataFilter implements ITableDataFilterSearch
{
   private static final long serialVersionUID = 8110877589242277920L;

   public static final int MAX_SUMMARY_LENGTH = 35;
   
   private String value;
   private boolean ignoreCase = true;
   private boolean useRegularExpression = true;

   /**
    * @param name
    * @param title
    * @param ignorecase
    * @param visible
    * @param value
    */
   public TableDataFilterSearch(String name, String title, boolean ignorecase, 
         boolean visible, String value)
   {
      super(name, title, DataType.STRING, FilterCriteria.SEARCH, visible);
      this.value = value;
      this.ignoreCase = ignorecase;
   }

   /**
    * @param name
    * @param title
    * @param ignorecase
    * @param value
    */
   public TableDataFilterSearch(String name, String title, boolean ignorecase, String value)
   {
      this(name, title, ignorecase, true, value);
   }

   /**
    * This is used when Column Name is to be set Later
    * And No Title to the Filter
    */
   public TableDataFilterSearch()
   {
      this("", "", true, true, "");
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#isFilterSet()
    */
   public boolean isFilterSet()
   {
      return StringUtils.isEmpty(value) ? false : true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#resetFilter()
    */
   public void resetFilter()
   {
      value = null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getFilterSummaryTitle()
    */
   public String getFilterSummaryTitle()
   {
      String str = isFilterSet() ? value : "";
      if(str.length() > MAX_SUMMARY_LENGTH)
      {
         str = str.substring(0, MAX_SUMMARY_LENGTH);
         str += "...";
      }
      return str;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#contains(java.lang.Object)
    */
   public boolean contains(final Object compareValue)
   {
      if(!isFilterSet() || compareValue == null || value == null)
         return true;

      if(compareValue instanceof String)
      {
         String compareString = ((String)compareValue).trim();
         String valueString = value.trim();

         if (useRegularExpression)
         {
            String regEx = valueString.replaceAll("\\*", ".*") + ".*";
            
            if (ignoreCase)
            {
               return compareString.toUpperCase().matches(regEx.toUpperCase());
            }
            else
            {
               return compareString.matches(regEx);
            }
         }
         else
         {
            if(ignoreCase)
            {
               return compareString.toUpperCase().contains(valueString.toUpperCase());
            }
            else
            {
               return compareString.contains(valueString);
            }
         }
      }
      else
      {
         throw new IllegalArgumentException("Argument expected is String, Received: " + 
               compareValue.getClass().getName());
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getClone()
    */
   public ITableDataFilter getClone()
   {
      return new TableDataFilterSearch(getName(), getTitle(), ignoreCase,
            isVisible(), value);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#copy(org.eclipse.stardust.ui.web.common.filter.ITableDataFilter)
    */
   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      TableDataFilterSearch filterToCopy = (TableDataFilterSearch)dataFilterToCopy;
      setValue(filterToCopy.getValue());
   }

   public String getValue()
   {
      return value;
   }

   public void setValue(String s)
   {
      value = s;
   }

   public void setIgnoreCase(boolean ignoreCase)
   {
      this.ignoreCase = ignoreCase;
   }

   public void setUseRegularExpression(boolean useRegularExpression)
   {
      this.useRegularExpression = useRegularExpression;
   }

   @Override
   public String toString()
   {
      return super.toString() + ":" + getValue();
   }
}
