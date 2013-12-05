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
package org.eclipse.stardust.ui.web.processportal.common;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.InternalException;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.UserDetailsLevel;
import org.eclipse.stardust.engine.api.model.Activity;
import org.eclipse.stardust.engine.api.model.Participant;
import org.eclipse.stardust.engine.api.model.ParticipantInfo;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.FilterAndTerm;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.FilterTerm;
import org.eclipse.stardust.engine.api.query.PerformedByUserFilter;
import org.eclipse.stardust.engine.api.query.PerformingOnBehalfOfFilter;
import org.eclipse.stardust.engine.api.query.PerformingParticipantFilter;
import org.eclipse.stardust.engine.api.query.PerformingUserFilter;
import org.eclipse.stardust.engine.api.query.Query;
import org.eclipse.stardust.engine.api.query.SubsetPolicy;
import org.eclipse.stardust.engine.api.query.UserDetailsPolicy;
import org.eclipse.stardust.engine.api.query.UserQuery;
import org.eclipse.stardust.engine.api.query.Users;
import org.eclipse.stardust.engine.api.query.Worklist;
import org.eclipse.stardust.engine.api.query.WorklistQuery;
import org.eclipse.stardust.engine.api.runtime.ActivityCompletionLog;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserInfo;
import org.eclipse.stardust.engine.api.runtime.WorkflowService;
import org.eclipse.stardust.ui.client.model.ProcessFilter;
import org.eclipse.stardust.ui.client.model.ProcessFilters;
import org.eclipse.stardust.ui.event.ActivityEvent;
import org.eclipse.stardust.ui.event.WorklistSelectionEvent;
import org.eclipse.stardust.ui.web.common.app.PortalApplication;
import org.eclipse.stardust.ui.web.common.util.MessagePropertiesBean;
import org.eclipse.stardust.ui.web.viewscommon.beans.SessionContext;
import org.eclipse.stardust.ui.web.viewscommon.common.configuration.UserPreferencesEntries;
import org.eclipse.stardust.ui.web.viewscommon.common.spi.IFilterProvider;
import org.eclipse.stardust.ui.web.viewscommon.utils.ClientContextBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.ExceptionHandler;
import org.eclipse.stardust.ui.web.viewscommon.utils.FilterProviderUtil;
import org.eclipse.stardust.ui.web.viewscommon.utils.MyPicturePreferenceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ParticipantWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.SpecialWorklistCacheManager;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.WorklistUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ResubmissionUtils.ModelResubmissionActivity;



/**
 * Contains the extracted API calls from TDS and additional new Queries needed for the
 * WorkflowExecution Perspective LaunchPanels
 * 
 * @author roland.stamm
 * 
 */
public class PPUtils
{
   private static final Logger trace = LogManager.getLogger(PPUtils.class);
   private static final String WORKLIST_VIEW_ID = "worklistPanel";//Note:this can be moved to any util class in process-portal

   private PPUtils()
   {}

   public static enum CompletionOptions {
      ACTIVATE_NEXT, NO_ACTIVATION
   }

   /**
    * @param viewKey
    * @param params
    */
   public static void openWorklistView(String viewKey, Map<String, Object> params)
   {
      PortalApplication.getInstance().openViewById(WORKLIST_VIEW_ID, viewKey, params, null, false);
   }

   /**
    * @param firstName
    * @param lastName
    * @return
    */
   public static Users getUsers_anyLike(String firstName, String lastName)
   {
      UserQuery query = UserQuery.findAll();
      String[] prefModules = {UserPreferencesEntries.M_ADMIN_PORTAL};
      UserDetailsPolicy userPolicy = new UserDetailsPolicy(UserDetailsLevel.Core);
      userPolicy.setPreferenceModules(prefModules);
      query.setPolicy(userPolicy);

      FilterAndTerm and = query.getFilter().addAndTerm();
      if (!isEmpty(firstName))
      {
         String first = firstName.replace('*', '%');
         first = "%" + first + "%";
         and.add(UserQuery.FIRST_NAME.like(first));
      }
      if (!isEmpty(lastName))
      {
         String last = lastName.replace('*', '%');
         last = "%" + last + "%";
         and.add(UserQuery.LAST_NAME.like(last));
      }
      return ServiceFactoryUtils.getQueryService().getAllUsers(query);

   }

