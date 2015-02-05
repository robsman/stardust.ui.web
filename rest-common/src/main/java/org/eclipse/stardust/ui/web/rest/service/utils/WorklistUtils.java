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
package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterAndNotTerm;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.CriticalityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PrioirtyDTO;
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
   private static final String COL_ACTIVITY_NAME = "overview";
   private static final String COL_ACTIVITY_INSTANCE_OID = "oid";
   private static final String COL_START_TIME = "started";
   private static final String COL_LAST_MODIFICATION_TIME = "lastModified";

   @Resource
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   private CriticalityUtils criticalityUtils;
   /**
    * @param participantQId
    * @return
    */
   public QueryResult<?> getWorklistForParticipant(String participantQId, Options options)
   {
      Participant participant = serviceFactoryUtils.getQueryService().getParticipant(participantQId);
      if (null != participant)
      {
         WorklistQuery query = org.eclipse.stardust.ui.web.viewscommon.utils.WorklistUtils.createWorklistQuery(participant);
         query.setPolicy(HistoricalStatesPolicy.WITH_LAST_USER_PERFORMER);
         query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
         
         addSortCriteria(query, options);
         
         addFilterCriteria(query, options);

         SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
         query.setPolicy(subsetPolicy);
         
         Worklist worklist = serviceFactoryUtils.getWorkflowService().getWorklist((WorklistQuery) query);
         QueryResult<?> queryResult = extractParticipantWorklist(worklist, participant);
         
         return queryResult;
      }
      return null;
   }
   
   /**
    * Adds filter criteria to the query
    * @param query Query
    * @param options Options
    */
	private void addFilterCriteria(Query query, Options options) {

		WorklistFilterDTO filterDTO = options.filter;

		if (filterDTO == null) {
			return;
		}

		FilterAndTerm filter = query.getFilter().addAndTerm();
		FilterOrTerm or = filter.addOrTerm();

		boolean worklistQuery = query instanceof WorklistQuery;

		// Activity ID
		if (null != filterDTO.oid) {
			if (null != filterDTO.oid.from) {
				filter.and((worklistQuery ? WorklistQuery.ACTIVITY_INSTANCE_OID
						: ActivityInstanceQuery.OID)
						.greaterOrEqual(filterDTO.oid.from));
			}
			if (null != filterDTO.oid.to) {
				filter.and((worklistQuery ? WorklistQuery.ACTIVITY_INSTANCE_OID
						: ActivityInstanceQuery.OID)
						.lessOrEqual(filterDTO.oid.to));
			}
		}

		// Start Filter
		if (null != filterDTO.started) {

			if (filterDTO.started.from != null) {
				Date fromDate = new Date(filterDTO.started.from);
				filter.and((worklistQuery ? WorklistQuery.START_TIME
						: ActivityInstanceQuery.START_TIME)
						.greaterOrEqual(fromDate.getTime()));
			}

			if (filterDTO.started.to != null) {
				Date toDate = new Date(filterDTO.started.to);
				filter.and((worklistQuery ? WorklistQuery.START_TIME
						: ActivityInstanceQuery.START_TIME).lessOrEqual(toDate
						.getTime()));
			}
		}

		// Modified Filter
		if (null != filterDTO.lastModified) {

			if (filterDTO.lastModified.from != null) {
				Date fromDate = new Date(filterDTO.lastModified.from);

				filter.and((worklistQuery ? WorklistQuery.LAST_MODIFICATION_TIME
						: ActivityInstanceQuery.LAST_MODIFICATION_TIME)
						.greaterOrEqual(fromDate.getTime()));
			}

			if (filterDTO.lastModified.to != null) {
				Date toDate = new Date(filterDTO.lastModified.to);

				filter.and((worklistQuery ? WorklistQuery.LAST_MODIFICATION_TIME
						: ActivityInstanceQuery.LAST_MODIFICATION_TIME)
						.lessOrEqual(toDate.getTime()));
			}
		}

		// Status Filter
		if (null != filterDTO.status) {

			for (String status : filterDTO.status.like) {

				Integer actState = Integer.parseInt(status);
				
				if (!worklistQuery) {
					or.add(ActivityInstanceQuery.STATE.isEqual(Long
							.parseLong(status.toString())));
				} else if (worklistQuery) {
					// Worklist Query uses ActivityStateFilter.
					or.add(new ActivityStateFilter(ActivityInstanceState
							.getState(actState)));
				}
			}
		}

		// Priority Filter
		if (null != filterDTO.priority) {

			for (PrioirtyDTO priority : filterDTO.priority.priorityLike) {
				or.or((worklistQuery ? WorklistQuery.PROCESS_INSTANCE_PRIORITY
						: ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY)
						.isEqual(Integer.valueOf(priority.value)));
			}
		}

		// Criticality Filter
		if (null != filterDTO.criticality) {

			for (CriticalityDTO criticality : filterDTO.criticality.criticalityLike) {
					or.or((worklistQuery ? WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY
							: ActivityInstanceQuery.CRITICALITY).between(
							criticality.rangeFrom, criticality.rangeTo));
			}

		}

		// Activities Filter
		if (null != filterDTO.overview) {

			if (!CollectionUtils.isEmpty(filterDTO.overview.activities)) {
				for (String activity : filterDTO.overview.activities) {
					if (!StringUtils.equals("-1", activity))
						or.add(ActivityFilter.forAnyProcess(activity));
				}
			}
		}

		// Process Filter
		if (null != filterDTO.processDefinition) {
			for (String processQId : filterDTO.processDefinition.processes) {
				if (!StringUtils.equals("-1", processQId))
					or.add(new ProcessDefinitionFilter(processQId, false));
			}
		}

	}



