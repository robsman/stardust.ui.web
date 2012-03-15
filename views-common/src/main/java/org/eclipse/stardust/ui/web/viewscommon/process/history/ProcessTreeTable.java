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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.List;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Yogesh.Manware
 *
 */
public class ProcessTreeTable implements TreeTableBean
{
   private static final long serialVersionUID = 1L;
   private ActivityInstance currentActivityInstance;

   // tree default model, used as a value for the tree component
   private DefaultTreeModel model;

   private IProcessHistoryDataModel processHistoryDataModel;
   private IProcessHistoryTableEntry processHistoryTableRoot;
   private List<ProcessInstance> processInstances;
   private ProcessInstance currentProcessInstance;
   private ProcessTableEntryUserObject selectedRow;
   private TreeTable treeTable;
   private TreeTableNode processInstanceNode;
   private final ProcessUIBuilder processUIBuilder;
   private DefaultColumnModelEventHandler columnModelEventHandler;

   /**
    * 
    */
   public ProcessTreeTable()
   {
      columnModelEventHandler = new DefaultColumnModelEventHandler();
      columnModelEventHandler.setNeedRefresh(false);
      processHistoryDataModel = new ProcessHistoryDataModel();
      processUIBuilder = new ProcessUIBuilder(columnModelEventHandler);
      columnModelEventHandler.setNeedRefresh(true);
   }

   /**
    * This needs to be called by Caller to Initialize the Component
    */
   public void initialize()
   {     
      fetchProcessData();     
   }


   /**
    * @param event
    */
   public void filterTable(ActionEvent event)
   {
      UICommand commandObject = (UICommand) event.getComponent();
      Map<String, Object> attributesMap = commandObject.getAttributes();

      String filterName = (String) attributesMap.get("name");

      // Update Filter on UI
      FilterToolbarItem filterToolbarItem = processUIBuilder.getFilterToolbarItem(filterName);
      filterToolbarItem.toggle();

      // Update Data Filters
      ITableDataFilterOnOff onOffFilter = (ITableDataFilterOnOff) processUIBuilder.getOnOffFilters().getDataFilter(
            filterName);
      onOffFilter.toggle();

      treeTable.rebuildList();
   }

   
   /**
    * @param tableEntry
    * @param parent
    * @throws Exception
    */
   @SuppressWarnings("unchecked")
   private void buildProcessHistoryTree(IProcessHistoryTableEntry tableEntry, TreeTableNode parent)
   {
      List<IProcessHistoryTableEntry> listChildren = tableEntry.getChildren();
      if (listChildren != null)
      {
         boolean selectedRowFlag = false;
         for (IProcessHistoryTableEntry childEntry : listChildren)
         {
            TreeTableNode node = TreeNodeFactory.createProcessTreeNode(treeTable, this,
                  (ProcessInstanceHistoryItem) childEntry, childEntry.isNodePathToActivityInstance());
            parent.add(node);
            buildProcessHistoryTree(childEntry, node);
            if (!selectedRowFlag)
            {
               selectedRowFlag = setSelectedRow(node);
            }
         }
      }
   }

   /**
    * @param node
    */
   private boolean setSelectedRow(TreeTableNode node)
   {
      ProcessTableEntryUserObject processTableEntryUserObject = (ProcessTableEntryUserObject) node.getUserObject();

      ProcessInstance selectedProcessInstance = null;

      if (null != selectedRow)
      {
         selectedProcessInstance = selectedRow.getProcessInstance();
      }
      else
      {
         selectedProcessInstance = getCurrentProcessInstance();
      }

      if (processTableEntryUserObject.getProcessInstance().getOID() == selectedProcessInstance.getOID())
      {
         processTableEntryUserObject.setSelected(true);
         selectedRow = processTableEntryUserObject;
         return true;
      }
      return false;
   }

