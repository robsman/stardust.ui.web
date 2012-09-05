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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.ui.web.common.PopupUIComponentBean;
import org.eclipse.stardust.ui.web.common.util.FacesUtils;
import org.eclipse.stardust.ui.web.processportal.common.Constants;
import org.eclipse.stardust.ui.web.processportal.common.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ParametricCallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;

/**
 * @author Yogesh.Manware
 * 
 */
public class WorklistColumnSelectorPopup extends PopupUIComponentBean
{
   private static final long serialVersionUID = -3516314336727808038L;
   private static final String BEAN_NAME = "worklistColumnSelectorPopup";

   private List<WorklistColumn> columns;
   private ParametricCallbackHandler parametricCallbackHandler;
   private String elementName = "";

   public static WorklistColumnSelectorPopup getInstance()
   {
      return (WorklistColumnSelectorPopup) FacesUtils.getBeanFromContext(BEAN_NAME);
   }

   public void initializePopup(List<String> storedList)
   {
      setTitle(org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean.getInstance().getString(
            "common.filterPopup.selectColumnsLabel"));

      this.setPopupAutoCenter(false);
      columns = new ArrayList<WorklistColumn>();

      WorklistColumn activityNameCol = new WorklistColumn(Constants.COL_ACTIVITY_NAME, getMessage("column.overview"),
            false);
      WorklistColumn colOid = new WorklistColumn(Constants.COL_OID, getMessage("column.oid"), false);
      WorklistColumn processDefnCol = new WorklistColumn(Constants.COL_PROCESS_DEFINITION, getMessage("processName"),
            false);
      WorklistColumn criticalityCol = new WorklistColumn(Constants.COL_CRITICALITY, getMessage("column.criticality"),
            false);
      WorklistColumn colDescriptors = new WorklistColumn(Constants.COL_DESCRIPTORS, getMessage("column.descriptors"),
            false);
      WorklistColumn colPriority = new WorklistColumn(Constants.COL_PRIORITY, getMessage("column.priority"), false);
      WorklistColumn colStarted = new WorklistColumn(Constants.COL_STARTED, getMessage("column.started"), false);
      WorklistColumn colLastMod = new WorklistColumn(Constants.COL_LAST_MODIFIED,
            getMessage("column.lastmodification"), false);
      WorklistColumn durationCol = new WorklistColumn(Constants.COL_DURATION, getMessage("column.duration"), false);
      WorklistColumn lastPerformerCol = new WorklistColumn(Constants.COL_LAST_PERFORMER,
            getMessage("column.lastPerformer"), false);
      WorklistColumn statusCol = new WorklistColumn(Constants.COL_STATUS, getMessage("column.status"), false);
      WorklistColumn assignedToCol = new WorklistColumn(Constants.COL_ASSIGNED_TO, getMessage("column.assignedTo"),
            false);

      columns.add(activityNameCol);
      columns.add(colOid);
      columns.add(processDefnCol);
      columns.add(criticalityCol);
      columns.add(colPriority);
      columns.add(colDescriptors);
      columns.add(colStarted);
      columns.add(colLastMod);
      columns.add(durationCol);
      columns.add(lastPerformerCol);
      columns.add(statusCol);
      columns.add(assignedToCol);

      columns.addAll(createDescriptorColumns());
      columns = orderAndSelectAsPerSavedState(storedList);
   }

   private String getMessage(String partialKey)
   {
      return MessagePropertiesBean.getInstance().getString("views.worklistPanel." + partialKey);
   }

   public void initializePopupWithColumns(List<WorklistColumn> columns)
   {
      this.setPopupAutoCenter(false);
      this.columns = new ArrayList<WorklistColumn>();
      for (WorklistColumn worklistColumn : columns)
      {
         this.columns.add(worklistColumn.clone());
      }
   }

   public void apply()
   {
      closePopup();
      if (null != parametricCallbackHandler)
      {
         parametricCallbackHandler.setParameter("columns", columns);
         parametricCallbackHandler.handleEvent(ICallbackHandler.EventType.APPLY);
      }
   }

   private List<WorklistColumn> orderAndSelectAsPerSavedState(List<String> storedList)
   {
      if (storedList == null)
      {
         return columns;
      }
      else
      {
         WorklistColumn colPreference;
         List<WorklistColumn> selectedOrderedList = new ArrayList<WorklistColumn>();
         Map<String, WorklistColumn> colsMap = getColumnsAsMap(columns);

         for (String colName : storedList)
         {
            colPreference = colsMap.get(colName);
            if (colPreference != null)
            {
               colPreference.setVisible(true);
               selectedOrderedList.add(colPreference);
            }
         }

         for (WorklistColumn columnPreference : columns)
         {
            if (!selectedOrderedList.contains(columnPreference))
            {
               columnPreference.setVisible(false);
               selectedOrderedList.add(columnPreference);
            }
         }
         return selectedOrderedList;
      }
   }

   /**
    * @param cols
    * @return
    */
   private Map<String, WorklistColumn> getColumnsAsMap(List<WorklistColumn> cols)
   {
      Map<String, WorklistColumn> colsPref = new HashMap<String, WorklistColumn>();
      for (WorklistColumn col : cols)
      {
         colsPref.put(col.getName(), col);
      }
      return colsPref;
   }

   private static List<WorklistColumn> createDescriptorColumns()
   {
      List<WorklistColumn> descriptorColumns = new ArrayList<WorklistColumn>();

      for (Entry<String, DataPath> descriptor : CommonDescriptorUtils.getAllDescriptors(false).entrySet())
      {
         String descriptorId = descriptor.getKey();
         DataPath dataPath = descriptor.getValue();

         WorklistColumn descriptorColumn = new WorklistColumn(descriptorId, I18nUtils.getDataPathName(dataPath), false);
         descriptorColumns.add(descriptorColumn);
      }
      return descriptorColumns;
   }

   public void initialize()
   {}

   public List<WorklistColumn> getColumns()
   {
      return columns;
   }

   public void setParametricCallbackHandler(ParametricCallbackHandler parametricCallbackHandler)
   {
      this.parametricCallbackHandler = parametricCallbackHandler;
   }

   public String getElementName()
   {
      return elementName;
   }

   public void setElementName(String elementName)
   {
      this.elementName = elementName;
   }
}
