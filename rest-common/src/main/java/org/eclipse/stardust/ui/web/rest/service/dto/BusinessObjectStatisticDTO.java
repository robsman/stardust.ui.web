package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.Map;

public class BusinessObjectStatisticDTO
{
   public String name;
   public String parentId;
   public long abortedCount;
   public long completedCount;
   public long totalCount;
   public boolean isGroup;
   public Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMap;
}
