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
package org.eclipse.stardust.ui.web.rest;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.faces.FacesException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.exception.PortalRestException;
import org.eclipse.stardust.ui.web.rest.service.ActivityInstanceService;
import org.eclipse.stardust.ui.web.rest.service.DelegationComponent;
import org.eclipse.stardust.ui.web.rest.service.MapAdapter;
import org.eclipse.stardust.ui.web.rest.service.ParticipantSearchComponent;
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.ProcessInstanceService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceOutDataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.CompletedActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.service.dto.PendingActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.PostponedActivitiesResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialManualActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.TrafficLightViewUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
@Path("/activity-instances")
public class ActivityInstanceResource
{
   private static final Logger trace = LogManager.getLogger(ActivityInstanceResource.class);

   @Autowired
   private ActivityInstanceService activityInstanceService;

   @Resource
   private ParticipantSearchComponent participantSearchComponent;

   @Resource
   private DelegationComponent delegationComponent;

   @Context
   private HttpServletRequest httpRequest;

   @Autowired
   ProcessDefinitionService processDefService;

   @Autowired
   private ProcessInstanceService processInstanceService;
   
   private final JsonMarshaller jsonIo = new JsonMarshaller();

   public static final String ACTIVE = "Active";

   public static final String COMPLETED = "Completed";

