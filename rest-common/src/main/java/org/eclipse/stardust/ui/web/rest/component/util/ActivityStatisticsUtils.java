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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.api.dto.RoleInfoDetails;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.ModelParticipantInfo;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.QualifiedRoleInfo;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.ParticipantAssociationFilter;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.runtime.Department;
import org.eclipse.stardust.engine.api.runtime.DeployedModel;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.ProcessInstancePriority;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.core.query.statistics.api.CriticalExecutionTimePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.DateRange;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatistics.PostponedActivities;
import org.eclipse.stardust.engine.core.query.statistics.api.PostponedActivitiesStatisticsQuery;
import org.eclipse.stardust.engine.core.query.statistics.api.StatisticsDateRangePolicy;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.Contribution;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatistics.PerformanceStatistics;
import org.eclipse.stardust.engine.core.query.statistics.api.UserPerformanceStatisticsQuery;
import org.eclipse.stardust.ui.web.bcc.WorkflowFacade;
import org.eclipse.stardust.ui.web.bcc.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.bcc.jsf.PostponedActivitiesCalculator;
import org.eclipse.stardust.ui.web.bcc.jsf.RoleItem;
import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.dto.CompletedActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.dto.CompletedActivityPerformanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.PostponedActivitiesResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.PostponedActivitiesStatsDTO;
import org.eclipse.stardust.ui.web.rest.dto.TeamleaderDTO;
import org.eclipse.stardust.ui.web.rest.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.viewscommon.common.Constants;
import org.eclipse.stardust.ui.web.viewscommon.common.ModelHelper;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ModelCache;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Johnson.Quadras
 *
 */
@Component
public class ActivityStatisticsUtils
{
   private static final Logger trace = LogManager.getLogger(ActivityStatisticsUtils.class);

   /**
    * 
    * @return
    */
   public List<PostponedActivitiesResultDTO> getForPostponedActivities()
   {

      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();

      Set<ModelParticipantInfo> participantList = geUsertRelevantModelParticipants();

      User user = facade.getUser();

      long totalCount, exceededDurationCount;
      Set<Long> allActivityOids = CollectionUtils.newHashSet();
      Set<Long> exceededActivityOids = CollectionUtils.newHashSet();
      String avgDuration;
      PostponedActivitiesStatistics pStat = getUserStatsForPostponedActivities();
      Users users = getRelevantUsers();
      List<UserItem> userItems = facade.getAllUsersAsUserItems(users);

      List<PostponedActivitiesResultDTO> resultList = new ArrayList<PostponedActivitiesResultDTO>();

      for (UserItem userItem : userItems)
      {
         user = userItem.getUser();

         PostponedActivities pActivities = pStat != null
               ? pStat.getPostponedActivities(userItem.getUser().getOID())
               : null;

         Collection<PostponedActivities> list = pStat.getPostponedActivities();
         for (PostponedActivities postponedActivities : list)
         {
            if (userItem.getUser().getOID() == postponedActivities.userOid)
            {
               pActivities = postponedActivities;
            }
         }
         Map<String, PostponedActivitiesStatsDTO> statsByParticipant = new HashMap<String, PostponedActivitiesStatsDTO>();
         if (pActivities != null)
         {
            PostponedActivitiesCalculator calc = new PostponedActivitiesCalculator(pActivities);
            for (ModelParticipantInfo mp : participantList)
            {
               if (calc != null)
               {
                  if (calc.getTotalCount(mp) != null && calc.getExceededDurationCount(mp) != null)
                  {
                     totalCount = calc.getTotalCount(mp);
                     avgDuration = calc.getAvgDuration(mp);
                     exceededDurationCount = calc.getExceededDurationCount(mp);
                     allActivityOids = calc.getAllActivityOIDs(mp);
                     exceededActivityOids = calc.getExceededActivityOIDs(mp);
                     PostponedActivitiesStatsDTO statsDTO = new PostponedActivitiesStatsDTO(totalCount, avgDuration,
                           exceededDurationCount, allActivityOids, exceededActivityOids);
                     statsByParticipant.put(ModelHelper.getParticipantName(mp), statsDTO);
                  }
                  else
                  {
                     PostponedActivitiesStatsDTO statsDTO = new PostponedActivitiesStatsDTO(0, StringUtils.EMPTY, 0,
                           allActivityOids, exceededActivityOids);
                     statsByParticipant.put(ModelHelper.getParticipantName(mp), statsDTO);
                  }
               }
            }
         }
         else
         {
            for (ModelParticipantInfo mp : participantList)
            {
               totalCount = 0;
               avgDuration = "";
               exceededDurationCount = 0;
               allActivityOids = CollectionUtils.newHashSet();
               exceededActivityOids = CollectionUtils.newHashSet();
               PostponedActivitiesStatsDTO statsDTO = new PostponedActivitiesStatsDTO(totalCount, avgDuration,
                     exceededDurationCount, allActivityOids, exceededActivityOids);
               statsByParticipant.put(ModelHelper.getParticipantName(mp), statsDTO);
            }

         }
         PostponedActivitiesResultDTO resultDTO = new PostponedActivitiesResultDTO();
         UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
         userDTO.displayName = UserUtils.getUserDisplayLabel(user);
         resultDTO.teamMember = userDTO;
         resultDTO.statsByParticipant = statsByParticipant;
         resultList.add(resultDTO);
      }
      return resultList;
   }

