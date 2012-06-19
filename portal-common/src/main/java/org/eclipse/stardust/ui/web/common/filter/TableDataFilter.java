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

import java.util.Random;

import org.eclipse.stardust.ui.web.common.column.ColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.table.IRowModel;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 *
 */
public abstract class TableDataFilter implements ITableDataFilter
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(TableDataFilter.class);

   private String id;
   private String name;
   private String property;
   private String title;
   private DataType dataType;
   private FilterCriteria filterCriteria;
   private boolean visible;
   
   /**
    * @param name
    * @param title
    * @param dataType
    * @param filterCriteria
    * @param visible
    */
   public TableDataFilter(String name, String title, DataType dataType,
         FilterCriteria filterCriteria, boolean visible)
   {
      this(name, "", title, dataType, filterCriteria, visible);
   }

   /**
    * @param name
    * @param title
    * @param dataType
    * @param filterCriteria
    * @param visible
    */
   public TableDataFilter(String name, String property, String title, DataType dataType,
         FilterCriteria filterCriteria, boolean visible)
   {
      this.name = name;
      this.property = property;
      this.title = title;
      this.dataType = dataType;
      this.filterCriteria = filterCriteria;
      this.visible = visible;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#isFilterOut(org.eclipse.stardust.ui.web.common.table.IRowModel)
    */
   public boolean isFilterOut(IRowModel rowData)
   {
      if(StringUtils.isEmpty(getProperty()))
         throw new IllegalStateException(
               "isFilterOut(): Can not perform this operation, Property not set for " + getName());

      try
      {
         Object value;
         if(ColumnPreference.isComplexProperty(getProperty()))
         {
            trace.debug("Using Complex Properties");
            value = ColumnModel.resolvePropertyAndInvokeGetter(rowData, property);
         }
         else
         {
            trace.debug("Using Simple Properties");
            value = ReflectionUtils.invokeGetterMethod(rowData, property);
         }
         
         return !contains(value);
      }
      catch(Exception e)
      {
         throw new IllegalArgumentException("Can not invoke Getter method for " + 
               getProperty() + "  on " + rowData);
      }
   }
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getValidationMessage()
    */
   public String getValidationMessage()
   {
      // Default Impl: No validation required
      return "";
   }

   public String getId()
   {
      if(StringUtils.isEmpty(id))
      {
         Random o = new Random();
         id = "DF" + o.nextInt(10000);
      }

      return id;
   }
   
   public String getTitle()
   {
      return StringUtils.isNotEmpty(title) ? title : name;
   }
   
   public DataType getDataType()
   {
      return dataType;
   }

   public String getName()
   {
      return name;
   }
   
   public void setName(String name)
   {
      this.name = name;
   }
   
   public String getProperty()
   {
      return property;
   }
   
   public void setProperty(String property)
   {
      this.property = property;
   }
   
   public FilterCriteria getFilterCriteria()
   {
      return filterCriteria;
   }

   public boolean isVisible()
   {
      return visible;
   }

   public void setVisible(boolean visible)
   {
      this.visible = visible;
   }

   @Override
   public String toString()
   {
      return getName() + ":" + isVisible() + ":" + isFilterSet() +  ":" + getDataType() + ":" + getFilterCriteria();
   }
}
