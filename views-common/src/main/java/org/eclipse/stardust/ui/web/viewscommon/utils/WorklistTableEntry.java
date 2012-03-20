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
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.QualityAssuranceUtils.QualityAssuranceState;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;



/**
 * @author roland.stamm
 * 
 */
public class WorklistTableEntry extends DefaultRowModel
{
   /**
    * 
    */
   private static final long serialVersionUID = 6456000973289432283L;
   
   private String processName;

   private boolean activatable;

   private String priority;
   private int processPriority;

   private Date startDate;

   private boolean abortActivity;

   private String lastPerformer;

   private String processDefinition;

   private String status;

   private String duration;

   private ActivityInstance activityInstance;

   private Date lastModificationTime;

   private long oid;

   private int notesCount;

   private Map<String, Object> descriptorValues;

   private long processInstanceOid;

   private boolean checkSelection;

   private boolean delegable;

   private CriticalityCategory criticality;
   
   private final boolean defaultCaseActivity;
   
   private boolean renderIcon;
   
   private String iconPath;
   
   private String priorityIcon;
   
   public WorklistTableEntry()
   {
      defaultCaseActivity = false;
   }
   
   /**
    * @param processName
    * @param processDescriptorsList
    * @param activatable
    * @param lastPerformer
    * @param priority
    * @param startDate
    * @param lastModificationTime
    * @param oid
    * @param duration
    * @param notesCount
    * @param descriptors
    * @param descriptorValues
    * @param processInstanceOid
    * @param activityInstance
    */
   public WorklistTableEntry(String processName, List<ProcessDescriptor> processDescriptorsList,
         boolean activatable, String lastPerformer, String priority, int processPriority, Date startDate,
         Date lastModificationTime, long oid, String duration, int notesCount, Map<String, Object> descriptorValues,
         long processInstanceOid, ActivityInstance activityInstance, long currentPerformerOID)
   {
      super();
      defaultCaseActivity= ActivityInstanceUtils.isDefaultCaseActivity(activityInstance);
      
      this.processDescriptorsList = processDescriptorsList;
      this.activatable = activatable;
      this.lastPerformer = lastPerformer;
      this.priority = priority;
      this.priorityIcon = ProcessInstanceUtils.getPriorityIcon(priority.toLowerCase());
      this.processPriority = processPriority;
      this.startDate = startDate;
      this.duration = duration;
      this.lastModificationTime = lastModificationTime;
      this.oid = oid;
      this.notesCount = notesCount;
      this.descriptorValues = descriptorValues;
      this.processInstanceOid = processInstanceOid;
      this.checkSelection = false;
      this.activityInstance = activityInstance;
      ProcessDefinition pd=ProcessDefinitionUtils.getProcessDefinition(activityInstance.getModelOID(), activityInstance.getProcessDefinitionId());
      this.processDefinition = I18nUtils.getProcessName(pd);

      if (!defaultCaseActivity)
      {
         this.abortActivity = ActivityInstanceUtils.isAbortable(activityInstance);
         delegable = ActivityInstanceUtils.isDelegable(activityInstance);
         this.processName = processName;
      }
      else
      {
         this.processName = ActivityInstanceUtils.getCaseName(activityInstance);
      }
      criticality = CriticalityConfigurationHelper.getInstance().getCriticality(getCriticalityValue());
      
      // Set Quality Assurance parameters
      if (QualityAssuranceState.IS_QUALITY_ASSURANCE.equals(activityInstance.getQualityAssuranceState()))
      {
         renderIcon = true;
         iconPath = Constants.WORKLIST_QA_AWAIT_STATE_IMAGE;
         // Disable the Activity Name link for Activities that are in QA when the logged
         // in user completed the prior non-QA Activity.
         long monitoredActivityPerformerOID = activityInstance.getQualityAssuranceInfo().getMonitoredInstance()
               .getPerformedByOID();
         if (monitoredActivityPerformerOID == currentPerformerOID)
         {
            this.activatable = false;
         }
      }
      else if (QualityAssuranceState.IS_REVISED.equals(activityInstance.getQualityAssuranceState()))
      {
         renderIcon = true;
         iconPath = Constants.WORKLIST_QA_FAILED_STATE_IMAGE;
      }
   }

