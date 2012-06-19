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

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceEditor;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceProvider;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceStore;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.common.util.StringUtils;



/**
 * @author Subodh.Godbole
 */
public class UserPreferencesHelper
{
   private static final Logger trace = LogManager.getLogger(UserPreferencesHelper.class);
   
   private static final String INSTANCE_FOR_MODULE = "#.?.UserPreferencesHelper/instance"; // scope.moduleId.UserPreferencesHelper/instance
   private static final String PREFERENCE_COLUMN_SELECTION_KEY = "?.#.selectedColumns"; // moduleId.viewId.selectedColumns
   private static final String STRING_SELECTION_KEY = "?.#.string"; // moduleId.viewId.string
   private static final String GENERIC_COMPONENT_SELECTION_KEY = "?.#."; // moduleId.viewId.<featureId>
   
   private static final String REFERENCE_ID = "preference";
   
   private PreferenceProvider preferenceProvider;
   
   private String moduleId;
   private PreferenceEditor userPreferencesEditor;
   private PreferenceStore userPreferencesStore;

   /**
    * @param moduleId
    */
   private UserPreferencesHelper(String moduleId, PreferenceScope preferenceScope)
   {
      preferenceProvider = UserPreferenceBean.getInstance().getPreferenceProvider();
      
      if(preferenceProvider.isEnabled())
      {
         this.moduleId = moduleId;
         userPreferencesEditor = preferenceProvider.getPreferenceEditor(preferenceScope, moduleId, REFERENCE_ID);
         userPreferencesStore = userPreferencesEditor;
      }
      else
      {
         log("Note: Not Saving User Preferences for Module -> " + moduleId);
      }
   }

   public static SelectItem[] getPreferencesScopesItems()
   {
      MessagePropertiesBean msgBean = MessagePropertiesBean.getInstance();

      int i = 0;
      SelectItem[] preferenceScopes = new SelectItem[2];
      preferenceScopes[i++] = new SelectItem(PreferenceScope.USER,
            msgBean.getString("common.preferenceScope.options.user"));
      preferenceScopes[i++] = new SelectItem(PreferenceScope.PARTITION, 
            msgBean.getString("common.preferenceScope.options.partition"));
      
      return preferenceScopes;
   }
   
   /**
    * @param moduleId
    * @return
    */
   public static UserPreferencesHelper getInstance(String moduleId)
   {
      return getInstance(moduleId, PreferenceScope.USER);
   }

   /**
    * @param moduleId
    * @param preferenceScope
    * @return
    */
   public static UserPreferencesHelper getInstance(String moduleId, PreferenceScope preferenceScope)
   {
      String sessionLookUpId = getSessionLookUpId(moduleId, preferenceScope);
      
      log("[UserPreferencesHelper.getInstance]: sessionLookUpId = " + sessionLookUpId);
      
      UserPreferencesHelper instance = UserPreferencesHelperCache.getInstance().get(sessionLookUpId);
      if (instance == null)
      {
         instance = new UserPreferencesHelper(moduleId, preferenceScope);
         UserPreferencesHelperCache.getInstance().put(sessionLookUpId, instance);
      }

      return instance;
   }

   /**
    * @param moduleId
    * @param viewId
    * @param featureId
    * @param isAdmin
    * @param defaultValue
    * @return
    */
   public int getInteger(String viewId, String featureId, int defaultValue)
   {
      String value = getSingleString(viewId, featureId);

      if(!StringUtils.isEmpty(value))
         return Integer.valueOf(value);

      return defaultValue;
   }

   /**
    * @param viewId
    * @param featureId
    * @param defaultValue
    * @return
    */
   public boolean getBoolean(String viewId, String featureId, boolean defaultValue)
   {
      String value = getSingleString(viewId, featureId);

      if(!StringUtils.isEmpty(value))
         return Boolean.parseBoolean(value);

      return defaultValue;
   }

   /**
    * @param viewId
    * @param featureId
    * @param values
    */
   public void setString(String viewId, String featureId, List<String> values)
   {
      privateSetString(getPreferencesId(viewId, GENERIC_COMPONENT_SELECTION_KEY + featureId), values);
   }

   /**
    * @param viewId
    * @param featureId
    * @param value
    */
   public void setString(String viewId, String featureId, String value)
   {
      List<String> values = new ArrayList<String>();
      values.add(value);
      privateSetString(getPreferencesId(viewId, GENERIC_COMPONENT_SELECTION_KEY + featureId), values);
   }
   
   /**
    * @param viewId
    * @param values
    */
   public void setString(String viewId, List<String> values)
   {
      privateSetString(getPreferencesId(viewId, STRING_SELECTION_KEY), values);
   }

