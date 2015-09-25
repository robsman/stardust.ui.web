package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Map;

public class BenchmarkTLVStatisticsResultDTO
{

   public String id;
   public String parentId;
   public long abortedCount;
   public long completedCount;
   public long totalCount;
   public Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMap;
   public String name;
   public boolean isActivity;

}
