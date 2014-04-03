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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.AbortActivityBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * helps to build an activity tree on process history view
 * 
 * @author Vikas.Mishra
 * 
 */
public class ActivityTreeTable implements TreeTableBean,ICallbackHandler
{
   private final ActivityUIBuilder activityUIBuilder;
   private static final Logger trace = LogManager.getLogger(ProcessHistoryTable.class);
   private final Map<ProcessInstance, IProcessHistoryTableEntry> treeCache = new HashMap<ProcessInstance, IProcessHistoryTableEntry>();
   private DefaultTreeModel model;
   private IProcessHistoryDataModel processHistoryDataModel;
   private List<ProcessInstance> processInstances;
   private MessagesViewsCommonBean propsBean;
   private ProcessInstance currentProcessInstance;
   private TreeTable treeTable;
   private boolean miniMode;
   private String tableTitle;
   private IProcessHistoryTableEntry activityTableRoot = null;
   private List<IProcessHistoryTableEntry> caseActivitiesRoot = null;
   private boolean caseProcess = false;
   private DefaultColumnModelEventHandler columnModelEventHandler;
   /**
     *
     */
   public ActivityTreeTable()
   {
      columnModelEventHandler = new DefaultColumnModelEventHandler();
      columnModelEventHandler.setNeedRefresh(false);
      propsBean = MessagesViewsCommonBean.getInstance();
      processHistoryDataModel = new ProcessHistoryDataModel();
      activityUIBuilder = new ActivityUIBuilder(columnModelEventHandler);
      columnModelEventHandler.setNeedRefresh(true);
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
      FilterToolbarItem filterToolbarItem = activityUIBuilder.getFilterToolbarItem(filterName);
      filterToolbarItem.toggle();

      // Update Data Filters
      ITableDataFilterOnOff onOffFilter = (ITableDataFilterOnOff) activityUIBuilder.getOnOffFilters().getDataFilter(
            filterName);
      onOffFilter.toggle();

      treeTable.rebuildList();
   }

