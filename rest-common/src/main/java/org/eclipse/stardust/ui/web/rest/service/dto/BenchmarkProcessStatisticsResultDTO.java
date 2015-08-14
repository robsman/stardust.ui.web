package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Map;

public class BenchmarkProcessStatisticsResultDTO
{

   public String processId;
   public long abortedProcessCount;
   public long completedProcessCount;
   public long totalCount;
   public Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMap;
   public String processName;


}
