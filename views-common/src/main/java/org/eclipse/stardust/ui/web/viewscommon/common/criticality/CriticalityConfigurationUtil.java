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
package org.eclipse.stardust.ui.web.viewscommon.common.criticality;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



/**
 * @author Shrikant.Gangal
 *
 */
public class CriticalityConfigurationUtil
{
   public static final String COLUMN_SEPARATOR = "#!#";
   
   public static final int PORTAL_CRITICALITY_MAX = 1000;
   public static final int PORTAL_CRITICALITY_MULTIPLICATION_FACTOR = 1000;
   public static final double PORTAL_CRITICALITY_DIV_FACTOR = 1000;
   public static final String CRITICALITY_CAT_PREF_KEY_PREFIX = "Criticality.Ranges.Range";
   public static final String CRITICALITY_CAT_TOTAL_COUNT = "TotalCount";
   public static final String CRITICALITY_CAT_LOWER_BOUND = "LowerBound";
   public static final String CRITICALITY_CAT_UPPER_BOUND = "UpperBound";
   public static final String CRITICALITY_CAT_LABEL = "Label";
   public static final String CRITICALITY_CAT_ICON = "Icon";
   public static final String CRITICALITY_CAT_ICON_DISPLAY = "IconDisplay";
   
   private static final int UNDEFINED_CC_RANGE_FROM = -1000;
   private static final int UNDEFINED_CC_RANGE_TO = -1;
   
   
   public static enum ICON_COLOR {
      WHITE, RED, GREEN, BLUE, PURPLE, YELLOW, ORANGE, PINK, WHITE_WARNING
   };
   
   private static final Map<ICON_COLOR, String> CRITICALITY_COLOR_FLAG_MAP = new HashMap<ICON_COLOR, String>();
   static {
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.WHITE, "/plugins/views-common/images/icons/criticality/flag_white.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.RED, "/plugins/views-common/images/icons/criticality/flag_red.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.GREEN, "/plugins/views-common/images/icons/criticality/flag_green.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.BLUE, "/plugins/views-common/images/icons/criticality/flag_blue.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.PURPLE, "/plugins/views-common/images/icons/criticality/flag_purple.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.ORANGE, "/plugins/views-common/images/icons/criticality/flag_orange.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.PINK, "/plugins/views-common/images/icons/criticality/flag_pink.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.YELLOW, "/plugins/views-common/images/icons/criticality/flag_yellow.png");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.WHITE_WARNING, "/plugins/views-common/images/icons/criticality/flag_white_warning.png");
   }
   
   public static String getIcon(ICON_COLOR color)
   {
      return CRITICALITY_COLOR_FLAG_MAP.get(color);
   }
   
   public static String getIcon(String color)
   {
      return CRITICALITY_COLOR_FLAG_MAP.get(ICON_COLOR.valueOf(color));
   }
   
   
   public static List<CriticalityCategory> getDefaultCriticalityCategoriesList()
   {
      final int DEFAULT_LOW_MIN = 0;
      final int DEFAULT_LOW_MAX = 333;
      final int DEFAULT_MEDIUM_MIN = 334;
      final int DEFAULT_MEDIUM_MAX = 666;
      final int DEFAULT_HIGH_MIN = 667;
      final int DEFAULT_HIGH_MAX = 1000;
      List<CriticalityCategory> defaultCriticalityList = new ArrayList<CriticalityCategory>();
      CriticalityCategory low = new CriticalityCategory();
      low.setRangeFrom(DEFAULT_LOW_MIN);
      low.setRangeTo(DEFAULT_LOW_MAX);
      low.setIconColor(ICON_COLOR.BLUE);
      low.setIconCount(1);
      low.setLabel(MessagesViewsCommonBean.getInstance().getString("views.criticalityConf.criticality.categories.label.default.low"));
      defaultCriticalityList.add(low);
      CriticalityCategory medium = new CriticalityCategory();
      medium.setRangeFrom(DEFAULT_MEDIUM_MIN);
      medium.setRangeTo(DEFAULT_MEDIUM_MAX);
      medium.setIconColor(ICON_COLOR.YELLOW);
      medium.setIconCount(1);
      medium.setLabel(MessagesViewsCommonBean.getInstance().getString("views.criticalityConf.criticality.categories.label.default.medium"));
      defaultCriticalityList.add(medium);
      CriticalityCategory high = new CriticalityCategory();
      high.setRangeFrom(DEFAULT_HIGH_MIN);
      high.setRangeTo(DEFAULT_HIGH_MAX);
      high.setIconColor(ICON_COLOR.RED);
      high.setIconCount(1);
      high.setLabel(MessagesViewsCommonBean.getInstance().getString("views.criticalityConf.criticality.categories.label.default.high"));
      defaultCriticalityList.add(high);
      
      return defaultCriticalityList;
   }
   
