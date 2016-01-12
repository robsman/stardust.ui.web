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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessDefinitions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.SubprocessSpawnInfo;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.dialogs.ConfirmationDialogHandler;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;



/**
 * 
 * @author vikas.mishra
 * @since 7.0
 * 
 * bean for spawn process dialog
 * 
 */
public class SpawnProcessDialogBean extends PopupUIComponentBean implements ConfirmationDialogHandler
{

   private static final long serialVersionUID = 1L;
   private static final String BEAN_NAME = "spawnProcessDialogBean";
   private List<SelectItem> spawnableProcessItems;
   private List<String> selectedProcess = new ArrayList<String>();
   private boolean showStartProcessView = true;
   private MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();

   private SpawnProcessHelper spawnProcessHelper;
   private ProcessInstance sourceProcessInstance;
   private Boolean hasSpawnProcessPermission;
   private boolean showSpawnedWorkItems;
   private boolean activateSpawnedWorkItems;

   @Override
   public void initialize()
   {

      spawnableProcessItems = new ArrayList<SelectItem>();
      selectedProcess.clear();

      ProcessDefinitions pds = ServiceFactoryUtils.getQueryService().getProcessDefinitions(ProcessDefinitionQuery.findStartable());
      for (ProcessDefinition pd : pds)
      {
         spawnableProcessItems.add(new SelectItem(pd.getQualifiedId(), I18nUtils.getProcessName(pd)));
      }
      Collections.sort(spawnableProcessItems, new SelectItemComparator());

      setShowSpawnedWorkItems(true);
      setActivateSpawnedWorkItems(true);

      UserPreferencesHelper userPrefHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_VIEWS_COMMON,
            PreferenceScope.USER);

      showSpawnedWorkItems = userPrefHelper.getBoolean(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_SHOW_WORK_ITEMS, true);
      activateSpawnedWorkItems = userPrefHelper.getBoolean(UserPreferencesEntries.V_WORKFLOW_EXEC_CONFIG_PANEL,
            UserPreferencesEntries.F_ACTIVATE_WORK_ITEMS, false);
   }

   /**
    * 
    * @return
    */

   public SpawnProcessHelper getSpawnProcessHelper()
   {
      return spawnProcessHelper;
   }

   /**
    * JSF action method open Spawn Process Dialog method get "processInstanceOID" from
    * request parameter to open dialog
    */
   @Override
   public void openPopup()
   {
      try
      {
         showStartProcessView = true;
         spawnProcessHelper = new SpawnProcessHelper();
         ProcessInstance rootProcessInstance = getProcessInstance();
         if (rootProcessInstance != null)
         {
            spawnProcessHelper.setRootProcessInstance(rootProcessInstance);
            spawnProcessHelper.initialize();
            initialize();

            super.openPopup();
         }
      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * 
    * @return
    */
   private ProcessInstance getProcessInstance()
   {
      if (null != sourceProcessInstance)
      {
         return sourceProcessInstance;
      }
      else
      {

         ProcessInstance rootProcessInstance = null;
         String processInstanceOID = FacesUtils.getRequestParameter("processInstanceOID");
         if (processInstanceOID != null)
         {
            rootProcessInstance = ProcessInstanceUtils.getProcessInstance(Long.valueOf(processInstanceOID));
         }
         return rootProcessInstance;
      }

   }

   /**
    * method takes process instances id as input parameter to open dialog
    * 
    * @param processInstanceIds
    */
   public void openPopup(List<Long> processInstanceIds)
   {
      try
      {
         showStartProcessView = true;
         if (CollectionUtils.isNotEmpty(processInstanceIds))
         {
            ProcessInstance rootProcessInstance = ProcessInstanceUtils.getProcessInstance(processInstanceIds.get(0));
            initialize();
            spawnProcessHelper.setRootProcessInstance(rootProcessInstance);
            spawnProcessHelper.initialize();

            super.openPopup();
         }

      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }

   }

   /**
    * JSF action method to spawn new process for selected process from Spawn process
    * dialog
    * 
    */
   public void startSpawnProcesses()
   {
      try
      {
         if (CollectionUtils.isNotEmpty(selectedProcess))
         {
            List<SubprocessSpawnInfo> infoList = CollectionUtils.newArrayList();
            for (String process : selectedProcess)
            {
               SubprocessSpawnInfo info = new SubprocessSpawnInfo(process, true, null);
               infoList.add(info);
            }
            showStartProcessView = false;
            spawnProcessHelper.spawnSubprocessInstances(spawnProcessHelper.getRootProcessInstance().getOID(), infoList);
            spawnProcessHelper.update();
           
            closePopup();
            
            if(showSpawnedWorkItems){
               openActivities();
            }

            if (activateSpawnedWorkItems)
            {
               spawnProcessHelper.activateSpawnedWorkItems();
            }

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
   public boolean accept()
   {
      return true;
   }

   /**
    * 
    */
   public boolean cancel()
   {
      return true;
   }
   /**
    * 
    * @return
    */
   public boolean isEnableSpawnProcess()
   {
      return CollectionUtils.isNotEmpty(selectedProcess);
   }

   /**
    * 
    */

   public void openActivities()
   {
      spawnProcessHelper.openActivities(COMMON_MESSAGE_BEAN.getString("views.spawnProcessDialog.worklist.title"));    
   }

   /**
    * 
    * @return
    */
   public static SpawnProcessDialogBean getInstance()
   {
      return (SpawnProcessDialogBean) FacesUtils.getBeanFromContext(BEAN_NAME);
   }
  
   public List<String> getSelectedProcess()
   {
      return selectedProcess;
   }   

   public void setSelectedProcess(List<String> selectedProcess)
   {
      this.selectedProcess = selectedProcess;
   }

   public List<SelectItem> getSpawnableProcessItems()
   {
      return spawnableProcessItems;
   }

   public void setSpawnableProcessItems(List<SelectItem> spawnableProcessItems)
   {
      this.spawnableProcessItems = spawnableProcessItems;
   }

   public boolean isShowStartProcessView()
   {
      return showStartProcessView;
   }

   public void setShowStartProcessView(boolean showStartProcessView)
   {
      this.showStartProcessView = showStartProcessView;
   }
   
   /**
    * 
    * @author Sidharth.Singh
    * 
    */
   public class SelectItemComparator implements Comparator<SelectItem>
   {
      public int compare(SelectItem s1, SelectItem s2)
      {
         return s1.getLabel().compareTo(s2.getLabel());
      }
   }

   public ProcessInstance getSourceProcessInstance()
   {
      return sourceProcessInstance;
   }

   public void setSourceProcessInstance(ProcessInstance sourceProcessInstance)
   {
      this.sourceProcessInstance = sourceProcessInstance;
   }


   public boolean isHasSpawnProcessPermission()
   {
      if (null == hasSpawnProcessPermission)
      {
         hasSpawnProcessPermission = AuthorizationUtils.hasSpawnProcessPermission();
      }
      return hasSpawnProcessPermission;
   }

   public boolean isShowSpawnedWorkItems()
   {
      return showSpawnedWorkItems;
   }

   public void setShowSpawnedWorkItems(boolean showSpawnedWorkItems)
   {
      this.showSpawnedWorkItems = showSpawnedWorkItems;
   }

   public boolean isActivateSpawnedWorkItems()
   {
      return activateSpawnedWorkItems;
   }

   public void setActivateSpawnedWorkItems(boolean activateSpawnedWorkItems)
   {
      this.activateSpawnedWorkItems = activateSpawnedWorkItems;
   }

}