   public String getStatus()
   {
      status = ActivityInstanceUtils.getActivityStateLabel(activityInstance);
      return status;
   }
   
   public String getProcessDefinition()
   {
      return processDefinition;
   }

   public void setProcessDefinition(String processDefinition)
   {
      this.processDefinition = processDefinition;
   }

   public String getLastPerformer()
   {
      return lastPerformer;
   }

   public void setLastPerformer(String lastPerformer)
   {
      this.lastPerformer = lastPerformer;
   }

   public boolean isAbortActivity()
   {

      return abortActivity;
   }

   public void setAbortActivity(boolean abortActivity)
   {
      this.abortActivity = abortActivity;

   }
   
   public ActivityInstance getActivityInstance()
   {
      return activityInstance;
   }

   public void setActivityInstance(ActivityInstance activityInstance)
   {
      this.activityInstance = activityInstance;
   }

   public void setStatus(String status)
   {
      this.status = activityInstance.getState().getName();
   }

   public String getAssignedTo()
   {
      return ActivityInstanceUtils.getAssignedToLabel(activityInstance);
   }

   public String getDuration()
   {
      return duration;
   }

   public void setDuration(String duration)
   {
      this.duration = duration;
   }

   
   public boolean isDelegable()
   {
      return delegable;
   }

   public void setDelegable(boolean delegable)
   {
      this.delegable = delegable;
   }

   public boolean isCheckSelection()
   {
      return checkSelection;
   }

   public void setCheckSelection(boolean checkSelection)
   {
      this.checkSelection = checkSelection;
   }

   private List<ProcessDescriptor> processDescriptorsList;
   
  

   public String getProcessName()
   {
      return processName;
   }

   public void setProcessName(String processName)
   {
      this.processName = processName;
   }

   public String getPriority()
   {
      return priority;
   }

   public void setPriority(String status)
   {
      this.priority = status;
   }

   public Date getStartDate()
   {
      return startDate;
   }

   public void setStartDate(Date startDate)
   {
      this.startDate = startDate;
   }

   public Date getLastModificationTime()
   {
      return lastModificationTime;
   }

   public void setLastModificationTime(Date lastModificationTime)
   {
      this.lastModificationTime = lastModificationTime;
   }

   public long getOid()
   {
      return oid;
   }

   public void setOid(long oid)
   {
      this.oid = oid;
   }

   public int getNotesCount()
   {
      return notesCount;
   }

   public void setNotesCount(int notesCount)
   {
      this.notesCount = notesCount;
   }

   public Map<String, Object> getDescriptorValues()
   {
      return descriptorValues;
   }

   public void setDescriptorValues(Map<String, Object> descriptorValues)
   {
      this.descriptorValues = descriptorValues;
   }

   public void setActivatable(boolean activatable)
   {
      this.activatable = activatable;
   }

   public boolean isActivatable()
   {
      return activatable;
   }

   public long getProcessInstanceOid()
   {
      return processInstanceOid;
   }

   public List<ProcessDescriptor> getProcessDescriptorsList()
   {
      return processDescriptorsList;
   }

   public void setProcessDescriptorsList(List<ProcessDescriptor> processDescriptorsList)
   {
      this.processDescriptorsList = processDescriptorsList;
   }

   public int getProcessPriority()
   {
      return processPriority;
   }
   
   public int getCriticalityValue()
   {
      return CriticalityConfigurationUtil.getPortalCriticality(activityInstance.getCriticality());
   }
   
   public CriticalityCategory getCriticality()
   {
      return criticality;
   }

   public boolean isDefaultCaseActivity()
   {
      return defaultCaseActivity;
   }

   /**
    * @return the renderIcon
    */
   public boolean isRenderIcon()
   {
      return renderIcon;
   }

   /**
    * @return the iconPath
    */
   public String getIconPath()
   {
      return iconPath;
   }

   public String getPriorityIcon()
   {
      return priorityIcon;
   }
   
}