   /**
    * 
    * @return
    */
   public List<CompletedActivitiesStatisticsDTO> getPerformanceStatsByTeamLead()
   {
      trace.debug("Getting statistics for completed activities by team leader");

      UserQuery query = UserQuery.findActive();
      query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Full));
      FilterTerm filter = query.getFilter().addOrTerm();

      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      Collection<DeployedModel> deployedModels = ModelCache.findModelCache()
            .getAllModels();
      UserPerformanceStatistics userStatistics = getUserStatistics();
      List<ProcessDefinition> processes = ProcessDefinitionUtils
            .getAllBusinessRelevantProcesses();

      Map<String, List<Participant>> teamMap = CollectionUtils.newHashMap();
      Map<String, QualifiedRoleInfo> roleInfoMap = CollectionUtils.newHashMap();
      List<CompletedActivitiesStatisticsDTO> resultList = new ArrayList<CompletedActivitiesStatisticsDTO>();
      // Create team map Team
      for (Object leadRole : facade.getTeamleadRoles())
      {
         QualifiedRoleInfo teamLeadRoleInfo = (QualifiedRoleInfo) leadRole;

         String participantKey = ParticipantUtils.getParticipantUniqueKey(teamLeadRoleInfo);
         roleInfoMap.put(participantKey, teamLeadRoleInfo);

         List<Participant> teams = teamMap.get(participantKey);
         if (teams == null)
         {
            teams = CollectionUtils.newArrayList();
            teamMap.put(participantKey, teams);
         }
         filter.add(ParticipantAssociationFilter.forParticipant(teamLeadRoleInfo));

         Role teamLeadRole = getRole(teamLeadRoleInfo, deployedModels);

         if (null != teamLeadRole)
         {
            for (Object teamIter : teamLeadRole.getTeams())
            {
               Participant teamMember = (Participant) teamIter;
               teams.add(teamMember);
            }
         }
      }

      if (teamMap.size() > 0)
      {
         if (query.getOrderCriteria().getCriteria().size() == 0)
         {
            query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME)
                  .and(UserQuery.ACCOUNT);
         }
         Users teamleader = facade.getAllUsers((UserQuery) query);
         List<UserItem> userItems = facade.getAllUsersAsUserItems(teamleader);

         // iterate team lead
         for (UserItem userItem : userItems)
         {
            User user = userItem.getUser();
            for (Grant grant : user.getAllGrants())
            {

               List<Participant> teams = teamMap.get(ParticipantUtils
                     .getGrantUniqueKey(grant));
               // Grant key and Participant key is suppose to same
               QualifiedRoleInfo roleInfo = roleInfoMap.get(ParticipantUtils
                     .getGrantUniqueKey(grant));

               // find role from model
               if (null == roleInfo)
               {
                  roleInfo = findRole(grant);
               }
               // create row for each Organization(as same user can be leader in many
               // Organizations)
               if (teams != null && null != roleInfo)
               {
                  List<Organization> orgs = findOrganizations(teams);
                  for (Organization org : orgs)
                  {
                     TeamleaderDTO tl = new TeamleaderDTO(userItem, roleInfo, org);
                     resultList.add(computeStatistics(tl, processes, userStatistics));
                  }
               }
            }
         }
      }

      return resultList;
   }
   
   /**
    * 
    * @return
    */
   private CompletedActivitiesStatisticsDTO computeStatistics(TeamleaderDTO tl,
         List<ProcessDefinition> processes, UserPerformanceStatistics userStatistics)
   {
      Map<String, CompletedActivityPerformanceDTO> statsByProcess = CollectionUtils
            .newHashMap();
      List<User> teamMembers = getTeamMembers(tl);

      for (User user : teamMembers)
      {
         setCompletedProcessStatisticsForUser(user, statsByProcess, processes,
               userStatistics);
      }

      // In case of empty
      if (CollectionUtils.isEmpty(statsByProcess.keySet()))
      {
         for (ProcessDefinition process : processes)
         {
            statsByProcess.put(I18nUtils.getProcessName(process),
                  new CompletedActivityPerformanceDTO(0, 0, 0));
         }
      }

      return buildResult(tl, statsByProcess);
   }

   /**
    * 
    */
   private CompletedActivitiesStatisticsDTO buildResult(TeamleaderDTO tl,
         Map<String, CompletedActivityPerformanceDTO> statsByProcess)
   {
      CompletedActivitiesStatisticsDTO resultDTO = new CompletedActivitiesStatisticsDTO();
      resultDTO.statisticsByProcess = statsByProcess;
      UserDTO userDTO = DTOBuilder.build(tl.user.getUser(), UserDTO.class);
      resultDTO.teamMember = userDTO;
      resultDTO.displayName = I18nUtils.getUserLabel(tl.user.getUser()) + " ("
            + tl.teamName + ")";
      return resultDTO;
   }

   /**
    * @return
    * 
    */
   @SuppressWarnings("rawtypes")
   public List<CompletedActivitiesStatisticsDTO> getForCompletedActivies()
   {

      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      Users users = getRelevantUsers();
      UserPerformanceStatistics userStatistics = getUserStatistics();
      Iterator<UserItem> userIter = facade.getAllUsersAsUserItems(users).iterator();
    
      Collection participants = facade.getAllRolesExceptCasePerformer();
      UserItem userItem;
      List<ProcessDefinition> processes = ProcessDefinitionUtils.getAllBusinessRelevantProcesses();

      ProcessDefinition process;
      PerformanceStatistics pStatistics;
      Contribution con = null;
      RoleItem roleItem;

      List<CompletedActivitiesStatisticsDTO> completedActivitiesList = new ArrayList<CompletedActivitiesStatisticsDTO>();

      while (userIter.hasNext())
      {
         userItem = (UserItem) userIter.next();
         CompletedActivitiesStatisticsDTO activityStatsDTO = new CompletedActivitiesStatisticsDTO();

         UserDTO userDTO = DTOBuilder.build(userItem.getUser(), UserDTO.class);
         userDTO.displayName = UserUtils.getUserDisplayLabel(userItem.getUser());
         activityStatsDTO.teamMember = userDTO;
         activityStatsDTO.displayName = userDTO.displayName;
         CompletedActivityPerformanceDTO performanceStatsDTO = null;

         Map<String, CompletedActivityPerformanceDTO> processStats = new HashMap<String, CompletedActivityPerformanceDTO>();
         if (processes != null)
         {
            for (int i = 0; i < processes.size(); i++)
            {
               process = (ProcessDefinition) processes.get(i);
               pStatistics = userStatistics != null ? userStatistics.getStatisticsForUserAndProcess(userItem.getUser()
                     .getOID(), process.getQualifiedId()) : null;

               int nAisCompletedToday = 0;
               int nAisCompletedWeek = 0;
               int nAisCompletedMonth = 0;

               if (pStatistics != null)
               {
                  for (Iterator<Participant> iter = participants.iterator(); iter.hasNext();)
                  {
                     roleItem = (RoleItem) iter.next();
                     con = pStatistics.findContribution(roleItem.getRole());
                     nAisCompletedToday += con.getOrCreatePerformanceInInterval(DateRange.TODAY).getnAisCompleted();
                     nAisCompletedWeek += con.getOrCreatePerformanceInInterval(DateRange.THIS_WEEK).getnAisCompleted();
                     nAisCompletedMonth += con.getOrCreatePerformanceInInterval(DateRange.THIS_MONTH)
                           .getnAisCompleted();
                  }
               }

               performanceStatsDTO = new CompletedActivityPerformanceDTO(nAisCompletedToday, nAisCompletedWeek,
                     nAisCompletedMonth);
               processStats.put(I18nUtils.getProcessName(process), performanceStatsDTO);
            }

            activityStatsDTO.statisticsByProcess = processStats;
         }

         completedActivitiesList.add(activityStatsDTO);
      }

      return completedActivitiesList;
   }

   /**
    * 
    * @return
    */
   private UserPerformanceStatistics getUserStatistics()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();

      List<DateRange> dateRange = CollectionUtils.newArrayList();
      dateRange.add(DateRange.TODAY);
      dateRange.add(DateRange.THIS_WEEK);
      dateRange.add(DateRange.THIS_MONTH);

      UserPerformanceStatisticsQuery userPerformanceStatisticsQuery = UserPerformanceStatisticsQuery.forAllUsers();
      userPerformanceStatisticsQuery.setPolicy(new StatisticsDateRangePolicy(dateRange));

      UserPerformanceStatistics userStatistics = (UserPerformanceStatistics) facade
            .getAllUsers(userPerformanceStatisticsQuery);

      return userStatistics;
   }

   /**
    * 
    * @param teamLeadRoleInfo
    * @return
    */
   private Role getRole(QualifiedRoleInfo qRoleInfo, Collection<DeployedModel> deployedModels)
   {
      if (qRoleInfo instanceof Role)
      {
         return (Role) qRoleInfo;
      }
      else
      {
         RoleInfoDetails roleInfo = (RoleInfoDetails) qRoleInfo;
         for (DeployedModel model : deployedModels)
         {
            String modelId = org.eclipse.stardust.ui.web.viewscommon.utils.ModelUtils.extractModelId(roleInfo
                  .getQualifiedId());
            if (model.getId().equals(modelId))
            {
               Role teamLeadRole = (Role) model.getParticipant(roleInfo.getId());
               if (teamLeadRole.getRuntimeElementOID() == roleInfo.getRuntimeElementOID())
               {
                  return teamLeadRole;
               }
            }
         }
      }
      return null;
   }

   /**
    * Calculates the complete activity statistics of User for different process
    * definitions it belongs
    * 
    * @param pStatistics
    * @param user
    * @param activityStatisticsList
    */
   private void setCompletedProcessStatisticsForUser(User user,
         Map<String, CompletedActivityPerformanceDTO> statsByProcess, List<ProcessDefinition> processes, UserPerformanceStatistics userStatistics)
   {
    
      if (userStatistics != null && processes != null)
      {
         for (ProcessDefinition process : processes)
         {
            PerformanceStatistics performanceStatistics = userStatistics.getStatisticsForUserAndProcess(user.getOID(),
                  process.getQualifiedId());

            CompletedActivityPerformanceDTO pStat = statsByProcess.get(I18nUtils.getProcessName(process));
            if (pStat == null)
            {
               pStat = new CompletedActivityPerformanceDTO();
               statsByProcess.put(I18nUtils.getProcessName(process), pStat);
            }

            if (performanceStatistics != null)
            {
               List contributions = performanceStatistics.contributions;
               for (int i = 0; i < contributions.size(); ++i)
               {
                  Contribution con = (Contribution) contributions.get(i);
                  pStat.day += con.getOrCreatePerformanceInInterval(DateRange.TODAY).getnAisCompleted();
                  pStat.week += con.getOrCreatePerformanceInInterval(DateRange.THIS_WEEK).getnAisCompleted();
                  pStat.month += con.getOrCreatePerformanceInInterval(DateRange.THIS_MONTH).getnAisCompleted();
               }
            }
         }
      }
   }

   /**
    * 
    * @return
    */
   private PostponedActivitiesStatistics getUserStatsForPostponedActivities()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      PostponedActivitiesStatisticsQuery query = PostponedActivitiesStatisticsQuery.forAllUsers();
      query.setPolicy(new CriticalExecutionTimePolicy(Constants.getCriticalDurationThreshold(
            ProcessInstancePriority.LOW, 1.0f), Constants.getCriticalDurationThreshold(ProcessInstancePriority.NORMAL,
            1.0f), Constants.getCriticalDurationThreshold(ProcessInstancePriority.HIGH, 1.0f)));

      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }
      PostponedActivitiesStatistics pStat = (PostponedActivitiesStatistics) facade.getAllUsers(query);
      return pStat;
   }

   /**
    * 
    * @param grant
    * @return
    */
   private Role findRole(Grant grant)
   {
      ModelCache modelCache = ModelCache.findModelCache();
      Model model = modelCache.getActiveModel(grant);
      Participant p = model != null ? model.getParticipant(grant.getId()) : null;
      if (p instanceof Role)
      {
         return (Role) p;
      }
      return null;
   }

   /**
    * 
    * @param teams
    * @return
    */
   private List<Organization> findOrganizations(List<Participant> teams)
   {
      List<Organization> orgs = CollectionUtils.newArrayList();
      for (Participant participant : teams)
      {
         if (participant instanceof Organization)
         {
            orgs.add((Organization) participant);
         }
      }
      return orgs;
   }

   /**
    * As per the TeamLeader returns the uesrs within
    * 
    * @param tl
    * @return list of teamLeadersRole
    */
   private List<User> getTeamMembers(TeamleaderDTO tl)
   {
      UserQuery query = UserQuery.findAll();
      query.getFilter().add(ParticipantAssociationFilter.forTeamLeader(tl.teamleaderRole));
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      return facade.getAllUsers(query);
   }

   /**
    * 
    * @return
    */
   private Users getRelevantUsers()
   {

      UserQuery query = WorkflowFacade.getWorkflowFacade().getTeamQuery(true);
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(UserPreferencesEntries.M_ADMIN_PORTAL);
      query.setPolicy(userPolicy);

      if (query.getOrderCriteria().getCriteria().size() == 0)
      {
         query.orderBy(UserQuery.LAST_NAME).and(UserQuery.FIRST_NAME).and(UserQuery.ACCOUNT);
      }

      Users users = facade.getAllUsers((UserQuery) query);

      return users;
   }
   /**
    * 
    */
   private Set<ModelParticipantInfo> geUsertRelevantModelParticipants()
   {
      WorkflowFacade facade = WorkflowFacade.getWorkflowFacade();
      User user = facade.getUser();
      List<Grant> userGrants = user.getAllGrants();
      ModelParticipantInfo modelParticipantInfo;
      Participant participant;
      ModelParticipant modelParticipant;
      Department department;

      Set<ModelParticipantInfo> participantList = new HashSet<ModelParticipantInfo>();
      for (Grant grant : userGrants)
      {
         participant = facade.getParticipant(grant.getQualifiedId());
         if (participant instanceof ModelParticipant)
         {
            modelParticipant = (ModelParticipant) participant;
            department = grant.getDepartment();
            modelParticipantInfo = (department == null) ? modelParticipant : department
                  .getScopedParticipant(modelParticipant);
            participantList.add(modelParticipantInfo);
         }
      }
      return participantList;
   }
}