   /**
    * @param viewId
    * @param featureId
    */
   public void resetValue(String viewId, String featureId)
   {
      userPreferencesEditor.reset(getPreferencesId(viewId, GENERIC_COMPONENT_SELECTION_KEY + featureId));
      userPreferencesEditor.save();
   }
   
   /**
    * @param viewId
    */
   public void resetValue(String viewId)
   {
      userPreferencesEditor.reset(getPreferencesId(viewId, STRING_SELECTION_KEY));
      userPreferencesEditor.save();
   }
   
   /**
    * @param viewId
    * @param featureId
    * @return
    *   null -> If no preference value is stored yet
    *   Empty List -> Stored value is empty
    *   Non empty List -> Stored value is not empty
    */
   public List<String> getString(String viewId, String featureId)
   {
      return privateGetString(getPreferencesId(viewId, GENERIC_COMPONENT_SELECTION_KEY + featureId));
   }
   
   /**
    * @param viewId
    * @param featureId
    * @return
    */
   public String getSingleString(String viewId, String featureId)
   {
      List<String> strings = privateGetString(getPreferencesId(viewId, GENERIC_COMPONENT_SELECTION_KEY + featureId));

      if(strings != null && !strings.isEmpty())
         return strings.get(0);
      else
         return "";
   }
   
   /**
    * @param viewId
    * @param featureId
    * @param defaultValue
    * @return
    */
   public String getSingleString(String viewId, String featureId, String defaultValue)
   {
      String value = getSingleString(viewId, featureId);
      return StringUtils.isEmpty(value) ? defaultValue : value;
   }

   /**
    * @param viewId
    * @return
    *   null -> If no preference value is stored yet
    *   Empty List -> Stored value is empty
    *   Non empty List -> Stored value is not empty
    */
   public List<String> getString(String viewId)
   {
      return privateGetString(getPreferencesId(viewId, STRING_SELECTION_KEY));
   }

   /**
    * @param viewId
    * @param cols
    */
   public void setSelectedColumns(String viewId, List<String> cols)
   {
      privateSetString(getPreferencesId(viewId, PREFERENCE_COLUMN_SELECTION_KEY), cols);
   }
   
   /**
    * @param viewId
    * @return
    *   null -> If no preference value is stored yet
    *   Empty List -> Stored value is empty
    *   Non empty List -> Stored value is not empty
    */
   public List<String> getSelectedColumns(String viewId)
   {
      return privateGetString(getPreferencesId(viewId, PREFERENCE_COLUMN_SELECTION_KEY));
   }

   /**
    * @param viewId
    */
   public void resetSelectedColumns(String viewId)
   {
      userPreferencesEditor.reset(getPreferencesId(viewId, PREFERENCE_COLUMN_SELECTION_KEY));
      userPreferencesEditor.save();
   }

   /**
    * @param prefId
    * @param values
    */
   private void privateSetString(String prefId, List<String> values)
   {
      if(!preferenceProvider.isEnabled())
         return;

      if(values.size() == 1)
      {
         userPreferencesEditor.setString(prefId, values.get(0));
      }
      else
      {
         userPreferencesEditor.setList(prefId, values);
      }
      userPreferencesEditor.save();
   }

   /**
    * @param prefId
    * @return
    *   null -> If no preference value is stored yet
    *   Empty List -> Stored value is empty
    *   Non empty List -> Stored value is not empty
    */
   private List<String> privateGetString(String prefId)
   {
      if(!preferenceProvider.isEnabled())
         return null;

      List<String> values = userPreferencesStore.getList(prefId);    

      log("[privateGetString]: " + prefId + ":" + values);
      
      // If Blank Value retrieved then return Blank List else non empty List
      return values; 
   }

   /**
    * @param moduleId
    * @param currentScope
    * @return
    */
   private static String getSessionLookUpId(String moduleId, PreferenceScope currentScope)
   {
      String str = StringUtils.replace(INSTANCE_FOR_MODULE, "#", currentScope.toString());
      str = StringUtils.replace(str, "?", moduleId);
      return str;
   }

   /**
    * Example: ModuleId = "bcc", viewId=businessProcessManagerView.activityTable
    * @param viewId
    * @param key
    * @return
    */
   private String getPreferencesId(String viewId, String key)
   {
      String str = StringUtils.replace(key, "?", moduleId);
      str = StringUtils.replace(str, "#", viewId);
      
      return str;
   }
   
   /**
    * @param msg
    */
   private static void log(String msg)
   {
      trace.debug(msg);
   }
   
   public boolean isUseRepository()
   {
      return preferenceProvider.isEnabled();
   }
}