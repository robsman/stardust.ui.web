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
package org.eclipse.stardust.ui.web.admin.views.qualityassurance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.QualityAssuranceAdminServiceFacade;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.web.admin.AdminportalConstants;
import org.eclipse.stardust.ui.web.admin.ResourcePaths;
import org.eclipse.stardust.ui.web.admin.WorkflowFacade;
import org.eclipse.stardust.ui.web.admin.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.admin.messages.AdminMessagesPropertiesBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterSearch;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTableRowSelector;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.QualityAssuranceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * QA Management contain following 2 tables
 * 
 * The QA Activities table lists all of the Activities flagged for QA. The table lists all
 * Activities in the currently deployed models alphabetically by Model Name
 * 
 * The Departments Table is populated when the Activity selected in the QA Activities
 * table is scoped (performer is scoped). It allows a QA sample size to be specified for
 * each department. By default, the table is sorted alphabetically by Department.
 * 
 * @author Yogesh.Manware
 * 
 */
public class QualityAssuranceManagementBean extends UIComponentBean implements ViewEventHandler
{
   private static final long serialVersionUID = 8242543351364690206L;
   private SortableTable<QualityAssuranceActivityTableEntry> activityTable;
   private SortableTable<QualityAssuranceDepartmentTableEntry> departmentTable;
   private boolean showAbsoleteActivities = false;
   private boolean departmentTableInitialized = false;
   private WorkflowFacade workflowFacade;
   private Map<String, List<Department>> departmentsCache;
   private Activity selectedActivity;
   private QualityAssuranceAdminServiceFacade qualityAssuranceAdminService;

   public QualityAssuranceManagementBean()
   {
      super(ResourcePaths.V_qaManagement);
      initQAActivitiesTable();
   }

