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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ActivityDetails;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.IDescriptorProvider;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.core.preferences.IPreferenceStorageManager;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.InvalidServiceException;
import org.eclipse.stardust.ui.web.bcc.jsf.PageMessage;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.AggregateActivityColumnItem;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.Category;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.CategoryRowItem;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.IColumnItem;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.IRowItem;
import org.eclipse.stardust.ui.web.bcc.legacy.traffic.TrafficLightViewPropertyProvider;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityInstanceWithPrioTableEntry;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.DefaultColumnModelEventHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Giridhara.G
 * @version $Revision: $
 */

public class TrafficLightViewManagerBean extends UIComponentBean
      implements ResourcePaths, ICallbackHandler,  ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager
         .getLogger(TrafficLightViewManagerBean.class);
   
   protected final static int ACTIVITY_INSTANCE_STATE_ALIVE = 1;

   protected final static int ACTIVITY_INSTANCE_STATE_COMPLETED = 2;

   private final static String UNDEFINED_ROW_ITEM_ID = "undefined";

   private final static String TOTAL_ROW_ITEM_ID = "total";

   public final static String COLUMN_MODEL_ID = "trafficLightViewManager/columnDataModel";

   public final static String DATA_ROW_MODEL = "trafficLightViewManager/dataRowModel";

   private SessionContext sessionCtx;

   private String processId;

   private String categoryFilter = null;

   private List/* <ActivityType> */activityList;

   private SortableTable<TrafficLightViewUserObject> trafficLightViewTable;

   private List<TrafficLightViewUserObject> trafficLightViewList;

   private SelectItem[] processSelectItem;

   private SelectItem[] categories;

   private Map tempTableDataMap = null;

   private boolean activityTable;

   private boolean allowConfiguration;

   private MessagesBCCBean propsBean;
   
   private WorkflowFacade facade;  
   
   private ActivityTableHelper notPassedActivityHelper;
   private ActivityTableHelper passedActivityHelper;
   
   private String selectedProcessActivityId;
   
   private String selectedProcessActivityName;
   
   private Map<Long, ProcessInstance> processInstances;
   
   private boolean passedActivityTableInitialized = false;
   private boolean notPassedActivityTableInitialized = false;

   
   public TrafficLightViewManagerBean()
   {
      super("trafficLightView");
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {       
         init();
      }
      if (ViewEventType.CLOSED == event.getType())
      {       
         clear();
      }

   }

