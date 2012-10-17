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
package org.eclipse.stardust.ui.web.bcc.views.criticalityManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.event.ActionEvent;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalityStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.views.TreeNodeFactory;
import org.eclipse.stardust.ui.web.bcc.views.criticalityManager.ICriticalityMgrTableEntry.CriticalityDetails;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
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
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.common.ISearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQuery;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppQueryResult;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityInstanceWithPrioTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;

/**
 * @author Shrikant.Gangal
 * 
 */
public class ActivityCriticalityManagerBean extends UIViewComponentBean
      implements TreeTableBean, ICallbackHandler, ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private static final int DEFAULT_COLUMNS_TO_DISPLAY = 3;

   public static final String BEAN_ID = "activityCriticalityManagerBean";

   private TreeTableNode rootModelNode;
   private TreeTable treeTable;
   private ActivityTableHelper activityTableHelper;
   private TableDataFilters onOffFilters;
   private List<FilterToolbarItem> processFilterToolbarItems;
   private Map<String, Map<String, CriticalityStatistics>> processCriticalityStatisticsMap;
   private boolean activityTableVisible;
   private boolean isActivated = false;

   /**
    * 
    */
   public ActivityCriticalityManagerBean()
   {
      super("activityCriticalityManagerView");
   }

   @Override
   public void initialize()
   {
      CriticalityMgrColumnModelListener columnSelectionListener = new CriticalityMgrColumnModelListener(this);
      List<CriticalityCategory> criticalityCategoryList = CriticalityConfigurationHelper.getInstance().getCriticalityConfiguration();
      initializeCriticalityTable(criticalityCategoryList, columnSelectionListener);

      List<DeployedModel> models = ModelCache.findModelCache().getActiveModels();
      boolean filterAuxiliaryProcesses = filterAuxiliaryProcesses();
      boolean filterAuxiliaryActivities = filterAuxiliaryActivities();
      processCriticalityStatisticsMap = new HashMap<String, Map<String, CriticalityStatistics>>();
      for (DeployedModel activeModel : models)
      {
         List<ICriticalityMgrTableEntry> processEntries = new ArrayList<ICriticalityMgrTableEntry>();

         /*
          * Fetch all process definitions irrespective of the filterAuxiliaryProcesses
          * flag as we need to show consider all processes for showing row counts.
          */
         List<ProcessDefinition> procDefs = ProcessDefinitionUtils.getAllProcessDefinitions(activeModel, false);

         for (ProcessDefinition procDef : procDefs)
         {
            List<ICriticalityMgrTableEntry> actEntries = new ArrayList<ICriticalityMgrTableEntry>();
            for (int i = 0; i < criticalityCategoryList.size() && i < DEFAULT_COLUMNS_TO_DISPLAY; i++)
            {
               updateCriticalityStatisticsMap(processCriticalityStatisticsMap, procDef, criticalityCategoryList.get(i));
            }

            @SuppressWarnings("unchecked")
            List<Activity> activities = procDef.getAllActivities();
            for (Activity act : activities)
            {
               actEntries.add(new ActivityDefCriticalityMgrTableEntry(act, procDef, processCriticalityStatisticsMap
                     .get(procDef.getQualifiedId())));
            }

            processEntries.add(new ProcessDefCriticalityMgrTableEntry(procDef, processCriticalityStatisticsMap
                  .get(procDef.getQualifiedId()), actEntries, filterAuxiliaryActivities));
         }

         ICriticalityMgrTableEntry modelWithCriticality = new ModelDefCriticalityMgrTableEntry(processEntries,
               activeModel, filterAuxiliaryProcesses, filterAuxiliaryActivities);

         TreeTableNode modelNode = TreeNodeFactory.createTreeNode(treeTable, this, modelWithCriticality, true);
         // Build Tree
         buildCriticalityOverviewTree(modelWithCriticality, modelNode);
         modelNode.getUserObject().setTreeTable(treeTable);
         rootModelNode.add(modelNode);
      }
      initializeTree();

      columnSelectionListener.setNeedRefresh(false);
      treeTable.initialize();

      initializeMissingCriticaliyStatistics();
      // Rebuild the Tree
      treeTable.rebuildList();
      columnSelectionListener.setNeedRefresh(true);
   }

   /**
    * 
    */
   public void initializeMissingCriticaliyStatistics()
   {
      List<ColumnPreference> cols = treeTable.getColumnModel().getSelectableColumns();
      for (ColumnPreference col : cols)
      {
         if (col.isVisible())
         {
            CriticalityCategory cCat = CriticalityConfigurationUtil.getCriticalityForLabel(col.getColumnName());
            if (null != cCat)
            {
               List<DeployedModel> models = ModelCache.findModelCache().getActiveModels();
               boolean filterAuxiliaryProcesses = filterAuxiliaryProcesses();
               for (DeployedModel activeModel : models)
               {
                  List<ProcessDefinition> procDefs = ProcessDefinitionUtils.getAllProcessDefinitions(activeModel,
                        filterAuxiliaryProcesses);
                  for (ProcessDefinition procDef : procDefs)
                  {
                     Map<String, CriticalityStatistics> csMap = processCriticalityStatisticsMap.get(procDef
                           .getQualifiedId());
                     if (null == csMap.get(cCat.getLabel()))
                     {
                        updateCriticalityStatisticsMap(processCriticalityStatisticsMap, procDef, cCat);
                     }
                  }
               }
            }
         }
      }

      initializeTree();
   }

   /**
    * 
    */
   public void reInitializeCriticaliyStatistics()
   {
      List<ColumnPreference> cols = treeTable.getColumnModel().getSelectableColumns();
      for (ColumnPreference col : cols)
      {
         if (col.isVisible())
         {
            CriticalityCategory cCat = CriticalityConfigurationUtil.getCriticalityForLabel(col.getColumnName());
            if (null != cCat)
            {
               List<DeployedModel> models = ModelCache.findModelCache().getActiveModels();
               boolean filterAuxiliaryProcesses = filterAuxiliaryProcesses();
               for (DeployedModel activeModel : models)
               {
                  List<ProcessDefinition> procDefs = ProcessDefinitionUtils.getAllProcessDefinitions(activeModel,
                        filterAuxiliaryProcesses);
                  for (ProcessDefinition procDef : procDefs)
                  {
                     updateCriticalityStatisticsMap(processCriticalityStatisticsMap, procDef, cCat);
                  }
               }
            }
         }
      }

      initializeTree();
   }

   /**
    * Refreshes the page
    */
   public void update()
   {
      initialize();
      if (activityTableVisible)
      {
         activityTableHelper.getActivityTable().refresh(true);
      }
   }

   /**
    * 
    */
   public void initializeTree()
   {
      List<TreeTableNode> modelNodes = rootModelNode.getChildren();
      if (!CollectionUtils.isEmpty(modelNodes))
      {
         for (TreeTableNode child : modelNodes)
         {
            ((CriticalityOverviewNodeObject) child.getUserObject()).initialize();
         }
      }
   }

   /**
    * @param pcsMap
    * @param pDef
    * @param cCat
    */
   private void updateCriticalityStatisticsMap(Map<String, Map<String, CriticalityStatistics>> pcsMap,
         ProcessDefinition pDef, CriticalityCategory cCat)
   {
      Map<String, CriticalityStatistics> criticaliyStatisticsMap = pcsMap.get(pDef.getQualifiedId());
      if (null == criticaliyStatisticsMap)
      {
         criticaliyStatisticsMap = new HashMap<String, CriticalityStatistics>();
         pcsMap.put(pDef.getQualifiedId(), criticaliyStatisticsMap);
      }

      CriticalityStatisticsQuery query = CriticalityStatisticsQuery.forProcesses(pDef);
      query.where(ActivityInstanceQuery.CRITICALITY.between(
            CriticalityConfigurationUtil.getEngineCriticality(cCat.getRangeFrom()),
            CriticalityConfigurationUtil.getEngineCriticality(cCat.getRangeTo())));
      QueryService queryService = SessionContext.findSessionContext().getServiceFactory().getQueryService();
      criticaliyStatisticsMap.put(cCat.getLabel(), (CriticalityStatistics) queryService.getAllActivityInstances(query));
   }

   /**
    * @param criticalityTableEntry
    * @param parent
    */
   private void buildCriticalityOverviewTree(ICriticalityMgrTableEntry criticalityTableEntry, TreeTableNode parent)
   {
      List<ICriticalityMgrTableEntry> listChildren = criticalityTableEntry.getChildren();
      if (listChildren != null)
      {
         Iterator<ICriticalityMgrTableEntry> itChildren = listChildren.iterator();
         while (itChildren.hasNext())
         {
            ICriticalityMgrTableEntry childEntry = itChildren.next();

            TreeTableNode node = TreeNodeFactory.createTreeNode(treeTable, this, childEntry, false);
            parent.add(node);

            buildCriticalityOverviewTree(childEntry, node);
         }
      }
   }

   /**
    * 
    * @param item
    * @param searchHandler
    */
   public void setDetailViewProperties(ISearchHandler searchHandler)
   {
      activityTableVisible = true;
      activityTableHelper.getActivityTable().setISearchHandler(new WrapSearchHandler<ActivityInstance>(searchHandler));
      refreshActivityTable();
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
   private void refreshActivityTable()
   {
      activityTableHelper.getActivityTable().refresh(
            new DataTableSortModel<ActivityInstanceWithPrioTableEntry>("startTime", false));
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         activityTableVisible = false;
         processFilterToolbarItems = new ArrayList<FilterToolbarItem>();
         FilterToolbarItem auxiliaryProcess = new FilterToolbarItem("" + 0, "auxiliaryProcess",
               "processHistory.processTable.showAuxiliaryProcess", "processHistory.processTable.hideAuxiliaryProcess",
               "process_auxiliary.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
         auxiliaryProcess.setActive(false);
         processFilterToolbarItems.add(auxiliaryProcess);

         FilterToolbarItem auxiliaryActivity = new FilterToolbarItem("" + 1, "auxiliaryActivity",
               "processHistory.processTable.showAuxiliaryActivities",
               "processHistory.processTable.hideAuxiliaryActivities", "activity_auxiliary.png",
               Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
         auxiliaryActivity.setActive(false);
         processFilterToolbarItems.add(auxiliaryActivity);

         initializeDataFilters();
         isActivated = false;
      }
      if (ViewEventType.ACTIVATED == event.getType())
      {
         if (!isActivated)
         {
            initialize();
            initializeActivityTable();
            isActivated = true;
         }
      }
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

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.treetable.TreeTableBean#getIncreasedLabelCount()
    */
   public int getIncreasedLabelCount()
   {
      return 0;
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.treetable.TreeTableBean#setSelectedNodeLabel(java.lang.String)
    */
   public void setSelectedNodeLabel(String label)
   {
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.treetable.TreeTableBean#setSelectedNodeObject(org.eclipse.stardust.ui.web.common.treetable.NodeUserObject)
    */
   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {
   }

   /**
    * @return
    */
   public TreeTable getTreeTable()
   {
      return treeTable;
   }

   /**
    * @param event
    */
   public void filterTable(ActionEvent event)
   {
      UICommand commandObject = (UICommand) event.getComponent();
      @SuppressWarnings("unchecked")
      Map<String, Object> attributesMap = commandObject.getAttributes();

      String filterName = (String) attributesMap.get("name");

      FilterToolbarItem filterToolbarItem = getFilterToolbarItem(filterName);
      filterToolbarItem.toggle();

      // Update Data Filters
      ITableDataFilterOnOff onOffFilter = (ITableDataFilterOnOff) getOnOffFilters().getDataFilter(filterName);
      onOffFilter.toggle();

      // treeTable.rebuildList();
      initialize();
   }

   /**
    * @param criticalityCategoryList
    * @param columnSelectionListener
    */
   public void initializeCriticalityTable(List<CriticalityCategory> criticalityCategoryList, CriticalityMgrColumnModelListener columnSelectionListener)
   {
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      ColumnPreference nameCol = new ColumnPreference("Name", "name", ColumnDataType.STRING, this.getMessages()
            .getString("criticalityTable.modelElementCol.label"), new TableDataFilterPopup(new TableDataFilterSearch()));
      fixedCols.add(nameCol);

      List<ColumnPreference> selectableColumns = new ArrayList<ColumnPreference>();
      
      for (int i = 0; i < criticalityCategoryList.size(); i++)
      {
         CriticalityCategory cCat = criticalityCategoryList.get(i);
         ColumnPreference criticalityCol = new ColumnPreference(cCat.getLabel(), "", cCat.getLabel(),
               ResourcePaths.V_activityCriticalityMgrDynamicColumns, false, false);
         criticalityCol.setColumnAlignment(ColumnAlignment.CENTER);
         if (i < DEFAULT_COLUMNS_TO_DISPLAY)
         {
            criticalityCol.setVisible(true);
         }
         selectableColumns.add(criticalityCol);
      }

      ColumnPreference performerCol = new ColumnPreference("PerformerName", "", this.getMessages().getString(
            "criticalityTable.roleCol.label"), ResourcePaths.V_activityCriticalityMgrColumns, true, false);
      performerCol.setColumnAlignment(ColumnAlignment.CENTER);
      selectableColumns.add(performerCol);
      DefaultColumnModel activityCriticalityManagerColumnModel = new DefaultColumnModel(selectableColumns, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_ACTIVITY_CRITICALITY_VIEW, columnSelectionListener);
      TableColumnSelectorPopup columnSelectorPopup = new TableColumnSelectorPopup(activityCriticalityManagerColumnModel);

      rootModelNode = TreeNodeFactory.createTreeNode(treeTable, this, new ModelDefCriticalityMgrTableEntry(), false);
      // Now Create a Model & Tree Table
      DefaultTreeModel model = new DefaultTreeModel(rootModelNode);
      treeTable = new TreeTable(model, columnSelectorPopup, getOnOffFilters());
      treeTable.setDataTableExportHandler(new ActivityCriticalityManagerTableExportHandler());

      treeTable.setFilterRootNode(false);
      treeTable.setHideRootNode(true);
      rootModelNode.getUserObject().setTreeTable(treeTable);
      treeTable.setTooltipURL(org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths.V_PANELTOOLTIP_URL);
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

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.table.paginator.spi.ISearchHandler#buildQuery()
       */
      public IQuery buildQuery()
      {
         return new IppQuery(searchHandler.createQuery());
      }

      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.table.paginator.spi.ISearchHandler#performSearch
       * (org.eclipse.stardust.ui.web.common.table.paginator.spi.IQuery, int, int)
       */
      @SuppressWarnings("unchecked")
      public IQueryResult<E> performSearch(IQuery query, int startRow, int pageSize)
      {
         return new IppQueryResult<E>(searchHandler.performSearch(((IppQuery) query).getQuery()));
      }
   }

   /**
    * @author Shrikant.Gangal
    *
    */
   private class ActivityCriticalityManagerTableExportHandler implements DataTableExportHandler<TreeTableUserObject>
   {
      /*
       * (non-Javadoc)
       * 
       * @see
       * org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport
       * (org.eclipse.stardust.ui.web.common.table.export.ExportType,
       * org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object,
       * java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column, TreeTableUserObject rowObj,
            Object value)
      {
         CriticalityOverviewNodeObject row = (CriticalityOverviewNodeObject) rowObj;
         if (null != row)
         {
            if ("Name".equals(column.getColumnName()))
            {
               return row.getName();
            }
            else if ("PerformerName".equals(column.getColumnName()))
            {
               return row.getDefaultPerformerName();
            }
            else
            {
               Map<String, CriticalityDetails> cMap = row.getCriticalityDetailsMap();
               if (null != cMap && null != cMap.get(column.getColumnName()))
               {
                  return "" + cMap.get(column.getColumnName()).getCount();
               }
            }
         }
         return "";
      }

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
   }

   /**
    * 
    */
   public void initializeDataFilters()
   {
      onOffFilters = new TableDataFilters();
      for (FilterToolbarItem filterToolbarItem : processFilterToolbarItems)
      {
         onOffFilters.addDataFilter(new TableDataFilterOnOff(filterToolbarItem.getName(), filterToolbarItem.getName(),
               null, true, false));
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
         activityTableHelper.initActivityTable();
         activityTableHelper.setCallbackHandler(this);
         activityTableHelper.setStrandedActivityView(false);
         activityTableHelper.getActivityTable().initialize();
      }
   }

   public TableDataFilters getOnOffFilters()
   {
      return onOffFilters;
   }

   public boolean isActivityTableVisible()
   {
      return activityTableVisible;
   }

   public ActivityTableHelper getActivityTableHelper()
   {
      return activityTableHelper;
   }

   public List<FilterToolbarItem> getProcessFilterToolbarItems()
   {
      return processFilterToolbarItems;
   }

   public void setProcessFilterToolbarItems(List<FilterToolbarItem> processFilterToolbarItems)
   {
      this.processFilterToolbarItems = processFilterToolbarItems;
   }
}
