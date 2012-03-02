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

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.autocomplete.AutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;

import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 * @since 7.0
 */
public class PriorityAutocompleteSelector extends AutocompleteMultiSelector<PriorityAutoCompleteItem>
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private boolean singleSelect;

   private static final int MAX_MATCHES_TO_DISPLAY = 15;
   private static final int MIN_CHARS_TO_INVOKE_SEARCH = 1;

   private List<PriorityAutoCompleteItem> selectedValues;

   protected IAutocompleteMultiSelectorListener<PriorityAutoCompleteItem> autocompleteMultiSelectorListener;

   protected boolean showAutocompletePanel;
   protected boolean showSelectedList;

   /**
    * @param provider
    * @param autoCompleteListner
    */
   public PriorityAutocompleteSelector(final IAutocompleteDataProvider provider,
         final IAutocompleteSelectorListener autoCompleteListner)
   {
      super(MAX_MATCHES_TO_DISPLAY, MIN_CHARS_TO_INVOKE_SEARCH);
      setDataProvider(provider);
      autocompleteContentUrl = ResourcePaths.V_AUTOCOMPLETE_PRIORITY_MULTIPLE_SELECTOR;
      setAutocompleteSelectorListener(autoCompleteListner);
   }

   /**
    * TODO - implement single select
    * 
    * @param provider
    * @param autoCompleteListner
    */
   public PriorityAutocompleteSelector(final IAutocompleteDataProvider provider, boolean singleSelect)
   {
      super(MAX_MATCHES_TO_DISPLAY, MIN_CHARS_TO_INVOKE_SEARCH);

      setDataProvider(provider);
      // setAutocompleteSelectorListener(autoCompleteListner);
      this.singleSelect = singleSelect;
      setAutocompleteContentUrl(singleSelect
            ? ResourcePaths.V_AUTOCOMPLETE_PRIORITY_SINGLE_SELECTOR
            : ResourcePaths.V_AUTOCOMPLETE_PRIORITY_MULTIPLE_SELECTOR);
      if (!singleSelect)
      {
         showSelectedList = true;
         showAutocompletePanel = true;
         setSelectedDataContentUrl(ResourcePaths.V_AUTOCOMPLETE_PRIORITY_SELECTOR_TABLE);
         setAutocompleteSelectorListener(new IAutocompleteSelectorListener()
         {
            public void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem)
            {
               if (selectedItem.getValue() instanceof PriorityAutoCompleteItem) // Safety
               // check
               {
                  PriorityAutoCompleteItem priorityItem = (PriorityAutoCompleteItem) selectedItem.getValue();
                  addPriority(priorityItem);
                  autoComplete.setValue(null);
               }
            }
         });
         selectedValues = new ArrayList<PriorityAutoCompleteItem>();
      }
   }

   /**
    * Overridden - to first check if the given event is in INVOKE_APPLICATION phase - this
    * is needed as the participant data provider uses bean members and we must wait till
    * their values are set.
    * 
    */
   public void searchValueChanged(ValueChangeEvent event)
   {
      try
      {
         if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
         {
            event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
            event.queue();
            return;
         }
         else
         {
            if (event.getComponent() instanceof SelectInputText)
            {
               SelectInputText autoComplete = (SelectInputText) event.getComponent();
               selectedItem = autoComplete.getSelectedItem();
               if (selectedItem != null)
               {
                  autoComplete.setValue(selectedItem.getLabel());
                  if (autocompleteSelectorListener != null)
                  {
                     autocompleteSelectorListener.actionPerformed(autoComplete, selectedItem);
                  }
               }
               else
               {
                  String newWord = (String) event.getNewValue();
                  newWord = newWord.trim();
                  if (newWord.length() >= minCharacters)
                  {
                     matchingData = dataProvider.getMatchingData(newWord, maxRows);
                  }
                  else
                  {
                     matchingData = null;
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param priorityItem
    */
   public void removePriority(PriorityAutoCompleteItem priorityItem)
   {
      validateMultiSelectMode();

      selectedValues.remove(priorityItem);
      // Fire Event
      if (autocompleteMultiSelectorListener != null)
      {
         autocompleteMultiSelectorListener.dataRemoved(priorityItem);
      }
   }

   /**
    * @param priorityItem
    */
   public void addPriority(PriorityAutoCompleteItem priorityItem)
   {
      if (!selectedValues.contains(priorityItem))
      {
         priorityItem.setPriorityAutocompleteSelector(this);
         selectedValues.add(priorityItem);
         setSelectedValues(selectedValues);
         setSearchValue("");

         // Fire Event
         if (autocompleteMultiSelectorListener != null)
         {
            autocompleteMultiSelectorListener.dataAdded(priorityItem);
         }
      }
   }

   public List<PriorityAutoCompleteItem> getSelectedValues()
   {
      validateMultiSelectMode();
      return selectedValues;
   }

   public void setSelectedValues(List<PriorityAutoCompleteItem> selectedValues)
   {
      validateMultiSelectMode();
      this.selectedValues = selectedValues;
   }

   public List<String> getSelectedValuesAsString()
   {
      validateMultiSelectMode();
      List<String> strs = new ArrayList<String>();
      List<PriorityAutoCompleteItem> cItemList = selectedValues;
      for (PriorityAutoCompleteItem cCat : cItemList)
      {
         strs.add(cCat.getLabel());
      }
      return strs;
   }

   public void setAutocompleteMultiSelectorListener(
         IAutocompleteMultiSelectorListener<PriorityAutoCompleteItem> autocompleteMultiSelectorListener)
   {
      validateMultiSelectMode();
      this.autocompleteMultiSelectorListener = autocompleteMultiSelectorListener;

   }

   /**
    * 
    */
   private void validateMultiSelectMode()
   {
      if (singleSelect)
      {
         throw new IllegalAccessError("Does not support Single Selection");
      }
   }

   public boolean isSingleSelect()
   {
      return singleSelect;
   }

   public boolean isShowAutocompletePanel()
   {
      return showAutocompletePanel;
   }

   public void setShowAutocompletePanel(boolean showAutocompletePanel)
   {
      this.showAutocompletePanel = showAutocompletePanel;
   }

   public boolean isShowSelectedList()
   {
      return showSelectedList;
   }

   public void setShowSelectedList(boolean showSelectedList)
   {
      this.showSelectedList = showSelectedList;
   }
}

/**
 * 
 * @author Sidharth.Singh
 * @version $Revision: $
 */
class PriorityAutocompleteDataProvider implements IAutocompleteDataProvider
{

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider#
    * getMatchingData(java.lang.String, int)
    */
   public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
   {
      List<SelectItem> matchingPriority = new ArrayList<SelectItem>();
      if (StringUtils.isNotEmpty(searchValue))
      {
         for (int i = -1; i < 2; i++)
         {
            // Populate PriorityAutoCompleteItem with priority Label ,priority and icon
            String iconVal = ProcessInstanceUtils.getPriorityLabel(i);
            String iconPath = "/plugins/processportal/images/icons/priority-" + iconVal.toLowerCase() + ".png";
            PriorityAutoCompleteItem pItem = new PriorityAutoCompleteItem(iconVal, iconPath, i);
            if (pItem.getLabel().toUpperCase().startsWith(searchValue.toUpperCase()))
            {
               matchingPriority.add(new SelectItem(pItem, pItem.getLabel()));
            }
         }
      }
      return matchingPriority;
   }
}
