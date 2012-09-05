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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.engine.api.runtime.ServiceFactory;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.PreferenceStoreUtils;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FileUtils;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.processportal.common.ResourcePaths;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.PreferencesResource;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.views.doctree.CommonFileUploadDialog.FileUploadDialogAttributes;

import com.icesoft.faces.component.inputfile.FileInfo;
import com.icesoft.faces.context.Resource;

/**
 * @author Yogesh.Manware
 * 
 */
public abstract class WorklistColumnConfigurationBean
{
   protected Resource fileResource;
   protected Map<String, Object> columnConfiguration;
   protected DataTable<WorklistConfigTableEntry> columnConfigurationTable;
   protected List<WorklistConfigTableEntry> columnConfTableEntries;
   protected Set<String> existingoIds = new HashSet<String>();
   private String preferenceId;

   public WorklistColumnConfigurationBean(String preferenceId)
   {
      this.preferenceId = preferenceId;
   }

   public Map<String, Object> defaultConf;

   public void initializeFileResource()
   {
      List<Preferences> preferencesList = new ArrayList<Preferences>();
      preferencesList.add(WorklistConfigurationUtil.getWorklistConfiguration(PreferenceScope.PARTITION, preferenceId));
      fileResource = new PreferencesResource(preferencesList);
   }

   public void importConfiguration()
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getInstance();
      fileUploadDialog.initializeBean();

      FileUploadDialogAttributes attributes = fileUploadDialog.getAttributes();
      attributes.setHeaderMessage("Upload configuration TODO");
      attributes.setTitle("Upload configuration TODO21");
      attributes.setViewDescription(false);
      attributes.setViewComment(false);
      attributes.setViewDocumentType(false);
      attributes.setEnableOpenDocument(false);
      attributes.setShowOpenDocument(false);
      fileUploadDialog.setCallbackHandler(new FileUploadCallbackHandler()
      {
         public void handleEvent(FileUploadEvent eventType)
         {
            if (eventType == FileUploadEvent.FILE_UPLOADED)
            {
               try
               {
                  loadConfiguration(getFileWrapper().getFileInfo());
               }
               catch (Exception e)
               {
               }
            }
         }
      });

      fileUploadDialog.openPopup();

   }

   /**
    * @param key
    * @return
    */
   protected String getMessage(String key)
   {
      return MessagePropertiesBean.getInstance().getString(key);
   }

   protected String getParamMessage(String key, String... params)
   {
      return MessagePropertiesBean.getInstance().getParamString(key, params);
   }

   private void loadConfiguration(FileInfo file)
   {
      String filePath = file.getPhysicalPath();
      InputStream inputStream = null;

      try
      {
         if (filePath.endsWith(FileUtils.ZIP_FILE))
         {
            inputStream = new FileInputStream(file.getFile());
            ServiceFactory serviceFactory = SessionContext.findSessionContext().getServiceFactory();
            PreferenceStoreUtils.loadFromZipFile(inputStream, serviceFactory);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
      finally
      {
         FileUtils.close(inputStream);
      }
      initialize();
   }

   /**
    * @return
    */
   public Resource getFileResource()
   {
      return fileResource;
   }

   protected void initialize()
   {
      ColumnPreference participantNameCol = new ColumnPreference("elementName", "elementName", MessagePropertiesBean
            .getInstance().getString("views.worklistPanelConfiguration.participant"),
            ResourcePaths.V_WLC_TABLE_COLUMNS, true, true);

      ColumnPreference actions = new ColumnPreference("actions", "actions",
            getMessage("views.worklistPanelConfiguration.actions"), ResourcePaths.V_WLC_TABLE_COLUMNS, true, false);

      List<ColumnPreference> participantWorkConfCols = new ArrayList<ColumnPreference>();
      participantWorkConfCols.add(participantNameCol);
      participantWorkConfCols.add(actions);

      IColumnModel participantsColumnModel = new DefaultColumnModel(participantWorkConfCols, null, null,
            UserPreferencesEntries.M_WORKFLOW, null);

      columnConfigurationTable = new SortableTable<WorklistConfigTableEntry>(participantsColumnModel, null,
            new SortableTableComparator<WorklistConfigTableEntry>("elementName", true));

      columnConfigurationTable.setRowSelector(new DataTableRowSelector("selected", true));
      columnConfigurationTable.initialize();
      columnConfiguration = WorklistConfigurationUtil.getWorklistConfigurationMap(PreferenceScope.PARTITION,
            preferenceId);
      retrieveandSetConfigurationValues();
      initializeFileResource();
   }

   /**
    * @param confTableEntry
    */
   public void fetchStoredValues(WorklistConfigTableEntry confTableEntry)
   {
      Map<String, Object> participantConf = WorklistConfigurationUtil.getStoredValues(confTableEntry.getElementOID(),
            columnConfiguration);

      if (null != participantConf)
      {
         confTableEntry.setConfiguration(participantConf);
         columnConfTableEntries.add(confTableEntry);
         existingoIds.add(confTableEntry.getElementOID());
      }
   }

   public void addEntry(WorklistConfigTableEntry confTableEntry)
   {
      existingoIds.add(confTableEntry.getElementOID());
      confTableEntry.setConfiguration(defaultConf);
      columnConfTableEntries.add(confTableEntry);
   }

   protected abstract void retrieveandSetConfigurationValues();

   public abstract void add();

   public void delete()
   {
      for (WorklistConfigTableEntry confTableEntry : columnConfTableEntries)
      {
         if (confTableEntry.isSelected() && !WorklistConfigurationUtil.DEFAULT.equals(confTableEntry.getElementOID()))
         {
            columnConfiguration.remove(confTableEntry.getElementOID());
            columnConfTableEntries.remove(confTableEntry);
            existingoIds.remove(confTableEntry.getElementOID());
         }
      }
   }

   public void save()
   {
      ArrayList<String> colsToBeSaved;
      for (WorklistConfigTableEntry confTableEntry : columnConfTableEntries)
      {
         colsToBeSaved = confTableEntry.getColumnsToBeSaved();
         WorklistConfigurationUtil.updateValues(confTableEntry.getElementOID(), colsToBeSaved, confTableEntry.isLock(),
               columnConfiguration);
      }
      WorklistConfigurationUtil.saveWorklistConfiguration(preferenceId, columnConfiguration);
   }

   public void reset()
   {
      initialize();
   }

   public boolean isDeleteDisabled()
   {
      boolean disabled = true;
      for (WorklistConfigTableEntry confTableEntry : columnConfTableEntries)
      {
         if (confTableEntry.isSelected())
         {
            disabled = false;
            if (WorklistConfigurationUtil.DEFAULT.equals(confTableEntry.getElementOID()))
            {
               disabled = true;
               break;
            }
         }
      }
      return disabled;
   }

   public DataTable<WorklistConfigTableEntry> getColumnConfigurationTable()
   {
      return columnConfigurationTable;
   }

}
