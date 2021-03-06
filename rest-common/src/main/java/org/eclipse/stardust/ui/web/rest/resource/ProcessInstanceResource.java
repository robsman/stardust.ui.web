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
package org.eclipse.stardust.ui.web.rest.resource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.error.InvalidValueException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessStateFilter;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkProcessStatisticsQuery;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.message.RestCommonClientMessages;
import org.eclipse.stardust.ui.web.rest.component.service.ActivityInstanceService;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessInstanceService;
import org.eclipse.stardust.ui.web.rest.component.util.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.rest.component.util.TrafficLightViewUtils;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessDescriptorDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.DataPathValueDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.util.MapAdapter;
import org.eclipse.stardust.ui.web.viewscommon.docmgmt.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * @author Anoop.Nair
 * @version $Revision: $
 */
@Path("/process-instances")
public class ProcessInstanceResource
{
   private static final Logger trace = LogManager.getLogger(ProcessInstanceResource.class);

   @Autowired
   private ProcessInstanceService processInstanceService;

   @Autowired
   private ActivityInstanceService activityInstanceService;

   @Autowired
   ProcessDefinitionService processDefService;

   @Resource
   private RestCommonClientMessages restCommonClientMessages;

   public static final String ACTIVE = "Active";

   public static final String COMPLETED = "Completed";

