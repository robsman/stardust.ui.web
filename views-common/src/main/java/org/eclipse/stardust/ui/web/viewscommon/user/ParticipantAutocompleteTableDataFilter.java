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
 * @author Shrikant.Gangal
 *
 */
public class ParticipantAutocompleteTableDataFilter extends TableDataFilterCustom
{
   private static final long serialVersionUID = 1L;

   protected ParticipantAutocompleteSelector participantSelector;

   public static final int MAX_SUMMARY_LENGTH = 35;
   public static final int MAX_ROWS = 10; // Can be moved as a Configuration
   public static final int MIN_CHARACTERS = 1; // Can be moved as a Configuration

   /**
    * 
    */
   public ParticipantAutocompleteTableDataFilter()
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
   public ParticipantAutocompleteTableDataFilter(String name, String property, String title, boolean visible)
   {
      super(name, property, title, visible, ResourcePaths.V_AUTOCOMPLETE_PARTICIPANT_SELECTOR_TABLE_FILTER);
      participantSelector = new ParticipantAutocompleteSelector(new ParticipantsDataProvider(), false);
      participantSelector.setAutocompleteMultiSelectorListener(new IAutocompleteMultiSelectorListener<ParticipantWrapper>(){
         public void dataAdded(ParticipantWrapper t)
         {
         }

         public void dataRemoved(ParticipantWrapper t)
         {
         }
      });
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#contains(java.lang.Object)
    */
   public boolean contains(Object compareValue)
   {
      if(!isFilterSet() || compareValue == null)
         return true;
      
      List<ParticipantWrapper> dataList = participantSelector.getSelectedValues();
      return dataList.contains(dataList);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#copyValues(org.eclipse.stardust.ui.web.common.filter.ITableDataFilter)
    */
   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      ParticipantAutocompleteTableDataFilter filterToCopy = (ParticipantAutocompleteTableDataFilter)dataFilterToCopy;
      participantSelector.setSelectedValues(CollectionUtils.copyList(filterToCopy.getParticipantSelector().getSelectedValues()));
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getClone()
    */
   public ITableDataFilter getClone()
   {
      ParticipantAutocompleteTableDataFilter cloneFilter = new ParticipantAutocompleteTableDataFilter(getName(), getProperty(), getTitle(), isVisible());
      ParticipantAutocompleteSelector cloneUserSelector = cloneFilter.getParticipantSelector();
      cloneUserSelector.setSelectedValues(CollectionUtils.copyList(participantSelector.getSelectedValues()));
      
      List<ParticipantWrapper> users = cloneUserSelector.getSelectedValues();
      for (ParticipantWrapper participantWrapper : users)
      {
         participantWrapper.setAutocompleteParticipantSelector(cloneUserSelector);
      }
      
      return cloneFilter;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#getFilterSummaryTitle()
    */
   public String getFilterSummaryTitle()
   {
      String str = "";
      List<String> list = participantSelector.getSelectedValuesAsString();
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
      List<ParticipantWrapper> dataList = participantSelector.getSelectedValues();
      return CollectionUtils.isNotEmpty(dataList);
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilter#resetFilter()
    */
   public void resetFilter()
   {
      participantSelector.setSelectedValues(new ArrayList<ParticipantWrapper>());
   }

   /**
    * @return
    */
   public ParticipantAutocompleteSelector getParticipantSelector()
   {
      return participantSelector;
   }
}
