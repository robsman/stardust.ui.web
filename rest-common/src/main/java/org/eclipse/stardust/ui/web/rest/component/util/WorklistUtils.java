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
package org.eclipse.stardust.ui.web.rest.component.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.engine.api.model.OrganizationInfo;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.EvaluateByWorkitemsPolicy;
import org.eclipse.stardust.engine.api.query.ExcludeUserPolicy;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformedByUserFilter;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.ReflectionUtils;
import org.eclipse.stardust.ui.web.rest.common.Resources;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.service.UserService;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.MyProcessDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.response.WorklistParticipantDTO;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.common.ParticipantLabel;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.provider.DefaultAssemblyLineActivityProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.provider.IAssemblyLineActivityProvider;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.SpiConstants;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils.ModelResubmissionActivity;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
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

   @Resource
   private UserService userService;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   public static Logger trace = LogManager.getLogger(WorklistUtils.class);

   /**
    * @param participantQId
    * @return
    */
   public QueryResult< ? > getWorklistForParticipant(String participantQId, String userId, DataTableOptionsDTO options)
   {
      // If the userId is not passed consider the user to be the logged in user.
      // User id is required to differentiate between the particpants when the deputy logs
      // in

      if (StringUtils.isEmpty(userId))
      {
         userId = userService.getLoggedInUser().id;
      }
      ParticipantInfo participantInfo = ParticipantWorklistCacheManager.getInstance().getParticipantInfoFromCache(
            participantQId);
      WorklistQuery query = (WorklistQuery) ParticipantWorklistCacheManager.getInstance().getWorklistQuery(
            participantInfo, userId);

      ActivityTableUtils.addCriterias(query, options);
      Worklist worklist = serviceFactoryUtils.getWorkflowService().getWorklist((WorklistQuery) query);
      QueryResult< ? > queryResult = extractParticipantWorklist(worklist, participantInfo);

      if (options.filter == null)
      {
         updateParticipantManagerCache(participantInfo, userId, queryResult);
      }

      return queryResult;

   }

   /**
    * @param userId
    * @return
    */
   public QueryResult< ? > getWorklistForUser(String userId, DataTableOptionsDTO options, boolean fetchAllStates)
   {
      User user = serviceFactoryUtils.getUserService().getUser(userId);

      if (null != user)
      {
         ActivityInstanceQuery query = null;
         if (fetchAllStates)
         {
            query = ActivityInstanceQuery.findAll();
         }
         else
         {
            query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
                  ActivityInstanceState.Application, ActivityInstanceState.Suspended});
         }

         FilterOrTerm or = query.getFilter().addOrTerm();
         or.add(ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID.isEqual(user.getOID()));

         ActivityTableUtils.addCriterias(query, options);

         ActivityInstances activityInstances = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

         ParticipantInfo participantInfo = ParticipantWorklistCacheManager.getInstance().getParticipantInfoFromCache(
               user.getQualifiedId());

         if (options.filter == null)
         {
            if (fetchAllStates)
            {

               updateActivitiyQueryCache(options.worklistId, activityInstances);
            }
            else
            {

               updateParticipantManagerCache(participantInfo, userId, activityInstances);
            }

         }

         return activityInstances;
      }
      else
      {
         throw new ObjectNotFoundException("UserId not found");
      }
   }

   /**
    * @param userId
    * @return
    */
   public QueryResult< ? > getUnifiedWorklistForUser(String userId, DataTableOptionsDTO options)
   {
      User user = null;
      if(StringUtils.isNotEmpty(userId)) {
         user = serviceFactoryUtils.getUserService().getUser(userId);
      }
      else {
         user = SessionContext.findSessionContext().getUser();
      }

      if (null != user)
      {
         ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
               ActivityInstanceState.Application, ActivityInstanceState.Suspended});

         FilterOrTerm or = query.getFilter().addOrTerm();
         or.add(new PerformingUserFilter(user.getOID()));
         Set<ParticipantInfo> participants = ParticipantWorklistCacheManager.getInstance().getWorklistParticipants()
               .get(user.getQualifiedId());
         for (ParticipantInfo participantInfo : participants)
         {
            if (!(participantInfo instanceof UserInfo))
            {
               or.add(PerformingParticipantFilter.forParticipant(participantInfo));
            }
         }

         query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
         query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

         ActivityTableUtils.addCriterias(query, options);

         ActivityInstances activityInstances = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

         return activityInstances;
      }
      else
      {
         throw new ObjectNotFoundException("UserId not found");
      }
   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForHighCriticality(DataTableOptionsDTO options)
   {
      ActivityInstanceQuery criticalActivitiesQuery = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});

      FilterOrTerm or = criticalActivitiesQuery.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);

      criticalActivitiesQuery.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      criticalActivitiesQuery.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

      List<CriticalityCategory> criticalityConfigs = CriticalityUtils.getCriticalityConfiguration();
      CriticalityCategory highCriticality = CriticalityUtils.getCriticalityCategory(
            CriticalityConfigurationUtil.PORTAL_CRITICALITY_MAX, criticalityConfigs);
      criticalActivitiesQuery.where(ActivityInstanceQuery.CRITICALITY.between(highCriticality.getRangeFrom()
            / ActivityTableUtils.PORTAL_CRITICALITY_MUL_FACTOR, highCriticality.getRangeTo()
            / ActivityTableUtils.PORTAL_CRITICALITY_MUL_FACTOR));

      ActivityTableUtils.addCriterias(criticalActivitiesQuery, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(criticalActivitiesQuery);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }
      return result;

   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getAllAssignedWorkItems(DataTableOptionsDTO options)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      FilterOrTerm or = query.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);
      query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }
      return result;
   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistItemsFromDate(String dateId, DataTableOptionsDTO options)
   {
      Date fromDate = ActivityTableUtils.determineDate(dateId);
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Suspended, ActivityInstanceState.Completed, ActivityInstanceState.Created,
            ActivityInstanceState.Interrupted, ActivityInstanceState.Application});
      FilterTerm where = query.getFilter().addAndTerm();
      where.add(ActivityInstanceQuery.LAST_MODIFICATION_TIME.greaterOrEqual(fromDate.getTime()));
      where.addOrTerm().add(PerformingUserFilter.CURRENT_USER).add(PerformedByUserFilter.CURRENT_USER);

      ActivityTableUtils.addCriterias(query, options);

      return serviceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   /**
    * 
    * @param criticalityValue
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistByProcess(String processQId, DataTableOptionsDTO options)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});

      FilterOrTerm where = query.getFilter().addOrTerm();
      where.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);
      query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);
      query.where(new ProcessDefinitionFilter(processQId, false));

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }
      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForResubmissionActivities(DataTableOptionsDTO options)
   {

      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(ActivityInstanceState.Hibernated);
      // new PerformingUserFilter(0) : For activities created in non-interactive context
      // (such as activity threads started by daemons)
      query.getFilter().addOrTerm().or(PerformingUserFilter.CURRENT_USER).or(new PerformingUserFilter(0));
      List<ModelResubmissionActivity> resubmissionActivities = CollectionUtils.newList();
      ResubmissionUtils.fillListWithResubmissionActivities(resubmissionActivities);

      if (resubmissionActivities.isEmpty())
      {
         query.getFilter().add(ActivityInstanceQuery.ACTIVITY_OID.isNull());
      }
      else
      {
         FilterOrTerm or = query.getFilter().addOrTerm();
         for (Iterator<ModelResubmissionActivity> as = resubmissionActivities.iterator(); as.hasNext();)
         {
            ModelResubmissionActivity activity = as.next();
            or.add(ActivityFilter.forProcess(activity.getActivityId(), activity.getProcessId(),
                  activity.getModelOids(), false));
         }
      }

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }

      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForLoggedInUser(DataTableOptionsDTO options)
   {

      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      query.getFilter().add(PerformingUserFilter.CURRENT_USER);

      query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      UserDTO loggedInUser = userService.getLoggedInUser();

      ParticipantInfo participantInfo = ParticipantWorklistCacheManager.getInstance().getParticipantInfoFromCache(
            loggedInUser.qualifiedId);

      if (null == options.filter)
      {
         updateParticipantManagerCache(participantInfo, loggedInUser.id, result);
      }

      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getAllActivable(DataTableOptionsDTO options)
   {

      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Hibernated, ActivityInstanceState.Application, ActivityInstanceState.Suspended});

      query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      
      ActivityTableUtils.addCriterias(query, options);

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      if (null == options.filter)
      {
         updateActivitiyQueryCache(options.worklistId, result);
      }

      return result;
   }

   /**
    * 
    * @param options
    * @return
    */
   public QueryResult< ? > getWorklistForProcessInstances(DataTableOptionsDTO options, List<String> pInstanceOids)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAlive();
      FilterOrTerm orTerm = query.getFilter().addOrTerm();

      query.setPolicy(ExcludeUserPolicy.EXCLUDE_USER);
      query.setPolicy(EvaluateByWorkitemsPolicy.WORKITEMS);

      ActivityTableUtils.addCriterias(query, options);

      for (String oid : pInstanceOids)
      {
         orTerm.add(ActivityInstanceQuery.PROCESS_INSTANCE_OID.isEqual(Long.valueOf(oid)));
      }

      QueryResult< ? > result = serviceFactoryUtils.getQueryService().getAllActivityInstances(query);

      return result;
   }

   /**
    * 
    * @param participantInfo
    * @param userId
    * @param queryResult
    */
   private <T> void updateParticipantManagerCache(ParticipantInfo participantInfo, String userId,
         QueryResult<T> queryResult)
   {
      ParticipantWorklistCacheManager.getInstance().setWorklistCount(participantInfo, userId,
            queryResult.getTotalCount());
      ParticipantWorklistCacheManager.getInstance().setWorklistThresholdCount(participantInfo, userId,
            queryResult.getTotalCountThreshold());
   }

   /**
    * 
    * @param worklistId
    * @param queryResult
    */
   private <T> void updateActivitiyQueryCache(String worklistId, QueryResult<T> queryResult)
   {
      if (SpecialWorklistCacheManager.isSpecialWorklist(worklistId))
      {
         SpecialWorklistCacheManager.getInstance().setWorklistCount(worklistId, queryResult.getTotalCount());
         SpecialWorklistCacheManager.getInstance().setWorklistThresholdCount(worklistId,
               queryResult.getTotalCountThreshold());
      }
      else
      {
         if (ProcessWorklistCacheManager.isInitialized())
         {
            ProcessWorklistCacheManager.getInstance().setWorklistCount(worklistId, queryResult.getTotalCount());
            ProcessWorklistCacheManager.getInstance().setWorklistThresholdCount(worklistId,
                  queryResult.getTotalCountThreshold());
         }
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

   public List<WorklistParticipantDTO> getWorklistAssignemnt(Boolean showEmptyWorklist, boolean reload) throws PortalException
   {

      if (reload)
      {
         ParticipantWorklistCacheManager.getInstance().reset();
      }
      Map<String, Set<ParticipantInfo>> participantMap = ParticipantWorklistCacheManager.getInstance()
            .getWorklistParticipants();
      List<WorklistParticipantDTO> rootUserObjectList = new ArrayList<WorklistParticipantDTO>();
      for (Entry<String, Set<ParticipantInfo>> entry : participantMap.entrySet())
      {
         WorklistParticipantDTO tempRootNode = null;
         WorklistParticipantDTO assemblyLineNode = null;
         Set<ParticipantInfo> participants = entry.getValue();
         boolean assemblyNodeCreated = false;
         if (null == assemblyLineNode)
         {
            assemblyLineNode = initAssemblyLineChild();
         }
         for (ParticipantInfo participantInfo : participants)
         {
            if (participantInfo.getQualifiedId().equals(entry.getKey()) && (participantInfo instanceof UserInfo))
            {
               tempRootNode = addParentNode(participantInfo, showEmptyWorklist);
            }

            if (isAssemblyLineMode() && getAssemblyLineParticipants().contains(participantInfo.getId()))
            {
               if (!assemblyNodeCreated)
               {
                  assemblyLineNode = addAssemblyLineChild(showEmptyWorklist, tempRootNode, assemblyLineNode);
                  assemblyNodeCreated = true;             
               }
              
               continue;
            }
            WorklistParticipantDTO childNode = addChild(participantInfo, true, tempRootNode, showEmptyWorklist);
            if (null != childNode)
            {
               if (entry.getKey().equals(participantInfo.getQualifiedId()) && (participantInfo instanceof UserInfo))
               {
                  childNode.name = restCommonClientMessages.getString("launchPanels.worklists.personalWorklist");
               }
            }
         }

         if (tempRootNode != null)
         {
            tempRootNode.activityCount = getTotalActivityCountForParentNode(tempRootNode);
            rootUserObjectList.add(tempRootNode);
         }
      }
      return rootUserObjectList;
   }

   private long getTotalActivityCountForParentNode(WorklistParticipantDTO tempRootNode)
   {
      List<WorklistParticipantDTO> childObjects = tempRootNode.children;
      long count = 0;
      for (WorklistParticipantDTO childObject : childObjects)
      {
         count = count + childObject.activityCount;
      }
      return count;
   }

   private WorklistParticipantDTO addChild(ParticipantInfo participantInfo, boolean isLeaf,
         WorklistParticipantDTO tempRootNode, boolean showEmptyWorklist)

   {
      String userParticipantId = tempRootNode.userId;

      if (showEmptyWorklist
            || (ParticipantWorklistCacheManager.getInstance().getWorklistCount(participantInfo, userParticipantId) > 0 || !isLeaf))
      {
         String labelName = null;
         ParticipantLabel label = ModelHelper.getParticipantLabel(participantInfo);
         String viewKey = ParticipantUtils.getWorklistViewKey(participantInfo);
         WorklistParticipantDTO child = new WorklistParticipantDTO();

         if (userParticipantId.equals(participantInfo.getQualifiedId()))
         {

            String personalLabel = restCommonClientMessages
                  .getParamString("views.worklistPanel.label.personalWorklist");
            child.userId = userParticipantId;
            labelName = personalLabel + " - " + label.getLabel();

         }
         else
         {
            if (participantInfo instanceof OrganizationInfo
                  && null != ((OrganizationInfo) participantInfo).getDepartment())
            {
               OrganizationInfo organization = (OrganizationInfo) participantInfo;
               child.participantQId = participantInfo.getQualifiedId() + organization.getDepartment().getId();
            }
            else
            {
               child.participantQId = participantInfo.getQualifiedId();
            }
            child.userId = userParticipantId;
            labelName = label.getLabel();
         }

         child.name = label.getLabel();
         child.icon = getParticipantIcon(participantInfo);
         child.id = participantInfo.getQualifiedId();
         child.activityCount = getActivityCount(participantInfo, userParticipantId);
         child.labelName = labelName;
         child.viewKey = viewKey;
         tempRootNode.children.add(child);
         return child;
      }
      return null;
   }

   /**
    * 
    * @param showEmptyWorklist
    * @param tempRootNode
    * @param assemblyLineNode
    * @return
    */
   private WorklistParticipantDTO addAssemblyLineChild(Boolean showEmptyWorklist, WorklistParticipantDTO tempRootNode,
         WorklistParticipantDTO assemblyLineNode)
   {

      if (showEmptyWorklist || assemblyLineNode.activityCount > 0)
      {
         tempRootNode.children.add(assemblyLineNode);
      }

      return assemblyLineNode;
   }

   private WorklistParticipantDTO addParentNode(ParticipantInfo participantInfo, boolean showEmptyWorklist)
   {
      String userParticipantId = participantInfo.getQualifiedId();
      WorklistParticipantDTO parent = null;
      parent = new WorklistParticipantDTO();
      ParticipantLabel label = ModelHelper.getParticipantLabel(participantInfo);
      parent.name = label.getLabel();
      parent.icon = getParticipantIcon(participantInfo);
      parent.id = userParticipantId;
      parent.children = new ArrayList<WorklistParticipantDTO>();

      String viewKey = ParticipantUtils.getWorklistViewKey(participantInfo);
      viewKey = viewKey + participantInfo.getQualifiedId();
      parent.viewKey = viewKey;
      parent.userId = userParticipantId;

      String unifiedLabel = restCommonClientMessages.getParamString("views.worklistPanel.label.unifiedWorklist");
      String labelName = unifiedLabel + " - " + label.getLabel();
      parent.labelName = labelName;
      return parent;
   }

   /**
    * @param participantInfo
    * @return
    */
   public static String getParticipantIcon(ParticipantInfo participantInfo)
   {
      String iconPath = "";

      switch (ParticipantUtils.getParticipantType(participantInfo))
      {
         case ORGANIZATION:
            iconPath = Resources.Icons.getOrganization();
            break;

         case ROLE:
            iconPath = Resources.Icons.getRole();
            break;

         case SCOPED_ORGANIZATION:
            iconPath = Resources.Icons.getScopedOrganization();
            break;

         case SCOPED_ROLE:
            iconPath = Resources.Icons.getScopedRole();
            break;

         case USER:
            if (participantInfo.getQualifiedId().equals(SessionContext.findSessionContext().getUser().getQualifiedId()))
            {
               iconPath = MyPicturePreferenceUtils.getLoggedInUsersImageURI();
            }
            else
            {
               UserInfo userInfo = (UserInfo) participantInfo;
               User user = UserUtils.getUser(userInfo.getId());
               iconPath = MyPicturePreferenceUtils.getUsersImageURI(user);
            }

            break;

         case USERGROUP:
            iconPath = Resources.Icons.getUserGroup();
            break;
      }

      return iconPath;
   }

   public Long getActivityCount(ParticipantInfo participantInfo, String userParticipantId)
   {
      Long totalCount = ParticipantWorklistCacheManager.getInstance().getWorklistCount(participantInfo,
            userParticipantId);
      Long totalCountThreshold = ParticipantWorklistCacheManager.getInstance().getWorklistCountThreshold(
            participantInfo, userParticipantId);
      if (totalCount < Long.MAX_VALUE)
         return totalCount;
      else
         return totalCountThreshold;
   }

   private WorklistParticipantDTO initAssemblyLineChild() throws PortalException
   {
      WorklistParticipantDTO assemblyLineParticipant = new WorklistParticipantDTO();
      assemblyLineParticipant.name = restCommonClientMessages.getString("launchPanels.worklists.assemblyLine.title");
      assemblyLineParticipant.activityCount = calculateActivityCount();
      assemblyLineParticipant.icon = "pi pi-assembly-line pi-lg";
      assemblyLineParticipant.isAssemblyLineParticipant = true;
      return assemblyLineParticipant;
   }

   public long calculateActivityCount() throws PortalException
   {
      long activityCount = 0;

      if (isAssemblyLineMode() && null != getAssemblyLineActivityProvider())
      {
         activityCount = getAssemblyLineActivityProvider().getAssemblyLineActivityCount(
               ServiceFactoryUtils.getProcessExecutionPortal(), getAssemblyLineParticipants());
      }
      return activityCount;

   }

   public Boolean isAssemblyLineMode()
   {
      return CollectionUtils.isNotEmpty(getAssemblyLineParticipants()) ? true : false;

   }

   /**
    * 
    * @return
    */
   public Set<String> getAssemblyLineParticipants()
   {
      Set<String> assemblyLineParticipants = ParticipantUtils.categorizeParticipants(
            SessionContext.findSessionContext().getUser()).getAssemblyLineParticipants();
      return assemblyLineParticipants;
   }

   private IAssemblyLineActivityProvider getAssemblyLineActivityProvider()
   {
      IAssemblyLineActivityProvider assemblyLineActivityProvider = null;

      String assemblyLineProvider = (String) Parameters.instance().get(SpiConstants.ASSEMBLY_LINE_ACTIVITY_PROVIDER);
      if (StringUtils.isNotEmpty(assemblyLineProvider))
      {
         Object instance = ReflectionUtils.createInstance(assemblyLineProvider);
         if (instance instanceof IAssemblyLineActivityProvider)
         {
            assemblyLineActivityProvider = ((IAssemblyLineActivityProvider) instance);
         }
         else
         {
            throw new PublicException(
                  "Assembly Line Provider is not an instance of org.eclipse.stardust.ui.web.processportal.spi.IAssemblyLineActivityProvider");
         }
      }
      else
      {
         assemblyLineActivityProvider = new DefaultAssemblyLineActivityProvider();
         trace.info("Using DefaultAssemblyLineActivityProvider...");
      }
      return assemblyLineActivityProvider;
   }

   public ActivityInstanceDTO getNextAssemblyLineActivity() throws PortalException
   {
      if (null != getAssemblyLineActivityProvider())
      {
         ActivityInstance ai = getAssemblyLineActivityProvider().getNextAssemblyLineActivity(
               serviceFactoryUtils.getProcessExecutionPortal(), getAssemblyLineParticipants());
         ActivityInstanceDTO dto = DTOBuilder.build(ai, ActivityInstanceDTO.class);
         dto.activatable = org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isActivatable(ai);
         dto.defaultCaseActivity = org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils
               .isDefaultCaseActivity(ai);
         return dto;
      }
      return null;
   }
   
   /**
    * 
    * @return
    */
   public List<MyProcessDTO> getUserProcesses(){
      List<MyProcessDTO> items = CollectionUtils.newArrayList();
      Set<ProcessDefinition> processDefs = ProcessWorklistCacheManager.getInstance().getProcesses();
      for (ProcessDefinition processDefinition : processDefs)
      {   MyProcessDTO myProcessDTO = new MyProcessDTO();
          myProcessDTO.id = processDefinition.getQualifiedId();
          myProcessDTO.name = I18nUtils.getProcessName(processDefinition);
          myProcessDTO.totalCount = getTotalCount(processDefinition);
         items.add(myProcessDTO);         
      }
      return items;
   }
   /**
    * 
    * @param processDefinition
    * @return
    */
   private String getTotalCount(ProcessDefinition processDefinition)
   {
      Long totalCount = ProcessWorklistCacheManager.getInstance().getWorklistCount(processDefinition);
      Long totalCountThreshold = ProcessWorklistCacheManager.getInstance().getWorklistCountThreshold(processDefinition);
      if (totalCount <= totalCountThreshold)
         return totalCount.toString();
      else
         return restCommonClientMessages.getParamString("common.notification.worklistCountThreshold",
               totalCountThreshold.toString());
   }
}
