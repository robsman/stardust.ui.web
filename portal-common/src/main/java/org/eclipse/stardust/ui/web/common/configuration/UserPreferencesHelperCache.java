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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.stardust.ui.web.html5.ManagedBeanUtils;


/**
 * @author Subodh.Godbole
 *
 */
public class UserPreferencesHelperCache implements Serializable
{
   private static final long serialVersionUID = 8636253562228142901L;

   public static String BEAN_NAME = "ippUserPreferencesHelperCache";

   private Map<String, UserPreferencesHelper> allUserPreferences = new HashMap<String, UserPreferencesHelper>();

   /**
    * @return
    */
   public static UserPreferencesHelperCache getInstance()
   {
      return (UserPreferencesHelperCache) ManagedBeanUtils.getManagedBean(BEAN_NAME);
   }
   
   /**
    * @param key
    * @param value
    */
   public void put(String key, UserPreferencesHelper value)
   {
      allUserPreferences.put(key, value);
   }
   
   /**
    * @param key
    * @return
    */
   public UserPreferencesHelper get(String key)
   {
      return allUserPreferences.get(key);
   }
}
