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

import java.util.List;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.util.StringUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class TableDataFilterPickList extends TableDataFilter implements ITableDataFilterPickList
{
   private static final long serialVersionUID = 6862390762348757310L;

   private List<SelectItem> all;
   private List<Object> selected;
   private RenderType renderType;
   private int visibleSize;
   private String filterSummaryTitle;

   /**
    * @param name
    * @param title
    * @param filterCriteria
    * @param visible
    * @param all
    * @param selected
    * @param renderType
    * @param visibleSize
    */
   public TableDataFilterPickList(String name, String title, FilterCriteria filterCriteria,
            boolean visible, List<SelectItem> all, List<Object> selected,
            RenderType renderType, int visibleSize)
   {
      super(name, title, DataType.NONE, filterCriteria, visible);
      this.all = all;
      this.selected = selected;
      this.renderType = renderType;
      this.visibleSize = visibleSize;
   }

   /**
    * @param filterCriteria
    * @param all
    * @param renderType
    * @param visibleSize
    * @param filterSummaryTitle
    */
   public TableDataFilterPickList(FilterCriteria filterCriteria,
         List<SelectItem> all, RenderType renderType, int visibleSize, String filterSummaryTitle)
   {
      this("", "", filterCriteria, true, all, null, renderType, visibleSize);
      this.filterSummaryTitle = filterSummaryTitle;
   }
   
   /**
    * This can be used for rendering Combobox, when FilterCriteria is SELECT_ONE
    * @param name
    * @param title
    * @param filterCriteria
    * @param visible
    * @param all
    * @param selected
    * @param renderType
    */
   public TableDataFilterPickList(String name, String title, FilterCriteria filterCriteria,
         boolean visible, List<SelectItem> all, List<Object> selected, RenderType renderType)
   {
      this(name, title, filterCriteria, visible, all, selected, renderType, 1);
   }

   /**
    * @param filterCriteria
    * @param all
    * @param renderType
    * @param filterSummaryTitle
    */
   public TableDataFilterPickList(FilterCriteria filterCriteria, List<SelectItem> all,
         RenderType renderType, String filterSummaryTitle)
   {
      this("", "", filterCriteria, true, all, null, renderType);
      this.filterSummaryTitle = filterSummaryTitle;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#isFilterSet()
    */
   public boolean isFilterSet()
   {
      return (selected != null && selected.size() > 0) ? true : false;
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#resetFilter()
    */
   public void resetFilter()
   {
      selected = null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getFilterSummaryTitle()
    */
   public String getFilterSummaryTitle()
   {
      String str = "";

      if(isFilterSet())
      {
         if(!StringUtils.isEmpty(filterSummaryTitle))
         {
            str = filterSummaryTitle;
         }
         else
         {
            // When multiple are selected, show value of 1st selected option 
            Object objToShow = selected.get(0);
            for (SelectItem selectItem : all)
            {
               if(selectItem.getValue().equals(objToShow))
               {
                  str = selectItem.getLabel();
                  if(selected.size() > 1)
                     str += "...";
                  break;
               }
            }
         }
      }

      return str;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#contains(java.lang.Object)
    */
   public boolean contains(final Object compareValue)
   {
      if(!isFilterSet() || compareValue == null )
      {
         return true;
      }

      Object selValue = null;
      for (Object selItem : selected)
      {
         if(selItem instanceof SelectItem)
         {
            selValue = ((SelectItem)selItem).getValue();
         }
         else
         {
            selValue = selItem;
         }

         if(compareValue.equals(selValue))
            return true;
      }

      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getClone()
    */
   public ITableDataFilter getClone()
   {
      return new TableDataFilterPickList(getName(), getTitle(), getFilterCriteria(),
            isVisible(), all, selected, renderType, visibleSize);
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#copy(org.eclipse.stardust.ui.web.common.filter.ITableDataFilter)
    */
   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      TableDataFilterPickList filterToCopy = (TableDataFilterPickList)dataFilterToCopy;
      setSelected(filterToCopy.getSelected());
   }
   
   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilterPickList#getRenderType()
    */
   public RenderType getRenderType()
   {
      return renderType;
   }

   public int getVisibleSize()
   {
      return visibleSize;
   }

   public List<SelectItem> getAll()
   {
      return all;
   }

   public List<Object> getSelected()
   {
      return selected;
   }

   public void setSelected(List<Object> selected)
   {
      this.selected = selected;
   }

   @Override
   public String toString()
   {
      return super.toString() + ":" + getRenderType() + ":" + getAll() + ":" + getSelected();
   }
}
