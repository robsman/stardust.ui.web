package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;
import java.util.Map;

public class BenchmarkTLVStatisticsByBOResultDTO extends AbstractDTO
{
   public BusinessObjectStatisticDTO  totalBusinessObjectStatistic;
   public List<BusinessObjectStatisticDTO> businessObjectsResultList;
   public Map<String,List<BusinessObjectStatisticDTO>> businessObjectsForGroupByMap;

}
