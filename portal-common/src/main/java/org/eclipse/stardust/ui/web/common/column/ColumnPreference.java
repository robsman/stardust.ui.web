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
package org.eclipse.stardust.ui.web.common.column;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 * 
 */
public class ColumnPreference implements Serializable
{
   private final static long serialVersionUID = 1l;

   // E.g. "user", "user.firstName", "user.roles[2].name"
   private static final String PROPERTY_REG_EX = "^(" +
                                 "([a-zA-Z\\_][0-9a-zA-Z\\_]*)"+ 
                                 "(\\[[0-9]+\\][.][a-zA-Z\\_][0-9a-zA-Z\\_]*)*" +
                                 "([.][a-zA-Z\\_][0-9a-zA-Z\\_]*)*" +
                                 ")*$";
   
   public static enum ColumnDataType
   {
      STRING,
      NUMBER,
      DATE,
      BOOLEAN,
      NONE
   }

   public static enum ColumnAlignment
   {
      LEFT,
      CENTER,
      RIGHT
   }

   public static enum ColumnConverterType
   {
      DATE,
      BOTH,
      NUMBER,
      CURRENCY,
      PERCENTAGE
   }
   
   public static enum ColumnRenderType
   {
      READ_ONLY,
      READ_WRITE,
   }

   protected String columnName;
   protected String columnProperty;
   protected ColumnDataType columnDataType;
   protected ColumnRenderType columnRenderType = ColumnRenderType.READ_ONLY;
   protected String columnTitle;
   protected TableDataFilterPopup columnDataFilterPopup;
   protected String columnContentUrl;
   
   protected Boolean visible = Boolean.TRUE;
   protected Boolean newlyVisible = Boolean.FALSE;
   protected Boolean sortable = Boolean.TRUE;
   protected Boolean noWrap = Boolean.FALSE;
   protected boolean escape = true;
   protected boolean exportable = Boolean.TRUE; // When data is exported, Allow export
   protected String width;
   
   protected ColumnAlignment columnAlignment = ColumnAlignment.LEFT;
   
   protected ColumnConverterType columnConverterType;
   
   protected List<ColumnPreference> children = new ArrayList<ColumnPreference>();
   protected Integer totalLeafCount;

   /**
    * @param columnName
    * @param columnProperty
    * @param columnDataType
    * @param columnTitle
    * @param columnDataFilterPopup
    */
   public ColumnPreference(String columnName, String columnProperty,
         ColumnDataType columnDataType, String columnTitle, TableDataFilterPopup columnDataFilterPopup)
   {
      this.columnName = columnName;
      setColumnProperty(columnProperty);
      this.columnDataType = columnDataType;
      this.columnTitle = columnTitle;
      setColumnDataFilterPopup(columnDataFilterPopup);
      
      adjustColumnAlignment();
   }

   /**
    * @param columnName
    * @param columnProperty
    * @param columnDataType
    * @param columnTitle
    */
  
   public ColumnPreference(String columnName, String columnProperty,
         ColumnDataType columnDataType, String columnTitle)
   {
      this(columnName, columnProperty, columnDataType, columnTitle, null);
   }


   /**
    * @param columnName
    * @param columnProperty
    * @param columnDataType
    * @param columnTitle
    * @param columnDataFilterPopup
    * @param visible
    * @param sortable
    */
   public ColumnPreference(String columnName, String columnProperty,
         ColumnDataType columnDataType, String columnTitle, TableDataFilterPopup columnDataFilterPopup,
         boolean visible, boolean sortable)
   {
      this(columnName, columnProperty, columnDataType, columnTitle);
      setColumnDataFilterPopup(columnDataFilterPopup);
      this.visible = visible;
      this.sortable = sortable;
   }

   /**
    * @param columnName
    * @param columnProperty
    * @param columnDataType
    * @param columnTitle
    * @param visible
    * @param sortable
    */
   public ColumnPreference(String columnName, String columnProperty,
         ColumnDataType columnDataType, String columnTitle, boolean visible, boolean sortable)
   {
      this(columnName, columnProperty, columnDataType, columnTitle, null, visible, sortable);
   }

