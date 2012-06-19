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
import java.util.List;

import javax.faces.event.ActionEvent;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModelListener;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterOnOff;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilterPopup;
import org.eclipse.stardust.ui.web.common.filter.TableDataFilters;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.FilterToolbarItem;
import org.eclipse.stardust.ui.web.viewscommon.core.ProcessInstanceDetailConfigurationBean;
import org.eclipse.stardust.ui.web.viewscommon.core.ResourcePaths;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.DelegationBean;
import org.eclipse.stardust.ui.web.viewscommon.dialogs.ICallbackHandler;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;



public class ActivityUIBuilder
{
   private static final Logger trace = LogManager.getLogger(ProcessHistoryTable.class);
   private MessagesViewsCommonBean propsBean;
   private ICallbackHandler callbackHandler;
   private IColumnModel activityTreeColumnModel;
   private IColumnModelListener columnModelListener;
   private List<FilterToolbarItem> activityFilterToolbarItems;
   private TableColumnSelectorPopup activityColumnFilterPopup;
   private TableDataFilterPopup dataFilterPopup;
   private TableDataFilters onOffFilters;
   private boolean showTitle;
   private boolean useColumnLevelFilters;
   private boolean useFilterDialog;

   /**
    * @param columnModelListener
    */
   public ActivityUIBuilder(IColumnModelListener columnModelListener)
   {
      propsBean = MessagesViewsCommonBean.getInstance();
      this.columnModelListener = columnModelListener;

      createFilterToolbar();
      initializeActivityColumnModel();
      initializeDataFilters();
   }

