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
package org.eclipse.stardust.ui.web.bcc.legacy.traffic;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceEditor;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceStore;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferencesManager;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



/**
 * The class reads properties from a configuration file and provides access methods to
 * well known configuration properties.
 * 
 * @author mueller1
 * 
 */
public class TrafficLightViewPropertyProvider
{

   private static final String PROPERTY_PROVIDER_INSTANCE = "trafficLightViewPropertyProvider/instance";

   private static final String FILTERED_COLUMN_ACTIVITY_IDS = "FilteredColumnActivityIds";

   private static final String FILTERED_CATEGORY = "FilteredCategory";

   public static final String PREFERENCES_ID = "traffic-light-view";

   public static final String MODULE_ID = "bcc";

   public static final String PROPERTY_PREFIX = "Infinity.Monitoring.TrafficLightView";

   public static final String PROPERTY_PROCESS_DEFINITION_IDS = "ProcessDefinitionIds";

   public static final String PROPERTY_COLUMN_ACTIVITY_IDS = "ColumnActivityIds";

   public static final String PROPERTY_PROCESS_ROW_DATA_ID = "RowDataId";

   public static final String PROPERTY_PROCESS_ROW_DATA_VALUES = "RowDataValues";

   public static final String PROPERTY_PROCESS_TOTAL_ROW = "DisplayTotalRow";

   public static final String PROPERTY_ACTIVITY_PROCESSING_THRESHOLD = "ProcessingThreshold";

   public static final String PROPERTY_KEY_SEPARATOR = ".";

   public static final String PROPERTY_VALUE_SEPARATOR = ",";

   public static final String PROPERTY_VALUE_STATE_CALCULATOR = "StateCalculator";

   public static final String PROPERTY_VALUE_DESCRIPTOR_FILTER = "DescriptorFilter";
   
   public static final String PRESELECTED_PROCESSES = "preSelectedProcesses";

   private IPreferencesManager pm;

   private transient IPreferenceEditor preferencesEditor;

   private IPreferenceStore preferencesStore;

   private IPreferenceEditor userPreferencesEditor;

   private IPreferenceStore userPreferencesStore;

   private boolean useRepository;

   private TrafficLightViewPropertyProvider()
   {
      useRepository = Parameters.instance().getBoolean(
            org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false);
      pm = SessionContext.findSessionContext().getPreferencesManager();
      if (useRepository)
      {
         try
         {
            preferencesEditor = pm.getPreferencesEditor(PreferenceScope.PARTITION, MODULE_ID,
                  PREFERENCES_ID);
            userPreferencesEditor = pm.getPreferencesEditor(PreferenceScope.USER, MODULE_ID,
                  PREFERENCES_ID);
            save();
            preferencesStore = preferencesEditor;
            userPreferencesStore = userPreferencesEditor;
         }
         catch (Exception e) 
         {
            useRepository = false;
            preferencesStore = pm.getPreferences(PreferenceScope.DEFAULT, MODULE_ID,
                  PREFERENCES_ID);
         }
         userPreferencesStore = userPreferencesEditor;
      }
      else
      {
         preferencesStore = pm.getPreferences(PreferenceScope.DEFAULT, MODULE_ID,
               PREFERENCES_ID);
      }
   }

   public static TrafficLightViewPropertyProvider getInstance()
   {
      TrafficLightViewPropertyProvider instance = (TrafficLightViewPropertyProvider) SessionContext
            .findSessionContext().lookup(PROPERTY_PROVIDER_INSTANCE);
      if (instance == null)
      {
         instance = new TrafficLightViewPropertyProvider();
         SessionContext.findSessionContext().bind(
               PROPERTY_PROVIDER_INSTANCE, instance);
      }
      return instance;
   }

   public Map<String, String> getAllProcessingThresholds()
   {
      Map<String, String> result = CollectionUtils.newHashMap();

      List<String> processDefinitionsFQIDs = getAllProcessDefinitionIDs();

      for ( String processFQId:processDefinitionsFQIDs)
      {         
         List<String> activities = getAllColumnIDs(processFQId);

         for (int i = 0; i < activities.size(); i++)
         {
            String activityId = activities.get(i);
            String value = preferencesStore.getString(PROPERTY_PREFIX
                  + PROPERTY_KEY_SEPARATOR + processFQId + PROPERTY_KEY_SEPARATOR
                  + PROPERTY_ACTIVITY_PROCESSING_THRESHOLD + PROPERTY_KEY_SEPARATOR
                  + activityId);

            if (value != null)
            {
               result.put(processFQId + "." + activityId, value);
            }
         }
      }

      return result;
   }

