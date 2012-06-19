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
public class TableDataFilterOnOff extends TableDataFilter implements ITableDataFilterOnOff
{
   private static final long serialVersionUID = -5478359349253839507L;

   private Boolean on;
   

   public TableDataFilterOnOff(String name, String property, String title, boolean visible, boolean on)
   {
      super(name, property, title, DataType.BOOLEAN, FilterCriteria.ONOFF, visible);
      this.on = on;
   }
   
   /**
    * @param name
    * @param title
    * @param visible
    * @param on
    */
   public TableDataFilterOnOff(String name, String title, boolean visible, Boolean on)
   {
      super(name, title, DataType.BOOLEAN, FilterCriteria.ONOFF, visible);
      this.on = on;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#isFilterSet()
    */
   public boolean isFilterSet()
   {
      return on == null ? false : true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#contains(java.lang.Object)
    */
   public boolean contains(final Object compareValue)
   {
      if (compareValue instanceof Boolean)
      {
         Boolean value = (Boolean) compareValue;
         return !value;
      }
      return true;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#resetFilter()
    */
   public void resetFilter()
   {
      on = null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getFilterSummaryTitle()
    */
   public String getFilterSummaryTitle()
   {
      return String.valueOf(isOn());
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff#toggle()
    */
   public void toggle()
   {
      this.on = this.on != null ? !this.on : true; // null can be assumed as false
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getClone()
    */
   public ITableDataFilter getClone()
   {
      return new TableDataFilterOnOff(getName(), getTitle(), isVisible(), on);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#copy(org.eclipse.stardust.ui.web.common.filter.ITableDataFilter)
    */
   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      TableDataFilterOnOff filterToCopy = (TableDataFilterOnOff)dataFilterToCopy;
      on = filterToCopy.isFilterSet() ? filterToCopy.isOn() : null;
   }
   
   public boolean isOn()
   {
      return on != null ? on : false;
   }

   public void setOn(boolean on)
   {
      this.on = on;
   }

   public String getValue()
   {
      return isFilterSet() ? (isOn() ? "true" : "false") : null;
   }

   public void setValue(String value)
   {
      this.on = StringUtils.isEmpty(value) ? null : Boolean.valueOf(value);
   }
}
