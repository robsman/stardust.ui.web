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
/**
 * 
 */
package org.eclipse.stardust.ui.web.bcc.views;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.swing.tree.DefaultTreeModel;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.legacy.ITimeProvider;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.InstanceManager;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.ModelManager;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.ModelTreeItem;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.ProcessProgressInstance;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.PropertyProvider;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.TimeElements;
import org.eclipse.stardust.ui.web.bcc.legacy.gantt.TimeUnit;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModelListener;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterListener;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.treetable.NodeUserObject;
import org.eclipse.stardust.ui.web.common.treetable.TreeTable;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableBean;
import org.eclipse.stardust.ui.web.common.treetable.TreeTableNode;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;

import com.icesoft.faces.async.render.IntervalRenderer;
import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;



/**
 * @author Ankita.Patel
 * 
 */
public class GanttChartManagerBean extends UIViewComponentBean
      implements TreeTableBean,ITableDataFilterListener,
      Renderable, ResourcePaths
{
   private static final long serialVersionUID = 1L;

   private static Logger logger = LogManager.getLogger(GanttChartManagerBean.class);

   private RenderManager renderManager;

   private PersistentFacesState state;

   private int processState = ProcessProgressInstance.PROCESS_STATE_ALL;

   private long startingPoint;

   private ModelManager modelManager;

   private InstanceManager instanceManager;

   private MessagesBCCBean propsBean;

   private SessionContext sessionCtx;

   private SortedSet <ModelTreeItem> children;

   private Map<String, Boolean> visibilityMap = null;

   private TimeElements timeElements;

   private long refreshInterval = Integer.MAX_VALUE;

   private double now;

   private int nowHeight;

   private int timeUnit = 2;

   private boolean showStatusBar = true;

   private boolean showDependencyBar = true;

   private boolean showInfoColumn = true;

   private boolean showActiveInstances = true;

   private boolean showCompletedInstances = true;

   private boolean showPercentageValue = true;

   private DefaultTreeModel model;

   private TreeTableNode rootModelNode;

   private TreeTable treeTable;

   private Long currentProcessOID;

   private IColumnModel ganttChartColumnModel;

   private TableDataFilters dataFilters;

   private List<SelectItem> timeElementsTypes = new ArrayList<SelectItem>();

   /**
    * 
    */
   public GanttChartManagerBean()
   {
      super(ResourcePaths.V_ganttChartView);
      setBasePath("..");
      propsBean = MessagesBCCBean.getInstance();
      sessionCtx = SessionContext.findSessionContext();
      state = PersistentFacesState.getInstance();
      this.visibilityMap = new HashMap();

      initializeColumnFilters();
      initializeDataFilters();
      ganttChartColumnModel.initialize();

      timeElementsTypes.add(new SelectItem(new Integer(0), this.getMessages().getString(
            "timeunit.options.minute")));
      timeElementsTypes.add(new SelectItem(new Integer(1), this.getMessages().getString(
            "timeunit.options.hour")));
      timeElementsTypes.add(new SelectItem(new Integer(2), this.getMessages().getString(
            "timeunit.options.day")));
      timeElementsTypes.add(new SelectItem(new Integer(3), this.getMessages().getString(
            "timeunit.options.week")));
      timeElementsTypes.add(new SelectItem(new Integer(4), this.getMessages().getString(
            "timeunit.options.month")));
      timeElementsTypes.add(new SelectItem(new Integer(5), this.getMessages().getString(
            "timeunit.options.year")));
      timeElementsTypes.add(new SelectItem(new Integer(6), this.getMessages().getString(
            "timeunit.options.decade")));

      // createGanttChart();

   }


   /**
    * Update the Page
    */
   public void update()
   {
      createGanttChart();
   }

   /**
    * Handles disable/enable info column
    * 
    * @param event
    */
   public void toggleInfoColumn(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      if (!showInfoColumn)
      {
         ganttChartColumnModel.getColumn("Info").setVisible(false);
         ganttChartColumnModel.initialize();
      }
      else
      {
         ganttChartColumnModel.getColumn("Info").setVisible(true);
         ganttChartColumnModel.initialize();
      }
   }

   /**
    * Show/Hide Active processes
    * 
    * @param event
    */
   public void updateForAI(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }

      processState = getProcessState();
      createGanttChart();

   }

   /**
    * Show/Hide completed processes
    * 
    * @param event
    */
   public void updateForCompletedAI(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      processState = getProcessState();
      createGanttChart();
   }

   /**
    * Updates gantt chart table as Time elements are changed
    * 
    * @param evt
    */
   public void updateTimeElements(ValueChangeEvent evt)
   {
      if ((evt.getNewValue()) == null)
      {
         return;
      }
      else
      {
         timeUnit = ((Integer) evt.getNewValue()).intValue();
      }
      // sessionCtx.bind("processInstanceOid", this.currentProcessOID);
      instanceManager = new InstanceManager();

      modelManager = new ModelManager(processState);
      ModelTreeItem rootItem = modelManager.retrieveProcessHierarchy(instanceManager);

      long minPlannedStartTime = rootItem.getMinPlannedStartTime(Long.MAX_VALUE);
      long minStartTime = rootItem.getMinStartTime(Long.MAX_VALUE);
      startingPoint = minPlannedStartTime != 0 ? Math.min(minPlannedStartTime,
            minStartTime) : minStartTime;
      initializeTimeElements();

      rootItem.updateTimeUnit(getTimeUnitType());
      updateTimeUnit(rootItem);
      rootItem.setStartingPoint(startingPoint, rootItem);
      this.visibilityMap.put(rootItem.getRoot().getNameAsKey(), Boolean.TRUE);
      rootItem.initializeVisibilityState(this.visibilityMap, Boolean.TRUE);
      rootModelNode = TreeNodeFactory
            .createModelTreeNode(treeTable, this, rootItem, true);

      model = new DefaultTreeModel(rootModelNode);
      treeTable = new TreeTable(model, ganttChartColumnModel, dataFilters);
      // treeTable.setTreeModel(model);
      treeTable.setAutoFilter(false);
      rootModelNode.getUserObject().setTreeTable(treeTable);
      traverseTreeItemTree(rootItem, new HashMap(), rootModelNode);
      // treeTable.initialize();
      treeTable.rebuildList();
   }

   /**
    * Initializes Data Filters
    */
   private void initializeDataFilters()
   {
      dataFilters = new TableDataFilters();
      dataFilters.addDataFilter(new TableDataFilterSearch("Name", null, true, ""));
   }

   /**
    * Initializes Column filters
    */
   private void initializeColumnFilters()
   {
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      fixedCols.add(new ColumnPreference("Name", "name", ColumnDataType.STRING, propsBean
            .getString("views.processOverviewView.priorityTable.column.name"),
            new TableDataFilterPopup(new TableDataFilterSearch())));

      List<ColumnPreference> selectableColumns = new ArrayList<ColumnPreference>();
      ColumnPreference infoColumn = new ColumnPreference("Info", "", this.getMessages()
            .getString("column.info"), V_ganttChartColumns, true, false);
      infoColumn.setNoWrap(true);
      ColumnPreference progressColumn = new ColumnPreference("ProgressBar", "", this
            .getMessages().getString("column.progressBar"), V_ganttChartColumns, true,
            false);
      progressColumn.setNoWrap(true);

      selectableColumns.add(infoColumn);
      selectableColumns.add(progressColumn);

      ganttChartColumnModel = new DefaultColumnModel(selectableColumns, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_GNATT_CHART);
   }

   /**
    * Prepares tree for Gantt chart view
    */
   public void createGanttChart()
   {
      if (null==currentProcessOID)
         return;

      try
      {
         sessionCtx.bind("processInstanceOid", this.currentProcessOID);
         instanceManager = new InstanceManager();
         if (instanceManager.getProcessInstanceOid() == null
               || instanceManager.getProcesses() == null
               || instanceManager.getProcesses().isEmpty())
         {
            MessageDialog.addErrorMessage(this.getMessages().get("loadProcessInstanceError"));
         }
         else
         {
            modelManager = new ModelManager(getProcessState());
            ModelTreeItem root = modelManager.retrieveProcessHierarchy(instanceManager);
            if (root != null && instanceManager.getProcessInstanceOid() != null)
            {
               long minPlannedStartTime = root.getMinPlannedStartTime(Long.MAX_VALUE);
               long minStartTime = root.getMinStartTime(Long.MAX_VALUE);

               startingPoint = minPlannedStartTime != 0 ? Math.min(minPlannedStartTime,
                     minStartTime) : minStartTime;
               initializeTimeElements();

               TimeUnit timeUnitType = getTimeUnitType();
               timeUnitType.calculateStartDate(startingPoint);
               root.updateTimeUnit(timeUnitType);
               root.setStartingPoint(startingPoint, root);
               this.visibilityMap.put(root.getRoot().getNameAsKey(), Boolean.TRUE);
               root.initializeVisibilityState(this.visibilityMap, Boolean.TRUE);

               rootModelNode = TreeNodeFactory.createModelTreeNode(treeTable, this, root,
                     true);

               // Now Create a Model & Tree Table
               model = new DefaultTreeModel(rootModelNode);
               treeTable = new TreeTable(model, ganttChartColumnModel, dataFilters);
               treeTable.setAutoFilter(false);
               // treeTable.setTreeModel(model);
               rootModelNode.getUserObject().setTreeTable(treeTable);

               // Build the tree
               this.traverseTreeItemTree(root, new HashMap(), rootModelNode);
               // treeTable.initialize();
               treeTable.rebuildList();
            }
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param visibilityMap
    * @param visible
    */
   private void initializeVisibilityState(Map visibilityMap, Boolean visible)
   {
      for (Iterator _iterator = this.children.iterator(); _iterator.hasNext();)
      {
         ModelTreeItem item = (ModelTreeItem) _iterator.next();
         String key = item.getRoot().getNameAsKey();
         visibilityMap.put(key, visible);
      }
   }

   /**
    * traverse the tree
    * 
    * @param item
    * @param dependencyMap
    * @param parent
    */
   private void traverseTreeItemTree(ModelTreeItem item, Map dependencyMap,
         TreeTableNode parent)
   {
      item.setVisible((Boolean) visibilityMap.get(item.getRoot().getNameAsKey()));
      this.computeDependencyHeight(item, dependencyMap);

      for (Iterator _iterator = item.getChildren().iterator(); _iterator.hasNext();)
      {
         ModelTreeItem child = (ModelTreeItem) _iterator.next();
         child.setVisible((Boolean) visibilityMap.get(child.getRoot().getNameAsKey()));
         if (child.isVisible())
         {
            TreeTableNode node = TreeNodeFactory.createModelTreeNode(treeTable, this,
                  child, true);
            parent.add(node);
            this.traverseTreeItemTree(child, dependencyMap, node);
         }
         else
         {
            List/* <ModelTreeItem> */chidren = new ArrayList/* <ModelTreeItem> */();
            chidren = child.getAllChildren(chidren);
            for (Iterator _i = chidren.iterator(); _i.hasNext();)
            {
               ModelTreeItem mti = (ModelTreeItem) _i.next();
               if (dependencyMap.containsKey(mti.getRoot().getBusinessId()))
               {
                  ModelTreeItem mti_tmp = (ModelTreeItem) dependencyMap.get(mti.getRoot()
                        .getBusinessId());
                  mti_tmp.getProgressStatus().removeDependencyHeight();

                  dependencyMap.remove(mti.getRoot().getBusinessId());
               }
            }
         }
      }
   }

   /**
    * computes the dependecy height for progress bar
    * 
    * @param item
    * @param dependencyMap
    */
   private void computeDependencyHeight(ModelTreeItem item, Map dependencyMap)
   {
      if (logger.isDebugEnabled())
      {
         logger.debug("Traverse node: " + item.getRoot().getBusinessId());
      }

      if (dependencyMap.containsKey(item.getRoot().getBusinessId()))
      {
         ModelTreeItem modelTreeItem = (ModelTreeItem) dependencyMap.get(item.getRoot()
               .getBusinessId());
         
         if (logger.isDebugEnabled())
         {
            logger.debug("Found dependent node: " + modelTreeItem.getRoot().getBusinessId());
            logger.debug("Compute dependency hight ("
                  + modelTreeItem.getProgressStatus().getDependencyHeight() + "): "
                  + modelTreeItem.getRoot().getBusinessId());
            logger.debug("Remove dependent node: " + modelTreeItem.getRoot().getBusinessId());
         }
         
         dependencyMap.remove(modelTreeItem.getRoot().getSuccessorId());
         item.getProgressStatus().setPredecessorNode(modelTreeItem.getProgressStatus());
      }
      else
      {
         if (item.getRoot().getSuccessorId() != null
               && !"".equals(item.getRoot().getSuccessorId()))
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("Found node with successor: " + item.getRoot().getBusinessId());
            }
            item.getProgressStatus().setDependencyHeight(0);
            dependencyMap.put(item.getRoot().getSuccessorId(), item);
         }
      }

      for (Iterator _iterator = dependencyMap.values().iterator(); _iterator.hasNext();)
      {
         ModelTreeItem modelTreeItem = (ModelTreeItem) _iterator.next();
         if (modelTreeItem.isVisible())
         {
            if (logger.isDebugEnabled())
            {
               logger.debug("Compute dependency height ("
                     + modelTreeItem.getProgressStatus().getDependencyHeight() + "): "
                     + modelTreeItem.getRoot().getBusinessId());
            }
            modelTreeItem.getProgressStatus().addDependencyHeight();
         }
      }

   }

   /**
    * Initializes Time elements
    */
   private void initializeTimeElements()
   {
      TimeUnit timeUnitType = getTimeUnitType();
      Calendar calculatedStartDate = timeUnitType.calculateStartDate(startingPoint);
      ITimeProvider itemTimeProvider = (ITimeProvider) Reflect
            .createInstance((PropertyProvider.getInstance().getTimeProviderClassName()));
      BigDecimal nowBD = new BigDecimal(new Long(itemTimeProvider.getCurrentTime()
            - calculatedStartDate.getTimeInMillis()).doubleValue());
      this.now = nowBD.divide(new BigDecimal(1000), 1, BigDecimal.ROUND_HALF_UP).divide(
            new BigDecimal(60), 1, BigDecimal.ROUND_HALF_UP).doubleValue();
      this.timeElements = new TimeElements(calculatedStartDate, timeUnitType);
   }

   /**
    * Update Time Unit
    * 
    * @param rootItem
    */
   private void updateTimeUnit(ModelTreeItem rootItem)
   {
      for (Iterator iterator = rootItem.getChildrenList().iterator(); iterator.hasNext();)
      {
         ModelTreeItem child = (ModelTreeItem) iterator.next();
         child.updateTimeUnit(getTimeUnitType());
         updateTimeUnit(child);
      }
   }

   // ************* Modified GETTER SETTER METHODS **********************

   private int getProcessState()
   {
      processState = ProcessProgressInstance.PROCESS_STATE_NONE;
      if (showCompletedInstances && showActiveInstances)
      {
         processState = ProcessProgressInstance.PROCESS_STATE_ALL;
      }
      else if (showCompletedInstances)
      {
         processState = ProcessProgressInstance.PROCESS_STATE_COMPLETED;
      }
      else if (showActiveInstances)
      {
         processState = ProcessProgressInstance.PROCESS_STATE_ACTIVE;
      }
      return processState;
   }

   public TimeUnit getTimeUnitType()
   {
      TimeUnit timeUnitType = null;
      switch (timeUnit)
      {
      case 0:
         timeUnitType = TimeUnit.MINUTE;
         break;
      case 1:
         timeUnitType = TimeUnit.HOUR;
         break;
      case 2:
         timeUnitType = TimeUnit.DAY;
         break;
      case 3:
         timeUnitType = TimeUnit.WEEK;
         break;
      case 4:
         timeUnitType = TimeUnit.MONTH;
         break;
      case 5:
         timeUnitType = TimeUnit.YEAR;
         break;
      case 6:
         timeUnitType = TimeUnit.DECADE;
         break;
      }
      return timeUnitType;
   }

   /**
    * Gets the current height
    * 
    * @return
    */
   public int getNowHeight()
   {
      nowHeight = 0;
      for (Iterator _iterator = this.visibilityMap.values().iterator(); _iterator
            .hasNext();)
      {
         if (((Boolean) _iterator.next()).booleanValue())
         {
            nowHeight = nowHeight + 38;
         }
      }
      return nowHeight;
   }

   /**
    * Updates the Gantt chart when activity instance is enabled or disabled
    * 
    * @param showActiveInstances
    */
   public void setShowActiveInstances(boolean showActiveInstances)
   {
      this.showActiveInstances = showActiveInstances;
   }

   /**
    * Updates the Gantt chart when complete activity instance is enabled or disabled
    * 
    * @param showCompletedInstances
    */
   public void setShowCompletedInstances(boolean showCompletedInstances)
   {
      this.showCompletedInstances = showCompletedInstances;
   }

   // ************* DEFAULT GETTER SETTER METHODS **********************

   public int getTimeUnit()
   {
      return timeUnit;
   }

   public void setTimeUnit(int timeUnit)
   {
      this.timeUnit = timeUnit;
   }

   public TimeElements getTimeElements()
   {
      return timeElements;
   }

   public void setTimeElements(TimeElements timeElements)
   {
      this.timeElements = timeElements;
   }

   public boolean isShowStatusBar()
   {
      return showStatusBar;
   }

   public void setShowStatusBar(boolean showStatusBar)
   {
      this.showStatusBar = showStatusBar;
   }

   public boolean isShowDependencyBar()
   {
      return showDependencyBar;
   }

   public void setShowDependencyBar(boolean showDependencyBar)
   {
      this.showDependencyBar = showDependencyBar;
   }

   public long getNow()
   {
      return getTimeUnitType().calculateSize(this.now);
   }

   @Override
   public void initialize()
   {

   }

   public int getIncreasedLabelCount()
   {
      return 0;
   }

   public void setSelectedNodeLabel(String label)
   {

   }

   public void setSelectedNodeObject(NodeUserObject nodeObject)
   {

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

   public boolean isShowInfoColumn()
   {
      return showInfoColumn;
   }

   public void setShowInfoColumn(boolean showInfoColumn)
   {
      this.showInfoColumn = showInfoColumn;
   }

   public boolean isShowActiveInstances()
   {
      return showActiveInstances;
   }

   public boolean isShowCompletedInstances()
   {
      return showCompletedInstances;
   }

   public boolean isShowPercentageValue()
   {
      return showPercentageValue;
   }

   public void setShowPercentageValue(boolean showPercentageValue)
   {
      this.showPercentageValue = showPercentageValue;
   }

   

   public void applyFilter(TableDataFilters tableDataFilters)
   {

   }

   public List<SelectItem> getTimeElementsTypes()
   {
      return timeElementsTypes;
   }

   public void setTimeElementsTypes(List<SelectItem> timeElementsTypes)
   {
      this.timeElementsTypes = timeElementsTypes;
   }

   public long getRefreshInterval()
   {
      return (0 < refreshInterval) ? new Long(refreshInterval) : new Long(
            Integer.MAX_VALUE);
   }

   public void setRefreshInterval(long refreshInterval)
   {
      this.refreshInterval = refreshInterval;
   }

   public PersistentFacesState getState()
   {
      return state;
   }

   public void renderingException(RenderingException arg0)
   {

   }

   public RenderManager getRenderManager()
   {
      return renderManager;
   }

   public void setRenderManager(RenderManager renderManager)
   {
      this.renderManager = renderManager;
      IntervalRenderer intRend = renderManager.getIntervalRenderer(this.toString());
      intRend.setInterval(refreshInterval);
      intRend.add(this);
      intRend.requestRender();
   }

   public Long getCurrentProcessOID()
   {
      return currentProcessOID;
   }

   public void setCurrentProcessOID(Long currentProcessOID)
   {
      this.currentProcessOID = currentProcessOID;
   }

   /*
    * public String getProcessId() { String processId = null; ProcessInstance
    * processInstance = null;
    * 
    * ProcessInstanceQuery query = ProcessInstanceQuery.findAll();
    * query.getFilter().add(ProcessInstanceQuery.OID.isEqual(currentProcessOID)); try {
    * WorkflowFacade facade = WorkflowFacade.getWorkflowFacade(); Iterator pIter =
    * facade.getAllProcessInstances(query).iterator(); if (pIter.hasNext()) {
    * processInstance = (ProcessInstance) pIter.next(); }
    * 
    * processId = processInstance.getProcessID();
    * 
    * } catch (InvalidServiceException e) { // ignore } return processId;
    * 
    * }
    */

}
