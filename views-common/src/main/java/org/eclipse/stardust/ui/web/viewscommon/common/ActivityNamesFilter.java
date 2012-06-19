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
package org.eclipse.stardust.ui.web.viewscommon.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.dto.ActivityDetails;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.common.filter.ITableDataFilter;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterCustom;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.IceComponentUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



public class ActivityNamesFilter extends TableDataFilterCustom
{
   private static final long serialVersionUID = 1L;
   private static final String ALL_PROCESSES = "All Processes";
   private static final String ALL_ACTIVITIES = "All Activities";
   private static Map<String, String> activityMap = new HashMap<String, String>();
   private List<SelectItem> activityList = new ArrayList<SelectItem>();
   private List<SelectItem> processDefnList = new ArrayList<SelectItem>();

   private SelectItem processDefnSelectItem = null;

   private String[] selectedActivities;
   private String[] selectedProcess;
   private boolean eventFired = false;

   public ActivityNamesFilter(String contentUrl)
   {
      super(contentUrl);
   }

   public ActivityNamesFilter(String name, String property, String title, boolean visible, String contentUrl)
   {
      super(name, property, title, visible, contentUrl);
      initActivityList();
   }

   public boolean contains(Object compareValue)
   {
      return true;
   }

   public void copyValues(ITableDataFilter dataFilterToCopy)
   {
      ActivityNamesFilter toCopy = (ActivityNamesFilter) dataFilterToCopy;
      this.selectedActivities = toCopy.selectedActivities;
      this.selectedProcess = toCopy.selectedProcess;
   }

   public List<SelectItem> getAllProcessDefinitions()
   {
      List<SelectItem> items = ProcessDefinitionUtils.getAllUniqueProcessDefinitionItems();
      if (!items.isEmpty())
      {
         items.add(0,
               new SelectItem(ALL_PROCESSES, MessagesViewsCommonBean.getInstance().get("messages.common.allProcesses")));
      }
      return items;
   }

   public ITableDataFilter getClone()
   {
      ActivityNamesFilter thisClone = new ActivityNamesFilter(getName(), getProperty(), getTitle(), isVisible(),
            this.contentUrl);
      thisClone.selectedActivities = this.selectedActivities;
      thisClone.selectedProcess = this.selectedProcess;

      return thisClone;
   }

   public String getFilterSummaryTitle()
   {
      if (selectedActivities.length == 1)
      {
         return activityMap.get(selectedActivities[0]);
      }
      else if (selectedActivities.length > 1)
      {
         return activityMap.get(selectedActivities[0]) + "...";
      }
      else
      {
         return "";
      }
   }

   public List<SelectItem> getProcessDefnList()
   {
      if (processDefnList.isEmpty())
      {
         processDefnList = getAllProcessDefinitions();
      }

      return processDefnList;
   }

   public SelectItem getProcessDefnSelectItem()
   {
      return processDefnSelectItem;
   }

   public final String[] getSelectedActivities()
   {
      if ((selectedActivities != null) && containsAllActivitiesSelected(selectedActivities))
      {
         List<String> list = new ArrayList<String>();
         List<ActivityDetails> acList = getAllActivities();

         for (ActivityDetails activity : acList)
         {
            list.add(activity.getQualifiedId());
         }

         return list.toArray(new String[0]);
      }

      return selectedActivities;
   }

   public void initActivityList()
   {

      activityList.clear();
      
      if (selectedProcess != null)
      {
         List<ActivityDetails> activities = getAllActivities();
         
         for (ActivityDetails activity : activities)
         {
            activityList.add(new SelectItem(activity.getId(), I18nUtils.getActivityName(activity)));
            activityMap.put(activity.getId(), I18nUtils.getActivityName(activity));
         }
         // sort process in ascending order
         Collections.sort(activityList, IceComponentUtil.SELECT_ITEM_ORDER);
      }  
      if (!getProcessDefnList().isEmpty())
      {
         activityList.add(0,
               new SelectItem(ALL_ACTIVITIES, MessagesViewsCommonBean.getInstance()
                     .get("messages.common.allActivities")));
         activityMap.put(ALL_ACTIVITIES, MessagesViewsCommonBean.getInstance().get("messages.common.allActivities"));
      }

   }

   public boolean isFilterSet()
   {
      return ((selectedActivities != null) && (selectedActivities.length != 0));
   }

   public void processDefnChanged(ValueChangeEvent vce)
   {
      eventFired = true;
      activityList.clear();

      if (vce.getNewValue() == null)
      {
         return;
      }

      selectedProcess = (String[]) vce.getNewValue(); 

      initActivityList();
   }

   public void resetFilter()
   {
      this.selectedActivities = null;
      this.selectedProcess = null;
      activityMap.clear();
      activityList.clear();
      eventFired = false;
   }

   public void setProcessDefnList(List<SelectItem> processDefnList)
   {
      this.processDefnList = processDefnList;
   }

   public void setProcessDefnSelectItem(SelectItem processDefnSelectItem)
   {
      this.processDefnSelectItem = processDefnSelectItem;
   }

   public final void setSelectedActivities(String[] selectedActivities)
   {
      this.selectedActivities = selectedActivities;
   }

   public List<SelectItem> getActivityList()
   {
      if (!eventFired)
      {
         initActivityList();
      }

      return activityList;
   }

   public final String[] getSelectedProcess()
   {
      return selectedProcess;
   }

   public final void setSelectedProcess(String[] selectedProcess)
   {
      this.selectedProcess = selectedProcess;
   }

   /**
    * check that "All Activities" is selected or not.
    * 
    * @param selectedActivities
    * @return
    */

   private boolean containsAllActivitiesSelected(String[] selectedActivities)
   {
      for (String activityName : selectedActivities)
      {
         if (ALL_ACTIVITIES.equals(activityName))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * check that "All Processes" is selected or not.
    * 
    * @param selectedProcesses
    * @return
    */

   private boolean containsAllProcessesSelected(String[] selectedProcesses)
   {
      for (String processName : selectedProcesses)
      {
         if (ALL_PROCESSES.equals(processName))
         {
            return true;
         }
      }

      return false;
   }

   

   private List<ActivityDetails> getAllActivities()
   {
      List<ActivityDetails> allActivities = new ArrayList<ActivityDetails>();

      if (containsAllProcessesSelected(selectedProcess))
      {
         List<ProcessDefinition> processDefinitions = ProcessDefinitionUtils.getAllAccessibleProcessDefinitions();

         for (ProcessDefinition def : processDefinitions)
         {
            allActivities.addAll(def.getAllActivities());
         }
      }
      else
      {
         for (String processName : selectedProcess)
         {
            ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(processName);
            @SuppressWarnings("unchecked")
            List<ActivityDetails> activities = (processDefinition != null)
                  ? processDefinition.getAllActivities()
                  : Collections.EMPTY_LIST;

            allActivities.addAll(activities);
         }
      }

      return allActivities;
   }
}