   public static final String ABORTED = "Aborted";

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{activityInstanceOid: \\d+}")
   public Response getActivityInstance(@PathParam("activityInstanceOid") long activityInstanceOid)
   {
      try
      {
         ActivityInstanceDTO aiDTO = getActivityInstanceService().getActivityInstance(activityInstanceOid);

         return Response.ok(aiDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @author Yogesh.Manware
    * @param activityInstanceOid
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{activityInstanceOid: \\d+}/correspondence-process-instance")
   public Response getCorrespondenceProcessInstance(@PathParam("activityInstanceOid") Long activityInstanceOid)
   {
      ProcessInstanceDTO processInstanceDTO = processInstanceService
            .getCorrespondenceProcessInstanceDTO(getActivityInstanceService().getActivityInstance(activityInstanceOid)
                  .getProcessInstanceOID());
      return Response.ok(processInstanceDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/trivialManualActivitiesDetails")
   public Response getTrivialManualActivitiesDetails(String postedData)
   {
      try
      {
         Map<String, Map<String, Object>> output = new LinkedHashMap<String, Map<String, Object>>();

         List<Long> oids = JsonDTO.getAsList(postedData, Long.class);

         if (oids.size() > 0)
         {
            Map<String, TrivialManualActivityDTO> details = getActivityInstanceService()
                  .getTrivialManualActivitiesDetails(oids, "default");

            // gson.toJson(details) is not working, inOutData is not Serialized. hence
            // below workaround
            // Workaround. Visit later TODO
            for (Entry<String, TrivialManualActivityDTO> entry : details.entrySet())
            {
               output.put(entry.getKey(), new LinkedHashMap<String, Object>());
               output.get(entry.getKey()).put("dataMappings", entry.getValue().dataMappings);
               output.get(entry.getKey()).put("inOutData", entry.getValue().inOutData);
            }
         }

         Gson gson = new Gson();
         String jsonText = gson.toJson(output);

         return Response.ok(jsonText, MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{oid: \\d+}/dataMappings")
   public Response getDataMappings(@PathParam("oid") long oid)
   {
      try
      {
         String jsonOutput = getActivityInstanceService().getAllDataMappingsAsJson(oid, "default");

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/dataMappings")
   public Response getAllDataMappings(String postedData)
   {
      try
      {
         StringBuffer output = new StringBuffer();

         List<Long> oids = JsonDTO.getAsList(postedData, Long.class);
         for (long oid : oids)
         {
            if (output.length() > 0)
            {
               output.append(",");
            }

            output.append("\"").append(oid).append("\" : ")
                  .append(getActivityInstanceService().getAllDataMappingsAsJson(oid, "default"));
         }

         output.insert(0, "{");
         output.append("}");

         return Response.ok(output.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{oid: \\d+}/inData")
   public Response getInData(@PathParam("oid") long oid)
   {
      try
      {
         Map<String, Serializable> values = getActivityInstanceService().getAllInDataValues(oid, "default");

         Gson gson = new Gson();
         String jsonOutput = gson.toJson(values);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/inData")
   public Response getAllInData(String postedData)
   {
      try
      {
         Map<String, Map<String, Serializable>> output = new LinkedHashMap<String, Map<String, Serializable>>();

         List<Long> oids = JsonDTO.getAsList(postedData, Long.class);
         for (Long oid : oids)
         {
            Map<String, Serializable> values = getActivityInstanceService().getAllInDataValues(oid, "default");
            if (null != values)
            {
               output.put(String.valueOf(oid), values);
            }
         }

         /*
          * Gson gson = new Gson(); String jsonOutput = gson.toJson(output);
          */

         String jsonOutput = GsonUtils.stringify(output);

         return Response.ok(jsonOutput, MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{oid: \\d+}/relocationTargets")
   public Response getRelocationTargets(@PathParam("oid") long oid)
   {
      try
      {
         return Response.ok(
               AbstractDTO.toJson(getActivityInstanceService().getAllRelocationTargets(
                     oid)), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/{oid: \\d+}/relocate")
   public Response relocate(@PathParam("oid") long oid, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);
         
         AbstractDTO dto = getActivityInstanceService().relocateActivity(oid,
               json.get("targetActivityId").getAsString());
         if (dto != null)
         {
            return Response.ok(dto.toJson(), MediaType.APPLICATION_JSON).build();
         }
         else
         {
            return Response.notModified().build();
         }
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/completeAll")
   public Response completeAll(String postedData)
   {
      try
      {
         List<ActivityInstanceOutDataDTO> activities = ActivityInstanceOutDataDTO.toList(postedData);

         String jsonOutput = getActivityInstanceService().completeAll(activities, "default");

         return Response.ok(jsonOutput, MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{activityInstanceOid: \\d+}/attachments.json")
   public Response getProcessesAttachments(@PathParam("activityInstanceOid") long activityInstanceOid)
   {
      try
      {
         List<DocumentDTO> processAttachments = getActivityInstanceService().getProcessAttachmentsForActivityInstance(
               activityInstanceOid);

         Gson gson = new Gson();
         String jsonOutput = gson.toJson(processAttachments);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("completeRendezvous.json")
   public Response completeRendezvous(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         Gson gson = new Gson();
         ActivityInstanceDTO aiDTO = gson.fromJson(json.get("pendingActivityInstance"), ActivityInstanceDTO.class);
         DocumentDTO documentDTO = gson.fromJson(json.get("document").getAsJsonObject(), DocumentDTO.class);

         List<ProcessInstanceDTO> processInstances = getActivityInstanceService().completeRendezvous(aiDTO.activityOID,
               documentDTO.uuid);

         String jsonOutput = gson.toJson(processInstances);

         return Response.ok(jsonOutput.toString(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @author Yogesh.Manware
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/abort")
   public Response abortActivities(String postedData)
   {
      return Response.ok(getActivityInstanceService().abortActivities(postedData), MediaType.APPLICATION_JSON).build();
   }

   /**
    * @author Yogesh.Manware
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/searchParticipants")
   public Response searchParticipant(String postedData, @QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize)
   {
      // postedData =
      // "{searchText: '', activities=[8], participantType='All', limitedSearch=false, disableAdministrator=false, excludeUserType=false}";
      // postedData = "{activities=[8], limitedSearch=false}";
      return Response.ok(participantSearchComponent.searchParticipants(postedData, skip, pageSize),
            MediaType.APPLICATION_JSON).build();
   }

   /**
    * @author Yogesh.Manware
    * @param postedData
    * @return
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/searchAllParticipants/{searchText}/{maxMatches}")
   public Response searchAllParticipant(@PathParam("searchText") String searchText,
         @PathParam("maxMatches") int maxMatches)
   {
      return Response.ok(participantSearchComponent.searchAllParticipants(searchText, maxMatches),
            MediaType.APPLICATION_JSON).build();
   }

   /**
    * @author Yogesh.Manware
    * @param postedData
    * @return
    * @throws PortalRestException
    * @throws PortalException
    * @throws FacesException
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/delegate")
   public Response delegateActivity(String postedData) throws PortalRestException, PortalException
   {
      // postedData = "{activities:[12], participantType:'User', participant:1}";
      // postedData =
      // "{activities:[8], participantType:'User', participant:2, department:false, activityData: {'Country':{'id':'India', 'States':[{ id: 'dd', name:'nameo'}, { id: 'dd2', name:'nameo2'}] } } }";
      // postedData =
      // "{activities:[8], participant:{}, department:false, activityData: {Country:{id:'India',States:[{id:'MAH',cities:[{id:'Pn',Name:'Pune'},{id:'Pn2',Name:'Pune2'}]},{id:'Dl',cities:[{id:'DL1',Name:'Delhi'}]}]}} }";

      return Response.ok(delegationComponent.delegate(postedData), MediaType.APPLICATION_JSON).build();
   }

   /**
    * creates folder if it does not exist
    * @author Yogesh.Manware
    * @param processOid
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/correspondence-out")
   public Response getCorrespondenceOutFolder(@PathParam("oid") Long activityOid)
   {
      FolderDTO folderDto = activityInstanceService.getCorrespondenceOutFolder(activityOid);
      //TODO move jsonHelper and MapAdapter to Portal-Common and then modify GsonUtils
      Gson gson = new GsonBuilder().registerTypeAdapter(Map.class, new MapAdapter()).disableHtmlEscaping().create();
      return Response.ok(gson.toJson(folderDto, FolderDTO.class), MediaType.APPLICATION_JSON).build();
   }
   
   /**
    * @author Johnson.Quadras
    * @param postedData
    * @return
    * @throws PortalRestException
    * @throws PortalException
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/performDefaultDelegate")
   public Response performDefaultDelegate(String postedData) throws PortalRestException, PortalException
   {
      return Response.ok(delegationComponent.performDefaultDelegate(postedData), MediaType.APPLICATION_JSON).build();
   }

   /**
    * @author Johnson.Quadras
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/availableCriticalities")
   public Response getAvailiableCriticalities()
   {
      try
      {
         return Response.ok(AbstractDTO.toJson(getActivityInstanceService().getCriticalities()),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @author Johnson.Quadras
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/allCounts")
   public Response getAllCounts()
   {
      try
      {
         InstanceCountsDTO acitivityInstanceCountDTO = getActivityInstanceService().getAllCounts();
         return Response.ok(GsonUtils.toJsonHTMLSafeString(acitivityInstanceCountDTO), MediaType.APPLICATION_JSON)
               .build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/allActivities")
   public Response getAllActivities(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getActivityInstanceService().getAllInstances(options, null);
         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/forTLVByCategory")
   public Response getActivitiesForTLVByCategory(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);

         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);
         JsonArray bOidsArray = postJSON.getAsJsonArray("bOids");
         Type type = new TypeToken<List<Long>>()
         {
         }.getType();
         List<Long> bOids = new ArrayList<Long>();
         if (null != bOidsArray)
         {
            bOids = new Gson().fromJson(bOidsArray.toString(), type);

         }

         String dateType = postJSON.getAsJsonPrimitive("dateType").getAsString();

         Integer dayOffset = postJSON.getAsJsonPrimitive("dayOffset").getAsInt();

         String processId = postJSON.getAsJsonPrimitive("processId").getAsString();
         String activityId = postJSON.getAsJsonPrimitive("activityId").getAsString();

         String state = postJSON.getAsJsonPrimitive("state").getAsString();

         ActivityInstanceQuery query = new ActivityInstanceQuery();
         query.where(ActivityFilter.forProcess(activityId, processId));

         FilterOrTerm benchmarkFilter = query.getFilter().addOrTerm();

         for (Long bOid : bOids)
         {
            benchmarkFilter.add(BenchmarkActivityStatisticsQuery.BENCHMARK_OID.isEqual(bOid));
         }

         Calendar startDate = TrafficLightViewUtils.getCurrentDayStart();
         Calendar endDate = TrafficLightViewUtils.getCurrentDayEnd();

         if (dayOffset > 0)
         {
            endDate = TrafficLightViewUtils.getfutureEndDate(dayOffset);
         }
         else if (dayOffset < 0)
         {
            startDate = TrafficLightViewUtils.getPastStartDate(dayOffset);
         }

         if (dateType.equals(TrafficLightViewUtils.BUSINESS_DATE))
         {
            query.where((DataFilter.between(TrafficLightViewUtils.getModelName(processId)
                  + TrafficLightViewUtils.BUSINESS_DATE, startDate.getTime(), endDate.getTime())));
         }
         else
         {
            query.where(ActivityInstanceQuery.START_TIME.between(startDate.getTimeInMillis(),
                  endDate.getTimeInMillis()));
         }

         if (postJSON.getAsJsonPrimitive("benchmarkCategory") != null)
         {
            Long benchmarkCategory = postJSON.getAsJsonPrimitive("benchmarkCategory").getAsLong();
            benchmarkFilter.add(ActivityInstanceQuery.BENCHMARK_VALUE.isEqual(benchmarkCategory));
         }

         if (state.equals(ACTIVE))
         {
            query.getFilter().add(ActivityStateFilter.ALIVE);
         }
         else if (state.equals(COMPLETED))
         {
            query.getFilter().add(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.COMPLETED));
         }
         else if (state.equals(ABORTED))
         {
            query.getFilter().add(ActivityInstanceQuery.STATE.isEqual(ActivityInstanceState.ABORTED));
         }

         QueryResultDTO resultDTO = getActivityInstanceService().getAllInstances(options, query);
         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
    * @author Johnson.Quadras
    * @param postedData
    * @return
    * @throws PortalRestException
    * @throws PortalException
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/reactivate")
   public Response reactivate(String postedData)
   {
      Map<String, Object> data = JsonDTO.getAsMap(postedData);
      NotificationMap result = activityInstanceService.reactivate(Long.valueOf(data.get("activityOID").toString()));
      return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
   }

   /**
    * @author Johnson.Quadras
    * @param postedData
    * @return
    * @throws PortalRestException
    * @throws PortalException
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/activate")
   public Response activate(String postedData)
   {
      Map<String, Object> data = JsonDTO.getAsMap(postedData);
      NotificationMap result = activityInstanceService.activate(Long.valueOf(data.get("activityOID").toString()));
      return Response.ok(GsonUtils.toJsonHTMLSafeString(result), MediaType.APPLICATION_JSON).build();
   }

   /**
    * Gets the completed activities by process
    * 
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/statistics/completedActivities")
   public Response getStatsForCompletedActivities(String postedData)
   {
      try
      {
         List<CompletedActivitiesStatisticsDTO> result = activityInstanceService.getStatsForCompletedActivities();
         return Response.ok(AbstractDTO.toJson(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
    * Gets the postponed activities by participant
    * 
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/statistics/postponedActivities")
   public Response getStatsByPostponedActivities(String postedData)
   {
      try
      {
         List<PostponedActivitiesResultDTO> result = activityInstanceService.getStatsByPostponedActivities();
         return Response.ok(AbstractDTO.toJson(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
    * Gets the completed activities by process
    * 
    * @param postedData
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/pendingActivities")
   public Response getPendingActivities()
   {
      try
      {
         List<PendingActivitiesStatisticsDTO> result = activityInstanceService.getPendingActivities();
         return Response.ok(AbstractDTO.toJson(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/allRoleColumns")
   public Response getRoleColumns()
   {
      return Response.ok(AbstractDTO.toJson(activityInstanceService.getAllRoles()), MediaType.APPLICATION_JSON).build();
   }

   /**
     * 
     */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/participantColumns")
   public Response getParticipantColumns()
   {
      try
      {
         List<ColumnDTO> result = activityInstanceService.getParticipantColumns();
         return Response.ok(AbstractDTO.toJson(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
     * 
     */
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Consumes(MediaType.APPLICATION_JSON)
   @Path("/oids")
   public Response getByActivityInstanceOids(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, @QueryParam("oids") String oids,
         String postData)
   {
      try
      {
         if (StringUtils.isEmpty(oids))
         {
            throw new IllegalArgumentException("param oids cant be empty");
         }
         List<Long> aInstanceOids = new ArrayList<Long>();

         for (String oid : Arrays.asList(oids.split(",")))
         {
            aInstanceOids.add(Long.valueOf(oid));
         }
         Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);
         QueryResultDTO resultDTO = getActivityInstanceService().getInstancesByOids(options, aInstanceOids, null);
         return Response.ok(resultDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
    * Gets the completed activities by process
    * 
    * @param postedData
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/statistics/completedActivitiesByTeamLead")
   public Response getPerformanceStatsByTeamLead()
   {
      try
      {
         List<CompletedActivitiesStatisticsDTO> result = activityInstanceService.getPerformanceStatsByTeamLead();
         return Response.ok(AbstractDTO.toJson(result), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/process/oid/{piOid}")
   public Response getWorklistByProcess(@PathParam("piOid") long piOid)
   {
      try
      {
         return Response.ok(AbstractDTO.toJson(activityInstanceService.getByProcessOid(piOid)),
               MediaType.APPLICATION_JSON).build();

      }
      catch (ObjectNotFoundException onfe)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      catch (Exception e)
      {
         trace.error("", e);
         return Response.status(Status.INTERNAL_SERVER_ERROR).build();
      }
   }

   /**
    * 
    * @param options
    * @param postData
    */
   private void populatePostData(Options options, String postData)
   {
      List<DescriptorColumnDTO> availableDescriptors = processDefService.getDescriptorColumns(true);
      ActivityTableUtils.populatePostData(options, postData, availableDescriptors);
   }

   /**
    * @return the activityInstanceService
    */
   public ActivityInstanceService getActivityInstanceService()
   {
      return activityInstanceService;
   }

   /**
    * @param activityInstanceService
    */
   public void setActivityInstanceService(ActivityInstanceService activityInstanceService)
   {
      this.activityInstanceService = activityInstanceService;
   }
}
