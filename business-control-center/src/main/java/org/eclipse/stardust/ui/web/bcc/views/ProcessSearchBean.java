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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Direction;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.bcc.ActivitySearchProvider;
import org.eclipse.stardust.ui.web.bcc.ProcessSearchProvider;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.ActivitySearchProvider.ActivityFilterAttributes;
import org.eclipse.stardust.ui.web.bcc.ProcessSearchProvider.FilterAttributes;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterLocalizerKey;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.common.GenericDataMapping;
import org.eclipse.stardust.ui.web.viewscommon.common.Localizer;
import org.eclipse.stardust.ui.web.viewscommon.common.ValidationMessageBean;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationHelper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DataMappingWrapper;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.helper.activityTable.ActivityTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.helper.processTable.ProcessTableHelper;
import org.eclipse.stardust.ui.web.viewscommon.user.UserAutocompleteMultiSelector;
import org.eclipse.stardust.ui.web.viewscommon.user.UserWrapper;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.DateUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.IceComponentUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * @author Yogesh.Manware
 * 
 */
@SuppressWarnings("unchecked")
public class ProcessSearchBean extends UIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   public static final String BEAN_ID = "processSearchBean";
   private static final Logger trace = LogManager.getLogger(ProcessSearchBean.class);
   public static final String INTERACTIVE_ACTIVITIES = "Interactive Activities";
   public static final String NONINTERACT_ACTIVITIES = "Non-interactive Activities";
   public static final String AUXILIARY_ACTIVITIES = "Auxiliary Activities";
   public static final String AUXILIARY_PROCESSES = "Auxiliary Processes";

   // Case related
   public static final String ALL_PROCESS_SEARCH = "ALL_PROCESS";
   public static final String CASE_SEARCH = "CASE";
   public static final String ROOT_PROC_SEARCH = "ROOT_PROCESS";

   private static enum SEARCH_OPTION {
      PROCESSES, ACTIVITIES
   }

   private List<SelectItem> searchOptions;
   private SEARCH_OPTION selectedSearchOption;
   private boolean expandSearchCriteria = true;
   private ValidationMessageBean validationMessageBean;

   // Processes
   private ProcessTableHelper processTableHelper;
   private ProcessSearchProvider processSearchProvider;
   private Map<String, ProcessDefinition> processDefinitions;
   private List<SelectItem> processes;
   private List<SelectItem> processStates;
   private List<FilterToolbarItem> processFilterToolbarItems;
   private String[] selectedProcesses;
   private boolean processTableInitialized = false;
   private List<SelectItem> priorityList;

   // Activities
   private ActivityTableHelper activityTableHelper;
   private ActivitySearchProvider activitySearchProvider;
   private Map<String, Activity> activityDefinitions;
   private List<SelectItem> activities;
   private List<SelectItem> activityStates;
   private List<FilterToolbarItem> activityFilterToolbarItems;
   private String[] selectedActivities;
   private UserAutocompleteMultiSelector performerSelector;
   private boolean activityTableInitialized = false;
   private boolean activityCriteriaInitialized = false;
   private List<SelectItem> criticalityList;

   // Descriptors
   private List<DataMappingWrapper> descriptorItems = new ArrayList<DataMappingWrapper>();
   private List<DataMappingWrapper> caseDescriptorItems = new ArrayList<DataMappingWrapper>();
   private DataPath[] commonDescriptors;

   // Case
   private UserAutocompleteMultiSelector ownerSelector;
   private String hierarchyFilter; 
  
   public ProcessSearchBean()
   {
      super(ResourcePaths.V_processSearch);
   }

   /**
    * @return ProcessSearchBean object
    */
   public static ProcessSearchBean getInstance()
   {
      return (ProcessSearchBean) FacesContext.getCurrentInstance().getApplication().getVariableResolver()
            .resolveVariable(FacesContext.getCurrentInstance(), BEAN_ID);
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * org.eclipse.stardust.ui.web.common.event.ViewEventHandler#handleEvent(com.sungard.framework
    * .ui.event.ViewEvent)
    */
   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {
         trace.debug("Initializing ProcessSearchBean...");
         initializeBean();
      }
   }

   /**
    * @param event
    */
   public void searchOptionChangeListener(ValueChangeEvent event)
   {
      if (new Integer(1).equals(event.getNewValue()))
      {
         selectedSearchOption = SEARCH_OPTION.ACTIVITIES;
         initializeActivityCriteria();
         refreshActivities(getSelectedProcessDefs());
      }
      else
      {
         selectedSearchOption = SEARCH_OPTION.PROCESSES;
      }
      // Will reset the hierarchy filter on change from Activity to Process
      setHierarchyValue(ALL_PROCESS_SEARCH);
      // This is just to remove/add case related descriptors
      // TODO: Review following code - separate filter can be used show/hide case
      // descriptors
      refreshDescriptorTable(getSelectedProcessDefs());
   }

   /**
    * Searches all ProcessInstances/Activity Instances by applying the filter attributes
    * and the specified descriptor values.
    */
   public void performSearch()
   {
      validationMessageBean.reset();
      
      // validate Descriptor of type Date
      for (DataMappingWrapper dataMappingWrapper : descriptorItems)
      {
         if (!DateUtils.validateDateRange(dataMappingWrapper.getFromDateValue(), dataMappingWrapper.getToDateValue()))
         {
            validationMessageBean.addError(this.getMessages().getString("DateRangeError"),
                  "from" + dataMappingWrapper.getId(), "to" + dataMappingWrapper.getId());
         }
      }
      // process search
      if (SEARCH_OPTION.PROCESSES.equals(selectedSearchOption))
      {
         // Validate start time and end time
         if (!DateUtils.validateDateRange(getFilterAttributes().getStartedFrom(), getFilterAttributes().getStartedTo()))
         {
            validationMessageBean.addError(this.getMessages().getString("startedDateRangeError"), "startedFrom",
                  "startedTo");
         }

         if (!DateUtils.validateDateRange(getFilterAttributes().getEndTimeFrom(), getFilterAttributes().getEndTimeTo()))
         {
            validationMessageBean.addError(this.getMessages().getString("processSearch.endDateRangeError"),
                  "endTimeFrom", "endTimeTo");
         }

         if (validationMessageBean.isContainMessages())
         {
            return;
         }

         // set case attributes
         if (getFilterAttributes().isIncludeCase() & ownerSelector.getSelectedValue() != null)
         {
            UserWrapper userWrapper = ownerSelector.getSelectedValue();
            if (userWrapper != null)
            {
               User u = userWrapper.getUser();
               if (u != null)
               {
                  getFilterAttributes().setUser(u);
               }
            }
         }
         else
         {
            getFilterAttributes().setUser(null);
         }

         initializeProcessTable();
         processSearchProvider.setSelectedProcesses(getSelectedProcessDefs(), descriptorItems, commonDescriptors);
         processTableHelper.getProcessTable().refresh(true);
      }

      // activity search
      if (SEARCH_OPTION.ACTIVITIES.equals(selectedSearchOption))
      {
         getFilterAttributes().setIncludeCase(false);

         // Validate start time and end time
         if (!DateUtils.validateDateRange(getActivityFilterAttributes().getStartedFrom(), getActivityFilterAttributes()
               .getStartedTo()))
         {
            validationMessageBean.addError(this.getMessages().getString("startedDateRangeError"),
                  "actStartedFrom", "actStartedTo");
         }

         if (!DateUtils.validateDateRange(getActivityFilterAttributes().getModifyTimeFrom(),
               getActivityFilterAttributes().getModifyTimeTo()))
         {
            validationMessageBean.addError(this.getMessages().getString("activitySearch.modifyDateRangeError"),
                  "actModifyTimeFrom", "actModifyTimeTo");
         }

         UserWrapper userWrapper = performerSelector.getSelectedValue();
         if (userWrapper != null)
         {
            User u = userWrapper.getUser();
            if (u != null)
            {
               getActivityFilterAttributes().setUser(u);
            }
         }
         else if (StringUtils.isNotEmpty(performerSelector.getSearchValue()))
         {
            validationMessageBean.addError(this.getMessages().getString("activitySearch.invalidPerformerError"),
                  "performerId");
         }
         else
         {
            getActivityFilterAttributes().setUser(null);
         }
         
         if (validationMessageBean.isContainMessages())
         {
            return;
         }
         
         initializeActivityTable();
         activitySearchProvider.setSelectedActivities(getSelectedActivityDefs(), descriptorItems, commonDescriptors);
         activityTableHelper.getActivityTable().refresh(true);
      }
      
      expandSearchCriteria = false;
   }

   /**
    * toggleProcessFilter
    * 
    * @param ae
    */
   public void toggleProcessFilter(ActionEvent ae)
   {
      toggleFilter(processFilterToolbarItems, ae);
      evaluateProcesses();
      List<ProcessDefinition> selectedProcessDefs = getSelectedProcessDefs();
      refreshDescriptorTable(selectedProcessDefs);
      refreshActivities(selectedProcessDefs);
   }

   /**
    * Selected Processs change listener
    * 
    * @param event
    */
   public void selectedProcessesChangeListener(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      selectedProcesses = (String[]) event.getNewValue();
      List<ProcessDefinition> selectedProcessDefs = getSelectedProcessDefs();
      refreshDescriptorTable(selectedProcessDefs);
      refreshActivities(selectedProcessDefs);
   }

   /**
    * toggle Activity Filter
    * 
    * @param ae
    */
   public void toggleActivityFilter(ActionEvent ae)
   {
      toggleFilter(activityFilterToolbarItems, ae);
      refreshActivities(getSelectedProcessDefs());
   }

   /**
    * Opens notes dialog
    * 
    * @param ae
    */
   public void openNotes(ActionEvent ae)
   {
      ActivityInstance ai = (ActivityInstance) ae.getComponent().getAttributes().get("activityInstance");
      if (null != ai)
      {
         ProcessInstanceUtils.openNotes(ai.getProcessInstance());
      }
   }

   /**
    * action method to reset filter attribute
    */
   public void resetSearch()
   {
      if (SEARCH_OPTION.ACTIVITIES.equals(selectedSearchOption))
      {
         activityCriteriaInitialized = false;
      }
      initializeBean();

   }

   /**
    * sets (filtered)process list on search criteria based on applied filters
    */
   private void evaluateProcesses()
   {
      processes = new ArrayList<SelectItem>();
      List<ProcessDefinition> processDefinitionsList;
      processDefinitions = new HashMap<String, ProcessDefinition>();

      if (isAuxiliaryProcessesSwitchOn())
      {
         processDefinitionsList = ProcessDefinitionUtils.getAllAccessibleProcessDefinitions();
      }
      else
      {
         processDefinitionsList = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();
      }

      for (ProcessDefinition pd : processDefinitionsList)
      {
         processDefinitions.put(pd.getQualifiedId(), pd);
         processes.add(new SelectItem(pd.getQualifiedId(), I18nUtils.getProcessName(pd)));
      }
      // sort process in ascending order
      Collections.sort(processes, IceComponentUtil.SELECT_ITEM_ORDER);
      processes.add(0, new SelectItem("ALL", Localizer.getString(BusinessControlCenterLocalizerKey.ALL_PROCESSES)));

      // set to 'All Processes'
      selectedProcesses = new String[1];
      selectedProcesses[0] = "ALL";
   }

   /**
    * @return selected process definitions
    */
   private List<ProcessDefinition> getSelectedProcessDefs()
   {
      List<ProcessDefinition> selectedProcessDefs = new ArrayList<ProcessDefinition>();

      for (int i = 0; i < selectedProcesses.length; i++)
      {
         if (selectedProcesses[i].equals("ALL")) // all processes selected
         {
            selectedProcessDefs = new ArrayList<ProcessDefinition>(processDefinitions.values());
            break;
         }
         selectedProcessDefs.add(processDefinitions.get(selectedProcesses[i]));
      }
      return selectedProcessDefs;
   }

   /**
    * sets (filtered) activity list on search criteria based on applied filters
    * 
    * @param selectedProcessDefs
    */
   private void evaluateActivities(List<ProcessDefinition> selectedProcessDefs)
   {
      activities = new ArrayList<SelectItem>();
      activityDefinitions = new HashMap<String, Activity>();
      List<Activity> processesActivities = new ArrayList<Activity>();
      List<Activity> allActivities;
      for (ProcessDefinition processDefinition : selectedProcessDefs)
      {
         allActivities = ActivityUtils.getAllActivities(ServiceFactoryUtils.getWorkflowService(),
               processDefinition.getQualifiedId(), true);
         List<Activity> filteredActivities = applyActivityFilters(allActivities);
         processesActivities.addAll(filteredActivities);
      }

      if (CollectionUtils.isNotEmpty(processesActivities))
      {
         for (Activity activity : processesActivities)
         {
            String uniqueKey = ActivityUtils.getActivityKey(activity);
            activityDefinitions.put(uniqueKey, activity);
            activities.add(new SelectItem(uniqueKey, I18nUtils.getActivityName(activity)));
         }
      }
      // sort activities in ascending order
      Collections.sort(activities, IceComponentUtil.SELECT_ITEM_ORDER);
      activities.add(0, new SelectItem("ALL", getMessages().getString("allActivities")));

      // set to "All Activities
      selectedActivities = new String[1];
      selectedActivities[0] = "ALL";
   }

   /**
    * @return selected activities
    */
   private List<Activity> getSelectedActivityDefs()
   {
      List<Activity> selectedActivityDefs = new ArrayList<Activity>();

      for (int i = 0; i < selectedActivities.length; i++)
      {
         if (selectedActivities[i].equals("ALL")) // all activities selected
         {
            selectedActivityDefs = new ArrayList<Activity>(activityDefinitions.values());
            break;
         }
         selectedActivityDefs.add(activityDefinitions.get(selectedActivities[i]));
      }
      return selectedActivityDefs;
   }

   /**
    * 
    * @param allActivities
    * @return
    */
   private List<Activity> applyActivityFilters(List<Activity> allActivities)
   {
      List<Activity> filteredActivities = allActivities;
      if (!isInteractiveActivitiesSwitchOn())
      {
         filteredActivities = ActivityUtils.filterInteractiveActivities(filteredActivities, true);
      }
      if (!isNonInteractiveActivitiesSwitchOn())
      {
         filteredActivities = ActivityUtils.filterInteractiveActivities(filteredActivities, false);
      }
      if (!isAuxiliaryActivitiesSwitchOn())
      {
         filteredActivities = ActivityUtils.filterAuxiliaryActivities(filteredActivities);
      }
      return filteredActivities;
   }

   /**
    * Initialized activity table and sets (filtered) activity list on search criteria
    * based on applied filters
    * 
    * @param selectedProcessDefs
    */
   private void refreshActivities(List<ProcessDefinition> selectedProcessDefs)
   {
      if (SEARCH_OPTION.ACTIVITIES.equals(selectedSearchOption))
      {
         evaluateActivities(selectedProcessDefs);
      }
   }

   /**
    * Evaluate the descriptors to be displayed
    */
   private void refreshDescriptorTable(List<ProcessDefinition> selectedProcessDefs)
   {
      descriptorItems.clear();
      if (CollectionUtils.isNotEmpty(selectedProcessDefs))
      {
         commonDescriptors = CommonDescriptorUtils.getCommonDescriptors(selectedProcessDefs, true);
         GenericDataMapping mapping;
         DataMappingWrapper dmWrapper;
         boolean includeCaseDescriptor = false;

         for (int i = 0; i < commonDescriptors.length; i++)
         {
            if (!includeCaseDescriptor)
            {
               includeCaseDescriptor = checkCaseProcess(commonDescriptors[i]);
            }
            mapping = new GenericDataMapping(commonDescriptors[i]);
            dmWrapper = new DataMappingWrapper(mapping, null, false);
            descriptorItems.add(dmWrapper);
            dmWrapper.setDefaultValue(null);
         }

         // If selected Process doesn't contain Case Process, and Hierarchy is Case
         // RootPI add Case descriptors to filter criteria
         if (!includeCaseDescriptor
               & (SEARCH_OPTION.PROCESSES.equals(selectedSearchOption) & getFilterAttributes().isIncludeCase()))
         {
            descriptorItems.addAll(caseDescriptorItems);
         }
      }
   }

   /**
    * If selected Process is Case Process, return true
    * 
    * @param dp
    * @return
    */
   private boolean checkCaseProcess(DataPath dp)
   {
      if (dp.getData().equals(PredefinedConstants.CASE_DATA_ID))
      {
         return true;
      }
      else
         return false;
   }

   /**
    * 
    * @param hierarchy
    */
   private void setHierarchyValue(String hierarchy)
   {
      if (CASE_SEARCH.equals(hierarchy))
      {
         getFilterAttributes().setIncludeCase(true);
         ownerSelector.setDisabled(false);
         getFilterAttributes().setIncludeRootProcess(false);
      }
      else if (ROOT_PROC_SEARCH.equals(hierarchy))
      {
         getFilterAttributes().setIncludeCase(false);
         ownerSelector.setDisabled(true);
         getFilterAttributes().setIncludeRootProcess(true);
      }
      else
      {
         getFilterAttributes().setIncludeCase(false);
         ownerSelector.setDisabled(true);
         getFilterAttributes().setIncludeRootProcess(false);
      }
      ownerSelector.setSearchValue(null);
      hierarchyFilter = hierarchy;
   }

   /**
    * 
    * @param event
    */
   public void updateHierarchy(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      else
      {
         String selectedOption = event.getNewValue().toString();
         setHierarchyValue(selectedOption);
         // TODO: Review following code - separate filter can be used show/hide case
         // descriptors
         refreshDescriptorTable(getSelectedProcessDefs());
      }
   }

   /**
    * @param filterToolbarItems
    * @param ae
    */
   private void toggleFilter(List<FilterToolbarItem> filterToolbarItems, ActionEvent ae)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      String name = (String) context.getExternalContext().getRequestParameterMap().get("name");

      if (StringUtils.isEmpty(name))
      {
         name = (String) ae.getComponent().getAttributes().get("name");
      }
      for (FilterToolbarItem fTI : filterToolbarItems)
      {
         if (fTI.getName().equals(name))
         {
            fTI.setActive(fTI.isActive() ? false : true);
         }
      }
   }

   /**
    * Initialize Bean
    */
   private void initializeBean()
   {
      validationMessageBean = new ValidationMessageBean();
      initializeProcessCriteria();
      evaluateProcesses();
      refreshDescriptorTable(getSelectedProcessDefs());
   }

   /**
    * Initialize process criteria
    */
   private void initializeProcessCriteria()
   {
      // initialize search Options
      searchOptions = new ArrayList<SelectItem>();
      searchOptions.add(new SelectItem(SEARCH_OPTION.PROCESSES.ordinal(), this.getMessages().getString(
            "searchCriteria.processes")));
      searchOptions.add(new SelectItem(SEARCH_OPTION.ACTIVITIES.ordinal(), this.getMessages().getString(
            "searchCriteria.activities")));
      selectedSearchOption = SEARCH_OPTION.PROCESSES;

      processSearchProvider = new ProcessSearchProvider();
      
      // initialize process list filter toolbar
      processFilterToolbarItems = new ArrayList<FilterToolbarItem>();
      FilterToolbarItem auxiliaryProcessFilter = new FilterToolbarItem("0", AUXILIARY_PROCESSES,
            "processHistory.processTable.showAuxiliaryProcess", "processHistory.processTable.hideAuxiliaryProcess",
            "process_auxiliary.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
      auxiliaryProcessFilter.setActive(false);
      processFilterToolbarItems.add(auxiliaryProcessFilter);
      
      // initialize Process States
      processStates = new ArrayList<SelectItem>();
      processStates.add(new SelectItem(ProcessSearchProvider.PROCESS_INSTANCE_STATE_ALIVE, this.getMessages()
            .getString("chooseProcess.options.alive.label")));
      processStates.add(new SelectItem(ProcessSearchProvider.PROCESS_INSTANCE_STATE_COMPLETED, this.getMessages()
            .getString("chooseProcess.options.completed.label")));
      processStates.add(new SelectItem(ProcessSearchProvider.PROCESS_INSTANCE_STATE_ABORTED, this.getMessages()
            .getString("chooseProcess.options.aborted.label")));
      processStates.add(new SelectItem(ProcessSearchProvider.PROCESS_INSTANCE_STATE_INTERRUPTED, this.getMessages()
            .getString("chooseProcess.options.interrupted.label")));
      processStates.add(new SelectItem(ProcessSearchProvider.PROCESS_INSTANCE_STATE_ALL, this.getMessages().getString(
            "chooseProcess.options.all.label")));

      // Case
      hierarchyFilter = ALL_PROCESS_SEARCH;
      ownerSelector = new UserAutocompleteMultiSelector(false, true);
      ownerSelector.setShowOnlineIndicator(false);
      ownerSelector.setDisabled(true);
      setHierarchyValue(hierarchyFilter);
      // Priority
      priorityList = new ArrayList<SelectItem>();
      priorityList.add(new SelectItem(ProcessSearchProvider.ALL_PRIORITIES, this.getMessages().getString(
            "chooseProcess.options.all.label")));
      priorityList.add(new SelectItem(ProcessInstancePriority.HIGH, this.getMessages().getString(
            "chooseProcess.priority.high.label")));
      priorityList.add(new SelectItem(ProcessInstancePriority.NORMAL, this.getMessages().getString(
            "chooseProcess.priority.normal.label")));
      priorityList.add(new SelectItem(ProcessInstancePriority.LOW, this.getMessages().getString(
            "chooseProcess.priority.low.label")));

      // init case descriptors

      ProcessDefinition caseProcessDefinition = ProcessDefinitionUtils
            .getProcessDefinition(PredefinedConstants.CASE_PROCESS_ID);
      List<DataPath> caseDataPath = caseProcessDefinition.getAllDataPaths();
      caseDescriptorItems.clear();
      GenericDataMapping mapping;
      DataMappingWrapper dmWrapper;
      for (DataPath dp : caseDataPath)
      {
         if (Direction.IN.equals(dp.getDirection()) && dp.isDescriptor()
               && (DescriptorFilterUtils.isDataFilterable(dp)))
         {
            mapping = new GenericDataMapping(dp);
            dmWrapper = new DataMappingWrapper(mapping, null, false);
            caseDescriptorItems.add(dmWrapper);
            dmWrapper.setDefaultValue(null);
         }
      }
   }

   /**
    * Initialize process table
    */
   private void initializeProcessTable()
   {
      if (!processTableInitialized)
      {
         processTableHelper = new ProcessTableHelper();
         processTableHelper.setCallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               processTableHelper.getProcessTable().refresh(true);
            }
         });
         processTableHelper.initializeProcessTable();
         processTableInitialized = true;
      }
      // Needs to be called again when Reset is called
      processTableHelper.getProcessTable().setISearchHandler(processSearchProvider.getSearchHandler());
      processTableHelper.getProcessTable().initialize();
   }

   /**
    * Initialize Activity Criteria
    */
   private void initializeActivityCriteria()
   {
      if (!activityCriteriaInitialized)
      {
         activitySearchProvider = new ActivitySearchProvider();

         // initialize autocomplete Selector
         performerSelector = new UserAutocompleteMultiSelector(false, true);
         performerSelector.setShowOnlineIndicator(false);

         // initialize activity list filter toolbar
         int i = 0;
         FilterToolbarItem nonInteractActivity = new FilterToolbarItem("" + i++, NONINTERACT_ACTIVITIES,
               "processHistory.activityTable.showApplicationActivity",
               "processHistory.activityTable.hideApplicationActivity", "activity_application.png",
               Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
         nonInteractActivity.setActive(false);

         FilterToolbarItem interactActivity = new FilterToolbarItem("" + i++, INTERACTIVE_ACTIVITIES,
               "processHistory.activityTable.showManualActivity", "processHistory.activityTable.hideManualActivity",
               "activity_manual.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
         interactActivity.setActive(true);

         FilterToolbarItem auxActivity = new FilterToolbarItem("" + i++, AUXILIARY_ACTIVITIES,
               "processHistory.activityTable.showAuxiliaryActivity",
               "processHistory.activityTable.hideAuxiliaryActivity", "activity_auxiliary.png",
               Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
         auxActivity.setActive(false);

         activityFilterToolbarItems = new ArrayList<FilterToolbarItem>();
         activityFilterToolbarItems.add(nonInteractActivity);
         activityFilterToolbarItems.add(interactActivity);
         activityFilterToolbarItems.add(auxActivity);

         // initialize activity states
         activityStates = new ArrayList<SelectItem>();
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_ALIVE, this.getMessages()
               .getString("chooseProcess.options.alive.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_COMPLETED, this.getMessages()
               .getString("chooseProcess.options.completed.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_ABORTED, this.getMessages()
               .getString("chooseProcess.options.aborted.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_SUSPENDED, this.getMessages()
               .getString("chooseProcess.options.suspended.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_HIBERNATED, this
               .getMessages().getString("chooseProcess.options.hibernated.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_ABORTING, this.getMessages()
               .getString("chooseProcess.options.aborting.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_CREATED, this.getMessages()
               .getString("chooseProcess.options.created.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_APPLICATION, this
               .getMessages().getString("chooseProcess.options.application.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_INTERRUPTED, this
               .getMessages().getString("chooseProcess.options.interrupted.label")));
         activityStates.add(new SelectItem(ActivitySearchProvider.ACTIVITY_INSTANCE_STATE_ALL, this.getMessages()
               .getString("chooseProcess.options.all.label")));

         activityCriteriaInitialized = true;

         // Criticality
         criticalityList = new ArrayList<SelectItem>();
         List<CriticalityCategory> cCats = CriticalityConfigurationHelper.getInstance().getCriticalityConfiguration();
         criticalityList.add(new SelectItem(this.getMessages().getString("chooseProcess.options.all.label")));
         for (CriticalityCategory cCat : cCats)
         {
            criticalityList.add(new SelectItem(cCat.getLabel()));
         }
      }
   }

   /**
    * Initialize Activity Table
    */
   private void initializeActivityTable()
   {
      if (!activityTableInitialized)
      {
         // initialize activity table
         activityTableHelper = new ActivityTableHelper();
         activityTableHelper.setCallbackHandler(new ICallbackHandler()
         {
            public void handleEvent(EventType eventType)
            {
               activityTableHelper.getActivityTable().refresh(true);
            }
         });
         activityTableHelper.initActivityTable();
         activityTableHelper.setStrandedActivityView(false);
         activityTableInitialized = true;
      }
      activityTableHelper.getActivityTable().setISearchHandler(activitySearchProvider.getSearchHandler());
      activityTableHelper.getActivityTable().initialize();
   }

   private boolean isNonInteractiveActivitiesSwitchOn()
   {
      return isActivitySwitchOn(NONINTERACT_ACTIVITIES);
   }

   private boolean isInteractiveActivitiesSwitchOn()
   {
      return isActivitySwitchOn(INTERACTIVE_ACTIVITIES);
   }

   private boolean isAuxiliaryActivitiesSwitchOn()
   {
      return isActivitySwitchOn(AUXILIARY_ACTIVITIES);
   }

   private boolean isAuxiliaryProcessesSwitchOn()
   {
      return isProcessSwitchOn(AUXILIARY_PROCESSES);
   }

   private boolean isSwitchOn(List<FilterToolbarItem> processFilterToolbarItems, String switchName)
   {
      for (FilterToolbarItem filterItem : processFilterToolbarItems)
      {
         if (filterItem.getName().equals(switchName))
         {
            return filterItem.isActive();
         }
      }
      return false;
   }

   private boolean isProcessSwitchOn(String switchName)
   {
      return isSwitchOn(processFilterToolbarItems, switchName);
   }

   private boolean isActivitySwitchOn(String switchName)
   {
      return isSwitchOn(activityFilterToolbarItems, switchName);
   }

   @Override
   public void initialize()
   {}

   /**
    * Called from UI to enable/disable End Time and Root PI fields.
    * 
    * @return
    */
   public boolean isCaseSpecificSearch()
   {
      if (getFilterAttributes().isIncludeCase() & StringUtils.isNotEmpty(ownerSelector.getSearchValue()))
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public List<SelectItem> getProcessStates()
   {
      return processStates;
   }

   public List<SelectItem> getProcesses()
   {
      return processes;
   }

   public List<SelectItem> getActivities()
   {
      return activities;
   }

   public List<SelectItem> getActivityStates()
   {
      return activityStates;
   }

   public boolean isExpandSearchCriteria()
   {
      return expandSearchCriteria;
   }

   public void setExpandSearchCriteria(boolean expandSearchCriteria)
   {
      this.expandSearchCriteria = expandSearchCriteria;
   }

   public List<SelectItem> getSearchOptions()
   {
      return searchOptions;
   }

   public FilterAttributes getFilterAttributes()
   {
      return processSearchProvider.getFilterAttributes();
   }

   public List<DataMappingWrapper> getDescriptorItems()
   {
      return descriptorItems;
   }

   public int getSelectedSearchOption()
   {
      return selectedSearchOption.ordinal();
   }

   public void setSelectedSearchOption(int selectedSearchOption)
   {}

   public ActivityFilterAttributes getActivityFilterAttributes()
   {
      return activitySearchProvider.getFilterAttributes();
   }

   public UserAutocompleteMultiSelector getPerformerSelector()
   {
      return performerSelector;
   }

   public ProcessTableHelper getProcessTableHelper()
   {
      return processTableHelper;
   }

   public ActivityTableHelper getActivityTableHelper()
   {
      return activityTableHelper;
   }

   public List<FilterToolbarItem> getActivityFilterToolbarItems()
   {
      return activityFilterToolbarItems;
   }

   public List<FilterToolbarItem> getProcessFilterToolbarItems()
   {
      return processFilterToolbarItems;
   }

   public String[] getSelectedProcesses()
   {
      return selectedProcesses;
   }

   public void setSelectedProcesses(String[] selectedProcesses)
   {
      this.selectedProcesses = selectedProcesses;
   }

   public String[] getSelectedActivities()
   {
      return selectedActivities;
   }

   public void setSelectedActivities(String[] selectedActivities)
   {
      this.selectedActivities = selectedActivities;
   }

   public boolean isProcessTableInitialized()
   {
      return processTableInitialized;
   }

   public boolean isActivityTableInitialized()
   {
      return activityTableInitialized;
   }

   public List<SelectItem> getCriticalityList()
   {
      return criticalityList;
   }

   public List<SelectItem> getPriorityList()
   {
      return priorityList;
   }

   public String getHierarchyFilter()
   {
      return hierarchyFilter;
   }

   public void setHierarchyFilter(String hierarchyFilter)
   {
      this.hierarchyFilter = hierarchyFilter;
   }

   public UserAutocompleteMultiSelector getOwnerSelector()
   {
      return ownerSelector;
   }

   public Map<String, ProcessDefinition> getProcessDefinitions()
   {
      return processDefinitions;
   }

   public ValidationMessageBean getValidationMessageBean()
   {
      return validationMessageBean;
   }   

   /**
    * priorityChangeListener to update process priority for ActivitySearchProvider
    * 
    * @param event
    */
   public void priorityChangeListener(ValueChangeEvent event)
   {
      if (!event.getPhaseId().equals(javax.faces.event.PhaseId.INVOKE_APPLICATION))
      {
         event.setPhaseId(javax.faces.event.PhaseId.INVOKE_APPLICATION);
         event.queue();
         return;
      }
      else
      {
         if (null != activitySearchProvider && null != activitySearchProvider.getFilterAttributes())
         {
            int priority = getFilterAttributes().getPriority();
            activitySearchProvider.getFilterAttributes().setPriority(priority);
         }
      }
   }
   
   
}