/**
    * @param userId
    * @return
    */
   public QueryResult<?> getWorklistForUser(String userId, Options options)
   {
      User user = serviceFactoryUtils.getUserService().getUser(userId);

      if(null != user)
      {
         // TODO: User WorklistQuery?
         ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
               ActivityInstanceState.Application, ActivityInstanceState.Suspended});
         // TODO - this is used to enhance performace but has a bug 
         // query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

         addSortCriteria(query, options);
         
         addFilterCriteria(query, options);

         SubsetPolicy subsetPolicy = new SubsetPolicy(options.pageSize, options.skip, true);
         query.setPolicy(subsetPolicy);
         
         FilterOrTerm or = query.getFilter().addOrTerm();
         or.add(PerformingParticipantFilter.ANY_FOR_USER).add(new PerformingUserFilter(user.getOID()));
         
         // Remove role activities
         FilterAndNotTerm not = query.getFilter().addAndNotTerm();
         List<Grant> allGrants = user.getAllGrants();
         for (Grant grant : allGrants)
         {
            not.add(PerformingParticipantFilter.forParticipant(serviceFactoryUtils.getQueryService().getParticipant(
                  grant.getId())));
         }         

         ActivityInstances activityInstances = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

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
   private Worklist extractParticipantWorklist(Worklist worklist, ParticipantInfo participantInfo)
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
   
   /**
    * @param query
    * @param options
    */
   private void addSortCriteria(Query query, Options options)
   {
      boolean worklistQuery = query instanceof WorklistQuery;

      if (COL_ACTIVITY_NAME.equals(options.orderBy))
      {
         query.orderBy(ActivityInstanceQuery.ACTIVITY_NAME.ascendig(options.asc));
      }
      else if (COL_ACTIVITY_INSTANCE_OID.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.ACTIVITY_INSTANCE_OID 
               : ActivityInstanceQuery.OID, options.asc);
      }
      else if (COL_START_TIME.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.START_TIME
               : ActivityInstanceQuery.START_TIME, options.asc);
      }
      else if (COL_LAST_MODIFICATION_TIME.equals(options.orderBy))
      {
         query.orderBy(worklistQuery
               ? WorklistQuery.LAST_MODIFICATION_TIME
               : ActivityInstanceQuery.LAST_MODIFICATION_TIME, options.asc);
      }
   }
}
