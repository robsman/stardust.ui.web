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
package org.eclipse.stardust.ui.web.admin.views.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.DeployedModelQuery;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.api.runtime.Daemon;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.DeployedModelDescription;
import org.eclipse.stardust.engine.api.runtime.DeploymentInfo;
import org.eclipse.stardust.engine.api.runtime.ImplementationDescription;
import org.eclipse.stardust.engine.api.runtime.Models;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.TreeNodeFactory;
import org.eclipse.stardust.ui.web.admin.views.model.dialog.ModelDeploymentDialogBean;
import org.eclipse.stardust.ui.web.admin.views.model.dialog.ModelExportDialogBean;
import org.eclipse.stardust.ui.web.admin.views.model.dialog.ModelImplementationDialogBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogActionType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogContentType;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialog.DialogStyle;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.message.MessageDialog.MessageType;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.DocumentViewUtil;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.XPathCacheManager;



/**
 * managed bean class for Model Management View
 * 
 * @author Vikas.Mishra
 * 
 */
public class ModelManagementBean extends UIComponentBean implements ViewEventHandler, TreeTableBean, ConfirmationDialogHandler
{
   private static final long serialVersionUID = 1L;

   private List<FilterToolbarItem> filterToolbarItems;
   private TableColumnSelectorPopup colSelectorPopup;
   private TableDataFilters tableDataFilters;
   private TreeTable modelTreeTable;
   private TreeTableNode rootModelNode;
   private boolean valueChanged = false;
   private AdminMessagesPropertiesBean adminMessagesBean;
   private ModelManagementUserObject selectedRow;
   private ConfirmationDialog modelMgmtConfirmationDialog;

   /**
    * Default Constructor
    */
   public ModelManagementBean()
   {
      super(ResourcePaths.V_modelManagementView);
      adminMessagesBean = AdminMessagesPropertiesBean.getInstance();
   }

   /**
    * method to build tree table
    * 
    * @see ModelManagementTreeItem,TreeTableNode
    * @param variables
    * @param parent
    */
   public void buildModelTree(TreeTableNode parent, ModelManagementTreeItem treeItem)
   {
      TreeTableNode treeNode = TreeNodeFactory.createTreeNode(modelTreeTable, this, treeItem, true);
      parent.add(treeNode);

      for (ModelManagementTreeItem item : treeItem.getChildren())
      {
         buildModelTree(treeNode, item);
      }
   }

   /**
    * Updates the changes
    */
   public void update()
   {
      ModelCache.findModelCache().reset();
      XPathCacheManager.getInstance().reset();
      valueChanged = false;
      createFilterToolBarItems();
      createTableDataFilters();
      initialize();

   }

   /**
    * action method to filter specific nodes
    * 
    * @param event
    */
   public void filterTable(ActionEvent event)
   {
      String type = (String) event.getComponent().getAttributes().get("type");

      FilterToolbarItem filterItem = getFilterToolbarItem(type);
      filterItem.toggle();

      // Update Data Filters
      ITableDataFilterOnOff onOffFilter = (ITableDataFilterOnOff) tableDataFilters.getDataFilter(type);
      onOffFilter.toggle();

      modelTreeTable.rebuildList();
   }

   /**
    * @param name
    * @return
    */
   public FilterToolbarItem getFilterToolbarItem(String name)
   {
      for (FilterToolbarItem filterToolbarItem : filterToolbarItems)
      {
         if (filterToolbarItem.getName().equals(name))
         {
            return filterToolbarItem;
         }
      }

      return null;
   }

   public List<FilterToolbarItem> getFilterToolbarItems()
   {
      return filterToolbarItems;
   }

   public int getIncreasedLabelCount()
   {
      return 0;
   }

   public TreeTable getModelTreeTable()
   {
      return modelTreeTable;
   }

