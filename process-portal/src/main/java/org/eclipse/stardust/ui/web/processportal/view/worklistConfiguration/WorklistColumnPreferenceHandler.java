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
package org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.common.column.IColumnPreferenceHandler;
import org.eclipse.stardust.ui.web.common.configuration.PreferencesScopesHelper;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;

/**
 * @author Yogesh.Manware
 * 
 */
public class WorklistColumnPreferenceHandler implements IColumnPreferenceHandler
{
   private static final long serialVersionUID = 6644714841497003229L;
   public static final String DEFAULT = WorklistConfigurationUtil.DEFAULT;

   private String preferenceId;
   private String identityKey;
   private List<String> storedList;
   private boolean lock;

   /**
    * @param identityKey
    * @param preferenceId
    */
   public WorklistColumnPreferenceHandler(String identityKey, String preferenceId)
   {
      this.preferenceId = preferenceId;
      this.identityKey = identityKey;
      fetchPreferences(PreferenceScope.USER);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.column.IColumnPreferenceHandler#saveColumnPreferences
    * (org.eclipse.stardust.engine.core.preferences.PreferenceScope, java.util.ArrayList,
    * java.lang.Boolean)
    */
   public void savePreferences(PreferenceScope prefScope, ArrayList<String> colsToBeSaved, Boolean lock)
   {
      Map<String, Object> worklistConfiguration;
      String id = identityKey;
      if (PreferenceScope.PARTITION == prefScope)
      {
         worklistConfiguration = WorklistConfigurationUtil.getWorklistConfigurationMap(
               PreferencesScopesHelper.wrapScope(prefScope), preferenceId);
         id = DEFAULT;
      }
      else
      {
         worklistConfiguration = WorklistConfigurationUtil.getWorklistConfigurationMap(
               PreferencesScopesHelper.wrapScope(prefScope), preferenceId);
      }

      WorklistConfigurationUtil.updateValues(id, colsToBeSaved, lock, worklistConfiguration);
      WorklistConfigurationUtil.savePreferences(PreferencesScopesHelper.wrapScope(prefScope), preferenceId,
            worklistConfiguration);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.column.IColumnPreferenceHandler#reset(org.eclipse
    * .stardust.engine.core.preferences.PreferenceScope)
    */
   @SuppressWarnings("unchecked")
   public void fetchPreferences(PreferenceScope scope)
   {
      Map<String, Object> configuration;
      if (PreferenceScope.PARTITION == scope)
      {
         configuration = WorklistConfigurationUtil.getStoredValuesPartitionDefault(preferenceId);
      }
      else
      {
         configuration = WorklistConfigurationUtil.getStoredValues(identityKey, preferenceId);
      }

      if (null != configuration)
      {
         this.storedList = (List<String>) configuration.get(WorklistConfigurationUtil.SELECTED_COLS);

         String lockStr = (String) configuration.get(WorklistConfigurationUtil.LOCK);
         boolean lock = false;
         if (Boolean.valueOf(lockStr))
         {
            lock = true;
         }
         this.lock = lock;
      }
   }

   public List<String> getPreferences()
   {
      return storedList;
   }

   public boolean isLock()
   {
      return lock;
   }

   public void resetPreferences(PreferenceScope prefScope)
   {
      Map<String, Object> worklistConfiguration;
      if (PreferenceScope.PARTITION != prefScope)
      {
         worklistConfiguration = WorklistConfigurationUtil.getWorklistConfigurationMap(
               PreferencesScopesHelper.wrapScope(prefScope), preferenceId);
         worklistConfiguration.remove(identityKey);
         WorklistConfigurationUtil.savePreferences(PreferencesScopesHelper.wrapScope(prefScope), preferenceId,
               worklistConfiguration);
      }
   }
}
