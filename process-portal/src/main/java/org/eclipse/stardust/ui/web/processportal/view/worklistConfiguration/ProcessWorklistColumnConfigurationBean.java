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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.ResourcePaths;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PreferencesResource;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;

/**
 * @author Yogesh.Manware
 * 
 */
public class ProcessWorklistColumnConfigurationBean extends WorklistColumnConfigurationBean
{
   @Override
   public void initialize()
   {
      ColumnPreference processNameCol = new ColumnPreference("elementName", "elementName", MessagePropertiesBean
            .getInstance().getString("views.worklistPanelConfiguration.process"), ResourcePaths.V_WLC_TABLE_COLUMNS,
            true, true);

      ColumnPreference processActions = new ColumnPreference("actions", "actions", MessagePropertiesBean.getInstance()
            .getString("views.worklistPanelConfiguration.actions"), ResourcePaths.V_WLC_TABLE_COLUMNS, true, false);

      List<ColumnPreference> processWorkConfCols = new ArrayList<ColumnPreference>();
      processWorkConfCols.add(processNameCol);
      processWorkConfCols.add(processActions);

      IColumnModel processColumnModel = new DefaultColumnModel(processWorkConfCols, null, null,
            UserPreferencesEntries.M_WORKFLOW, UserPreferencesEntries.V_WORKLIST_PROC_CONF);

      participantWorkConfTable = new SortableTable<WorklistConfigTableEntry>(processColumnModel, null,
            new SortableTableComparator<WorklistConfigTableEntry>("elementName", true));

      participantWorkConfTable.setRowSelector(new DataTableRowSelector("selected", true));
      participantWorkConfTable.initialize();
      worklistConfiguration = WorklistConfigurationUtil.getProcessWorklistConfigurationMap(PreferenceScope.PARTITION);
      retrieveandSetConfigurationValues();
      initializeFileResource();
   }

   @Override
   public void add()
   {
      SelectProcessPopup dialog = SelectProcessPopup.getInstance();
      dialog.setCallbackHandler(new ParametricCallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (EventType.APPLY == eventType)
            {
               addProcess((List<ProcessDefinition>) getParameters().get("processes"));
            }
         }
      });
      dialog.initializeBean();
      dialog.openPopup();
   }

   @Override
   public void save()
   {
      ArrayList<String> colsToBeSaved;
      for (WorklistConfigTableEntry confTableEntry : participantWorkConfTableEntries)
      {
         colsToBeSaved = confTableEntry.getColumnsToBeSaved();
         WorklistConfigurationUtil.updateValues(confTableEntry.getElementOID(), colsToBeSaved, confTableEntry.isLock(),
               worklistConfiguration);
      }

      WorklistConfigurationUtil.saveProcessWorklistConfiguration(worklistConfiguration);
   }

   @Override
   public void delete()
   {
      for (WorklistConfigTableEntry confTableEntry : participantWorkConfTableEntries)
      {
         if (confTableEntry.isSelected() && !WorklistConfigurationUtil.DEFAULT.equals(confTableEntry.getElementOID()))
         {
            WorklistConfigurationUtil.deleteValues(confTableEntry.getElementOID(), worklistConfiguration);
            participantWorkConfTableEntries.remove(confTableEntry);
         }
      }
   }

   public void reset()
   {
      initialize();
   }

   public void initializeFileResource()
   {
      List<Preferences> preferencesList = new ArrayList<Preferences>();
      preferencesList.add(WorklistConfigurationUtil.getProcessWorklistConfiguration(PreferenceScope.PARTITION));
      fileResource = new PreferencesResource(preferencesList);
   }

   private void addProcess(List<ProcessDefinition> processes)
   {
      for (ProcessDefinition processDefinition : processes)
      {
         WorklistConfigTableEntry entry = new WorklistConfigTableEntry(processDefinition);
         entry.setConfiguration(defaultConf);
         participantWorkConfTableEntries.add(entry);
      }
   }

   private void retrieveandSetConfigurationValues()
   {
      try
      {
         ProcessWorklistCacheManager.getInstance().reset();
         Set<ProcessDefinition> processDefs = ProcessWorklistCacheManager.getInstance().getProcesses();

         participantWorkConfTableEntries = new ArrayList<WorklistConfigTableEntry>();

         // set default entry
         defaultConf = WorklistConfigurationUtil.getStoredValues(WorklistConfigurationUtil.DEFAULT,
               worklistConfiguration);
         WorklistConfigTableEntry defaultEntry = new WorklistConfigTableEntry(WorklistConfigurationUtil.DEFAULT);
         defaultEntry.setConfiguration(defaultConf);
         participantWorkConfTableEntries.add(defaultEntry);

         List<String> processOIDs = new ArrayList<String>();

         for (ProcessDefinition processDefinition : processDefs)
         {
            if (processOIDs.contains(String.valueOf(processDefinition.getElementOID())))
            {
               continue;
            }
            processOIDs.add(String.valueOf(processDefinition.getElementOID()));
            addifEligible(processDefinition);
         }
         participantWorkConfTable.setList(participantWorkConfTableEntries);

      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param confTableEntry
    */
   private void addifEligible(ProcessDefinition processDef)
   {
      Map<String, Object> processConf = WorklistConfigurationUtil.getStoredValues(
            String.valueOf(processDef.getElementOID()), worklistConfiguration);

      if (null != processConf)
      {
         WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(processDef);
         confTableEntry.setConfiguration(processConf);
         participantWorkConfTableEntries.add(confTableEntry);
      }
   }

   public DataTable<WorklistConfigTableEntry> getProcessWorkConfTable()
   {
      return participantWorkConfTable;
   }

}
