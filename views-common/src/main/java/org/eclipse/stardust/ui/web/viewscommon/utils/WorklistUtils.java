/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.viewscommon.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.config.Parameters;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Model;
import org.eclipse.stardust.engine.api.model.ModelParticipant;
import org.eclipse.stardust.engine.api.model.Organization;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.model.Role;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.DescriptorPolicy;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.HistoricalStatesPolicy;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.Grant;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.ProcessPortalConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.constant.TaskAssignmentConstants;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityConfigurationUtil;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterProvider;


/**
 * @author rsauer
 * @version $Revision: 53539 $
 */
public class WorklistUtils
{
   private static final Logger trace = LogManager.getLogger(WorklistUtils.class);
   
   private static User getLoginUser()
   {
      SessionContext sessionContext = SessionContext.findSessionContext();
      if(sessionContext != null)
      {
         return sessionContext.getUser();
      }
      return null;
   }
   
   public static Set<String> getAssemblyLineAssignmentParticipants()
   {
      User user = getLoginUser();
      if(user != null)
      {
         return categorizeParticipants(user).assemblyLineParticipants;
      }
      return Collections.emptySet();
   }
      

   
   public static WorklistQuery getWorkshopTasksOutlineQuery(List<Participant> participantRestrictionList)
   {
      return getWorkshopTasksQuery(participantRestrictionList, true);
   }
   
   private static void addParticipantToQuery(WorklistQuery query, 
         Map<String, Participant> participantIdRestrictionMap,
         String participantId, boolean isGroup, boolean outline)
   {
      if(participantIdRestrictionMap.isEmpty() ||
            participantIdRestrictionMap.containsKey(participantId))
      {
         PerformingParticipantFilter filter = isGroup ?
             PerformingParticipantFilter.forUserGroup(participantId)
             : PerformingParticipantFilter.forModelParticipant(participantId);

         if (outline)
         {
            query.setParticipantContribution(filter, new SubsetPolicy(0, true));
         }
         else
         {
            query.setParticipantContribution(filter);
         }
      }
   }

   private static WorklistQuery getWorkshopTasksQuery(List<Participant> participantRestrictionList, boolean outline)
   {
      User user = getLoginUser();
      CategorizedParticipants categorizedParticipants = 
         user != null ? categorizeParticipants(user) : null;
      Map<String, Participant> participantIdRestrictionMap = CollectionUtils.newMap();
      if(participantRestrictionList != null)
      {
         Iterator<Participant> iter = participantRestrictionList.iterator();
         while (iter.hasNext())
         {
            Participant p = iter.next();
            if(!participantIdRestrictionMap.containsKey(p.getId()))
            {
               participantIdRestrictionMap.put(p.getId(), p);
            }
         }
      }
      WorklistQuery query;
      if (categorizedParticipants != null 
            && !categorizedParticipants.assemblyLineParticipants.isEmpty())
      {
         query = new WorklistQuery();

         query.setUserContribution(true);
         
         for (Iterator<String> i = categorizedParticipants.workshopParticipants.iterator(); i.hasNext();)
         {
            String participantId = i.next();
            addParticipantToQuery(query, participantIdRestrictionMap, participantId, false, outline);
         }
         for (Iterator i = user.getAllGroups().iterator(); i.hasNext();)
         {
            UserGroup ug = (UserGroup) i.next();
            addParticipantToQuery(query, participantIdRestrictionMap, ug.getId(), true, outline);
         }
         
         if(outline)
         {
            query.setPolicy(DescriptorPolicy.NO_DESCRIPTORS);
            query.setPolicy(HistoricalStatesPolicy.NO_HIST_STATES);
            query.setPolicy(new UserDetailsPolicy(UserDetailsLevel.Core));
         }
      }
      else
      {
         if(participantIdRestrictionMap.isEmpty())
         {
            query = WorklistQuery.findCompleteWorklist();
            if (outline)
            {
               query.setPolicy(new SubsetPolicy(0, true));
            }
         }
         else
         {
            query = new WorklistQuery();

            query.setUserContribution(true);
            if(outline)
            {
               query.setUserContribution(new SubsetPolicy(0, true));
            }
            for (Iterator<Participant> iter = participantIdRestrictionMap.values().iterator(); iter
                  .hasNext();)
            {
               Participant participant = iter.next();
               PerformingParticipantFilter filter = null;
               if(participant instanceof ModelParticipant)
               {
                  filter = PerformingParticipantFilter.forModelParticipant(participant.getId());
               }
               else if(participant instanceof UserGroup)
               {
                  filter = PerformingParticipantFilter.forUserGroup(participant.getId());
               }

               if (outline)
               {
                  query.setParticipantContribution(filter, new SubsetPolicy(0, true));
               }
               else
               {
                  query.setParticipantContribution(filter);
               }
            }
         }
      }
      return query;
   }

