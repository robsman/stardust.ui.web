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
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterAndNotTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.springframework.stereotype.Component;

/**
 * @author Subodh.Godbole
 * @author Johnson.Quadras
 * @version $Revision: $
 */
@Component
public class WorklistUtils
{

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private ProcessDefinitionUtils processDefUtils;

   /**
    * @param participantQId
    * @return
    */
   public QueryResult< ? > getWorklistForParticipant(String participantQId,
         Options options)
   {
      Participant participant = serviceFactoryUtils.getQueryService().getParticipant(
            participantQId);
      if (null != participant)
      {
         WorklistQuery query = org.eclipse.stardust.ui.web.viewscommon.utils.WorklistUtils
               .createWorklistQuery(participant);
         
         query.setPolicy(HistoricalStatesPolicy.WITH_LAST_USER_PERFORMER);

         ActivityTableUtils.addDescriptorPolicy(options, query);

         ActivityTableUtils.addSortCriteria(query, options);

         ActivityTableUtils.addFilterCriteria(query, options);

         SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip,
               true);
         query.setPolicy(subsetPolicy);

         Worklist worklist = serviceFactoryUtils.getWorkflowService().getWorklist(
               (WorklistQuery) query);
         QueryResult< ? > queryResult = extractParticipantWorklist(worklist, participant);

         return queryResult;
      }
      return null;
   }

   /**
    * @param userId
    * @return
    */
   public QueryResult< ? > getWorklistForUser(String userId, Options options)
   {
      User user = serviceFactoryUtils.getUserService().getUser(userId);

      if (null != user)
      {
         // TODO: User WorklistQuery?
         ActivityInstanceQuery query = ActivityInstanceQuery
               .findInState(new ActivityInstanceState[] {
                     ActivityInstanceState.Application, ActivityInstanceState.Suspended});
         // TODO - this is used to enhance performace but has a bug
         // query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

         ActivityTableUtils.addDescriptorPolicy(options, query);

         ActivityTableUtils.addSortCriteria(query, options);

         ActivityTableUtils.addFilterCriteria(query, options);

         SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip,
               true);
         query.setPolicy(subsetPolicy);

         FilterOrTerm or = query.getFilter().addOrTerm();
         or.add(PerformingParticipantFilter.ANY_FOR_USER).add(
               new PerformingUserFilter(user.getOID()));

         // Remove role activities
         FilterAndNotTerm not = query.getFilter().addAndNotTerm();
         List<Grant> allGrants = user.getAllGrants();
         for (Grant grant : allGrants)
         {
            not.add(PerformingParticipantFilter.forParticipant(serviceFactoryUtils
                  .getQueryService().getParticipant(grant.getId())));
         }

         ActivityInstances activityInstances = serviceFactoryUtils.getQueryService()
               .getAllActivityInstances(query);

         return activityInstances;
      }
      else
      {
         throw new ObjectNotFoundException("UserId not found");
      }
   }
   

   /**
    * @param worklist
    * @param participantInfo
    * @return
    */
   @SuppressWarnings("unchecked")
   private Worklist extractParticipantWorklist(Worklist worklist,
         ParticipantInfo participantInfo)
   {
      Worklist extractedWorklist = null;

      switch (ParticipantUtils.getParticipantType(participantInfo))
      {
         case ORGANIZATION:
         case ROLE:
         case SCOPED_ORGANIZATION:
         case SCOPED_ROLE:
         case USERGROUP:
            Iterator<Worklist> worklistIter1 = worklist.getSubWorklists();
            Worklist subWorklist;
            while (worklistIter1.hasNext())
            {
               subWorklist = worklistIter1.next();
               if (ParticipantUtils.areEqual(participantInfo, subWorklist.getOwner()))
               {
                  extractedWorklist = subWorklist;
                  break;
               }
            }
            break;

         case USER:
            if (ParticipantUtils.areEqual(participantInfo, worklist.getOwner()))
            {
               extractedWorklist = worklist;
               break;
            }
            else
            {
               // User-Worklist(Deputy Of) is contained in Sub-worklist of
               // User worklist(Deputy)
               Iterator<Worklist> subWorklistIter = worklist.getSubWorklists();
               Worklist subWorklist1;
               while (subWorklistIter.hasNext())
               {
                  subWorklist1 = subWorklistIter.next();
                  if (ParticipantUtils.areEqual(participantInfo, subWorklist1.getOwner()))
                  {
                     extractedWorklist = subWorklist1;
                     break;
                  }
               }
            }
      }

      return extractedWorklist;
   }

}