   public static QualityAssuranceManagementBean getInstance()
   {
      return (QualityAssuranceManagementBean) FacesUtils.getBeanFromContext("qaManagementBean");
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
         initialize();
      }
   }

   /*
    * Initialize activity table (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {
      qualityAssuranceAdminService = ServiceFactoryUtils.getQualityCheckAdminServiceFacade();
      activityTable.setList(getQAActivityTableEntries());
      activityTable.initialize();
   }

   /**
    * action: Toggles display of QA Activity/Performer pairs that do not exist in the
    * current model versions.
    */
   public void showHideAbsActivities()
   {
      if (showAbsoleteActivities)
      {
         showAbsoleteActivities = false;
      }
      else
      {
         showAbsoleteActivities = true;
      }
      initialize();
   }

   /**
    * Places selected row(s) in edit mode (QA% only). QA % will become a spin box with
    * values 0-100.
    */
   public void editActivityQA()
   {
      List<QualityAssuranceActivityTableEntry> activityList = activityTable.getList();
      for (QualityAssuranceActivityTableEntry activityTableEntry : activityList)
      {
         if (activityTableEntry.isSelectedRow())
         {
            activityTableEntry.setEditOn(true);
         }
      }
   }

   /**
    * Returns selected Activities count
    * 
    * @return
    */
   public int getSelectedActivitiesCount()
   {
      int count = 0;
      List<QualityAssuranceActivityTableEntry> activityList = activityTable.getList();
      for (QualityAssuranceActivityTableEntry activityTableEntry : activityList)
      {
         if (activityTableEntry.isSelectedRow())
            count++;
      }
      return count;
   }

   /**
    * Display/refresh department table for the given activity
    * 
    * @param activity
    */
   public void displayDepartmentTable(Activity activity)
   {
      selectedActivity = activity;
      initQADepartmentTable();
      departmentTable.setList(getQADepartmentTableEntries(activity));
      departmentTable.initialize();
   }

   /**
    * Places selected row(s) in edit mode (QA% only). QA % will become a spin box with
    * values 0-100.
    */
   public void editDepartmentQA()
   {
      List<QualityAssuranceDepartmentTableEntry> departments = departmentTable.getList();
      for (QualityAssuranceDepartmentTableEntry departmentTableEntry : departments)
      {
         if (departmentTableEntry.isSelectedRow())
         {
            departmentTableEntry.setEditOn(true);
         }
      }
   }

   /**
    * Returns selected departments count
    * 
    * @return
    */
   public int getSelectedDepartmentsCount()
   {
      int count = 0;
      List<QualityAssuranceDepartmentTableEntry> departments = departmentTable.getList();
      for (QualityAssuranceDepartmentTableEntry departmentTableEntry : departments)
      {
         if (departmentTableEntry.isSelectedRow())
            count++;
      }
      return count;
   }

   public void save()
   {
      boolean validationFailed = false;
      // validate Activity QAs
      List<QualityAssuranceActivityTableEntry> activityList = activityTable.getList();
      for (QualityAssuranceActivityTableEntry activityTableEntry : activityList)
      {
         if (activityTableEntry.isModified())
         {
            if (!QualityAssuranceUtils.isQAProbabilityValid(activityTableEntry.getQaPercentage()))
            {
               validationFailed = true;
               break;
            }
         }
      }
      // validate Department QAs
      List<QualityAssuranceDepartmentTableEntry> departments = departmentTable.getList();
      if (!validationFailed)
      {
         for (QualityAssuranceDepartmentTableEntry departmentTableEntry : departments)
         {
            if (departmentTableEntry.isModified())
            {
               if (!QualityAssuranceUtils.isQAProbabilityValid(departmentTableEntry.getQaPercentageD()))
               {
                  validationFailed = true;
                  break;
               }
            }
         }
      }

      if (validationFailed)
      {
         MessageDialog.addErrorMessage(AdminMessagesPropertiesBean.getInstance().getString(
               "views.qaManagementView.qaError"));
         return;
      }

      // save activity level QA percentage
      for (QualityAssuranceActivityTableEntry activityTableEntry : activityList)
      {
         if (activityTableEntry.isModified())
         {
            qualityAssuranceAdminService.setQualityAssuranceParticipantProbability(activityTableEntry.getActivity(),
                  null, QualityAssuranceUtils.getIntegerValueofQAProbability(activityTableEntry.getQaPercentage()));
         }
      }

      // save department level QA default
      for (QualityAssuranceDepartmentTableEntry departmentTableEntry : departments)
      {
         if (departmentTableEntry.isModified())
         {
            qualityAssuranceAdminService.setQualityAssuranceParticipantProbability(departmentTableEntry.getActivity(),
                  departmentTableEntry.getDepartment(),
                  QualityAssuranceUtils.getIntegerValueofQAProbability(departmentTableEntry.getQaPercentageD()));
         }
      }
      initialize();
      displayDepartmentTable(selectedActivity);
   }

   /**
    * initialize QA Activity Table
    */
   private void initQAActivitiesTable()
   {
      // model id
      ColumnPreference modelCol = new ColumnPreference("modelName", "modelName", AdminMessagesPropertiesBean
            .getInstance().getString("views.qaManagementView.modelName.label"),
            ResourcePaths.V_QA_ACTIVITIES_TABLE_COLUMNS, true, true);

      modelCol.setColumnDataFilterPopup(new TableDataFilterPopup(new TableDataFilterSearch("", "", true, "")));

      // process name
      ColumnPreference processCol = new ColumnPreference("processName", "processName", MessagesViewsCommonBean
            .getInstance().getString("views.processTable.column.processName"),
            ResourcePaths.V_QA_ACTIVITIES_TABLE_COLUMNS, true, true);

      processCol.setColumnDataFilterPopup(new TableDataFilterPopup(new TableDataFilterSearch("", "", true, "")));

      // activity name
      ColumnPreference activityCol = new ColumnPreference("activityName", "activityName", MessagesViewsCommonBean
            .getInstance().getString("views.activityTable.column.activityName"),
            ResourcePaths.V_QA_ACTIVITIES_TABLE_COLUMNS, true, true);
      

      activityCol.setColumnDataFilterPopup(new TableDataFilterPopup(new TableDataFilterSearch("", "", true, "")));

      // default performer
      ColumnPreference defaultPerformerCol = new ColumnPreference("defaultPerformer", "defaultPerformer",
            AdminMessagesPropertiesBean.getInstance().getString("views.qaManagementView.defaultPerformer.label"),
            ResourcePaths.V_QA_ACTIVITIES_TABLE_COLUMNS, true, true);

      defaultPerformerCol
            .setColumnDataFilterPopup(new TableDataFilterPopup(new TableDataFilterSearch("", "", true, "")));

      // qa percentage
      ColumnPreference qaPercentageCol = new ColumnPreference("qaPercentage", "qaPercentage",
            AdminMessagesPropertiesBean.getInstance().getString("views.qaManagementView.qaPercentage.label"),
            ResourcePaths.V_QA_ACTIVITIES_TABLE_COLUMNS, true, true);
      qaPercentageCol.setColumnAlignment(ColumnAlignment.CENTER);

      List<ColumnPreference> activityCols = new ArrayList<ColumnPreference>();
      activityCols.add(modelCol);
      activityCols.add(processCol);
      activityCols.add(activityCol);
      activityCols.add(defaultPerformerCol);
      activityCols.add(qaPercentageCol);

      IColumnModel activityColumnModel = new DefaultColumnModel(activityCols, null, null,
            UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_QA_ACTIVITY_VIEW);

      activityTable = new SortableTable<QualityAssuranceActivityTableEntry>(activityColumnModel, null,
            new SortableTableComparator<QualityAssuranceActivityTableEntry>("modelName", true));

      activityTable.setRowSelector(new DataTableRowSelector("selectedRow", true));
      activityTable.initialize();
   }

   /**
    * @return List<QAActivityTableEntry>
    */
   @SuppressWarnings("unchecked")
   private List<QualityAssuranceActivityTableEntry> getQAActivityTableEntries()
   {
      List<QualityAssuranceActivityTableEntry> activityEntries = CollectionUtils.newList();
      if (isShowAbsoleteActivities()) // Show old model's activities
      {
         activityEntries = getQAActivityEntries();
      }
      else
      {// show only latest activities
         List<ProcessDefinition> allProcesses = ProcessDefinitionUtils.getAllProcessDefinitionsOfActiveModels();
         List<Activity> activities;
         for (ProcessDefinition processDefinition : allProcesses)
         {
            activities = processDefinition.getAllActivities();
            for (Activity activity : activities)
            {
               if (activity.isQualityAssuranceEnabled())
               {
                  activityEntries.add(new QualityAssuranceActivityTableEntry(activity, qualityAssuranceAdminService
                        .getQualityAssuranceParticipantProbability(activity, null)));
               }
            }
         }
      }
      return activityEntries;
   }

   /**
    * initialize department table
    */
   private void initQADepartmentTable()
   {
      if (!departmentTableInitialized)
      {
         // department name
         ColumnPreference departmentCol = new ColumnPreference("deptName", "deptName", AdminMessagesPropertiesBean
               .getInstance().getString("views.qaManagementView.department.label"),
               ResourcePaths.V_QA_DEPARTMENT_TABLE_COLUMNS, true, true);

         departmentCol.setColumnDataFilterPopup(new TableDataFilterPopup(new TableDataFilterSearch("", "", true, "")));

         // qa percentage
         ColumnPreference qaPercentageCol = new ColumnPreference("qaPercentageD", "qaPercentageD",
               AdminMessagesPropertiesBean.getInstance().getString("views.qaManagementView.qaPercentage.label"),
               ResourcePaths.V_QA_DEPARTMENT_TABLE_COLUMNS, true, true);
         qaPercentageCol.setColumnAlignment(ColumnAlignment.CENTER);

         List<ColumnPreference> departmentCols = new ArrayList<ColumnPreference>();
         departmentCols.add(departmentCol);
         departmentCols.add(qaPercentageCol);

         IColumnModel departmentsColumnModel = new DefaultColumnModel(departmentCols, null, null,
               UserPreferencesEntries.M_ADMIN, UserPreferencesEntries.V_QA_DEPARTMENT_VIEW);

         departmentTable = new SortableTable<QualityAssuranceDepartmentTableEntry>(departmentsColumnModel, null,
               new SortableTableComparator<QualityAssuranceDepartmentTableEntry>("deptName", true));

         departmentTable.setRowSelector(new DataTableRowSelector("selectedRow", true));
         departmentTable.initialize();

         departmentsCache = new HashMap<String, List<Department>>();
         workflowFacade = (WorkflowFacade) SessionContext.findSessionContext().lookup(
               AdminportalConstants.WORKFLOW_FACADE);

         departmentTableInitialized = true;
      }
   }

   /**
    * This will look in department cache first, if not available retrieves department from
    * database
    * 
    * @return List<QADepartmentTableEntry>
    */
   private List<QualityAssuranceDepartmentTableEntry> getQADepartmentTableEntries(Activity activity)
   {
      ModelParticipant participant = activity.getDefaultPerformer();
      List<QualityAssuranceDepartmentTableEntry> departments = CollectionUtils.newList();
      // detect if the participant is scoped
      if (participant.isDepartmentScoped())
      {
         OrganizationInfo orgInfo = (OrganizationInfo) participant;
         List<Department> deptList;
         if (departmentsCache.containsKey(orgInfo.getQualifiedId()))
         {
            deptList = departmentsCache.get(orgInfo.getQualifiedId());
         }
         else
         {
            QueryService qs = getQryService();
            deptList = qs.findAllDepartments(orgInfo.getDepartment(), orgInfo);
            departmentsCache.put(orgInfo.getQualifiedId(), deptList);
         }

         for (Department department : deptList)
         {
            departments.add(new QualityAssuranceDepartmentTableEntry(activity, department, qualityAssuranceAdminService
                  .getQualityAssuranceParticipantProbability(activity, department)));
         }
      }
      return departments;
   }

   /**
    * returns all activities across all model versions without having duplicate
    * activity-performer pair
    * 
    * @param ws
    * @param processQualifiedId
    * @param doFilterAccess
    * @return
    */
   @SuppressWarnings("unchecked")
   private List<QualityAssuranceActivityTableEntry> getQAActivityEntries()
   {
      WorkflowService workflowService = ServiceFactoryUtils.getWorkflowService();
      Map<String, QualityAssuranceActivityTableEntry> activityEntries = CollectionUtils.newHashMap();
      // get all models
      List<DeployedModel> models = CollectionUtils.newArrayList(ModelUtils.getAllModels());
      List<ProcessDefinition> processes;
      List<Activity> activities, filteredActivities;

      for (DeployedModel model : models)
      {
         // get all process definitions from the model
         processes = model.getAllProcessDefinitions();
         for (ProcessDefinition processDefinition : processes)
         {
            // get all activities from process
            activities = processDefinition.getAllActivities();
            filteredActivities = ActivityUtils.filterAccessibleActivities(workflowService, activities);
            // search quality assured activities
            for (Activity activity : filteredActivities)
            {
               if (activity.isQualityAssuranceEnabled())
               {
                  QualityAssuranceActivityTableEntry entry = new QualityAssuranceActivityTableEntry(activity,
                        qualityAssuranceAdminService.getQualityAssuranceParticipantProbability(activity, null));
                  entry.setModelStatus(model.isActive());
                  activityEntries.put(activity.getQualifiedId() + activity.getDefaultPerformer().getId(), entry);
               }
            }
         }
      }
      return CollectionUtils.newArrayList(activityEntries.values());
   }

   /**
    * @return
    */
   private QueryService getQryService()
   {
      return workflowFacade.getQueryService();
   }

   public SortableTable<QualityAssuranceActivityTableEntry> getActivityTable()
   {
      return activityTable;
   }

   public boolean isShowAbsoleteActivities()
   {
      return showAbsoleteActivities;
   }

   public SortableTable<QualityAssuranceDepartmentTableEntry> getDepartmentTable()
   {
      return departmentTable;
   }

   public boolean isDepartmentTableInitialized()
   {
      return departmentTableInitialized;
   }
}
