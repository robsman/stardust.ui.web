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

/**
 * @author Subodh.Godbole
 * 
 */
public interface PreferenceProvider extends Serializable
{
   boolean isEnabled();
   
   /**
    * Returns the Preference Store for current user for module Id & preferencesId
    * Default PreferenceScope would be USER
    * Preference Store is used to read preferences
    * @param moduleId
    * @param preferencesId
    * @return
    */
   PreferenceStore getPreferenceStore(String moduleId, String preferencesId);

   /**
    * Returns the Preference Store for current user for module Id & preferencesId and Preference Scope
    * Preference Store is used to read preferences
    * @param scope
    * @param moduleId
    * @param preferencesId
    * @return
    */
   PreferenceStore getPreferenceStore(PreferenceScope scope, String moduleId, String preferencesId);

   /**
    * Returns the Preference Editor for current user for module Id & preferencesId and Preference Scope
    * Preference Editor is used to persist preferences 
    * @param scope
    * @param moduleId
    * @param preferencesId
    * @return
    */
   PreferenceEditor getPreferenceEditor(PreferenceScope scope, String moduleId, String preferencesId);
}
