package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;
import java.util.Map;

public class BenchmarkProcessActivitiesTLVStatisticsResultDTO extends AbstractDTO
{
   public Map<String, List<BenchmarkTLVStatisticsResultDTO>> bATLVStatsMap;

   public List<BenchmarkTLVStatisticsResultDTO> benchmarkTLVProcessStas;
}
