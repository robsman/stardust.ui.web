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
import java.util.Arrays;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModelListener;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.common.message.MessageDialog;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.core.ProcessInstanceDetailConfigurationBean;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorColumnUtils;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AbortProcessBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;



/**
 * @author Yogesh.Manware
 * 
 */
public class ProcessUIBuilder
{
   public static final String IMAGE_BASE_PATH = "/plugins/views-common/images/icons/process-history/";
   private MessagesViewsCommonBean propsBean;
   private List<SelectItem> priorityItems;
   private ICallbackHandler callbackHandler;
   private IColumnModel processTreeColumnModel;
   private IColumnModelListener columnModelListener;
   private TableColumnSelectorPopup processColumnFilterPopup;
   private boolean embedded = true;
   private boolean miniMode;
   private boolean showTitle;
   private boolean useColumnLevelFilters;
   private boolean useFilterDialog;
   private List<FilterToolbarItem> processFilterToolbarItems;
   private TableDataFilters onOffFilters;

   /**
    * @param columnModelListener
    */
   public ProcessUIBuilder(IColumnModelListener columnModelListener)
   {
      propsBean = MessagesViewsCommonBean.getInstance();

      priorityItems = new ArrayList<SelectItem>();
      priorityItems.add(new SelectItem(Integer.valueOf(1), propsBean
            .getString("processHistory.processTable.priorities.high")));
      priorityItems.add(new SelectItem(Integer.valueOf(0), propsBean
            .getString("processHistory.processTable.priorities.normal")));
      priorityItems.add(new SelectItem(Integer.valueOf(-1), propsBean
            .getString("processHistory.processTable.priorities.low")));

      this.columnModelListener = columnModelListener;

      initializeProcessColumnModel();
   }

   /**
    * 
    */
   public void initializeProcessColumnModel()
   {
      createFilterToolbar();
      initializeDataFilters();
      
      // ****************** COLUMN SELECTOR ******************
      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();

      List<ColumnPreference> fixedBeforeColumns = new ArrayList<ColumnPreference>();

      ColumnPreference selectColPref = new ColumnPreference("ColumnSelect", null, propsBean
            .getString("processHistory.processTable.select"), ResourcePaths.VIEW_PROCESS_INSTANCE_HISTORY_COLUMNS,
            true, false);
      selectColPref.setExportable(false);
      fixedBeforeColumns.add(selectColPref);

      ColumnPreference nameColPref = new ColumnPreference("Process Name", "text", ColumnDataType.STRING, propsBean
            .getString("processHistory.processTable.nameLabel"));

      selectableCols.add(nameColPref);

      ColumnPreference oidColPref = new ColumnPreference("OID", "OID", ColumnDataType.NUMBER, propsBean
            .getString("processHistory.processTable.oidLabel"));

      selectableCols.add(oidColPref);

      ColumnPreference priorityColPref = new ColumnPreference("Priority", "priority", propsBean
            .getString("processHistory.processTable.priority"), ResourcePaths.VIEW_PROCESS_INSTANCE_HISTORY_COLUMNS,
            true, false);

      selectableCols.add(priorityColPref);

      ColumnPreference descColPref = new ColumnPreference("Descriptors", "descriptors", propsBean
            .getString("processHistory.processTable.descriptors"), ResourcePaths.VIEW_PROCESS_INSTANCE_HISTORY_COLUMNS,
            true, false);

      selectableCols.add(descColPref);

      ColumnPreference startingUserColPref = new ColumnPreference("Starting User", "startingUser",
            ColumnDataType.STRING, propsBean.getString("processHistory.processTable.startingUser"));

      selectableCols.add(startingUserColPref);

      ColumnPreference startTimeColPref = new ColumnPreference("Start Time", "startTime", ColumnDataType.DATE,
            propsBean.getString("processHistory.processTable.startLabel"), true, true);
      startTimeColPref.setNoWrap(true);

      selectableCols.add(startTimeColPref);

      ColumnPreference durationColPref = new ColumnPreference("Duration", "duration", ColumnDataType.STRING, propsBean
            .getString("processHistory.processTable.duration"));

      selectableCols.add(durationColPref);

      ColumnPreference statusColPref = new ColumnPreference("Status", "state", ColumnDataType.STRING, propsBean
            .getString("processHistory.processTable.statusLabel"));

      selectableCols.add(statusColPref);

      ColumnPreference rootOIDColPref = new ColumnPreference("Root Process OID", "processInstanceRootOID",
            ColumnDataType.NUMBER, propsBean.getString("processHistory.processTable.processInstanceRootOID"));
      rootOIDColPref.setVisible(false);
      selectableCols.add(rootOIDColPref);

      ColumnPreference endTimeColPref = new ColumnPreference("End Time", "endTime", ColumnDataType.DATE, propsBean
            .getString("processHistory.processTable.endTime"));
      endTimeColPref.setVisible(false);
      selectableCols.add(endTimeColPref);

      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();

      ColumnPreference actionColPref = new ColumnPreference("ActionsColumn", "", propsBean
            .getString("processHistory.processTable.abortActionLabel"),
            ResourcePaths.VIEW_PROCESS_INSTANCE_HISTORY_COLUMNS, true, false);
      actionColPref.setExportable(false);
      fixedAfterColumns.add(actionColPref);

      selectableCols.addAll(DescriptorColumnUtils.createDescriptorColumns());

      processTreeColumnModel = new DefaultColumnModel(selectableCols, fixedBeforeColumns, fixedAfterColumns,
            "views-common", "mytab.processInstanceHistory", columnModelListener);
      processColumnFilterPopup = new TableColumnSelectorPopup(processTreeColumnModel);
      
   }
   
