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

import java.util.List;

/**
 * API for Persisting Preferences
 * @author Subodh.Godbole
 *
 */
public interface PreferenceEditor extends PreferenceStore
{
   /**
    * Sets value for given key/name
    * @param name
    * @param value
    */
   void setString(String name, String value);

   /**
    * Sets value for given key/name
    * @param name
    * @param value
    */
   void setInt(String name, int value);

   /**
    * Sets value for given key/name
    * @param name
    * @param value
    */
   void setList(String name, List<String> values);
   
   /**
    * Resets the value for given key/name
    * @param name
    * @param value
    */
   void reset(String name);
   
   /**
    * All Sets values using above APIs are persisted
    */
   void save();
}
