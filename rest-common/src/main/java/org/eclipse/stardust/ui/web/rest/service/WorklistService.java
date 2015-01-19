/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.WorklistUtils;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
public class WorklistService
{
   @Resource
   private WorklistUtils worklistUtils;

   @Resource
   private ActivityInstanceUtils activityInstanceUtils;

   /**
    * @param participantQId
    * @return
    */
   public QueryResultDTO getWorklistForParticipant(String participantQId, String context)
   {
      QueryResult<?> queryResult = worklistUtils.getWorklistForParticipant(participantQId);
      return buildWorklistResult(queryResult, context);
   }

   /**
    * @param userId
    * @return
    */
   public QueryResultDTO getWorklistForUser(String userId, String context)
   {
      QueryResult<?> queryResult = worklistUtils.getWorklistForUser(userId);
      return buildWorklistResult(queryResult, context);
   }

   /**
    * @param queryResult
    * @return
    */
   private QueryResultDTO buildWorklistResult(QueryResult<?> queryResult, String context)
   {
      List<ActivityInstanceDTO> list = new ArrayList<ActivityInstanceDTO>();
      for (Object object : queryResult)
      {
         if (object instanceof ActivityInstance)
         {
            ActivityInstance ai = (ActivityInstance) object;

            ActivityInstanceDTO dto;
            if (null == context)
            {
               dto = DTOBuilder.build(ai, ActivityInstanceDTO.class);
            }
            else
            {
               TrivialActivityInstanceDTO trivialDto = DTOBuilder.build(ai, TrivialActivityInstanceDTO.class);
               trivialDto.trivial = activityInstanceUtils.isTrivialManualActivity(ai, context);
               dto = trivialDto;
            }
            list.add(dto);
         }
      }

      QueryResultDTO resultDTO = new QueryResultDTO();
      resultDTO.list = list;
      resultDTO.totalCount = queryResult.getTotalCount();

      return resultDTO;
   }
}
