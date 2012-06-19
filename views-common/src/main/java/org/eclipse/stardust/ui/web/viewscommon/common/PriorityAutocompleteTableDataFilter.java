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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector.IAutocompleteMultiSelectorListener;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterCustom;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 * @since 7.0
 */
public class PriorityAutocompleteTableDataFilter extends TableDataFilterCustom
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private static final int FILTER_TITLE_ITEM_MAX_ALPHA = 5;
   private static final int FILTER_TITLE_ITEM_TRUNCATE_TO_LENGTH = 3;
   public static final int MAX_SUMMARY_LENGTH = 35;
   public static final int MIN_CHARACTERS = 1; // Can be moved as a Configuration

   private PriorityAutocompleteSelector priorityAutocompleteSelector;

   public PriorityAutocompleteTableDataFilter()
   {
      this("", "", "", true);
   }

   public PriorityAutocompleteTableDataFilter(String name, String property, String title, boolean visible)
   {
      super(name, property, title, visible, ResourcePaths.V_PRIORITY_TABLE_FILTER);
      priorityAutocompleteSelector = new PriorityAutocompleteSelector(new PriorityAutocompleteDataProvider(), false);
      priorityAutocompleteSelector
            .setAutocompleteMultiSelectorListener(new IAutocompleteMultiSelectorListener<PriorityAutoCompleteItem>()
            {

               public void dataAdded(PriorityAutoCompleteItem t)
               {}

               public void dataRemoved(PriorityAutoCompleteItem t)
               {}

            });
   }

   public boolean isFilterSet()
   {
      List<PriorityAutoCompleteItem> dataList = priorityAutocompleteSelector.getSelectedValues();
      return CollectionUtils.isNotEmpty(dataList);
   }

   public void resetFilter()
   {
      priorityAutocompleteSelector.setSelectedValues(new ArrayList<PriorityAutoCompleteItem>());

   }

   public String getFilterSummaryTitle()
   {
      String str = "";
      List<String> list = getCustomizedSelectedValuesList(priorityAutocompleteSelector.getSelectedValuesAsString());

      str = StringUtils.join(list.iterator(), ", ");

      if (StringUtils.isNotEmpty(str))
      {
         if (str.length() > MAX_SUMMARY_LENGTH)
         {
            str = str.substring(0, MAX_SUMMARY_LENGTH);
            str += "...";
         }
      }

      return str;
   }

   public boolean contains(Object compareValue)
   {
      if (!isFilterSet() || compareValue == null)
         return true;

      List<PriorityAutoCompleteItem> dataList = priorityAutocompleteSelector.getSelectedValues();
      return dataList.contains(dataList);
   }

   public ITableDataFilter getClone()
   {
      PriorityAutocompleteTableDataFilter cloneFilter = new PriorityAutocompleteTableDataFilter(getName(),
            getProperty(), getTitle(), isVisible());
      PriorityAutocompleteSelector clonePrioritySelector = cloneFilter.getPriorityAutocompleteSelector();
      clonePrioritySelector
            .setSelectedValues(CollectionUtils.copyList(priorityAutocompleteSelector.getSelectedValues()));

      List<PriorityAutoCompleteItem> cItems = clonePrioritySelector.getSelectedValues();
      for (PriorityAutoCompleteItem cItem : cItems)
      {
         cItem.setPriorityAutocompleteSelector(clonePrioritySelector);
      }

      return cloneFilter;
   }

   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      PriorityAutocompleteTableDataFilter filterToCopy = (PriorityAutocompleteTableDataFilter) dataFilterToCopy;
      priorityAutocompleteSelector.setSelectedValues(CollectionUtils.copyList(filterToCopy
            .getPriorityAutocompleteSelector().getSelectedValues()));

   }

   public PriorityAutocompleteSelector getPriorityAutocompleteSelector()
   {
      return priorityAutocompleteSelector;
   }

   public void setPriorityAutocompleteSelector(PriorityAutocompleteSelector priorityAutocompleteSelector)
   {
      this.priorityAutocompleteSelector = priorityAutocompleteSelector;
   }

   private List<String> getCustomizedSelectedValuesList(List<String> strs)
   {
      List<String> cStrs = new ArrayList<String>();
      for (String str : strs)
      {
         str = (str.length() > FILTER_TITLE_ITEM_MAX_ALPHA)
               ? (str.substring(0, FILTER_TITLE_ITEM_TRUNCATE_TO_LENGTH) + "...")
               : str;
         cStrs.add(str);
      }

      return cStrs;
   }

}
