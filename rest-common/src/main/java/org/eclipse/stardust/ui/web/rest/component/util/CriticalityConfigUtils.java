package org.eclipse.stardust.ui.web.rest.component.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.javascript.CriticalityEvaluationAction;
import org.eclipse.stardust.engine.core.monitoring.ActivityInstanceStateChangeMonitor;
import org.eclipse.stardust.engine.core.monitoring.UpdateCriticalityAction;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStoreUtils;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.preferences.PreferencesConstants;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil.ICON_COLOR;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.springframework.stereotype.Component;

@Component("CriticalityConfigUtilsREST")
public class CriticalityConfigUtils
{
   private static final Logger trace = LogManager.getLogger(CriticalityConfigUtils.class);
   
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;
   
   public static final String COLUMN_SEPARATOR = "#!#";
   
   public static final String CRITICALITY_CAT_PREF_KEY_PREFIX = "Criticality.Ranges.Range";
   public static final String CRITICALITY_CAT_TOTAL_COUNT = "TotalCount";
   public static final String CRITICALITY_CAT_LOWER_BOUND = "LowerBound";
   public static final String CRITICALITY_CAT_UPPER_BOUND = "UpperBound";
   public static final String CRITICALITY_CAT_LABEL = "Label";
   public static final String CRITICALITY_CAT_ICON = "Icon";
   public static final String CRITICALITY_CAT_ICON_DISPLAY = "IconDisplay";
   
   private static final int UNDEFINED_CC_RANGE_FROM = -1000;
   private static final int UNDEFINED_CC_RANGE_TO = -1;
   
 

