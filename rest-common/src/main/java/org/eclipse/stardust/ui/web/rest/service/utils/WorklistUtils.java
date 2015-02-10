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

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.engine.api.model.DataPath;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.query.*;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.common.column.ColumnPreference.ColumnDataType;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO.DescriptorFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO.RangeDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.WorklistFilterDTO.TextSearchDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.DateRange;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.DescriptorFilterUtils;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.GenericDescriptorFilterModel;
import org.eclipse.stardust.ui.web.viewscommon.descriptors.NumberRange;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;

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
   private ProcessDefinitionUtils processDefUtils;
   
   private double PORTAL_CRITICALITY_MUL_FACTOR = 1000;
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
	      FilterOrTerm or = filter.addOrTerm();
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
	      FilterOrTerm or = filter.addOrTerm();
			for (String priority : filterDTO.priority.like) {
				or.or((worklistQuery ? WorklistQuery.PROCESS_INSTANCE_PRIORITY
						: ActivityInstanceQuery.PROCESS_INSTANCE_PRIORITY)
						.isEqual(Integer.valueOf(priority)));
			}
		}
		

		// Criticality Filter
		if (null != filterDTO.criticality) {
         FilterOrTerm or = filter.addOrTerm();
			for (RangeDTO criticality : filterDTO.criticality.rangeLike) {
					or.or((worklistQuery ? WorklistQuery.ACTIVITY_INSTANCE_CRITICALITY
							: ActivityInstanceQuery.CRITICALITY).between(
							(criticality.from /PORTAL_CRITICALITY_MUL_FACTOR),criticality.to/PORTAL_CRITICALITY_MUL_FACTOR));
			}

		}
		

		// Activities Filter
		if (null != filterDTO.overview) {
		
		   if (!CollectionUtils.isEmpty(filterDTO.overview.activities)) {
		      FilterOrTerm or = filter.addOrTerm();
		      if(filterDTO.overview.activities.contains("-1")) {
		      }else{
		         for (String activity : filterDTO.overview.activities) {
		            
		            or.add(ActivityFilter.forAnyProcess(activity));
		         }
		      }
		   }
		   
		   if (!CollectionUtils.isEmpty(filterDTO.overview.processes)) {
		      FilterOrTerm or = filter.addOrTerm();
		      if( !filterDTO.overview.processes.contains("-1")){
	            for (String processQId : filterDTO.overview.processes) {
	               
	               or.add(new ProcessDefinitionFilter(processQId, false));
	            }
	         }
         }
		}

		// Process Filter
		if (null != filterDTO.processDefinition) {
		   FilterOrTerm or = filter.addOrTerm();
		   if( !filterDTO.processDefinition.processes.contains("-1")){
		      for (String processQId : filterDTO.processDefinition.processes) {
		         
		         or.add(new ProcessDefinitionFilter(processQId, false));
		      }
		   }
		}
		
		addDescriptorFilters(query, filterDTO);

	}
	
	
	private void addDescriptorFilters(Query query , WorklistFilterDTO worklistDTO){
	   
	   Map<String, DescriptorFilterDTO> descFilterMap = worklistDTO.descriptorFilterMap;
	   
	   if( null != descFilterMap){
	      query.setPolicy(DescriptorPolicy.WITH_DESCRIPTORS);
	      Map<String, DataPath> descriptors = processDefUtils.getAllDescriptors(true);
	      GenericDescriptorFilterModel filterModel = GenericDescriptorFilterModel.create(descriptors.values());
         filterModel.setFilterEnabled(true);
         
         
         for (java.util.Map.Entry<String, DescriptorFilterDTO> descriptor : descFilterMap.entrySet())
         {
            Object value = null;
            // Boolean type desc
            if (descriptor.getValue().type.equals(ColumnDataType.BOOLEAN.toString()))
            {
               value = descriptor.getValue().value;
            }
            // String type desc
            else  if (descriptor.getValue().type.equals(ColumnDataType.STRING.toString()))
            {
               value = ((TextSearchDTO)descriptor.getValue().value).textSearch;
            }
            // Number type desc
            else  if (descriptor.getValue().type.equals(ColumnDataType.NUMBER.toString()))
            {
                  Number from =  ((RangeDTO)descriptor.getValue().value).from ;
                  Number to =  ((RangeDTO)descriptor.getValue().value).to ;
                  value = new NumberRange(from, to);
            }
            // Date type desc
            else  if (descriptor.getValue().type.equals(ColumnDataType.DATE.toString()))
            {  
               Long from =  ((RangeDTO)descriptor.getValue().value).from ;
               Long to =  ((RangeDTO)descriptor.getValue().value).to ;
               value=  new DateRange(new Date(from), new Date(to));
            }
            
            filterModel.setFilterValue(descriptor.getKey(),(Serializable) value);
         }
         
         DescriptorFilterUtils.applyFilters(query, filterModel);
	   }else{
	      query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
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
