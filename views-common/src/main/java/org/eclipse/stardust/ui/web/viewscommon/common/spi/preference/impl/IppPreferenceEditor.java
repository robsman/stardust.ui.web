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
package org.eclipse.stardust.ui.web.viewscommon.common.spi.preference.impl;

import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceEditor;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceEditor;




/**
 * @author Subodh.Godbole
 *
 */
public class IppPreferenceEditor extends IppPreferenceStore implements PreferenceEditor
{
   private static final long serialVersionUID = 1L;

   protected transient IPreferenceEditor preferenceEditor;
   
   /**
    * @param preferencesEditor
    */
   public IppPreferenceEditor(IPreferenceEditor preferenceEditor)
   {
      super(preferenceEditor);
      this.preferenceEditor = preferenceEditor;
   }

   public void setInt(String name, int value)
   {
      preferenceEditor.setValue(name, value);
      log(name, value);      
   }

   public void setList(String name, List<String> values)
   {
      String value = null;
      if(values != null)
      {
         value = values.isEmpty() ? IppPreferenceProvider.PROPERTY_VALUE_SEPARATOR : 
            StringUtils.join(values.iterator(), IppPreferenceProvider.PROPERTY_VALUE_SEPARATOR);
      }

      preferenceEditor.setValue(name, value);
      log(name, value);
   }

   public void setString(String name, String value)
   {
      preferenceEditor.setValue(name, value);
      log(name, value);
   }

   public void reset(String name)
   {
      preferenceEditor.resetValue(name);
   }

   public void save()
   {
      preferenceEditor.save();
   }
}
