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
   private static final String COMMON_PROPERTY_KEY = "views.worklistPanelConfiguration.";
   private static final String PROPERTY_KEY_TITLE = "title";
   private static final String PROPERTY_KEY_HEADERMSG = "headerMsg";
   private static final String PROPERTY_KEY_NAME = "name";
   private static final String PROPERTY_KEY_ACTIONS = "actions";
   private static final String PROPERTY_KEY_FILE_NAME = "fileName";

   private static final String COLUMN_NAME = "elementName";
   private static final String COLUMN_ACTIONS = "actions";

   protected Map<String, Object> columnConfiguration;
   protected DataTable<WorklistConfigTableEntry> columnConfigurationTable;
   protected List<WorklistConfigTableEntry> columnConfTableEntries;
   protected Set<String> existingConfigurations;
   protected Map<String, Object> defaultConf;
   protected Resource fileResource;
   private String preferenceId;

   /**
    * @param preferenceId
    */
   public WorklistColumnConfigurationBean(String preferenceId)
   {
      this.preferenceId = preferenceId;
   }

   public abstract void add();

   /**
    * soft delete the entry, actually gets deleted when the configuration is saved
    */
   public void delete()
   {
      for (WorklistConfigTableEntry confTableEntry : columnConfTableEntries)
      {
         if (confTableEntry.isSelected() && !WorklistConfigurationUtil.DEFAULT.equals(confTableEntry.getIdentityKey()))
         {
            columnConfiguration.remove(confTableEntry.getIdentityKey());
            columnConfTableEntries.remove(confTableEntry);
            existingConfigurations.remove(confTableEntry.getIdentityKey());
         }
      }
   }

   /**
    * save configuration
    */
   public void save()
   {
      ArrayList<String> colsToBeSaved;
      for (WorklistConfigTableEntry confTableEntry : columnConfTableEntries)
      {
         colsToBeSaved = confTableEntry.getColumnsToBeSaved();
         WorklistConfigurationUtil.updateValues(confTableEntry.getIdentityKey(), colsToBeSaved,
               confTableEntry.isLock(), columnConfiguration);
      }
      WorklistConfigurationUtil.savePreferences(PreferenceScope.PARTITION, preferenceId, columnConfiguration);
   }

   public void reset()
   {
      initializeStoredValues();
      // WorklistConfigurationUtil.resetPreference();
   }

   /**
    * import and load uploaded configuration
    */
   public void importConfiguration()
   {
      CommonFileUploadDialog fileUploadDialog = CommonFileUploadDialog.getInstance();
      fileUploadDialog.initializeBean();

      FileUploadDialogAttributes attributes = fileUploadDialog.getAttributes();
      attributes.setTitle(getMessage(getPropertyKey() + PROPERTY_KEY_TITLE));
      attributes.setHeaderMessage(getMessage(getPropertyKey() + PROPERTY_KEY_HEADERMSG));
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

   protected void initialize()
   {
      ColumnPreference nameCol = new ColumnPreference(COLUMN_NAME, COLUMN_NAME, getMessage(getPropertyKey()
            + PROPERTY_KEY_NAME), ResourcePaths.V_WLC_TABLE_COLUMNS, true, true);

      ColumnPreference actions = new ColumnPreference(COLUMN_ACTIONS, COLUMN_ACTIONS, getMessage(PROPERTY_KEY_ACTIONS),
            ResourcePaths.V_WLC_TABLE_COLUMNS, true, false);

      List<ColumnPreference> workConfCols = new ArrayList<ColumnPreference>();
      workConfCols.add(nameCol);
      workConfCols.add(actions);

      IColumnModel worklistColumnModel = new DefaultColumnModel(workConfCols, null, null,
            UserPreferencesEntries.M_WORKFLOW, null);

      columnConfigurationTable = new SortableTable<WorklistConfigTableEntry>(worklistColumnModel, null,
            new SortableTableComparator<WorklistConfigTableEntry>(COLUMN_NAME, true));

      columnConfigurationTable.setRowSelector(new DataTableRowSelector("selected", true));
      columnConfigurationTable.initialize();

      initializeStoredValues();
   }

   /**
    * 
    */
   protected void initializeStoredValues()
   {
      columnConfTableEntries = new ArrayList<WorklistConfigTableEntry>();
      existingConfigurations = new HashSet<String>();
      columnConfiguration = WorklistConfigurationUtil.getWorklistConfigurationMap(PreferenceScope.PARTITION,
            preferenceId);

      // set default entry
      defaultConf = WorklistConfigurationUtil.getStoredValues(WorklistConfigurationUtil.DEFAULT, columnConfiguration);
      WorklistConfigTableEntry defaultEntry = new WorklistConfigTableEntry(WorklistConfigurationUtil.DEFAULT);
      defaultEntry.setConfiguration(defaultConf);
      columnConfTableEntries.add(defaultEntry);

      retrieveConfigurations();
      initializeFileResource();
      columnConfigurationTable.setList(columnConfTableEntries);
   }

   /**
    * @param confTableEntry
    */
   protected void addEntry(WorklistConfigTableEntry confTableEntry)
   {
      existingConfigurations.add(confTableEntry.getIdentityKey());
      confTableEntry.setConfiguration(defaultConf);
      columnConfTableEntries.add(confTableEntry);
   }

   /**
    * @param confTableEntry
    */
   protected void fetchStoredValues(WorklistConfigTableEntry confTableEntry)
   {
      Map<String, Object> configuration = WorklistConfigurationUtil.getStoredValues(confTableEntry.getIdentityKey(),
            columnConfiguration);

      if (null != configuration)
      {
         confTableEntry.setConfiguration(configuration);
         columnConfTableEntries.add(confTableEntry);
         existingConfigurations.add(confTableEntry.getIdentityKey());
      }
   }

   public String getFileName()
   {
      return getMessage(getPropertyKey() + PROPERTY_KEY_FILE_NAME);
   }

   protected abstract void retrieveConfigurations();

   protected abstract String getPropertyKey();

   /**
    * @param file
    */
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
    * prepare download file resource
    */
   private void initializeFileResource()
   {
      List<Preferences> preferencesList = new ArrayList<Preferences>();
      preferencesList.add(WorklistConfigurationUtil.getWorklistConfiguration(PreferenceScope.PARTITION, preferenceId));
      fileResource = new PreferencesResource(preferencesList);
   }

   public boolean isDeleteDisabled()
   {
      boolean disabled = true;
      for (WorklistConfigTableEntry confTableEntry : columnConfTableEntries)
      {
         if (confTableEntry.isSelected())
         {
            disabled = false;
            if (WorklistConfigurationUtil.DEFAULT.equals(confTableEntry.getIdentityKey()))
            {
               disabled = true;
               break;
            }
         }
      }
      return disabled;
   }

   /**
    * @return
    */
   public Resource getFileResource()
   {
      return fileResource;
   }

   public DataTable<WorklistConfigTableEntry> getColumnConfigurationTable()
   {
      return columnConfigurationTable;
   }

   /**
    * @param key
    * @return
    */
   protected String getMessage(String key)
   {
      return MessagePropertiesBean.getInstance().getString(COMMON_PROPERTY_KEY + key);
   }

   /**
    * @param key
    * @param params
    * @return
    */
   protected String getParamMessage(String key, String... params)
   {
      return MessagePropertiesBean.getInstance().getParamString(COMMON_PROPERTY_KEY + key, params);
   }
}