   /**
    * @param refresh
    */
   public void initialize()
   {
      reset();
      renderTree();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler#handleEvent(org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType)
    */
   public void handleEvent(EventType eventType)
   {
      if (eventType == EventType.APPLY)
      {        
         fetchActivityData();        
      }
   }

   /**
    * @return
    */
   public IProcessHistoryDataModel getProcessHistoryDataModel()
   {
      if (processHistoryDataModel == null)
      {
         processHistoryDataModel = new ProcessHistoryDataModel();
      }
      return processHistoryDataModel;
   }

   
   /**
    * Abort Activity
    * 
    * @param ae
    */
   public void openAbortActivityDialog(ActionEvent ae)
   {
      ActivityTableEntryUserObject row = (ActivityTableEntryUserObject) ae.getComponent().getAttributes().get("row");

      if (row.getTableEntry().getRuntimeObject() instanceof ActivityInstance)
      {
         ActivityInstance activityInstance = (ActivityInstance) row.getTableEntry().getRuntimeObject();
         AbortActivityBean abortActivity = AbortActivityBean.getInstance();
         abortActivity.setCallbackHandler(this);
         abortActivity.abortActivity(activityInstance);
      }
      else
      {
         trace.warn(this.getClass().getName() + " Method: openAbortActivityDialog()"
               + " Runtime Object is not of type ActivityInstance");
      }
   }
   
   
   /**
    * 
    * @param rootNode
    * @param activityRootInstance
    */
   @SuppressWarnings("unchecked")
   private void buildActivityTreeTable(TreeTableNode rootNode, IProcessHistoryTableEntry activityRootInstance)
   {
      List<IProcessHistoryTableEntry> childs = activityRootInstance.getChildren();

      if ((childs != null) && !childs.isEmpty())
      {
         for (IProcessHistoryTableEntry activiyItem : childs)
         {
            TreeTableNode activityNode = TreeNodeFactory.createActivityTreeNode(treeTable, this, activiyItem, true);
            buildActivityTreeTable(activityNode, activiyItem);
            rootNode.add(activityNode);
         }
      }
   }

   /**
     *
     */
   private void fetchActivityData()
   {
      try
      {
         if (treeCache.containsKey(currentProcessInstance))
         {
            activityTableRoot = treeCache.get(currentProcessInstance);
         }
         else
         {
            activityTableRoot = processHistoryDataModel.getActivityDataModel(currentProcessInstance,
                  processInstances, true);
            treeCache.put(currentProcessInstance, activityTableRoot);
         }
         if (caseProcess)
         {
            caseActivitiesRoot = CollectionUtils.newArrayList();
            for (ProcessInstance pi : processInstances)
            {
               // populate the activityRoot of all PI's invloved in Case, used to display
               // the Participants
               if(!pi.isCaseProcessInstance())
               {
                  caseActivitiesRoot.add(processHistoryDataModel.getActivityDataModel(pi, processInstances, true));   
               }
            }
         }

         // Root Node
         TreeTableNode activityRootNode = TreeNodeFactory.createActivityTreeNode(treeTable, this, activityTableRoot,
               true);

         // Now Create a Model & Tree Table
         model = new DefaultTreeModel(activityRootNode);
         treeTable = new TreeTable(model, activityUIBuilder.getActivityColumnFilterPopup(), activityUIBuilder
               .getOnOffFilters());
         treeTable.setDataTableExportHandler(new ActivityTreeTableExportHandler());
         treeTable.setHideRootNode(true);
         treeTable.setAutoFilter(false);
         activityRootNode.getUserObject().setTreeTable(treeTable);
         activityRootNode.getUserObject().setLeaf(false);
         if (null != treeTable)
            treeTable.setTooltipURL(ResourcePaths.V_PANELTOOLTIP_URL);
         buildActivityTreeTable(activityRootNode, activityTableRoot);
         
         columnModelEventHandler.setNeedRefresh(false);
         // Build Tree
         treeTable.initialize();
         columnModelEventHandler.setNeedRefresh(true);
         
         // reset the filters
         for (FilterToolbarItem filterToolbarItem : activityUIBuilder.getActivityFilterToolbarItems())
         {
            ((TableDataFilterOnOff) activityUIBuilder.getOnOffFilters().getDataFilter(filterToolbarItem.getName()))
                  .setOn(!filterToolbarItem.isActive());
         }

         // Rebuild the Tree
         treeTable.rebuildList();
         ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(currentProcessInstance
               .getModelOID(), currentProcessInstance.getProcessID());
         String processName = I18nUtils.getProcessName(processDefinition);
         tableTitle = propsBean.getParamString("processHistory.activityTable.label", processName);
      }
      catch (Exception e)
      {
         trace.error("Unable to fetch activity data", e);
         MessageDialog.addErrorMessage(e.getMessage());
      }
   } 

   public ProcessInstance getCurrentProcessInstance()
   {
      return currentProcessInstance;
   }

   public int getIncreasedLabelCount()
   {
      return 0;
   }

   public List<ProcessInstance> getProcessInstances()
   {
      return processInstances;
   }

   public MessagesViewsCommonBean getPropsBean()
   {
      return propsBean;
   }

   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   public boolean isMiniMode()
   {
      return miniMode;
   }

   public void renderTree()
   {
      fetchActivityData();
   }

   public void reset()
   {
      treeCache.clear();
   }

   public void setCurrentProcessInstance(ProcessInstance currentProcessInstance)
   {
      this.currentProcessInstance = currentProcessInstance;
   }

   public void setMiniMode(boolean miniMode)
   {
      this.miniMode = miniMode;
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

   public void setTreeTable(TreeTable treeTable)
   {
      this.treeTable = treeTable;
   }

   public ActivityUIBuilder getActivityUIBuilder()
   {
      return activityUIBuilder;
   }

   public String getTableTitle()
   {
      return tableTitle;
   }

   /**
    * @return the activityRootInstance
    */
   public IProcessHistoryTableEntry getActivityTableRoot()
   {
      return activityTableRoot;
   }

   public List<IProcessHistoryTableEntry> getCaseActivitiesRoot()
   {
      return caseActivitiesRoot;
   }

   public void setCaseProcess(boolean caseProcess)
   {
      this.caseProcess = caseProcess;
   }
   
   /**
    * @author Sidharth.Singh
    * 
    */
   private class ActivityTreeTableExportHandler implements DataTableExportHandler<TreeTableUserObject>
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
         ActivityTableEntryUserObject thisRow = ((ActivityTableEntryUserObject) row);
         if ("Event Details".equals(column.getColumnName()))
         {
            if(StringUtils.isNotEmpty(thisRow.getFullDetails()))
            {
               return thisRow.getFullDetails();
            }
            else
            {
               return value;
            }
         }
         else
         {
            return value;
         }
            
      }

   }

}