public void init()
   {

      propsBean = MessagesBCCBean.getInstance();
      allowConfiguration = Parameters.instance().getBoolean(
            IPreferenceStorageManager.PRP_USE_DOCUMENT_REPOSITORY, false);
      sessionCtx = SessionContext.findSessionContext();
      facade = WorkflowFacade.getWorkflowFacade();

      Iterator iterator = TrafficLightViewPropertyProvider.getInstance().getAllProcessDefinitionIDs().iterator();
      this.processId = iterator.hasNext() ? (String) iterator.next() : "";
      selectedProcessActivityId=null;
      initialize();
   }
   
   /**
    * Refresh the table
    */
   public void update()
   {
      initialize();
      if(passedActivityTableInitialized && notPassedActivityTableInitialized)
      {
      passedActivityHelper.refreshActivityTable();
      notPassedActivityHelper.refreshActivityTable();
      }
   }

   /**
    * Used to get the currentInstance
    * 
    * @return currentInstance
    */
   public static TrafficLightViewManagerBean getCurrent()
   {
      return (TrafficLightViewManagerBean) FacesContext.getCurrentInstance()
            .getApplication().getVariableResolver().resolveVariable(
                  FacesContext.getCurrentInstance(), "trafficLightViewManagerBean");
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {
      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      
      ColumnPreference colCategory = new ColumnPreference("Category", "categoryName",
            ColumnDataType.STRING, this.getMessages().getString("column.category"), true,
            true);
      colCategory.setColumnAlignment(ColumnAlignment.CENTER);

      fixedCols.add(colCategory);

      long startingDate = System.currentTimeMillis();

      activityList = getAllColumnActivities();
      Map tableDataMap = this.createModel();
      tempTableDataMap = new HashMap(tableDataMap);

      this.bindModelData(tableDataMap, startingDate);

      List tableData = getTableData(tableDataMap.values(), activityList);

      ActivityDetails activityDetails;
      
      List<ColumnPreference> selCols = new ArrayList<ColumnPreference>();
      for (int i = 0; i < activityList.size(); i++)
      {
         activityDetails = (ActivityDetails) activityList.get(i);
         ColumnPreference colActivity = new ColumnPreference(processId + "Activity" + i,
               "trafficLightViewDynamicUserObjectList[" + i + "].symbolName",
               I18nUtils.getActivityName(activityDetails), V_trafficLightViewColumns, true, false);

         colActivity.setColumnAlignment(ColumnAlignment.CENTER);
         
         selCols.add(colActivity);
      }
      
      if (tableData != null && tableData.size() > 0)
      {
         trafficLightViewList = new ArrayList<TrafficLightViewUserObject>();
         List<TrafficLightViewDynamicUserObject> trafficLightDyna;
         for (int i = 0; i < tableData.size(); i++)
         {
            CategoryRowItem categoryRowItem = null;
            AggregateActivityColumnItem aggregateActivityColumnItem = null;
            trafficLightDyna = new ArrayList<TrafficLightViewDynamicUserObject>();
            Object obj[] = ((Object[]) tableData.get(i));
            String categoryValue = "";
            for (int j = 0; j < obj.length; j++)
            {
               if (obj[j] instanceof CategoryRowItem)
               {
                  categoryRowItem = (CategoryRowItem) obj[j];
                  categoryValue = categoryRowItem.getCategoryValue();

               }
               else if (obj[j] instanceof AggregateActivityColumnItem)
               {
                  aggregateActivityColumnItem = (AggregateActivityColumnItem) obj[j];

                  trafficLightDyna.add(new TrafficLightViewDynamicUserObject(aggregateActivityColumnItem.getId(),
                        aggregateActivityColumnItem.getQualifiedId(), Integer.toString(aggregateActivityColumnItem
                              .getCompleted()), aggregateActivityColumnItem.getSymbolUrl(), aggregateActivityColumnItem
                              .getSymbolName(), aggregateActivityColumnItem.getCompletedIcon(),
                        aggregateActivityColumnItem.isActivePIs(), categoryValue));
               }
            }

            trafficLightViewList.add(new TrafficLightViewUserObject(categoryRowItem
                  .getName(), Long.toString(categoryRowItem.getTotalCount()),
                  trafficLightDyna));
         }
      }
      DefaultColumnModelEventHandler columnModelEventHandler = new DefaultColumnModelEventHandler();
      IColumnModel columnModel = new DefaultColumnModel(selCols, fixedCols, null, UserPreferencesEntries.M_BCC,
            UserPreferencesEntries.V_TRAFFIC_LIGHT + processId, columnModelEventHandler);

      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      trafficLightViewTable = new SortableTable<TrafficLightViewUserObject>(
            colSelecpopup, null, new TrafficLightComparator(
                  "categoryName", true));
      trafficLightViewTable.setList(trafficLightViewList);
      
      columnModelEventHandler.setNeedRefresh(false);
      trafficLightViewTable.initialize();
      columnModelEventHandler.setNeedRefresh(true);
   }

   private void clear()
   {
      passedActivityTableInitialized = false;
      notPassedActivityTableInitialized = false;
   }

   /**
    * @return ProcessDefinition
    */
   private ProcessDefinition getProcessDefinition()
   {
      if (StringUtils.isNotEmpty(this.processId))
      {
         String modelId=ModelUtils.extractModelId(processId);
         Model model=ModelUtils.getActiveModel(modelId);
         ProcessDefinition processDefinition = model.getProcessDefinition(processId);
         return processDefinition;
      }
      return null;
   }

   /**
    * @return activities
    */
   private List<Activity> getAllColumnActivities()
   {
      List<Activity> activities = new ArrayList<Activity>();
      if(StringUtils.isEmpty(this.processId))
      {
         return activities;
      }
      
      Map<String,Activity> verifiedActivities = new HashMap<String,Activity>();
     
      List<String> validActivities = TrafficLightViewPropertyProvider.getInstance()
            .getAllColumnIDs(this.processId);
      String modelId=ModelUtils.extractModelId(processId);
      Model model=  ModelUtils.getActiveModel(modelId);
      if (validActivities.isEmpty())
      {        
         ProcessDefinition processDefinition = model.getProcessDefinition(processId);
         activities = processDefinition != null ? processDefinition.getAllActivities(): Collections.EMPTY_LIST;
      }
      else
      {
         List<ProcessDefinition> processDefinitions = model.getAllProcessDefinitions();      
         for (ProcessDefinition pDefType: processDefinitions)
         {        
            if (this.processId.equals(pDefType.getQualifiedId()))
            {
               List<Activity> actitivityList = pDefType.getAllActivities();
              
               for ( Activity activityType:actitivityList)
               { 
                  if (validActivities.contains(activityType.getId()))
                  {
                     verifiedActivities.put(activityType.getId(), activityType);
                  }
               }
            }
         }

        
         Activity activityType;
         for ( String activityId:validActivities)
         {
         
            activityType = (Activity) verifiedActivities.get(activityId);
            if (activityType != null)
            {
               activities.add(activityType);
            }
         }
      }
      return activities;
   }

   /**
    * @return Map
    */
   private Map createModel()
   {
      Map dataModel = new HashMap();
      ProcessDefinition processDefinition = getProcessDefinition();

      List/* <String> */columns = TrafficLightViewPropertyProvider.getInstance()
            .getAllColumnIDs(this.processId);
      List/* <String> */rows = TrafficLightViewPropertyProvider.getInstance()
            .getAllRowIDsAsList(this.processId, categoryFilter);

      String rowId;
      IRowItem rowItem;
      String columnId;
      Activity activity;
      String activityName;
      IColumnItem columnItem;
      for (int i = 0; i < rows.size(); i++)
      {
         rowId = (String) rows.get(i);

         rowItem = new CategoryRowItem(this.processId, categoryFilter, rowId,
               rowId);

         for (int j = 0; j < columns.size(); j++)
         {
            columnId = (String) columns.get(j);
            activity = processDefinition.getActivity(columnId);
            activityName = activity != null ? I18nUtils.getActivityName(activity) : columnId;
            columnItem = new AggregateActivityColumnItem(columnId, activity.getQualifiedId(), activityName, rowItem);
            rowItem.addColumnItem(columnItem);
         }

         dataModel.put(rowId, rowItem);
      }

      IRowItem undefinedRowItem = new CategoryRowItem(this.processId,
            UNDEFINED_ROW_ITEM_ID,  getMessages().getString("category.undefined"), UNDEFINED_ROW_ITEM_ID);

      for (int j = 0; j < columns.size(); j++)
      {
         columnId = (String) columns.get(j);
         activity = processDefinition.getActivity(columnId);
         activityName = activity != null ? I18nUtils.getActivityName(activity) : columnId;
         columnItem = new AggregateActivityColumnItem(columnId, activity.getQualifiedId(), activityName,
               undefinedRowItem);
         undefinedRowItem.addColumnItem(columnItem);
      }

      dataModel.put(undefinedRowItem.getId(), undefinedRowItem);

      IRowItem totalRowItem = new CategoryRowItem(this.processId, TOTAL_ROW_ITEM_ID,
            getMessages().getString("category.total"), TOTAL_ROW_ITEM_ID);

      for (int j = 0; j < columns.size(); j++)
      {
         columnId = (String) columns.get(j);
         activity = processDefinition.getActivity(columnId);
         activityName = activity != null ? I18nUtils.getActivityName(activity) : columnId;
         columnItem = new AggregateActivityColumnItem(columnId, activity.getQualifiedId(), activityName, totalRowItem);
         totalRowItem.addColumnItem(columnItem);
      }

      dataModel.put(totalRowItem.getId(), totalRowItem);

      return dataModel;

   }

   /**
    * @param dataModel
    * @param startingDate
    */
   private void bindModelData(Map dataModel, long startingDate)
   {
      List/* <String> */columns = TrafficLightViewPropertyProvider.getInstance()
            .getAllColumnIDs(this.processId);

      List/* <String> */rows = TrafficLightViewPropertyProvider.getInstance()
            .getAllRowIDsAsList(this.processId, categoryFilter);
      rows.add(UNDEFINED_ROW_ITEM_ID);

      ProcessInstances pInstances = getProcessInstances(processId, startingDate);
      ActivityInstances aInstances = getActivityInstances(pInstances, columns);

      Map totalCountMap = this.createTotalCountMap(pInstances);

      String categoryId = categoryFilter;

      IRowItem undefinedRowItem = (IRowItem) dataModel.get(UNDEFINED_ROW_ITEM_ID);
      String categoryValue;
      IRowItem rowItem;
      List /* <ProcessInstance> */processes;
      ActivityInstance aInstance;
      IColumnItem columnItem;
      Long totalCount;
      for (int i = 0; i < rows.size(); i++)
      {

         categoryValue = (String) rows.get(i);

         rowItem = (IRowItem) dataModel.get(categoryValue);

         processes = getActivePIs(pInstances, categoryId, categoryValue);

         if (rowItem != null && aInstances != null)
         {

            for (int a = 0; a < aInstances.size(); a++)
            {
               aInstance = (ActivityInstance) aInstances.get(a);
               String value = null;
               if (aInstance.getDescriptorValue(categoryId) != null)
                  value = ((Object) aInstance.getDescriptorValue(categoryId)).toString();

               if (value != null && categoryValue.equals(value))
               {
                  columnItem = rowItem.getColumnItem(aInstance.getActivity().getId());
                  if (ActivityInstanceState.Completed.equals(aInstance.getState()))
                  {
                     columnItem.addCompletedActivity();
                     columnItem.addActivityInstance(aInstance);
                  }
               }

               if (value == null && UNDEFINED_ROW_ITEM_ID.equals(categoryValue))
               {
                  columnItem = undefinedRowItem.getColumnItem(aInstance.getActivity()
                        .getId());
                  if (columnItem != null
                        && ActivityInstanceState.Completed.equals(aInstance.getState()))
                  {
                     columnItem.addCompletedActivity();
                     columnItem.addActivityInstance(aInstance);
                  }
                  
               }
            }
         }

         if (!UNDEFINED_ROW_ITEM_ID.equals(categoryValue))
         {
            totalCount = (Long) totalCountMap.get(rowItem.getCategoryValue());

            rowItem.setActivePIs(processes);
            rowItem.setTotalCount(totalCount);
            rowItem.calculateColumnStates();
         }
      }

      List /* <ProcessInstance> */undefinedPIs = getActivePIs(pInstances, categoryId,
            UNDEFINED_ROW_ITEM_ID);
      undefinedRowItem.setActivePIs(undefinedPIs);

      Long undefRowTotalNumber = (Long) totalCountMap.get(undefinedRowItem
            .getCategoryValue());

      undefinedRowItem.setTotalCount(undefRowTotalNumber != null
            ? undefRowTotalNumber
            : new Long(0));
      if (!undefinedPIs.isEmpty())
      {
         undefinedRowItem.calculateColumnStates();
      }

      IRowItem totalRowItem = (IRowItem) dataModel.get(TOTAL_ROW_ITEM_ID);
      if (totalRowItem != null && aInstances != null)
      {
         List /* <ProcessInstance> */totalPIs = getActivePIs(pInstances, categoryId,
               TOTAL_ROW_ITEM_ID);
         for (int a = 0; a < aInstances.size(); a++)
         {
            aInstance = (ActivityInstance) aInstances.get(a);
            columnItem = totalRowItem.getColumnItem(aInstance.getActivity().getId());
            if (columnItem != null
                  && ActivityInstanceState.Completed.equals(aInstance.getState()))
            {
               columnItem.addCompletedActivity();
               columnItem.addActivityInstance(aInstance);
            }
            if (columnItem != null)
            {
               AggregateActivityColumnItem aggregateActivity = (AggregateActivityColumnItem) columnItem;
               aggregateActivity.addTotalActivityCnt();
            }
         }

         totalRowItem.setActivePIs(totalPIs);

         Long totalRowTotalNumber = (Long) totalCountMap.get(totalRowItem
               .getCategoryValue());

         totalRowItem.setTotalCount(totalRowTotalNumber != null
               ? totalRowTotalNumber
               : new Long(0));
         totalRowItem.calculateColumnStates();
      }

   }

   /**
    * Method retrieves all process instances which are belonging to a given process ID and
    * being started within a specific time period. This time period corresponds to the 24
    * hours, in that the passed startingDate belongs to.
    * 
    * @param processId
    * @param startingDate
    * @return
    */
   private ProcessInstances getProcessInstances(String processId, long startingDate)
   {
      Calendar c = Calendar.getInstance(PortalApplication.getInstance().getTimeZone());
      c.setTimeInMillis(startingDate);

      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      long start = c.getTimeInMillis();

      c.set(Calendar.HOUR_OF_DAY, 23);
      c.set(Calendar.MINUTE, 59);
      c.set(Calendar.SECOND, 59);
      c.set(Calendar.MILLISECOND, 99);
      long end = c.getTimeInMillis();
      
      ProcessInstanceQuery pQuery = ProcessInstanceQuery.findForProcess(processId, false);
      pQuery.getFilter().add(ProcessInstanceQuery.START_TIME.between(start, end));
      pQuery.setPolicy(SubsetPolicy.UNRESTRICTED);
      pQuery.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

      ProcessInstances pInstances = this.sessionCtx.getServiceFactory().getQueryService()
            .getAllProcessInstances(pQuery);

      return pInstances;
   }

   /**
    * Method retrieves all activity instances belonging to the one of the given activity
    * IDs and one of the given process instance OIDs.
    * 
    * @param pInstances
    * @param activityIDs
    * @return
    */
   private ActivityInstances getActivityInstances(ProcessInstances pInstances, List<String> activityIDs)
   {
      ActivityInstances aInstances = null;

      if (!pInstances.isEmpty())
      {
         ActivityInstanceQuery aQuery = ActivityInstanceQuery.findAll();

         FilterOrTerm processInstanceOrTerm = aQuery.getFilter().addOrTerm();
         for (ProcessInstance pInstance: pInstances)
         {           
            processInstanceOrTerm.or(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pInstance.getOID()));
         }

         FilterOrTerm activityOrTerm = aQuery.getFilter().addOrTerm();
         for (String activityId:activityIDs)
         {            
            activityOrTerm.or(ActivityFilter.forAnyProcess(activityId));
         }

         aInstances = this.sessionCtx.getServiceFactory().getQueryService().getAllActivityInstances(aQuery);
      }
      return aInstances;
    }

   /**
    * Method creates a map that contains the different values of the process data as keys
    * and the number of process instances that have assigned this process data as values.
    * 
    * The different process data values are corresponding to the rows of the Traffic Light
    * View table.
    * 
    * @param pInstances
    * @return
    */
   private Map/* <String, Long> */createTotalCountMap(ProcessInstances pInstances)
   {
      try
      {
         Map/* <String, Long> */rowGroupCountMap = new HashMap/*
                                                               * <String, Long>
                                                               */();
         for (Iterator/* <ProcessInstance> */_iterator = pInstances.iterator(); _iterator
               .hasNext();)
         {

            ProcessInstance pInstance = (ProcessInstance) _iterator.next();

            // retrieve specified process data from configuration file
            String descriptorKey = categoryFilter;
            if (StringUtils.isEmpty(descriptorKey))
            {
               descriptorKey = TrafficLightViewPropertyProvider.getInstance().getRowId(
                     this.processId);
            }

            // retrieve all specified values for this process data
            String descriptorValue = "";
            if (((IDescriptorProvider) pInstance).getDescriptorValue(descriptorKey) != null)
            {
               descriptorValue = ((Object) ((IDescriptorProvider) pInstance)
                     .getDescriptorValue(descriptorKey)).toString();
            }

            if (StringUtils.isEmpty(descriptorValue))
            {
               descriptorValue = UNDEFINED_ROW_ITEM_ID;
            }

            // build a map that contains the number of instances belonging to a
            // process data
            // value
            if (descriptorValue != null)
            {
               if (!rowGroupCountMap.containsKey(descriptorValue))
               {
                  rowGroupCountMap.put(descriptorValue, new Long(1));
               }
               else
               {
                  Long totalCount = (Long) rowGroupCountMap.get(descriptorValue);
                  totalCount = new Long(totalCount.longValue() + 1);
                  rowGroupCountMap.remove(descriptorValue);
                  rowGroupCountMap.put(descriptorValue, totalCount);
               }
            }
         }

         rowGroupCountMap.put(TOTAL_ROW_ITEM_ID, new Long(pInstances.size()));
         return rowGroupCountMap;
      }
      catch (Exception e)
      {
         trace.error(e);
         return null;
      }
   }

   /**
    * @param pInstances
    * @param categoryId
    * @param categoryValue
    * @return activePIs
    */
   private List<ProcessInstance> getActivePIs(ProcessInstances pInstances, String categoryId, String categoryValue)
   {
      List<ProcessInstance> activePIs = new ArrayList<ProcessInstance>();
      for (ProcessInstance pInstance : pInstances)
      {
         if (pInstance instanceof ProcessInstanceDetails)
         {
            String value = null;
            if (((ProcessInstanceDetails) pInstance).getDescriptorValue(categoryId) != null)
            {
               value = ((Object) ((ProcessInstanceDetails) pInstance)
                     .getDescriptorValue(categoryId)).toString();
            }

            if ((value != null && categoryValue.equals(value))
                  || TOTAL_ROW_ITEM_ID.equals(categoryValue)
                  || (value == null && categoryId != null && UNDEFINED_ROW_ITEM_ID
                        .equals(categoryValue)))
            {
               activePIs.add(pInstance);
            }
         }
      }
      return activePIs;
   }

   /**
    * @param rowItems
    * @param activityList
    * @return
    */
   protected List getTableData(Collection/* <IRowItem> */rowItems,
         List/* <ActivityType> */activityList)
   {
      // total row
      IRowItem totalRowItem = getRowItem(rowItems, TOTAL_ROW_ITEM_ID);
      if (totalRowItem != null)
      {
         rowItems.remove(totalRowItem);
      }

      // undefined row
      IRowItem undefinedRowItem = getRowItem(rowItems, UNDEFINED_ROW_ITEM_ID);
      if (undefinedRowItem != null)
      {
         rowItems.remove(undefinedRowItem);
      }

      Iterator/* <IRowItem> */rowItemIterator = rowItems.iterator();
      int columnsCount = activityList.size() + 1;// columnModel.getColumnList().size();
      List data = new ArrayList();
      while (rowItemIterator.hasNext())
      {
         IRowItem rowItem = (IRowItem) rowItemIterator.next();

         Object currentRow[] = null;
         if (totalRowItem.getColumnItems() != null
               && totalRowItem.getColumnItems().size() > 0)
         {
            currentRow = new Object[columnsCount];
         }
         else
         {
            currentRow = new Object[1];
         }
         currentRow[0] = rowItem;
         data.add(currentRow);

         Iterator/* <IColumnItem> */columnItemIterator = rowItem.getColumnItems()
               .iterator();
         while (columnItemIterator.hasNext())
         {
            IColumnItem columnItem = (IColumnItem) columnItemIterator.next();
            for (int i = 0; i < activityList.size(); i++)
            {
               ActivityDetails activityDetails = (ActivityDetails) activityList.get(i);
               if (activityDetails.getId().equals(columnItem.getId()))
               {
                  currentRow[i + 1] = columnItem;
                  break;
               }
            }
         }

      }

      if (undefinedRowItem != null && categoryFilter != null
            && undefinedRowItem.getTotalCount().longValue() > 0)
      {

         Object currentRow[] = null;
         if (totalRowItem.getColumnItems() != null
               && totalRowItem.getColumnItems().size() > 0)
         {
            currentRow = new Object[columnsCount];
         }
         else
         {
            currentRow = new Object[1];
         }
         currentRow[0] = undefinedRowItem;
         data.add(currentRow);

         Iterator/* <IColumnItem> */columnItemIterator = undefinedRowItem
               .getColumnItems().iterator();
         while (columnItemIterator.hasNext())
         {
            IColumnItem columnItem = (IColumnItem) columnItemIterator.next();
            for (int i = 0; i < activityList.size(); i++)
            {
               ActivityDetails activityDetails = (ActivityDetails) activityList.get(i);
               if (activityDetails.getId().equals(columnItem.getId()))
               {
                  currentRow[i + 1] = columnItem;
                  break;
               }
            }
         }
      }

      boolean evaluateTotalRow = TrafficLightViewPropertyProvider.getInstance()
            .withTotalRow(this.processId);
      if (totalRowItem != null && evaluateTotalRow)
      {
         Object currentRow[] = null;
         if (totalRowItem.getColumnItems() != null
               && totalRowItem.getColumnItems().size() > 0)
         {
            currentRow = new Object[columnsCount];
         }
         else
         {
            currentRow = new Object[1];
         }

         currentRow[0] = totalRowItem;
         data.add(currentRow);

         Iterator/* <IColumnItem> */columnItemIterator = totalRowItem.getColumnItems()
               .iterator();
         // int i=1;
         while (columnItemIterator.hasNext())
         {
            IColumnItem columnItem = (IColumnItem) columnItemIterator.next();
            for (int i = 0; i < activityList.size(); i++)
            {
               ActivityDetails activityDetails = (ActivityDetails) activityList.get(i);
               if (activityDetails.getId().equals(columnItem.getId()))
               {
                  currentRow[i + 1] = columnItem;
                  break;
               }
            }
         }
      }

      return data;
   }

   /**
    * @param rowItems
    * @param rowItemId
    * @return
    */
   private IRowItem getRowItem(Collection/* <IRowItem> */rowItems, String rowItemId)
   {
      IRowItem result = null;
      for (Iterator _iterator = rowItems.iterator(); _iterator.hasNext();)
      {
         IRowItem rowItem = (IRowItem) _iterator.next();
         if (rowItemId.equals(rowItem.getId()))
         {
            result = rowItem;
            break;
         }
      }
      return result;
   }

   /**
    * @return
    */
   public SelectItem[] getAllProcesses()
   {
      List<String> processesFQIds = TrafficLightViewPropertyProvider.getInstance()
            .getAllProcessDefinitionIDs();
     

      processSelectItem = new SelectItem[processesFQIds.size()];

      for (int i=0;i<processesFQIds.size();i++)
      {
        String processFQId = processesFQIds.get(i);   
        //check FQID 
        ProcessDefinition processDefinition=ProcessDefinitionUtils.getProcessDefinition(QName.valueOf(processFQId).getLocalPart());
        processSelectItem[i] = new SelectItem(processFQId,I18nUtils.getProcessName(processDefinition)); 
      }
      return processSelectItem;
   }

   /**
    * @return
    */
   public SelectItem[] getAllCategory()
   {
      List<Category> categoriesList = new ArrayList<Category>();

      ProcessDefinition processDefinition = getProcessDefinition();
      if (processDefinition != null)
      {
         List<DataPath> dataPaths = processDefinition.getAllDataPaths();
         for (DataPath dataPath : dataPaths)
         {
            if (dataPath.isDescriptor())
            {
               List values = TrafficLightViewPropertyProvider.getInstance().getAllRowIDsAsList(this.processId,
                     dataPath.getId());
               if (!values.isEmpty())
               {
                  Category category = new Category(dataPath);
                  categoriesList.add(category);
               }
            }
         }
      }
      categories = new SelectItem[categoriesList.size() + 1];
      categories[0] = new SelectItem("", "");
      for (int j = 0; j < categoriesList.size(); j++)
      {
         Category category = (Category) categoriesList.get(j);
         categories[j + 1] = new SelectItem(category.getId(), category.getName());
      }

      return categories;
   }

   /**
    * @param event
    */
   public void processChangeListener(ValueChangeEvent event)
   {
      this.activityTable = false;
      if (event.getNewValue() != null)
      {
         this.processId = (String) event.getNewValue();
         this.categoryFilter = null;
         initialize();
      }

   }

   /**
    * @param event
    */
   public void categoriesChangeListener(ValueChangeEvent event)
   {
      this.activityTable = false;
      if (event.getNewValue() != null)
      {
         this.categoryFilter = event.getNewValue().toString();
      }
      else
      {
         this.categoryFilter = null;
      }
      initialize();

   }

   /**
    * @param ae
    */
   public void getProcessesAITable(ActionEvent ae)
   {
      selectedProcessActivityName = null;
      this.activityTable = true;
      
      String selectedCategoryValue = (String) ae.getComponent().getAttributes().get("categoryValue");
      selectedProcessActivityId = (String) ae.getComponent().getAttributes().get("processActivityId");
      String processActivityQualifiedId = (String) ae.getComponent().getAttributes().get("processActivityQualifiedId");

      long startingDate = System.currentTimeMillis();
      List<String> columns = TrafficLightViewPropertyProvider.getInstance().getAllColumnIDs(this.processId);

      ProcessInstances pInstances = getProcessInstances(processId, startingDate);
      ActivityInstances aInstances = getActivityInstances(pInstances, columns);

      List<ProcessInstance> processesNotPassedAI = getActivePIs(pInstances, categoryFilter, selectedCategoryValue);//we should use DescriptorFilter 
      List<ProcessInstance> processesPassedAI = new ArrayList<ProcessInstance>();

      IRowItem selectedRowItem = (IRowItem) tempTableDataMap.get(selectedCategoryValue);
      selectedRowItem.setActivePIs(processesNotPassedAI);
      Map activePIs = getActivePIs(selectedRowItem);
      if (selectedRowItem != null && aInstances != null)
      {
         for (int a = 0; a < aInstances.size(); a++)
         {
            ActivityInstance aInstance = (ActivityInstance) aInstances.get(a);

            if (aInstance.getModelElementID().equals(selectedProcessActivityId))
            {
               if (null == selectedProcessActivityName)
               {
                  selectedProcessActivityName = I18nUtils.getActivityName(aInstance.getActivity());
               }
               if (ActivityInstanceState.Completed.equals(aInstance.getState()))
               {

                  ProcessInstance pi = (ProcessInstance) activePIs.get(new Long(aInstance.getProcessInstanceOID()));
                  if (pi != null)
                  {
                     processesPassedAI.add(pi);
                     processesNotPassedAI.remove(pi);
                  }
               }
            }
         }
      }

      initializeActivityPassedListTable(processActivityQualifiedId, processesPassedAI,
            ACTIVITY_INSTANCE_STATE_COMPLETED);
      initializeActivityNotPassedListTable(processActivityQualifiedId, processesNotPassedAI,
            ACTIVITY_INSTANCE_STATE_ALIVE);
   }
   

   /**
    * @param rowItem
    * @return
    */
   private Map getActivePIs(IRowItem rowItem)
   {
      Map activePIs = new HashMap();
      for (Iterator iterator = rowItem.getActivePIs().iterator(); iterator.hasNext();)
      {
         ProcessInstance pi = (ProcessInstance) iterator.next();
         activePIs.put(new Long(pi.getOID()), pi);
      }
      return activePIs;
   }

   public void initializeActivityPassedListTable(String processActivityId, List<ProcessInstance> processesPassedAI,
         int state)
   {

      if (!passedActivityTableInitialized)
      {
         passedActivityHelper = new ActivityTableHelper();
         passedActivityHelper.getColumnModelListener().setNeedRefresh(false);
         passedActivityHelper.setCallbackHandler(this);
         passedActivityHelper.initActivityTable();
         passedActivityHelper.getActivityTable().initialize();
         passedActivityTableInitialized = true;
         passedActivityHelper.getColumnModelListener().setNeedRefresh(true);
      }
      TrafficLightActivitySearchHandler trafficLightActivitySearchHandler = new TrafficLightActivitySearchHandler(
            processActivityId, processesPassedAI, state);

      passedActivityHelper.getActivityTable().setISearchHandler(trafficLightActivitySearchHandler);
      passedActivityHelper.refreshActivityTable();
   }

   public void initializeActivityNotPassedListTable(String processActivityId, List processesNotPassedAI, int state)
   {
      if (!notPassedActivityTableInitialized)
      {
         notPassedActivityHelper = new ActivityTableHelper();
         notPassedActivityHelper.getColumnModelListener().setNeedRefresh(false);
         notPassedActivityHelper.setCallbackHandler(this);
         notPassedActivityHelper.initActivityTable();
         notPassedActivityHelper.getActivityTable().initialize();
         notPassedActivityTableInitialized = true;
         notPassedActivityHelper.getColumnModelListener().setNeedRefresh(true);
      }
      TrafficLightActivitySearchHandler trafficLightActivitySearchHandler = new TrafficLightActivitySearchHandler(
            processActivityId, processesNotPassedAI, state);
      notPassedActivityHelper.getActivityTable().setISearchHandler(trafficLightActivitySearchHandler);
      notPassedActivityHelper.refreshActivityTable();
   }

   public String getActivityNotPassedListTableTitle(){
      if (selectedProcessActivityName != null)
      {
         return propsBean.getParamString("views.trafficLightView.trafficLightViewActivityPending",
               selectedProcessActivityName);
      }
      else
      {
         return "";
      }
   }
   
   public String getActivityPassedListTableTitle(){
      if (selectedProcessActivityName != null)
      {
         return propsBean.getParamString("views.trafficLightView.trafficLightViewActivityCompleted",
               selectedProcessActivityName);
      }
      else
      {
         return "";
      }
   }
   /**
    * @author Subodh.Godbole
    *
    */
   public class TrafficLightActivitySearchHandler extends IppSearchHandler<ActivityInstance>
   {
      private String processActivityId;
      private List<ProcessInstance> processesAI;
      private int state;

      /**
       * @param activityId
       */
      public TrafficLightActivitySearchHandler(String processActivityId, List<ProcessInstance> processesAI, int state)
      {
         this.processActivityId = processActivityId;
         this.processesAI = processesAI;
         this.state = state;
      }

      @Override
      public Query createQuery()
      {
         ActivityInstanceQuery query = new ActivityInstanceQuery();
         if (state == ACTIVITY_INSTANCE_STATE_ALIVE)
         {
            query = ActivityInstanceQuery.findAlive();
         }
         else if (state == ACTIVITY_INSTANCE_STATE_COMPLETED)
         {
            if (CollectionUtils.isNotEmpty(processesAI) && processActivityId != null)
            {
               ActivityInstanceState[] aiStateArray = {ActivityInstanceState.Completed, ActivityInstanceState.Aborted};
               query = ActivityInstanceQuery.findInState(aiStateArray);
               FilterOrTerm orTerm = query.getFilter().addOrTerm();
               orTerm.add(ActivityFilter.forAnyProcess(processActivityId));
            }
           
         }

         if (CollectionUtils.isEmpty(processesAI) && processActivityId != null)
         {
            query.getFilter().add(ActivityInstanceQuery.ACTIVITY_OID.isNull());
         }
         else
         {

            FilterOrTerm orTerm = query.getFilter().addOrTerm();
            for (ProcessInstance pInstance:processesAI)
            {               
               orTerm.add(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pInstance.getOID()));
            }
         }

         return query;
      }

      @Override
      public QueryResult<ActivityInstance> performSearch(Query query)
      {
         try
         {
            QueryResult<ActivityInstance> result = facade.getAllActivityInstances((ActivityInstanceQuery) query);
            processInstances = ProcessInstanceUtils.getProcessInstancesAsMap(result, true);
            notPassedActivityHelper.setProcessInstanceMap(processInstances);
            passedActivityHelper.setProcessInstanceMap(processInstances);
            return result;
         }
         catch (InvalidServiceException e)
         {
            PageMessage.setMessage(e);
         }
         return null;
      }
   }   
   
   public PaginatorDataTable<ActivityInstanceWithPrioTableEntry, ActivityInstance> getPassedActivityTableView()
   {
      return passedActivityHelper.getActivityTable();
   }

   public PaginatorDataTable<ActivityInstanceWithPrioTableEntry, ActivityInstance> getNotPassedActivityTableView()
   {
      return notPassedActivityHelper.getActivityTable();
   }

   public SortableTable<TrafficLightViewUserObject> getTrafficLightViewTable()
   {
      return trafficLightViewTable;
   }

   public SelectItem[] getProcessSelectItem()
   {
      return processSelectItem;
   }

   public void setProcessSelectItem(SelectItem[] processSelectItem)
   {
      this.processSelectItem = processSelectItem;
   }

   public String getProcessId()
   {
      return processId;
   }

   public void setProcessId(String processId)
   {
      this.processId = processId;
   }

   public SelectItem[] getCategories()
   {
      return categories;
   }

   public void setCategories(SelectItem[] categories)
   {
      this.categories = categories;
   }

   public String getCategoryFilter()
   {
      return categoryFilter;
   }

   public void setCategoryFilter(String categoryFilter)
   {
      this.categoryFilter = categoryFilter;
   }

   public boolean isActivityTable()
   {
      return activityTable;
   }

   public void setActivityTable(boolean activityTable)
   {
      this.activityTable = activityTable;
   }

   public boolean isAllowConfiguration()
   {
      return allowConfiguration;
   }

   public void setAllowConfiguration(boolean allowConfiguration)
   {
      this.allowConfiguration = allowConfiguration;
   }
   
	public ActivityTableHelper getNotPassedActivityHelper() {
		return notPassedActivityHelper;
	}

	public void setNotPassedActivityHelper(
			ActivityTableHelper notPassedActivityHelper) {
		this.notPassedActivityHelper = notPassedActivityHelper;
	}

	public ActivityTableHelper getPassedActivityHelper() {
		return passedActivityHelper;
	}

	public void setPassedActivityHelper(ActivityTableHelper passedActivityHelper) {
		this.passedActivityHelper = passedActivityHelper;
	}
   
   public void handleEvent(EventType eventType)
   {
      update();
   }
   
   public boolean isPassedActivityTableInitialized()
   {
      return passedActivityTableInitialized;
   }

   public boolean isNotPassedActivityTableInitialized()
   {
      return notPassedActivityTableInitialized;
   }
   
   /**
    * 
    * @author Sidharth.Singh
    * 
    */
   public class TrafficLightComparator extends SortableTableComparator<TrafficLightViewUserObject>
   {

      public TrafficLightComparator(String sortColumnProperty, boolean ascending)
      {
         super(sortColumnProperty, ascending);
      }

      @Override
      public int compare(TrafficLightViewUserObject o1, TrafficLightViewUserObject o2)
      {
         if (getMessages().getString("category.undefined").equals(o1.getCategoryName())
               && getMessages().getString("category.total").equals(o2.getCategoryName()))
         {
            return -1;
         }
         else if (getMessages().getString("category.total").equals(o1.getCategoryName())
               && getMessages().getString("category.undefined").equals(o2.getCategoryName()))
         {
               return 1;
         }
         else if (getMessages().getString("category.undefined").equals(o1.getCategoryName())
               || getMessages().getString("category.total").equals(o1.getCategoryName()))
         {
            return 1;
         }
         else if (getMessages().getString("category.undefined").equals(o2.getCategoryName())
               || getMessages().getString("category.total").equals(o2.getCategoryName()))
         {
            return -1;
         }
         else
         {
            return super.compare(o1, o2);
         }
      }

   }

}
