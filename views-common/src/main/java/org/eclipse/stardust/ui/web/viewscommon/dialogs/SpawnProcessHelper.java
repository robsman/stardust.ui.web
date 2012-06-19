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
package org.eclipse.stardust.ui.web.viewscommon.dialogs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.SubprocessSpawnInfo;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.table.DataTable;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



public class SpawnProcessHelper
{
   private DataTable<SpawnProcessTableEntry> spawnedProcessTable;
   private MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
   private List<ProcessInstance> subprocessInstances;
   private ProcessInstance rootProcessInstance;
   

   /**
    * 
    */
   public void reset()
   {
      spawnedProcessTable = null;
      subprocessInstances = null;
   }

   /**
    * 
    */
   public void update()
   {
      spawnedProcessTable.setList(getSpawnProcesses());
      spawnedProcessTable.initialize();

   }

   /**
    * 
    * @param rootProcessInstanceOid
    * @param subprocessSpawnInfo
    * @return
    */
   public List<ProcessInstance> spawnSubprocessInstances(long rootProcessInstanceOid,
         List<SubprocessSpawnInfo> subprocessSpawnInfo)
   {
      subprocessInstances = ServiceFactoryUtils.getWorkflowService().spawnSubprocessInstances(rootProcessInstanceOid,
            subprocessSpawnInfo);
      return subprocessInstances;
   }

   /**
    * 
    * @return
    */
   public List<ProcessInstance> getSubprocessInstances()
   {
      return subprocessInstances;
   }

   public void setSubprocessInstances(List<ProcessInstance> subprocessInstances)
   {
      this.subprocessInstances = subprocessInstances;
   }

   /**
    * method create Spawn Processes Table
    */
   private void createSpawnProcessesTable()
   {
      ColumnPreference processNameCol = new ColumnPreference("PROCESS_NAME", "processName", ColumnDataType.STRING,
            COMMON_MESSAGE_BEAN.getString("views.spawnProcessDialog.spawnedProcess.column.process"), true, false);

      List<ColumnPreference> fixedBeforeCols = new ArrayList<ColumnPreference>();
      fixedBeforeCols.add(processNameCol);

      IColumnModel processColumnModel = new DefaultColumnModel(null, fixedBeforeCols, null,
            UserPreferencesEntries.M_VIEWS_COMMON, "SpawnProcessDialog");
      spawnedProcessTable = new DataTable<SpawnProcessTableEntry>(processColumnModel, null);
      spawnedProcessTable.initialize();
   }

   /**
    * JSF action method to spawn new process for selected process from Spawn process
    * dialog
    * 
    */
   public void spawnProcesses()
   {
      try
      {
         if (CollectionUtils.isNotEmpty(subprocessInstances))
         {
            update();
         }
         else
         {
            MessageDialog.addWarningMessage(COMMON_MESSAGE_BEAN
                  .getString("views.spawnProcessDialog.spawnedProcess.errorMsg.emptyValue"));
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * 
    */

   public void initialize()
   {
      createSpawnProcessesTable();
   }

   /**
    * 
    * @return
    */

   public DataTable<SpawnProcessTableEntry> getSpawnedProcessTable()
   {
      return spawnedProcessTable;
   }

   /**
    * 
    * @param subprocessInstances
    * @return
    */
   public List<SpawnProcessTableEntry> getSpawnProcesses()
   {
      List<SpawnProcessTableEntry> spawnedProcesses = new ArrayList<SpawnProcessTableEntry>();
      for (ProcessInstance processInstance : subprocessInstances)
      {
         spawnedProcesses.add(new SpawnProcessTableEntry(processInstance));
      }
      return spawnedProcesses;
   }

   /**
    * 
    * @author vikas.mishra
    * @version $Revision: $
    */
   public static class SpawnProcessTableEntry extends DefaultRowModel
   {
      private static final long serialVersionUID = 1L;
      private final ProcessInstance processInstance;

      public SpawnProcessTableEntry(ProcessInstance processInstance)
      {
         this.processInstance = processInstance;
      }

      public String getProcessName()
      {
         return ProcessInstanceUtils.getProcessLabel(processInstance);
      }

      public ProcessInstance getProcessInstance()
      {
         return processInstance;
      }

   }

   /**
    * 
    */

   public boolean openActivities(String workListTitle)
   {
      try
      {
         if (CollectionUtils.isNotEmpty(subprocessInstances))
         {

            Map<String, Object> params = CollectionUtils.newTreeMap();
            ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();

            FilterOrTerm orTerm = query.getFilter().addOrTerm();
            for (ProcessInstance pInstance : subprocessInstances)
            {
               orTerm.add(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(pInstance.getOID()));
            }
            params.put(Query.class.getName(), query);
            params.put("name", workListTitle);
            long id = new Date().getTime();//to make view unique
            PortalApplication.getInstance().openViewById("worklistPanel", "id=" + id, params, null, false);
         }
      }
      catch (Exception e)
      {

         ExceptionHandler.handleException(e);
         return false;
      }
      return true;
   }

   public ProcessInstance getRootProcessInstance()
   {
      return rootProcessInstance;
   }

   public void setRootProcessInstance(ProcessInstance rootProcessInstance)
   {
      this.rootProcessInstance = rootProcessInstance;
   }

}
