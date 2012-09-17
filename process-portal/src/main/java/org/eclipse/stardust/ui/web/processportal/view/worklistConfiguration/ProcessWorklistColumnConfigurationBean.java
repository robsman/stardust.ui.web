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

import java.util.List;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.processportal.common.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.processportal.dialogs.SelectProcessPopup;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class ProcessWorklistColumnConfigurationBean extends WorklistColumnConfigurationBean
{
   private static final String PROCESS_KEY = "process.";
   private static final String PROCESS_EXIST_KEY = PROCESS_KEY + "exist";
   private static final String NEW_LINE_CHAR = "\n";

   /**
    * default constructor
    */
   public ProcessWorklistColumnConfigurationBean()
   {
      this(UserPreferencesEntries.P_WORKLIST_PROC_CONF);
      initialize();
   }

   /**
    * @param preferenceID
    */
   private ProcessWorklistColumnConfigurationBean(String preferenceID)
   {
      super(preferenceID);
   }

   @Override
   public void add()
   {
      SelectProcessPopup selectProcessPopup = SelectProcessPopup.getInstance();
      selectProcessPopup.setCallbackHandler(new ParametricCallbackHandler()
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
      selectProcessPopup.initializeBean();
      selectProcessPopup.openPopup();
   }

   /**
    * @param processes
    */
   private void addProcess(List<ProcessDefinition> processes)
   {
      StringBuffer existingProcesses = new StringBuffer();
      if (CollectionUtils.isNotEmpty(processes))
      {
         for (ProcessDefinition processDefinition : processes)
         {
            WorklistConfigTableEntry entry = new WorklistConfigTableEntry(processDefinition);
            if (existingConfigurations.contains(entry.getIdentityKey()))
            {
               existingProcesses.append(NEW_LINE_CHAR);
               existingProcesses.append(entry.getElementName());
            }
            else
            {
               addEntry(entry);
            }
         }

         if (StringUtils.isNotEmpty(existingProcesses.toString()))
         {
            MessageDialog.addErrorMessage(getParamMessage(PROCESS_EXIST_KEY, existingProcesses.toString()));
         }
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.processportal.view.worklistConfiguration.
    * WorklistColumnConfigurationBean#retrieveandSetConfigurationValues()
    */
   protected void retrieveConfigurations()
   {
      try
      {
         List<ProcessDefinition> processDefs = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();

         for (ProcessDefinition processDefinition : processDefs)
         {
            WorklistConfigTableEntry confTableEntry = new WorklistConfigTableEntry(processDefinition);
            if (existingConfigurations.contains(confTableEntry.getIdentityKey()))
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

   protected String getPropertyKey()
   {
      return PROCESS_KEY;
   }
}