   /**
    * 
    */
   private void fetchProcessData()
   {
      // Works if either Process Instance or Activity Instance is set
      if ((getCurrentProcessInstance() != null) || (getCurrentActivityInstance() != null))
      {
         if (getCurrentProcessInstance() != null)
         {
            processHistoryTableRoot = processHistoryDataModel.getProcessHistoryDataModel(getCurrentProcessInstance(),
                  processInstances, true);
         }
         else
         {
            processHistoryTableRoot = processHistoryDataModel.getProcessHistoryDataModel(getCurrentActivityInstance(),
                  processInstances, true);
         }

         // Root Node
         processInstanceNode = TreeNodeFactory.createProcessTreeNode(treeTable, this,
               (ProcessInstanceHistoryItem) processHistoryTableRoot, true);

         // Now Create a Model & Tree Table
         model = new DefaultTreeModel(processInstanceNode);

         treeTable = new TreeTable(model, processUIBuilder.getProcessColumnFilterPopup(), processUIBuilder.getOnOffFilters());
         treeTable.setDataTableExportHandler(new ProcessTreeTableExportHandler());
         if ((processInstanceNode != null) && (processInstanceNode.getUserObject() != null))
         {
            processInstanceNode.getUserObject().setTreeTable(treeTable);
            setSelectedRow(processInstanceNode);
            // Build Tree
            buildProcessHistoryTree(processHistoryTableRoot, processInstanceNode);
         }
         
         treeTable.setAutoFilter(false);
         columnModelEventHandler.setNeedRefresh(false);
         // Build Tree
         treeTable.initialize();
         columnModelEventHandler.setNeedRefresh(true);

      // reset the filters
         for (FilterToolbarItem filterToolbarItem : processUIBuilder.getProcessFilterToolbarItems())
         {
            ((TableDataFilterOnOff) processUIBuilder.getOnOffFilters().getDataFilter(filterToolbarItem.getName()))
                  .setOn(!filterToolbarItem.isActive());
         }
         
         // Rebuild the Tree
         treeTable.rebuildList();
      }
   }

   /**
    * @author Sidharth.Singh
    * 
    */
   private class ProcessTreeTableExportHandler implements DataTableExportHandler<TreeTableUserObject>
   {
       /*
       * (non-Javadoc)
       * 
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#
       * handleHeaderCellExport
       * (org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler.ExportType,
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
         ProcessTableEntryUserObject thisRow = ((ProcessTableEntryUserObject) row);
         if ("Priority".equals(column.getColumnName()))
         {
            return ProcessInstanceUtils.getPriorityLabel(thisRow.getPriority());
         }
         else if ("Descriptors".equals(column.getColumnName()))
         {
            return DescriptorColumnUtils.exportDescriptors(thisRow.getProcessDescriptorsList(),
                  ExportType.EXCEL == exportType ? "\n" : ", ");
         }
         else
         {
            return value;
         }
      }

   }

   /**
    * 
    * @param node
    * @return
    */
   private boolean isPriorityModified(ProcessInstanceHistoryItem node)
   {
      boolean isPriorityModified = false;
      if (null != node)
      {
         if (node.getOldPriority() != node.getPriority())
         {
            return true;
         }

         List<IProcessHistoryTableEntry> childs = node.getChildren();

         for (IProcessHistoryTableEntry entry : childs)
         {
            if (entry instanceof ProcessInstanceHistoryItem)
            {
               ProcessInstanceHistoryItem item = (ProcessInstanceHistoryItem) entry;
               isPriorityModified = isPriorityModified(item);

               if (isPriorityModified)
               {
                  break;
               }
            }
         }
      }
      return isPriorityModified;
   }   

   public ActivityInstance getCurrentActivityInstance()
   {
      return currentActivityInstance;
   }

   public int getIncreasedLabelCount()
   {
      return 0;
   }

   public IProcessHistoryDataModel getProcessHistoryDataModel()
   {
      return processHistoryDataModel;
   }

   public ProcessTableEntryUserObject getSelectedRow()
   {
      return selectedRow;
   }
   
   public void setSelectedRow(ProcessTableEntryUserObject selectedRow)
   {
      this.selectedRow = selectedRow;
   }

   public boolean isPriorityChanged()
   {
      ProcessInstanceHistoryItem node = (ProcessInstanceHistoryItem) processHistoryTableRoot;

      return isPriorityModified(node);
   }

   public void setCurrentActivityInstance(ActivityInstance currentActivityInstance)
   {
      this.currentActivityInstance = currentActivityInstance;
   }

   public void setCurrentProcessInstance(ProcessInstance currentProcessInstance)
   {
      this.currentProcessInstance = currentProcessInstance;
   }

   public void setProcessHistoryDataModel(IProcessHistoryDataModel processHistoryDataModel)
   {
      this.processHistoryDataModel = processHistoryDataModel;
   }

   public void setProcessInstances(List<ProcessInstance> processInstances)
   {
      this.processInstances = processInstances;
   }

   public void setSelectedNodeLabel(String label)
   {}

   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {}

   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   public IProcessHistoryTableEntry getProcessHistoryTableRoot()
   {
      return processHistoryTableRoot;
   }

   public ProcessInstance getCurrentProcessInstance()
   {
      return currentProcessInstance;
   }

   public ProcessUIBuilder getProcessUIBuilder()
   {
      return processUIBuilder;
   }

   public DefaultColumnModelEventHandler getColumnModelEventHandler()
   {
      return columnModelEventHandler;
   }
   
}