   /**
    * method to handle view events
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         ModelCache.findModelCache().reset();
         XPathCacheManager.getInstance().reset();
         createFilterToolBarItems();
         createTableDataFilters();
         initializeTreeTableColumnModel();

      }
      else if (ViewEventType.ACTIVATED == event.getType())
      {
         initialize();

      }

      if ((ModelDeploymentDialogBean.getCurrent() != null) && ModelDeploymentDialogBean.getCurrent().isVisible())
      {
         ModelDeploymentDialogBean.getCurrent().handleEvent(event);
      }
   }

   /**
    * method to initialize tree table
    */
   @Override
   public void initialize()
   {
      // create root node
      ModelManagementTreeItem hiddenRootItem = new ModelManagementTreeItem(null);
      hiddenRootItem.setType(ModelManagementTreeItem.Type.NONE);
      // rootItem.setLabel("Models");
      rootModelNode = TreeNodeFactory.createTreeNode(modelTreeTable, this, hiddenRootItem, true);
      // Now Create a Model & Tree Table
      DefaultTreeModel rootNode = new DefaultTreeModel(rootModelNode);
      modelTreeTable = new TreeTable(rootNode, colSelectorPopup, tableDataFilters);
      modelTreeTable.setDataTableExportHandler(new ModelManagementTableExportHandler());
      modelTreeTable.setHideRootNode(true);// Hiding root node
      modelTreeTable.setAutoFilter(false);
      modelTreeTable.setFilterRootNode(true);
      rootModelNode.getUserObject().setTreeTable(modelTreeTable);
      if (null != modelTreeTable)
      {
         modelTreeTable.setTooltipURL(org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths.V_PANELTOOLTIP_URL);
      }
      List<ModelManagementTreeItem> list = buildTreeTableData(hiddenRootItem);

      for (ModelManagementTreeItem item : list)
      {
         buildModelTree(rootModelNode, item);
      }

      // Build Tree
      // modelTreeTable.initialize();
      // Rebuild the Tree
      modelTreeTable.rebuildList();
   }

   /**
    * getter for valueChanged property
    * 
    * @return
    */
   public boolean isValueChanged()
   {
      return valueChanged;
   }

   /**
    * Action method to open deploy model wizard dialog
    * 
    * @param event
    */
   public void openDeployModel(ActionEvent event)
   {
      ModelDeploymentDialogBean deployBean = ModelDeploymentDialogBean.getCurrent();
      deployBean.setOverwrite(false);
      deployBean.setAllowBrowse(true);
      deployBean.openPopup();
   }

   /**
    * Action method to open Modify Implementation dialog
    * 
    * @param event
    */
   public void openModifyImplementation(ActionEvent event)
   {
      ModelImplementationDialogBean modifyBean = ModelImplementationDialogBean.getCurrent();
      ModelManagementUserObject row = (ModelManagementUserObject) event.getComponent().getAttributes().get("row");
      modifyBean.setUserObject(row);
      modifyBean.openPopup();
   }

