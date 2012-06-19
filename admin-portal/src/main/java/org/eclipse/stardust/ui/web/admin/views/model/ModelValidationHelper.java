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
import java.util.List;

import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.api.model.Inconsistency;
import org.eclipse.stardust.engine.api.runtime.ModelReconfigurationInfo;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.admin.views.TreeNodeFactory;
import org.eclipse.stardust.ui.web.admin.views.model.dialog.DeploymentStatusTableEntry;
import org.eclipse.stardust.ui.web.admin.views.model.dialog.ErrorWarningTreeItem;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;



public class ModelValidationHelper
{
   private SortableTable<DeploymentStatusTableEntry> deploymentStatus;
   private IColumnModel modelVariablesColumnModel;
   private TreeTable treeTable;
   private TreeTableNode rootModelNode = null;
   private List<ModelReconfigurationInfo> deploymentInfoList = null;
   private AdminMessagesPropertiesBean propsBean;
   private boolean containsErrors = false;
   private boolean containsWarnings = false;
   private TreeTableBean treeTableBean;

   public ModelValidationHelper()
   {
      propsBean = AdminMessagesPropertiesBean.getInstance();
   }

   /**
    * method define tree table structure
    */
   public void createTreeTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colMessage = new ColumnPreference("Message", "messageDetail", ColumnDataType.STRING,
            propsBean.getString("views.deploymodel.deploymentStatus.errorwarning.table.column.message"));

      ColumnPreference colModel = new ColumnPreference("Model", "modelId", ColumnDataType.STRING,
            propsBean.getString("views.deploymodel.deploymentStatus.errorwarning.table.column.model"));

      ColumnPreference colElement = new ColumnPreference("Element", "element", ColumnDataType.STRING,
            propsBean.getString("views.deploymodel.deploymentStatus.errorwarning.table.column.element"));

      cols.add(colMessage);
      cols.add(colModel);
      cols.add(colElement);

      modelVariablesColumnModel = new DefaultColumnModel(cols, null, null, UserPreferencesEntries.M_ADMIN,
            ResourcePaths.V_configurationVariablesView);

