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
package org.eclipse.stardust.ui.web.viewscommon.common.criticality;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteMultiSelector.IAutocompleteMultiSelectorListener;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterCustom;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;



/**
 * @author Shrikant.Gangal
 * 
 */
public class CriticalityAutocompleteTableDataFilter extends TableDataFilterCustom
{
   private static final long serialVersionUID = 1L;
   private static final int FILTER_TITLE_ITEM_MAX_ALPHA = 5;
   private static final int FILTER_TITLE_ITEM_TRUNCATE_TO_LENGTH = 3;

   private CriticalityAutocompleteSelector criticalitySelector;

   public static final int MAX_SUMMARY_LENGTH = 35;
   public static final int MAX_ROWS = 10; // Can be moved as a Configuration
   public static final int MIN_CHARACTERS = 1; // Can be moved as a Configuration

   public CriticalityAutocompleteTableDataFilter()
   {
      this("", "", "", true);
   }

   public CriticalityAutocompleteTableDataFilter(String name, String property, String title, boolean visible)
   {
      super(name, property, title, visible, ResourcePaths.V_CRITICALITY_TABLE_FILTER);
      criticalitySelector = new CriticalityAutocompleteSelector(new CriticalityAutocompleteDataProvider(), false);
      criticalitySelector
            .setAutocompleteMultiSelectorListener(new IAutocompleteMultiSelectorListener<CriticalityAutocompleteItem>()
            {
               public void dataAdded(CriticalityAutocompleteItem t)
               {
               }

               public void dataRemoved(CriticalityAutocompleteItem t)
               {
               }
            });
   }

   public boolean contains(Object compareValue)
   {
      if (!isFilterSet() || compareValue == null)
         return true;

      List<CriticalityAutocompleteItem> dataList = criticalitySelector.getSelectedValues();
      return dataList.contains(dataList);
   }

   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      CriticalityAutocompleteTableDataFilter filterToCopy = (CriticalityAutocompleteTableDataFilter) dataFilterToCopy;
      criticalitySelector.setSelectedValues(CollectionUtils.copyList(filterToCopy.getCriticalitySelector()
            .getSelectedValues()));
   }

   public ITableDataFilter getClone()
   {
      CriticalityAutocompleteTableDataFilter cloneFilter = new CriticalityAutocompleteTableDataFilter(getName(),
            getProperty(), getTitle(), isVisible());
      CriticalityAutocompleteSelector cloneCriticailtySelector = cloneFilter.getCriticalitySelector();
      cloneCriticailtySelector.setSelectedValues(CollectionUtils.copyList(criticalitySelector.getSelectedValues()));

      List<CriticalityAutocompleteItem> cItems = cloneCriticailtySelector.getSelectedValues();
      for (CriticalityAutocompleteItem cItem : cItems)
      {
         cItem.setCriticalityAutocompleteSelector(cloneCriticailtySelector);
      }

      return cloneFilter;
   }

   public String getFilterSummaryTitle()
   {
      String str = "";
      List<String> list = getCustomizedSelectedValuesList(criticalitySelector.getSelectedValuesAsString());
      
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

   public boolean isFilterSet()
   {
      List<CriticalityAutocompleteItem> dataList = criticalitySelector.getSelectedValues();
      return CollectionUtils.isNotEmpty(dataList);
   }

   public void resetFilter()
   {
      criticalitySelector.setSelectedValues(new ArrayList<CriticalityAutocompleteItem>());
   }

   public CriticalityAutocompleteSelector getCriticalitySelector()
   {
      return criticalitySelector;
   }
   
   private List<String> getCustomizedSelectedValuesList(List<String> strs)
   {
      List<String> cStrs = new ArrayList<String>();
      for(String str : strs)
      {
         str = (str.length() > FILTER_TITLE_ITEM_MAX_ALPHA) ? (str.substring(0, FILTER_TITLE_ITEM_TRUNCATE_TO_LENGTH) + "...") : str;
         cStrs.add(str);
      }
      
      return cStrs;
   }
}
