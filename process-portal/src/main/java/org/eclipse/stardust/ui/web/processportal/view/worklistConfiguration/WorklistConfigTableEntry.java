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
import java.util.Map;

import org.eclipse.stardust.engine.api.dto.ModelParticipantInfoDetails;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.ui.web.common.table.DefaultRowModel;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class WorklistConfigTableEntry extends DefaultRowModel
{
   private static final long serialVersionUID = 1533502219424771534L;
   private boolean selected;
   private boolean lock = false;
   protected String elementOID;
   private ArrayList<String> storedList;
   private List<WorklistColumn> worklistColumns;
   private String elementName;

   /**
    * @param participantInfo
    */
   public WorklistConfigTableEntry(ParticipantInfo participantInfo)
   {
      elementName = ModelHelper.getParticipantLabel(participantInfo).getLabel();

      if (participantInfo instanceof ModelParticipant)
      {
         elementOID = String.valueOf(((ModelParticipant) participantInfo).getElementOID());

      }
      else if (participantInfo instanceof ModelParticipantInfoDetails)
      {
         elementOID = String.valueOf(((ModelParticipantInfoDetails) participantInfo).getRuntimeElementOID());
      }
   }

   public WorklistConfigTableEntry(DepartmentInfo departmentInfo)
   {
      elementName = ModelHelper.getDepartmentLabel(departmentInfo).getLabel();
      elementOID = String.valueOf(departmentInfo.getOID());
   }

   public WorklistConfigTableEntry(ProcessDefinition processDefinition)
   {
      elementName = I18nUtils.getProcessName(processDefinition);
      this.elementOID = String.valueOf(processDefinition.getElementOID());
   }

   public WorklistConfigTableEntry(String customRow)
   {
      this.elementOID = customRow;
      elementName = MessagePropertiesBean.getInstance().getString("views.worklistPanelConfiguration.default");
   }

   public String getElementName()
   {
      return elementName;
   }

   public boolean isSelected()
   {
      return selected;
   }

   public void setSelected(boolean selected)
   {
      this.selected = selected;
   }

   public ArrayList<String> getColumnsToBeSaved()
   {
      ArrayList<String> colsToBeSaved;
      if (null != worklistColumns)
      {
         colsToBeSaved = new ArrayList<String>();
         for (WorklistColumn column : worklistColumns)
         {
            if (column.isVisible())
            {
               colsToBeSaved.add(column.getName());
            }
         }
      }
      else
      {
         colsToBeSaved = storedList;
      }

      return colsToBeSaved;

   }

   @SuppressWarnings("unchecked")
   public void setConfiguration(Map<String, Object> configuration)
   {
      this.storedList = (ArrayList<String>) configuration.get(WorklistConfigurationUtil.SELECTED_COLS);

      if (null == this.storedList)
      {
         this.storedList = (ArrayList<String>) WorklistConfigurationUtil.DEFAULT_CONF
               .get(WorklistConfigurationUtil.SELECTED_COLS);
      }
      String lock = (String) configuration.get(WorklistConfigurationUtil.LOCK);
      if (Boolean.valueOf(lock))
      {
         this.lock = true;
      }
   }

   public void configureColumns()
   {
      WorklistColumnSelectorPopup columnSelectorPopup = WorklistColumnSelectorPopup.getInstance();

      if (null != worklistColumns)
      {
         columnSelectorPopup.initializePopupWithColumns(worklistColumns);
      }
      else
      {
         columnSelectorPopup.initializePopup(storedList);
      }

      columnSelectorPopup.setParametricCallbackHandler(new ParametricCallbackHandler()
      {
         public void handleEvent(EventType eventType)
         {
            setSelectableColumns((List<WorklistColumn>) getParameter("columns"));
         }
      });
      columnSelectorPopup.openPopup();
   }

   private void setSelectableColumns(List<WorklistColumn> columns)
   {
      worklistColumns = columns;
   }

   public List<WorklistColumn> getSelectableColumns()
   {
      return worklistColumns;
   }

   public void lockValueChanged()
   {
      lock = !lock;
   }

   public boolean isLock()
   {
      return lock;
   }

   public String getElementOID()
   {
      return elementOID;
   }

   public List<String> getStoredList()
   {
      return storedList;
   }
}