   /**
    * @param ae
    */
   public void openNotes(ActionEvent ae)
   {
      ProcessInstance pi = (ProcessInstance) ae.getComponent().getAttributes().get("processInstance");
      ProcessInstanceUtils.openNotes(pi);
   }

   /**
    * Recovers Process
    * 
    * @param ae
    */
   public void recoverProcess(ActionEvent ae)
   {
      ProcessInstance pi = (ProcessInstance) ae.getComponent().getAttributes().get("processInstance");
      try
      {
         if (pi != null)
         {
            ProcessInstanceUtils.recoverProcessInstance(Arrays.asList(pi.getOID()));
         }
      }
      catch (AccessForbiddenException e)
      {
         MessageDialog.addErrorMessage(propsBean.getString("common.authorization.msg"));
      }
      catch (Exception e)
      {
         MessageDialog.addErrorMessage(e);
      }
   }


   /**
    * Terminates Process
    * 
    * @param ae
    */
   public void terminateProcess(ActionEvent ae)
   {
      ProcessInstance pi = (ProcessInstance) ae.getComponent().getAttributes().get("processInstance");
      if (null != pi)
      {
         AbortProcessBean abortProcessHelper = AbortProcessBean.getInstance();
         abortProcessHelper.abortProcess(pi);
      }
   }

   public void createFilterToolbar()
   {
      int i = 0;
      processFilterToolbarItems = new ArrayList<FilterToolbarItem>();
      FilterToolbarItem filterToolbarItem = new FilterToolbarItem("" + i++, "AuxiliaryProcess",
            "processHistory.processTable.showAuxiliaryProcess", "processHistory.processTable.hideAuxiliaryProcess",
            "process_auxiliary.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH);
      filterToolbarItem.setActive(ProcessInstanceDetailConfigurationBean.isAuxiliaryProcessFilterOn());
      processFilterToolbarItems.add(filterToolbarItem);
   }

   /**
    * @param name
    * @return
    */
   public FilterToolbarItem getFilterToolbarItem(String name)
   {
      for (FilterToolbarItem filterToolbarItem : processFilterToolbarItems)
      {
         if (filterToolbarItem.getName().equals(name))
         {
            return filterToolbarItem;
         }
      }
      return null;
   }

   /**
    * 
    */
   public void initializeDataFilters()
   {
      onOffFilters = new TableDataFilters();
      for (FilterToolbarItem filterToolbarItem : processFilterToolbarItems)
      {
         onOffFilters.addDataFilter(new TableDataFilterOnOff(filterToolbarItem.getName(), null, true, false));
      }
   }

   public ICallbackHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public IColumnModelListener getColumnModelListener()
   {
      return columnModelListener;
   }

   public List<SelectItem> getPriorityItems()
   {
      return priorityItems;
   }

   public TableColumnSelectorPopup getProcessColumnFilterPopup()
   {
      return processColumnFilterPopup;
   }

   public IColumnModel getProcessTreeColumnModel()
   {
      return processTreeColumnModel;
   }

   public boolean isEmbedded()
   {
      return embedded;
   }

   public boolean isMiniMode()
   {
      return miniMode;
   }

   public boolean isShowTitle()
   {
      return showTitle;
   }

   public boolean isUseColumnLevelFilters()
   {
      return useColumnLevelFilters;
   }

   public boolean isUseFilterDialog()
   {
      return useFilterDialog;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public void setColumnModelListener(IColumnModelListener columnModelListener)
   {
      this.columnModelListener = columnModelListener;
   }

   public void setEmbedded(boolean embedded)
   {
      this.embedded = embedded;
   }

   public void setMiniMode(boolean miniMode)
   {
      this.miniMode = miniMode;
   }

   public void setProcessColumnFilterPopup(TableColumnSelectorPopup processColumnFilterPopup)
   {
      this.processColumnFilterPopup = processColumnFilterPopup;
   }

   public void setProcessTreeColumnModel(IColumnModel processTreeColumnModel)
   {
      this.processTreeColumnModel = processTreeColumnModel;
   }

   public void setShowTitle(boolean showTitle)
   {
      this.showTitle = showTitle;
   }

   public void setUseColumnLevelFilters(boolean useColumnLevelFilters)
   {
      this.useColumnLevelFilters = useColumnLevelFilters;
   }

   public void setUseFilterDialog(boolean useFilterDialog)
   {
      this.useFilterDialog = useFilterDialog;
   }
   public List<FilterToolbarItem> getProcessFilterToolbarItems()
   {
      return processFilterToolbarItems;
   }
   public TableDataFilters getOnOffFilters()
   {
      return onOffFilters;
   }
}