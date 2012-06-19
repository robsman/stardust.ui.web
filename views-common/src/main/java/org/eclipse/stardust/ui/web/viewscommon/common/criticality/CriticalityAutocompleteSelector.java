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

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.autocomplete.AutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * @author Shrikant.Gangal
 *
 */
public class CriticalityAutocompleteSelector extends AutocompleteMultiSelector<CriticalityAutocompleteItem>
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private boolean singleSelect;

   private static final int MAX_MATCHES_TO_DISPLAY = 15;
   private static final int MIN_CHARS_TO_INVOKE_SEARCH = 1;

   private List<CriticalityAutocompleteItem> selectedValues;

   protected IAutocompleteMultiSelectorListener<CriticalityAutocompleteItem> autocompleteMultiSelectorListener;

   protected boolean showAutocompletePanel;
   protected boolean showSelectedList;

   /**
    * @param provider
    * @param autoCompleteListner
    */
   public CriticalityAutocompleteSelector(final IAutocompleteDataProvider provider,
         final IAutocompleteSelectorListener autoCompleteListner)
   {
      super(MAX_MATCHES_TO_DISPLAY, MIN_CHARS_TO_INVOKE_SEARCH);
      setDataProvider(provider);
      autocompleteContentUrl = ResourcePaths.V_AUTOCOMPLETE_CRITICALITY_MULTIPLE_SELECTOR;
      setAutocompleteSelectorListener(autoCompleteListner);
   }

   /**
    * TODO - implement single select
    * 
    * @param provider
    * @param autoCompleteListner
    */
   public CriticalityAutocompleteSelector(final IAutocompleteDataProvider provider, boolean singleSelect)
   {
      super(MAX_MATCHES_TO_DISPLAY, MIN_CHARS_TO_INVOKE_SEARCH);

      setDataProvider(provider);
      // setAutocompleteSelectorListener(autoCompleteListner);
      this.singleSelect = singleSelect;
      setAutocompleteContentUrl(singleSelect
            ? ResourcePaths.V_AUTOCOMPLETE_CRITICALITY_SINGLE_SELECTOR
            : ResourcePaths.V_AUTOCOMPLETE_CRITICALITY_MULTIPLE_SELECTOR);
      if (!singleSelect)
      {
         showSelectedList = true;
         showAutocompletePanel = true;
         setSelectedDataContentUrl(ResourcePaths.V_AUTOCOMPLETE_CRITICALITY_SELECTOR_TABLE);
         setAutocompleteSelectorListener(new IAutocompleteSelectorListener()
         {
            public void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem)
            {
               if (selectedItem.getValue() instanceof CriticalityAutocompleteItem) // Safety
                                                                                   // check
               {
                  CriticalityAutocompleteItem criticality = (CriticalityAutocompleteItem) selectedItem.getValue();
                  addCriticality(criticality);
                  autoComplete.setValue(null);
               }
            }
         });
         selectedValues = new ArrayList<CriticalityAutocompleteItem>();
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
    * @param criticality
    */
   public void removeCriticality(CriticalityAutocompleteItem criticality)
   {
      validateMultiSelectMode();

      selectedValues.remove(criticality);
      // Fire Event
      if (autocompleteMultiSelectorListener != null)
      {
         autocompleteMultiSelectorListener.dataRemoved(criticality);
      }
   }

   /**
    * @param criticality
    */
   public void addCriticality(CriticalityAutocompleteItem criticality)
   {
      validateMultiSelectMode();
      if (!selectedValues.contains(criticality))
      {
         criticality.setCriticalityAutocompleteSelector(this);
         selectedValues.add(criticality);
         setSelectedValues(selectedValues);
         setSearchValue("");

         // Fire Event
         if (autocompleteMultiSelectorListener != null)
         {
            autocompleteMultiSelectorListener.dataAdded(criticality);
         }
      }
   }

   public List<CriticalityAutocompleteItem> getSelectedValues()
   {
      validateMultiSelectMode();
      return selectedValues;
   }

   public List<String> getSelectedValuesAsString()
   {
      validateMultiSelectMode();
      List<String> strs = new ArrayList<String>();
      List<CriticalityAutocompleteItem> cItemList = selectedValues;
      for (CriticalityAutocompleteItem cCat : cItemList)
      {
         strs.add(cCat.getLabel());
      }
      return strs;
   }

   public void setAutocompleteMultiSelectorListener(
         IAutocompleteMultiSelectorListener<CriticalityAutocompleteItem> autocompleteMultiSelectorListener)
   {
      validateMultiSelectMode();
      this.autocompleteMultiSelectorListener = autocompleteMultiSelectorListener;
   }

   public void setSelectedValues(List<CriticalityAutocompleteItem> selectedValues)
   {
      validateMultiSelectMode();
      this.selectedValues = selectedValues;
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

   /**
    * 
    */
   private void validateMultiSelectMode()
   {
      if (singleSelect)
      {
         throw new IllegalAccessError("Does not support Multi Selection");
      }
   }

   public boolean isSingleSelect()
   {
      return singleSelect;
   }
}