   /**
    * Action listener method for popup menu It handle DELETE,OVERWRITE,RUN_REPORT,EXPORT
    * 
    * @param event
    */
   public void popUpActionListener(ActionEvent event)
   {
      ModelManagementUserObject row = (ModelManagementUserObject) event.getComponent().getAttributes().get("row");
      String type = (String) event.getComponent().getAttributes().get("type");
      selectedRow = row;
      if ((row != null) && type.equals(PopUpActionType.DELETE.name()))
      {
         String title = adminMessagesBean.getParamString("views.modelManagementView.confirmDeleteModel.title",
               new String[] {row.getParent().getLabel(), row.getLabel()});
         // Confirmation Dialog on Delete action
         modelMgmtConfirmationDialog = new ConfirmationDialog(DialogContentType.NONE, DialogActionType.YES_NO, null,
               DialogStyle.COMPACT, this);
         modelMgmtConfirmationDialog.setTitle(title);
         modelMgmtConfirmationDialog.setMessage(adminMessagesBean
               .getString("views.modelManagementView.confirmDeleteModel.info"));
         modelMgmtConfirmationDialog.openPopup();
      }
      else if ((row != null) && type.equals(PopUpActionType.OVERWRITE.name()))
      {
         ModelDeploymentDialogBean deployBean = ModelDeploymentDialogBean.getCurrent();
         deployBean.setOverwriteModel(row.getOid(), row.getParent().getLabel(), row.getVersion());
         deployBean.setAllowBrowse(true);
         deployBean.openPopup();
      }
      else if ((row != null) && type.equals(PopUpActionType.RUN_REPORT.name()))
      {
         Map<String, Object> urlParamMap = new HashMap<String, Object>();
         urlParamMap.put("ModelID", row.getModelDescription().getId());
         urlParamMap.put("ModelOID", String.valueOf(row.getOid()));
         DocumentViewUtil.openActiveModelReport(RepositoryUtility.MODEL_DETAILS_DESIGN, row.getModelDescription().getId(), urlParamMap);
      }
      else if ((row != null) && type.equals(PopUpActionType.EXPORT.name()))
      {
         ModelExportDialogBean exportModelDialog = ModelExportDialogBean.getCurrent();
         exportModelDialog.setUserObject(row);
         exportModelDialog.openPopup();
      }
   }

   public void openActiveModelsReport()
   {
      DocumentViewUtil.openActiveModelReport(RepositoryUtility.DEFAULT_ACTIVE_MODEL_REPORT, null, null);
   }

   public void deleteModel()
   {
      SessionContext sessionContext = SessionContext.findSessionContext();
      if (selectedRow != null)
      {

         AdministrationService administrationService = sessionContext.getServiceFactory().getAdministrationService();
         DeploymentInfo info = null;
         Daemon[] stoppedDaemons = null;
         try
         {
            stoppedDaemons = ModelManagementUtil.stopDaemons(sessionContext);
            info = administrationService.deleteModel(selectedRow.getOid());

            if (info != null && info.hasErrors())
            {
               StringBuilder strBuilder = new StringBuilder();
               for (Inconsistency inc : info.getErrors())
               {
                  strBuilder.append(inc.getMessage()).append("\n");
               }
               MessageDialog.addErrorMessage(strBuilder.toString());
            }
            else if (info != null && info.hasWarnings())
            {
               if (info != null && info.hasWarnings())
               {
                  StringBuilder strBuilder = new StringBuilder();
                  for (Inconsistency inc : info.getWarnings())
                  {
                     strBuilder.append(inc.getMessage()).append("\n");
                  }
                  MessageDialog.addWarningMessage(strBuilder.toString());

               }

               initialize();
            }
            else
            {
               MessageDialog.addInfoMessage(getMessages().getString("modelSuccessfullyDeleted"));
               initialize();
            }

         }
         catch (Exception e)
         {
            // here require to customize exception so not using ExceptionHandler class
            MessageDialog.addMessage(MessageType.ERROR, getMessages().getString("couldNotDeleteModel"),
                  e.getLocalizedMessage(), e);
         }
         finally
         {
            ModelCache.findModelCache().reset();
            XPathCacheManager.getInstance().reset();
            SessionContext.findSessionContext().resetSession();
            if (stoppedDaemons != null && stoppedDaemons.length > 0)
            {
               ModelManagementUtil.startDaemons(stoppedDaemons, sessionContext);
            }
         }

      }

   }
  
   /**
    * 
    */
   public boolean accept()
   {
      modelMgmtConfirmationDialog = null;
      deleteModel();
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      modelMgmtConfirmationDialog = null;
      return true;
   }

   public void setSelectedNodeLabel(String label)
   {}

   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {}

   /**
    * setter for valueChanged property
    * 
    * @param valueChanged
    */
   public void setValueChanged(boolean valueChanged)
   {
      this.valueChanged = valueChanged;
   }