   /**
    *
    */
   public void createFilterToolbar()
   {
      int i = 0;
      activityFilterToolbarItems = new ArrayList<FilterToolbarItem>();

      activityFilterToolbarItems.add(new FilterToolbarItem("" + i++, "ApplicationActivity",
            "processHistory.activityTable.showApplicationActivity",
            "processHistory.activityTable.hideApplicationActivity", "activity_application.png",
            Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));
      activityFilterToolbarItems.add(new FilterToolbarItem("" + i++, "ManualActivity",
            "processHistory.activityTable.showManualActivity", "processHistory.activityTable.hideManualActivity",
            "activity_manual.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));
      activityFilterToolbarItems.add(new FilterToolbarItem("" + i++, "Auxiliary",
            "processHistory.activityTable.showAuxiliaryActivity", "processHistory.activityTable.hideAuxiliaryActivity",
            "activity_auxiliary.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      activityFilterToolbarItems.add(new FilterToolbarItem("" + i++, "Delegate",
            "processHistory.activityTable.showDelegate", "processHistory.activityTable.hideDelegate", "delegate.png",
            Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));
      activityFilterToolbarItems.add(new FilterToolbarItem("" + i++, "Exception",
            "processHistory.activityTable.showException", "processHistory.activityTable.hideException",
            "exception.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      activityFilterToolbarItems.add(new FilterToolbarItem("" + i++, "ActivityCompleted",
            "processHistory.activityTable.showEventsCompleted", "processHistory.activityTable.hideEventsCompleted",
            "activity_completed.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      activityFilterToolbarItems.add(new FilterToolbarItem("" + i++, "StateChange",
            "processHistory.activityTable.showStateChange", "processHistory.activityTable.hideStateChange",
            "activity_state.png", Constants.PROCESS_HISTORY_IMAGES_BASE_PATH));

      setValuesFromConfiguration();
   }

   /**
    * 
    */
   public void initializeActivityColumnModel()
   {
      List<ColumnPreference> fixedBeforeColumns = new ArrayList<ColumnPreference>();
      List<ColumnPreference> fixedAfterColumns = new ArrayList<ColumnPreference>();

      // ****************** COLUMN SELECTOR ******************
      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();

      ColumnPreference nameColPref = new ColumnPreference("Activity Name", "text", ColumnDataType.STRING, propsBean
            .getString("processHistory.activityTable.nameLabel"));

      ColumnPreference eventDetailColPref = new ColumnPreference("Event Details", "details", propsBean
            .getString("processHistory.activityTable.detailsLabel"),
            ResourcePaths.VIEW_ACTIVITY_INSTANCE_HISTORY_COLUMNS, true, false);

      selectableCols.add(eventDetailColPref);

      ColumnPreference oIDColPref = new ColumnPreference("OID", "OID", ColumnDataType.NUMBER, propsBean
            .getString("processHistory.activityTable.oidLabel"));

      selectableCols.add(oIDColPref);
      
      ColumnPreference criticalityCol = new ColumnPreference("Criticality", "criticalityLabel",
            propsBean.getString("processHistory.activityTable.criticality"),
            ResourcePaths.VIEW_ACTIVITY_INSTANCE_HISTORY_COLUMNS, true, false);
      selectableCols.add(criticalityCol);

      ColumnPreference startTimecolPref = new ColumnPreference("Started", "startTime", ColumnDataType.DATE, propsBean
            .getString("processHistory.activityTable.startLabel"), true, true);
      startTimecolPref.setNoWrap(true);

      selectableCols.add(startTimecolPref);

      ColumnPreference lastModificationColPref = new ColumnPreference("LastModification", "modificationTime",
            ColumnDataType.DATE, propsBean.getString("processHistory.activityTable.lastModificationLabel"), true, true);
      lastModificationColPref.setNoWrap(true);

      selectableCols.add(lastModificationColPref);

      ColumnPreference performerColPref = new ColumnPreference("Event Performer", "user", ColumnDataType.STRING,
            propsBean.getString("processHistory.activityTable.performerLabel"));

      selectableCols.add(performerColPref);

      ColumnPreference assignedToColPref = new ColumnPreference("Assigned To", "assignedTo", ColumnDataType.STRING,
            propsBean.getString("processHistory.activityTable.assignedTo"));

      selectableCols.add(assignedToColPref);

      ColumnPreference statusColPref = new ColumnPreference("Status", "state", ColumnDataType.STRING, propsBean
            .getString("processHistory.activityTable.statusLabel"));

      selectableCols.add(statusColPref);

      ColumnPreference actionColPref = new ColumnPreference("Actions", "", propsBean
            .getString("processHistory.activityTable.abortActionLabel"),
            ResourcePaths.VIEW_ACTIVITY_INSTANCE_HISTORY_COLUMNS, true, false);
      actionColPref.setExportable(false);
      fixedBeforeColumns.add(nameColPref);
      fixedAfterColumns.add(actionColPref);

      activityTreeColumnModel = new DefaultColumnModel(selectableCols, fixedBeforeColumns, fixedAfterColumns, "common",
            "mytab.processHistory11", columnModelListener);
      activityColumnFilterPopup = new TableColumnSelectorPopup(activityTreeColumnModel);
   }

   /**
    * 
    */
   public void initializeDataFilters()
   {
      onOffFilters = new TableDataFilters();

      for (FilterToolbarItem filterToolbarItem : activityFilterToolbarItems)
      {
         onOffFilters.addDataFilter(new TableDataFilterOnOff(filterToolbarItem.getName(), null, true, false));
      }
   }

   /**
    * @param name
    * @return
    */
   public FilterToolbarItem getFilterToolbarItem(String name)
   {
      for (FilterToolbarItem filterToolbarItem : activityFilterToolbarItems)
      {
         if (filterToolbarItem.getName().equals(name))
         {
            return filterToolbarItem;
         }
      }
      return null;
   }

   /**
    * opens delegation dialog
    * 
    * @param ae
    */
   public void openDelegateDialog(ActionEvent ae)
   {
      ActivityTableEntryUserObject row = (ActivityTableEntryUserObject) ae.getComponent().getAttributes().get("row");
      DelegationBean delegationBean = DelegationBean.getCurrent();

      if (row.getTableEntry().getRuntimeObject() instanceof ActivityInstance)
      {
         ActivityInstance activityInstance = (ActivityInstance) row.getTableEntry().getRuntimeObject();
         delegationBean.setAi(activityInstance);
         delegationBean.setICallbackHandler(callbackHandler);
         delegationBean.openPopup();
      }
      else
      {
         trace.warn(this.getClass().getName() + " Method: openDelegateDialog()"
               + " Runtime Object is not of type ActivityInstance");
      }
   }

   /**
    * 
    */
   private void setValuesFromConfiguration()
   {
      activityFilterToolbarItems.get(0).setActive(ProcessInstanceDetailConfigurationBean.isApplicationActivityFilterOn());
      activityFilterToolbarItems.get(1).setActive(ProcessInstanceDetailConfigurationBean.isManualActivityFilterOn());
      activityFilterToolbarItems.get(2).setActive(ProcessInstanceDetailConfigurationBean.isAuxiliaryActivityFilterOn());
      activityFilterToolbarItems.get(3).setActive(ProcessInstanceDetailConfigurationBean.isDelegateFilterOn());
      activityFilterToolbarItems.get(4).setActive(ProcessInstanceDetailConfigurationBean.isExceptionFilterOn());
      activityFilterToolbarItems.get(5).setActive(ProcessInstanceDetailConfigurationBean.isActivityCompletedFilterOn());
      activityFilterToolbarItems.get(6).setActive(ProcessInstanceDetailConfigurationBean.isStateChangedFilterOn());
   }
   
   public TableColumnSelectorPopup getActivityColumnFilterPopup()
   {
      return activityColumnFilterPopup;
   }

   public List<FilterToolbarItem> getActivityFilterToolbarItems()
   {
      return activityFilterToolbarItems;
   }

   public IColumnModel getActivityTreeColumnModel()
   {
      return activityTreeColumnModel;
   }

   public ICallbackHandler getCallbackHandler()
   {
      return callbackHandler;
   }

   public IColumnModelListener getColumnModelListener()
   {
      return columnModelListener;
   }

   public TableDataFilterPopup getDataFilterPopup()
   {
      return dataFilterPopup;
   }

   public TableDataFilters getOnOffFilters()
   {
      return onOffFilters;
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

   public void setActivityColumnFilterPopup(TableColumnSelectorPopup activityColumnFilterPopup)
   {
      this.activityColumnFilterPopup = activityColumnFilterPopup;
   }

   public void setActivityFilterToolbarItems(List<FilterToolbarItem> activityFilterToolbarItems)
   {
      this.activityFilterToolbarItems = activityFilterToolbarItems;
   }

   public void setActivityTreeColumnModel(IColumnModel activityTreeColumnModel)
   {
      this.activityTreeColumnModel = activityTreeColumnModel;
   }

   public void setCallbackHandler(ICallbackHandler callbackHandler)
   {
      this.callbackHandler = callbackHandler;
   }

   public void setColumnModelListener(IColumnModelListener columnModelListener)
   {
      this.columnModelListener = columnModelListener;
   }

   public void setDataFilterPopup(TableDataFilterPopup dataFilterPopup)
   {
      this.dataFilterPopup = dataFilterPopup;
   }

   public void setOnOffFilters(TableDataFilters onOffFilters)
   {
      this.onOffFilters = onOffFilters;
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
}