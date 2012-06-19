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
package org.eclipse.stardust.ui.web.bcc.views;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.OpenActivitiesStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.ResourcePaths;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.OpenActivitiesCalculator;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.messsages.MessagesBCCBean;
import org.eclipse.stardust.ui.web.common.UIComponentBean;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference;
import org.eclipse.stardust.ui.web.common.column.DefaultColumnModel;
import org.eclipse.stardust.ui.web.common.column.IColumnModel;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnAlignment;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.common.columnSelector.TableColumnSelectorPopup;
import org.eclipse.stardust.ui.web.common.event.ViewEvent;
import org.eclipse.stardust.ui.web.common.event.ViewEventHandler;
import org.eclipse.stardust.ui.web.common.event.ViewEvent.ViewEventType;
import org.eclipse.stardust.ui.web.common.table.SortableTable;
import org.eclipse.stardust.ui.web.common.table.SortableTableComparator;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;



/**
 * @author Giridhara.G
 * @version
 */

public class OpenActivitiesBean extends UIComponentBean implements ResourcePaths,ViewEventHandler
{
   private static final long serialVersionUID = 1L;
   private static final Logger trace = LogManager.getLogger(OpenActivitiesBean.class);
   
   private static final int COLUMN_SIZE = 5;

   protected static final int TODAY_COL_OFFSET = 0;

   protected static final int YESTERDAY_COL_OFFSET = 1;

   protected static final int DAY_AVG_COL_OFFSET = 2;

   private SortableTable<OpenActivitiesUserObject> pendingActTable;

   private WorkflowFacade facade;

   private MessagesBCCBean propsBean;
   
   
   /**
    * OpenActivitiesBean Constructor
    */
   public OpenActivitiesBean()
   {
      super(V_pendingActivitiesView);
   }