   private void createFilterToolBarItems()
   {
      filterToolbarItems = new ArrayList<FilterToolbarItem>(4);

      FilterToolbarItem versionTool = new FilterToolbarItem(ModelManagementTreeItem.Type.MODEL_VERSION.name(),
            ModelManagementTreeItem.Type.MODEL_VERSION.name(), "version",
            "/plugins/views-common/images/icons/book_open.png");
      versionTool.setActive(false);
      filterToolbarItems.add(versionTool);

      FilterToolbarItem consumerTool = new FilterToolbarItem(
            ModelManagementTreeItem.Type.MODEL_VERSION_PROVIDER.name(),
            ModelManagementTreeItem.Type.MODEL_VERSION_PROVIDER.name(), "provider",
            "/plugins/views-common/images/icons/resultset_next.png");
      filterToolbarItems.add(consumerTool);

      FilterToolbarItem providerTool = new FilterToolbarItem(
            ModelManagementTreeItem.Type.MODEL_VERSION_CONSUMER.name(),
            ModelManagementTreeItem.Type.MODEL_VERSION_CONSUMER.name(), "consumer",
            "/plugins/views-common/images/icons/resultset_previous.png");
      filterToolbarItems.add(providerTool);

      FilterToolbarItem primaryInterfaceTool = new FilterToolbarItem(
            ModelManagementTreeItem.Type.PRIMARY_INTERFACE.name(),
            ModelManagementTreeItem.Type.PRIMARY_INTERFACE.name(), "primaryinterface",
            "/plugins/views-common/images/icons/cog.png");
      filterToolbarItems.add(primaryInterfaceTool);

      Collections.unmodifiableCollection(filterToolbarItems);
   }

   private void createTableDataFilters()
   {
      tableDataFilters = new TableDataFilters();

      for (FilterToolbarItem item : filterToolbarItems)
      {
         tableDataFilters.addDataFilter(new TableDataFilterOnOff(item.getName(), null, true, !item.isActive()));
      }
   }

   private ModelManagementTreeItem createTreeNode(DeployedModelDescription model, ModelManagementTreeItem.Type type,
         ModelManagementTreeItem parentItem)
   {
      ModelManagementTreeItem node = new ModelManagementTreeItem(parentItem);
      node.setComment(model.getDeploymentComment());
      node.setLabel(I18nUtils.getLabel(ModelUtils.getModel(model.getModelOID()), model.getName()));
      node.setOid(model.getModelOID());
      node.setType(type);
      node.setValidFrom(model.getValidFrom());
      node.setVersion(model.getVersion());
      node.setActiveVersion(model.isActive());
      node.setModelDescription(model);
      parentItem.getChildren().add(node);
      return node;
   }