   public List<CriticalityCategory> getCriticalityCategoriesList()
   {
      List<CriticalityCategory> criticalityCategoriesList = new ArrayList<CriticalityCategory>();
      Map<String, Serializable> criticalityCategoryPrefs = readCriticalityCategoryPrefsMap();
      if (!CollectionUtils.isEmpty(criticalityCategoryPrefs))
      {
         int noOfCategories = (Integer) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + "." + CRITICALITY_CAT_TOTAL_COUNT);
         for (int i = 0; i < noOfCategories; i++)
         {
            CriticalityCategory cc = new CriticalityCategory();
            cc.setRangeFrom((Integer) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_LOWER_BOUND));
            cc.setRangeTo((Integer) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_UPPER_BOUND));
            cc.setLabel((String) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_LABEL));
            if (!criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_ICON).toString().isEmpty()) {
               cc.setIconColor(ICON_COLOR.valueOf((String) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_ICON)));
            }
            cc.setIconCount((Integer) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_ICON_DISPLAY));
            criticalityCategoriesList.add(cc);
         }
      }
      else
      {
         criticalityCategoriesList = CriticalityConfigurationUtil.getDefaultCriticalityCategoriesList();
      }
      
      return criticalityCategoriesList;
   }
   
   /**
    * @return
    */
   public Map<String, Serializable> readCriticalityCategoryPrefsMap()
   {
      Map<String, Serializable> prefMap = null;
      Preferences prefs = getPreferences(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
            UserPreferencesEntries.P_ACTIVITY_CRITICALITY_CONFIG);
      if (null != prefs)
      {
         prefMap = prefs.getPreferences();
      }

      return prefMap;
   }
   
   /**
    * @param preferences
    */
   public void saveCriticalityCategories(Map<String, Serializable> preferences)
   {
      savePreferenceMap(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
            UserPreferencesEntries.P_ACTIVITY_CRITICALITY_CONFIG, preferences);
   }
   
   /**
    * 
    */
   public void saveCriticalityEnginePreferences(boolean onActivityCreation, boolean onActivitySuspendAndSave,
         boolean onProcessPriorityChange, String defaultCriticalityFormula)
   {
      AdministrationService adminService = getAdministrationService();
      Map<String, Serializable> preferenceMap = new HashMap<String, Serializable>();
      preferenceMap.put(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONCREATE,
            onActivityCreation);
      preferenceMap.put(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONSUSPEND,
            onActivitySuspendAndSave);
      preferenceMap.put(UpdateCriticalityAction.CRITICALITY_PREF_RECALC_ONPRIORITY, onProcessPriorityChange);
      preferenceMap.put(CriticalityEvaluationAction.DEFAULT_PREF_CRITICALITY_FORMULA, defaultCriticalityFormula);

      Preferences prefs = new Preferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES, preferenceMap);

      adminService.savePreferences(prefs);
   }
   
   public CriticalityCategory getUndefinedCriticalityCategory()
   {
      CriticalityCategory cc = new CriticalityCategory();
      cc.setRangeFrom(UNDEFINED_CC_RANGE_FROM);
      cc.setRangeTo(UNDEFINED_CC_RANGE_TO);
      cc.setLabel(MessagesViewsCommonBean.getInstance().getString("views.criticalityConf.criticality.categories.undefined.label"));
      cc.setIconColor(ICON_COLOR.WHITE_WARNING);
      cc.setIconCount(1);
      
      return cc;
   }
   
   /**
    * @return
    */
   public boolean retrieveOnCreateCriticalityCalc()
   {
      AdministrationService adminService = getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Boolean criteria = (Boolean) retrievedPrefs.getPreferences().get(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONCREATE);
      
      if (null != criteria)
      {
         return criteria.booleanValue();
      }

      return true; //default true
   }
   
   /**
    * @return
    */
   public boolean retrieveOnSuspendCriticalityCalc()
   {
      AdministrationService adminService = getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Boolean criteria = (Boolean) retrievedPrefs.getPreferences().get(ActivityInstanceStateChangeMonitor.CRITICALITY_PREF_RECALC_ONSUSPEND);
      
      if (null != criteria)
      {
         return criteria.booleanValue();
      }
    
      return true; //default true
   }
   
   /**
    * @return
    */
   public boolean retrieveOnPrioChangeCriticalityCalc()
   {
      AdministrationService adminService = getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      Boolean criteria = (Boolean) retrievedPrefs.getPreferences().get(UpdateCriticalityAction.CRITICALITY_PREF_RECALC_ONPRIORITY);
      
      if (null != criteria)
      {
         return criteria.booleanValue();
      }
      
      return true; //default true
   }
   
   /**
    * @return
    */
   public String retrieveDefaultCriticalityFormula()
   {
      AdministrationService adminService = getAdministrationService();
      Preferences retrievedPrefs = adminService.getPreferences(PreferenceScope.PARTITION,
            PreferencesConstants.MODULE_ID_ENGINE_INTERNALS,
            PreferencesConstants.PREFERENCE_ID_WORKFLOW_CRITICALITES);
      String defaultFormula = (String) retrievedPrefs.getPreferences().get(CriticalityEvaluationAction.DEFAULT_PREF_CRITICALITY_FORMULA);
      
      if (StringUtils.isNotEmpty(defaultFormula))
      {
         return defaultFormula;
      }
      
      return "";
   }
   
   public void exportCriticalityConfig(OutputStream outputStream) throws Exception {
      List<Preferences> preferencesList = new ArrayList<Preferences>();
      preferencesList.add(readCriticalityCategoryPrefs());
      
      if (preferencesList != null)
      {
         PreferenceStoreUtils.backupToZipFile(outputStream, preferencesList, serviceFactoryUtils.getServiceFactory());
      }
   }
   
   public void importCriticalityConfig(File file) throws Exception
   {
      String filePath = file.getAbsolutePath();
      InputStream inputStream = null;

      MessagesViewsCommonBean messageBean = MessagesViewsCommonBean.getInstance();

      try
      {
         if (filePath.endsWith(FileUtils.ZIP_FILE))
         {
            inputStream = new FileInputStream(file);
            PreferenceStoreUtils.loadFromZipFile(inputStream, serviceFactoryUtils.getServiceFactory());
         }
         else
         {
            throw new Exception(messageBean.getString("views.configurationImportDialog.invalidFileFormat"));
         }
      }
      catch (Exception e)
      {
         trace.error(e, e);
         throw e;
      }
      finally
      {
         FileUtils.close(inputStream);
      }
   }
   
   /**
    * @return
    */
   public Preferences readCriticalityCategoryPrefs()
   {
      return getPreferences(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
            UserPreferencesEntries.P_ACTIVITY_CRITICALITY_CONFIG);
   }
   
   /**
    * @return
    */
   private void savePreferenceMap(PreferenceScope scope, String moduleId, String preferenceId, Map<String, Serializable> preferences)
   {
      AdministrationService adminService = getAdministrationService();
      Preferences prefs = new Preferences(scope, moduleId, preferenceId, preferences);
      adminService.savePreferences(prefs);
   }
   
   /**
    * @return
    */
   private Preferences getPreferences(PreferenceScope scope, String moduleId, String preferenceId)
   {
      AdministrationService adminService = getAdministrationService();
      return adminService.getPreferences(scope, moduleId, preferenceId);
   }
   
   private AdministrationService getAdministrationService() {
      return serviceFactoryUtils.getAdministrationService();
   }
}
