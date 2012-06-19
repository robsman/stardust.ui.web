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
package org.eclipse.stardust.ui.web.viewscommon.user;

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
 * @author Subodh.Godbole
 *
 */
public class UserAutocompleteTableDataFilter extends TableDataFilterCustom
{
   private static final long serialVersionUID = 1L;

   public static final int MAX_SUMMARY_LENGTH = 35;

   protected UserAutocompleteMultiSelector userSelector;
   
   public UserAutocompleteTableDataFilter()
   {
      this("", "", "", true);
   }
   /**
    * @param name
    * @param property
    * @param title
    * @param visible
    * @param contentUrl
    */
   public UserAutocompleteTableDataFilter(String name, String property, String title, boolean visible)
   {
      super(name, property, title, visible, ResourcePaths.V_AUTOCOMPLETE_USER_SELECTOR_TABLE_FILTER);
      userSelector = new UserAutocompleteMultiSelector(false);
      userSelector.setShowOnlineIndicator(false);
      userSelector.setAutocompleteMultiSelectorListener(new IAutocompleteMultiSelectorListener<UserWrapper>(){
         public void dataAdded(UserWrapper t)
         {}

         public void dataRemoved(UserWrapper t)
         {}
      });
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#contains(java.lang.Object)
    */
   public boolean contains(Object compareValue)
   {
      if(!isFilterSet() || compareValue == null)
         return true;
      
      List<UserWrapper> dataList = userSelector.getSelectedValues();
      return dataList.contains(dataList);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#copyValues(org.eclipse.stardust.ui.web.common.filter.ITableDataFilter)
    */
   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      UserAutocompleteTableDataFilter filterToCopy = (UserAutocompleteTableDataFilter)dataFilterToCopy;
      userSelector.setSelectedValues(CollectionUtils.copyList(filterToCopy.getUserSelector().getSelectedValues()));
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getClone()
    */
   public ITableDataFilter getClone()
   {
      UserAutocompleteTableDataFilter cloneFilter = new UserAutocompleteTableDataFilter(getName(), getProperty(), getTitle(), isVisible());
      UserAutocompleteMultiSelector cloneUserSelector = cloneFilter.getUserSelector();
      cloneUserSelector.setSelectedValues(CollectionUtils.copyList(userSelector.getSelectedValues()));
      
      List<UserWrapper> users = cloneUserSelector.getSelectedValues();
      for (UserWrapper userWrapper : users)
      {
         userWrapper.setAutocompleteUserSelector(cloneUserSelector);
      }
      
      return cloneFilter;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getFilterSummaryTitle()
    */
   public String getFilterSummaryTitle()
   {
      String str = "";
      List<String> list = userSelector.getSelectedValuesAsString();
      if(CollectionUtils.isNotEmpty(list))
      {
         str = list.get(0);
      }

      if (StringUtils.isNotEmpty(str))
      {
         if(str.length() > MAX_SUMMARY_LENGTH)
         {
            str = str.substring(0, MAX_SUMMARY_LENGTH);
            str += "...";
         }
         else if (list.size() > 1)
         {
            str += "...";
         }
      }

      return str;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#isFilterSet()
    */
   public boolean isFilterSet()
   {
      List<UserWrapper> dataList = userSelector.getSelectedValues();
      return CollectionUtils.isNotEmpty(dataList);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#resetFilter()
    */
   public void resetFilter()
   {
      if (isFilterSet())
      {
         FacesUtils.refreshPage(); // This is needed for UI to show new values
      }
      userSelector.setSelectedValues(new ArrayList<UserWrapper>());
   }

   public UserAutocompleteMultiSelector getUserSelector()
   {
      return userSelector;
   }
}
