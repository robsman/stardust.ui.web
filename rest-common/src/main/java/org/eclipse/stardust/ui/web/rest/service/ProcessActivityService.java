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
package org.eclipse.stardust.ui.web.rest.service;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.service.utils.ProcessActivityUtils;
import org.springframework.stereotype.Component;

/**
 * @author Aditya.Gaikwad
 *
 */

@Component
public class ProcessActivityService
{

   @Resource
   private ProcessActivityUtils processActivityUtils;

   @Resource
   private ProcessDefinitionService processDefinitionService;

   @Resource
   private org.eclipse.stardust.ui.web.rest.service.utils.ProcessInstanceUtils processInstanceUtilsREST;

   /**
    * 
    * @param options
    * @param postData
    * @param processSearchAttributes
    * @return
    */
   public QueryResultDTO performSearch(Options options, String postData,
         ProcessSearchCriteriaDTO processSearchAttributes, List<DescriptorColumnDTO> availableDescriptors)
   {
      if (processSearchAttributes.filterObject == 0)
      {
         QueryResult<ProcessInstance> processInstances = getProcessActivityUtils().performProcessSearch(options,
               postData, processSearchAttributes, availableDescriptors);
         return buildProcessSearchresult(processInstances);
      }
      else if (processSearchAttributes.filterObject == 1)
      {
         QueryResult<ActivityInstance> activityInstances = getProcessActivityUtils().performActivitySearch(options,
               postData, processSearchAttributes, availableDescriptors);
         return buildActivitySearchResult(activityInstances);
      }
      return null;
   }

   /**
    * @param processInstanceDTOs
    */
   private QueryResultDTO buildProcessSearchresult(QueryResult<ProcessInstance> processInstances)
   {
      QueryResultDTO processList = processInstanceUtilsREST.buildProcessListResult(processInstances);
      return processList;
   }

   /**
    * @param processInstanceDTOs
    */
   private QueryResultDTO buildActivitySearchResult(QueryResult<ActivityInstance> activityInstances)
   {
      QueryResultDTO buildWorklistResult = ActivityTableUtils.buildTableResult(activityInstances, MODE.ACTIVITY_TABLE);
      return buildWorklistResult;
   }

   public ProcessActivityUtils getProcessActivityUtils()
   {
      return processActivityUtils;
   }

   public void setProcessActivityUtils(ProcessActivityUtils processActivityUtils)
   {
      this.processActivityUtils = processActivityUtils;
   }

}