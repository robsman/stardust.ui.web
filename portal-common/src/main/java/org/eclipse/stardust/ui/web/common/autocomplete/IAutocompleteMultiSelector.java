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

/**
 * @author Subodh.Godbole
 *
 */
public interface IAutocompleteMultiSelector<T> extends IAutocompleteSelector<T>
{
   List<T> getSelectedValues();
   void setSelectedValues(List<T> selectedValues);
   List<String> getSelectedValuesAsString();

   void setAutocompleteMultiSelectorListener(IAutocompleteMultiSelectorListener<T> autocompleteMultiSelectorListener);
   
   String getSelectedDataContentUrl();
   void setSelectedDataContentUrl(String selectedDataContentUrl);
   
   /**
    * @author Subodh.Godbole
    *
    * @param <T>
    */
   public interface IAutocompleteMultiSelectorListener<T>
   {
      void dataAdded(T t);
      void dataRemoved(T t);
   }
}