   /**
    * @param columnName
    * @param columnProperty
    * @param columnTitle
    * @param columnContentUrl
    * @param columnDataFilterPopup
    * @param visible
    * @param sortable
    */
   public ColumnPreference(String columnName, String columnProperty, String columnTitle,
         String columnContentUrl, TableDataFilterPopup columnDataFilterPopup,
         boolean visible, boolean sortable)
   {
      this(columnName, columnProperty, null, columnTitle);
      this.columnContentUrl = columnContentUrl;
      setColumnDataFilterPopup(columnDataFilterPopup);
      this.visible = visible;
      this.sortable = sortable;

      if(StringUtils.isEmpty(columnContentUrl))
         throw new IllegalArgumentException("columnContentUrl can not be empty");
   }

   /**
    * @param columnName
    * @param columnProperty
    * @param columnTitle
    * @param columnContentUrl
    * @param visible
    * @param sortable
    */
   public ColumnPreference(String columnName, String columnProperty, String columnTitle,
         String columnContentUrl, boolean visible, boolean sortable)
   {
      this(columnName, columnProperty, columnTitle, columnContentUrl, null, visible, sortable);
   }

   /**
    * Primarily used by Column Group
    * @param columnName
    * @param columnTitle
    */
   public ColumnPreference(String columnName, String columnTitle)
   {
      this(columnName, "", ColumnDataType.STRING, columnTitle);
      sortable = Boolean.FALSE;
   }

   /**
    * @param columnProperty
    * @return Return true if Property Mapping is a complex one. Contains "." or "[]"
    */
   public static boolean isComplexProperty(String columnProperty)
   {
      if(!StringUtils.isEmpty(columnProperty))
      {
         return columnProperty.contains(".") || columnProperty.contains("[") ;
      }

      return false;
   }

   /**
    * @return
    */
   public boolean isComplexProperty()
   {
      return isComplexProperty(columnProperty);
   }

   /**
    * @return
    */
   public ColumnConverterType getColumnConverterType()
   {
      if(columnConverterType == null)
      {
         if(columnDataType == ColumnDataType.DATE)
            columnConverterType = ColumnConverterType.BOTH;
         else if(columnDataType == ColumnDataType.NUMBER)
            columnConverterType = ColumnConverterType.NUMBER;
      }

      return columnConverterType;
   }

   /**
    * @return
    */
   public String getColumnFormatter()
   {
      if(ColumnDataType.DATE == columnDataType)
      {
         switch (getColumnConverterType())
         {
         case DATE:
            return DateUtils.getDateFormat();

         case BOTH:
            return DateUtils.getDateTimeFormat();
         }
      }
      
      return "";
   }

   /**
    * @param colPref
    */
   public void addChildren(ColumnPreference colPref)
   {
      children.add(colPref);
   }
   
   /**
    * @return
    */
   public int getChildrenDepth()
   {
      // TODO for n depth
      int depth = (children != null && children.size() > 0) ? 1 : 0;
      return depth;
   }
   
   /**
    * @return
    */
   public int getChildrenCount()
   {
      return children != null ? children.size() : 0;
   }

   /**
    * Return Count of Columns leaf Columns
    * @return
    */
   public int getTotalLeafCount()
   {
      //if(totalLeafCount == null)
      //{
         int leafCount = 0;
         if(children == null || children.size() == 0)
         {
            leafCount++; // No Children, means this itself is Leaf
         }
         else
         {
            for (ColumnPreference childColPref : children)
            {
               leafCount += childColPref.getTotalLeafCount();
            }
         }
         
         totalLeafCount = leafCount;
      //}
         
      return totalLeafCount;
   }

   public void setColumnDataFilterPopup(TableDataFilterPopup columnDataFilterPopup)
   {
      this.columnDataFilterPopup = columnDataFilterPopup;
      if (this.columnDataFilterPopup != null
            && this.columnDataFilterPopup.getDataFilters() != null
            && this.columnDataFilterPopup.getDataFilters().getList().size() > 0)
      {
         // Usually a Column will only have one filter associated
         this.columnDataFilterPopup.getDataFilters().getList().get(0).setProperty(columnProperty);
         this.columnDataFilterPopup.getDataFilters().getList().get(0).setName(columnName);
      }
   }
   
