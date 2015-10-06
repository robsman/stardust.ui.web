package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery.Option;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkBusinessObjectStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkCategoryCounts;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkCategoryDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkProcessActivitiesTLVStatisticsResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkTLVStatisticsByBOResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkTLVStatisticsResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BusinessObjectStatisticDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.springframework.stereotype.Component;

@Component
public class TrafficLightViewUtils
{

   @Resource
   ServiceFactoryUtils serviceFactoryUtils;
   
   public static final String TOTAL_PROCESS = "Total Process";
   
   public static final String TOTAL_ACTIVITY = "Total Activity";

   /**
    * 
    * @param isAllBenchmarks
    * @param isAllProcessess
    * @param bOids
    * @param processes
    * @param dateType
    * @param dayOffset
    * @param benchmarkCategories
    * @param processActivitiesMap
    * @return
    */
   public BenchmarkProcessActivitiesTLVStatisticsResultDTO getTrafficLightViewStatastic(Boolean isAllBenchmarks,
         Boolean isAllProcessess, List<Long> bOids, List<ProcessDefinitionDTO> processes, String dateType,
         Integer dayOffset, List<BenchmarkCategoryDTO> benchmarkCategories,
         Map<String, List<String>> processActivitiesMap)
   {
      Set<String> setOfprocesses = new TreeSet<String>();
      for (ProcessDefinitionDTO processDef : processes)
      {
         setOfprocesses.add(processDef.id);

      }
      BenchmarkProcessStatisticsQuery query;
      if (isAllProcessess)
      {
         query = BenchmarkProcessStatisticsQuery.forAllProcesses();
      }
      else
      {
         query = BenchmarkProcessStatisticsQuery.forProcessIds(setOfprocesses);
      }

      FilterOrTerm benchmarkFilter = query.getFilter().addOrTerm();

      for (Long bOid : bOids)
      {
         benchmarkFilter.add(BenchmarkProcessStatisticsQuery.BENCHMARK_OID.isEqual(bOid));
      }

      Calendar startDate = getCurrentDayStart();
      Calendar endDate = getCurrentDayEnd();

      if (dayOffset > 0)
      {
         endDate = getfutureEndDate(dayOffset);
      }
      else if (dayOffset < 0)
      {
         startDate = getPastStartDate(dayOffset);
      }

      FilterTerm dateFilter = query.getFilter().addOrTerm();
      if (dateType.equals(PredefinedConstants.BUSINESS_DATE))
      {
         for (String processId : setOfprocesses)
         {
            dateFilter.add((DataFilter.between(getModelName(processId) + PredefinedConstants.BUSINESS_DATE,
                  startDate.getTime(), endDate.getTime())));
         }

      }
      else
      {

         query.where(ProcessInstanceQuery.START_TIME.between(startDate.getTimeInMillis(), endDate.getTimeInMillis()));
      }

      BenchmarkProcessStatistics stats = (BenchmarkProcessStatistics) serviceFactoryUtils.getQueryService()
            .getAllProcessInstances(query);

      List<BenchmarkTLVStatisticsResultDTO> bPSRDTOList = new ArrayList<BenchmarkTLVStatisticsResultDTO>();

      // Created Object for Total Process Row
      BenchmarkTLVStatisticsResultDTO processStatsTotal = new BenchmarkTLVStatisticsResultDTO();
      processStatsTotal.name = TOTAL_PROCESS;
      processStatsTotal.benchmarkCategoryCountMap = CollectionUtils.newMap();
      processStatsTotal.isActivity = false;

      for (ProcessDefinitionDTO processDef : processes)
      {
         BenchmarkTLVStatisticsResultDTO bPSRDTO = new BenchmarkTLVStatisticsResultDTO();
         bPSRDTO.id = processDef.id;
         bPSRDTO.name = processDef.name;
         bPSRDTO.isActivity = false;

         bPSRDTO.abortedCount = stats.getAbortedCountForProcess(processDef.id);
         processStatsTotal.abortedCount = processStatsTotal.abortedCount + bPSRDTO.abortedCount;

         bPSRDTO.completedCount = stats.getCompletedCountForProcess(processDef.id);
         processStatsTotal.completedCount = processStatsTotal.completedCount + bPSRDTO.completedCount;

         bPSRDTO.totalCount = bPSRDTO.abortedCount + bPSRDTO.completedCount;
         processStatsTotal.totalCount = processStatsTotal.totalCount + bPSRDTO.totalCount;

         BenchmarkCategoryCounts benchmarkCategoryCounts = stats.getBenchmarkCategoryCountsForProcess(processDef.id);
         // this method will populate the counts of benchmark category for the process.
         populateBenchmarkCategoryResult(benchmarkCategories, bPSRDTO, benchmarkCategoryCounts, processStatsTotal);
         
         bPSRDTOList.add(bPSRDTO);
      }
      // map will store activity stats for each process selected
      Map<String, List<BenchmarkTLVStatisticsResultDTO>> bASRDTOMap = new HashMap<String, List<BenchmarkTLVStatisticsResultDTO>>();

      // Created object for total activity row.
      BenchmarkTLVStatisticsResultDTO activityStatsTotal = new BenchmarkTLVStatisticsResultDTO();
      activityStatsTotal.name = TOTAL_ACTIVITY;
      activityStatsTotal.benchmarkCategoryCountMap = CollectionUtils.newMap();
      activityStatsTotal.isActivity = true;

      // Calling activity statistics
      getActivityBenchmarkStatistics(processes, bOids, dateType, dayOffset, benchmarkCategories, processActivitiesMap,
            bASRDTOMap, activityStatsTotal);

      List<BenchmarkTLVStatisticsResultDTO> benchmarkTLVProcessStas = new ArrayList<BenchmarkTLVStatisticsResultDTO>();
      benchmarkTLVProcessStas.add(0, processStatsTotal); // setting total process row at index 0
      benchmarkTLVProcessStas.add(1, activityStatsTotal);// setting total activity row at index 1
      benchmarkTLVProcessStas.addAll(2, bPSRDTOList);
      BenchmarkProcessActivitiesTLVStatisticsResultDTO result = new BenchmarkProcessActivitiesTLVStatisticsResultDTO();
      result.benchmarkTLVProcessStas = benchmarkTLVProcessStas;
      result.bATLVStatsMap = bASRDTOMap;
      return result;
   }
   
