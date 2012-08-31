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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class SelectProcessPopup extends PopupUIComponentBean
{

   private static final long serialVersionUID = 7327885270237102911L;
   private static final String BEAN_NAME = "selectProcessPopup";
   private static final String ALL = "ALL";

   private Map<String, ProcessDefinition> processDefinitions = new HashMap<String, ProcessDefinition>();
   private List<ProcessDefinition> allAccessibleProcesses = null;
   private List<SelectItem> processes = null;
   private String[] selectedProcesses;

   private ParametricCallbackHandler callbackHandler;

   public SelectProcessPopup()
   {
      super();
   }

   public static SelectProcessPopup getInstance()
   {
      return (SelectProcessPopup) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public final String[] getSelectedProcesses()
   {
      if (selectedProcesses == null || selectedProcesses.length == 0)
      {
         resetSelectedProcesses();
      }
      return selectedProcesses;
   }

   public List<SelectItem> getProcesses()
   {
      return processes;
   }

   public final void setSelectedProcesses(String[] selectedProcesses)
   {
      this.selectedProcesses = selectedProcesses;
   }

   public void initializeBean()
   {
      this.setPopupAutoCenter(false);
      setTitle("Select Processes");
      processes = new ArrayList<SelectItem>();
      processDefinitions.clear();
      List<ProcessDefinition> allProcessDefinitions = null;
      allAccessibleProcesses = ProcessDefinitionUtils.getAllAccessibleProcessDefinitions();
      allProcessDefinitions = allAccessibleProcesses;
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
   public List<ProcessDefinition> getSelectedProcessDefs()
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

   public void apply()
   {
      closePopup();
      if (null != callbackHandler)
      {
         Map<String, Object> selectedProcesses = new HashMap<String, Object>();
         selectedProcesses.put("processes", getSelectedProcessDefs());
         callbackHandler.setParameters(selectedProcesses);
         callbackHandler.handleEvent(ICallbackHandler.EventType.APPLY);
      }
   }

   public void setCallbackHandler(ParametricCallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public void initialize()
   {}
}
