/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
/**
 * @author Johnson.Quadras
 */

package org.eclipse.stardust.ui.web.rest.component.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.component.util.TrafficLightViewUtils;
import org.eclipse.stardust.ui.web.rest.dto.BenchmarkCategoryDTO;
import org.eclipse.stardust.ui.web.rest.dto.BenchmarkProcessActivitiesTLVStatisticsResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.BenchmarkTLVStatisticsByBOResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessDefinitionDTO;
import org.springframework.stereotype.Component;

@Component
public class TrafficLightViewService
{
   @Resource
   TrafficLightViewUtils trafficLightViewUtils;

   private static final Logger trace = LogManager.getLogger(TrafficLightViewService.class);

   public BenchmarkProcessActivitiesTLVStatisticsResultDTO getTrafficLightViewStatastic(Boolean isAllBenchmarks,
         Boolean isAllProcessess, List<Long> bOids, List<ProcessDefinitionDTO> processes, String dateType,
         Integer dayOffset, List<BenchmarkCategoryDTO> benchmarkCategories,
         Map<String, List<String>> processActivitiesMap)
   {

      return trafficLightViewUtils.getTrafficLightViewStatastic(isAllBenchmarks, isAllProcessess, bOids, processes,
            dateType, dayOffset, benchmarkCategories, processActivitiesMap);
   }

   public BenchmarkTLVStatisticsByBOResultDTO getTrafficLightViewStatasticByBO(Boolean isAllBenchmarks,
         Boolean isAllProcessess, List<Long> bOids, List<ProcessDefinitionDTO> processes, String dateType,
         Integer dayOffset, List<BenchmarkCategoryDTO> benchmarkCategories, String businessObjectQualifiedId,
         Set<?> selBOInstances, String groupBybusinessQualifiedId, Set<?> selGroupByBOInstances, String selectedBOType)
   {
      return trafficLightViewUtils.getTrafficLightViewStatasticByBO(isAllBenchmarks, isAllProcessess, bOids, processes,
            dateType, dayOffset, benchmarkCategories, businessObjectQualifiedId, selBOInstances,
            groupBybusinessQualifiedId, selGroupByBOInstances, selectedBOType);
   }
}
