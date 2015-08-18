package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkCategoryCounts;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkActivityStatisticsResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkCategoryDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkProcessStatisticsResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.springframework.stereotype.Component;

@Component
public class TrafficLightViewUtils
{

   @Resource
   ServiceFactoryUtils serviceFactoryUtils;

   public static final String BUSINESS_DATE = "BUSINESS_DATE";

   public QueryResultDTO getTrafficLightViewStatastic(Boolean isAllBenchmarks, Boolean isAllProcessess,
         List<Long> bOids, List<ProcessDefinitionDTO> processes, String dateType, Integer dayOffset,
         List<BenchmarkCategoryDTO> benchmarkCategories)
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

      if (dateType.equals(BUSINESS_DATE))
      {
         for (String processId : setOfprocesses)
         {
            benchmarkFilter.add((DataFilter.between(getModelName(processId) + BUSINESS_DATE, (Serializable) startDate,
                  (Serializable) endDate)));
         }

      }
      else
      {

         benchmarkFilter.add(ProcessInstanceQuery.START_TIME.between(startDate.getTimeInMillis(),
               endDate.getTimeInMillis()));
      }

      BenchmarkProcessStatistics stats = (BenchmarkProcessStatistics) serviceFactoryUtils.getQueryService()
            .getAllProcessInstances(query);

      List<BenchmarkProcessStatisticsResultDTO> bPSRDTOList = new ArrayList<BenchmarkProcessStatisticsResultDTO>();

      for (ProcessDefinitionDTO processDef : processes)
      {
         BenchmarkProcessStatisticsResultDTO bPSRDTO = new BenchmarkProcessStatisticsResultDTO();
         bPSRDTO.processId = processDef.id;
         bPSRDTO.processName = processDef.name;
         bPSRDTO.abortedProcessCount = stats.getAbortedCountForProcess(processDef.id);
         bPSRDTO.completedProcessCount = stats.getCompletedCountForProcess(processDef.id);
         bPSRDTO.totalCount = bPSRDTO.abortedProcessCount + bPSRDTO.completedProcessCount;
         BenchmarkCategoryCounts benchmarkCategoryCounts = stats.getBenchmarkCategoryCountsForProcess(processDef.id);
         bPSRDTO.benchmarkCategoryCountMap = CollectionUtils.newMap();
         if (null != benchmarkCategoryCounts)
         {
            for (BenchmarkCategoryDTO benchmarkCategory : benchmarkCategories)
            {
               BenchmarkCategoryDTO benCategory = new BenchmarkCategoryDTO();
               benCategory.count = benchmarkCategoryCounts.getBenchmarkCategoryCount().get(benchmarkCategory.index) != null
                     ? benchmarkCategoryCounts.getBenchmarkCategoryCount().get(benchmarkCategory.index)
                     : 0;
               benCategory.color = benchmarkCategory.color;
               bPSRDTO.totalCount = bPSRDTO.totalCount + benCategory.count;
               bPSRDTO.benchmarkCategoryCountMap.put(benchmarkCategory.name, benCategory);
            }
         }
         else
         {
            for (BenchmarkCategoryDTO benchmarkCategory : benchmarkCategories)
            {
               BenchmarkCategoryDTO benCategory = new BenchmarkCategoryDTO();
               benCategory.count = 0L;
               benCategory.color = benchmarkCategory.color;
               bPSRDTO.benchmarkCategoryCountMap.put(benchmarkCategory.name, benCategory);
            }
         }
         bPSRDTOList.add(bPSRDTO);
      }

      QueryResultDTO result = new QueryResultDTO();
      result.list = bPSRDTOList;
      result.totalCount = bPSRDTOList.size();
      return result;
   }

   public QueryResultDTO getActivityBenchmarkStatistics(String processId, List<Long> bOids, String dateType,
         Integer dayOffset, List<BenchmarkCategoryDTO> benchmarkCategories)
   {
      // Query
      BenchmarkActivityStatisticsQuery query = BenchmarkActivityStatisticsQuery.forProcessId(processId);

      // Only for the selected benchmarks.
      FilterOrTerm benchmarkFilter = query.getFilter().addOrTerm();

      for (Long bOid : bOids)
      {
         benchmarkFilter.add(BenchmarkActivityStatisticsQuery.BENCHMARK_OID.isEqual(bOid));
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

      if (dateType.equals(BUSINESS_DATE))
      {
         benchmarkFilter.add((DataFilter.between(getModelName(processId) + BUSINESS_DATE, (Serializable) startDate,
               (Serializable) endDate)));
      }
      else
      {
         benchmarkFilter.add(ActivityInstanceQuery.START_TIME.between(startDate.getTimeInMillis(),
               endDate.getTimeInMillis()));
      }

      BenchmarkActivityStatistics stats = (BenchmarkActivityStatistics) serviceFactoryUtils.getQueryService()
            .getAllActivityInstances(query);

      List<BenchmarkActivityStatisticsResultDTO> bASRDTOList = new ArrayList<BenchmarkActivityStatisticsResultDTO>();

      Set<Pair<String, String>> keySet = stats.getBenchmarkCategoryCounts().keySet();
      for (Pair<String, String> key : keySet)
      {
         BenchmarkActivityStatisticsResultDTO bASRDTO = new BenchmarkActivityStatisticsResultDTO();
         bASRDTO.activityId = key.getSecond();
         // bPSRDTO.activityName = processDef.name;
         
      // first = processId ,second=activityId
         bASRDTO.abortedActivityCount = stats.getAbortedCountForActivity(key.getFirst(), key.getSecond()); 
         bASRDTO.completedActivityCount = stats.getCompletedCountForActivity(key.getFirst(), key.getSecond());
         bASRDTO.totalCount = bASRDTO.completedActivityCount + bASRDTO.abortedActivityCount;
         BenchmarkCategoryCounts benchmarkCategoryCounts = stats.getBenchmarkCategoryCountsForActivity(key.getFirst(),
               key.getSecond());
         bASRDTO.benchmarkCategoryCountMap = CollectionUtils.newMap();
         if (null != benchmarkCategoryCounts)
         {
            for (BenchmarkCategoryDTO benchmarkCategory : benchmarkCategories)
            {
               BenchmarkCategoryDTO benCategory = new BenchmarkCategoryDTO();
               benCategory.count = benchmarkCategoryCounts.getBenchmarkCategoryCount().get(benchmarkCategory.index) != null
                     ? benchmarkCategoryCounts.getBenchmarkCategoryCount().get(benchmarkCategory.index)
                     : 0;
               benCategory.color = benchmarkCategory.color;
               bASRDTO.totalCount = bASRDTO.totalCount + benCategory.count;
               bASRDTO.benchmarkCategoryCountMap.put(benchmarkCategory.name, benCategory);
            }
         }
         else
         {
            for (BenchmarkCategoryDTO benchmarkCategory : benchmarkCategories)
            {
               BenchmarkCategoryDTO benCategory = new BenchmarkCategoryDTO();
               benCategory.count = 0L;
               benCategory.color = benchmarkCategory.color;
               bASRDTO.benchmarkCategoryCountMap.put(benchmarkCategory.name, benCategory);
            }
         }
         bASRDTOList.add(bASRDTO);
      }
      
      QueryResultDTO result = new QueryResultDTO();
      result.list = bASRDTOList;
      result.totalCount = bASRDTOList.size();
      return result;
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
}
