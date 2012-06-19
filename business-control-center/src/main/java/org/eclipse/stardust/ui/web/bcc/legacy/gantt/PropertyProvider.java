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
package org.eclipse.stardust.ui.web.bcc.legacy.gantt;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.core.compatibility.ui.preferences.AbstractCachedPreferencesManager;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceEditor;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceStore;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferencesManager;
import org.eclipse.stardust.ui.web.bcc.legacy.DefaultTimeProvider;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



public class PropertyProvider
{
   private static final String PROPERTY_PROVIDER_INSTANCE = "ganttChartPropertyProvider/instance";

   public static final String PREFERENCES_ID = "gantt-chart";

   public static final String MODULE_ID = "bcc";

   public static final String PROPERTY_PREFIX = "Infinity.Monitoring.GanttChartView";

   public static final String ESTIMATED_DURATION_PROPERTY = "estimatedDurationSeconds";

   public static final String PLANNED_START_TIME_PROPERTY = "plannedStartTimeHHHH";

   public static final String PLANNED_TERMINATION_TIME_PROPERTY = "plannedTerminationTimeHHHH";

   public static final String THRESHOLD_PROPERTY = "thresholdPercentage";

   public static final String SUCCESSOR_PROPERTY = "successor";

   public static final String PREDECESSOR_PROPERTY = "predecessor";

   public static final String INFINTIY_IGNORE_PROCESS_IDS = "infinity.ingnore.processIds";

   public static final String INSTANCE_DESCRIPTOR_VALUES = "instance.descriptor.values";

   public static final String INSTANCE_DESCRIPTOR_KEY = "instance.descriptor.key";

   public static final String PROPERTY_PROCESS_DEFINITION_IDS = "ProcessDefinitionIds";

   public static final String GANTT_DIAGRAM_TIME_PROVIDER = "bcc.gantt.diagram.time.provider";

   public static final String PROPERTY_KEY_SEPARATOR = ".";

   public static final String ENABLE_DIAGRAM_PROPERTY = "enableDiagram";

   public static final String DISABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY = "disableDiagramForAllProcessDefinitions";

   public static final String ENABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY = "enableDiagramForAllProcessDefinitions";

   private IPreferencesManager pm;

   private IPreferenceEditor preferencesEditor;

   private IPreferenceStore preferencesStore;

   private PropertyProvider()
   {
      boolean useRepository = Parameters.instance().getBoolean(
            org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false);
      pm = SessionContext.findSessionContext().getPreferencesManager();
      if (useRepository)
      {
         try
         {
            preferencesEditor = pm.getPreferencesEditor(PreferenceScope.PARTITION, MODULE_ID,
                  PREFERENCES_ID);
            save();
            preferencesStore = preferencesEditor;
         }
         catch (Exception e) 
         {
            useRepository = false;
            preferencesStore = pm.getPreferences(PreferenceScope.DEFAULT, MODULE_ID,
                  PREFERENCES_ID);
         }
      }
      else
      {
         preferencesStore = pm.getPreferences(PreferenceScope.DEFAULT, MODULE_ID,
               PREFERENCES_ID);
      }
   }

   public static PropertyProvider getInstance()
   {
      PropertyProvider instance = (PropertyProvider) SessionContext.findSessionContext()
            .lookup(PROPERTY_PROVIDER_INSTANCE);
      if (instance == null)
      {
         instance = new PropertyProvider();
         SessionContext.findSessionContext().bind(PROPERTY_PROVIDER_INSTANCE, instance);
      }
      return instance;
   }

   public String getTimeProviderClassName()
   {
      String value = preferencesStore.getString(GANTT_DIAGRAM_TIME_PROVIDER);
      value = StringUtils.isEmpty(value) ? Parameters.instance().getString(
            GANTT_DIAGRAM_TIME_PROVIDER, DefaultTimeProvider.class.getName()) : value;
      return value;
   }

   public List<String> getAllProcessDefinitionIDs()
   {
      List<String> result = new ArrayList<String>();

      String value = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + PROPERTY_PROCESS_DEFINITION_IDS);

      if (value != null)
      {
         StringTokenizer strTok = new StringTokenizer(value, ",");
         while (strTok.hasMoreElements())
         {
            String key = (String) strTok.nextElement();
            if (!result.contains(key))
               result.add(key);
         }
      }
      return result;
   }

   public String getProperty(String propertyId)
   {
      return preferencesStore.getString(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
   }

   public boolean getBooleanProperty(String propertyId)
   {
      return preferencesStore.getBoolean(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
   }

   public boolean getBooleanProperty(String processId, String propertyId)
   {
      boolean value = preferencesStore.getBoolean(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + processId
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
      return value;
   }

   public String getProperty(String processId, String propertyId)
   {
      String value = preferencesStore.getString(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + processId
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
      return value;
   }

   public boolean isConfigurationExistent(String processId)
   {
      return !(StringUtils.isEmpty(preferencesStore.getString(PROPERTY_PREFIX
            + PROPERTY_KEY_SEPARATOR + processId + PROPERTY_KEY_SEPARATOR
            + PLANNED_START_TIME_PROPERTY)) && StringUtils.isEmpty(preferencesStore
            .getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processId
                  + PROPERTY_KEY_SEPARATOR + PLANNED_TERMINATION_TIME_PROPERTY)));
   }

   public boolean isConfigurationExistent(String processId, String businessId)
   {
      if (businessId != null && !"".equals(businessId))
      {
         return preferencesStore.contains(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
               + processId + PROPERTY_KEY_SEPARATOR + businessId.replace(' ', '_') + "."
               + PLANNED_START_TIME_PROPERTY);
      }
      else
      {
         return isConfigurationExistent(processId);
      }
   }

   private boolean containsProperty(String propertyId)
   {
      return preferencesStore.contains(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
   }

   private boolean containsProperty(String processId, String propertyId)
   {
      return preferencesStore.contains(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + processId
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
   }

   public boolean hasConfigParam(String propertyId)
   {
      return containsProperty(propertyId)
            && !StringUtils.isEmpty(getProperty(propertyId));
   }

   public boolean hasConfigParam(String processId, String propertyId)
   {
      return containsProperty(processId, propertyId)
            && !StringUtils.isEmpty(getProperty(processId, propertyId));
   }

   public void setProperty(String processId, String propertyId, String value)
   {
      preferencesEditor.setValue(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + processId
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId, value);
   }

   public void setProperty(String propertyId, boolean value)
   {
      preferencesEditor.setValue(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId, value);
   }

   public void setProperty(String propertyId, String value)
   {
      preferencesEditor.setValue(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId, value);
   }

   public void setProperty(String processId, String propertyId, boolean value)
   {
      preferencesEditor.setValue(PropertyProvider.PROPERTY_PREFIX
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + processId
            + PropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId, value);
   }

   public void save()
   {
      preferencesEditor.save();
   }

}