   public static CategorizedParticipants categorizeParticipants(User user)
   {
      List<ModelParticipant> pendingParticipants = new LinkedList<ModelParticipant>();
      ModelCache modelCache = ModelCache.findModelCache();
      for (Iterator i = user.getAllGrants().iterator(); i.hasNext();)
      {
         Grant grant = (Grant) i.next();
         // (fh) !!! quick workaround, need to collect all models, not only the active ones
         Model model = modelCache.getActiveModel(grant);
         Participant participant = model != null ? 
               model.getParticipant(grant.getId()) : null;
         if (participant instanceof ModelParticipant)
         {
            pendingParticipants.add((ModelParticipant)participant);
         }
      }
      
      Set<ModelParticipant> resolvedParticipants = new HashSet<ModelParticipant>();
      Set<String> participants = new TreeSet<String>();
      while ( !pendingParticipants.isEmpty())
      {
         ModelParticipant participant = pendingParticipants.remove(0);
         if ( !resolvedParticipants.contains(participant))
         {
            participants.add(participant.getId());
            resolvedParticipants.add(participant);
      
            for (Iterator i = participant.getAllSuperOrganizations().iterator(); i.hasNext();)
            {
               ModelParticipant superOrg = (ModelParticipant) i.next();
               if ( !resolvedParticipants.contains(superOrg))
               {
                  pendingParticipants.add(superOrg);
               }
            }
         }
      }
      
      boolean enableAssemblyLineMode = Parameters.instance().getBoolean(
            ProcessPortalConstants.ASSEMBLY_LINE_MODE_ENABLED, true);
      
      Set<String> assemblyLineParticipants = new HashSet<String>();
      Set<String> workshopParticipants = new HashSet<String>();

      Model activeModel = modelCache.getActiveModel();
      if(activeModel != null)
      {
         for (Iterator<String> i = participants.iterator(); i.hasNext();)
         {
            String participantId = i.next();
            Participant participant = activeModel.getParticipant(participantId);
            Object mode = (null != participant) //
                  ? participant.getAllAttributes().get(
                        TaskAssignmentConstants.ASSIGNMENT_MODE)
                  : null;
            if (enableAssemblyLineMode
                  && TaskAssignmentConstants.WORK_MODE_ASSEMBLY_LINE.equals(mode))
            {
               // assembly line
               assemblyLineParticipants.add(participantId);
            }
            else
            {
               // work shop, means as usual
               workshopParticipants.add(participantId);
            }
         }
      }
      return new CategorizedParticipants(workshopParticipants, assemblyLineParticipants);
   }
   
