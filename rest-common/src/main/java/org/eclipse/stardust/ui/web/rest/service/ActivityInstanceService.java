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
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.service;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.service.dto.*;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.CriticalityUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;

/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
public class ActivityInstanceService
{
	@Resource
	private ActivityInstanceUtils activityInstanceUtils;
	@Resource
	private ParticipantSearchComponent participantSearchComponent;
	@Resource
	private DelegationComponent delegateComponent;

	@Resource
	CriticalityUtils criticalityUtils;

	/**
	 * @param activityInstanceOid
	 * @return
	 */
	public ActivityInstanceDTO getActivityInstance(long activityInstanceOid)
	{
		ActivityInstance ai = activityInstanceUtils.getActivityInstance(activityInstanceOid);
		return DTOBuilder.build(ai, ActivityInstanceDTO.class);
	}

	/**
	 * @param oid
	 * @return
	 */
	public String getAllDataMappingsAsJson(long oid, String context)
	{
		ActivityInstance ai = activityInstanceUtils.getActivityInstance(oid);
		String json = activityInstanceUtils.getAllDataMappingsAsJson(ai, context);
		return json;
	}

	/**
	 * @param oid
	 * @return
	 */
	public Map<String, Serializable> getAllInDataValues(long oid, String context)
	{
		ActivityInstance ai = activityInstanceUtils.getActivityInstance(oid);
		Map<String, Serializable> values = activityInstanceUtils.getAllInDataValues(ai, context);

		return values;
	}

	/**
	 * @param userId
	 * @return
	 */
	public Map<String, TrivialManualActivityDTO> getTrivialManualActivitiesDetails(List<Long> oids, String context)
	{
		Map<String, TrivialManualActivityDTO> dto = activityInstanceUtils
				.getTrivialManualActivitiesDetails(oids, context);
		return dto;
	}

	/**
	 * @param activities
	 * @param context
	 * @return
	 */
	public Map<Long, String> completeAll(List<ActivityInstanceOutDataDTO> activities, String context)
	{
		Map<Long, String> result = new HashMap<Long, String>();
		for (ActivityInstanceOutDataDTO aiDto : activities)
		{
			try
			{
				activityInstanceUtils.complete(aiDto.oid, context, aiDto.outData);
			}
			catch (Exception e)
			{
				result.put(aiDto.oid, e.getMessage());
			}
		}
		return result;
	}

	/**
	 * @param activityInstanceOid
	 * @return
	 */
	public List<DocumentDTO> getProcessAttachmentsForActivityInstance(
			long activityInstanceOid)
			{
		List<Document> processAttachments = activityInstanceUtils
				.getProcessAttachments(activityInstanceOid);

		List<DocumentDTO> processAttachmentsDTO = DocumentDTOBuilder
				.build(processAttachments);

		return processAttachmentsDTO;
			}

	/**
	 * @param oid
	 * @param documentId
	 * @return
	 */
	public List<ProcessInstanceDTO> completeRendezvous(long oid, String documentId)
	{
		ActivityInstance completedAi = activityInstanceUtils.completeRendezvous(oid,
				documentId);

		// TODO: Change method return type
		// return completedAi;

		return null;
	}

	/**
	 * @author Yogesh.Manware
	 * @param request
	 * @return
	 */
	public String abortActivities(String request)
	{
		JsonObject json = GsonUtils.readJsonObject(request);
		String scope = GsonUtils.extractString(json, "scope");

		Type listType = new TypeToken<List<Long>>(){}.getType();

		@SuppressWarnings("unchecked")
		List<Long> activities = (List<Long>) GsonUtils.extractList(GsonUtils.extractJsonArray(json, "activities"),
				listType);
		NotificationMap notificationMap = new NotificationMap();

		if ("activity".equalsIgnoreCase(scope))
		{
			notificationMap = activityInstanceUtils.abortActivities(AbortScope.SubHierarchy, activities);
		}
		else if ("rootProcess".equalsIgnoreCase(scope))
		{
			notificationMap = activityInstanceUtils.abortActivities(AbortScope.RootHierarchy, activities);
		}
		return GsonUtils.toJsonHTMLSafeString(notificationMap);
	}

	/**
	 * Get all available criticalities
	 * @return List
	 */
	public List<CriticalityDTO> getCriticalities() {

		List<CriticalityDTO> criticalityCategories = new ArrayList<CriticalityDTO>();
		for (CriticalityCategory category : criticalityUtils
				.getCriticalityConfiguration()) {
			CriticalityDTO criticalityDTO = DTOBuilder.build(category,
					CriticalityDTO.class);
			criticalityCategories.add(criticalityDTO);
		}
		return criticalityCategories;
	}

	
	/**
	 * Returns all states
	 * @return List
	 */
	public List<StatusDTO> getAllActivityStates() {
	      MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
	      List<StatusDTO> allStatusList = new ArrayList<StatusDTO>();

	      allStatusList.add(new StatusDTO(ActivityInstanceState.ABORTED, propsBean
	            .getString("views.activityTable.statusFilter.aborted")));

	      allStatusList.add(new StatusDTO(ActivityInstanceState.ABORTING, propsBean
	            .getString("views.activityTable.statusFilter.aborting")));

	      allStatusList.add(new StatusDTO(ActivityInstanceState.APPLICATION, propsBean
	            .getString("views.activityTable.statusFilter.application")));

	      allStatusList.add(new StatusDTO(ActivityInstanceState.COMPLETED, propsBean
	            .getString("views.activityTable.statusFilter.completed")));

	      allStatusList.add(new StatusDTO(ActivityInstanceState.CREATED, propsBean
	            .getString("views.activityTable.statusFilter.created")));

	      allStatusList.add(new StatusDTO(ActivityInstanceState.HIBERNATED, propsBean
	            .getString("views.activityTable.statusFilter.hibernated")));

	      allStatusList.add(new StatusDTO(ActivityInstanceState.INTERRUPTED, propsBean
	            .getString("views.activityTable.statusFilter.interrupted")));

	      allStatusList.add(new StatusDTO(ActivityInstanceState.SUSPENDED, propsBean
	            .getString("views.activityTable.statusFilter.suspended")));
	      return allStatusList;
	   }
}
