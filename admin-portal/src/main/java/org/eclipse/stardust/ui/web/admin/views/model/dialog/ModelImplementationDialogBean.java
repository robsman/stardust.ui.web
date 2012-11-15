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
package org.eclipse.stardust.ui.web.admin.views.model.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.ImplementationDescription;
import org.eclipse.stardust.engine.api.runtime.LinkingOptions;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.model.ModelManagementUserObject;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.table.RowDeselectionListener;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;


/**
 * 
 * @author Vikas.Mishra
 * 
 */
public class ModelImplementationDialogBean extends PopupUIComponentBean implements RowDeselectionListener
{
   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "modelImplementationDialogBean";
   private AdminMessagesPropertiesBean propsBean;
   private List<ImplementationTableEntry> result;

   private SortableTable<ImplementationTableEntry> implementationTable;
   private String comment;

   private String modelName;
   private String version;
   private int modelOID;
   private ModelManagementUserObject userObject;
   private String processName;

   public ModelImplementationDialogBean()
   {
      propsBean = AdminMessagesPropertiesBean.getInstance();
      createTable();

   }

   /**
    * @return
    */
   public static ModelImplementationDialogBean getCurrent()
   {
      ModelImplementationDialogBean bean = (ModelImplementationDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
      bean.userObject = null;

      return bean;
   }

   public String getComment()
   {
      return comment;
   }

   public SortableTable<ImplementationTableEntry> getImplementationTable()
   {
      return implementationTable;
   }

   @Override
   public void initialize()
   {
      implementationTable.setList(result);
      implementationTable.initialize();
   }

   private int getSelectedIndex(List<ImplementationTableEntry> list)
   {
      int size = list.size();
      for (int i = 0; i < size; i++)
      {
         if (list.get(i).isCheckSelection())
         {
            return i;
         }
      }
      return -1;
   }

   /**
 * 
 */
   public void save()
   {

      AdministrationService administrationService = SessionContext.findSessionContext().getServiceFactory()
            .getAdministrationService();

      LinkingOptions options = new LinkingOptions();
      options.setComment(comment);
      ImplementationTableEntry selectedRow = null;

      int index = getSelectedIndex(implementationTable.getList());
      String implementationId = null;
      if (index != -1)
      {
         selectedRow = implementationTable.getList().get(index);
         implementationId = selectedRow.getImplementationId();
      }

      try
      {
         // non of process is selected then it should reset to default implementation.
         // CRNT-20237
         // administrationService.setPrimaryImplementation(userObject.getParent().getOid(),
         // processName,
         // implementationModelID, options);
         administrationService.setPrimaryImplementation(userObject.getParent().getOid(), userObject.getTreeItem()
               .getProcessId(), implementationId, options);

         ModelCache.findModelCache().updateModel(userObject.getParent().getOid());
         closePopup();
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   public void setComment(String comment)
   {
      this.comment = comment;
   }

   public void setResult(List<ImplementationTableEntry> result)
   {
      this.result = result;
   }

   /**
    * method to create table definition for model file
    */
   private void createTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colModel = new ColumnPreference("ModelName", "modelName", ColumnDataType.STRING,
            propsBean.getString("views.modifyModelImplementationDialog.column.modelName"), true, true);

      ColumnPreference colVersion = new ColumnPreference("Version", "version", ColumnDataType.STRING,
            propsBean.getString("views.modifyModelImplementationDialog.column.version"), true, false);
      colVersion.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colModelOID = new ColumnPreference("ModelOID", "modelOID", ColumnDataType.STRING,
            propsBean.getString("views.modifyModelImplementationDialog.column.modelOID"), true, true);
      colModelOID.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colProcess = new ColumnPreference("Process", "process", ColumnDataType.STRING,
            propsBean.getString("views.modifyModelImplementationDialog.column.process"), true, false);
      colProcess.setColumnAlignment(ColumnAlignment.LEFT);

      cols.add(colVersion);
      cols.add(colModelOID);
      cols.add(colProcess);

      List<ColumnPreference> fixedBeforeColumns = new ArrayList<ColumnPreference>();
      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();
      fixedBeforeColumns.add(colModel);

      IColumnModel deployModelColumnModel = new DefaultColumnModel(cols, fixedBeforeColumns, fixedAfterColumns,
            UserPreferencesEntries.M_ADMIN, ResourcePaths.V_modelManagementView);

      implementationTable = new SortableTable<ImplementationTableEntry>(deployModelColumnModel, null,
            new SortableTableComparator<ImplementationTableEntry>("modelName", true));
      implementationTable.setRowSelector(new DataTableRowSelector("checkSelection"));
      implementationTable.initialize();
   }

   @Override
   public void openPopup()
   {
      comment = null;
      initialize();
      super.openPopup();

   }

   public String getModelName()
   {
      return modelName;
   }

   public void setModelName(String modelName)
   {
      this.modelName = modelName;
   }

   public String getVersion()
   {
      return version;
   }

   public void setVersion(String version)
   {
      this.version = version;
   }

   public int getModelOID()
   {
      return modelOID;
   }

   public void setModelOID(int modelOID)
   {
      this.modelOID = modelOID;
   }

   public ModelManagementUserObject getUserObject()
   {
      return userObject;
   }

   public void setUserObject(ModelManagementUserObject userObject)
   {
      this.userObject = userObject;
      if (userObject != null)
      {
         // comment = userObject.getComment();
         // modelName = userObject.getLabel();
         version = userObject.getVersion();
         modelOID = userObject.getOid();

         initData(userObject);
      }

   }

   public List<ImplementationTableEntry> initData(ModelManagementUserObject userObject)
   {
      processName = stripBracket(userObject.getLabel());
      DeployedModelDescription description = userObject.getParent().getModelDescription();
      modelName = I18nUtils.getLabel(description, description.getName()) + " - " + processName;
      Map<String, List<ImplementationDescription>> implementationProcesses = userObject.getParent()
            .getModelDescription().getImplementationProcesses();

      //List<ImplementationDescription> implementations = implementationProcesses.get(processName);
      List<ImplementationDescription> implementations = implementationProcesses.get(userObject.getTreeItem().getProcessId());

      List<ImplementationTableEntry> list = new ArrayList<ImplementationTableEntry>();

      if (implementations != null)
      {
         for (ImplementationDescription implementation : implementations)
         {
            if (implementation.isActive())
            {
               DeployedModel model = ModelCache.findModelCache().getModel(implementation.getImplementationModelOid());
               ImplementationTableEntry entry = new ImplementationTableEntry();
               entry.setModelName(I18nUtils.getLabel(model, model.getName()));
               ProcessDefinition processdefination = ProcessDefinitionUtils.getProcessDefinition(
                     implementation.getImplementationModelOid(), implementation.getImplementationProcessId());
               entry.setProcess(I18nUtils.getProcessName(processdefination));
               entry.setVersion(model.getVersion());
               entry.setImplementationId(processdefination.getQualifiedId());
               entry.setModelOID((int) implementation.getImplementationModelOid());
               entry.setCheckSelection(implementation.isPrimaryImplementation());
               entry.setRowDeselectionListener(this);
               list.add(entry);
            }
         }
      }

      result = list;

      return result;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.viewscommon.common.table.RowDeselectionListener#rowDeselected()
    */
   public void rowDeselected()
   {
      for (ImplementationTableEntry implementationTableEntry : implementationTable.getList())
      {
         implementationTableEntry.resetCheckSelection();
      }
   }
   /**
    * Util method to get String value between bracket
    * 
    * @param label
    * @return
    */

   private static String stripBracket(String label)
   {
      if (!StringUtils.isEmpty(label))
      {

         int first = label.lastIndexOf("(");
         // int last = label.lastIndexOf(")");
         if (first > -1)
         {
            return label.substring(0, first);
         }
      }
      return label;
   }

}