      modelVariablesColumnModel.initialize();
   }

   private List<DeploymentStatusTableEntry> getDeploymentStatusTableData()
   {
      List<DeploymentStatusTableEntry> list = new ArrayList<DeploymentStatusTableEntry>();
      DeploymentStatusTableEntry entry = getDeploymentStatusTableEntry();
      list.add(entry);

      return list;
   }

   private DeploymentStatusTableEntry getDeploymentStatusTableEntry()
   {
      DeploymentStatusTableEntry entry = new DeploymentStatusTableEntry();

      for (ModelReconfigurationInfo info : deploymentInfoList)
      {
         if (info.hasErrors())
         {
            int errorCount = entry.getErrors() + info.getErrors().size();
            entry.setErrors(errorCount);
         }

         if (info.hasWarnings())
         {
            int warningCount = entry.getWarnings() + info.getWarnings().size();
            entry.setWarnings(warningCount);
         }
      }

      // if contain error(s) then deployment is incomplete otherwise successfully deployed
      entry.setComplete(!entry.hasErrors());
      containsErrors = entry.hasErrors();
      containsWarnings = (entry.getWarnings() > 0) ? true : false;

      return entry;
   }

   public boolean isContainsErrors()
   {
      return containsErrors;
   }

   public boolean isContainsWarnings()
   {
      return containsWarnings;
   }

   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   public TreeTableNode getRootModelNode()
   {
      return rootModelNode;
   }

   public void setTreeTable(TreeTable treeTable)
   {
      this.treeTable = treeTable;
   }

   /**
    * method to create table definition for model file
    */
   public void createStatusTable()
   {
      List<ColumnPreference> cols = new ArrayList<ColumnPreference>();

      ColumnPreference colStatus = new ColumnPreference("Status", null,
            propsBean.getString("views.deploymodel.deploymentStatus.column.status"),
            ResourcePaths.V_DEPLOYMENT_STATUS_COLUMNS, true, false);

      colStatus.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colErrors = new ColumnPreference("Errors", "errors", ColumnDataType.NUMBER,
            propsBean.getString("views.deploymodel.deploymentStatus.column.errors"));
      colErrors.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference colWarnings = new ColumnPreference("Warnings", "warnings", ColumnDataType.NUMBER,
            propsBean.getString("views.deploymodel.deploymentStatus.column.warnings"));

      colWarnings.setColumnAlignment(ColumnAlignment.CENTER);

      cols.add(colStatus);
      cols.add(colErrors);
      cols.add(colWarnings);

      List<ColumnPreference> fixedBeforeColumns = new ArrayList<ColumnPreference>();
      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();

      IColumnModel deploymentStatusColumnModel = new DefaultColumnModel(cols, fixedBeforeColumns, fixedAfterColumns,
            UserPreferencesEntries.M_ADMIN, ResourcePaths.V_modelManagementView);

      deploymentStatus = new SortableTable<DeploymentStatusTableEntry>(deploymentStatusColumnModel, null,
            new SortableTableComparator<DeploymentStatusTableEntry>("errors", true));
      deploymentStatus.initialize();
   }

   private ErrorWarningTreeItem createTreeItem(Inconsistency inconsistency, String modelId)
   {
      ErrorWarningTreeItem childItem = new ErrorWarningTreeItem();
      childItem.setElement(inconsistency.getSourceElementName());
      childItem.setElementId(inconsistency.getSourceElementId());
      childItem.setMessage(inconsistency.getMessage());
      childItem.setModelId(modelId);

      if (inconsistency.getSeverity() == Inconsistency.ERROR)
      {
         childItem.setType(ErrorWarningTreeItem.Type.ERROR);
      }
      else
      {
         childItem.setType(ErrorWarningTreeItem.Type.WARNING);
      }

      return childItem;
   }

   /**
    * Mock data
    * 
    * @return
    */
   private List<ErrorWarningTreeItem> getErrorWarningTreeTableData()
   {
      List<ErrorWarningTreeItem> treeItems = new ArrayList<ErrorWarningTreeItem>();
      ErrorWarningTreeItem errorItem = new ErrorWarningTreeItem();
      errorItem.setType(ErrorWarningTreeItem.Type.ERROR);

      ErrorWarningTreeItem warningItem = new ErrorWarningTreeItem();
      warningItem.setType(ErrorWarningTreeItem.Type.WARNING);

      for (ModelReconfigurationInfo info : deploymentInfoList)
      {
         // add errors
         for (Inconsistency inconsistency : info.getWarnings())
         {
            ErrorWarningTreeItem childItem = createTreeItem(inconsistency, info.getId());
            warningItem.getChildrens().add(childItem);
         }

         for (Inconsistency inconsistency : info.getErrors())
         {
            ErrorWarningTreeItem childItem = createTreeItem(inconsistency, info.getId());
            errorItem.getChildrens().add(childItem);
         }
      }

      if (!errorItem.getChildrens().isEmpty())
      {
         errorItem.setMessage(propsBean.getParamString("views.deploymodel.deploymentStatus.errorCount",
               new String[] {String.valueOf(errorItem.getChildrens().size())}));
         treeItems.add(errorItem);
      }

      if (!warningItem.getChildrens().isEmpty())
      {
         warningItem.setMessage(propsBean.getParamString("views.deploymodel.deploymentStatus.warningCount",
               new String[] {String.valueOf(warningItem.getChildrens().size())}));
         treeItems.add(warningItem);
      }

      return treeItems;
   }

   /**
       *
       */
   public void initialize()
   {
      if ((deploymentInfoList != null) && !deploymentInfoList.isEmpty())
      {
         if(deploymentStatus!=null && getDeploymentStatusTableData()!=null)
         {
            deploymentStatus.setList(getDeploymentStatusTableData());
            deploymentStatus.initialize();
         }

         // create root node
         ErrorWarningTreeItem rootItem = new ErrorWarningTreeItem();
         rootItem.setMessage("");
         rootModelNode = TreeNodeFactory.createTreeNode(treeTable, treeTableBean, rootItem, true);

         // Now Create a Model & Tree Table
         DefaultTreeModel rootNode = new DefaultTreeModel(rootModelNode);
         treeTable = new TreeTable(rootNode, modelVariablesColumnModel, null);
         treeTable.setAutoFilter(false);
         treeTable.setFilterRootNode(true);
         rootModelNode.getUserObject().setTreeTable(treeTable);

         List<ErrorWarningTreeItem> treeItems = getErrorWarningTreeTableData();

         for (ErrorWarningTreeItem item : treeItems)
         {
            buildTableTree(rootModelNode, item);
         }

         // Build Tree
         treeTable.initialize();
         // Rebuild the Tree
         treeTable.rebuildList();
      }
   }

   public SortableTable<DeploymentStatusTableEntry> getDeploymentStatus()
   {
      return deploymentStatus;
   }

   public List<ModelReconfigurationInfo> getDeploymentInfoList()
   {
      return deploymentInfoList;
   }

   public void setDeploymentInfoList(List<ModelReconfigurationInfo> deploymentInfoList)
   {
      this.deploymentInfoList = deploymentInfoList;
   }

   private void buildTableTree(TreeTableNode parentNode, ErrorWarningTreeItem treeItem)
   {
      TreeTableNode treeNode = TreeNodeFactory.createTreeNode(treeTable, treeTableBean, treeItem, true);
      parentNode.add(treeNode);

      for (ErrorWarningTreeItem item : treeItem.getChildrens())
      {
         buildTableTree(treeNode, item);
      }
   }

   public TreeTableBean getTreeTableBean()
   {
      return treeTableBean;
   }

   public void setTreeTableBean(TreeTableBean treeTableBean)
   {
      this.treeTableBean = treeTableBean;
   }
}
