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

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider;



/**
 * @author Shrikant.Gangal
 *
 */
public class CriticalityAutocompleteDataProvider implements IAutocompleteDataProvider
{

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.autocomplete.IAutocompleteDataProvider#getMatchingData(java.lang.String, int)
    */
   public List<SelectItem> getMatchingData(String searchValue, int maxMatches)
   {
      List<SelectItem> matchingCriticalityConfigs = new ArrayList<SelectItem>();
      if (StringUtils.isNotEmpty(searchValue))
      {
         List<CriticalityCategory> cCats = CriticalityConfigurationHelper.getInstance().getCriticalityConfiguration();
         for (CriticalityCategory cCat : cCats)
         {
            CriticalityAutocompleteItem cItem = new CriticalityAutocompleteItem(cCat);
            if (cItem.getLabel().toUpperCase().startsWith(searchValue.toUpperCase()))
            {
               matchingCriticalityConfigs.add(new SelectItem(cItem, cItem.getLabel()));
            }
         }
      }
      return matchingCriticalityConfigs;
   }
}