   public void handleEvent(ViewEvent event)
   {
      if (ViewEventType.CREATED == event.getType())
      {        
         facade = WorkflowFacade.getWorkflowFacade();
         propsBean = MessagesBCCBean.getInstance();
         initialize();

      }
   }
   /**
    * Refresh the table
    */
   public void update()
   {
      initialize();
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.stardust.ui.web.common.UIComponentBean#initialize()
    */
   public void initialize()
   {
      OpenActivitiesStatisticsQuery query = OpenActivitiesStatisticsQuery
            .forAllProcesses();
      query.setPolicy(new CriticalExecutionTimePolicy(Constants
            .getCriticalDurationThreshold(ProcessInstancePriority.LOW, 1.0f), Constants
            .getCriticalDurationThreshold(ProcessInstancePriority.NORMAL, 1.0f),
            Constants.getCriticalDurationThreshold(ProcessInstancePriority.HIGH, 1.0f)));
      OpenActivitiesStatistics openActivityStatistics = (OpenActivitiesStatistics) facade
            .getAllActivityInstances(query);

      Collection<ProcessDefinition> processDefinition =facade.getAllProcessDefinitions();

      OpenActivitiesCalculator openActivitiesCalculator = new OpenActivitiesCalculator(
            processDefinition, openActivityStatistics);

      List<String> overviewList = new ArrayList<String>();
      overviewList.add(this.getMessages().getString("column.totalOpenActivity"));
      overviewList.add(this.getMessages().getString("column.criticalOpenActivity"));

      String[] participantname = null;
      List<OpenActivitiesUserObject> pendingActList = new ArrayList<OpenActivitiesUserObject>();
      List< RoleItem > participantList = facade.getAllRoles();

      List<Object[]> data = new ArrayList<Object[]>();

      int colSize = 1;
      if (participantList != null)
      {
         colSize = (participantList.size() * 3);
         participantname = new String[participantList.size()];
      }
      Object rowTotalOpenActivities[] = new Object[colSize];

      Object rowCriticalOpenActivities[] = new Object[colSize];

      if (participantList != null)
      {
         int index = 0;
         RoleItem roleItem;
         ModelParticipantInfo roledetails;     
         Map totalOpenActivities;
         Map criticalOpenActivities;
         for (int i = 0; i < participantList.size(); i++)
         {
            roleItem =  participantList.get(i);
            roledetails = roleItem.getRole();
            participantname[i] = roleItem.getRoleName();

            totalOpenActivities = openActivitiesCalculator
                  .getTotalOpenActivities(roledetails);
            criticalOpenActivities = openActivitiesCalculator
                  .getCriticalOpenActivities(roledetails);

            setRowContent(rowTotalOpenActivities, index, totalOpenActivities);
            setRowContent(rowCriticalOpenActivities, index, criticalOpenActivities);
            index = index + 3;
         }
         data.add(rowTotalOpenActivities);
         data.add(rowCriticalOpenActivities);
      }

      if (data != null && data.size() > 0)
      {
         int count = 0;
         List<OpenActivitiesDynamicUserObject> pendingActDynamicList;
         Object[] obj;
         for (int j = 0; j < data.size(); j++)
         {

            pendingActDynamicList = new ArrayList<OpenActivitiesDynamicUserObject>();
            obj = (Object[]) data.get(j);
            if (obj != null && obj.length > 0)
            {
               for (int k = 0; k < obj.length; k++)
               {
                  pendingActDynamicList.add(new OpenActivitiesDynamicUserObject(
                        (Long) (obj[k]), (Long) (obj[++k]), (Double) (obj[++k])));
               }
               pendingActList.add(new OpenActivitiesUserObject(overviewList.get(count)
                     .toString(), pendingActDynamicList));
               count++;
            }
         }
      }

      List<ColumnPreference> fixedCols = new ArrayList<ColumnPreference>();
      fixedCols.add(new ColumnPreference("Overview", "overviewLabel", this.getMessages()
            .getString("column.overView"), V_pendingActivitiesColumns,
            null, true, true));
      
      // Add Columns as per Data
      List<ColumnPreference> selectableCols = new ArrayList<ColumnPreference>();
      if (pendingActList.size() > 0)
      {
         List<OpenActivitiesDynamicUserObject> roleList = pendingActList.get(0)
               .getParticipantList();
         int i = 0;
         boolean visible = true;
         ColumnPreference dynaCol;
         ColumnPreference colToday;
         ColumnPreference colYesterday;
         ColumnPreference colMonth;         
         for (OpenActivitiesDynamicUserObject re : roleList)
         {
            dynaCol = new ColumnPreference("participant" + i, participantname[i]);
            
            visible = i >= COLUMN_SIZE ? false : true;
            
            dynaCol.setVisible(visible);
            
            colToday = new ColumnPreference("today" + i, "participantList[" + i
                  + "].today", ColumnDataType.NUMBER, this.getMessages().getString(
                  "column.today"), true, false);
            colToday.setColumnAlignment(ColumnAlignment.CENTER);

            colYesterday = new ColumnPreference("yesterday" + i, "participantList[" + i
                  + "].yesterday", ColumnDataType.NUMBER, this.getMessages().getString(
                  "column.yesterday"), true, false);

            colYesterday.setColumnAlignment(ColumnAlignment.CENTER);

            colMonth = new ColumnPreference("month" + i, "participantList[" + i
                  + "].month", ColumnDataType.NUMBER, this.getMessages().getString(
                  "column.dayMonth"), true, false);

            colMonth.setColumnAlignment(ColumnAlignment.CENTER);

            dynaCol.addChildren(colToday);
            dynaCol.addChildren(colYesterday);
            dynaCol.addChildren(colMonth);

            i++;
            selectableCols.add(dynaCol);
         }
         
      }
      IColumnModel columnModel = new DefaultColumnModel(selectableCols, fixedCols, null,
            UserPreferencesEntries.M_BCC, UserPreferencesEntries.V_PENDING_ACTIVITIES);
      TableColumnSelectorPopup colSelecpopup = new TableColumnSelectorPopup(columnModel);

      pendingActTable = new SortableTable<OpenActivitiesUserObject>(colSelecpopup, null,
            new SortableTableComparator<OpenActivitiesUserObject>("overviewLabel", true));
      pendingActTable.setList(pendingActList);
      pendingActTable.initialize();

   }

   /**
    * @param row
    * @param index
    * @param openActivities
    */
   private void setRowContent(Object[] row, int index, Map openActivities)
   {
      row[index + TODAY_COL_OFFSET] = new Long(((Double) openActivities
            .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_TODAY)).longValue());
      row[index + YESTERDAY_COL_OFFSET] = new Long(((Double) openActivities
            .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_YESTERDAY)).longValue());
      row[index + DAY_AVG_COL_OFFSET] = openActivities
            .get(OpenActivitiesCalculator.OPEN_ACTIVITIES_AVG);
   }

   public SortableTable<OpenActivitiesUserObject> getPendingActTable()
   {
      return pendingActTable;
   }
}
