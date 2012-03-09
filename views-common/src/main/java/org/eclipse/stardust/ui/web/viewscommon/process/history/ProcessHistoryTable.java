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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.UIViewComponentBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



public class ProcessHistoryTable extends UIViewComponentBean
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(ProcessHistoryTable.class);
   private static final String BEAN_NAME = "processHistoryTable";
   public static final String MSG_PREFIX = "processHistory.";
   
   private final ActivityTreeTable activityTreeTable;
   private final ProcessTreeTable processTreeTable;
   private IProcessHistoryDataModel processHistoryDataModel;
   private ProcessTableEntryUserObject selectedRow;
   private boolean enableCase=false;
   
   private boolean hasSpawnProcessPermission;
   private boolean hasSwitchProcessPermission;
   private boolean hasJoinProcessPermission;

   /**
    * Construction the default tree structure by combining tree nodes.
    */
   public ProcessHistoryTable()
   {
      processTreeTable = new ProcessTreeTable();
      activityTreeTable = new ActivityTreeTable();    
   }

   private void initializePermissions()
   {
      hasSpawnProcessPermission = AuthorizationUtils.hasSpawnProcessPermission();
      hasSwitchProcessPermission = AuthorizationUtils.hasAbortAndStartProcessInstancePermission();
      hasJoinProcessPermission = AuthorizationUtils.hasAbortAndJoinProcessInstancePermission();
   }
   /**
    * This needs to be called by Caller to Initialize the Component
    */
   public void initialize()
   {
      initializePermissions();
      trace.info("-----------> Process History Table Initialize");
      List<ProcessInstance> processInstances;
       if (getCurrentProcessInstance() != null)
      {
         processInstances = processHistoryDataModel.getAllProcesses(getCurrentProcessInstance(), true);
      }
      else
      {
         ProcessInstance processInstance = processHistoryDataModel
               .getCurrentProcessByActivityInstance(getCurrentActivityInstance());
         processInstances = processHistoryDataModel.getAllProcesses(processInstance, true);
      }

      // initialize process history tree table
      processTreeTable.setProcessInstances(processInstances);
      processTreeTable.setProcessHistoryDataModel(processHistoryDataModel);
      processTreeTable.setCurrentActivityInstance(getCurrentActivityInstance());
      processTreeTable.setCurrentProcessInstance(getCurrentProcessInstance());
      processTreeTable.initialize();

      // initialize Activity tree table
      activityTreeTable.setProcessInstances(processInstances);
      activityTreeTable.setProcessHistoryDataModel(processHistoryDataModel);
      activityTreeTable.setCurrentProcessInstance(processTreeTable.getSelectedRow().getProcessInstance());
      // Set to get caseActivitiesRoot for retrieving all participant for all PI's of Case
      // Process
      if (enableCase)
      {
         activityTreeTable.setCaseProcess(true);
      }
      activityTreeTable.initialize();
      
      selectedRow = processTreeTable.getSelectedRow();

      trace.info("<----------- Process History Table Initialize");
   }

   /**
    * 
    * @param event
    */
   public void applyChanges(ActionEvent event)
   {
      ProcessInstanceHistoryItem root = (ProcessInstanceHistoryItem) processTreeTable.getProcessHistoryTableRoot();
      Map<Object, Integer> changedProcesses = new HashMap<Object, Integer>();
      if (root.getOldPriority() != root.getPriority())
      {
         changedProcesses.put((ProcessInstance) root.getRuntimeObject(), root.getPriority());
      }

      List<IProcessHistoryTableEntry> childs = root.getChildren();

      for (IProcessHistoryTableEntry entry : childs)
      {
         if (entry instanceof ProcessInstanceHistoryItem)
         {
            ProcessInstanceHistoryItem item = (ProcessInstanceHistoryItem) entry;

            if (item.getOldPriority() != item.getPriority())
            {
               changedProcesses.put((ProcessInstance) item.getRuntimeObject(), item.getPriority());
            }
         }
      }
      ProcessInstanceUtils.updatePriorities(changedProcesses);
      initialize();
   }
   
   

   /**
    * action listener to open Switch process dialog
    */
   public void openSwitchProcess(ActionEvent event)
   {
      ProcessInstance processInstance = (ProcessInstance) event.getComponent().getAttributes().get("processInstance");     
      ProcessInstanceUtils.openSwitchProcessDialog(processInstance);
   }
   
   /**
    * action listener to open Spawn process dialog
    */
   public void openSpawnProcess(ActionEvent event)
   {
      ProcessInstance processInstance = (ProcessInstance) event.getComponent().getAttributes().get("processInstance");
      ProcessInstanceUtils.openSpawnProcessDialog(processInstance);
   }
   
   /**
    * action listener to open Join process
    */
   public void openJoinProcess(ActionEvent event)
   {
      try
      {
         ProcessInstance processInstance = (ProcessInstance) event.getComponent().getAttributes()
               .get("processInstance");

         if (null != processInstance)
         {
            ProcessInstanceUtils.openJoinProcessDialog(processInstance);
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }

   /**
    * @param event
    */
   public void selectRow(ActionEvent event)
   {
      Object pi = event.getComponent().getAttributes().get("selectedRow");
      if (selectedRow != null)
      {
         if (selectedRow != pi)
         {
            selectedRow.setSelected(false);
         }
         else
         {
            return;
         }
      }
      if ((pi != null) && pi instanceof ProcessTableEntryUserObject)
      {
         selectedRow = (ProcessTableEntryUserObject) pi;
         selectedRow.setSelected(true);
         processTreeTable.setSelectedRow(selectedRow);
         activityTreeTable.setCurrentProcessInstance(selectedRow.getProcessInstance());
         activityTreeTable.renderTree();
      }
   }

   /**
    * @param processHistoryDataModel
    */
   public void setProcessHistoryDataModel(IProcessHistoryDataModel processHistoryDataModel)
   {
      this.processHistoryDataModel = processHistoryDataModel;
   }
   
   /**
    * @return
    */
   public static ProcessHistoryTable getCurrent()
   {
      return (ProcessHistoryTable) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public ActivityTreeTable getActivityTreeTable()
   {
      return activityTreeTable;
   }

   /**
    * @return
    */
   public IProcessHistoryDataModel getProcessHistoryDataModel()
   {
      return processHistoryDataModel;
   }

   public ProcessTreeTable getProcessTreeTable()
   {
      return processTreeTable;
   }

   public ProcessTableEntryUserObject getSelectedRow()
   {
      return selectedRow;
   }

   public boolean isEnableCase()
   {
      return enableCase;
   }

   public void setEnableCase(boolean enableCase)
   {
      this.enableCase = enableCase;
   }
   
   public boolean isEnableSpawnProcess()
   {
      return hasSpawnProcessPermission;
   }
   public boolean isEnableSwitchProcess()
   {
      return hasSwitchProcessPermission;
   }
   public boolean isEnableJoinProcess()
   {
      return hasJoinProcessPermission;
   } 

}