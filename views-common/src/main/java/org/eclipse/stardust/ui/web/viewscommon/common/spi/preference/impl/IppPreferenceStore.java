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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceStore;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceStore;



/**
 * @author Subodh.Godbole
 *
 */
public class IppPreferenceStore implements PreferenceStore
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager.getLogger(IppPreferenceStore.class);
   
   protected IPreferenceStore preferenceStore;
   protected PreferenceScope scope;
 
   /**
    * @param preferenceStore
    */
   public IppPreferenceStore(IPreferenceStore preferenceStore)
   {
      this.preferenceStore = preferenceStore;
      if(org.eclipse.stardust.engine.core.preferences.PreferenceScope.PARTITION.equals(preferenceStore.getScope()))
      {
         scope = PreferenceScope.PARTITION;
      }
      else if(org.eclipse.stardust.engine.core.preferences.PreferenceScope.USER.equals(preferenceStore.getScope()))
      {
         scope = PreferenceScope.USER;
      }
      else
      {
         scope = PreferenceScope.USER;
      }
   }
   
   public PreferenceScope getScope()
   {
      return scope;
   }

   public String getString(String name)
   {
      String value = preferenceStore.getString(name);
      log(name, value);
      return value;
   }

   public int getInt(String name)
   {
      int value = preferenceStore.getInt(name);
      log(name, value);
      return value;
   }

   public boolean getBoolean(String name)
   {
      boolean value = preferenceStore.getBoolean(name);
      log(name, value);
      return value;
   }

   public double getDouble(String name)
   {
      double value = preferenceStore.getDouble(name);
      log(name, value);
      return value;
   }

   public float getFloat(String name)
   {
      float value = preferenceStore.getFloat(name);
      log(name, value);
      return value;
   }

   public long getLong(String name)
   {
      long value = preferenceStore.getLong(name);
      log(name, value);
      return value;
   }

   public List<String> getList(String name)
   {
      String value = getString(name);
      if(StringUtils.isEmpty(value))
      {
         return null; // If empty Value retrieved then return null
      }

      List<String> values = new ArrayList<String>();      
      if (!IppPreferenceProvider.PROPERTY_VALUE_SEPARATOR.equals(value))
      {
         Iterator<String> propIter = StringUtils.split(value, IppPreferenceProvider.PROPERTY_VALUE_SEPARATOR);
         while (propIter.hasNext())
         {
            values.add(propIter.next());
         }
      }
      
      return values;
   }
   
   /**
    * @param key
    * @param value
    */
   protected void log(String key, Object value)
   {
      if (trace.isDebugEnabled())
      {
         trace.debug("IppPreference => " + preferenceStore.getScope() + ":" + key + "=" + value);
      }
   }
}
