/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Abhay.Thappan
 */
package org.eclipse.stardust.ui.web.rest.component.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.runtime.AdministrationService;
import org.eclipse.stardust.engine.core.preferences.PreferenceScope;
import org.eclipse.stardust.engine.core.preferences.Preferences;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalProcessingTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.DateRange;
import org.eclipse.stardust.engine.core.query.statistics.api.StatisticsDateRangePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatistics.WorktimeStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserWorktimeStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.BusinessControlCenterConstants;
import org.eclipse.stardust.ui.web.bcc.jsf.ProcessingTimePerProcess;
import org.eclipse.stardust.ui.web.bcc.views.CustomColumnUtils;
import org.eclipse.stardust.ui.web.common.configuration.UserPreferencesHelper;
import org.eclipse.stardust.ui.web.common.util.CollectionUtils;
import org.eclipse.stardust.ui.web.common.util.DateUtils;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.dto.ColumnDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessingTimeDTO;
import org.eclipse.stardust.ui.web.rest.dto.ResourcePerformanceQueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.ResourcePerformanceStatisticsDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
public class ResourcePerformanceUtils
{
   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   public ResourcePerformanceQueryResultDTO createUserStatistics(String roleId)
   {
      List<ResourcePerformanceStatisticsDTO> userStatistics = new ArrayList<ResourcePerformanceStatisticsDTO>();
      List<ProcessingTimePerProcess> tableData = new ArrayList<ProcessingTimePerProcess>();
      Iterator<ProcessDefinition> pIter = ProcessDefinitionUtils.getAllBusinessRelevantProcesses().iterator();
      ProcessingTimePerProcess ptp = null;
      Set<String> participants = new HashSet<String>();

      Map<String, DateRange> customColumnDateRange = CollectionUtils.newHashMap();
      Map<String, ColumnDefinitionDTO> columnDefMap = CollectionUtils.newHashMap();
      addCustomColsFromPreference(PreferenceScope.USER, customColumnDateRange, columnDefMap);
      addCustomColsFromPreference(PreferenceScope.PARTITION, customColumnDateRange, columnDefMap);

      ModelParticipant participant = (ModelParticipant) ModelCache.findModelCache().getParticipant(roleId, null);
      if (participant != null)
      {
         participants.add(participant.getId());
      }
      ProcessDefinition caseProcess = ProcessDefinitionUtils.getProcessDefinition(PredefinedConstants.CASE_PROCESS_ID);
      while (pIter.hasNext())
      {
         ProcessDefinition pd = pIter.next();
         // Filter the PDs based on ROLE, Case Process will be always visible
         if (ProcessDefinitionUtils.hasProcessPerformingActivity(pd, participants)
               || (pd.getQualifiedId().equals(caseProcess.getQualifiedId())))
         {
            ptp = new ProcessingTimePerProcess(pd, columnDefMap);
            tableData.add(ptp);
         }
      }

      UserWorktimeStatistics stat = getWorktimeStatistics(customColumnDateRange);

      if (stat != null && participant != null)
      {
         Set<Long> ids = stat.getAvailableUserOids();
         for (Long userOID : ids)
         {

            WorktimeStatistics wStat = stat.getWorktimeStatistics(userOID);
            if (wStat != null)
            {
               for (ProcessingTimePerProcess cpp : tableData)
               {
                  Contribution con = wStat.findContribution(cpp.getProcessDefinition().getQualifiedId(), participant);
                  cpp.addContribution(con, customColumnDateRange);

               }
            }
         }
      }
      for (ProcessingTimePerProcess cpp : tableData)
      {
         Map<String, ProcessingTimeDTO> statisticsByColumns = CollectionUtils.newHashMap();
         statisticsByColumns.put("Today",
               new ProcessingTimeDTO(cpp.getAverageTimeToday(), cpp.getAverageWaitingTimeToday(), cpp.getTodayState()));

         statisticsByColumns.put(
               "Last Week",
               new ProcessingTimeDTO(cpp.getAverageTimeLastWeek(), cpp.getAverageWaitingTimeLastWeek(), cpp
                     .getLastWeekState()));

         statisticsByColumns.put(
               "Last Month",
               new ProcessingTimeDTO(cpp.getAverageTimeLastMonth(), cpp.getAverageWaitingTimeLastMonth(), cpp
                     .getLastMonthState()));
         Set<String> colsSet = columnDefMap.keySet();

         Map<String, Object> customColumns = cpp.getCustomColumns();

         for (String colKey : colsSet)
         {

            double dTimeValue = (Double) customColumns.get(colKey + CustomColumnUtils.CUSTOM_COL_TIME_SUFFIX);

            String timeValue = DateUtils.formatDurationInHumanReadableFormat((long) dTimeValue);

            double dWaitTime = (Double) customColumns.get(colKey + CustomColumnUtils.CUSTOM_COL_WAIT_TIME_SUFFIX);

            String waitTime = DateUtils.formatDurationInHumanReadableFormat((long) dWaitTime);

            int thresholdValue = (Integer) customColumns.get(colKey + CustomColumnUtils.CUSTOM_COL_STATUS_SUFFIX);

            statisticsByColumns.put(columnDefMap.get(colKey).columnTitle, new ProcessingTimeDTO(timeValue, waitTime,
                  thresholdValue));

         }

         ResourcePerformanceStatisticsDTO resourcePerformanceStatisticsDTO = new ResourcePerformanceStatisticsDTO(
               I18nUtils.getProcessName(cpp.getProcessDefinition()), statisticsByColumns);
         userStatistics.add(resourcePerformanceStatisticsDTO);
      }

      columnDefMap.put("Today", new ColumnDefinitionDTO("Today", PreferenceScope.DEFAULT.toString()));
      columnDefMap.put("Week", new ColumnDefinitionDTO("Last Week", PreferenceScope.DEFAULT.toString()));
      columnDefMap.put("Month", new ColumnDefinitionDTO("Last Month", PreferenceScope.DEFAULT.toString()));

      if (CollectionUtils.isEmpty(userStatistics))
      {

         Map<String, ProcessingTimeDTO> statisticsByColumns = CollectionUtils.newHashMap();
         /*
          * statisticsByColumns.put("Today", new ProcessingTimeDTO());
          * 
          * statisticsByColumns.put( "Last Week", new ProcessingTimeDTO());
          * 
          * statisticsByColumns.put( "Last Month", new ProcessingTimeDTO());
          */
         Set<String> colsSet = columnDefMap.keySet();

         for (String colKey : colsSet)
         {
            statisticsByColumns.put(columnDefMap.get(colKey).toString(), new ProcessingTimeDTO());
         }

         ResourcePerformanceStatisticsDTO resourcePerformanceStatisticsDTO = new ResourcePerformanceStatisticsDTO(
               I18nUtils.getProcessName(null), statisticsByColumns);
         userStatistics.add(resourcePerformanceStatisticsDTO);

      }

      ResourcePerformanceQueryResultDTO queryResult = new ResourcePerformanceQueryResultDTO();
      queryResult.columns = columnDefMap.keySet();
      queryResult.columnsDefinition = columnDefMap;
      queryResult.list = userStatistics;
      queryResult.totalCount = userStatistics.size();
      return queryResult;
   }

