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
package org.eclipse.stardust.ui.web.viewscommon.helper.activityTable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Ankita.Patel
 * @version $Revision: $
 */
public class ActivityInstanceWithPrioTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1L;
   private ActivityInstanceWithPrio aiWithPrio;
   
   private int oldPriority;
   private boolean prioChanged;

   private String activityI18nName;
   private String processId;
   
   private boolean checkSelection;
   private boolean delegable;
   private boolean activatable;
   private boolean abortActivity;
   private String status;
   private boolean abortProcess;
   private CriticalityCategory criticality;
   
   private List<ProcessDescriptor> processDescriptorsList = new ArrayList<ProcessDescriptor>();
   private Map<String, Object> descriptorValues = new HashMap<String, Object>();
   
   private boolean caseInstance;
   
   /**
    * 
    */
   public ActivityInstanceWithPrioTableEntry()
   {
   }

   /**
    * @param aiWithPrio
    */
   public ActivityInstanceWithPrioTableEntry(ActivityInstanceWithPrio aiWithPrio)
   {
      super();
      this.aiWithPrio = aiWithPrio;
      this.oldPriority = aiWithPrio.getPriority();
      this.checkSelection = false;
      
      if (ActivityInstanceUtils.isDefaultCaseActivity(aiWithPrio.getActivityInstance()))
      {
         activityI18nName = ActivityInstanceUtils.getCaseName(aiWithPrio.getActivityInstance());
      }
      else
      {
         activityI18nName = I18nUtils.getActivityName(aiWithPrio.getActivityInstance().getActivity());
      }      

      processId = I18nUtils.getProcessName(ProcessDefinitionUtils.getProcessDefinition(aiWithPrio.getActivityInstance()
            .getModelOID(), aiWithPrio.getProcessId()));

      delegable = ActivityInstanceUtils.isDelegable(aiWithPrio.getActivityInstance());
      activatable = ActivityInstanceUtils.isActivatable(aiWithPrio.getActivityInstance());
      status = ActivityInstanceUtils.getActivityStateLabel(aiWithPrio.getActivityInstance());  
      abortProcess = ProcessInstanceUtils.isAbortable(aiWithPrio.getActivityInstance().getProcessInstance());
      criticality = CriticalityConfigurationHelper.getInstance().getCriticality(getCriticalityValue());
      caseInstance = aiWithPrio.getActivityInstance().getProcessInstance().isCaseProcessInstance();
      abortActivity =!caseInstance && ActivityInstanceUtils.isAbortable(aiWithPrio.getActivityInstance());
    }

   /**
    * @param aiWithPrio
    * @param getDescriptors
    */
   public ActivityInstanceWithPrioTableEntry(ActivityInstanceWithPrio aiWithPrio, boolean getDescriptors)
   {
      this(aiWithPrio);

      if (getDescriptors)
      {
         ModelCache modelCache = ModelCache.findModelCache();
         Model model = modelCache.getModel(aiWithPrio.getActivityInstance().getModelOID());
         ProcessDefinition processDefinition = model != null ? model.getProcessDefinition(aiWithPrio.getActivityInstance()
               .getProcessDefinitionId()) : null;
         if (processDefinition != null)
         {
            ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) aiWithPrio.getActivityInstance()
                  .getProcessInstance();
            descriptorValues = processInstanceDetails.getDescriptors();
            if (caseInstance)
            {
               processDescriptorsList = CommonDescriptorUtils.createCaseDescriptors(
                     processInstanceDetails.getDescriptorDefinitions(), descriptorValues, processDefinition, true);
            }
            else
            {
               processDescriptorsList = CommonDescriptorUtils.createProcessDescriptors(
                     processInstanceDetails.getDescriptors(), processDefinition, true);
            }
         }
      }
   }

   public String getActivityName()
   {
      return activityI18nName;
   }

   public long getActivityOID()
   {
      return aiWithPrio.getActivityOID();
   }

   public long getProcessOID()
   {
      return aiWithPrio.getProcessOID();
   }

   public String getProcessID()
   {
      return this.processId;
   }

   public int getPriority()
   {
      return aiWithPrio.getPriority();
   }

   public void setPriority(int priority)
   {
      aiWithPrio.setPriority(priority);
   }

   public Date getStartTime()
   {
      return aiWithPrio.getStartTime();
   }

   public String getDuration()
   {
      return aiWithPrio.getDuration();
   }

   public String getParticipantPerformer()
   {
      return aiWithPrio.getDefaultPerformerName();
   }

   public String getCurrentPerformer()
   {
      return aiWithPrio.getCurrentPerformerName();
   }

   public String getPerformedBy()
   {
      return aiWithPrio.getPerformedByName();
   }

   public boolean isPriorityChanged()
   {
      if (this.oldPriority != this.getPriority())
      {
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean isCheckSelection()
   {
      return checkSelection;
   }

   public boolean isResubmissionActivity()
   {
      return aiWithPrio.isResubmissionActivity();
   }

   public String getStateName()
   {
      return aiWithPrio.getStateName();
   }

   @Override
   public String toString()
   {
      return "ActivityInstance: " + aiWithPrio.getActivityName();
   }

   public void setCheckSelection(boolean checkSelection)
   {
      this.checkSelection = checkSelection;
   }

   public boolean isActivatable()
   {
      return activatable;
   }

   public String getProcessInstanceName()
   {
      return aiWithPrio.getActivityInstance().getProcessInstance().getProcessName();
   }

   public List<ProcessDescriptor> getProcessDescriptorsList()
   {
      return processDescriptorsList;
   }

   public boolean isDelegable()
   {
      return delegable;
   }

   public ActivityInstance getActivityInstance()
   {
      return aiWithPrio.getActivityInstance();
   }

   public String getStatus()
   {
      return status;
   }

   public boolean isAbortActivity()
   {
      return abortActivity;
   }

   public void setAbortActivity(boolean abortActivity)
   {
      this.abortActivity = abortActivity;
   }

   public boolean isAbortProcess()
   {
      return abortProcess;
   }

   public void setAbortProcess(boolean abortProcess)
   {
      this.abortProcess = abortProcess;
   }

   public boolean isPrioChanged()
   {
      return prioChanged;
   }

   public void setPrioChanged(boolean prioChanged)
   {
      this.prioChanged = prioChanged;
   }

   public int getNotesCount()
   {
      return aiWithPrio.getNoteCount();
   }

   public Date getLastModified()
   {
      return aiWithPrio.getActivityInstance().getLastModificationTime();
   }

   public int getOldPriority()
   {
      return oldPriority;
   }

   public Map<String, Object> getDescriptorValues()
   {
      return descriptorValues;
   }

   public void setDescriptorValues(Map<String, Object> descriptorValues)
   {
      this.descriptorValues = descriptorValues;
   }
   
   public boolean isModifyProcessInstance()
   {
      return aiWithPrio.isModifyProcessInstance();
   }
   
   public int getCriticalityValue()
   {
      return CriticalityConfigurationUtil.getPortalCriticality(aiWithPrio.getActivityInstance().getCriticality());
   }
   
   public CriticalityCategory getCriticality()
   {
      return criticality;
   }

   public boolean isCaseInstance()
   {
      return caseInstance;
   }

}
