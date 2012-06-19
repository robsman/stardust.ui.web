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
package org.eclipse.stardust.ui.web.viewscommon.process.history;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDescriptor;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;



/**
 * 
 * @author Vikas.Mishra
 * 
 */
public class ProcessInstanceHistoryItem extends AbstractProcessHistoryTableEntry
{
   private static final String STATUS_PREFIX = "views.processTable.statusFilter.";
   private String processType = "ProcessInstance";
   private Date endTime;
   private Date lastModificationTime;
   private Date startTime;
   private List<ProcessDescriptor> processDescriptorsList;
   private Map<String, Object> descriptorValues;
   private String duration;
   private String name;
   private String performer;
   private String startingUser;
   private String state;
   private int oldPriority;
   private int priority;
   private long processInstanceRootOID;
   private int notesCount;
   private boolean enableTerminate;
   private boolean enableRecover;
   private boolean selected;
   private boolean caseInstance;
   private boolean enableDetach;
   private final ProcessInstance rootProcessInstance;

   /**
    * @param processInstance
    * @param children
    */
   public ProcessInstanceHistoryItem(ProcessInstance processInstance,ProcessInstance rootProcessInstance, List<IProcessHistoryTableEntry> children)
   {
      super(processInstance, children); 
      this.rootProcessInstance=rootProcessInstance;
      init(processInstance);
      
   }

   /**
    * @param processInstance
    */
   private void init(ProcessInstance processInstance)
   {
      if (processInstance != null)
      {
         
         priority = processInstance.getPriority();
         oldPriority = processInstance.getPriority();
         startingUser = UserUtils.getUserDisplayLabel(processInstance.getStartingUser());
         startTime = processInstance.getStartTime();
         endTime = processInstance.getTerminationTime();
         if (startTime != null)
         {
            duration=ProcessInstanceUtils.getDuration(processInstance);
         }

         processInstanceRootOID = processInstance.getRootProcessInstanceOID();
         notesCount = ProcessInstanceUtils.getNotes(processInstance).size();
         this.enableTerminate = ProcessInstanceUtils.isAbortable(processInstance);

         this.enableRecover = true;
         Model model = ModelCache.findModelCache().getModel(processInstance.getModelOID());

         if (model != null)
         {
            this.caseInstance = processInstance.isCaseProcessInstance();
            MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
            ProcessDefinition processDefinition = model.getProcessDefinition(processInstance.getProcessID());
            name = I18nUtils.getProcessName(processDefinition);
            lastModificationTime = processInstance.getTerminationTime();
            performer = UserUtils.getUserDisplayLabel(processInstance.getStartingUser());
            //state = processInstance.getState().toString();
            state = propsBean.getString(STATUS_PREFIX +  processInstance.getState().getName().toLowerCase()); 
            ProcessInstanceDetails processInstanceDetails = (ProcessInstanceDetails) processInstance;
            descriptorValues = processInstanceDetails.getDescriptors();

            if (null == descriptorValues)
            {
               descriptorValues = new HashMap<String, Object>();
               processDescriptorsList = new ArrayList<ProcessDescriptor>();
            }
            else
            {
               if (this.caseInstance)
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
            if (ProcessDefinitionUtils.isAuxiliaryProcess(processDefinition))
            {
               processType = "AuxiliaryProcess";
            }
            else if (this.caseInstance)
            {
               processType = "CaseInstance";
            }
            else if (processInstanceRootOID != processInstance.getOID())
            {
               processType = "SubProcess";
            }
            //check to enable detach
            if (!caseInstance && processInstanceRootOID != processInstance.getOID())
            {              
               if (null != rootProcessInstance)
               {
                  boolean isRootCase = rootProcessInstance.isCaseProcessInstance();
                  boolean canManageCase= AuthorizationUtils.hasManageCasePermission(rootProcessInstance);
                  enableDetach = isRootCase && canManageCase;
               }               
            }
            
         }
      }
   }

   public Map<String, Object> getDescriptorValues()
   {
      return descriptorValues;
   }

   public String getDuration()
   {
      return duration;
   }

   public Date getEndTime()
   {
      return endTime;
   }

   public Date getLastModificationTime()
   {
      return lastModificationTime;
   }

   public String getName()
   {
      return name;
   }

   public int getOldPriority()
   {
      return oldPriority;
   }

   public String getPerformer()
   {
      return performer;
   }

   public int getPriority()
   {
      return priority;
   }

   public List<ProcessDescriptor> getProcessDescriptorsList()
   {
      return processDescriptorsList;
   }

   public long getProcessInstanceRootOID()
   {
      return processInstanceRootOID;
   }

   public String getRuntimeObjectType()
   {
      return processType;
   }

   public Date getStartTime()
   {
      return startTime;
   }

   public String getStartingUser()
   {
      return startingUser;
   }

   public String getState()
   {
      return state;
   }

   public void setDuration(String duration)
   {
      this.duration = duration;
   }

   public void setEndTime(Date endTime)
   {
      this.endTime = endTime;
   }

   public void setOldPriority(int oldPriority)
   {
      this.oldPriority = oldPriority;
   }

   public void setPriority(int priority)
   {
      this.priority = priority;
   }

   public void setProcessInstanceRootOID(long processInstanceRootOID)
   {
      this.processInstanceRootOID = processInstanceRootOID;
   }

   public void setStartingUser(String startingUser)
   {
      this.startingUser = startingUser;
   }

   protected void runtimeObjectChanged()
   {
      init((ProcessInstance) getRuntimeObject());
   }

   public int getNotesCount()
   {
      return notesCount;
   }

   public void setNotesCount(int notesCount)
   {
      this.notesCount = notesCount;
   }

   public boolean isEnableTerminate()
   {
      return enableTerminate;
   }

   public void setEnableTerminate(boolean enableTerminate)
   {
      this.enableTerminate = enableTerminate;
   }

   public boolean isEnableRecover()
   {
      return enableRecover;
   }

   public void setEnableRecover(boolean enableRecover)
   {
      this.enableRecover = enableRecover;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   public boolean isCaseInstance()
   {
      return caseInstance;
   }

   public void setCaseInstance(boolean caseInstance)
   {
      this.caseInstance = caseInstance;
   }

   public boolean isEnableDetach()
   {
      return enableDetach;
   }

   public ProcessInstance getRootProcessInstance()
   {
      return rootProcessInstance;
   }
   
   
}