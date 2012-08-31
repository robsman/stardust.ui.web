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

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserInfoDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.processportal.common.Constants;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;

/**
 * @author Yogesh.Manware
 * 
 */
public class WorklistConfigurationUtil
{
   public static final String SELECTED_COLS = "selectedColumns";
   public static final String LOCK = "lock";
   public static final String DEFAULT = "Default";

   public static Map<String, Object> DEFAULT_CONF;
   static
   {
      DEFAULT_CONF = new HashMap<String, Object>();
      ArrayList<String> colsToBeSaved = new ArrayList<String>();
      colsToBeSaved.add(Constants.COL_ACTIVITY_NAME);
      colsToBeSaved.add(Constants.COL_OID);
      //colsToBeSaved.add(Constants.COL_PROCESS_DEFINITION);
      colsToBeSaved.add(Constants.COL_CRITICALITY);
      colsToBeSaved.add(Constants.COL_PRIORITY);
      colsToBeSaved.add(Constants.COL_DESCRIPTORS);
      colsToBeSaved.add(Constants.COL_STARTED);
      colsToBeSaved.add(Constants.COL_LAST_MODIFIED);
      colsToBeSaved.add(Constants.COL_DURATION);
      colsToBeSaved.add(Constants.COL_LAST_PERFORMER);
      //colsToBeSaved.add(Constants.COL_STATUS);
      //colsToBeSaved.add(Constants.COL_ASSIGNED_TO);
      DEFAULT_CONF.put(WorklistConfigurationUtil.SELECTED_COLS, colsToBeSaved);
      DEFAULT_CONF.put(WorklistConfigurationUtil.LOCK, "false");
   }

   public static Map<String, Object> getParticipantStoredValues(ParticipantInfo participantInfo)
   {
      return getParticipantStoredValues(getOid(participantInfo));
   }

   public static String getOid(ParticipantInfo participantInfo)
   {
      Long oId = null;
      String strOid;
      if (participantInfo instanceof UserInfoDetails)
      {
         oId = ((UserInfoDetails) participantInfo).getOID();
      }
      else if (participantInfo instanceof ModelParticipant)
      {
         oId = (long) ((ModelParticipant) participantInfo).getElementOID();
      }
      else if (participantInfo instanceof ModelParticipantInfoDetails)
      {
         oId = ((ModelParticipantInfoDetails) participantInfo).getRuntimeElementOID();
      }
      else if (participantInfo instanceof DepartmentInfo)
      {
         oId = ((DepartmentInfo) participantInfo).getOID();
      }

      if (null == oId)
      {
         strOid = String.valueOf(oId);
      }
      else
      {
         strOid = DEFAULT;
      }
      return strOid;
   }

   public static Map<String, Object> getParticipantStoredValues(String oId)
   {
      return getStoredValues(oId, true);
   }

   public static Map<String, Object> getProcessStoredValues(String oId)
   {
      return getStoredValues(oId, false);
   }

   public static Map<String, Object> getStoredValues(String oId, boolean isParticipant)
   {
      // check for the lock in default table
      Map<String, Object> worklistConf;
      if (isParticipant)
      {
         worklistConf = getParticipantWorklistConfigurationMap(PreferenceScope.PARTITION);
      }
      else
      {
         worklistConf = getProcessWorklistConfigurationMap(PreferenceScope.PARTITION);
      }

      Map<String, Object> configuration = getStoredValues(oId, worklistConf);
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
         fetchUserConf = true;
      }

      if (fetchUserConf)
      {

         if (isParticipant)
         {
            worklistConf = getParticipantWorklistConfigurationMap(PreferenceScope.USER);
         }
         else
         {
            worklistConf = getProcessWorklistConfigurationMap(PreferenceScope.USER);
         }

         if (CollectionUtils.isNotEmpty(worklistConf))
         {
            configuration = getStoredValues(oId, worklistConf);
         }
      }