   private List<ModelManagementTreeItem> buildTreeTableData(ModelManagementTreeItem parentItem)
   {

      ModelCache modelCache=ModelCache.findModelCache();
      
      Map<String, String> i18nModelNameMap = new HashMap<String, String>();

      Map<String, List<DeployedModelDescription>> modelMap = new HashMap<String, List<DeployedModelDescription>>();
      Map<Long, DeployedModelDescription> modelOIDMap = new HashMap<Long, DeployedModelDescription>();

      List<ModelManagementTreeItem> treeItemList = new ArrayList<ModelManagementTreeItem>();
      Models models = ServiceFactoryUtils.getQueryService().getModels(DeployedModelQuery.findAll());

      for (DeployedModelDescription model : models)
      {
         modelOIDMap.put(new Long(model.getModelOID()), model);

         if (!modelMap.containsKey(model.getId()))
         {
            modelMap.put(model.getId(), new ArrayList<DeployedModelDescription>());
            i18nModelNameMap.put(model.getId(),
                  I18nUtils.getLabel(ModelUtils.getModel(model.getModelOID()), model.getName()));
         }
         modelMap.get(model.getId()).add(model);
      }

      for (String modelId : modelMap.keySet())
      {
         ModelManagementTreeItem modelItem = new ModelManagementTreeItem(parentItem);     
         modelItem.setLabel(i18nModelNameMap.get(modelId));
         modelItem.setType(ModelManagementTreeItem.Type.MODEL);
         treeItemList.add(modelItem);

         List<DeployedModelDescription> modelVersions = modelMap.get(modelId);

         for (DeployedModelDescription version : modelVersions)
         {
            ModelManagementTreeItem versionItem = new ModelManagementTreeItem(modelItem);
            String description = I18nUtils.getDescriptionAsHtml(version, version.getDescription());
            versionItem.setComment(version.getDeploymentComment());
            versionItem.setLabel(adminMessagesBean.getParamString("views.deploymodel.nodelabel",
                  new String[] {version.getVersion(), String.valueOf(version.getModelOID())}));
            versionItem.setOid(version.getModelOID());
            versionItem.setType(ModelManagementTreeItem.Type.MODEL_VERSION);
            versionItem.setValidFrom(version.getValidFrom());
            versionItem.setVersion(version.getVersion());
            versionItem.setActiveVersion(version.isActive());
            versionItem.setModelDescription(version);
            modelItem.getChildren().add(versionItem);
            versionItem.setVersionLabel(description); 
            if (version.isActive())
            {
               modelItem.setVersionLabel(description);
            }
            List<Long> consumerModels = version.getConsumerModels();
            List<Long> providerModels = version.getProviderModels();
            Map<String, List<ImplementationDescription>> implementationProcesses = version.getImplementationProcesses();

            // create consumer nodes
            if (consumerModels != null && !consumerModels.isEmpty())
            {
               for (Long consumerModelOID : consumerModels)
               {
                  DeployedModelDescription consumerModel = modelOIDMap.get(consumerModelOID);
                  if (consumerModel != null)
                  {
                     createTreeNode(consumerModel, ModelManagementTreeItem.Type.MODEL_VERSION_CONSUMER, versionItem);
                  }
               }
            }
            // create provider nodes
            if (providerModels != null && !providerModels.isEmpty())
            {
               for (Long providerModelOID : providerModels)
               {
                  DeployedModelDescription providerModel = modelOIDMap.get(providerModelOID);
                  if (providerModel != null)
                  {
                     createTreeNode(providerModel, ModelManagementTreeItem.Type.MODEL_VERSION_PROVIDER, versionItem);
                  }
               }
            }
            // create process implementation nodes
            if (implementationProcesses != null && !implementationProcesses.isEmpty())
            {

               for (String processName : implementationProcesses.keySet())
               {

                  List<ImplementationDescription> implementationProcessList = implementationProcesses.get(processName);

                  if (implementationProcessList != null && !implementationProcessList.isEmpty())
                  {
                     for (ImplementationDescription implementationProcess : implementationProcessList)
                     {
                        if (implementationProcess.isPrimaryImplementation() && implementationProcess.isActive())
                        {
                           DeployedModelDescription implementationModelDesc = modelOIDMap.get(implementationProcess
                                 .getImplementationModelOid());
                           if (implementationModelDesc != null)
                           {
                              ModelManagementTreeItem node = createTreeNode(implementationModelDesc,
                                    ModelManagementTreeItem.Type.PRIMARY_INTERFACE, versionItem);
                              //implementation model and process
                              DeployedModel implementationModel=modelCache.getModel(implementationModelDesc.getModelOID());
                              ProcessDefinition implementationPD=implementationModel.getProcessDefinition(implementationProcess.getImplementationProcessId());                              
                              String implementationPDName = I18nUtils.getProcessName(implementationPD);
                              //interface model and process
                              DeployedModel interfaceModel=modelCache.getModel(version.getModelOID());
                              ProcessDefinition interfacePD=interfaceModel.getProcessDefinition(processName);
                              String interfacePDName = I18nUtils.getProcessName(interfacePD);                    
                              node.setProcessId(processName);
                              node.setLabel(interfacePDName + " ("+I18nUtils.getLabel(modelCache.getModel(implementationModelDesc.getModelOID()), implementationModelDesc.getName())+" - "+implementationPDName+")");
                           }
                        }
                     }
                  }

               }
            }

         }
      }
      modelMap = null;
      modelOIDMap = null;
      i18nModelNameMap = null;
      return treeItemList;
   }