   /**
    * 
    * @param benchmarkCategories
    * @param bPSRDTO
    * @param benchmarkCategoryCounts
    * @param statsTotal
    */
   private void populateBenchmarkCategoryResult(List<BenchmarkCategoryDTO> benchmarkCategories,
         BenchmarkTLVStatisticsResultDTO bPSRDTO, BenchmarkCategoryCounts benchmarkCategoryCounts,
         BenchmarkTLVStatisticsResultDTO statsTotal)
   {
      bPSRDTO.benchmarkCategoryCountMap = CollectionUtils.newMap();
      if (null != benchmarkCategoryCounts)
      {
         for (BenchmarkCategoryDTO bCategory : benchmarkCategories)
         {
            BenchmarkCategoryDTO benchmarkCategory = new BenchmarkCategoryDTO();
            benchmarkCategory.color = bCategory.color;
            benchmarkCategory.name = bCategory.name;
            benchmarkCategory.index = bCategory.index;
            benchmarkCategory.count = benchmarkCategoryCounts.getBenchmarkCategoryCount().get(benchmarkCategory.index) != null
                  ? benchmarkCategoryCounts.getBenchmarkCategoryCount().get(benchmarkCategory.index)
                  : 0;
            bPSRDTO.totalCount = bPSRDTO.totalCount + benchmarkCategory.count;
            bPSRDTO.benchmarkCategoryCountMap.put(benchmarkCategory.name, benchmarkCategory);

            Map<String, BenchmarkCategoryDTO> totalBenchmarkCategoryMap = statsTotal.benchmarkCategoryCountMap;
            if (totalBenchmarkCategoryMap.get(benchmarkCategory.name) != null)
            {
               BenchmarkCategoryDTO totalBenchmarkCategory = totalBenchmarkCategoryMap.get(benchmarkCategory.name);
               totalBenchmarkCategory.count = totalBenchmarkCategory.count + benchmarkCategory.count;
               statsTotal.benchmarkCategoryCountMap.put(benchmarkCategory.name, totalBenchmarkCategory);
               statsTotal.totalCount = statsTotal.totalCount + benchmarkCategory.count;
            }
            else
            {
               statsTotal.benchmarkCategoryCountMap.put(benchmarkCategory.name, benchmarkCategory);
               statsTotal.totalCount = statsTotal.totalCount + benchmarkCategory.count;
            }

         }
      }
      else
      {
         for (BenchmarkCategoryDTO bCategory : benchmarkCategories)
         {
            BenchmarkCategoryDTO benchmarkCategory = new BenchmarkCategoryDTO();
            benchmarkCategory.color = bCategory.color;
            benchmarkCategory.name = bCategory.name;
            benchmarkCategory.index = bCategory.index;
            benchmarkCategory.count = 0L;
            bPSRDTO.benchmarkCategoryCountMap.put(benchmarkCategory.name, benchmarkCategory);
            Map<String, BenchmarkCategoryDTO> totalBenchmarkCategoryMap = statsTotal.benchmarkCategoryCountMap;
            if (totalBenchmarkCategoryMap.get(benchmarkCategory.name) != null)
            {
               BenchmarkCategoryDTO totalBenchmarkCategory = totalBenchmarkCategoryMap.get(benchmarkCategory.name);
               totalBenchmarkCategory.count = totalBenchmarkCategory.count + benchmarkCategory.count;
               statsTotal.benchmarkCategoryCountMap.put(benchmarkCategory.name, totalBenchmarkCategory);
               statsTotal.totalCount = statsTotal.totalCount + benchmarkCategory.count;
            }
            else
            {
               statsTotal.benchmarkCategoryCountMap.put(benchmarkCategory.name, benchmarkCategory);
               statsTotal.totalCount = statsTotal.totalCount + benchmarkCategory.count;
            }
         }
      }
   }
   /**
    * 
    * @param processes
    * @param bOids
    * @param dateType
    * @param dayOffset
    * @param benchmarkCategories
    * @param processActivitiesMap
    * @param bASRDTOMap
    * @param activityStatsTotal
    */
   private void getActivityBenchmarkStatistics(List<ProcessDefinitionDTO> processes, List<Long> bOids, String dateType,
         Integer dayOffset, List<BenchmarkCategoryDTO> benchmarkCategories,
         Map<String, List<String>> processActivitiesMap, Map<String, List<BenchmarkTLVStatisticsResultDTO>> bASRDTOMap,
         BenchmarkTLVStatisticsResultDTO activityStatsTotal)
   {
      Set<String> setOfprocesses = new TreeSet<String>();
      for (ProcessDefinitionDTO processDef : processes)
      {
         setOfprocesses.add(processDef.id);

      }
      // Query
      BenchmarkActivityStatisticsQuery query = BenchmarkActivityStatisticsQuery.forProcessIds(setOfprocesses);

      // Only for the selected benchmarks.
      FilterOrTerm benchmarkFilter = query.getFilter().addOrTerm();

      for (Long bOid : bOids)
      {
         benchmarkFilter.add(BenchmarkActivityStatisticsQuery.BENCHMARK_OID.isEqual(bOid));
      }

      FilterTerm activityFilter = query.getFilter().addOrTerm();

      for (String process : processActivitiesMap.keySet())
      {
         for (String activityId : processActivitiesMap.get(process))
         {
            activityFilter.add(ActivityFilter.forProcess(activityId, process));
         }
      }

      // Only for date.

      Calendar startDate = getCurrentDayStart();
      Calendar endDate = getCurrentDayEnd();

      if (dayOffset > 0)
      {
         endDate = getfutureEndDate(dayOffset);
      }
      else if (dayOffset < 0)
      {
         startDate = getPastStartDate(dayOffset);
      }

      FilterTerm dateFilter = query.getFilter().addOrTerm();
      if (dateType.equals(PredefinedConstants.BUSINESS_DATE))
      {
         for (String processId : setOfprocesses)
         {
            dateFilter.add((DataFilter.between(getModelName(processId) + PredefinedConstants.BUSINESS_DATE,
                  startDate.getTime(), endDate.getTime())));
         }

      }
      else
      {

         query.where(ActivityInstanceQuery.START_TIME.between(startDate.getTimeInMillis(), endDate.getTimeInMillis()));
      }

      BenchmarkActivityStatistics stats = (BenchmarkActivityStatistics) serviceFactoryUtils.getQueryService()
            .getAllActivityInstances(query);

      for (String processId : processActivitiesMap.keySet())
      {
         List<BenchmarkTLVStatisticsResultDTO> bASRDTOList = new ArrayList<BenchmarkTLVStatisticsResultDTO>();
         for (String activityId : processActivitiesMap.get(processId))
         {
            BenchmarkTLVStatisticsResultDTO bASRDTO = new BenchmarkTLVStatisticsResultDTO();
            bASRDTO.id = activityId;
            bASRDTO.parentId = processId;
            bASRDTO.name = getActivityOrProcessName(activityId);
            bASRDTO.isActivity = true;

            bASRDTO.abortedCount = stats.getAbortedCountForActivity(processId, activityId);
            activityStatsTotal.abortedCount = activityStatsTotal.abortedCount + bASRDTO.abortedCount;

            bASRDTO.completedCount = stats.getCompletedCountForActivity(processId, activityId);
            activityStatsTotal.completedCount = activityStatsTotal.completedCount + bASRDTO.completedCount;

            bASRDTO.totalCount = bASRDTO.completedCount + bASRDTO.abortedCount;
            activityStatsTotal.totalCount = activityStatsTotal.totalCount + bASRDTO.totalCount;

            BenchmarkCategoryCounts benchmarkCategoryCounts = stats.getBenchmarkCategoryCountsForActivity(processId,
                  activityId);
            populateBenchmarkCategoryResult(benchmarkCategories, bASRDTO, benchmarkCategoryCounts, activityStatsTotal);
            bASRDTOList.add(bASRDTO);
         }
         bASRDTOMap.put(processId, bASRDTOList);

      }
   }

