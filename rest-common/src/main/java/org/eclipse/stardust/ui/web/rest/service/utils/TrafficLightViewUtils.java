package org.eclipse.stardust.ui.web.rest.service.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;
import javax.xml.namespace.QName;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjects;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.BusinessObjectQuery.Option;
import org.eclipse.stardust.engine.api.runtime.BusinessObject;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkBusinessObjectStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkCategoryCounts;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkCategoryDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.BenchmarkTLVStatisticsResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

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

      FilterTerm dateFilter = query.getFilter().addOrTerm();
      if (dateType.equals(BUSINESS_DATE))
      {
         for (String processId : setOfprocesses)
         {
            dateFilter.add((DataFilter.between(getModelName(processId) + BUSINESS_DATE, startDate.getTime(),
                  endDate.getTime())));
         }

      }
      else
      {

         query.where(ProcessInstanceQuery.START_TIME.between(startDate.getTimeInMillis(),
               endDate.getTimeInMillis()));
      }

      BenchmarkProcessStatistics stats = (BenchmarkProcessStatistics) serviceFactoryUtils.getQueryService()
            .getAllProcessInstances(query);

      List<BenchmarkTLVStatisticsResultDTO> bPSRDTOList = new ArrayList<BenchmarkTLVStatisticsResultDTO>();

      for (ProcessDefinitionDTO processDef : processes)
      {
         BenchmarkTLVStatisticsResultDTO bPSRDTO = new BenchmarkTLVStatisticsResultDTO();
         bPSRDTO.id = processDef.id;
         bPSRDTO.name = processDef.name;
         bPSRDTO.isActivity = false;
         bPSRDTO.abortedCount = stats.getAbortedCountForProcess(processDef.id);
         bPSRDTO.completedCount = stats.getCompletedCountForProcess(processDef.id);
         bPSRDTO.totalCount = bPSRDTO.abortedCount + bPSRDTO.completedCount;
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
         Integer dayOffset, List<BenchmarkCategoryDTO> benchmarkCategories, Set<String> processActivitySet)
   {
      // Query
      BenchmarkActivityStatisticsQuery query = BenchmarkActivityStatisticsQuery.forProcessId(processId);

      // Only for the selected benchmarks.
      FilterOrTerm benchmarkFilter = query.getFilter().addOrTerm();

      for (Long bOid : bOids)
      {
         benchmarkFilter.add(BenchmarkActivityStatisticsQuery.BENCHMARK_OID.isEqual(bOid));
      }
      
      FilterTerm activityFilter = query.getFilter().addOrTerm();
      for(String activityId : processActivitySet) {
         activityFilter.add(ActivityFilter.forProcess(activityId, processId));
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
         query.where((DataFilter.between(getModelName(processId) + BUSINESS_DATE, startDate.getTime(),
               endDate.getTime())));
      }
      else
      {
         query.where(ActivityInstanceQuery.START_TIME.between(startDate.getTimeInMillis(),
               endDate.getTimeInMillis()));
      }

      BenchmarkActivityStatistics stats = (BenchmarkActivityStatistics) serviceFactoryUtils.getQueryService()
            .getAllActivityInstances(query);

      List<BenchmarkTLVStatisticsResultDTO> bASRDTOList = new ArrayList<BenchmarkTLVStatisticsResultDTO>();

      for (String activityId : processActivitySet)
      {
         BenchmarkTLVStatisticsResultDTO bASRDTO = new BenchmarkTLVStatisticsResultDTO();
         bASRDTO.id = activityId;
         bASRDTO.parentId = processId;
         bASRDTO.name = getActivityOrProcessName(activityId);
         bASRDTO.isActivity = true;
         
         bASRDTO.abortedCount = stats.getAbortedCountForActivity(processId, activityId);
         bASRDTO.completedCount = stats.getCompletedCountForActivity(processId, activityId);
         bASRDTO.totalCount = bASRDTO.completedCount + bASRDTO.abortedCount;
         BenchmarkCategoryCounts benchmarkCategoryCounts = stats.getBenchmarkCategoryCountsForActivity(processId,
               activityId);
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

   public QueryResultDTO getTrafficLightViewStatasticByBO(Boolean isAllBenchmarks, Boolean isAllProcessess,
         List<Long> bOids, List<ProcessDefinitionDTO> processes, String dateType, Integer dayOffset,
         List<BenchmarkCategoryDTO> benchmarkCategories, String businessObjectQualifiedId,
         Set<String> selBOInstances, String groupBybusinessQualifiedId, Set<String> selGroupByBOInstances)
   {
      Set<ProcessDefinition> processDefns = new HashSet<ProcessDefinition>();
      if(!isAllProcessess){
         for(ProcessDefinitionDTO process : processes){
            ProcessDefinition benchmarkProcess = serviceFactoryUtils.getQueryService()
                  .getProcessDefinition(process.id);
            processDefns.add(benchmarkProcess);
         }
         
         BusinessObjectQuery businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
               businessObjectQualifiedId);
         businessObjectQuery.setPolicy(new BusinessObjectQuery.Policy(Option.WITH_DESCRIPTION));

         BusinessObjects bos = serviceFactoryUtils.getQueryService().getAllBusinessObjects(businessObjectQuery);
         BusinessObject baseData = bos.get(0);
         
        Set<Serializable> selBOInstancesSet = Collections.<Serializable>emptySet();
         
         if(selBOInstances != null){
           selBOInstancesSet = new HashSet<Serializable>((Collection< ? extends Serializable>) selBOInstances);
         }
         
         BusinessObject baseGroupBy = null;
         Set<Serializable> selGroupByBOInstancesSet  = null;
         if(groupBybusinessQualifiedId != null){
            businessObjectQuery = BusinessObjectQuery.findForBusinessObject(
                  groupBybusinessQualifiedId);
            
            bos = serviceFactoryUtils.getQueryService().getAllBusinessObjects(businessObjectQuery);
            baseGroupBy = bos.get(0);
            
            if(selGroupByBOInstances != null){
               selGroupByBOInstancesSet = new HashSet<Serializable>((Collection< ? extends Serializable>) selGroupByBOInstances);
            }
         
         }
               
         BenchmarkProcessStatisticsQuery query = BenchmarkProcessStatisticsQuery
               .forProcessesAndBusinessObject(processDefns,
                     baseData, selBOInstancesSet,
                     baseGroupBy, selGroupByBOInstancesSet);
         
         
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
            for (ProcessDefinitionDTO process : processes)
            {
               query.where((DataFilter.between(getModelName(process.id) + BUSINESS_DATE,startDate.getTime(),
                     endDate.getTime())));
            }

         }
         else
         {

            query.where(ProcessInstanceQuery.START_TIME.between(startDate.getTimeInMillis(),
                  endDate.getTimeInMillis()));
         }
                  
         BenchmarkBusinessObjectStatistics stats = (BenchmarkBusinessObjectStatistics) serviceFactoryUtils
               .getQueryService().getAllProcessInstances(query);
         
         System.out.println(stats);
         
      }
      // TODO Auto-generated method stub
      return null;
   }
}
