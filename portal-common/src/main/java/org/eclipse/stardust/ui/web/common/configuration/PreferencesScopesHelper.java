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
package org.eclipse.stardust.ui.web.common.configuration;

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;


/**
 * @author Subodh.Godbole
 *
 */
public class PreferencesScopesHelper implements Converter, Serializable
{
   private static final long serialVersionUID = 1L;

   private SelectItem[] allPreferenceScopes;
   private PreferenceScope selectedPreferenceScope = PreferenceScope.USER;
   
   public PreferencesScopesHelper()
   {
      allPreferenceScopes = UserPreferencesHelper.getPreferencesScopesItems();
   }
   
   public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2)
   {
      for (SelectItem si : allPreferenceScopes)
         if (si.getLabel().equals(arg2))
            return si.getValue();
      throw new IllegalArgumentException("Item not found: " + arg2);
   }

   public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2)
   {
      for (SelectItem si : allPreferenceScopes)
      {
         if (si.getValue().equals(arg2))
         {
            return si.getLabel();
         }
      }

      // throw new IllegalArgumentException("Item not found: " + arg2);
      return null;
   }

   public SelectItem[] getAllPreferenceScopes()
   {
      return allPreferenceScopes;
   }

   public void setAllPreferenceScopes(SelectItem[] allPreferenceScopes)
   {
      this.allPreferenceScopes = allPreferenceScopes;
   }
   
   public PreferenceScope getSelectedPreferenceScope()
   {
      return selectedPreferenceScope;
   }

   public void setSelectedPreferenceScope(PreferenceScope selectedPreferenceScope)
   {
      this.selectedPreferenceScope = selectedPreferenceScope;
   }
}
