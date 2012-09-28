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

import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferencesManager;
import org.eclipse.stardust.ui.web.common.configuration.PreferencesScopesHelper;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceEditor;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceProvider;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceStore;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;




/**
 * @author Subodh.Godbole
 *
 */
public class IppPreferenceProvider implements PreferenceProvider
{
   private static final long serialVersionUID = 1L;

   public static final String PROPERTY_VALUE_SEPARATOR = "$#$";
   
   /**
    * 
    */
   public IppPreferenceProvider()
   {
   }

   public boolean isEnabled()
   {
      return Parameters.instance().getBoolean(
            IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false);
   }

   public PreferenceStore getPreferenceStore(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      IPreferencesManager pm = SessionContext.findSessionContext().getPreferencesManager();
      return new IppPreferenceStore(pm.getPreferences(PreferencesScopesHelper.wrapScope(scope), moduleId, preferencesId));
   }

   public PreferenceStore getPreferenceStore(String moduleId, String preferencesId)
   {
      return getPreferenceStore(PreferenceScope.USER, moduleId, preferencesId);
   }

   public PreferenceEditor getPreferenceEditor(PreferenceScope scope, String moduleId,
         String preferencesId)
   {
      IPreferencesManager pm = SessionContext.findSessionContext().getPreferencesManager();
      return new IppPreferenceEditor(pm.getPreferencesEditor(PreferencesScopesHelper.wrapScope(scope), moduleId, preferencesId));
   }
}