   /**
    * method to define tree table structure
    */
   private void initializeTreeTableColumnModel()
   {
      List<ColumnPreference> fixedBeforeColumns = new ArrayList<ColumnPreference>();
      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colModel = new ColumnPreference("Model", "label", ColumnDataType.STRING, this.getMessages()
            .getString("column.model"));
      colModel.setColumnAlignment(ColumnAlignment.LEFT);

      ColumnPreference colVersion = new ColumnPreference("Version", "version", ColumnDataType.STRING, this
            .getMessages().getString("column.version"));
      colVersion.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colOid = new ColumnPreference("OID", "oid", ColumnDataType.NUMBER, this.getMessages().getString(
            "column.oid"));
      colOid.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colvalidFrom = new ColumnPreference("ValidFrom", null, this.getMessages().getString(
            "column.validFrom"), ResourcePaths.V_MODEL_MANAGEMENT_VIEW_COLUMNS, true, false);

      ColumnPreference colComment = new ColumnPreference("Comment", null, this.getMessages()
            .getString("column.comment"), ResourcePaths.V_MODEL_MANAGEMENT_VIEW_COLUMNS, true, false);

      colComment.setColumnAlignment(ColumnAlignment.LEFT);

      ColumnPreference colAction = new ColumnPreference("Actions", null,
            this.getMessages().getString("column.actions"), ResourcePaths.V_MODEL_MANAGEMENT_VIEW_COLUMNS, true, false);

      colAction.setColumnAlignment(ColumnAlignment.CENTER);
      colAction.setExportable(false);
      fixedBeforeColumns.add(colModel);

      cols.add(colVersion);
      cols.add(colOid);
      // cols.add(colDisabled);
      cols.add(colvalidFrom);
      cols.add(colComment);

      fixedAfterColumns.add(colAction);

      IColumnModel modelVariablesColumnModel = new DefaultColumnModel(cols, fixedBeforeColumns, fixedAfterColumns,
            UserPreferencesEntries.M_ADMIN, ResourcePaths.V_modelManagementView);
      colSelectorPopup = new TableColumnSelectorPopup(modelVariablesColumnModel);

      modelVariablesColumnModel.initialize();
   }
   
  /**
   * 
   * @author Sidharth.Singh
   * 
   */
   private class ModelManagementTableExportHandler implements DataTableExportHandler<TreeTableUserObject>
   {

      /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#
       * handleHeaderCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType,
       * org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.String)
       */
      public String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text)
      {
         return text;
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport
       * (org.eclipse.stardust.ui.web.common.table.export.ExportType,
       * org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object,
       * java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column, TreeTableUserObject row,
            Object value)
      {
         ModelManagementUserObject thisRow = ((ModelManagementUserObject) row);

         if ("Model".equals(column.getColumnName()))
         {
            return thisRow.getLabel();
         }
         else if ("ValidFrom".equals(column.getColumnName()))
         {
            return thisRow.getValidFrom();
         }
         else if ("Comment".equals(column.getColumnName()))
         {
            return thisRow.getComment();
         }
         else
         {
            return value;
         }
      }

   }
   
   /**
    * 
    * @author Vikas.Mishra
    * @version $Revision: $
    */
   public static enum PopUpActionType {
      DELETE, EXPORT, OVERWRITE, RUN_REPORT;
   }

   public ConfirmationDialog getModelMgmtConfirmationDialog()
   {
      return modelMgmtConfirmationDialog;
   }
   
   

}
