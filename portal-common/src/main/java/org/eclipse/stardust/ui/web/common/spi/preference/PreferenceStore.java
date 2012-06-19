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
package org.eclipse.stardust.ui.web.common.spi.preference;

import java.io.Serializable;
import java.util.List;

/**
 * API for Retrieving Preferences
 * @author Subodh.Godbole
 *
 */
public interface PreferenceStore extends Serializable
{
   /**
    * Returns the Scope of this Preference Store
    * @return
    */
   PreferenceScope getScope();
  
   /**
    * Returns the Stored value as String for given key/name
    * @param name
    * @return
    */
   String getString(String name);

   /**
    * Returns the Stored value as integer for given key/name
    * @param name
    * @return
    */
   int getInt(String name);

   /**
    * Returns the Stored value as long for given key/name
    * @param name
    * @return
    */
   long getLong(String name);
   
   /**
    * Returns the Stored value as float for given key/name
    * @param name
    * @return
    */
   float getFloat(String name);
   
   /**
    * Returns the Stored value as double for given key/name
    * @param name
    * @return
    */
   double getDouble(String name);
   
   /**
    * Returns the Stored value as boolean for given key/name
    * @param name
    * @return
    */
   boolean getBoolean(String name);
   
   /**
    * Returns the Stored value as list for given key/name
    * @param name
    * @return
    */
   List<String> getList(String name);
}
