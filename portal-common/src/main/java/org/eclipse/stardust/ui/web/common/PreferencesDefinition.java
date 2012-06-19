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
package org.eclipse.stardust.ui.web.common;

/**
 * @author subodh.godbole
 *
 */
public class PreferencesDefinition extends UiDefinition<PreferencePage>
{
   public static final String PREF_CONFIG_PANEL = "configuration";
   public static final String PREF_ICON = "icon";
   public static final String PREF_HELP_DOCUMENTATION = "helpDocumentation";
   
   /**
    * @param name
    * @return
    */
   public PreferencePage getPreference(String name)
   {
      for (PreferencePage prefPage : getElements())
      {
         if(prefPage.getName().equals(name))
            return prefPage;
      }
      
      return null;
   }
}
