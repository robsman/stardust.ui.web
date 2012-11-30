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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.DepartmentDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.UserGroupInfo;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.extensions.dms.data.DmsPrincipal;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.processportal.common.Constants;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class WorklistConfigurationUtil
{
   public static final String SELECTED_COLS = "selectedColumns";
   public static final String LOCK = "lock";
   public static final String DEFAULT = "Default";
   public static final String PREFERNCE_NAME = ".prefs";

   public static Map<String, Object> DEFAULT_CONF;

   // system level configuration
   static
   {
      DEFAULT_CONF = new HashMap<String, Object>();
      ArrayList<String> colsToBeSaved = new ArrayList<String>();
      colsToBeSaved.add(Constants.COL_ACTIVITY_NAME);
      colsToBeSaved.add(Constants.COL_OID);
      // colsToBeSaved.add(Constants.COL_PROCESS_DEFINITION);
      colsToBeSaved.add(Constants.COL_CRITICALITY);
      colsToBeSaved.add(Constants.COL_PRIORITY);
      colsToBeSaved.add(Constants.COL_DESCRIPTORS);
      colsToBeSaved.add(Constants.COL_STARTED);
      colsToBeSaved.add(Constants.COL_LAST_MODIFIED);
      colsToBeSaved.add(Constants.COL_DURATION);
      colsToBeSaved.add(Constants.COL_LAST_PERFORMER);
      // colsToBeSaved.add(Constants.COL_STATUS);
      // colsToBeSaved.add(Constants.COL_ASSIGNED_TO);
      DEFAULT_CONF.put(WorklistConfigurationUtil.SELECTED_COLS, colsToBeSaved);
      DEFAULT_CONF.put(WorklistConfigurationUtil.LOCK, "false");
   }

   /**
    * This method first checks PARTITION level configuration and if required and
    * permissible, USER level.
    * 
    * @param id
    * @param preferenceId
    * @return
    */
   public static Map<String, Object> getStoredValues(String id, String preferenceId)
   {
      // Check at partition level
      Map<String, Object> worklistConf = getWorklistConfigurationMap(PreferenceScope.PARTITION, preferenceId);

      Map<String, Object> configuration = getStoredValues(id, worklistConf);
      
      boolean fetchUserConf = false;

      if (CollectionUtils.isNotEmpty(configuration))
      {
         String lock = (String) configuration.get(WorklistConfigurationUtil.LOCK);
         if (!Boolean.valueOf(lock))
         {
            fetchUserConf = true;
         }
      }
      else
      {
         // check default configuration for Participant, if it is locked, don't fetch user
         // level configurations
         configuration = getStoredValues(DEFAULT, worklistConf);
         String lock = (String) configuration.get(WorklistConfigurationUtil.LOCK);
         if (!Boolean.valueOf(lock))
         {
            fetchUserConf = true;
         }
      }

      // Check at user level
      if (fetchUserConf)
      {
         Map<String, Object> worklistConfUser = getWorklistConfigurationMap(PreferenceScope.USER, preferenceId);

         if (CollectionUtils.isNotEmpty(worklistConfUser))
         {
            Map<String, Object> configurationUser = getStoredValues(id, worklistConfUser);
            if (CollectionUtils.isNotEmpty(configurationUser))
            {
               configuration = configurationUser;
            }
         }
      }

      if (CollectionUtils.isEmpty(configuration))
      {
         configuration = getStoredValues(DEFAULT, worklistConf);
      }
      return configuration;
   }

   /**
    * @param prefScope
    * @param id
    * @param preferenceId
    * @return
    */
   public static Map<String, Object> getStoredValuesPartitionDefault(String preferenceId)
   {
      // Check at partition level
      Map<String, Object> worklistConf;
      worklistConf = getWorklistConfigurationMap(PreferenceScope.PARTITION, preferenceId);
      return getStoredValues(DEFAULT, worklistConf);
   }
   
   
   /**
    * @param id
    * @param worklistConf
    * @return
    */
   @SuppressWarnings("unchecked")
   public static Map<String, Object> getStoredValues(String id, Map<String, Object> worklistConf)
   {
      Map<String, Object> configuration = (Map<String, Object>) worklistConf.get(id);

      if (DEFAULT.equals(id))
      {
         if (CollectionUtils.isEmpty(configuration))
         {
            configuration = DEFAULT_CONF;
         }
         else
         {
            if (CollectionUtils.isEmpty((ArrayList<String>) configuration.get(SELECTED_COLS)))
            {
               configuration.put(SELECTED_COLS,
                     WorklistConfigurationUtil.DEFAULT_CONF.get(WorklistConfigurationUtil.SELECTED_COLS));
            }
         }
      }

      return configuration;
   }

   /**
    * @param id
    * @param selectColumns
    * @param lock
    * @param worklistConf
    */
   @SuppressWarnings("unchecked")
   public static void updateValues(String id, ArrayList<String> selectColumns, boolean lock,
         Map<String, Object> worklistConf)
   {
      HashMap<String, Serializable> elementPreference = (HashMap<String, Serializable>) worklistConf.get(id);

      if (null == elementPreference)
      {
         elementPreference = new HashMap<String, Serializable>();
         worklistConf.put(String.valueOf(id), elementPreference);
      }
      elementPreference.put(SELECTED_COLS, selectColumns);
      elementPreference.put(LOCK, String.valueOf(lock));
   }

   /**
    * @param scope
    * @param preferenceId
    * @param preferences
    */
   public static void savePreferences(PreferenceScope scope, String preferenceId, Map<String, Object> preferences)
   {
      Map<String, Serializable> preferencesEnc = new HashMap<String, Serializable>();

      for (Entry<String, Object> entry : preferences.entrySet())
      {
         preferencesEnc.put(entry.getKey(), GsonUtils.stringify(entry.getValue()));
      }

      Preferences prefs = new Preferences(scope, UserPreferencesEntries.M_WORKFLOW, preferenceId, preferencesEnc);

      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      adminService.savePreferences(prefs);
   }

   /**
    * @param preferenceScope
    * @param preferenceId
    * @return
    */
   public static Map<String, Object> getWorklistConfigurationMap(PreferenceScope preferenceScope, String preferenceId)
   {
      Map<String, Object> prefMap = new HashMap<String, Object>();
      Preferences prefs = getWorklistConfiguration(preferenceScope, preferenceId);
      if (null != prefs)
      {
         for (Entry<String, Serializable> entry : prefs.getPreferences().entrySet())
         {
            if (null != entry.getValue())
            {
               prefMap.put(entry.getKey(), GsonUtils.readJsonMap((String) entry.getValue()));
            }
         }
      }
      return prefMap;
   }

   /**
    * @param preferenceScope
    * @param preferenceId
    * @return
    */
   public static Preferences getWorklistConfiguration(PreferenceScope preferenceScope, String preferenceId)
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      return adminService.getPreferences(preferenceScope, UserPreferencesEntries.M_WORKFLOW, preferenceId);
   }

   /**
    * @param participantInfo
    * @return
    */
   public static String getParticipantKey(ParticipantInfo participantInfo)
   {
      if (participantInfo instanceof ModelParticipantInfo)
      {
         ModelParticipantInfo modelParticipantInfo = (ModelParticipantInfo) participantInfo;
         String modelId = ModelUtils.extractModelId(modelParticipantInfo.getQualifiedId());
         DmsPrincipal dp = new DmsPrincipal(modelParticipantInfo, modelId);
         return dp.getName();
      }
      else if (participantInfo instanceof UserGroupInfo)
      {
         DmsPrincipal dp = new DmsPrincipal((UserGroupInfo) participantInfo);
         return dp.getName();
      }
      return participantInfo.getQualifiedId();
   }

   /**
    * @param departmentInfo
    * @return
    */
   public static String getDepartmentKey(DepartmentInfo departmentInfo)
   {
      if (departmentInfo instanceof DepartmentDetails)
      {
         DepartmentDetails departmentDetails = (DepartmentDetails) departmentInfo;
         String modelId = ModelUtils.extractModelId(departmentDetails.getOrganization().getQualifiedId());
         return DmsPrincipal.getModelParticipantPrincipalName(departmentDetails.getOrganization().getId(),
               departmentDetails.getId(), modelId);
      }
      return departmentInfo.getId();
   }

   /**
    * utility method for DEV
    */
   private static void resetPreference()
   {
      savePreferences(PreferenceScope.PARTITION, "worklist-participant-columns", new HashMap<String, Object>());
      savePreferences(PreferenceScope.PARTITION, "worklist-process-columns", new HashMap<String, Object>());
      // savePreferenceMap(PreferenceScope.PARTITION, "preference", new HashMap<String,
      // Object>());
      //

      Preferences prefs = new Preferences(PreferenceScope.PARTITION, "bcc", "traffic-light-view",
            new HashMap<String, Serializable>());
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      adminService.savePreferences(prefs);

      // prefs = new Preferences(PreferenceScope.PARTITION,
      // UserPreferencesEntries.M_WORKFLOW,
      // "worklist-participant-columns", new HashMap<String, Serializable>());
      // adminService.savePreferences(prefs);
      //
      // prefs = new Preferences(PreferenceScope.PARTITION,
      // UserPreferencesEntries.M_WORKFLOW, "worklist-process-columns",
      // new HashMap<String, Serializable>());
      // adminService.savePreferences(prefs);

      prefs = new Preferences(PreferenceScope.PARTITION, UserPreferencesEntries.M_WORKFLOW, "preference",
            new HashMap<String, Serializable>());
      adminService.savePreferences(prefs);
      // UserPreferencesHelper helper =
      // UserPreferencesHelper.getInstance(UserPreferencesEntries.M_WORKFLOW,
      // org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope.PARTITION);
      // helper.setString(UserPreferencesEntries.V_WORKLIST, new ArrayList<String>());
      /*
       * prefs = new Preferences(PreferenceScope.USER, UserPreferencesEntries.M_WORKFLOW,
       * "worklist-process-columns", new HashMap<String, Serializable>());
       * adminService.savePreferences(prefs);
       */

      /*
       * prefs = new Preferences(PreferenceScope.USER, "ipp-views-common", "preference",
       * new HashMap<String, Serializable>()); adminService.savePreferences(prefs);
       */

   }
}
