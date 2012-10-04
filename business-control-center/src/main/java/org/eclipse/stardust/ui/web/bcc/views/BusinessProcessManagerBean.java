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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.IActivitySearchHandler;
import org.eclipse.stardust.ui.web.bcc.jsf.IProcessDefinitionSearchHandler;
import org.eclipse.stardust.ui.web.bcc.jsf.IProcessInstancesPrioritySearchHandler;
import org.eclipse.stardust.ui.web.bcc.jsf.IQueryExtender;
import org.eclipse.stardust.ui.web.bcc.jsf.ModelWithPrio;
import org.eclipse.stardust.ui.web.bcc.jsf.PriorityOverviewEntry;
import org.eclipse.stardust.ui.web.bcc.jsf.ProcessDefinitionSearchHandler;
import org.eclipse.stardust.ui.web.bcc.jsf.ProcessDefinitionWithPrio;
import org.eclipse.stardust.ui.web.bcc.jsf.ProcessInstancesPrioritySearchHandler;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IQuery;
import org.eclipse.stardust.ui.web.common.table.IQueryResult;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableUserObject;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQuery;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQueryResult;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityInstanceWithPrioTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessInstanceTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;




/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class BusinessProcessManagerBean extends UIViewComponentBean
      implements TreeTableBean, ICallbackHandler, ResourcePaths,ViewEventHandler
{
   private static final long serialVersionUID = 1L;

   private static final Logger trace = LogManager
         .getLogger(BusinessProcessManagerBean.class);

   public static final String BEAN_ID = "businessProcessManager";

   private IProcessDefinitionSearchHandler processDefinitionSearchHandler;

   private IProcessInstancesPrioritySearchHandler processInstancePrioritySearchHandler;

   private DefaultTreeModel model;

   private TreeTableNode rootModelNode;

   private TreeTable treeTable;

   private IQueryExtender queryExtender;

   private IColumnModel bccProcessManagerColumnModel;

   TableColumnSelectorPopup priorityColSelecpopup;

   private boolean processTableVisible;

   private boolean activityTableVisible;

   private List<SelectItem> allPriorities;

   List<ActivityInstance> nonAbortableAis;

   List<ActivityInstance> abortableAis;   
   private ActivityTableHelper activityTableHelper;   
   private ProcessTableHelper processHelper;
   private boolean isActivated = false;
   private List<FilterToolbarItem> processFilterToolbarItems;
   private TableDataFilters onOffFilters;   
   private DefaultColumnModelEventHandler columnModelEventHandler = new DefaultColumnModelEventHandler();

   /**
    * 
    */
   public BusinessProcessManagerBean()
   {
      super("processOverviewView");
   }

   @Override
   public void initialize()
   {

      rootModelNode = TreeNodeFactory.createTreeNode(treeTable, this, new ModelWithPrio(), false);
      // Now Create a Model & Tree Table
      model = new DefaultTreeModel(rootModelNode);
      treeTable = new TreeTable(model, priorityColSelecpopup, getOnOffFilters());
      treeTable.setDataTableExportHandler(new BusinessProcessManagerTableExportHandler());

      treeTable.setFilterRootNode(false);
      treeTable.setHideRootNode(true);
      rootModelNode.getUserObject().setTreeTable(treeTable);

      List<DeployedModel> models = new ArrayList<DeployedModel>(ModelCache.findModelCache().getActiveModels());
      boolean filterAuxiliaryProcesses = filterAuxiliaryProcesses();
      boolean filterAuxiliaryActivities = filterAuxiliaryActivities();
      
      //sort models 
      Collections.sort(models, new ModelComparator());
      
      for (DeployedModel activeModel : models)
      {
         List<ProcessDefinitionWithPrio> processDefList = processDefinitionSearchHandler
               .getProcessDefinitions(false, filterAuxiliaryActivities, activeModel);

         // sort processes
         if (CollectionUtils.isNotEmpty(processDefList))
         {
            Collections.sort(processDefList, new ProcessComparator());
         }
         
         PriorityOverviewEntry modelWithPrio = new ModelWithPrio(processDefList, processInstancePrioritySearchHandler,
               activeModel.getModelOID(), filterAuxiliaryProcesses);

         TreeTableNode modelNode = TreeNodeFactory.createTreeNode(treeTable, this, modelWithPrio, true);
         if (null != treeTable)
         {
            treeTable.setTooltipURL(org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths.V_PANELTOOLTIP_URL);
         }
         // Build Tree
         buildPriorityOverviewTree(modelWithPrio, modelNode);
         modelNode.getUserObject().setTreeTable(treeTable);
         rootModelNode.add(modelNode);
      }
      columnModelEventHandler.setNeedRefresh(false);
      treeTable.initialize();

//      ITableDataFilterOnOff onOffFilter = (ITableDataFilterOnOff) getOnOffFilters().getDataFilter("auxiliaryProcess");
//      onOffFilter.setOn(true);

      // Rebuild the Tree
      treeTable.rebuildList();
      columnModelEventHandler.setNeedRefresh(true);
   }
   
   public void handleEvent(ViewEvent event)
   {
      try
      {
         if (ViewEventType.CREATED == event.getType())
         {
            processFilterToolbarItems = new ArrayList<FilterToolbarItem>();
            FilterToolbarItem auxiliaryProcess = new FilterToolbarItem("" + 0, "auxiliaryProcess",
                  "processHistory.processTable.showAuxiliaryProcess",
                  "processHistory.processTable.hideAuxiliaryProcess", "process_auxiliary.png",
                  Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
            auxiliaryProcess.setActive(false);
            processFilterToolbarItems.add(auxiliaryProcess);

            FilterToolbarItem auxiliaryActivity = new FilterToolbarItem("" + 1, "auxiliaryActivity",
                  "processHistory.processTable.showAuxiliaryActivities",
                  "processHistory.processTable.hideAuxiliaryActivities", "activity_auxiliary.png",
                  Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
            auxiliaryActivity.setActive(false);
            processFilterToolbarItems.add(auxiliaryActivity);

            initializeDataFilters();
            
            setEmbedded(false);
            setBasePath("..");
   
            // TODO: Remove this later
            processDefinitionSearchHandler = new ProcessDefinitionSearchHandler();
            processInstancePrioritySearchHandler = new ProcessInstancesPrioritySearchHandler();
            // TODO: END
   
            allPriorities = new ArrayList<SelectItem>();
            allPriorities.add(new SelectItem(new Integer(1), this.getMessages().getString("options.high")));
            allPriorities.add(new SelectItem(new Integer(0), this.getMessages().getString("options.normal")));
            allPriorities.add(new SelectItem(new Integer(-1), this.getMessages().getString("options.low")));
            isActivated = false;
         }
         else if (ViewEventType.ACTIVATED == event.getType())
   
         {
            if (!isActivated)
            {
               initializePriorityTable();
               initializeProcessTable();
               initializeActivityTable();
               initialize();
               isActivated = true;
            }
         }
      }
      catch (Exception e)
      {
         trace.error("BusinessProcessManagerBean.handleEvent() failed", e);
      }
   }

   /**
    * @return BusinessProcessManagerBean object
    */
   public static BusinessProcessManagerBean getCurrent()
   {
      return (BusinessProcessManagerBean) FacesContext.getCurrentInstance()
            .getApplication().getVariableResolver().resolveVariable(
                  FacesContext.getCurrentInstance(), BEAN_ID);
   }

   /**
    * Refreshes the page
    */
   public void update()
   {
      if (processTableVisible)
      {
         processHelper.getProcessTable().refresh(true);        
      }
      else if (activityTableVisible)
      {
         activityTableHelper.getActivityTable().refresh(true);       
      }
      initialize();
   }

   /**
    * After delegation, handles refreshing current page
    */
   public void handleEvent(EventType eventType)
   {
      if (eventType == EventType.APPLY)
      {
         initialize();
      }
   }

   /**
    * @param event
    */
   public void filterTable(ActionEvent event)
   {
      UICommand commandObject = (UICommand) event.getComponent();
      Map<String, Object> attributesMap = commandObject.getAttributes();

      String filterName = (String) attributesMap.get("name");

      FilterToolbarItem filterToolbarItem = getFilterToolbarItem(filterName);
      filterToolbarItem.toggle();

      // Update Data Filters
      ITableDataFilterOnOff onOffFilter = (ITableDataFilterOnOff) getOnOffFilters().getDataFilter(filterName);
      onOffFilter.toggle();

      //treeTable.rebuildList();
      initialize();
   }

   /**
    * 
    */
   public void initializeDataFilters()
   {
      onOffFilters = new TableDataFilters();
      for (FilterToolbarItem filterToolbarItem : processFilterToolbarItems)
      {
         onOffFilters.addDataFilter(new TableDataFilterOnOff(filterToolbarItem.getName(), filterToolbarItem.getName(), null, true, false));
      }
   }

   /**
    * @param name
    * @return
    */
   private FilterToolbarItem getFilterToolbarItem(String name)
   {
      for (FilterToolbarItem filterToolbarItem : processFilterToolbarItems)
      {
         if (filterToolbarItem.getName().equals(name))
         {
            return filterToolbarItem;
         }
      }
      return null;
   }

   
   /**
    * Initializes Activity table columns
    */
   private void initializeActivityTable()
   {
      activityTableHelper = new ActivityTableHelper();
      if (activityTableHelper != null)
      {
         activityTableHelper.getColumnModelListener().setNeedRefresh(false);
         activityTableHelper.initActivityTable();
         activityTableHelper.setCallbackHandler(this);
         activityTableHelper.setStrandedActivityView(false);
         activityTableHelper.getActivityTable().initialize();
         activityTableHelper.getColumnModelListener().setNeedRefresh(true);
      }
   }

   /**
    * Initializes Process Table columns
    */
   private void initializeProcessTable()
   {
      // Search Handler will be set later
      processHelper = new ProcessTableHelper();
      if (null != processHelper)
      {
         processHelper.getColumnModelListener().setNeedRefresh(false);
         processHelper.setCallbackHandler(this);
         processHelper.initializeProcessTable();
         processHelper.getProcessTable().initialize();
         processHelper.getColumnModelListener().setNeedRefresh(true);
      }
   }

   /**
    * Initializes Priority table columns
    */
   private void initializePriorityTable()
   {
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference nameCol = new ColumnPreference("Name", "name",
            ColumnDataType.STRING, this.getMessages().getString(
                  "priorityTable.column.name"), new TableDataFilterPopup(
                  new TableDataFilterSearch()));
      fixedCols.add(nameCol);

      List<ColumnPreference> selectableColumns = new ArrayList<ColumnPreference>();
      ColumnPreference statusCol = new ColumnPreference("Status", "", this.getMessages()
            .getString("priorityTable.column.status"), V_processOverviewViewColumns,
            true, false);
      statusCol.setColumnAlignment(ColumnAlignment.CENTER);
      selectableColumns.add(statusCol);

      ColumnPreference colHighPrio = new ColumnPreference("HighPriority", this
            .getMessages().getString("priorityTable.column.highPrio"));
      ColumnPreference hTotalCountCol = new ColumnPreference("highPriority", "", this
            .getMessages().getString("priorityTable.column.totalCount"),
            V_processOverviewViewColumns, true, false);
      hTotalCountCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference hCriticalCountCol = new ColumnPreference("criticalHighPriority",
            "", this.getMessages().getString("priorityTable.column.criticalCount"),
            V_processOverviewViewColumns, true, false);
      hCriticalCountCol.setColumnAlignment(ColumnAlignment.CENTER);

      colHighPrio.addChildren(hTotalCountCol);
      colHighPrio.addChildren(hCriticalCountCol);
      selectableColumns.add(colHighPrio);

      ColumnPreference colNormPrio = new ColumnPreference("NormPrio", this.getMessages()
            .getString("priorityTable.column.normalPrio"));
      ColumnPreference nTotalCountCol = new ColumnPreference("normalPriority", "", this
            .getMessages().getString("priorityTable.column.totalCount"),
            V_processOverviewViewColumns, true, false);
      nTotalCountCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference nCriticalCountCol = new ColumnPreference("criticalNormalPriority",
            "", this.getMessages().getString("priorityTable.column.criticalCount"),
            V_processOverviewViewColumns, true, false);
      nCriticalCountCol.setColumnAlignment(ColumnAlignment.CENTER);

      colNormPrio.addChildren(nTotalCountCol);
      colNormPrio.addChildren(nCriticalCountCol);
      selectableColumns.add(colNormPrio);

      ColumnPreference colLowPrio = new ColumnPreference("LowPrio", this.getMessages()
            .getString("priorityTable.column.lowPrio"));
      ColumnPreference lTotalCountCol = new ColumnPreference("lowPriority", "", this
            .getMessages().getString("priorityTable.column.totalCount"),
            V_processOverviewViewColumns, true, false);
      lTotalCountCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference lCriticalCountCol = new ColumnPreference("criticalLowPriority",
            "", this.getMessages().getString("priorityTable.column.criticalCount"),
            V_processOverviewViewColumns, true, false);
      lCriticalCountCol.setColumnAlignment(ColumnAlignment.CENTER);

      colLowPrio.addChildren(lTotalCountCol);
      colLowPrio.addChildren(lCriticalCountCol);
      selectableColumns.add(colLowPrio);

      ColumnPreference colTotalPrio = new ColumnPreference("TotalPrio", this
            .getMessages().getString("priorityTable.column.total"));
      ColumnPreference totalCol = new ColumnPreference("totalPriority", "", this
            .getMessages().getString("priorityTable.column.totalCount"),
            V_processOverviewViewColumns, true, false);
      totalCol.setColumnAlignment(ColumnAlignment.CENTER);
      ColumnPreference criticalTotalCol = new ColumnPreference("criticalTotalPriority",
            "", this.getMessages().getString("priorityTable.column.criticalCount"),
            V_processOverviewViewColumns, true, false);
      criticalTotalCol.setColumnAlignment(ColumnAlignment.CENTER);

      colTotalPrio.addChildren(totalCol);
      colTotalPrio.addChildren(criticalTotalCol);
      selectableColumns.add(colTotalPrio);

      ColumnPreference performerCol = new ColumnPreference("PerformerName", "", this
            .getMessages().getString("priorityTable.column.role"),
            V_processOverviewViewColumns, true, false);
      performerCol.setColumnAlignment(ColumnAlignment.CENTER);
      selectableColumns.add(performerCol);
      bccProcessManagerColumnModel = new DefaultColumnModel(selectableColumns, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_PRIORITY_VIEW, columnModelEventHandler);
      priorityColSelecpopup = new TableColumnSelectorPopup(bccProcessManagerColumnModel);

   }

   /**
    * @param prioOverviewEntry
    * @param parent
    */
   private void buildPriorityOverviewTree(PriorityOverviewEntry prioOverviewEntry,
         TreeTableNode parent)
   {
      List listChildren = prioOverviewEntry.getChildren();
      if (listChildren != null)
      {
         Iterator itChildren = listChildren.iterator();
         while (itChildren.hasNext())
         {
            PriorityOverviewEntry childEntry = (PriorityOverviewEntry) itChildren.next();

            if (childEntry instanceof ProcessDefinitionWithPrio)
            {
               ProcessDefinitionWithPrio pdwp = (ProcessDefinitionWithPrio) childEntry;
               ((ProcessDefinitionWithPrio) childEntry).activateChildrenFetch();
            }

            TreeTableNode node = TreeNodeFactory.createTreeNode(treeTable, this, childEntry, false);
            parent.add(node);

            buildPriorityOverviewTree(childEntry, node);
         }
      }
   }

   /**
    * 
    * @param item
    * @param searchHandler
    */
   public void setDetailViewProperties(PriorityOverviewEntry item,
         ISearchHandler searchHandler)
   {
      if (searchHandler instanceof IProcessInstancesPrioritySearchHandler)
      {
         processTableVisible = true;
         activityTableVisible = false;
         processHelper.getProcessTable().setISearchHandler(new WrapSearchHandler<ProcessInstance>(searchHandler));
         refreshProcessTable();
         
      }
      else if (searchHandler instanceof IActivitySearchHandler)
      {
         processTableVisible = false;
         activityTableVisible = true;
         activityTableHelper.getActivityTable().setISearchHandler(new WrapSearchHandler<ActivityInstance>(searchHandler));
         refreshActivityTable();
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class WrapSearchHandler<E> implements org.eclipse.stardust.ui.web.common.table.ISearchHandler<E> 
   {
      private static final long serialVersionUID = 1L;

      ISearchHandler searchHandler;
      
      public WrapSearchHandler(ISearchHandler searchHandler)
      {
         this.searchHandler = searchHandler;
      }
      
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.ISearchHandler#buildQuery()
       */
      public IQuery buildQuery()
      {
         return new IppQuery(searchHandler.createQuery());
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.paginator.spi.ISearchHandler#performSearch(org.eclipse.stardust.ui.web.common.table.paginator.spi.IQuery, int, int)
       */
      @SuppressWarnings("unchecked")
      public IQueryResult<E> performSearch(IQuery query, int startRow, int pageSize)
      {
         return new IppQueryResult<E>(searchHandler.performSearch(((IppQuery)query).getQuery()));
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   private class BusinessProcessManagerTableExportHandler implements DataTableExportHandler<TreeTableUserObject>
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object, java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column, TreeTableUserObject row,
            Object value)
      {
         PriorityOverviewUserObject thisRow = ((PriorityOverviewUserObject)row);

         if ("Name".equals(column.getColumnName()))
         {
            return thisRow.getName();
         }
         else if ("Status".equals(column.getColumnName()))
         {
            String ret = "";
            switch(thisRow.getThresholdState())
            {
            case 1:
               ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.critical");
               break;
            case 2:
               ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.warning");
               break;
            case 3:
               ret = MessagesBCCBean.getInstance().getString("views.trafficLightView.normal");
               break;
            }
            return ret;
         }
         else if ("highPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getHighPriority());
         }
         else if ("criticalHighPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getCriticalHighPriority());
         }
         else if ("normalPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getNormalPriority());
         }
         else if ("criticalNormalPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getCriticalNormalPriority());
         }
         else if ("lowPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getLowPriority());
         }
         else if ("criticalLowPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getCriticalLowPriority());
         }
         else if ("totalPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getTotalPriority());
         }
         else if ("criticalTotalPriority".equals(column.getColumnName()))
         {
            return String.valueOf(thisRow.getCriticalTotalPriority());
         }
         else if ("PerformerName".equals(column.getColumnName()))
         {
            return thisRow.getDefaultPerformerName();
         }
         else
         {
            return value;
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleHeaderCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.String)
       */
      public String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text)
      {
         return text;
      }
   }
   
   /**
    * @author Yogesh.Manware
    * 
    */
   public static class ModelComparator implements Comparator<DeployedModel>
   {
      /*
       * (non-Javadoc)
       * 
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(DeployedModel model1, DeployedModel model2)
      {
         String model1Name = I18nUtils.getModelName(model1);
         String model2Name = I18nUtils.getModelName(model2);
         return model1Name.compareTo(model2Name);
      }
   }

   /**
    * @author Yogesh.Manware
    * 
    */
   public static class ProcessComparator implements Comparator<ProcessDefinitionWithPrio>
   {
      /*
       * (non-Javadoc)
       * 
       * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(ProcessDefinitionWithPrio process1, ProcessDefinitionWithPrio process2)
      {
         String process1Name = I18nUtils.getProcessName(process1.getProcessDefinition());
         String process2Name = I18nUtils.getProcessName(process2.getProcessDefinition());
         return process1Name.compareTo(process2Name);
      }
   }
   
   /**
    * @return
    */
   public boolean filterAuxiliaryProcesses()
   {
      FilterToolbarItem filterToolbarItem = getFilterToolbarItem("auxiliaryProcess");
      return !filterToolbarItem.isActive();
   }
   

   /**
    * @return
    */
   public boolean filterAuxiliaryActivities()
   {
      FilterToolbarItem filterToolbarItem = getFilterToolbarItem("auxiliaryActivity");
      return !filterToolbarItem.isActive();
   }
   
   
   /**
    * 
    */
   private void refreshProcessTable()
   {

      processHelper.getProcessTable().refresh(new DataTableSortModel<ProcessInstanceTableEntry>(
            "startTime", false));
   }

   /**
    * 
    */
   private void refreshActivityTable()
   {
      activityTableHelper.getActivityTable().refresh(new DataTableSortModel<ActivityInstanceWithPrioTableEntry>(
            "startTime", false));
   }

   // ***************** Default Getter & Setter Methods ***********
   public IQueryExtender getQueryExtender()
   {
      return queryExtender;
   }

   public int getIncreasedLabelCount()
   {
      return 0;
   }

   public void setSelectedNodeLabel(String label)
   {}

   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {}

   public IProcessDefinitionSearchHandler getProcessDefinitionSearchHandler()
   {
      return processDefinitionSearchHandler;
   }

   public void setProcessDefinitionSearchHandler(
         IProcessDefinitionSearchHandler processDefinitionSearchHandler)
   {
      this.processDefinitionSearchHandler = processDefinitionSearchHandler;
   }

   public IProcessInstancesPrioritySearchHandler getProcessInstancePrioritySearchHandler()
   {
      return processInstancePrioritySearchHandler;
   }

   public void setProcessInstancePrioritySearchHandler(
         IProcessInstancesPrioritySearchHandler processInstancePrioritySearchHandler)
   {
      this.processInstancePrioritySearchHandler = processInstancePrioritySearchHandler;
   }

   public DefaultTreeModel getModel()
   {
      return model;
   }

   public TreeTableNode getRootModelNode()
   {
      return rootModelNode;
   }

   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   public boolean isProcessTableVisible()
   {
      return processTableVisible;
   }

   public boolean isActivityTableVisible()
   {
      return activityTableVisible;
   }

   public List<SelectItem> getAllPriorities()
   {
      return allPriorities;
   }

   public ProcessTableHelper getProcessHelper()
   {
      return processHelper;
   }

   public ActivityTableHelper getActivityTableHelper()
   {
      return activityTableHelper;
   }

   public List<FilterToolbarItem> getProcessFilterToolbarItems()
   {
      return processFilterToolbarItems;
   }

   public TableDataFilters getOnOffFilters()
   {
      return onOffFilters;
   }
}