   public static CategorizedActivities getWorkshopActivities(String processId)
   {
      SessionContext sessionCtx = SessionContext.findSessionContext();
      User user = sessionCtx != null ? sessionCtx.getUser() : null;
      CategorizedParticipants categorizedParticipants = user != null ?
            categorizeParticipants(user) : null;
      
      List<Activity> workshopActivities = CollectionUtils.newList();
      List<Activity> assemblyLineActivities = CollectionUtils.newList();
      
      ModelCache modelCache = ModelCache.findModelCache();
      for (Iterator i = modelCache.getAllModels().iterator(); i.hasNext();)
      {
         Model model = (Model) i.next();
         ProcessDefinition process = model.getProcessDefinition(processId);
         if (null != process)
         {
            for (Iterator j = process.getAllActivities().iterator(); j.hasNext();)
            {
               Activity activity = (Activity) j.next();
               if (activity.isInteractive() && categorizedParticipants != null)
               {
                  ModelParticipant defaultPerformer = activity.getDefaultPerformer();
                  if (((defaultPerformer instanceof Role) || (defaultPerformer instanceof Organization))
                        && categorizedParticipants.assemblyLineParticipants.contains(defaultPerformer.getId()))
                  {
                     assemblyLineActivities.add(activity);
                  }
                  else if (((defaultPerformer instanceof Role) || (defaultPerformer instanceof Organization))
                        && categorizedParticipants.workshopParticipants.contains(defaultPerformer.getId()))
                  {
                     workshopActivities.add(activity);
                  }
               }
            }
         }
      }
      
      return new CategorizedActivities(workshopActivities, assemblyLineActivities);
   }
   
   /**
    * Fetches a List of Worklists. This query do not retrieve any ActivityInstance, only
    * counts them. The query includes contributions from the current user and all model
    * participants (roles/organizations) granted to the user.
    * 
    * @return
    */
   public static List<Worklist> getWorklist_anyForUser()
   {
      SubsetPolicy policy = new SubsetPolicy(0, true);
      WorklistQuery query = new WorklistQuery();
      query.setUserContribution(policy);
      query.setParticipantContribution(PerformingParticipantFilter.ANY_FOR_USER, policy);

      applyFilterProviders(query);

      List<Worklist> worklists = CollectionUtils.newArrayList();
      Worklist worklist = ServiceFactoryUtils.getWorkflowService().getWorklist(query);
      worklists.add(worklist);
      @SuppressWarnings("unchecked")
      Iterator<Worklist> subworklists = worklist.getSubWorklists();
      while (subworklists.hasNext())
      {
         worklists.add(subworklists.next());
      }
      return worklists;
   }

   /**
    * @param query
    */
   public static void applyFilterProviders(Query query)
   {

      List<IFilterProvider> filterProviders = FilterProviderUtil.getInstance().getFilterProviders();

      if (trace.isDebugEnabled())
      {
         trace.debug("Applying Filter Providers = " + filterProviders.size());
      }

      for (IFilterProvider filterProvider : filterProviders)
      {
         filterProvider.applyFilter(query);
      }
   }

   /**
    * @param participantInfo
    * @return
    */
   public static WorklistQuery createWorklistQuery(ParticipantInfo participantInfo)
   {
      WorklistQuery query = new WorklistQuery();

      switch (ParticipantUtils.getParticipantType(participantInfo))
      {
      case ORGANIZATION:
      case ROLE:
      case SCOPED_ORGANIZATION:
      case SCOPED_ROLE:
      case USERGROUP:
         query.setUserContribution(false);
         query.setParticipantContribution(PerformingParticipantFilter.forParticipant(participantInfo, false));
         break;

      case USER:
         query.setUserContribution(true);
         break;
      }

      applyFilterProviders(query);

      return query;
   }