   public static final String ABORTED = "Aborted";

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{documentId}/split")
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.DocumentDTO")
   public Response splitDocument(@PathParam("processInstanceOid") long processInstanceOid,
         @PathParam("documentId") String documentId, String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response.ok(getProcessInstanceService().splitDocument(processInstanceOid, documentId, json).toString(),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents")
   @ResponseDescription("The response will contain List<DataPathValueDTO>")
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.response.DataPathValueDTO")
   public Response getDocuments(@PathParam("processInstanceOid") String processInstanceOid)
   {
      try
      {
         return Response.ok(
               AbstractDTO.toJson(
                     getProcessInstanceService().getProcessInstanceDocuments(Long.parseLong(processInstanceOid))),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/correspondence")
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO")
   public Response getCorrespondenceFolder(@PathParam("oid") Long processOid)
   {
      FolderDTO folderDto = null;
      try
      {
         folderDto = getProcessInstanceService().getCorrespondenceFolderDTO(processOid);
      }
      catch (Exception e)
      {
         folderDto = new FolderDTO();
         // do nothing - correspondence folder does not exist
      }
      // TODO move jsonHelper and MapAdapter to Portal-Common and then modify GsonUtils
      Gson gson = new GsonBuilder().registerTypeAdapter(Map.class, new MapAdapter()).disableHtmlEscaping().create();
      return Response.ok(gson.toJson(folderDto, FolderDTO.class), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processInstanceOid: \\d+}/documents/{dataPathId}")
   @ResponseDescription("The response will contain List<DataPathValueDTO>")
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.response.DataPathValueDTO")
   public Response getDocumentsForDatapath(@PathParam("processInstanceOid") String processInstanceOid,
         @PathParam("dataPathId") String dataPathId)
   {
      try
      {
         return Response.ok(
               AbstractDTO.toJson(
                     getProcessInstanceService().getDataPathValueFor(Long.parseLong(processInstanceOid), dataPathId)),
               MediaType.APPLICATION_JSON).build();
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
   @Path("documentRendezvous.json")
   public Response getPendingProcesses(String postedData)
   {
      try
      {
         JsonObject json = jsonIo.readJsonObject(postedData);

         return Response
               .ok(getProcessInstanceService().getPendingProcesses(json).toString(), MediaType.APPLICATION_JSON)
               .build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   public Response startProcess(List<Attachment> attachments)
   {
      return Response.ok(getProcessInstanceService().startProcess(attachments).toJson(), MediaType.APPLICATION_JSON)
            .build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/updatePriorities")
   public Response updatePriorities(String postedData)
   {
      try
      {
         Map<String, Integer> oidPriorityMap = (Map) GsonUtils.extractMap(JsonDTO.getJsonObject(postedData),
               "priorities");
         String jsonOutput = getProcessInstanceService().updatePriorities(oidPriorityMap);
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
   @Path("/allCounts")
   public Response getAllCounts()
   {
      try
      {
         InstanceCountsDTO processInstanceCountDTO = getProcessInstanceService().getAllCounts();
         return Response.ok(GsonUtils.toJsonHTMLSafeString(processInstanceCountDTO), MediaType.APPLICATION_JSON)
               .build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/abort")
   public Response abortProcesses(String postedData)
   {
      return Response.ok(getProcessInstanceService().abortProcesses(postedData), MediaType.APPLICATION_JSON).build();
   }

   /**
    * @param postedData
    * @return
    */
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/recover")
   public Response recoverProcesses(String postedData)
   {
      return Response.ok(getProcessInstanceService().recoverProcesses(postedData), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/attachToCase")
   public Response attachToCase(String request)
   {
      return Response.ok(processInstanceService.attachToCase(request), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/createCase")
   public Response createCase(String request)
   {
      return Response.ok(processInstanceService.createCase(request), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/search")
   public Response search(@PathParam("participantQId") String participantQId,
         @QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy,
               "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);

         return Response.ok(GsonUtils.toJsonHTMLSafeString(processInstanceService.getProcessInstances(null, options)),
               MediaType.APPLICATION_JSON).build();
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
   @Path("/forTLVByCategory")
   public Response getProcesslistForTLV(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy,
               "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);

         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);

         String drillDownType = postJSON.getAsJsonPrimitive("drillDownType").getAsString();
         ProcessInstanceQuery query = new ProcessInstanceQuery();

         if (drillDownType.equals("PROCESS_WORKITEM"))
         {
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

            JsonArray processIds = postJSON.getAsJsonArray("processIds");

            Type processType = new TypeToken<List<String>>()
            {
            }.getType();
            List<String> processIDs = new ArrayList<String>();
            if (null != processIds)
            {
               processIDs = new Gson().fromJson(processIds.toString(), processType);

            }

            String state = postJSON.getAsJsonPrimitive("state").getAsString();

            FilterOrTerm processIdFilter = query.getFilter().addOrTerm();
            for (String processId : processIDs)
            {
               processIdFilter.add(new ProcessDefinitionFilter(processId));
            }

            FilterOrTerm benchmarkFilter = query.getFilter().addOrTerm();

            for (Long bOid : bOids)
            {
               benchmarkFilter.add(BenchmarkProcessStatisticsQuery.BENCHMARK_OID.isEqual(bOid));
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

            if (dateType.equals(PredefinedConstants.BUSINESS_DATE))
            {
               FilterOrTerm businessDateFilter = query.getFilter().addOrTerm();
               for (String processId : processIDs)
               {
                  businessDateFilter.add((DataFilter.between(
                        TrafficLightViewUtils.getModelName(processId) + PredefinedConstants.BUSINESS_DATE,
                        startDate.getTime(), endDate.getTime())));
               }
            }
            else
            {
               query.where(
                     ProcessInstanceQuery.START_TIME.between(startDate.getTimeInMillis(), endDate.getTimeInMillis()));
            }

            if (postJSON.getAsJsonPrimitive("benchmarkCategory") != null)
            {
               Long benchmarkCategory = postJSON.getAsJsonPrimitive("benchmarkCategory").getAsLong();
               query.where(ProcessInstanceQuery.BENCHMARK_VALUE.isEqual(benchmarkCategory));
            }
            else
            {
               query.where(ProcessInstanceQuery.BENCHMARK_VALUE.greaterThan(0l));
            }

            if (state.equals(ACTIVE))
            {
               query.where(ProcessStateFilter.ALIVE);
            }
            else if (state.equals(COMPLETED))
            {
               query.where(ProcessStateFilter.COMPLETED);
            }
            else if (state.equals(ABORTED))
            {
               query.where(ProcessStateFilter.ABORTED);
            }
         }
         else
         {
            // for business object by process

            JsonArray instanceOids = postJSON.getAsJsonArray("oids");

            Type processType = new TypeToken<List<Long>>()
            {
            }.getType();
            List<Long> oids = new ArrayList<Long>();
            if (null != instanceOids)
            {
               oids = new Gson().fromJson(instanceOids.toString(), processType);

            }

            FilterOrTerm oidsFilter = query.getFilter().addOrTerm();
            for (Long oid : oids)
            {
               oidsFilter.add(ProcessInstanceQuery.OID.isEqual(oid));
            }

         }

         return Response.ok(GsonUtils.toJsonHTMLSafeString(processInstanceService.getProcessInstances(query, options)),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

   /**
    * @author Nikhil.Gahlot
    * @param processInstanceOID
    * @return
    */
   @GET
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/spawnableProcesses")
   public Response getTargetProcessesForSpawnSwitch()
   {
      return Response.ok(processInstanceService.getTargetProcessesForSpawnSwitch(), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/checkIfProcessesAbortable")
   public Response checkIfProcessesAbortable(String postedData, @QueryParam("type") String type)
   {
      return Response.ok(processInstanceService.checkIfProcessesAbortable(postedData, type), MediaType.APPLICATION_JSON)
            .build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/switchProcess")
   public Response switchProcess(String postedData)
   {
      return Response.ok(processInstanceService.switchProcess(postedData), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/abortAndJoinProcess")
   public Response abortAndJoinProcess(String postedData)
   {
      return Response.ok(processInstanceService.abortAndJoinProcess(postedData), MediaType.APPLICATION_JSON).build();
   }

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/getRelatedProcesses")
   public Response getRelatedProcesses(String postedData, @QueryParam("matchAny") String matchAnyStr,
         @QueryParam("searchCases") String searchCasesStr)
   {
      boolean matchAny = "true".equals(matchAnyStr);
      boolean searchCases = "true".equals(searchCasesStr);

      return Response.ok(processInstanceService.getRelatedProcesses(postedData, matchAny, searchCases),
            MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/allProcessColumns")
   public Response getProcessColumns()
   {
      return Response.ok(AbstractDTO.toJson(processInstanceService.getProcessesColumns()), MediaType.APPLICATION_JSON)
            .build();
   }

   /**
    * @param oid
    * @param fetchDescriptors
    * @param withHierarchyInfo
    * @param withEvents
    * @return
    * @throws ResourceNotFoundException
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{oid}")
   @ResponseDescription("return requested ProcessInstanceDTO json")
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO")
   public Response getProcessByOid(@PathParam("oid") Long oid,
         @QueryParam("fetchDescriptors") @DefaultValue("false") boolean fetchDescriptors,
         @QueryParam("withEvents") @DefaultValue("false") boolean withEvents) throws ResourceNotFoundException
   {
      return Response
            .ok(processInstanceService.getProcessByOid(oid, fetchDescriptors).toJson(), MediaType.APPLICATION_JSON)
            .build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{oid}/process-descriptors")
   @ResponseDescription("returns list of DescriptorItemTableEntry")

   public Response getProcessByOid(@PathParam("oid") Long oid) throws ResourceNotFoundException
   {
      return Response.ok(AbstractDTO.toJson(processInstanceService.getProcessDescriptorsWithModifyByAndDate(oid)),
            MediaType.APPLICATION_JSON).build();
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/process-descriptor")
   public Response updateProcessDescriptor(@PathParam("oid") Long processOid, String postedData)
   {
      ProcessDescriptorDTO descriptorDTO = GsonUtils.fromJson(postedData, ProcessDescriptorDTO.class);
      String errorMsg = "true";
      processInstanceService.updateProcessDescriptor(processOid, descriptorDTO);
      return Response.ok(errorMsg, MediaType.APPLICATION_JSON).build();
   }

   /**
    * @param oid
    * @param withEvents
    * @return
    * @throws ResourceNotFoundException
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{oid}/activity-instances")
   @ResponseDescription("Response contains the list of all activity instance for the given process instance with hirarachy")
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO, org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO")
   public Response getAllActivityInstances(@PathParam("oid") Long oid,
         @QueryParam("withEvents") @DefaultValue("false") boolean withEvents) throws ResourceNotFoundException
   {
      return Response.ok(processInstanceService.getProcessSummary(oid).toJson(), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("startingActivityOID/{aiOid}")
   public Response findByStartingActivityOid(@PathParam("aiOid") Long aOid)
   {
      return Response.ok(processInstanceService.findByStartingActivityOid(aOid).toJson(), MediaType.APPLICATION_JSON)
            .build();
   }

   /**
    * @author Yogesh.Manware
    * @param processOid
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/address-book")
   public Response getAddressBook(@PathParam("oid") Long processOid)
   {
      return Response.ok(GsonUtils.toJsonHTMLSafeString(processInstanceService.getAddressBookDTO(processOid)),
            MediaType.APPLICATION_JSON).build();
   }

   /**
    * @author Yogesh.Manware
    * @param processOid
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/data-path-values{dataPathId:.*}")
   public Response getDataPathValues(@PathParam("oid") Long processOid, @PathParam("dataPathId") String dataPathId)
   {
      List<DataPathValueDTO> dataPathValuesDTO = processInstanceService.getDataPathValueFor(processOid, dataPathId);
      return Response.ok(GsonUtils.toJsonHTMLSafeString(dataPathValuesDTO), MediaType.APPLICATION_JSON).build();
   }

   @PUT
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/data-paths")
   public Response setDataPaths(@PathParam("oid") Long processOid, String postedData)
   {
      Map<String, Object> dataPaths = (Map) GsonUtils.extractMap(JsonDTO.getJsonObject(postedData), "dataPaths");
      Boolean status = getProcessInstanceService().setDataPaths(processOid, dataPaths);
      return Response.ok(status.toString(), MediaType.APPLICATION_JSON).build();
   }

   /**
    * @author Yogesh.Manware
    * @param processOid
    * @return
    * @throws Exception
    */
   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/documents{dataPathId:.*}")
   public Response uploadDocument(List<Attachment> attachments, @PathParam("oid") Long processOid,
         @PathParam("dataPathId") String dataPathId) throws Exception
   {
      if (StringUtils.isEmpty(dataPathId))
      {
         dataPathId = DmsConstants.PATH_ID_ATTACHMENTS;
      }

      Map<String, Object> result = processInstanceService.addProcessDocuments(processOid, attachments, dataPathId);

      return Response.ok(GsonUtils.toJsonHTMLSafeString(result)).build();
   }

   /**
    * @author Yogesh.Manware
    * @param processOid
    * @return
    * @throws Exception
    */
   @DELETE
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/documents/{dataPathId}/{documentId}")
   public Response removeDocument(List<Attachment> attachments, @PathParam("oid") Long processOid,
         @PathParam("dataPathId") String dataPathId, @PathParam("documentId") String documentId) throws Exception
   {
      if (StringUtils.isEmpty(dataPathId))
      {
         dataPathId = DmsConstants.PATH_ID_ATTACHMENTS;
      }

      processInstanceService.removeProcessDocument(processOid, dataPathId, documentId);

      return Response.ok(GsonUtils.toJsonHTMLSafeString(restCommonClientMessages.get("success.message"))).build();
   }

   /**
    * Populate the options with the post data.
    * 
    * @param options
    * @param postData
    * @return
    */
   public DataTableOptionsDTO populatePostData(DataTableOptionsDTO options, String postData)
   {
      List<DescriptorColumnDTO> availableDescriptors = processDefService.getDescriptorColumns(true);
      return ProcessInstanceUtils.populatePostData(options, postData, availableDescriptors);
   }

   /**
    * @return
    */
   public ProcessInstanceService getProcessInstanceService()
   {
      return processInstanceService;
   }

   /**
    * @param ProcessInstanceService
    */
   public void setProcessInstanceService(ProcessInstanceService processInstanceService)
   {
      this.processInstanceService = processInstanceService;
   }
}
