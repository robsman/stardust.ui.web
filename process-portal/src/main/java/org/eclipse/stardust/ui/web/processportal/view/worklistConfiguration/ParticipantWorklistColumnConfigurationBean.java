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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.engine.api.runtime.PerformerType;
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
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.IDepartmentProvider;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PreferencesResource;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.WorklistParticipantsProvider;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

/**
 * @author Yogesh.Manware
 * 
 */
public class ParticipantWorklistColumnConfigurationBean extends WorklistColumnConfigurationBean
{
   WorklistParticipantsProvider provider;

   @Override
   public void initialize()
   {
      ColumnPreference participantNameCol = new ColumnPreference("elementName", "elementName", MessagePropertiesBean
            .getInstance().getString("views.worklistPanelConfiguration.participant"),
            ResourcePaths.V_WLC_TABLE_COLUMNS, true, true);

      ColumnPreference actions = new ColumnPreference("actions", "actions", MessagePropertiesBean.getInstance()
            .getString("views.worklistPanelConfiguration.actions"), ResourcePaths.V_WLC_TABLE_COLUMNS, true, false);

      List<ColumnPreference> participantWorkConfCols = new ArrayList<ColumnPreference>();
      participantWorkConfCols.add(participantNameCol);
      participantWorkConfCols.add(actions);

      IColumnModel participantsColumnModel = new DefaultColumnModel(participantWorkConfCols, null, null,
            UserPreferencesEntries.M_WORKFLOW, UserPreferencesEntries.V_WORKLIST_PART_CONF);

      participantWorkConfTable = new SortableTable<WorklistConfigTableEntry>(participantsColumnModel, null,
            new SortableTableComparator<WorklistConfigTableEntry>("elementName", true));

      participantWorkConfTable.setRowSelector(new DataTableRowSelector("selected", true));
      participantWorkConfTable.initialize();
      worklistConfiguration = WorklistConfigurationUtil
            .getParticipantWorklistConfigurationMap(PreferenceScope.PARTITION);
      retrieveandSetConfigurationValues();
      initializeFileResource();
   }

   @Override
   public void add()
   {
      DelegationBean delegationBean = DelegationBean.getCurrent();
      delegationBean.setDelegateCase(false);
      delegationBean.setSelectedParticipantCase(true);
      WorklistParticipantsProvider provider = getProvider();
      delegationBean.setDelegatesProvider((IDelegatesProvider) provider);
      delegationBean.setDepartmentDelegatesProvider((IDepartmentProvider) provider);

      delegationBean.setICallbackHandler(new ParametricCallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            if (EventType.APPLY == eventType)
            {
               addParticipant(getParameters().get("selectedParticipant"));
            }
         }
      });
      delegationBean.openPopup();
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
      WorklistConfigurationUtil.saveParticipantWorklistConfiguration(worklistConfiguration);
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

   @Override
   public void initializeFileResource()
   {
      List<Preferences> preferencesList = new ArrayList<Preferences>();
      preferencesList.add(WorklistConfigurationUtil.getPartcipantWorklistConfiguration(PreferenceScope.PARTITION));
      fileResource = new PreferencesResource(preferencesList);
   }

   private void retrieveandSetConfigurationValues()
   {
      try
      {
         Map<PerformerType, List< ? extends ParticipantInfo>> delegates = getProvider().findDelegates(null,
               getDelegateProviderOptions());

         List< ? extends ParticipantInfo> modelParticipants = delegates.get(PerformerType.ModelParticipant);

         participantWorkConfTableEntries = new ArrayList<WorklistConfigTableEntry>();

         // set default configuration
         defaultConf = WorklistConfigurationUtil.getStoredValues(WorklistConfigurationUtil.DEFAULT,
               worklistConfiguration);
         WorklistConfigTableEntry defaultEntry = new WorklistConfigTableEntry(WorklistConfigurationUtil.DEFAULT);
         defaultEntry.setConfiguration(defaultConf);
         participantWorkConfTableEntries.add(defaultEntry);

         List<String> participantOIDs = new ArrayList<String>();

         for (ParticipantInfo participantInfo : modelParticipants)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(participantInfo);
            if (participantOIDs.contains(String.valueOf(confTableEntry.getElementOID())))
            {
               continue;
            }
            participantOIDs.add(String.valueOf(confTableEntry.getElementOID()));
            addifEligible(confTableEntry);
         }
         Set<DepartmentInfo> departments = getProvider().findDepartments(null, getDepartmentOptions()).get(
               "Departments");
         for (DepartmentInfo departmentInfo : departments)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(departmentInfo);
            addifEligible(confTableEntry);
         }

         // Set<ParticipantInfo> participants =
         // ParticipantWorklistCacheManager.getInstance().getWorklistParticipants();
         participantWorkConfTable.setList(participantWorkConfTableEntries);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   private WorklistParticipantsProvider getProvider()
   {
      if (null == provider)
      {
         provider = new WorklistParticipantsProvider();
      }
      return provider;
   }

   private void addParticipant(Object obj)
   {
      if (obj instanceof ParticipantInfo)
      {
         WorklistConfigTableEntry entry = new WorklistConfigTableEntry((ParticipantInfo) obj);
         entry.setConfiguration(defaultConf);
         participantWorkConfTableEntries.add(entry);
      }
      else if (obj instanceof DepartmentInfo)
      {
         WorklistConfigTableEntry entry = new WorklistConfigTableEntry((DepartmentInfo) obj);
         entry.setConfiguration(defaultConf);
         participantWorkConfTableEntries.add(entry);
      }
   }

   /**
    * @param confTableEntry
    */
   private void addifEligible(WorklistConfigTableEntry confTableEntry)
   {
      Map<String, Object> participantConf = WorklistConfigurationUtil.getStoredValues(confTableEntry.getElementOID(),
            worklistConfiguration);

      if (null != participantConf)
      {
         confTableEntry.setConfiguration(participantConf);
         participantWorkConfTableEntries.add(confTableEntry);
      }
   }

   private IDepartmentProvider.Options getDepartmentOptions()
   {
      return new IDepartmentProvider.Options()
      {

         public String getNameFilter()
         {
            return "";
         }

         public boolean isStrictSearch()
         {
            return false;
         }
      };
   }

   private Options getDelegateProviderOptions()
   {
      return new IDelegatesProvider.Options()
      {
         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #getPerformerTypes()
          */
         public Set<Integer> getPerformerTypes()
         {
            Set<Integer> result = CollectionUtils.newSet();
            result.add(IDelegatesProvider.ROLE_TYPE);
            result.add(IDelegatesProvider.ORGANIZATION_TYPE);
            result.add(IDelegatesProvider.DEPARTMENT_TYPE);
            return Collections.unmodifiableSet(result);
         }

         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #isStrictSearch()
          */
         public boolean isStrictSearch()
         {
            return true;
         }

         /*
          * (non-Javadoc)
          * 
          * @see
          * org.eclipse.stardust.ui.web.viewscommon.dialogs.IDelegatesProvider.Options
          * #getNameFilter()
          */
         public String getNameFilter()
         {
            return "";
         }
      };
   }

   public DataTable<WorklistConfigTableEntry> getParticipantWorkConfTable()
   {
      return participantWorkConfTable;
   }
}