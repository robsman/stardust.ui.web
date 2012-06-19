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

import java.io.Serializable;
import java.util.List;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import com.icesoft.faces.component.selectinputtext.SelectInputText;

/**
 * @author Subodh.Godbole
 *
 */
public interface IAutocompleteSelector<T> extends Serializable
{
   String getSearchValue();
   void setSearchValue(String searchValue);
   T getSelectedValue();
   
   int getMaxRows();
   void setMaxRows(int maxRows);

   int getMinCharacters();
   void setMinCharacters(int minCharacters);

   List<SelectItem> getMatchingData();
   IAutocompleteDataProvider getDataProvider();
   void setDataProvider(IAutocompleteDataProvider dataProvider);

   void searchValueChanged(ValueChangeEvent event);

   boolean isDisabled();
   void setDisabled(boolean disabled);

   String getAutocompleteContentUrl();
   void setAutocompleteContentUrl(String autocompleteContentUrl);
   void setAutocompleteSelectorListener(IAutocompleteSelectorListener autocompleteSelectorListener);
   
   /**
    * @author Subodh.Godbole
    *
    */
   public interface IAutocompleteSelectorListener
   {
      void actionPerformed(SelectInputText autoComplete, SelectItem selectedItem);
   }
}
