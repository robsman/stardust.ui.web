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

/**
 * @author Subodh.Godbole
 * 
 */
public abstract class AutocompleteMultiSelector<T> extends AutocompleteSelector<T>
      implements IAutocompleteMultiSelector<T>
{
   private static final long serialVersionUID = 1L;

   protected String selectedDataContentUrl;

   /**
    * @param maxRows
    * @param minCharacters
    */
   public AutocompleteMultiSelector(int maxRows, int minCharacters)
   {
      super(maxRows, minCharacters);
   }

   public String getSelectedDataContentUrl()
   {
      return selectedDataContentUrl;
   }
   
   public void setSelectedDataContentUrl(String selectedDataContentUrl)
   {
      this.selectedDataContentUrl = selectedDataContentUrl;
   }
}