   /**
    * @return
    */
   public ColumnPreference getClone()
   {
      ColumnPreference clone = new ColumnPreference(this.columnName, this.columnTitle);

      clone.columnProperty = this.columnProperty;
      clone.columnDataType = this.columnDataType;
      clone.columnRenderType = this.columnRenderType;
      clone.columnDataFilterPopup = this.columnDataFilterPopup;
      clone.columnContentUrl = this.columnContentUrl;
      clone.visible = this.visible;
      clone.sortable = this.sortable;
      clone.noWrap = this.noWrap;
      clone.exportable = this.exportable;
      clone.width = this.width;
      clone.columnAlignment = this.columnAlignment;
      clone.columnConverterType = this.columnConverterType;
      clone.totalLeafCount = this.totalLeafCount;
      
      clone.children = new ArrayList<ColumnPreference>();
      for (ColumnPreference childColPref : this.children)
      {
         clone.children.add(childColPref.getClone());
      }
      
      return clone;
   }
   
   /**
    * 
    */
   private void adjustColumnAlignment()
   {
      if(columnDataType != null)
      {
         if(columnDataType == ColumnDataType.BOOLEAN)
         {
            columnAlignment = ColumnAlignment.CENTER;
         }
         else if(columnDataType == ColumnDataType.DATE || columnDataType == ColumnDataType.NUMBER)
         {
            columnAlignment = ColumnAlignment.CENTER;
            noWrap = true;
         }
         else
         {
            columnAlignment = ColumnAlignment.LEFT;
         }
      }
   }

   /**
    * @param columnProperty
    */
   private void setColumnProperty(String columnProperty)
   {
      this.columnProperty = columnProperty;
      
      // Validate the Format
      if(!StringUtils.isEmpty(columnProperty))
      {
         /*
         RegularExpression regEx = new RegularExpression(PROPERTY_REG_EX);
         if(!regEx.matches(columnProperty))
         {
            throw new IllegalArgumentException("Incorrect Syntax for Property, " + columnProperty);
         }
         */

         if(!Pattern.matches(PROPERTY_REG_EX, columnProperty))
         {
            throw new IllegalArgumentException("Incorrect Syntax for Property, " + columnProperty);
         }
      }
   }

   @Override
   public String toString()
   {
      return columnName + ":" + visible + ":" + columnTitle;
   }

   public String getColumnName()
   {
      return columnName;
   }

   public Boolean getSortable()
   {
      return isSortable();
   }

   public Boolean isSortable()
   {
      return sortable;
   }

   public void setSortable(Boolean sortable)
   {
      this.sortable = sortable;
   }

   public Boolean getVisible()
   {
      return isVisible();
   }

   public Boolean isVisible()
   {
      return visible;
   }

   public void setVisible(Boolean visible)
   {
      this.visible = visible;
   }
   
   public Boolean isNewlyVisible()
   {
      return newlyVisible;
   }
   
   public void setNewlyVisible(Boolean newlyVisible)
   {
      this.newlyVisible = newlyVisible;
   }

   public String getColumnProperty()
   {
      return columnProperty;
   }

   public String getColumnTitle()
   {
      return columnTitle;
   }

   public void setColumnTitle(String columnTitle)
   {
      this.columnTitle = columnTitle;
   }

   public ColumnDataType getColumnDataType()
   {
      return columnDataType;
   }
   
   public ColumnRenderType getColumnRenderType()
   {
      return columnRenderType;
   }

   public void setColumnRenderType(ColumnRenderType columnRenderType)
   {
      this.columnRenderType = columnRenderType;
   }
   
   public String getColumnContentUrl()
   {
      return columnContentUrl;
   }
   
   public void setColumnContentUrl(String columnContentUrl) 
   {
	  this.columnContentUrl = columnContentUrl;
   }
   
   public TableDataFilterPopup getColumnDataFilterPopup()
   {
      return columnDataFilterPopup;
   }

   public List<ColumnPreference> getChildren()
   {
      return children;
   }

   public ColumnAlignment getColumnAlignment()
   {
      return columnAlignment;
   }

   public void setColumnAlignment(ColumnAlignment columnAlignment)
   {
      this.columnAlignment = columnAlignment;
   }

   public void setColumnConverterType(ColumnConverterType columnConverterType)
   {
      this.columnConverterType = columnConverterType;
   }

   public Boolean getNoWrap()
   {
      return noWrap;
   }

   public void setNoWrap(Boolean noWrap)
   {
      this.noWrap = noWrap;
   }

   public Boolean isExportable()
   {
      return exportable;
   }

   public void setExportable(boolean exportable)
   {
      this.exportable = exportable;
   }

   public String getWidth()
   {
      return width;
   }

   public void setWidth(String width)
   {
      this.width = width;
   }

   public boolean isEscape()
   {
      return escape;
   }

   public void setEscape(boolean escape)
   {
      this.escape = escape;
   }
}
