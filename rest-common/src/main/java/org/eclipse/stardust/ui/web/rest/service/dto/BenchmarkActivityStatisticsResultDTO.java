package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Map;

public class BenchmarkActivityStatisticsResultDTO
{

   public String activityId;
   public long abortedActivityCount;
   public long completedActivityCount;
   public long totalCount;
   public Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMap;
   public String activityName;


}
