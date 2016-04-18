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

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.eclipse.stardust.common.error.AccessForbiddenException;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.query.ActivityFilter;
import org.eclipse.stardust.engine.api.query.ActivityInstanceQuery;
import org.eclipse.stardust.engine.api.query.ActivityStateFilter;
import org.eclipse.stardust.engine.api.query.DataFilter;
import org.eclipse.stardust.engine.api.query.FilterOrTerm;
import org.eclipse.stardust.engine.api.runtime.ActivityInstanceState;
import org.eclipse.stardust.engine.core.query.statistics.api.BenchmarkActivityStatisticsQuery;
import org.eclipse.stardust.engine.extensions.dms.data.DmsConstants;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.component.exception.NotificationMapException;
import org.eclipse.stardust.ui.web.rest.component.service.ActivityInstanceService;
import org.eclipse.stardust.ui.web.rest.component.service.DelegationComponent;
import org.eclipse.stardust.ui.web.rest.component.service.ParticipantSearchComponent;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.component.service.ProcessInstanceService;
import org.eclipse.stardust.ui.web.rest.component.util.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.component.util.TrafficLightViewUtils;
import org.eclipse.stardust.ui.web.rest.documentation.DTODescription;
import org.eclipse.stardust.ui.web.rest.documentation.RequestDescription;
import org.eclipse.stardust.ui.web.rest.documentation.ResponseDescription;
import org.eclipse.stardust.ui.web.rest.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceOutDataDTO;
import org.eclipse.stardust.ui.web.rest.dto.ColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.CompletedActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.dto.CriticalityDTO;
import org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO;
import org.eclipse.stardust.ui.web.rest.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.dto.NotificationMap;
import org.eclipse.stardust.ui.web.rest.dto.PendingActivitiesStatisticsDTO;
import org.eclipse.stardust.ui.web.rest.dto.PostponedActivitiesResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.dto.TrivialManualActivityDTO;
import org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO;
import org.eclipse.stardust.ui.web.rest.exception.PortalRestException;
import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.util.MapAdapter;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
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
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
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
   @DTODescription(response = "org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO")
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
   @RequestDescription("Post data needs to be JSON in the below format \r\n" 
         + "``` javascript\r\n" 
         + "[ \"oid1\",\"oid2\",...] //List of OIDs \r\n"
         + "```\r\n")
   @ResponseDescription("Returns the below JSON\n" 
         + "``` javascript\r\n" 
         + "{\"oid\" :\r\n" + 
         "    {\r\n" + 
         "        \"dataMappings\":[],\r\n" + 
         "        \"inOutData\":{}\r\n" + 
         "    }\r\n" + 
         "}\r\n"
         + "```\r\n")
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
         return Response.ok(AbstractDTO.toJson(getActivityInstanceService().getAllRelocationTargets(oid)),
               MediaType.APPLICATION_JSON).build();
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
      catch (AccessForbiddenException e)
      {
         trace.error(e, e);

         return Response.status(Status.BAD_REQUEST).entity("processportal.toolbars-workflowActions-relocation-dialog-notAuthorized").build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.status(Status.INTERNAL_SERVER_ERROR).entity("processportal.toolbars-workflowActions-relocation-dialog-internalServerError").build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/completeAll")
   @RequestDescription("Post data needs to be JSON in the below format \r\n" + 
         "``` javascript\r\n" +
         "[{\"oid\" : ,\r\n" + 
         "  \"dataMappings\":{},\r\n" + 
         "  \"outData\" : {}\r\n" + 
         "}\r\n" + 
         "]\r\n"
         + "```")
   @ResponseDescription("Returns the below JSON\r\n" + 
         "``` javascript\r\n" + 
         "{\"failure[{\"OID\":<number>,\"message\":<String>}],\r\n"
         + "\"success\":[{\"OID\":<number>,\"message\":<String>}]}\n" + 
         "```")
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
   @RequestDescription("POST JSON needs to be in the below format.\r\n"
         + "``` javascript\r\n"
         + "{ 'pendingActivityInstance' : {'OID' : 1}, \r\n "
         + "  'document': {'id' : String } "
         + "}\r\n"
         + "```")
   @ResponseDescription("Response will have List of \r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ProcessInstanceDTO")
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
   @ResponseDescription("Returns the below JSON\r\n" + 
         "``` javascript\r\n" + 
         "{\"failure[{\"OID\":<number>,\"message\":<String>}],\n" + 
         "\"success\":[{\"OID\":<number>,\"message\":<String>}]}\n" + 
         "```")
   @RequestDescription("Post data needs to be JSON in the below format\r\n"
         + "``` javascript\n"
         + "{'scope' : 'string',\r\n"
         + " 'activities' : []\r\n"
         + "}\r\n"
         + "```")
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
   @RequestDescription("The Request JSON should be in below format \r\n"
         + "``` javascript\r\n"
         + "{\r\n" + 
         "    searchText:String value //can be empty\r\n" + 
         "    participantType: String value //default=All, one of All, Users, Roles, Organizations, Department\r\n" + 
         "    limitedSearch: boolean value //default true, indicates scope of search around activities or across models\r\n" + 
         "    activities : Array of type Long//if it single activity and limitedSearch = false, all active models will searched,\r\n" + 
         "     //limitedSearch=false, but required always due to legacy code\r\n" + 
         "     //in case of multiple activities, common participants are searched\r\n" + 
         "    disableAdministrator = boolean value, // default is false, Indicates that the predefined <code>ADMINISTRATOR</code> role is not a valid delegate\r\n" + 
         "    excludeUserType = boolean value // default is false, used in worklist configuration flow where only standard model participants are valid\r\n" + 
         "}\r\n"
         + "```")
   @ResponseDescription("Returns the below JSON(List of ParticipantDTO)\r\n"
         + "``` javascript\r\n"
         + "[{\n" + 
         "    qualifiedId : string value //e.g. {M2015}Administrator\r\n" + 
         "    OID : long value //for departments OID is must\r\n" + 
         "    name : string value //I18ned participant Name\r\n" + 
         "    type : string value //any of the [USER, USERGROUP, ROLE, SCOPED_ROLE, ORGANIZATION, SCOPED_ORGANIZATION, DEPARTMENT]\r\n" + 
         "    onlineStatus : boolean //[true or false]//applicable to user only\r\n" + 
         "}, ...]\r\n"
         + "```")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO")
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
   @ResponseDescription("Returns the below JSON(List of ParticipantDTO)\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO")
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
   @RequestDescription("The post data needs to be JSON format as below\r\n"
         + "``` javascript\r\n"
         + "{\n" + 
         "    activities : [OId1, OId2....],\n" + 
         "    participant: qualifiedId or OID,\n" + 
         "    participantType: String value //one of[USER, USERGROUP, ROLE, SCOPED_ROLE, ORGANIZATION, SCOPED_ORGANIZATION, DEPARTMENT]\n" + 
         "     \n" + 
         "    //Optional\n" + 
         "    updateNotes: boolean, //indicates if notes needs to be updated\n" + 
         "    buildDefaultNotes:boolean,   //indicates if default notes needs to be generated in case not provided with request\n" + 
         "    notes: String //notes content\n" + 
         "    delegateCase: boolean,\n" + 
         "    context: String value  // varies based on type of activity\n" + 
         "    activityOutData : {\n" + 
         "        key1 : {\n" + 
         "            value\n" + 
         "        },\n" + 
         "        key2 : value\n" + 
         "    } //This is optional and only required when delegated from activity panel\n" + 
         "}\r\n"
         + "```")
   @ResponseDescription("Returns the below JSON\r\n" + 
         "``` javascript\r\n" + 
         "{\"failure[{\"OID\":<number>,\"message\":<String>}],\r\n"
         + "\"success\":[{\"OID\":<number>,\"message\":<String>}]}\n" + 
         "```")
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
    * 
    * @author Yogesh.Manware
    * @param processOid
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/correspondence-out")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.response.FolderDTO")
   public Response getCorrespondenceOutFolder(@PathParam("oid") Long activityOid)
   {
      FolderDTO folderDto = activityInstanceService.getCorrespondenceOutFolder(activityOid);
      // TODO move jsonHelper and MapAdapter to Portal-Common and then modify GsonUtils
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
   @RequestDescription("Post data needs to be JSON in the below format\r\n" +
         "``` javascript\r\n" + 
         "{\n" + 
         "oid :  //oid of the activity,\n" + 
         "state //ActivityInstanceState\n" + 
         " \n" + 
         "}\r\n"
         + "```")
   @ResponseDescription("Returns the below JSON\r\n"
         + "``` javascript\r\n"
         + "{\"failure\":[{\"OID\": <number> }],\r\n"
         + "\"success\":[{\"OID\":<number> }]"
         + "}\r\n"
         + "```")
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
   @ResponseDescription("The response json will have list of CriticalityDTO\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute.CriticalityDTO")
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
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.InstanceCountsDTO")
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
   @DTODescription(request="org.eclipse.stardust.ui.web.rest.dto.DataTableOptionsDTO")
   @ResponseDescription("Return JSON with below information\r\n"
         + "``` javascript\r\n"
         + "{\n" 
         + "  totalCount: <number>\n" 
         + "  list: [] // Activity Instance Details\n" 
         + "}\r\n"
         + "```")
   public Response getAllActivities(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
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
   @RequestDescription("The post data JSON format as below\r\n"
         + "``` javascript\r\n"
         + "{\r\n"
         + "bOids :[4]\r\n" + 
         "benchmarkCategory : 1\r\n" + 
         "dateType : 'BUSINESS_DATE' \r\n" + 
         "dayOffset : -3\n" + 
         "descriptors : {fetchAll: true, visibleColumns: []}\n" + 
         "drillDownType : 'PROCESS_WORKITEM'\r\n" + 
         "fetchTrivialManualActivities : false\r\n" + 
         "processActivitiesMap : {{DocumentDescriptor}Document_Descriptor: [\"{DocumentDescriptor}SecondActivity\"]}\n" + 
         "state: 'Active'\r\n"
         + "}\r\n"
         + "```")
   @ResponseDescription("Response will have list of ActivityInstanceDTO\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
   public Response getActivitiesForTLVByCategory(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      try
      {
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
         populatePostData(options, postData);

         JsonMarshaller jsonIo = new JsonMarshaller();
         JsonObject postJSON = jsonIo.readJsonObject(postData);

         ActivityInstanceQuery query = new ActivityInstanceQuery();

         String drillDownType = postJSON.getAsJsonPrimitive("drillDownType").getAsString();
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

            JsonObject processActivityMap = postJSON.getAsJsonObject("processActivitiesMap");
            Set<Entry<String, JsonElement>> processActivityMapEntrySet = processActivityMap.entrySet();

            Map<String, List<String>> processActivitiesMap = new HashMap<String, List<String>>();
            for (Entry<String, JsonElement> entry : processActivityMapEntrySet)
            {
               JsonArray processActivityArray = entry.getValue().getAsJsonArray();

               Type processActivityType = new TypeToken<List<String>>()
               {
               }.getType();
               List<String> activities = new ArrayList<String>();
               if (null != processActivityArray)
               {
                  activities = new Gson().fromJson(processActivityArray.toString(), processActivityType);

               }
               processActivitiesMap.put(entry.getKey(), activities);
            }
            String state = postJSON.getAsJsonPrimitive("state").getAsString();

            FilterOrTerm processActivityFilter = query.getFilter().addOrTerm();
            for (String processId : processActivitiesMap.keySet())
            {
               for (String activityId : processActivitiesMap.get(processId))
               {
                  processActivityFilter.add(ActivityFilter.forProcess(activityId, processId));
               }

            }

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

            if (dateType.equals(PredefinedConstants.BUSINESS_DATE))
            {
               FilterOrTerm businessDateFilter = query.getFilter().addOrTerm();
               for (String processId : processActivitiesMap.keySet())
               {
                  businessDateFilter.add((DataFilter.between(TrafficLightViewUtils.getModelName(processId)
                        + PredefinedConstants.BUSINESS_DATE, startDate.getTime(), endDate.getTime())));
               }
            }
            else
            {
               query.where(ActivityInstanceQuery.START_TIME.between(startDate.getTimeInMillis(),
                     endDate.getTimeInMillis()));
            }

            if (postJSON.getAsJsonPrimitive("benchmarkCategory") != null)
            {
               Long benchmarkCategory = postJSON.getAsJsonPrimitive("benchmarkCategory").getAsLong();
               query.where(ActivityInstanceQuery.BENCHMARK_VALUE.isEqual(benchmarkCategory));
            }
            else
            {
               query.where(ActivityInstanceQuery.BENCHMARK_VALUE.greaterThan(0l));
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

         }
         else
         {
            // for business object by activities

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
               oidsFilter.add(ActivityInstanceQuery.OID.isEqual(oid));
            }

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
   @RequestDescription("Post data needs to be JSON in the below format\r\n" + 
         "``` javascript\r\n" + 
         "{\n" + 
         "  activityOID : {activityOID}\n" + 
         "}\r\n"
         + "```")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.NotificationMap")
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
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
   public Response activate(@QueryParam("interactionAware") @DefaultValue("false") Boolean interactionAware, String postedData)
   {
      try
      {
         Map<String, Object> data = JsonDTO.getAsMap(postedData);
         ActivityInstanceDTO dto = activityInstanceService.activate(Long.valueOf(data.get("activityOID").toString()), interactionAware);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(dto), MediaType.APPLICATION_JSON).build();
      }
      catch (NotificationMapException e)
      {
         ResponseBuilder rb = Response.ok(GsonUtils.toJsonHTMLSafeString(e.getNotificationMap()), MediaType.APPLICATION_JSON);
         rb.status(e.getStatus());
         return rb.build();
      }
   }

   /**
    * Gets the completed activities by process
    * 
    * @return
    */
   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/statistics/completedActivities")
   @ResponseDescription("The response will contain list of CompletedActivitiesStatisticsDTO\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.CompletedActivitiesStatisticsDTO")
   public Response getStatsForCompletedActivities()
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
   @ResponseDescription("The response will contain list of PostponedActivitiesResultDTO\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.PostponedActivitiesResultDTO")
   public Response getStatsByPostponedActivities()
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
   @ResponseDescription("The response will contain list of PendingActivitiesStatisticsDTO\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.PendingActivitiesStatisticsDTO")
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
   @ResponseDescription("The response will contain list of SelectItemDTO having roleId and name\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.SelectItemDTO")
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
   @ResponseDescription("The response will contain list of ColumnDTO having qualifiedId as id and Participant name as label\r\n")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ColumnDTO")
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
   @Path("/byOids")
   @ResponseDescription("The Response will contain list of ActivityInstanceDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
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
         DataTableOptionsDTO options = new DataTableOptionsDTO(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));
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
   @ResponseDescription("The Response will contain list of CompletedActivitiesStatisticsDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.CompletedActivitiesStatisticsDTO")
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
   @ResponseDescription("The response will contain list of ActivityInstanceDTO")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
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
    * @author Yogesh.Manware
    * @param attachments
    * @param activityOid
    * @param dataPathId
    * @return
    * @throws Exception
    */
   @POST
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{oid}/documents")
   public Response uploadDocuments(List<Attachment> attachments, @PathParam("oid") Long activityOid,
         @PathParam("dataPathId") String dataPathId) throws Exception
   {
      if (StringUtils.isEmpty(dataPathId))
      {
         dataPathId = DmsConstants.PATH_ID_ATTACHMENTS;
      }

      Map<String, Object> result = activityInstanceService.addProcessDocuments(activityOid, attachments);

      return Response.ok(GsonUtils.toJsonHTMLSafeString(result)).build();
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/complete/{activityOid}")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
   public Response complete(@PathParam("activityOid") Long activityOid)
   {
      try
      {
         ActivityInstanceDTO dto = activityInstanceService.complete(activityOid);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(dto), MediaType.APPLICATION_JSON).build();
      }
      catch (NotificationMapException e)
      {
         ResponseBuilder rb = Response.ok(GsonUtils.toJsonHTMLSafeString(e.getNotificationMap()), MediaType.APPLICATION_JSON);
         rb.status(Status.BAD_REQUEST);
         return rb.build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/suspend-and-save/{oid}")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
   public Response suspendAndSave(@PathParam("oid") Long oid, @QueryParam("toUser") @DefaultValue("false") boolean toUser)
   {
      try
      {
         ActivityInstanceDTO dto = activityInstanceService.suspend(oid, toUser, true);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(dto), MediaType.APPLICATION_JSON).build();
      }
      catch (NotificationMapException e)
      {
         ResponseBuilder rb = Response.ok(GsonUtils.toJsonHTMLSafeString(e.getNotificationMap()), MediaType.APPLICATION_JSON);
         rb.status(Status.BAD_REQUEST);
         return rb.build();
      }
   }

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/suspend/{oid}")
   @DTODescription(response="org.eclipse.stardust.ui.web.rest.dto.ActivityInstanceDTO")
   public Response suspend(@PathParam("oid") Long oid, @QueryParam("toUser") @DefaultValue("false") boolean toUser)
   {
      try
      {
         ActivityInstanceDTO dto = activityInstanceService.suspend(oid, toUser, false);
         return Response.ok(GsonUtils.toJsonHTMLSafeString(dto), MediaType.APPLICATION_JSON).build();
      }
      catch (NotificationMapException e)
      {
         ResponseBuilder rb = Response.ok(GsonUtils.toJsonHTMLSafeString(e.getNotificationMap()), MediaType.APPLICATION_JSON);
         rb.status(Status.BAD_REQUEST);
         return rb.build();
      }
   }

   /**
    * 
    * @param options
    * @param postData
    */
   private void populatePostData(DataTableOptionsDTO options, String postData)
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
