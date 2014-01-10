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
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceEditor;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferenceStore;
import org.eclipse.stardust.engine.core.preferences.manager.IPreferencesManager;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;



public class GanttChartBtnPropertyProvider
{
   private static final String PROPERTY_PROVIDER_INSTANCE = "ganttChartBtnCfgPropertyProvider/instance";

   public static final String PREFERENCES_ID = "gantt-chart";

   public static final String MODULE_ID = "bcc";

   public static final String PROPERTY_PREFIX = "Infinity.Monitoring.GanttChartView";

   public static final String PROPERTY_KEY_SEPARATOR = ".";

   public static final String ENABLE_DIAGRAM_PROPERTY = "enableDiagram";

   public static final String DISABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY = "disableDiagramForAllProcessDefinitions";

   public static final String ENABLE_DIAGRAM_FOR_ALL_PROCESSES_PROPERTY = "enableDiagramForAllProcessDefinitions";

   public static final String PROPERTY_PROCESS_DEFINITION_IDS = "ProcessDefinitionIds";

   public static final String ENABLED_PROCESSES = "GanttDiagramSelectionBean/processes";

   protected IPreferencesManager pm;

   protected transient IPreferenceEditor preferencesEditor;

   protected IPreferenceStore preferencesStore;

   protected GanttChartBtnPropertyProvider()
   {
      boolean useRepository = Parameters.instance().getBoolean(
            org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false);
      pm = SessionContext.findSessionContext().getPreferencesManager();
      if (useRepository)
      {
         try
         {
            preferencesEditor = pm.getPreferencesEditor(PreferenceScope.PARTITION,
                  MODULE_ID, PREFERENCES_ID);
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

   public static GanttChartBtnPropertyProvider getInstance()
   {
      GanttChartBtnPropertyProvider instance = (GanttChartBtnPropertyProvider) SessionContext
            .findSessionContext().lookup(PROPERTY_PROVIDER_INSTANCE);
      if (instance == null)
      {
         instance = new GanttChartBtnPropertyProvider();
         SessionContext.findSessionContext().bind(PROPERTY_PROVIDER_INSTANCE, instance);
      }
      return instance;
   }

   public List/* <String> */getAllProcessDefinitionIDs()
   {
      List/* <String> */result = new ArrayList/* <String> */();

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

   public boolean getBooleanProperty(String propertyId)
   {
      return preferencesStore.getBoolean(GanttChartBtnPropertyProvider.PROPERTY_PREFIX
            + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
   }

   public boolean getBooleanProperty(String processId, String propertyId)
   {
      boolean value = preferencesStore
            .getBoolean(GanttChartBtnPropertyProvider.PROPERTY_PREFIX
                  + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + processId
                  + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
      return value;
   }

   public String getProperty(String processId, String propertyId)
   {
      String value = preferencesStore
            .getString(GanttChartBtnPropertyProvider.PROPERTY_PREFIX
                  + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + processId
                  + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
      return value;
   }

   private boolean containsProperty(String processId, String propertyId)
   {
      return preferencesStore.contains(GanttChartBtnPropertyProvider.PROPERTY_PREFIX
            + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + processId
            + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId);
   }

   public boolean hasConfigParam(String processId, String propertyId)
   {
      return containsProperty(processId, propertyId)
            && !StringUtils.isEmpty(getProperty(processId, propertyId));
   }

   public void setProperty(String propertyId, String value)
   {
      preferencesEditor.setValue(GanttChartBtnPropertyProvider.PROPERTY_PREFIX
            + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId, value);
   }

   public void setProperty(String processId, String propertyId, boolean value)
   {
      preferencesEditor.setValue(GanttChartBtnPropertyProvider.PROPERTY_PREFIX
            + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + processId
            + GanttChartBtnPropertyProvider.PROPERTY_KEY_SEPARATOR + propertyId, value);
   }

   public void save()
   {
      preferencesEditor.save();
   }

}