   public List<String> getAllProcessDefinitionIDs()
   {
      List<String> result = CollectionUtils.newArrayList();

      String value = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + PROPERTY_PROCESS_DEFINITION_IDS);

      if (value != null)
      {
         StringTokenizer strTok = new StringTokenizer(value, PROPERTY_VALUE_SEPARATOR);

         while (strTok.hasMoreElements())
         {
            String key = (String) strTok.nextElement();
            if (!result.contains(key))
               result.add(key);
         }
      }
      return result;
   }

   public List<String> getAllPreSelectedProcesses()
   {
      List<String> processesQIDs = CollectionUtils.newArrayList();

      String value = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + PRESELECTED_PROCESSES);

      if (value != null)
      {
         StringTokenizer strTok = new StringTokenizer(value, PROPERTY_VALUE_SEPARATOR);

         while (strTok.hasMoreElements())
         {
            String key = (String) strTok.nextElement();
            if (!processesQIDs.contains(key))
               processesQIDs.add(key);
         }
      }
      return processesQIDs;
   }
   
   public void setAllPreSelectedProcesses(List<ProcessDefinition> processDefinitions)
   {
      StringBuffer ids = new StringBuffer();
      for (Iterator<ProcessDefinition> iterator = processDefinitions.iterator(); iterator.hasNext();)
      {
         ProcessDefinition pd = iterator.next();
         ids.append(pd.getQualifiedId());
         if (iterator.hasNext())
         {
            ids.append(PROPERTY_VALUE_SEPARATOR);
         }
      }
      preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + PRESELECTED_PROCESSES, ids.toString());
   }
   
   
   public List<String> getAllColumnIDs(String processFQId)
   {
      List<String> result = CollectionUtils.newArrayList();

      String pKey = PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
            + PROPERTY_KEY_SEPARATOR + PROPERTY_COLUMN_ACTIVITY_IDS;
      String value = preferencesStore.getString(pKey);

      if (value != null)
      {
         StringTokenizer strTok = new StringTokenizer(value, PROPERTY_VALUE_SEPARATOR);
         while (strTok.hasMoreElements())
         {
            String key = (String) strTok.nextElement();
            if (!result.contains(key))
               result.add(key);
         }
      }
      return result;
   }

   public List<String> getAllRowIDsAsList(String processFQId, String dataFilter)
   {
      List<String> result = CollectionUtils.newArrayList();

      String value = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + processFQId + PROPERTY_KEY_SEPARATOR + dataFilter + PROPERTY_KEY_SEPARATOR
            + PROPERTY_PROCESS_ROW_DATA_VALUES);

      if (value != null)
      {
         StringTokenizer strTok = new StringTokenizer(value, PROPERTY_VALUE_SEPARATOR);

         while (strTok.hasMoreElements())
         {
            String key = (String) strTok.nextElement();
            key = key.trim();
            if (!result.contains(key))
               result.add(key);
         }
      }

      return result;
   }

   public String getAllRowDataValues(String processFQId, String dataFilter)
   {
      String value = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + processFQId + PROPERTY_KEY_SEPARATOR + dataFilter + PROPERTY_KEY_SEPARATOR
            + PROPERTY_PROCESS_ROW_DATA_VALUES);

      if (StringUtils.isEmpty(value))
      {
         value = null;
      }

      return value;
   }

   public String getRowId(String processFQId)
   {
      String result = null;

      result = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + processFQId + PROPERTY_KEY_SEPARATOR + PROPERTY_PROCESS_ROW_DATA_ID);

      return result;
   }

   public boolean withTotalRow(String processFQId)
   {
      boolean result = true;

      String name = PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
            + PROPERTY_KEY_SEPARATOR + PROPERTY_PROCESS_TOTAL_ROW;
      if (preferencesStore.contains(name))
      {
         result = preferencesStore.getBoolean(name);
      }

      return result;
   }

   public String getStateCalculatorClassName(String processFQId)
   {
      String value = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + processFQId + PROPERTY_KEY_SEPARATOR + PROPERTY_VALUE_STATE_CALCULATOR);

      return value;
   }

   public String getDescriptorFilterName(String processFQId)
   {
      String value = preferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + processFQId + PROPERTY_KEY_SEPARATOR + PROPERTY_VALUE_DESCRIPTOR_FILTER);

      return value;
   }

   public void setAllProcessDefinitionIds(List<ProcessDefinition> processDefinitions)
   {
      StringBuffer ids = new StringBuffer();
      for (Iterator<ProcessDefinition> iterator = processDefinitions.iterator(); iterator.hasNext();)
      {
         ProcessDefinition pd = iterator.next();
         ids.append(pd.getQualifiedId());
         if (iterator.hasNext())
         {
            ids.append(PROPERTY_VALUE_SEPARATOR);
         }
      }
      preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
            + PROPERTY_PROCESS_DEFINITION_IDS, ids.toString());
   }

   public void setStateCalculator(String processFQId, String stateCalculator)
   {
      preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
            + PROPERTY_KEY_SEPARATOR + PROPERTY_VALUE_STATE_CALCULATOR, stateCalculator);
   }

   public void setDescriptorFilterName(String processFQId, String descriptorFilter)
   {
      preferencesEditor
            .setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
                  + PROPERTY_KEY_SEPARATOR + PROPERTY_VALUE_DESCRIPTOR_FILTER,
                  descriptorFilter);
   }

   public void setDisplayTotalRow(String processFQId, boolean displayTotalRow)
   {
      preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
            + PROPERTY_KEY_SEPARATOR + PROPERTY_PROCESS_TOTAL_ROW, displayTotalRow);
   }

   public void setProcessingThreshold(String processFQId, String activityId,
         String threshold)
   {
      preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
            + PROPERTY_KEY_SEPARATOR + PROPERTY_ACTIVITY_PROCESSING_THRESHOLD
            + PROPERTY_KEY_SEPARATOR + activityId, threshold);
   }

   public void setAllColumnIds(String processFQId, String columnIds)
   {
      preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
            + PROPERTY_KEY_SEPARATOR + PROPERTY_COLUMN_ACTIVITY_IDS, columnIds);
   }

   public void setRowDataValues(String processFQId, String dataFilter, String rowDataValues)
   {

      if (StringUtils.isNotEmpty(rowDataValues))
      {

         preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId + PROPERTY_KEY_SEPARATOR
               + dataFilter + PROPERTY_KEY_SEPARATOR + PROPERTY_PROCESS_ROW_DATA_VALUES, rowDataValues);
      }
      else if (rowDataValues!=null && "".equals(rowDataValues.trim()))
      {
         String rowData = userPreferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
               + PROPERTY_KEY_SEPARATOR + dataFilter + PROPERTY_KEY_SEPARATOR + PROPERTY_PROCESS_ROW_DATA_VALUES);
         
         if (StringUtils.isNotEmpty(rowData))
         {
            preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId + PROPERTY_KEY_SEPARATOR
                  + dataFilter + PROPERTY_KEY_SEPARATOR + PROPERTY_PROCESS_ROW_DATA_VALUES, rowDataValues);
         }
      }
   }

   public void setRowDataIds(String processFQId, String rowDataIds)
   {

      preferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR + processFQId
            + PROPERTY_KEY_SEPARATOR + PROPERTY_PROCESS_ROW_DATA_ID, rowDataIds);

   }

   public void save()
   {
      preferencesEditor.save();
   }

   public String getSelectedCategoryFilter(String processFQId)
   {
      String filter = null;
      if (useRepository)
      {
         filter = userPreferencesStore.getString(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
               + processFQId + PROPERTY_KEY_SEPARATOR + FILTERED_CATEGORY);
      }
      return filter;
   }

   public void setSelectedCategoryFilter(String processFQId, String filter)
   {
      if (useRepository)
      {
         userPreferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
               + processFQId + PROPERTY_KEY_SEPARATOR + FILTERED_CATEGORY, filter);
         userPreferencesEditor.save();
      }
   }

   public List<String> getSelectedColumns(String processFQId)
   {
      List<String> selectedColumns = CollectionUtils.newArrayList();
      if (useRepository)
      {
         String value = userPreferencesStore.getString(PROPERTY_PREFIX
               + PROPERTY_KEY_SEPARATOR + processFQId + PROPERTY_KEY_SEPARATOR
               + FILTERED_COLUMN_ACTIVITY_IDS);
         if (value != null)
         {
            StringTokenizer tokenizer = new StringTokenizer(value,
                  PROPERTY_VALUE_SEPARATOR);
            while (tokenizer.hasMoreElements())
            {
               String id = (String) tokenizer.nextElement();
               selectedColumns.add(id);
            }
         }
      }
      return selectedColumns;
   }

   public void setSelectedColumns(String processFQId, List<String> activityIds)
   {
      if (useRepository)
      {
         StringBuilder builder = new StringBuilder();
         for ( String activityId :activityIds)
         {           
            builder.append(activityId + PROPERTY_VALUE_SEPARATOR);
         }
         userPreferencesEditor.setValue(PROPERTY_PREFIX + PROPERTY_KEY_SEPARATOR
               + processFQId + PROPERTY_KEY_SEPARATOR + FILTERED_COLUMN_ACTIVITY_IDS,
               builder.toString());
         userPreferencesEditor.save();
      }
   }
}
