/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * 
 * @author Johnson.Quadras
 *
 */
package org.eclipse.stardust.ui.web.rest.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.query.PreferenceQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariable;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableScope;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariableUtils;
import org.eclipse.stardust.engine.core.preferences.configurationvariables.ConfigurationVariables;
import org.eclipse.stardust.ui.web.rest.service.dto.PreferenceDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PreferenceService
{
   private static final Logger trace = LogManager.getLogger(PreferenceService.class);

   private static final String PASSWORD_CHAR = "*";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Autowired
   UserService userService;

   /**
    * 
    * @return
    */
   public List<PreferenceDTO> fetchPartitionPreferences()
   {

      QueryService qService = serviceFactoryUtils.getQueryService();
      List<Preferences> prefs = new ArrayList<Preferences>();

      trace.debug("Fetchong preferences for PARTITION scope");
      // fetch all the Partition preferences
      prefs = qService.getAllPreferences(PreferenceQuery.findAll(PreferenceScope.PARTITION));

      List<PreferenceDTO> prefList = buildPreferenceList(prefs);

      return prefList;

   }

   /**
    * 
    * @param userId
    * @param realmId
    * @return
    */
   public List<PreferenceDTO> fetchUserPreferences(String userId, String realmId)
   {
      List<Preferences> prefs = new ArrayList<Preferences>();
      // fetch all preference store entries for User, the moduleId and PreferenceId
      // can be passed as '*'
      if (null != userId && null != realmId)
      {
         prefs = serviceFactoryUtils.getQueryService().getAllPreferences(
               PreferenceQuery.findPreferencesForUsers(realmId, userId, "*", "*"));
      }

      List<PreferenceDTO> prefList = buildPreferenceList(prefs);

      return prefList;
   }

   /**
    * 
    * @param prefs
    * @return
    */
   private List<PreferenceDTO> buildPreferenceList(List<Preferences> prefs)
   {
      List<PreferenceDTO> prefList = new ArrayList<PreferenceDTO>();
      Map<String, ConfigurationVariables> ConfigVariables = getConfigurationVariables();

      for (Preferences pref : prefs)
      {
         Map<String, Serializable> pref11 = pref.getPreferences();

         for (Map.Entry<String, Serializable> entry : pref11.entrySet())
         {
            boolean isPasswordType = isPassword(ConfigVariables, pref, entry.getKey());
            String value = entry.getValue().toString();
            isPasswordType = false;
            if (isPasswordType)
            {
               value = getPasswordText(value);
            }
            PreferenceDTO dto = new PreferenceDTO(pref.getScope().name(), pref.getModuleId(),
                  pref.getPreferencesId(), entry.getKey(), value, isPasswordType, pref.getPartitionId());
            prefList.add(dto);

         }
      }

      return prefList;
   }

   private String getPasswordText(String value)
   {
      StringBuilder passwordText = new StringBuilder();
      for (int index = 0; index < value.length(); index++)
      {
         passwordText.append(PASSWORD_CHAR);
      }
      return passwordText.toString();
   }

   /**
    * @param ConfigVariables
    * @param pref
    * @param key
    * @return
    */
   private boolean isPassword(Map<String, ConfigurationVariables> ConfigVariables, Preferences pref, String key)
   {
      if (ConfigurationVariableUtils.CONFIGURATION_VARIABLES.equals(pref.getModuleId()))
      {
         ConfigurationVariables confVariables = ConfigVariables.get(pref.getPreferencesId());
         List<ConfigurationVariable> cvs = confVariables.getConfigurationVariables();
         for (ConfigurationVariable configurationVariable : cvs)
         {
            if (configurationVariable.getName().equals(ConfigurationVariableUtils.getName(key)))
            {
               if (ConfigurationVariableScope.Password.equals(configurationVariable.getType()))
               {
                  return true;
               }
            }
         }

      }
      return false;
   }

   /**
    * @return
    */
   private Map<String, ConfigurationVariables> getConfigurationVariables()
   {
      Map<String, ConfigurationVariables> configVariables = new HashMap<String, ConfigurationVariables>();

      AdministrationService administrationService = serviceFactoryUtils.getAdministrationService();
      Collection<DeployedModel> models = ModelCache.findModelCache().getAllModels();

      Set<String> idSet = new HashSet<String>();

      for (Model model : models)
      {
         idSet.add(model.getId());
      }

      for (String id : idSet)
      {
         // Retrieving config variable(String type) and password type
         ConfigurationVariables confVariables = administrationService.getConfigurationVariables(id, true);

         // add model only if ConfigurationVariables present for model id
         if (!confVariables.getConfigurationVariables().isEmpty())
         {
            configVariables.put(id, confVariables);
         }
      }

      return configVariables;
   }

   /**
   *
   */
   public static enum PREF_VIEW_TYPE {
      PARTITION, USER;
   }

}