   public static Calendar getPastStartDate(Integer dayOffset)
   {
      Calendar now = Calendar.getInstance();
      now.add(Calendar.DATE, dayOffset);
      now.set(Calendar.HOUR_OF_DAY, 0);
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.MILLISECOND, 0);

      return now;
   }

   public static Calendar getfutureEndDate(Integer dayOffset)
   {
      Calendar now = Calendar.getInstance();
      now.add(Calendar.DATE, dayOffset);
      now.set(Calendar.HOUR_OF_DAY, 23);
      now.set(Calendar.MINUTE, 59);
      now.set(Calendar.MILLISECOND, 0);

      return now;
   }

   public static String getModelName(String qualifiedProcessId)
   {
      String modelName = null;
      if (qualifiedProcessId.indexOf("{") != -1)
      {
         int lastIndex = qualifiedProcessId.lastIndexOf("}");
         modelName = qualifiedProcessId.substring(0, lastIndex + 1);
      }
      return modelName;
   }

   public static String getActivityOrProcessName(String qualifiedId)
   {
      String name = null;
      if (qualifiedId.indexOf("{") != -1)
      {
         int lastIndex = qualifiedId.lastIndexOf("}");
         name = qualifiedId.substring(lastIndex + 1);
      }
      return name;
   }

   public static Calendar getCurrentDayEnd()
   {
      Calendar now = Calendar.getInstance();

      now.set(Calendar.HOUR_OF_DAY, 23);
      now.set(Calendar.MINUTE, 59);
      now.set(Calendar.MILLISECOND, 0);

      return now;
   }

   public static Calendar getCurrentDayStart()
   {
      Calendar now = Calendar.getInstance();

      now.set(Calendar.HOUR_OF_DAY, 0);
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.MILLISECOND, 0);

      return now;
   }
   /**
    * 
    * @param isAllBenchmarks
    * @param isAllProcessess
    * @param bOids
    * @param processes
    * @param dateType
    * @param dayOffset
    * @param benchmarkCategories
    * @param businessObjectQualifiedId
    * @param selBOInstances
    * @param groupBybusinessQualifiedId
    * @param selGroupByBOInstances
    * @return
    */
   public BenchmarkTLVStatisticsByBOResultDTO getTrafficLightViewStatasticByBO(Boolean isAllBenchmarks,
         Boolean isAllProcessess, List<Long> bOids, List<ProcessDefinitionDTO> processes, String dateType,
         Integer dayOffset, List<BenchmarkCategoryDTO> benchmarkCategories, String businessObjectQualifiedId,
         Set<String> selBOInstances, String groupBybusinessQualifiedId, Set<String> selGroupByBOInstances)
   {
      Set<ProcessDefinition> processDefns = new HashSet<ProcessDefinition>();
      for (ProcessDefinitionDTO process : processes)
      {
         ProcessDefinition benchmarkProcess = serviceFactoryUtils.getQueryService().getProcessDefinition(process.id);
         processDefns.add(benchmarkProcess);
      }

      BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(businessObjectQualifiedId);
      businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

      BusinessObjects bos = serviceFactoryUtils.getQueryService().getAllBusinessObjects(businessObjectQuery);
      BusinessObject baseData = bos.get(0);

      Set<Serializable> selBOInstancesSet = Collections.<Serializable> emptySet();

      if (selBOInstances != null)
      {
         selBOInstancesSet = new HashSet<Serializable>((Collection< ? extends Serializable>) selBOInstances);
      }

      BusinessObject baseGroupBy = null;
      Set<Serializable> selGroupByBOInstancesSet = null;
      if (groupBybusinessQualifiedId != null)
      {
         businessObjectQuery = BusinessObjectQuery.findForBusinessObject(groupBybusinessQualifiedId);

         bos = serviceFactoryUtils.getQueryService().getAllBusinessObjects(businessObjectQuery);
         baseGroupBy = bos.get(0);

         if (selGroupByBOInstances != null)
         {
            selGroupByBOInstancesSet = new HashSet<Serializable>(
                  (Collection< ? extends Serializable>) selGroupByBOInstances);
         }

      }

      BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery.forProcessesAndBusinessObject(
            processDefns, baseData, selBOInstancesSet, baseGroupBy, selGroupByBOInstancesSet);

      FilterOrTerm benchmarkFilter = query.getFilter().addOrTerm();

      for (Long bOid : bOids)
      {
         benchmarkFilter.add(BenchmarkProcessStatisticsQuery.BENCHMARK_OID.isEqual(bOid));
      }

      Calendar startDate = getCurrentDayStart();
      Calendar endDate = getCurrentDayEnd();

      if (dayOffset > 0)
      {
         endDate = getfutureEndDate(dayOffset);
      }
      else if (dayOffset < 0)
      {
         startDate = getPastStartDate(dayOffset);
      }

      FilterOrTerm businessDateFilter = query.getFilter().addOrTerm();

      if (dateType.equals(PredefinedConstants.BUSINESS_DATE))
      {
         for (ProcessDefinitionDTO process : processes)
         {
            businessDateFilter.add((DataFilter.between(getModelName(process.id) + PredefinedConstants.BUSINESS_DATE,
                  startDate.getTime(), endDate.getTime())));
         }

      }
      else
      {

         query.where(ProcessInstanceQuery.START_TIME.between(startDate.getTimeInMillis(), endDate.getTimeInMillis()));
      }

      BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactoryUtils
            .getQueryService().getAllProcessInstances(query);
      BenchmarkTLVStatisticsByBOResultDTO bTLVStatsByBOResultDTO = new BenchmarkTLVStatisticsByBOResultDTO();
      
      BusinessObjectStatisticDTO totalBOSDTO = getTotalForBOProcessStatistic(benchmarkCategories,stats);
      
      Set<String> groupByValues = stats.getGroupByValues();
      if (!(groupByValues.size() == 1 && groupByValues.contains(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE)))
      {
         List<BusinessObjectStatisticDTO> businessObjectsResultList = new ArrayList<BusinessObjectStatisticDTO>();
         for (String groupByName : groupByValues)
         {
            if (!groupByName.equals(BenchmarkBusinessObjectStatistics.NO_GROUPBY_VALUE))
            {
               BusinessObjectStatisticDTO boGroupLevel = new BusinessObjectStatisticDTO();
               boGroupLevel.name = groupByName;
               boGroupLevel.isGroup = true;
               
               boGroupLevel.abortedCount = stats.getAbortedCount(groupByName, null);
               boGroupLevel.abortedInstanceOids = stats.getAbortedInstanceOIDs(groupByName, null);
               
               boGroupLevel.completedCount = stats.getCompletedCount(groupByName, null);
               boGroupLevel.completedInstanceOids = stats.getCompletedInstanceOIDs(groupByName, null);
                     
               boGroupLevel.totalCount = boGroupLevel.abortedCount + boGroupLevel.completedCount;
               Set<Long> totalInstanceOids = new HashSet<Long>();
               totalInstanceOids.addAll(boGroupLevel.abortedInstanceOids);
               totalInstanceOids.addAll(boGroupLevel.completedInstanceOids);
               
               
               Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMap = populateBenchmarkCategoryForBusinessObject(benchmarkCategories, stats, groupByName, null, boGroupLevel, totalInstanceOids);
               
               boGroupLevel.totalInstanceOids = totalInstanceOids;
               boGroupLevel.benchmarkCategoryCountMap = benchmarkCategoryCountMap;
               businessObjectsResultList.add(boGroupLevel);

               Set<String> filterValues = stats.getFilterValues(groupByName);
               if (!filterValues.isEmpty())
               {
                  Map<String, List<BusinessObjectStatisticDTO>> businessObjectsForGroupByMap = new HashMap<String, List<BusinessObjectStatisticDTO>>();
                  List<BusinessObjectStatisticDTO> boFilterLevelList = new ArrayList<BusinessObjectStatisticDTO>();
                  
                  populateFilterValuesCount(benchmarkCategories, stats, boFilterLevelList, groupByName, filterValues);
                  
                  businessObjectsForGroupByMap.put(groupByName, boFilterLevelList);
                  bTLVStatsByBOResultDTO.businessObjectsForGroupByMap = businessObjectsForGroupByMap;
               }
            }
            else
            {

               Set<String> filterValues = stats.getFilterValues(null);
               if (!filterValues.isEmpty())
               {
                  populateFilterValuesCount(benchmarkCategories, stats, businessObjectsResultList, null,
                        filterValues);
               }
            }
            
         }
         businessObjectsResultList.add(0, totalBOSDTO);
         bTLVStatsByBOResultDTO.businessObjectsResultList = businessObjectsResultList; 
      }
      else
      {
         Set<String> filterValues = stats.getFilterValues(null);
         if (!filterValues.isEmpty())
         {
            List<BusinessObjectStatisticDTO> boFilterLevelList = new ArrayList<BusinessObjectStatisticDTO>();
            
            populateFilterValuesCount(benchmarkCategories, stats, boFilterLevelList, null,
                  filterValues);
            
            boFilterLevelList.add(0, totalBOSDTO);
            bTLVStatsByBOResultDTO.businessObjectsResultList = boFilterLevelList;
         }
      }
      return bTLVStatsByBOResultDTO;
   }
   /**
    * 
    * @param benchmarkCategories
    * @param stats
    * @param groupByName
    * @param filterValueName
    * @param boFilterLevel
    * @param totalFilterLevelInstanceOids
    * @return
    */
   private Map<String, BenchmarkCategoryDTO> populateBenchmarkCategoryForBusinessObject(
         List<BenchmarkCategoryDTO> benchmarkCategories, BenchmarkBusinessObjectStatistics stats, String groupByName,
         String filterValueName, BusinessObjectStatisticDTO boFilterLevel, Set<Long> totalFilterLevelInstanceOids)
   {
      Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMapFilterLevel = new HashMap<String, BenchmarkCategoryDTO>();
      for (BenchmarkCategoryDTO bCategory : benchmarkCategories)
      {
         BenchmarkCategoryDTO benchmarkCategory = new BenchmarkCategoryDTO();
         benchmarkCategory.color = bCategory.color;
         benchmarkCategory.name = bCategory.name;
         benchmarkCategory.index = bCategory.index;
         benchmarkCategory.count = stats.getBenchmarkCategoryCount(groupByName, filterValueName,
               benchmarkCategory.index);
         benchmarkCategory.instanceOids = stats.getInstanceOIDsForBenchmarkCategory(groupByName, filterValueName, benchmarkCategory.index); 
         boFilterLevel.totalCount = boFilterLevel.totalCount + benchmarkCategory.count;
         benchmarkCategoryCountMapFilterLevel.put(benchmarkCategory.name, benchmarkCategory);
         totalFilterLevelInstanceOids.addAll(benchmarkCategory.instanceOids);
      }
      return benchmarkCategoryCountMapFilterLevel;
   }
   /**
    * 
    * @param benchmarkCategories
    * @param stats
    * @param businessObjectsResultList
    * @param groupByName
    * @param filterValues
    */
   private void populateFilterValuesCount(List<BenchmarkCategoryDTO> benchmarkCategories,
         BenchmarkBusinessObjectStatistics stats, List<BusinessObjectStatisticDTO> businessObjectsResultList,
         String groupByName, Set<String> filterValues)
   {
      for (String filterValueName : filterValues)
      {
         BusinessObjectStatisticDTO boFilterLevel = new BusinessObjectStatisticDTO();
         boFilterLevel.name = filterValueName;
         boFilterLevel.parentId = groupByName;
         boFilterLevel.isGroup = false;
         
         boFilterLevel.abortedCount = stats.getAbortedCount(groupByName, filterValueName);
         boFilterLevel.abortedInstanceOids = stats.getAbortedInstanceOIDs(groupByName, filterValueName);
         
         boFilterLevel.completedCount = stats.getCompletedCount(groupByName, filterValueName);
         boFilterLevel.completedInstanceOids = stats.getCompletedInstanceOIDs(groupByName, filterValueName);
         
         boFilterLevel.totalCount = boFilterLevel.abortedCount + boFilterLevel.completedCount;
         Set<Long> totalFilterLevelInstanceOids = new HashSet<Long>();
         totalFilterLevelInstanceOids.addAll(boFilterLevel.abortedInstanceOids);
         totalFilterLevelInstanceOids.addAll(boFilterLevel.completedInstanceOids);
         
         Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMapFilterLevel = populateBenchmarkCategoryForBusinessObject(
               benchmarkCategories, stats, groupByName, filterValueName, boFilterLevel, totalFilterLevelInstanceOids);
         boFilterLevel.benchmarkCategoryCountMap = benchmarkCategoryCountMapFilterLevel;
         boFilterLevel.totalInstanceOids = totalFilterLevelInstanceOids;
         businessObjectsResultList.add(boFilterLevel);
      }
   }
   /**
    * 
    * @param benchmarkCategories
    * @param stats
    * @return
    */
   private BusinessObjectStatisticDTO getTotalForBOProcessStatistic(List<BenchmarkCategoryDTO> benchmarkCategories, BenchmarkBusinessObjectStatistics stats)
   {
      BusinessObjectStatisticDTO totalBOSDTO = new BusinessObjectStatisticDTO();
      totalBOSDTO.name = "TOTAL";
      totalBOSDTO.isGroup = true;
      
      totalBOSDTO.abortedCount = stats.getAbortedCount(null, null);
      totalBOSDTO.abortedInstanceOids = stats.getAbortedInstanceOIDs(null, null);
      
      totalBOSDTO.completedCount = stats.getCompletedCount(null, null);
      totalBOSDTO.completedInstanceOids = stats.getCompletedInstanceOIDs(null, null);
      
      totalBOSDTO.totalCount = totalBOSDTO.abortedCount  + totalBOSDTO.completedCount;
      Set<Long> totalFilterLevelInstanceOids = new HashSet<Long>();
      totalFilterLevelInstanceOids.addAll(totalBOSDTO.abortedInstanceOids);
      totalFilterLevelInstanceOids.addAll(totalBOSDTO.completedInstanceOids);
      
      Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMapFilterLevel = populateBenchmarkCategoryForBusinessObject(benchmarkCategories, stats, null, null, totalBOSDTO, totalFilterLevelInstanceOids);
      totalBOSDTO.benchmarkCategoryCountMap = benchmarkCategoryCountMapFilterLevel; 
      totalBOSDTO.totalInstanceOids = totalFilterLevelInstanceOids;
      return totalBOSDTO;
   }
}
