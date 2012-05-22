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
package org.eclipse.stardust.ui.web.processportal.view;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.Note;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.CustomOrderCriterion;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.ActivityEventObserver;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.app.View;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModelListener;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterBetween;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterPickList;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterDate;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterNumber;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPickList;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.DataType;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter.FilterCriteria;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilterPickList.RenderType;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.DataTableSortModel;
import org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder;
import org.eclipse.stardust.ui.web.common.table.PaginatorDataTable;
import org.eclipse.stardust.ui.web.common.table.SortCriterion;
import org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler;
import org.eclipse.stardust.ui.web.common.table.export.ExportType;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.processportal.EventController;
import org.eclipse.stardust.ui.web.processportal.common.PPUtils;
import org.eclipse.stardust.ui.web.processportal.common.Resources;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.ActivityNamesFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.PriorityAutoCompleteItem;
import org.eclipse.stardust.ui.web.viewscommon.common.PriorityAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityAutocompleteItem;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityAutocompleteTableDataFilter;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppFilterHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSearchHandler;
import org.eclipse.stardust.ui.web.viewscommon.common.table.IppSortHandler;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.AbortActivityBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ClientContextBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.QueryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;



/**
 * @author roland.stamm
 * 
 */
public class WorklistTableBean extends UIComponentBean
      implements InitializingBean, DisposableBean,
      ActivityEventObserver, IUserObjectBuilder<WorklistTableEntry>, 
      ICallbackHandler, Serializable,ViewEventHandler
{

   protected final static String PROCESS_DEFINITION_MODEL = "carnotBcProcessInstanceFilter/processDefinitionModel";
   
   private static final long serialVersionUID = -4541966602037548481L;

   private static final String COL_OID = "OID";

   private static final String COL_LAST_MODIFIED = "Last Modified";

   private static final String COL_STARTED = "Started";

   private static final String COL_PRIORITY = "Priority";

   private static final String COL_DESCRIPTORS = "Descriptors";

   private static final String COL_ACTIVITY_NAME = "Overview";

   private static final String COL_PROCESS_DEFINITION = "Process Definition";

   private static final String COL_SELECT = "Select";

   private static final String COL_STATUS = "Status";

   private static final String COL_DURATION = "Duration";

   private static final String COL_LAST_PERFORMER = "Last Performer";

   private static final String COL_ASSIGNED_TO = "Assigned to";

   private static final String COL_ACTIONS = "Actions";

   private static final Logger trace = LogManager.getLogger(WorklistTableBean.class);   

   private PaginatorDataTable<WorklistTableEntry, ? extends Object> worklistTable;

   private TableColumnSelectorPopup worklistColSelecpopup;

   private EventController eventController;

   private ClientContextBean processPortalContext;

   private View view;

   //holds <DataId, DataPath>
   private Map<String, DataPath> allDescriptors = CollectionUtils.newMap();

   private List<ColumnPreference> fixedColumns1 = CollectionUtils.newList();

   private List<ColumnPreference> fixedColumns2 = CollectionUtils.newList();

   private Map<Long, ProcessInstance> processInstances;
   
   private boolean isActivated = false;

   private Query query;

   private ParticipantInfo participantInfo;

   private String id;

   private String name;
   
   private boolean filtersAddedToQuery;
   
   private boolean needUpdateForActvityEvent;
   
   private long currentPerformerOID;
   
   private Set<String> visibleDescriptorsIds;
   
   private boolean fetchAllDescriptors;

   public WorklistTableBean()
   {
      super("worklistPanel");
      // TODO activityFilter;
   }

   public static WorklistTableBean getCurrentInstance()
   {
      return (WorklistTableBean) FacesUtils.getBeanFromContext("worklistTableBean");
   }

   /* (non-Javadoc)
    * @see org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(org.eclipse.stardust.ui.web.common.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      switch (event.getType())
      {
      case CREATED:
         this.view = event.getView();
         currentPerformerOID = ServiceFactoryUtils.getUserService().getUser().getOID();
         initViewParams();
         break;
         
      case ACTIVATED:
         if (!isActivated || needUpdateForActvityEvent)
         {
            update();
            isActivated = true;
            needUpdateForActvityEvent = false;
         }
         break;
         
      case POST_OPEN_LIFECYCLE: // if the view is already open and refresh it with latest
                                // assignments
         Object refreshWorklistTableObj = getParamFromView("refreshWorklistTable");
         if (null != refreshWorklistTableObj)
         {
            Boolean refreshWorklistTable = (Boolean) refreshWorklistTableObj;
            if (refreshWorklistTable)
            {
               update();
            }
         }
      }
   }
   
   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
    */
   public void afterPropertiesSet() throws Exception
   {
      if (processPortalContext == null)
      {
         processPortalContext = ClientContextBean.getCurrentInstance();
      }

      eventController.registerObserver((ActivityEventObserver) this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.springframework.beans.factory.DisposableBean#destroy()
    */
   public void destroy() throws Exception
   {
      eventController.unregisterObserver((ActivityEventObserver) this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.event.ActivityEventObserver#handleEvent(org.eclipse.stardust.ui.event.ActivityEvent)
    */
   public void handleEvent(ActivityEvent activityEvent)
   {
      View pinView = PortalApplication.getInstance().getPinView();
      if (null != pinView && pinView == view)
      {
         update(); // If Current View is pinned, then it needs immediate update 
      }
      else
      {
         needUpdateForActvityEvent = true;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.dialogs.ICallbackHandler#handleEvent(org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler.EventType)
    */
   public void handleEvent(EventType eventType)
   {
      if (eventType.equals(EventType.APPLY))
      {
         // TODO notify launchpanels to update or implement
         // ActivityEvent.delegated()
         update();
      }
      if (eventType.equals(EventType.CANCEL))
      {
      }
   }

   /**
    * @param event
    */
   public void openActivity(ActionEvent event)
   {
      try
      {
         String oidStr = org.eclipse.stardust.ui.web.common.util.FacesUtils.getRequestParameter("oid");
         Long oid = (oidStr != null) ? Long.parseLong(oidStr) : 0;

         ActivityInstance ai = getActivityObjectById(oid);
         ActivityInstanceUtils.openActivity(ai);
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void openNotes(ActionEvent event)
   {
      String oidStr = org.eclipse.stardust.ui.web.common.util.FacesUtils.getRequestParameter("oid");
      Long oid = (oidStr != null) ? Long.parseLong(oidStr) : 0;
      ActivityInstance ai = getActivityObjectById(oid);
      if (ai != null)
      {
         ProcessInstanceUtils.openNotes(ai.getProcessInstance());
      }
   }

   /**
    * @param event
    */

   public void openDelegateDialog(ActionEvent ae)
   {
      List<ActivityInstance> ais = getSelectedActivityInstances(ae);
      ActivityInstanceUtils.openDelegateDialog(ais, this);
   }

   /**
    * Refreshes page
    */
   public void refresh()
   {
      update();
   }

   private QueryResult fetchQueryResult(Query query, ParticipantInfo participantInfo)
   {
      QueryResult queryResult = null;
      
      if (query instanceof WorklistQuery)
      {
         Worklist worklist = ServiceFactoryUtils.getWorkflowService().getWorklist((WorklistQuery) query);
         queryResult = PPUtils.extractParticipantWorklist(worklist, participantInfo);

         if (!filtersAddedToQuery)
         {
            ParticipantWorklistCacheManager.getInstance().setWorklistCount(participantInfo, queryResult.getTotalCount());
         }
      }
      else if (query instanceof ActivityInstanceQuery)
      {
         queryResult = ServiceFactoryUtils.getQueryService().getAllActivityInstances((ActivityInstanceQuery) query);
         
         if (!filtersAddedToQuery)
         {
            ProcessWorklistCacheManager.getInstance().setWorklistCount(id, queryResult.getTotalCount());
         }
      }
      
      return queryResult;
   }
   
   /**
    * @return
    * 
    */
   private void update()
   {
      // TODO: Is retrieving the query here required?
      // Get the original query again since it may have been modified by the addition of
      // filters, sort criteria etc.
      query = QueryUtils.getClonedQuery((Query) getParamFromView(Query.class.getName()));
      updateWorklistTable();
   }

   /**
    * 
    */
   private void initViewParams()
   {
      query = (Query) getParamFromView(Query.class.getName());
      participantInfo = (ParticipantInfo) getParamFromView("participantInfo");
      
      id = (String) getParamFromView("id");

      if (null != participantInfo)
      {      
         name = ModelHelper.getParticipantName(participantInfo);
      }
      else // For Duration Related worklists
      {
         Object worklistName = getParamFromView("name");
         if (null != worklistName)
         {
            name = worklistName.toString();
         }
      }
   }
   
   /**
    * @param key
    * @return
    */
   private Object getParamFromView(String key)
   {
      Object ret = null;
      if (null != view)
      {
         ret = view.getViewParams().get(key);
      }
      return ret;
   }

   /**
    * @param oid
    * @return
    */
   private ActivityInstance getActivityObjectById(Long oid)
   {
      // TODO optimization: maybe lookup in lastFetchedWorklist first
      return ActivityInstanceUtils.getActivityInstance(oid);
   }

   /**
    * 
    */
   private void updateWorklistTable()
   {
      if (processPortalContext != null && query != null)
      {
         processPortalContext.getClient().getModels().getProcessFilters().update();

         if (worklistColSelecpopup == null)
         {
            initColumnModel();
         }

         if (worklistTable == null)
         {
            initWorklistTable();
         }
         worklistTable.refresh(true);
      }
   }

   /**
    * 
    */
   private void initWorklistTable()
   {
      worklistTable = new PaginatorDataTable<WorklistTableEntry, Object>(worklistColSelecpopup, new SearchHandler(),
            new FilterHandler(), new SortHandler(), this,
            new DataTableSortModel<WorklistTableEntry>("startDate", false));
      worklistTable.setRowSelector(new DataTableRowSelector("checkSelection",true));
      worklistTable.setDataTableExportHandler(new WorklistExportHandler());
      worklistTable.initialize();
   }

   public static List<SelectItem> getAllProcessDefinitions()
   {
      List<ProcessDefinition> processes = Collections.<ProcessDefinition> emptyList();
      List<SelectItem> allProcessDefns = CollectionUtils.newList();

      processes = ProcessDefinitionUtils.getAllProcessDefinitions();

      for (ProcessDefinition procDefn : processes)
      {
         allProcessDefns.add(new SelectItem(procDefn.getQualifiedId(), I18nUtils.getProcessName(procDefn)));
      }

      return allProcessDefns;
   }

   /**
    * 
    * @return
    */
   public static List<SelectItem> getAllStatus()
   {
      return ActivityInstanceUtils.getAllActivityStates();
   }

   private void initColumnModel()
   {

      ColumnPreference activityNameCol = new ColumnPreference(COL_ACTIVITY_NAME,
            "processName", this.getMessages().getString("column.overview"),
            Resources.VIEW_WORKLIST_COLUMNS, true, true);

      activityNameCol.setColumnDataFilterPopup(new TableDataFilterPopup(
            new ActivityNamesFilter("/plugins/views-common/activityNamesFilter.xhtml")));

      ColumnPreference colOid = new ColumnPreference(COL_OID, "oid", ColumnDataType.NUMBER, this.getMessages()
            .getString("column.oid"), new TableDataFilterPopup(new TableDataFilterNumber(COL_OID, "", DataType.LONG,
            true, null, null)), true, true);

      ColumnPreference processDefnCol = new ColumnPreference(COL_PROCESS_DEFINITION,
            "processDefinition", ColumnDataType.STRING, this.getMessages().getString(
                  "processName"), false, false);
      
      ColumnPreference criticalityCol = new ColumnPreference("Criticality", "criticality",
            this.getMessages().getString("column.criticality"),
            Resources.VIEW_WORKLIST_COLUMNS, true, true);
      criticalityCol.setColumnAlignment(ColumnAlignment.CENTER);
      criticalityCol.setColumnDataFilterPopup(new TableDataFilterPopup(new CriticalityAutocompleteTableDataFilter()));
      
      processDefnCol.setColumnDataFilterPopup(new TableDataFilterPopup(
            new TableDataFilterPickList(FilterCriteria.SELECT_MANY,
                  getAllProcessDefinitions(), RenderType.LIST, 5, null)));

      ColumnPreference colDescriptors = new ColumnPreference(COL_DESCRIPTORS,
            "processDescriptorsList", this.getMessages().getString("column.descriptors"),
            Resources.VIEW_WORKLIST_COLUMNS, true, false);

      ColumnPreference colPriority = new ColumnPreference(COL_PRIORITY, "priority", this
            .getMessages().getString("column.priority"), Resources.VIEW_WORKLIST_COLUMNS,
            true, true);
      colPriority.setColumnAlignment(ColumnAlignment.CENTER);
      colPriority.setColumnDataFilterPopup(new TableDataFilterPopup(new PriorityAutocompleteTableDataFilter()));

      ColumnPreference colStarted = new ColumnPreference(COL_STARTED, "startDate",
            ColumnDataType.DATE, this.getMessages().getString("column.started"),
            new TableDataFilterPopup(new TableDataFilterDate(COL_STARTED, "",
                  DataType.DATE, true, null, null)), true, true);
      colStarted.setNoWrap(true);

      ColumnPreference colLastMod = new ColumnPreference(COL_LAST_MODIFIED,
            "lastModificationTime", ColumnDataType.DATE, this.getMessages().getString(
                  "column.lastmodification"), new TableDataFilterPopup(
                  new TableDataFilterDate(COL_LAST_MODIFIED, "", DataType.DATE, true,
                        null, null)), true, true);
      colLastMod.setNoWrap(true);

      ColumnPreference durationCol = new ColumnPreference(COL_DURATION, "duration",
            ColumnDataType.STRING, this.getMessages().getString("column.duration"), null, true, false);
      durationCol.setNoWrap(true);
      durationCol.setColumnAlignment(ColumnAlignment.CENTER);

      ColumnPreference lastPerformerCol = new ColumnPreference(COL_LAST_PERFORMER,
            "lastPerformer", ColumnDataType.STRING, this.getMessages().getString("column.lastPerformer"), null, true, false);

      ColumnPreference statusCol = new ColumnPreference(COL_STATUS, "status",
            ColumnDataType.STRING, this.getMessages().getString("column.status"),new TableDataFilterPopup(new TableDataFilterPickList(
            FilterCriteria.SELECT_MANY, getAllStatus(), RenderType.LIST, 3, null)), false, false);

      ColumnPreference assignedToCol = new ColumnPreference(COL_ASSIGNED_TO,
            "assignedTo", ColumnDataType.STRING, this.getMessages().getString("column.assignedTo"), false, false);

      // Fixed Column 2
      ColumnPreference colActions = new ColumnPreference(COL_ACTIONS,
            "processActionsList", this.getMessages().getString("column.actions"),
            Resources.VIEW_WORKLIST_COLUMNS, true, false);
      colActions.setExportable(false);
      fixedColumns2.add(colActions);

      List<ColumnPreference> standardColumns = CollectionUtils.newList();
      standardColumns.add(activityNameCol);
      standardColumns.add(colOid);
      standardColumns.add(processDefnCol);
      standardColumns.add(criticalityCol);
      standardColumns.add(colPriority);
      standardColumns.add(colDescriptors);
      standardColumns.add(colStarted);
      standardColumns.add(colLastMod);
      standardColumns.add(durationCol);
      standardColumns.add(lastPerformerCol);
      standardColumns.add(statusCol);
      standardColumns.add(assignedToCol);

      //set descriptors list and map<dataId, dataPath>
      allDescriptors = CommonDescriptorUtils.getAllDescriptors(false);
      List<ColumnPreference> descriptorColumns = DescriptorColumnUtils.createDescriptorColumns(worklistTable, allDescriptors);
      standardColumns.addAll(descriptorColumns);

      
      String preferenceId = (id != null) ? id : UserPreferencesEntries.V_WORKLIST;

      IColumnModel worklistColumnModel = new DefaultColumnModel(standardColumns, fixedColumns1,
            fixedColumns2, UserPreferencesEntries.M_WORKFLOW, preferenceId, new ColumnModelListener());
      DescriptorColumnUtils.setDescriptorColumnFilters(worklistColumnModel, allDescriptors);
      worklistColSelecpopup = new TableColumnSelectorPopup(worklistColumnModel);

      initWorklistTable(); 
   }
   

   /**
    * @return
    */
   public DataTable<WorklistTableEntry> getWorklistTable()
   {
      return worklistTable;
   }

   /**
    * needed by portal-common gernericDataTable
    * 
    * @return basepath
    */
   public String getBasePath()
   {
      return ".";
   }

   public String getName()
   {
      return name;
   }

   public void setEventController(EventController eventController)
   {
      this.eventController = eventController;
   }

   public void setProcessPortalContext(ClientContextBean processPortalContext)
   {
      this.processPortalContext = processPortalContext;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.table.IUserObjectBuilder#createUserObject
    * (java.lang .Object)
    */

   public WorklistTableEntry createUserObject(Object resultRow)
   {
      WorklistTableEntry worklistTableEntry = null;

      if (resultRow instanceof ActivityInstance)
      {
         try
         {
            ActivityInstance ai = (ActivityInstance) resultRow;
            Map<String, Object> descriptorValues = new HashMap<String, Object>();
            List<ProcessDescriptor> processDescriptorsList = CollectionUtils.newList();
            ModelCache modelCache = ModelCache.findModelCache();
            Model model = null;
            ProcessDefinition processDefinition = null;

            model = modelCache.getModel(ai.getModelOID());
            processDefinition = model != null ? model.getProcessDefinition(ai.getProcessDefinitionId()) : null;
            if (processDefinition != null)
            {
               ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) ai.getProcessInstance();
               descriptorValues = processInstanceDetails.getDescriptors();
               if (processInstanceDetails.isCaseProcessInstance())
               {
                  processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                        processInstanceDetails.getDescriptorDefinitions(), descriptorValues, processDefinition, true);
               }
               else
               {
                  processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(descriptorValues,
                        processDefinition, true);
               }
            }

            ProcessInstance pi = null;
            if (null != processInstances)
            {
               pi = processInstances.get(ai.getProcessInstanceOID());
            }
            if (null == pi)
            {
               pi = ProcessInstanceUtils.getProcessInstance(ai);
            }
            String priorityValue = ProcessInstanceUtils.getPriorityValue(pi);
            List<Note> notes = ProcessInstanceUtils.getNotes(pi);
            int notesSize = null != notes ? ProcessInstanceUtils.getNotes(pi).size() : 0;

            worklistTableEntry = new WorklistTableEntry(I18nUtils.getActivityName(ai.getActivity()),
                  processDescriptorsList, ActivityInstanceUtils.isActivatable(ai),
                  ActivityInstanceUtils.getLastPerformer(ai), priorityValue, pi.getPriority(), ai.getStartTime(),
                  ai.getLastModificationTime(), ai.getOID(), this.getDuration(ai), notesSize, descriptorValues,
                  ai.getProcessInstanceOID(), ai, currentPerformerOID);
         }
         catch (Exception e)
         {
            trace.error(e);
            worklistTableEntry = new WorklistTableEntry();
            worklistTableEntry.setLoaded(false);
            worklistTableEntry.setCause(e);
         }
      }
      else
      {
         trace.warn("Could not build UserObject<WorklistTableEntry> in " + this.toString());
      }

      return worklistTableEntry;
   }

   public String getDuration(ActivityInstance ai)
   {
      long timeInMillis = Calendar.getInstance().getTimeInMillis();
      if (ai.getState() == ActivityInstanceState.Completed
            || ai.getState() == ActivityInstanceState.Aborted)
      {
         timeInMillis = ai.getLastModificationTime().getTime();
      }
      return DateUtils.formatDurationInHumanReadableFormat(timeInMillis
                - ai.getStartTime().getTime());
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.jsf.common.ISortHandler#isSortableColumn(java.lang.String)
    */
   public boolean isSortableColumn(String arg0)
   {
      throw new UnsupportedOperationException("isSortableColumn");
   }

   @Override
   public void initialize()
   {
   // TODO Auto-generated method stub

   }

   public int getSelectedItemCount()
   {
      int count = 0;
      if (worklistTable != null)
      {
         List<WorklistTableEntry> activityList = worklistTable.getCurrentList();
         for (Iterator<WorklistTableEntry> iterator = activityList.iterator(); iterator.hasNext();)
         {
            WorklistTableEntry activityInstanceWithPrioTableEntry = iterator.next();
            if (activityInstanceWithPrioTableEntry.isCheckSelection())
            {
               count++;
            }
         }
      }
      return count;
   }

   public List<ActivityInstance> getSelectedActivityInstances(ActionEvent ae)
   {
      ActivityInstance ai = (ActivityInstance) ae.getComponent().getAttributes().get(
            "activityInstance");

      List<ActivityInstance> ais = CollectionUtils.newList();

      if (ai != null)
      {
         ais.add(ai);
      }
      else
      {
         List<WorklistTableEntry> ait = worklistTable.getCurrentList();
         for (Iterator<WorklistTableEntry> iterator = ait.iterator(); iterator.hasNext();)
         {
            WorklistTableEntry at = iterator.next();
            if (at.isCheckSelection())
            {
               ActivityInstance a = at.getActivityInstance();
               ais.add(a);
            }
         }
      }

      return ais;
   }

   public void openAbortDialog(ActionEvent ae)
   {
      List<ActivityInstance> ais = getSelectedActivityInstances(ae);
      if (CollectionUtils.isNotEmpty(ais))
      {
         AbortActivityBean abortActivity = AbortActivityBean.getInstance();
         abortActivity.setCallbackHandler(this);
         abortActivity.abortActivities(ais);
      }
   }

   /**
    * 
    */
   private void initializeSelectiveDescriptorFetchProperties()
   {
      List<ColumnPreference> colPrefs = worklistColSelecpopup.getColumnModel().getSelectableColumns();
      visibleDescriptorsIds = new HashSet<String>();
      fetchAllDescriptors = false;
      for (ColumnPreference colPref : colPrefs)
      {
         if (COL_DESCRIPTORS.equals(colPref.getColumnName()) && colPref.isVisible())
         {
            fetchAllDescriptors = true;
         }
         else if (isDescriptorColumn(colPref) && colPref.isVisible())
         {
            visibleDescriptorsIds.add(colPref.getColumnName());
         }
      }
   }
   
   /**
    * @return
    */
   public boolean isFetchAllDescriptors()
   {
      if (null == visibleDescriptorsIds)
      {
         initializeSelectiveDescriptorFetchProperties();
      }
      return fetchAllDescriptors;
   }

   /**
    * Returns a set descriptor IDs of the descriptor columns that are visible.
    * 
    * @return
    */
   public Set<String> getVisibleDescriptorsIds()
   {
      if (null == visibleDescriptorsIds)
      {
         initializeSelectiveDescriptorFetchProperties();
      }
      return visibleDescriptorsIds;
   }
   
   /**
    * @param colPref
    * @return
    */
   private boolean isDescriptorColumn(ColumnPreference colPref)
   {
      return allDescriptors.keySet().contains(colPref.getColumnName());
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class SearchHandler extends IppSearchHandler<Object>
   {
      private static final long serialVersionUID = 1L;

      @Override
      public Query createQuery()
      {
         // Get the original query again since it may have been modified by the addition of
         // filters, sort criteria etc.
         query = QueryUtils.getClonedQuery((Query) getParamFromView(Query.class.getName()));         
         query.setPolicy(HistoricalStatesPolicy.WITH_LAST_USER_PERFORMER);
         if (isFetchAllDescriptors())
         {
            query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         }
         else if (CollectionUtils.isEmpty(getVisibleDescriptorsIds()))
         {
            query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
         }
         else
         {
            query.setPolicy(DescriptorPolicy.withIds(getVisibleDescriptorsIds()));
         }
         filtersAddedToQuery = false;

         return query;
      }

      @Override
      public QueryResult<Object> performSearch(Query query)
      {
         QueryResult queryResult = fetchQueryResult(query, participantInfo);
         processInstances = ProcessInstanceUtils.getProcessInstancesAsMap(queryResult, true);
         return queryResult;
      }
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   public class FilterHandler extends IppFilterHandler
   {
      private static final long serialVersionUID = 1L;

      @Override
      public void applyFiltering(Query query, List<ITableDataFilter> filters)
      {
         FilterAndTerm filter = query.getFilter().addAndTerm();
         filtersAddedToQuery = filters.size() > 0;
         GenericDescriptorFilterModel filterModel = null;
         
         for (ITableDataFilter tableDataFilter : filters)
         {
            if (tableDataFilter.isFilterSet())
            {
               String dataId = tableDataFilter.getName();
               if (COL_ACTIVITY_NAME.equals(dataId))
               {
                  ActivityNamesFilter pfilter = (ActivityNamesFilter) tableDataFilter;
                  String[] selectedActivities = pfilter.getSelectedActivities();
                  FilterOrTerm or = filter.addOrTerm();

                  for (String activityId : selectedActivities)
                  {
                     if (activityId != null)
                     {
                        or.add(org.eclipse.stardust.engine.api.query.ActivityFilter.forAnyProcess(activityId));
                     }
                  } // for each
               }
               else if (COL_OID.equals(dataId))
               {
                  Long start = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Long end = (Long) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (start != null)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        filter.and(ActivityInstanceQuery.OID.greaterOrEqual(start));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        filter.and(WorklistQuery.ACTIVITY_INSTANCE_OID.greaterOrEqual(start));
                     }
                  }
                  if (end != null)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        filter.and(ActivityInstanceQuery.OID.lessOrEqual(end));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        filter.and(WorklistQuery.ACTIVITY_INSTANCE_OID.lessOrEqual(end));
                     }
                  }
               }
               else if ("ProcessName".equals(dataId))
               {
                  if (((ITableDataFilterPickList) tableDataFilter).getSelected() != null)
                  {
                     FilterOrTerm or = filter.addOrTerm();
                     if (((ITableDataFilterPickList) tableDataFilter).getSelected().size() > 0)
                     {
                        for (int i = 0; i < ((ITableDataFilterPickList) tableDataFilter)
                              .getSelected().size(); i++)
                        {
                           or.add(new ProcessDefinitionFilter(
                                 ((ITableDataFilterPickList) tableDataFilter).getSelected()
                                       .get(i).toString(), false));
                        }
                     }
                  }
               }
               else if (COL_PROCESS_DEFINITION.equals(dataId))
               {
                  if (((ITableDataFilterPickList) tableDataFilter).getSelected() != null)
                  {
                     FilterOrTerm or = filter.addOrTerm();
                     if (((ITableDataFilterPickList) tableDataFilter).getSelected().size() > 0)
                     {
                        for (int i = 0; i < ((ITableDataFilterPickList) tableDataFilter)
                              .getSelected().size(); i++)
                        {
                           or.add(new ProcessDefinitionFilter(
                                 ((ITableDataFilterPickList) tableDataFilter).getSelected()
                                       .get(i).toString(), false));
                        }
                     }
                  }

               }
               else if ("Criticality".equals(dataId))
               {
                  CriticalityAutocompleteTableDataFilter criticalityfilter = (CriticalityAutocompleteTableDataFilter) tableDataFilter;
                  List<CriticalityAutocompleteItem> criticalityItems = criticalityfilter.getCriticalitySelector().getSelectedValues();

                  FilterOrTerm or = filter.addOrTerm();
                  for (CriticalityAutocompleteItem criticality : criticalityItems)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        or.or(ActivityInstanceQuery.CRITICALITY.between(criticality.getRangeFromDouble(), criticality.getRangeToDouble()));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        or.or(WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY.between(criticality.getRangeFromDouble(), criticality.getRangeToDouble()));
                     }
                  }
               }
               else if (COL_PRIORITY.equals(dataId))
               {
                  PriorityAutocompleteTableDataFilter priorityfilter = (PriorityAutocompleteTableDataFilter) tableDataFilter;
                  List<PriorityAutoCompleteItem> priorityItems = priorityfilter.getPriorityAutocompleteSelector()
                        .getSelectedValues();

                  FilterOrTerm or = filter.addOrTerm();
                  for (PriorityAutoCompleteItem priority : priorityItems)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        or.or(ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY.isEqual(priority.getPriority()));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        or.or(WorklistQuery.PROCESS_INSTANCE_PRIORITY.isEqual(priority.getPriority()));
                     }
                  }
               }
               else if (COL_STARTED.equals(dataId))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        filter.and(ActivityInstanceQuery.START_TIME.greaterOrEqual(startTime
                              .getTime()));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        filter.and(WorklistQuery.START_TIME.greaterOrEqual(startTime
                              .getTime()));
                     }
                  }
                  if (endTime != null)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        filter.and(ActivityInstanceQuery.START_TIME.lessOrEqual(endTime
                              .getTime()));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        filter.and(WorklistQuery.START_TIME.lessOrEqual(endTime.getTime()));
                     }
                  }
               }
               else if (COL_LAST_MODIFIED.equals(dataId))
               {
                  Date startTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getStartValueAsDataType();
                  Date endTime = (Date) ((ITableDataFilterBetween) tableDataFilter)
                        .getEndValueAsDataType();

                  if (startTime != null)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        filter.and(ActivityInstanceQuery.LAST_MODIFICATION_TIME
                              .greaterOrEqual(startTime.getTime()));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        filter.and(WorklistQuery.LAST_MODIFICATION_TIME
                              .greaterOrEqual(startTime.getTime()));
                     }
                  }
                  if (endTime != null)
                  {
                     if (query instanceof ActivityInstanceQuery)
                     {
                        filter.and(ActivityInstanceQuery.LAST_MODIFICATION_TIME
                              .lessOrEqual(endTime.getTime()));
                     }
                     else if (query instanceof WorklistQuery)
                     {
                        filter.and(WorklistQuery.LAST_MODIFICATION_TIME.lessOrEqual(endTime
                              .getTime()));
                     }
                  }
               }
               else if (COL_STATUS.equals(dataId))
               {
                  if (((ITableDataFilterPickList) tableDataFilter).getSelected() != null)
                  {
                     FilterOrTerm or = filter.addOrTerm();
                     if (((ITableDataFilterPickList) tableDataFilter).getSelected().size() > 0)
                     {
                        for (int i = 0; i < ((ITableDataFilterPickList) tableDataFilter).getSelected().size(); i++)
                        {
                           String status = ((ITableDataFilterPickList) tableDataFilter).getSelected().get(i).toString();
                           Integer actState = Integer.parseInt(status);
                           if (query instanceof ActivityInstanceQuery)
                           {
                              or.add(ActivityInstanceQuery.STATE.isEqual(Long.parseLong(status.toString())));
                           }
                           else if (query instanceof WorklistQuery)
                           {
                              //Worklist Query uses ActivityStateFilter.
                              or.add(new ActivityStateFilter(ActivityInstanceState.getState(actState)));
                           }
                        }
                     }
                  }
               }// Filtering by descriptors
               else if (allDescriptors.containsKey(dataId))
               {
                  query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);

                  if (null == filterModel)
                  {
                     filterModel = GenericDescriptorFilterModel.create(allDescriptors.values());
                     filterModel.setFilterEnabled(true);
                  }
                  filterModel.setFilterValue(dataId,
                        DescriptorColumnUtils.getFilterValue(tableDataFilter, allDescriptors.get(dataId)));
               }
               else
               {
                  trace
                        .info("ProcessTableFilterAndSortHandler.applyFiltering() : Filtering not implemented for "
                              + tableDataFilter);
               }
            }
         }
        
         if (null != filterModel)
         {
            DescriptorFilterUtils.applyFilters(query, filterModel);
         }
      }
   }
   
   /**
    * @author Subodh.Godbole
    *
    */
   public class SortHandler extends IppSortHandler
   {
      private static final long serialVersionUID = 1L;

      @Override
      public void applySorting(Query query, List<SortCriterion> sortCriteriaList)
      {
         Iterator<SortCriterion> iterator = sortCriteriaList.iterator();

         boolean worklistQuery = query instanceof WorklistQuery;

         // As per current Architecture, this list will hold only one item
         if (iterator.hasNext())
         {
            SortCriterion sortCriterion = iterator.next();
            if (trace.isDebugEnabled())
            {
               trace.debug("sortCriterion = " + sortCriterion);
            }
            if ("oid".equals(sortCriterion.getProperty()))
            {
               query.orderBy(worklistQuery
                     ? WorklistQuery.ACTIVITY_INSTANCE_OID
                     : ActivityInstanceQuery.OID, sortCriterion.isAscending());
            }
            else if ("criticality".equals(sortCriterion.getProperty()))
            {
               query.orderBy(worklistQuery
                     ? WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY
                     : ActivityInstanceQuery.CRITICALITY, sortCriterion.isAscending());
            }
            else if ("priority".equals(sortCriterion.getProperty()))
            {
               query.orderBy(worklistQuery
                     ? WorklistQuery.PROCESS_INSTANCE_PRIORITY
                     : ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY, sortCriterion.isAscending());
            }
            else if ("lastModificationTime".equals(sortCriterion.getProperty()))
            {
               query.orderBy(worklistQuery
                     ? WorklistQuery.LAST_MODIFICATION_TIME
                     : ActivityInstanceQuery.LAST_MODIFICATION_TIME, sortCriterion
                     .isAscending());
            }
            else if ("startDate".equals(sortCriterion.getProperty()))
            {
               query.orderBy(worklistQuery
                     ? WorklistQuery.START_TIME
                     : ActivityInstanceQuery.START_TIME, sortCriterion.isAscending());
            }
            else if ("processName".equals(sortCriterion.getProperty()))
            {
               CustomOrderCriterion o = ActivityInstanceQuery.ACTIVITY_NAME
                     .ascendig(sortCriterion.isAscending());
               query.orderBy(o);
            }
            // Is this a descriptor column?
            else if (sortCriterion.getProperty().startsWith("descriptorValues."))
            {
               String[] descriptorNames = sortCriterion.getProperty().split("\\.");
               String descriptorId = descriptorNames[1];
               if (allDescriptors.containsKey(descriptorId))
               {
                  DescriptorFilterUtils.applySorting(query, descriptorId, allDescriptors.get(descriptorId),
                        sortCriterion.isAscending());
               }
            }
            else
            {
               if (trace.isDebugEnabled())
               {
                  trace.debug("ProcessTableFilterAndSortHandler.applySorting() : Sorting not implemented for " + sortCriterion);
               }
            }
         }
      }
   }

   /**
    * @author Subodh.Godbole
    *
    */
   public class ColumnModelListener implements IColumnModelListener
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.column.IColumnModelListener#columnsRearranged(org.eclipse.stardust.ui.web.common.column.IColumnModel)
       */
      public void columnsRearranged(IColumnModel columnModel)
      {
         handleNewlyAddedDescriptorColumns(columnModel);
      }
      
      /**
       * Refresh the table if a descriptor column or the "descriptors" column is newly added.
       * Doesn't refresh if any descriptor columns are newly added but the "descriptors" column was already visible.
       *   
       * @param columnModel
       */
      private void handleNewlyAddedDescriptorColumns(IColumnModel columnModel)
      {
         initializeSelectiveDescriptorFetchProperties();
         boolean descriptorsColWasVisibleBefore = false;
         boolean hasNewlyAddedDescColumns = false;
         List<ColumnPreference> colPrefs = columnModel.getSelectableColumns();
         for (ColumnPreference colPref : colPrefs)
         {
            if (allDescriptors.containsKey(colPref.getColumnName()) && colPref.isNewlyVisible())
            {
               hasNewlyAddedDescColumns = true;
            }
            
            if (COL_DESCRIPTORS.equals(colPref.getColumnName()) && colPref.isVisible())
            {
               if (colPref.isNewlyVisible())
               {
                  hasNewlyAddedDescColumns = true;
               }
               else
               {
                  descriptorsColWasVisibleBefore = true;
               }
            }
         }

         if (!descriptorsColWasVisibleBefore && hasNewlyAddedDescColumns)
         {
            refresh();
         }
      }
   }

   /**
    * @author Subodh.Godbole
    *
    * @param <T>
    */
   private class WorklistExportHandler implements DataTableExportHandler<WorklistTableEntry>
   {
      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleCellExport(org.eclipse.stardust.ui.web.common.table.export.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.Object, java.lang.Object)
       */
      public Object handleCellExport(ExportType exportType, ColumnPreference column, WorklistTableEntry row,
            Object value)
      {
         if (COL_ACTIVITY_NAME.equals(column.getColumnName()))
         {
            return row.getProcessName();
         }
         else if (COL_PRIORITY.equals(column.getColumnName()))
         {
            return ProcessInstanceUtils.getPriorityLabel(row.getProcessPriority());
         }
         else if (COL_DESCRIPTORS.equals(column.getColumnName()))
         {
            return DescriptorColumnUtils.exportDescriptors(row.getProcessDescriptorsList(),
                  ExportType.EXCEL == exportType ? "\n" : ", ");
         }
         else if ("Criticality".equals(column.getColumnName()))
         {
            return CriticalityConfigurationUtil.getCriticalityDisplayLabel(row.getCriticalityValue(), row.getCriticality());
         }
         else
         {
            return value;
         }
      }

      /* (non-Javadoc)
       * @see org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler#handleHeaderCellExport(org.eclipse.stardust.ui.web.common.table.export.DataTableExportHandler.ExportType, org.eclipse.stardust.ui.web.common.column.ColumnPreference, java.lang.String)
       */
      public String handleHeaderCellExport(ExportType exportType, ColumnPreference column, String text)
      {
         return text;
      }
   }
}