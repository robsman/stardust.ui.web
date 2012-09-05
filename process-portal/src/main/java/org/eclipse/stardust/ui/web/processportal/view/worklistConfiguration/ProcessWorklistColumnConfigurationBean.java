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
package org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;

/**
 * @author Yogesh.Manware
 * 
 */
public class ProcessWorklistColumnConfigurationBean extends WorklistColumnConfigurationBean
{

   private ProcessWorklistColumnConfigurationBean(String preferenceID)
   {
      super(preferenceID);
   }

   public ProcessWorklistColumnConfigurationBean()
   {
      this(UserPreferencesEntries.V_WORKLIST_PROC_CONF);
      initialize();
   }

   @Override
   public void add()
   {
      SelectProcessPopup dialog = SelectProcessPopup.getInstance();
      dialog.setCallbackHandler(new ParametricCallbackHandler()
      {
         @SuppressWarnings("unchecked")
         public void handleEvent(EventType eventType)
         {
            if (EventType.APPLY == eventType)
            {
               addProcess((List<ProcessDefinition>) getParameters().get(SelectProcessPopup.PROCESSES));
            }
         }
      });
      dialog.initializeBean();
      dialog.openPopup();
   }

   /**
    * @param processes
    */
   private void addProcess(List<ProcessDefinition> processes)
   {
      StringBuffer existingProcess = new StringBuffer();
      for (ProcessDefinition processDefinition : processes)
      {
         WorklistConfigTableEntry entry = new WorklistConfigTableEntry(processDefinition);
         if (existingoIds.contains(entry.getElementOID()))
         {
            existingProcess.append("\n");
            existingProcess.append(entry.getElementName());
         }
         else
         {
            addEntry(entry);
         }
      }

      if (StringUtils.isNotEmpty(existingProcess.toString()))
      {
         MessageDialog.addErrorMessage(getParamMessage("views.worklistPanelConfiguration.error.processExist",
               existingProcess.toString()));
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration.
    * WorklistColumnConfigurationBean#retrieveandSetConfigurationValues()
    */
   protected void retrieveandSetConfigurationValues()
   {
      try
      {
         ProcessWorklistCacheManager.getInstance().reset();
         Set<ProcessDefinition> processDefs = ProcessWorklistCacheManager.getInstance().getProcesses();

         columnConfTableEntries = new ArrayList<WorklistConfigTableEntry>();

         // set default entry
         defaultConf = WorklistConfigurationUtil
               .getStoredValues(WorklistConfigurationUtil.DEFAULT, columnConfiguration);
         WorklistConfigTableEntry defaultEntry = new WorklistConfigTableEntry(WorklistConfigurationUtil.DEFAULT);
         defaultEntry.setConfiguration(defaultConf);
         columnConfTableEntries.add(defaultEntry);

         for (ProcessDefinition processDefinition : processDefs)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(processDefinition);
            if (existingoIds.contains(confTableEntry.getElementOID()))
            {
               continue;
            }
            fetchStoredValues(confTableEntry);
         }
         columnConfigurationTable.setList(columnConfTableEntries);

      }
      catch (Exception e)
      {
         ExceptionHandler.handleException(e);
      }
   }
}