      if (CollectionUtils.isEmpty(configuration))
      {
         configuration = getStoredValues(DEFAULT, worklistConf);
      }
      return configuration;
   }

   public static Map<String, Object> getStoredValues(String oId, Map<String, Object> worklistConf)
   {
      @SuppressWarnings("unchecked")
      Map<String, Object> configuration = (Map<String, Object>) worklistConf.get(oId);

      if (DEFAULT.equals(oId))
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

   public static void updateValues(String Oid, ArrayList<String> selectColumns, boolean lock,
         Map<String, Object> procPartWorklistConf)
   {
      HashMap<String, Serializable> elementPreference = (HashMap<String, Serializable>) procPartWorklistConf.get(Oid);

      if (null == elementPreference)
      {
         elementPreference = new HashMap<String, Serializable>();
         procPartWorklistConf.put(String.valueOf(Oid), elementPreference);
      }
      elementPreference.put(SELECTED_COLS, selectColumns);
      elementPreference.put(LOCK, String.valueOf(lock));
   }

   public static void deleteValues(String Oid, Map<String, Object> procPartWorklistConf)
   {
      if (null != procPartWorklistConf)
      {
         procPartWorklistConf.remove(Oid);
      }
   }

   public static void saveParticipantWorklistConfiguration(Map<String, Object> preferences)
   {
      savePreferenceMap(PreferenceScope.PARTITION, UserPreferencesEntries.M_WORKFLOW,
            UserPreferencesEntries.V_WORKLIST_PART_CONF, preferences);
   }

   public static void saveProcessWorklistConfiguration(Map<String, Object> preferences)
   {
      savePreferenceMap(PreferenceScope.PARTITION, UserPreferencesEntries.M_WORKFLOW,
            UserPreferencesEntries.V_WORKLIST_PROC_CONF, preferences);
   }

   /**
    * @return
    */
   private static void savePreferenceMap(PreferenceScope scope, String moduleId, String preferenceId,
         Map<String, Object> preferences)
   {
      Map<String, Serializable> preferencesNew = new HashMap<String, Serializable>();

      if (null != preferences)
      {
         preferencesNew.put(preferenceId, GsonUtils.stringify(preferences));
      }

      Preferences prefs = new Preferences(scope, moduleId, preferenceId, preferencesNew);

      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      adminService.savePreferences(prefs);
   }

   public static Map<String, Object> getParticipantWorklistConfigurationMap(PreferenceScope preferenceScope)
   {
      Map<String, Object> prefMap = new HashMap<String, Object>();
      Preferences prefs = getPartcipantWorklistConfiguration(preferenceScope);
      if (null != prefs)
      {
         Object prefObject = prefs.getPreferences().get(UserPreferencesEntries.V_WORKLIST_PART_CONF);
         if (null != prefObject)
         {
            prefMap = GsonUtils.readJsonMap((String) prefObject);
         }
      }
      return prefMap;
   }

   public static Map<String, Object> getProcessWorklistConfigurationMap(PreferenceScope preferenceScope)
   {
      Map<String, Object> prefMap = new HashMap<String, Object>();
      Preferences prefs = getProcessWorklistConfiguration(PreferenceScope.PARTITION);
      if (null != prefs)
      {
         if (null != prefs)
         {
            Object prefObject = prefs.getPreferences().get(UserPreferencesEntries.V_WORKLIST_PROC_CONF);
            if (null != prefObject)
            {
               prefMap = GsonUtils.readJsonMap((String) prefObject);
            }
         }
      }
      return prefMap;
   }

   public static Preferences getPartcipantWorklistConfiguration(PreferenceScope preferenceScope)
   {
      return getPreferences(preferenceScope, UserPreferencesEntries.M_WORKFLOW,
            UserPreferencesEntries.V_WORKLIST_PART_CONF);
   }

   public static Preferences getProcessWorklistConfiguration(PreferenceScope preferenceScope)
   {
      return getPreferences(preferenceScope, UserPreferencesEntries.M_WORKFLOW,
            UserPreferencesEntries.V_WORKLIST_PROC_CONF);
   }

   /**
    * @return
    */
   private static Preferences getPreferences(PreferenceScope scope, String moduleId, String preferenceId)
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();
      return adminService.getPreferences(scope, moduleId, preferenceId);
   }

}