   /**
    * Converts the criticality 'java double' value in range [0, 1] to an integer in range [0, 1000]
    * 
    * @param criticality
    * @return
    */
   public static int getPortalCriticality(double criticality)   
   {
      return (int) Math.round(criticality * PORTAL_CRITICALITY_MULTIPLICATION_FACTOR);
   }
   
   /**
    * Converts the criticality 'java int' value in range [0, 1000] to a double in range [0, 1] 
    * 
    * @param criticality
    * @return
    */
   public static double getEngineCriticality(int criticality)   
   {
      return (double) (criticality / PORTAL_CRITICALITY_DIV_FACTOR);
   }
   
   
   /**
    * @param criticality
    * @return
    */
   public static CriticalityCategory getCriticalityForLabel(String label)
   {
      List<CriticalityCategory> cCats = CriticalityConfigurationHelper.getInstance().getCriticalityConfiguration();
      for (CriticalityCategory cCat : cCats)
      {
         if (cCat.getLabel().equals(label))
         {
            return cCat;
         }
      }

      return null;
   }
   
   public static String getCriticalityDisplayLabel(int criticalityVal, CriticalityCategory criticality)
   {
      return criticality.getLabel() + " (" + criticalityVal + ")";
   }
   
   public static List<CriticalityCategory> getCriticalityCategoriesList()
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
            cc.setIconColor(ICON_COLOR.valueOf((String) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_ICON)));
            cc.setIconCount((Integer) criticalityCategoryPrefs.get(CRITICALITY_CAT_PREF_KEY_PREFIX + i + "." + CRITICALITY_CAT_ICON_DISPLAY));
            criticalityCategoriesList.add(cc);
         }
      }
      else
      {
         criticalityCategoriesList = getDefaultCriticalityCategoriesList();
      }
      
      return criticalityCategoriesList;
   }
   
   /**
    * @return
    */
   public static Map<String, Serializable> readCriticalityCategoryPrefsMap()
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
    * @return
    */
   public static Preferences readCriticalityCategoryPrefs()
   {
      Map<String, Serializable> prefMap = null;
      return getPreferences(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
            UserPreferencesEntries.P_ACTIVITY_CRITICALITY_CONFIG);
   }   
   
   /**
    * @param preferences
    */
   public static void saveCriticalityCategories(Map<String, Serializable> preferences)
   {
      savePreferenceMap(PreferenceScope.PARTITION, UserPreferencesEntries.M_ADMIN_PORTAL,
            UserPreferencesEntries.P_ACTIVITY_CRITICALITY_CONFIG, preferences);
   }
   
   public static CriticalityCategory getUndefinedCriticalityCategory()
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
   private static void savePreferenceMap(PreferenceScope scope, String moduleId, String preferenceId, Map<String, Serializable> preferences)
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      Preferences prefs = new Preferences(scope, moduleId, preferenceId, preferences);
      adminService.savePreferences(prefs);
   }
   
   /**
    * @return
    */
   private static Preferences getPreferences(PreferenceScope scope, String moduleId, String preferenceId)
   {
      AdministrationService adminService = SessionContext.findSessionContext().getServiceFactory().getAdministrationService();
      return adminService.getPreferences(scope, moduleId, preferenceId);
   }
}