   public UserWorktimeStatistics getWorktimeStatistics(Map<String, DateRange> customColumnDateRange)
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserWorktimeStatisticsQuery wsQuery = UserWorktimeStatisticsQuery.forAllUsers();
      wsQuery.setPolicy(CriticalProcessingTimePolicy.criticalityByDuration(BusinessControlCenterConstants
            .getProcessingTimeThreshold(BusinessControlCenterConstants.YELLOW_THRESHOLD, 1.0f),
            BusinessControlCenterConstants.getProcessingTimeThreshold(BusinessControlCenterConstants.RED_THRESHOLD,
                  1.0f)));

      List<DateRange> dateRange = CollectionUtils.newArrayList();
      dateRange.add(DateRange.TODAY);
      dateRange.add(DateRange.LAST_WEEK);
      dateRange.add(DateRange.LAST_MONTH);

      if (!CollectionUtils.isEmpty(customColumnDateRange))
      {
         for (String columnId : customColumnDateRange.keySet())
         {
            DateRange range = customColumnDateRange.get(columnId);
            if (null != range)
            {
               dateRange.add(range);
            }
         }
      }

      if (!dateRange.isEmpty())
      {
         wsQuery.setPolicy(new StatisticsDateRangePolicy(dateRange));
      }
      UserWorktimeStatistics stat = (UserWorktimeStatistics) facade.getAllUsers(wsQuery);
      return stat;
   }

   private List<String> getCustomColumnsPreference(UserPreferencesHelper userPreferenceHelper)
   {
      return userPreferenceHelper.getString(UserPreferencesEntries.V_RESOURCE_PERFORMANCE,
            UserPreferencesEntries.V_CUSTOM_AllCOLUMNS);
   }

   private void addCustomColsFromPreference(PreferenceScope prefScope, Map<String, DateRange> customColumnDateRange,
         Map<String, ColumnDefinitionDTO> columnDefMap)
   {

      AdministrationService adminService = serviceFactoryUtils.getAdministrationService();
      Preferences preferences = adminService.getPreferences(prefScope, "ipp-business-control-center", "preference");
      Serializable obj = preferences.getPreferences().get("ipp-business-control-center.ResourcePerformance.allColumns");
      JsonParser jsonParser = new JsonParser();
      try
      {
         JsonArray jsonArray = (JsonArray) jsonParser.parse(obj.toString());
         for (int i = 0; i < jsonArray.size(); i++)
         {
            JsonObject columnDefinition = jsonArray.get(i).getAsJsonObject();
            updateColDefAndcustomColumnDateRange(columnDefinition, customColumnDateRange, columnDefMap, prefScope);
         }
      }
      catch (Exception e)
      {
         org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope prefScopeForCustColumn = org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope.PARTITION;
         if (prefScope.equals(PreferenceScope.USER))
         {
            prefScopeForCustColumn = org.eclipse.stardust.ui.web.common.spi.preference.PreferenceScope.USER;
         }
         UserPreferencesHelper prefHelper = UserPreferencesHelper.getInstance(UserPreferencesEntries.M_BCC,
               prefScopeForCustColumn);
         List<String> allCols = getCustomColumnsPreference(prefHelper);
         if (!CollectionUtils.isEmpty(allCols))
         {
            for (String col : allCols)
            {
               if (col.contains("#{"))
               {
                  String[] columnDef = col.split("#");
                  JsonObject columnDefinition = GsonUtils.readJsonObject(columnDef[1]);
                  updateColDefAndcustomColumnDateRange(columnDefinition, customColumnDateRange, columnDefMap, prefScope);

               }
            }
         }
      }
   }

   private void updateColDefAndcustomColumnDateRange(JsonObject columnDefinition,
         Map<String, DateRange> customColumnDateRange, Map<String, ColumnDefinitionDTO> columnDefMap,
         PreferenceScope prefScope)
   {
      String columnId = GsonUtils.extractString(columnDefinition, "columnId");
      // String columnTitle = GsonUtils.extractString(columnDefinition, "columnTitle");
      ColumnDefinitionDTO columnDefDTO = new ColumnDefinitionDTO();
      columnDefDTO.columnTitle = GsonUtils.extractString(columnDefinition, "columnTitle");
      columnDefDTO.prefScope = prefScope.name();
      columnDefMap.put(columnId, columnDefDTO);
      CustomColumnUtils.updateCustomColumnDateRange(columnDefinition, customColumnDateRange);
   }
}
