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
package org.eclipse.stardust.ui.web.common.autocomplete;

import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * @author Subodh.Godbole
 *
 */
public abstract class AutocompleteSelector<T> implements IAutocompleteSelector<T>
{
   private static final long serialVersionUID = 1L;
   
   private static Logger trace = LogManager.getLogger(AutocompleteSelector.class);

   protected String searchValue;
   protected SelectItem selectedItem;
   
   protected int maxRows;
   protected int minCharacters;
   
   protected List<SelectItem> matchingData;
   protected IAutocompleteDataProvider dataProvider;

   protected boolean disabled;
   protected IAutocompleteSelectorListener autocompleteSelectorListener;
   
   protected String autocompleteContentUrl;
   
   /**
    * @param maxRows
    * @param minCharacters
    */
   public AutocompleteSelector(int maxRows, int minCharacters)
   {
      this.maxRows = maxRows;
      this.minCharacters = minCharacters;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.filter.ITableDataFilterAutocomplete#searchValueChanged(javax.faces.event.ValueChangeEvent)
    */
   public void searchValueChanged(ValueChangeEvent event)
   {
      if (event.getComponent() instanceof SelectInputText)
      {
         SelectInputText autoComplete = (SelectInputText) event.getComponent();
         selectedItem = autoComplete.getSelectedItem();

         if (selectedItem != null)
         {
            autoComplete.setValue(selectedItem.getLabel());
            if(autocompleteSelectorListener != null)
            {
               autocompleteSelectorListener.actionPerformed(autoComplete, selectedItem);
            }
            else
            {
               trace.info("[AutocompleteSelector]: No Autocomplete Selector Listener defined.");
            }
         }
         else
         {
            String newWord = (String) event.getNewValue();
            newWord = newWord.trim();
            if(newWord.length() >= minCharacters)
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
   
   /**
    * @param searchWord
    */
   public void searchAndPreSelect(String searchWord)
   {
      matchingData = dataProvider.getMatchingData(searchWord, maxRows);
      if (matchingData.size() >= 1)
      {
         selectedItem = matchingData.get(0);
         searchValue = selectedItem.getLabel();
      }
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteSelector#getSelectedValue()
    */
   @SuppressWarnings("unchecked")
   public T getSelectedValue()
   {
      if(selectedItem != null)
      {
         return (T)selectedItem.getValue();
      }

      return null;
   }

   public IAutocompleteDataProvider getDataProvider()
   {
      return dataProvider;
   }

   public void setDataProvider(IAutocompleteDataProvider dataProvider)
   {
      this.dataProvider = dataProvider;
   }

   public int getMinCharacters()
   {
      return minCharacters;
   }
   
   public void setMinCharacters(int minCharacters)
   {
      this.minCharacters = minCharacters;
   }

   public int getMaxRows()
   {
      return maxRows;
   }

   public void setMaxRows(int maxRows)
   {
      this.maxRows = maxRows;
   }

   public String getSearchValue()
   {
      return searchValue;
   }

   public void setSearchValue(String searchValue)
   {
      this.searchValue = searchValue;
   }
   
   public boolean isDisabled()
   {
      return disabled;
   }

   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }
   
   public List<SelectItem> getMatchingData()
   {
      return matchingData;
   }

   public void setAutocompleteSelectorListener(IAutocompleteSelectorListener autocompleteSelectorListener)
   {
      this.autocompleteSelectorListener = autocompleteSelectorListener;
   }
   
   public String getAutocompleteContentUrl()
   {
      return autocompleteContentUrl;
   }
   
   public void setAutocompleteContentUrl(String autocompleteContentUrl)
   {
      this.autocompleteContentUrl = autocompleteContentUrl;
   }
}
