package org.eclipse.stardust.ui.web.viewscommon.common;

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
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterCustom;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class ProcessActivityDataFilter extends TableDataFilterCustom
{
   private static final long serialVersionUID = 1L;
   public static final String AUXILIARY_PROCESSES = "Auxiliary Processes";
   public static final String AUXILIARY_ACTIVITIES = "Auxiliary Activities";
   public static final String ALL = "ALL";

   private boolean initialized = false;

   // Processes
   private Map<String, ProcessDefinition> processDefinitions = new HashMap<String, ProcessDefinition>();
   private List<ProcessDefinition> allAccessibleProcesses = null;
   private List<ProcessDefinition> allBusinessRelevantProcesses = null;
   private List<SelectItem> processes = null;
   private String[] selectedProcesses;
   private List<FilterToolbarItem> processFilterToolbarItems;

   // Activities
   private Map<String, Activity> activityDefinitions = new HashMap<String, Activity>();
   private Map<String, List<Activity>> allAccessibleActivities = new HashMap<String, List<Activity>>();
   private Map<String, List<Activity>> allBusinessRelevantActivities = new HashMap<String, List<Activity>>();
   private List<SelectItem> activities = null;
   private String[] selectedActivities;
   private List<FilterToolbarItem> activityFilterToolbarItems;
   private boolean showActivityFilter = false;

   /**
    * @param contentUrl
    * @param showActivityFilter
    */
   public ProcessActivityDataFilter(String contentUrl, boolean showActivityFilter)
   {
      super(contentUrl);
      this.showActivityFilter = showActivityFilter;
   }

   /**
    * @param name
    * @param property
    * @param title
    * @param visible
    * @param contentUrl
    */
   private ProcessActivityDataFilter(String name, String property, String title, boolean visible, String contentUrl)
   {
      super(name, property, title, visible, contentUrl);
   }

   /**
    * @param vce
    */
   public void selectedProcessesChanged(ValueChangeEvent vce)
   {
      if (vce.getNewValue() == null)
      {
         return;
      }

      selectedProcesses = (String[]) vce.getNewValue();
      initActivities(getSelectedProcessDefs());
   }

   /**
    * @return
    */
   public final String[] getSelectedActivities()
   {
      if (selectedActivities == null || selectedActivities.length == 0)
      {
         resetSelectedActivities();
      }
      return selectedActivities;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.infinity.bpm.portal.common.filter.ITableDataFilter#isFilterSet()
    */
   public boolean isFilterSet()
   {
      if (showActivityFilter)
      {
         return ((selectedActivities != null) && (selectedActivities.length != 0));
      }
      else
      {
         return ((selectedProcesses != null) && (selectedProcesses.length != 0));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.infinity.bpm.portal.common.filter.ITableDataFilter#resetFilter()
    */
   public void resetFilter()
   {
      if (initialized)
      {
         initFilters();
         initProcesses();
         initActivities(getSelectedProcessDefs());

         this.selectedActivities = null;
         this.selectedProcesses = null;
      }
   }

   public List<SelectItem> getActivities()
   {
      return activities;
   }

   public final String[] getSelectedProcesses()
   {
      if (selectedProcesses == null || selectedProcesses.length == 0)
      {
         resetSelectedProcesses();
      }
      return selectedProcesses;
   }

   /**
    * toggle Activity Filter
    * 
    * @param ae
    */
   public void toggleActivityFilter(ActionEvent ae)
   {
      toggleFilter(activityFilterToolbarItems, ae);
      initActivities(getSelectedProcessDefs());
   }

   /**
    * toggleProcessFilter
    * 
    * @param ae
    */
   public void toggleProcessFilter(ActionEvent ae)
   {
      toggleFilter(processFilterToolbarItems, ae);
      initProcesses();
      initActivities(getSelectedProcessDefs());
   }

   /**
    * @return Activities
    */
   public List<Activity> getSelectedActivityDefs()
   {
      List<Activity> selectedActivityDefs = new ArrayList<Activity>();

      for (int i = 0; i < selectedActivities.length; i++)
      {
         if (ALL.equals(selectedActivities[i])) // all activities selected
         {
            selectedActivityDefs = new ArrayList<Activity>(activityDefinitions.values());
            break;
         }
         selectedActivityDefs.add(activityDefinitions.get(selectedActivities[i]));
      }

      return selectedActivityDefs;
   }

   public boolean contains(Object compareValue)
   {
      return true;
   }

   public List<SelectItem> getProcesses()
   {
      return processes;
   }

   public boolean isShowActivityFilter()
   {
      return showActivityFilter;
   }

   public final void setSelectedProcesses(String[] selectedProcesses)
   {
      this.selectedProcesses = selectedProcesses;
   }

   public List<FilterToolbarItem> getProcessFilterToolbarItems()
   {
      return processFilterToolbarItems;
   }

   public List<FilterToolbarItem> getActivityFilterToolbarItems()
   {
      return activityFilterToolbarItems;
   }

   public final void setSelectedActivities(String[] selectedActivities)
   {
      this.selectedActivities = selectedActivities;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.infinity.bpm.portal.common.filter.ITableDataFilter#getFilterSummaryTitle()
    */
   public String getFilterSummaryTitle()
   {
      String summary = "";
      if (showActivityFilter)
      {
         if (ALL.equals(selectedActivities[0]))
         {
            summary = MessagesViewsCommonBean.getInstance().get("messages.common.allActivities");
         }
         else
         {
            summary = I18nUtils.getActivityName(activityDefinitions.get(selectedActivities[0]));
         }

         if (selectedActivities.length > 1)
         {
            summary += "...";
         }
      }
      else
      {
         if (ALL.equals(selectedProcesses[0]))
         {
            summary = MessagesViewsCommonBean.getInstance().get("messages.common.allProcesses");
         }
         else
         {
            summary = I18nUtils.getProcessName(processDefinitions.get(selectedProcesses[0]));
         }
         if (selectedProcesses.length > 1)
         {
            summary += "...";
         }
      }
      return summary;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.infinity.bpm.portal.common.filter.ITableDataFilter#copyValues(com.infinity.bpm
    * .portal.common.filter.ITableDataFilter)
    */
   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      ProcessActivityDataFilter toCopy = (ProcessActivityDataFilter) dataFilterToCopy;

      // Processes
      this.processes = toCopy.processes;
      this.selectedProcesses = toCopy.selectedProcesses;
      this.processFilterToolbarItems = toCopy.processFilterToolbarItems;

      // Activities
      if (showActivityFilter)
      {
         this.activities = toCopy.activities;
         this.selectedActivities = toCopy.selectedActivities;
         this.activityFilterToolbarItems = toCopy.activityFilterToolbarItems;
         this.showActivityFilter = toCopy.showActivityFilter;
      }
   }

   /**
    * This is the first method which gets invoked when the user selects the column level
    * filter
    * 
    * @see com.infinity.bpm.portal.common.filter.ITableDataFilter#getClone()
    */

   public ITableDataFilter getClone()
   {
      if (!initialized)
      {
         initialized = true;
         resetFilter();
      }

      ProcessActivityDataFilter thisClone = new ProcessActivityDataFilter(getName(), getProperty(), getTitle(),
            isVisible(), this.contentUrl);

      thisClone.initialized = this.initialized;

      // Processes
      thisClone.processDefinitions = this.processDefinitions;
      thisClone.allAccessibleProcesses = this.allAccessibleProcesses;
      thisClone.allBusinessRelevantProcesses = this.allBusinessRelevantProcesses;

      thisClone.processes = getSelectItemClone(this.processes);
      thisClone.selectedProcesses = this.selectedProcesses == null ? null : this.selectedProcesses.clone();
      thisClone.processFilterToolbarItems = getFilterToolbarClone(this.processFilterToolbarItems);

      // Activities
      if (showActivityFilter)
      {
         thisClone.activityDefinitions = this.activityDefinitions;
         thisClone.allAccessibleActivities = this.allAccessibleActivities;
         thisClone.allBusinessRelevantActivities = this.allBusinessRelevantActivities;

         thisClone.activities = getSelectItemClone(this.activities);
         thisClone.selectedActivities = this.selectedActivities == null ? null : this.selectedActivities.clone();
         thisClone.activityFilterToolbarItems = getFilterToolbarClone(this.activityFilterToolbarItems);
         thisClone.showActivityFilter = this.showActivityFilter;
      }
      return thisClone;
   }

   /**
    * @param Items
    * @return
    */
   private static List<SelectItem> getSelectItemClone(List<SelectItem> Items)
   {
      List<SelectItem> clone = new ArrayList<SelectItem>();
      for (SelectItem item : Items)
      {
         clone.add(new SelectItem(item.getValue(), item.getLabel()));
      }
      return clone;
   }

   /**
    * @param processFilterToolbarItems
    * @return
    */
   private static List<FilterToolbarItem> getFilterToolbarClone(List<FilterToolbarItem> processFilterToolbarItems)
   {
      List<FilterToolbarItem> clone = new ArrayList<FilterToolbarItem>();
      for (FilterToolbarItem filterToolbarItem : processFilterToolbarItems)
      {
         FilterToolbarItem filterItem = new FilterToolbarItem(filterToolbarItem.getId(), filterToolbarItem.getName(),
               filterToolbarItem.getMsgKeyActive(), filterToolbarItem.getMsgKeyInactive(), filterToolbarItem.getImage());
         filterItem.setActive(filterToolbarItem.isActive());
         clone.add(filterItem);
      }
      return clone;
   }

   private void initFilters()
   {
      // initialize process list filter tool-bar

      FilterToolbarItem auxiliaryProcessFilter = new FilterToolbarItem("0", AUXILIARY_PROCESSES,
            "processHistory.processTable.hideAuxiliaryProcess", "processHistory.processTable.showAuxiliaryProcess",
            "process_auxiliary.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
      auxiliaryProcessFilter.setActive(false);

      processFilterToolbarItems = new ArrayList<FilterToolbarItem>();
      processFilterToolbarItems.add(auxiliaryProcessFilter);

      if (showActivityFilter)
      {
         // initialize activity list filter tool-bar
         FilterToolbarItem auxiliaryActivityFilter = new FilterToolbarItem("0", AUXILIARY_ACTIVITIES,
               "processHistory.processTable.hideAuxiliaryActivities",
               "processHistory.processTable.showAuxiliaryActivities", "activity_auxiliary.png",
               Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
         auxiliaryActivityFilter.setActive(false);

         activityFilterToolbarItems = new ArrayList<FilterToolbarItem>();
         activityFilterToolbarItems.add(auxiliaryActivityFilter);
      }
   }

   private void initProcesses()
   {
      processes = new ArrayList<SelectItem>();
      processDefinitions.clear();
      List<ProcessDefinition> allProcessDefinitions = null;
      if (isAuxiliaryProcessesSwitchOn())
      {
         if (null == allAccessibleProcesses)
         {
            allAccessibleProcesses = ProcessDefinitionUtils.getAllAccessibleProcessDefinitions();
         }
         allProcessDefinitions = allAccessibleProcesses;
      }
      else
      {
         if (null == allBusinessRelevantProcesses)
         {
            allBusinessRelevantProcesses = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();
         }
         allProcessDefinitions = allBusinessRelevantProcesses;
      }

      // sort process in ascending order
      Collections.sort(allProcessDefinitions, ProcessDefinitionUtils.PROCESS_ORDER);

      if (!allProcessDefinitions.isEmpty())
      {
         processes.add(new SelectItem(ALL, MessagesViewsCommonBean.getInstance().get("messages.common.allProcesses")));
      }

      for (ProcessDefinition procDefn : allProcessDefinitions)
      {
         processDefinitions.put(procDefn.getQualifiedId(), procDefn);
         processes.add(new SelectItem(procDefn.getQualifiedId(), I18nUtils.getProcessName(procDefn)));
      }

      resetSelectedProcesses();
   }

   private void resetSelectedProcesses()
   {
      selectedProcesses = new String[1];
      selectedProcesses[0] = ALL;
   }

   /**
    * @return selected process definitions
    */
   private List<ProcessDefinition> getSelectedProcessDefs()
   {
      List<ProcessDefinition> selectedProcessDefs = new ArrayList<ProcessDefinition>();

      if (null != selectedProcesses)
      {
         for (int i = 0; i < selectedProcesses.length; i++)
         {
            if (ALL.equals(selectedProcesses[i])) // all processes selected
            {
               selectedProcessDefs = new ArrayList<ProcessDefinition>(processDefinitions.values());
               break;
            }
            selectedProcessDefs.add(processDefinitions.get(selectedProcesses[i]));
         }
      }
      return selectedProcessDefs;
   }

   /**
    * @return selected process Qids
    */
   public List<String> getSelectedProcessQIds()
   {
      List<String> selectedProcessQIds = new ArrayList<String>();
      List<ProcessDefinition> selectedProcessDefs = getSelectedProcessDefs();
      if (CollectionUtils.isNotEmpty(selectedProcessDefs))
      {
         for (ProcessDefinition processdef : selectedProcessDefs)
         {
            selectedProcessQIds.add(processdef.getQualifiedId());
         }
      }
      else
      {
         selectedProcessQIds.add(ALL);
      }
      return selectedProcessQIds;
   }

   private void resetSelectedActivities()
   {
      selectedActivities = new String[1];
      selectedActivities[0] = ALL;
   }

   /**
    * sets (filtered) activity list on search criteria based on applied filters
    * 
    * @param selectedProcessDefs
    */
   private void initActivities(List<ProcessDefinition> selectedProcessDefs)
   {
      if (showActivityFilter)
      {
         activities = new ArrayList<SelectItem>();
         activityDefinitions.clear();
         List<Activity> processesActivities = new ArrayList<Activity>();
         List<Activity> allActivities;
         for (ProcessDefinition processDefinition : selectedProcessDefs)
         {
            if (!allAccessibleActivities.containsKey(processDefinition.getQualifiedId()))
            {
               allAccessibleActivities.put(
                     processDefinition.getQualifiedId(),
                     ActivityUtils.getAllActivities(ServiceFactoryUtils.getWorkflowService(),
                           processDefinition.getQualifiedId(), true));
            }

            allActivities = allAccessibleActivities.get(processDefinition.getQualifiedId());

            if (!isAuxiliaryActivitiesSwitchOn())
            {
               if (!allBusinessRelevantActivities.containsKey(processDefinition.getQualifiedId()))
               {
                  allBusinessRelevantActivities.put(processDefinition.getQualifiedId(),
                        ActivityUtils.filterAuxiliaryActivities(allActivities));
               }

               allActivities = allBusinessRelevantActivities.get(processDefinition.getQualifiedId());
            }
            processesActivities.addAll(allActivities);
         }

         // sort activities in ascending order
         Collections.sort(processesActivities, ActivityUtils.ACTIVITY_ORDER);

         activities
               .add(new SelectItem(ALL, MessagesViewsCommonBean.getInstance().get("messages.common.allActivities")));

         if (CollectionUtils.isNotEmpty(processesActivities))
         {
            for (Activity activity : processesActivities)
            {
               String uniqueKey = ActivityUtils.getActivityKey(activity);
               activityDefinitions.put(uniqueKey, activity);
               activities.add(new SelectItem(uniqueKey, I18nUtils.getActivityName(activity)));
            }
         }
         resetSelectedActivities();
      }
   }

   private boolean isAuxiliaryProcessesSwitchOn()
   {
      return isSwitchOn(processFilterToolbarItems, AUXILIARY_PROCESSES);
   }

   private boolean isAuxiliaryActivitiesSwitchOn()
   {
      return isSwitchOn(activityFilterToolbarItems, AUXILIARY_ACTIVITIES);
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
}