   /**
    * Fetches a List of Worklists. This query do not retrieve any ActivityInstance, only
    * counts them. The query includes contributions from the current user.
    * 
    * @return
    */
   public static List<Worklist> getWorklist_onlyUser()
   {
      SubsetPolicy policy = new SubsetPolicy(0, true);
      WorklistQuery query = new WorklistQuery();
      query.setUserContribution(policy);
      // query.setParticipantContribution(PerformingParticipantFilter.ANY_FOR_USER,
      // policy);

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
    * Fetches a UserWorklist where LAST_MODIFICATION_TIME is >= the parameter date. This
    * query do not retrieve any ActivityInstance, only counts them. The query includes
    * contributions from the current user.
    * 
    * @param date
    * @return
    */
   public static List<Worklist> getWorklist_lastNForUser(Date date)
   {
      // TODO change to StatisticQuery+ ActivityInstanceQuery?
      SubsetPolicy policy = new SubsetPolicy(0, true);
      WorklistQuery query = new WorklistQuery();
      query.setUserContribution(policy);
      // query.setParticipantContribution(PerformingParticipantFilter.ANY_FOR_USER,
      // policy);
      // TODO correct query to search through Activity History (completed, aborted ect.)
      query.where(WorklistQuery.LAST_MODIFICATION_TIME.greaterOrEqual(date.getTime()));

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
    * @param worklist
    * @param participantInfo
    */
   public static Worklist extractParticipantWorklist(Worklist worklist, ParticipantInfo participantInfo)
   {
      Worklist extractedWorklist = null;

      switch (ParticipantUtils.getParticipantType(participantInfo))
      {
      case ORGANIZATION:
      case ROLE:
      case SCOPED_ORGANIZATION:
      case SCOPED_ROLE:
      case USERGROUP:
         @SuppressWarnings("unchecked")
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
    * @param date
    * @return
    */
   public static ActivityInstances getActivityInstances_lastNPerformedForUser(Date date)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Suspended, ActivityInstanceState.Completed, ActivityInstanceState.Created,
            ActivityInstanceState.Interrupted, ActivityInstanceState.Application});
      FilterTerm where = query.getFilter().addAndTerm();
      where.add(ActivityInstanceQuery.LAST_MODIFICATION_TIME.greaterOrEqual(date.getTime()));
      where.addOrTerm().add(PerformingUserFilter.CURRENT_USER).add(PerformedByUserFilter.CURRENT_USER);

      applyFilterProviders(query);

      ActivityInstances ais = ServiceFactoryUtils.getQueryService().getAllActivityInstances(query);

      return ais;
   }

   /**
    * @param participantInfo
    * @return
    */
   public static ActivityInstances getActivityInstances_forParticipant(ParticipantInfo participantInfo)
   {
      return getActivityInstances_forParticipant(participantInfo, true);
   }

   /**
    * @param participantInfo
    * @return
    */
   public static ActivityInstances getActivityInstances_forParticipant(ParticipantInfo participantInfo,
         boolean recursively)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      query.setPolicy(new SubsetPolicy(0, true));
      query.where(PerformingOnBehalfOfFilter.forParticipant(participantInfo, recursively));

      applyFilterProviders(query);

      return ServiceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   /**
    * @return
    */
   public static ActivityInstances getActivityInstances_anyActivatable()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Hibernated, ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      // only evaluate count
      query.setPolicy(new SubsetPolicy(0, true));
      // query.where(new ProcessDefinitionFilter(process.getId(), false));
      // FilterOrTerm or = query.getFilter().addOrTerm();
      // or.add(PerformingParticipantFilter.ANY_FOR_USER).add(
      // PerformingUserFilter.CURRENT_USER);

      // handleWorkshopFilter(query, process);

      applyFilterProviders(query);

      return ServiceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   public static ActivityInstances getActivityInstances_Resubmission()
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(ActivityInstanceState.Hibernated);
      query.getFilter().add(PerformingUserFilter.CURRENT_USER);

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
            or.add(ActivityFilter.forProcess(activity.getActivityId(), activity.getProcessId(),//TODO:check FQID change
                  activity.getModelOids(), false));
         }
      }

      applyFilterProviders(query);

      return ServiceFactoryUtils.getQueryService().getAllActivityInstances((ActivityInstanceQuery) query);

   }

   /**
    * @param userOid
    * @return
    */
   public static ActivityInstances getActivityInstances_forUser(long userOid)
   {
      ActivityInstanceQuery query = ActivityInstanceQuery.findAll();
      // only evaluate count
      query.setPolicy(new SubsetPolicy(0, true));
      // query.where(new ProcessDefinitionFilter(process.getId(), false));
      FilterOrTerm or = query.getFilter().addOrTerm();
      or.add(ActivityInstanceQuery.CURRENT_USER_PERFORMER_OID.isEqual(userOid));

      // handleWorkshopFilter(query, process);

      return ServiceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   /**
    * @param n
    * @return
    */
   public static ActivityInstances getActivityInstances_nCriticalForUser(int n)
   {
      // TODO limit to N entries
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Application, ActivityInstanceState.Suspended});
      // only evaluate count
      query.setPolicy(new SubsetPolicy(0, true));
      // query.where(new ProcessDefinitionFilter(process.getId(), false));
      FilterOrTerm or = query.getFilter().addOrTerm();
      or.add(PerformingParticipantFilter.ANY_FOR_USER).add(PerformingUserFilter.CURRENT_USER);

      applyFilterProviders(query);

      // handleWorkshopFilter(query, process);
      return ServiceFactoryUtils.getQueryService().getAllActivityInstances(query);
   }

   // /**
   // * @param scope
   // * @param ai
   // */
   // public static void abort(AbortScope scope, ActivityInstance ai)
   // {
   // ActivityInstance newAi;
   // if (scope == null)
   // {
   // newAi = ServiceFactoryUtils.getWorkflowService().abortActivityInstance(ai.getOID());
   // }
   // else
   // {
   // newAi = ServiceFactoryUtils.getWorkflowService().abortActivityInstance(ai.getOID(),
   // scope);
   // }
   // sendActivityEvent(ai, ActivityEvent.aborted(newAi));
   // }

   /**
    * @param selected
    */
   public static void selectWorklist(ParticipantInfo participantInfo)
   {
      Participant participant = null;
      if (participant == null)
      {
         // using default user
         participant = ServiceFactoryUtils.getSessionContext().getUser();
      }
//      else
//      {
//         participant = ParticipantUtils.getParticipant(participantInfo);
//      }

      ClientContextBean.getCurrentInstance().getClientContext()
            .sendWorklistSelectionEvent(new WorklistSelectionEvent(participant));
      // client.getContext().getWorklistSelectionObservers().notifyObservers(
      // new ParticipantWorklistSelectionEvent(this));
   }

    /**
    * @param context
    * @param data
    * @param options
    * @param ai
    * @return
    */
   public static WorkflowActivityCompletionLog complete(String context, Map<String, ? > data,
         CompletionOptions options, ActivityInstance ai)
   {
      ActivityInstance completedAi = null;
      ActivityInstance newInstance = null;

      boolean closeViewAndProceed = true;
      boolean success = false;
      boolean delayViewClose = false;

      try
      {
         if (trace.isDebugEnabled())
         {
            trace.debug("Completing Activity '" + ai.getActivity().getName() + "' with out data = " + data);
         }

         switch (options)
         {
         case ACTIVATE_NEXT:
            ActivityCompletionLog log = ServiceFactoryUtils.getWorkflowService().complete(ai.getOID(), context, data,
                  WorkflowService.FLAG_ACTIVATE_NEXT_ACTIVITY_INSTANCE);

            completedAi = log.getCompletedActivity();
            newInstance = log.getNextForUser();
            break;

         default:
            completedAi = ServiceFactoryUtils.getWorkflowService().complete(ai.getOID(), context, data);
         }

         sendActivityEvent(ai, ActivityEvent.completed(completedAi, null != newInstance));

         if (null != newInstance)
         {
            sendActivityEvent(null, ActivityEvent.activated(newInstance));
         }

         success = true;
      }
      catch (ConcurrencyException ce)
      {
         delayViewClose = true;
         ExceptionHandler.handleException(ce,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.concurrencyError"));
      }
      catch (AccessForbiddenException af)
      {
         delayViewClose = true;
         ExceptionHandler.handleException(af,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.acccessForbiddenError"));
      }
      catch (PublicException pe)
      {
         closeViewAndProceed = false;
         ExceptionHandler.handleException(pe,
               MessagePropertiesBean.getInstance().getString("views.activityPanel.generalError"));
      }
      catch (InternalException ie)
      {
         delayViewClose = true;
         ExceptionHandler.handleException(ie);
      }
      catch (Exception e)
      {
         closeViewAndProceed = false;
         ExceptionHandler.handleException(e);
      }

      return new WorkflowActivityCompletionLog(completedAi, newInstance, success, closeViewAndProceed, delayViewClose);
   }

   /**
    * @param processDefinition
    * @param synchronous
    * @return
    */
   public static ProcessInstance startProcess(ProcessDefinition processDefinition, boolean synchronous)
   {
      WorkflowService wfs = ServiceFactoryUtils.getWorkflowService();
      return wfs.startProcess(processDefinition.getQualifiedId(), null, synchronous);
   }

   /**
    * @param processDefinition
    * @param synchronous
    * @return
    */
   public static ProcessInstance startProcess(ProcessDefinition processDefinition, Map<String, ? > data,
         boolean synchronous)
   {
      WorkflowService wfs = ServiceFactoryUtils.getWorkflowService();
      return wfs.startProcess(processDefinition.getQualifiedId(), data, synchronous);
   }

   /**
    * @param pi
    * @return
    */
   public static ActivityInstance activateNextActivityInstance(ProcessInstance pi)
   {
      ActivityInstance rtAi = ServiceFactoryUtils.getWorkflowService().activateNextActivityInstanceForProcessInstance(
            pi.getOID());
      if (rtAi != null)
      {
         sendActivityEvent(null, ActivityEvent.activated(rtAi));

         return rtAi;
      }
      return null;
   }

   /**
    * @param iterator
    * @return
    */
   public static String[] getCommonDescriptors(Iterator<ActivityInstance> iterator)
   {
      // TODO remove dependency on client.getModels() and replace with DescriptorUtils
      // from old ProcessPortal
      ArrayList<String> descriptors = calculateCommonDescriptors(iterator, ClientContextBean.getCurrentInstance()
            .getClient().getModels().getProcessFilters());
      return descriptors.toArray(new String[descriptors.size()]);
   }

   /**
    * @param itr
    * @param filters
    * @return
    */
   private static ArrayList<String> calculateCommonDescriptors(Iterator<ActivityInstance> itr, ProcessFilters filters)
   {
      HashSet<String> processedIds = new HashSet<String>();
      ArrayList<String> descriptors = new ArrayList<String>();
      boolean first = true;
      while (itr.hasNext())
      {
         ActivityInstance activityInstance = itr.next();
         String processId = activityInstance.getProcessDefinitionId();
         if (!processedIds.contains(processId))
         {
            processedIds.add(processId);
            ProcessFilter filter = filters.getFilter(processId);
            if (first)
            {
               descriptors.addAll(Arrays.asList(filter.getDescriptors()));
            }
            else
            {
               descriptors.retainAll(Arrays.asList(filter.getDescriptors()));
            }
            first = false;
         }
      }
      return descriptors;
   }

   /**
    * @param query
    * @param process
    */
   private static void handleWorkshopFilter(ActivityInstanceQuery query, ProcessDefinition process)
   {
      WorklistUtils.CategorizedActivities activities = WorklistUtils.getWorkshopActivities(process.getQualifiedId());

      if (!activities.assemblyLineActivities.isEmpty())
      {
         FilterOrTerm workshopFilter = query.getFilter().addOrTerm();
         if (!activities.workshopActivities.isEmpty())
         {
            // TODO fix purusha scenarios, i.e. activity being workshop in model A and
            // assembly line in model B
            Set<String> resolvedActivities = new HashSet<String>();
            for (Iterator<Activity> i = activities.workshopActivities.iterator(); i.hasNext();)
            {
               Activity activity = i.next();
               if (!resolvedActivities.contains(activity.getId()))
               {
                  workshopFilter.add(ActivityFilter.forProcess(activity.getId(), process.getQualifiedId()));//TODO:check FQID change
                  resolvedActivities.add(activity.getId());
               }
            }
         }
         else
         {
            // there exists no workshop activity, so force result set to be empty
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
         if(participantInfo.getQualifiedId().equals(SessionContext.findSessionContext().getUser().getQualifiedId()))
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

   /**
    * @param oldAi
    * @param activityEvent
    */
   public static void sendActivityEvent(ActivityInstance oldAi, ActivityEvent activityEvent)
   {
      ParticipantWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      if (ProcessWorklistCacheManager.isInitialized())
      {
         ProcessWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      }
      SpecialWorklistCacheManager.getInstance().handleActivityEvent(oldAi, activityEvent);
      ClientContextBean.getCurrentInstance().getClientContext().sendActivityEvent(activityEvent);
   }
}