   /**
    * @param process
    * @return
    */
   public static ActivityInstances getActivityInstances_anyActivatableByProcess(ProcessDefinition process)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      // only evaluate count
      query.setPolicy(new SubsetPolicy(0, true));
      query.where(new ProcessDefinitionFilter(process.getQualifiedId(), false));
      FilterOrTerm or = query.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);

      handleWorkshopFilter(query, process);

      applyFilterProviders(query);

      return ServiceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   /**
    * returns unified list of activities assigned to user
    * 
    * @return
    */
   public static ActivityInstances getAllAssignedActivities()
   {
      ActivityInstanceQuery allAssignedActivitiesQuery = ActivityInstanceQuery.findAll();
      FilterOrTerm or = allAssignedActivitiesQuery.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);
      allAssignedActivitiesQuery.setPolicy(new SubsetPolicy(0, true));
      allAssignedActivitiesQuery.orderBy(ActivityInstanceQuery.START_TIME);

      applyFilterProviders(allAssignedActivitiesQuery);

      return ServiceFactoryUtils.getQueryService().getAllActivityInstances(allAssignedActivitiesQuery);
   }

   /**
    * returns unified list of activities which are critical assigned to user
    * 
    * @param criticality
    * @return
    */
   public static ActivityInstances getCriticalActivities(CriticalityCategory criticality)
   {
      ActivityInstanceQuery criticalActivitiesQuery = ActivityInstanceQuery.findAll();
      FilterOrTerm or = criticalActivitiesQuery.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);
      criticalActivitiesQuery.setPolicy(new SubsetPolicy(0, true));
      criticalActivitiesQuery.orderBy(ActivityInstanceQuery.START_TIME);

      criticalActivitiesQuery.where(ActivityInstanceQuery.CRITICALITY.between(
            CriticalityConfigurationUtil.getEngineCriticality(criticality.getRangeFrom()),
            CriticalityConfigurationUtil.getEngineCriticality(criticality.getRangeTo())));

      applyFilterProviders(criticalActivitiesQuery);

      return ServiceFactoryUtils.getQueryService().getAllActivityInstances(criticalActivitiesQuery);
   }
   
   /**
    * @param query
    * @param process
    */
   private static void handleWorkshopFilter(ActivityInstanceQuery query, ProcessDefinition process)
   {
      CategorizedActivities activities = getWorkshopActivities(process.getId());

      if (!activities.assemblyLineActivities.isEmpty())
      {
         FilterOrTerm workshopFilter = query.getFilter().addOrTerm();
         if (!activities.workshopActivities.isEmpty())
         {
            // TODO fix purusha scenarios, i.e. activity being workshop in
            // model A and
            // assembly line in model B
            Set<String> resolvedActivities = new HashSet<String>();
            for (Iterator<Activity> i = activities.workshopActivities.iterator(); i.hasNext();)
            {
               Activity activity = i.next();
               if (!resolvedActivities.contains(activity.getId()))
               {
                  workshopFilter.add(ActivityFilter.forProcess(activity.getQualifiedId(), process.getQualifiedId()));
                  resolvedActivities.add(activity.getId());
               }
            }
         }
         else
         {
            // there exists no workshop activity, so force result set to be
            // empty
            workshopFilter.add(ActivityInstanceQuery.ACTIVITY_OID.isNull());
         }
      }
      // try
      // {
      // PortalBackingBean.getSessionPortal().applyWorklistFilters(query);
      // }
      // catch (PortalException e)
      // {
      // // don't handle invalid session exceptions
      // }
      // WorklistDescriptorFilterProvider.applyCurrentFilter(query);
   }
   

   public static class CategorizedParticipants
   {
      public final Set<String> workshopParticipants;
      public final Set<String> assemblyLineParticipants;

      public CategorizedParticipants(Set<String> workshopParticipants,
            Set<String> assemblyLineParticipants)
      {
         this.workshopParticipants = workshopParticipants;
         this.assemblyLineParticipants = assemblyLineParticipants;
      }
   }

   public static class CategorizedActivities
   {
      public final List<Activity> workshopActivities;
      public final List<Activity> assemblyLineActivities;

      public CategorizedActivities(List<Activity> workshopActivities,
            List<Activity> assemblyLineActivities)
      {
         this.workshopActivities = workshopActivities;
         this.assemblyLineActivities = assemblyLineActivities;
      }
   }
}