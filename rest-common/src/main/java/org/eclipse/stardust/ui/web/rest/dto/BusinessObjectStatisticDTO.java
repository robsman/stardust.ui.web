package org.eclipse.stardust.ui.web.rest.dto;

import java.util.Map;
import java.util.Set;

public class BusinessObjectStatisticDTO
{
   public String name;
   public String parentId;
   public long abortedCount;
   public Set<Long> abortedInstanceOids;
   public long completedCount;
   public Set<Long> completedInstanceOids;
   public long totalCount;
   public Set<Long> totalInstanceOids;
   public boolean isGroup;
   public Map<String, BenchmarkCategoryDTO> benchmarkCategoryCountMap;
}
