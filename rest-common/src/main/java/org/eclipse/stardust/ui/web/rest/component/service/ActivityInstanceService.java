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
/**
 * @author Johnson.Quadras
 */
package org.eclipse.stardust.ui.web.rest.component.service;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ConcurrencyException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityInstances;
import org.eclipse.stardust.engine.api.query.HistoricalEventPolicy;
import org.eclipse.stardust.engine.api.query.ProcessInstanceFilter;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.ActivityInstance;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.api.runtime.BpmRuntimeError;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.QueryService;
import org.eclipse.stardust.engine.api.runtime.ScanDirection;
import org.eclipse.stardust.engine.api.runtime.TransitionOptions;
import org.eclipse.stardust.engine.api.runtime.TransitionReport;
import org.eclipse.stardust.engine.api.runtime.TransitionTarget;
import org.eclipse.stardust.engine.core.runtime.beans.AbortScope;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.exception.NotificationMapException;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityStatisticsUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils.MODE;
import org.eclipse.stardust.ui.web.rest.component.util.CriticalityUtils;
import org.eclipse.stardust.ui.web.rest.component.util.ServiceFactoryUtils;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceIntractionDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceOutDataDTO;
import org.eclipse.stardust.ui.web.rest.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.CompletedActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.dto.CriticalityDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap.NotificationDTO;
import org.eclipse.stardust.ui.web.rest.dto.PendingActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.dto.PostponedActivitiesResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.dto.TrivialManualActivityDTO;
import org.eclipse.stardust.ui.web.rest.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.builder.DocumentDTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.builder.FolderDTOBuilder;
import org.eclipse.stardust.ui.web.rest.dto.request.DocumentContentRequestDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.util.FileUploadUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.criticality.CriticalityCategory;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.RepositoryUtility;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

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
   private ServiceFactoryUtils serviceFactoryUtils;

   @Resource
   CriticalityUtils criticalityUtils;

   @Resource
   private ActivityStatisticsUtils activityStatisticsUtils;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;
   
   @Resource
   private RepositoryService repositoryService;
   
   @Resource
   private NotesService notesService;

   private static final Logger trace = LogManager.getLogger(ActivityInstanceService.class);

   /**
    * @param activityInstanceOid
    * @return
    */
   public ActivityInstanceDTO getActivityInstance(long activityInstanceOid)
   {
      ActivityInstance ai = activityInstanceUtils.getActivityInstance(activityInstanceOid);
      ActivityInstanceDTO aDTO = DTOBuilder.build(ai, ActivityInstanceDTO.class);
      aDTO.activityName = activityInstanceUtils.getActivityLabel(ai);
      return aDTO;
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
   public List<ActivityInstanceDTO> getAllRelocationTargets(long oid)
   {
      List<TransitionTarget> targets = serviceFactoryUtils.getWorkflowService()
            .getAdHocTransitionTargets(oid, TransitionOptions.DEFAULT,
                  ScanDirection.BACKWARD);
      List<ActivityInstanceDTO> list = new ArrayList<ActivityInstanceDTO>();
      if (null != targets)
      {
         for (TransitionTarget target : targets)
         {
            // TODO - needs a new transition target DTO?
            ActivityInstanceDTO dto = new ActivityInstanceDTO();
            dto.activity = new ActivityDTO();
            dto.activity.name = target.getActivityName();
            dto.activity.id = target.getActivityId();
            list.add(dto);
         }
      }

      return list;
   }

   /**
    * @param activityOid
    * @param targetActivityOid
    */
   public ActivityInstanceDTO relocateActivity(long activityOid, String activityId)
   {
      // TODO - not performant - check if this step can be avoided.
      List<TransitionTarget> targets = serviceFactoryUtils.getWorkflowService()
            .getAdHocTransitionTargets(activityOid, TransitionOptions.DEFAULT,
                  ScanDirection.BACKWARD);
      List<ActivityInstanceDTO> list = new ArrayList<ActivityInstanceDTO>();
      if (null != targets)
      {
         for (TransitionTarget target : targets)
         {
            if (target.getActivityId().equals(activityId)) {
               ActivityInstance activityInstance = activityInstanceUtils.getActivityInstance(target.getActivityInstanceOid());
               if (org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.isRelocationEligible(activityInstance))
               {
                  if (activityInstance.getActivity().isInteractive()
                        && activityInstance.getState().equals(
                              ActivityInstanceState.Suspended))
                  {
                     org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.activateActivity(activityInstance);
                  }
               }
               TransitionReport report = serviceFactoryUtils.getWorkflowService().performAdHocTransition(target, false);
               return DTOBuilder.build(report.getTargetActivityInstance(), ActivityInstanceDTO.class);
            }
         }
      }
      
      return null;
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
   public String completeAll(List<ActivityInstanceOutDataDTO> activities, String context)
   {
      NotificationMap notificationMap = new NotificationMap();

      for (ActivityInstanceOutDataDTO aiDto : activities)
      {
         try
         {
            activityInstanceUtils.complete(aiDto.oid, context, aiDto.outData);
            notificationMap.addSuccess(new NotificationDTO(aiDto.oid, null, ActivityInstanceState.Completed.getName()));
         }
         catch (Exception e)
         {
            notificationMap.addFailure(new NotificationDTO(aiDto.oid, null, e.getMessage()));
         }
      }
      return GsonUtils.toJsonHTMLSafeString(notificationMap);
   }

   /**
    * @param activityOID
    * @param activateNext
    * @param context
    * @return
    * @throws NotificationMapException
    */
   public ActivityInstanceDTO complete(Long activityOID) throws NotificationMapException
   {
      ActivityInstance ai = activityInstanceUtils.complete(activityOID);
      return DTOBuilder.build(ai, ActivityInstanceIntractionDTO.class);
   }

   /**
    * @param activityOID
    * @param toUser
    * @param withData
    * @return
    * @throws NotificationMapException
    */
   public ActivityInstanceDTO suspend(Long activityOID, boolean toUser, boolean withData) throws NotificationMapException
   {
      ActivityInstance ai = activityInstanceUtils.suspend(activityOID, toUser, withData);
      return DTOBuilder.build(ai, ActivityInstanceIntractionDTO.class);
   }

   /**
    * @param activityInstanceOid
    * @return
    */
   public List<DocumentDTO> getProcessAttachmentsForActivityInstance(long activityInstanceOid)
   {
      List<Document> processAttachments = activityInstanceUtils.getProcessAttachments(activityInstanceOid);

      List<DocumentDTO> processAttachmentsDTO = DocumentDTOBuilder.build(processAttachments,
            serviceFactoryUtils.getDocumentManagementService());

      return processAttachmentsDTO;
   }

   /**
    * @param oid
    * @param documentId
    * @return
    */
   public List<ProcessInstanceDTO> completeRendezvous(long oid, String documentId)
   {
      ActivityInstance completedAi = activityInstanceUtils.completeRendezvous(oid, documentId);

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

      Type listType = new TypeToken<List<Long>>()
      {
      }.getType();

      @SuppressWarnings("unchecked")
      List<Long> activities = (List<Long>) GsonUtils.extractList(GsonUtils.extractJsonArray(json, "activities"),
            listType);
      NotificationMap notificationMap = new NotificationMap();

      if (AbortScope.SubHierarchy.toString().equalsIgnoreCase(scope))
      {
         notificationMap = activityInstanceUtils.abortActivities(AbortScope.SubHierarchy, activities);
      }
      else if (AbortScope.RootHierarchy.toString().equalsIgnoreCase(scope))
      {
         notificationMap = activityInstanceUtils.abortActivities(AbortScope.RootHierarchy, activities);
      }
      else
      {
         throw new IllegalArgumentException("Scope not valid : " + scope);
      }
      return GsonUtils.toJsonHTMLSafeString(notificationMap);
   }

   /**
    * Get all available criticalities
    *
    * @return List
    */
   public List<CriticalityDTO> getCriticalities()
   {

      List<CriticalityDTO> criticalityCategories = new ArrayList<CriticalityDTO>();
      for (CriticalityCategory category : CriticalityUtils.getCriticalityConfiguration())
      {
         CriticalityDTO criticalityDTO = DTOBuilder.build(category, CriticalityDTO.class);
         criticalityCategories.add(criticalityDTO);
      }
      return criticalityCategories;
   }

   /**
    * Get all activity instances count
    *
    * @return List
    */
   public InstanceCountsDTO getAllCounts()
   {

      InstanceCountsDTO countDTO = new InstanceCountsDTO();

      countDTO.aborted = getAbortedActivityInstanceCount();
      countDTO.active = getActiveInstanceCount();
      countDTO.total = getTotalActivityInstanceCount();
      countDTO.waiting = getWaitingAcitivityInstanceCount();
      countDTO.completed = getCompletedActivityInstanceCount();

      return countDTO;

   }

   /**
    *
    * @return
    */
   public long getActiveInstanceCount()
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      return new Long(service.getActivityInstancesCount(ActivityInstanceQuery
            .findInState(ActivityInstanceState.Application)));
   }

   /**
    *
    * @return
    */
   public long getAbortedActivityInstanceCount()
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      return new Long(service.getActivityInstancesCount(ActivityInstanceQuery
            .findInState(ActivityInstanceState.Aborted)));
   }

   /**
    *
    * @return
    */
   public long getCompletedActivityInstanceCount()
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      return new Long(service.getActivityInstancesCount(ActivityInstanceQuery.findCompleted()));
   }

   /**
    *
    * @return
    */
   public long getTotalActivityInstanceCount()
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      return new Long(service.getActivityInstancesCount(ActivityInstanceQuery.findAll()));
   }

   /**
    *
    * @return
    */
   public long getWaitingAcitivityInstanceCount()
   {
      QueryService service = serviceFactoryUtils.getQueryService();
      ActivityInstanceQuery query = ActivityInstanceQuery.findInState(new ActivityInstanceState[] {
            ActivityInstanceState.Interrupted, ActivityInstanceState.Suspended, ActivityInstanceState.Hibernated});
      return new Long(service.getActivityInstancesCount(query));
   }

   /**
    * @param userId
    * @return
    */
   public QueryResultDTO getAllInstances(DataTableOptionsDTO options, ActivityInstanceQuery query)
   {
      QueryResult< ? > queryResult = activityInstanceUtils.getActivityInstances(options, query);
      if(CollectionUtils.isNotEmpty(options.extraColumns))
      {
         return ActivityTableUtils.buildTableResult(queryResult, MODE.ACTIVITY_TABLE, null, options.extraColumns);
      }
      else
      {
         return ActivityTableUtils.buildTableResult(queryResult, MODE.ACTIVITY_TABLE);   
      }
   }

   /**
    * @param oid
    * @param withEvents
    * @return
    */
   public AbstractDTO getAllActivityInstancesForProcess(Long processInstanceOid, boolean withEvents)
   {
      ActivityInstanceQuery aiQuery = ActivityInstanceQuery.findAll();
      ProcessInstanceFilter processFilter = new ProcessInstanceFilter(processInstanceOid, false);
      aiQuery.where(processFilter);
      aiQuery.orderBy(ActivityInstanceQuery.START_TIME).and(ActivityInstanceQuery.OID);

      if (withEvents)
      {
         aiQuery.setPolicy(HistoricalEventPolicy.ALL_EVENTS);
      }

      ActivityInstances activityInstances = serviceFactoryUtils.getQueryService().getAllActivityInstances(aiQuery);
      QueryResultDTO activityQR = ActivityTableUtils.buildTableResult(activityInstances, MODE.ACTIVITY_TABLE);
      
      //Add Notes
      for (Object aDTOB : activityQR.list)
      {
         ActivityInstanceDTO activityInstanceDTO = (ActivityInstanceDTO) aDTOB;
         activityInstanceDTO.notes = notesService.getActivityNotes(activityInstanceDTO.activityOID, true);
      }
      return activityQR;   
   }
   
   /**
    * @return
    */
   public QueryResultDTO getInstancesByOids( DataTableOptionsDTO options, List<Long> oids, ActivityInstanceQuery query)
   {
      QueryResult< ? > queryResult = activityInstanceUtils.getActivitiesByOids(options, oids, query);
      if(CollectionUtils.isNotEmpty(options.extraColumns))
      {
         return ActivityTableUtils.buildTableResult(queryResult, MODE.ACTIVITY_TABLE, null, options.extraColumns);
      }
      else
      {
         return ActivityTableUtils.buildTableResult(queryResult, MODE.ACTIVITY_TABLE);   
      }
   }

   /**
    *
    * @param activityOID
    * @return
    */
   public NotificationMap reactivate(Long activityOID)
   {
      NotificationMap notification = new NotificationMap();
      ActivityInstance ai = null;
      try
      {
         ai = org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.getActivityInstance(activityOID);
         ai = org.eclipse.stardust.ui.web.viewscommon.utils.ActivityInstanceUtils.activateActivity(ai);
         
         serviceFactoryUtils.getWorkflowService().unbindActivityEventHandler(activityOID, "Resubmission");
         notification.addSuccess(new NotificationDTO(activityOID, ai.getActivity().getName(), null));
      }
      catch (ConcurrencyException ce)
      {
         trace.error("Unable to activate Activity, activity not in worklist", ce);
         String msg = restCommonClientMessages.getString("activity.concurrencyError");
         notification.addFailure(new NotificationDTO(activityOID, activityInstanceUtils.getActivityLabel(ai), msg));
      }
      catch (AccessForbiddenException af)
      {
         trace.error("User not authorized to activate", af);
         String msg = restCommonClientMessages.getString("activity.acccessForbiddenError");
         notification.addFailure(new NotificationDTO(activityOID, activityInstanceUtils.getActivityLabel(ai), msg));
      }
      catch (Exception exception)
      {
         trace.error("Exception occurred while reactivating activity", exception);
         notification.addFailure(new NotificationDTO(activityOID, null, exception.getMessage()));
      }
      return notification;
   }

   /**
    * @param activityOID
    * @param interactionAware
    * @return
    */
   public ActivityInstanceDTO activate(Long activityOID, boolean interactionAware) throws NotificationMapException
   {
      NotificationMap notification = new NotificationMap();
      ActivityInstance ai = null;
      try
      {
         ai = activityInstanceUtils.activate(activityOID);
         if (interactionAware)
         {
            ActivityInstanceIntractionDTO dto = DTOBuilder.build(ai, ActivityInstanceIntractionDTO.class);
            dto.panelUri = activityInstanceUtils.initializeInteractionController(ai);
            return dto;
         }
         else
         {
            return DTOBuilder.build(ai, ActivityInstanceDTO.class);
         }
      }
      catch(NotificationMapException ne)
      {
         throw ne;
      }
      catch (ConcurrencyException ce)
      {
         trace.error("Unable to activate Activity, activity not in worklist", ce);
         String msg = restCommonClientMessages.getString("activity.concurrencyError");
         notification.addFailure(new NotificationDTO(activityOID, activityInstanceUtils.getActivityLabel(ai), msg));
         throw new NotificationMapException(notification, Status.CONFLICT);
      }
      catch (AccessForbiddenException af)
      {
         trace.error("User not authorized to activate", af);
         String msg = restCommonClientMessages.getString("activity.acccessForbiddenError");
         notification.addFailure(new NotificationDTO(activityOID, activityInstanceUtils.getActivityLabel(ai), msg));
         throw new NotificationMapException(notification, Status.UNAUTHORIZED);
      }
      catch (Exception e)
      {
         trace.error("Exception occurred while activating", e);
         String msg = e.getMessage();
         notification.addFailure(new NotificationDTO(activityOID, activityInstanceUtils.getActivityLabel(ai), msg));
         throw new NotificationMapException(notification, Status.INTERNAL_SERVER_ERROR);
      }
   }

   /**
    * @param activityOid
    * @return
    */
   public FolderDTO getCorrespondenceOutFolder(Long activityOid)
   {
      ActivityInstance ai = getActivityInstance(activityOid);
      Folder correspondenceOutFolder = RepositoryUtility.getOrCreateCorrespondenceOutFolder(ai);
      FolderDTO folderDTO = FolderDTOBuilder.build(correspondenceOutFolder);
      folderDTO.documents = DocumentDTOBuilder.build(correspondenceOutFolder.getDocuments(),
            serviceFactoryUtils.getDocumentManagementService());
      return folderDTO;
   }
   
   
   /**
    * @param activityOid
    * @param attachments
    * @return
    * @throws Exception
    */
   public Map<String, Object> addProcessDocuments(long activityOid, List<Attachment> attachments) throws Exception
   {
      ActivityInstance activityInstance = activityInstanceUtils.getActivityInstance(activityOid);
      if (activityInstance == null)
      {
         throw new ObjectNotFoundException(BpmRuntimeError.ATDB_NO_MATCHING_ACTIVITY_INSTANCE.raise());
      }
      // parse attachments
      List<DocumentContentRequestDTO> uploadedDocuments = FileUploadUtils.parseAttachments(attachments);
      return repositoryService.createActivityDocuments(uploadedDocuments, activityInstance);
   }
   
   /**
    *
    * @return
    */
   public List<CompletedActivitiesStatisticsDTO> getStatsForCompletedActivities()
   {
      return activityStatisticsUtils.getForCompletedActivies();
   }

   /**
    *
    * @return
    */
   public List<PendingActivitiesStatisticsDTO> getPendingActivities()
   {
      return activityInstanceUtils.getPendingActivities();
   }

   public List<SelectItemDTO> getAllRoles()
   {
      return activityInstanceUtils.getAllRoles();
   }

   /**
    *
    * @return
    */
   public List<PostponedActivitiesResultDTO> getStatsByPostponedActivities()
   {
      return activityStatisticsUtils.getForPostponedActivities();
   }

   /**
    *
    * @return
    */
   public List<ColumnDTO> getParticipantColumns()
   {
      return activityInstanceUtils.getParticipantColumns();
   }

   /**
    *
    * @return
    */
   public List<CompletedActivitiesStatisticsDTO> getPerformanceStatsByTeamLead()
   {
      return activityStatisticsUtils.getPerformanceStatsByTeamLead();
   }

   /**
    *
    * @param piOid
    * @return
    */
   public List<ActivityInstanceDTO> getByProcessOid(long piOid)
   {
      return activityInstanceUtils.getByProcessOid(piOid);
   }

   public ActivityInstance getActivityInstance(Long activityOId){
      return activityInstanceUtils.getActivityInstance(activityOId);
   }
  
}
