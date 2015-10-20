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
import java.util.Set;

import org.eclipse.stardust.common.criticality.CriticalityUtils;
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
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.WHITE, "pi pi-lg pi-criticalty-flag criticality-flag-WHITE");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.RED, "pi pi-lg pi-criticalty-flag criticality-flag-RED");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.GREEN, "pi pi-lg pi-criticalty-flag criticality-flag-GREEN");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.BLUE, "pi pi-lg pi-criticalty-flag criticality-flag-BLUE");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.PURPLE, "pi pi-lg pi-criticalty-flag criticality-flag-PURPLE");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.ORANGE, "pi pi-lg pi-criticalty-flag criticality-flag-ORANGE");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.PINK, "pi pi-lg pi-criticalty-flag criticality-flag-PINK");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.YELLOW, "pi pi-lg pi-criticalty-flag criticality-flag-YELLOW");
      CRITICALITY_COLOR_FLAG_MAP.put(ICON_COLOR.WHITE_WARNING, "pi pi-lg pi-criticalty-flag criticality-flag-WHITE-WARNING");
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
      List<CriticalityCategory> defaultCriticalityList = new ArrayList<CriticalityCategory>();
      
      String lowLabel = MessagesViewsCommonBean.getInstance().getString("views.criticalityConf.criticality.categories.label.default.low");
      String mediumLabel = MessagesViewsCommonBean.getInstance().getString("views.criticalityConf.criticality.categories.label.default.medium");
      String highLabel = MessagesViewsCommonBean.getInstance().getString("views.criticalityConf.criticality.categories.label.default.high");
      
      Map<String,ICON_COLOR > defaultIcons = new HashMap<String, ICON_COLOR>();
      defaultIcons.put(lowLabel, ICON_COLOR.BLUE);
      defaultIcons.put(mediumLabel, ICON_COLOR.YELLOW);
      defaultIcons.put(highLabel, ICON_COLOR.RED);
      
      
      Set<org.eclipse.stardust.common.criticality.CriticalityCategory> defaultCategories = CriticalityUtils.getCriticalityDefaultCategories(lowLabel, mediumLabel, highLabel);
      
      for (org.eclipse.stardust.common.criticality.CriticalityCategory criticalCategory : defaultCategories)
      {
         CriticalityCategory category = new CriticalityCategory();
         
         int portalLowerBound = (int) (criticalCategory.lowerBound() * PORTAL_CRITICALITY_MULTIPLICATION_FACTOR);
         int portalUpperBound = (int) (criticalCategory.upperBound() * PORTAL_CRITICALITY_MULTIPLICATION_FACTOR);
         category.setRangeFrom(portalLowerBound);
         category.setRangeTo(portalUpperBound);
         category.setIconColor(defaultIcons.get(criticalCategory.name()));
         category.setIconCount(1);
         category.setLabel(criticalCategory.name());
         defaultCriticalityList.add(category);
      }
      
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
      return null != criticality ? criticality.getLabel() + " (" + criticalityVal + ")" : null;
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
