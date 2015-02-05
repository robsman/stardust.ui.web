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
package org.eclipse.stardust.ui.web.rest;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.faces.FacesException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.exception.PortalRestException;
import org.eclipse.stardust.ui.web.rest.service.ActivityInstanceService;
import org.eclipse.stardust.ui.web.rest.service.DelegationComponent;
import org.eclipse.stardust.ui.web.rest.service.ParticipantSearchComponent;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceOutDataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialManualActivityDTO;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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
   
   private final JsonMarshaller jsonIo = new JsonMarshaller();

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{activityInstanceOid: \\d+}")
   public Response getActivityInstance(@PathParam("activityInstanceOid")
   long activityInstanceOid)
   {
      try
      {
         ActivityInstanceDTO aiDTO = getActivityInstanceService().getActivityInstance(
               activityInstanceOid);

         return Response.ok(aiDTO.toJson(), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
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

            // gson.toJson(details) is not working, inOutData is not Serialized. hence below workaround
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
         Gson gson = new Gson();
         String jsonOutput = gson.toJson(output);
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

   @POST
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/completeAll")
   public Response completeAll(String postedData)
   {
      try
      {
         List<ActivityInstanceOutDataDTO> activities = ActivityInstanceOutDataDTO.toList(postedData);

         Map<Long, String> result = getActivityInstanceService().completeAll(activities, "default");

         String jsonOutput = GsonUtils.stringify(result);

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
   public Response getProcessesAttachments(@PathParam("activityInstanceOid")
   long activityInstanceOid)
   {
      try
      {
         List<DocumentDTO> processAttachments = getActivityInstanceService()
               .getProcessAttachmentsForActivityInstance(activityInstanceOid);

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
         ActivityInstanceDTO aiDTO = gson.fromJson(json.get("pendingActivityInstance"),
               ActivityInstanceDTO.class);
         DocumentDTO documentDTO = gson.fromJson(json.get("document").getAsJsonObject(),
               DocumentDTO.class);

         List<ProcessInstanceDTO> processInstances = getActivityInstanceService()
               .completeRendezvous(aiDTO.oid, documentDTO.getUuid());

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
      //postedData = "{scope: 'activity', activities : [11]}";
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
   public Response searchParticipant(String postedData)
   {
      // postedData = "{searchText: '', activities=[8], participantType='All', limitedSearch=false, disableAdministrator=false, excludeUserType=false}";
      // postedData = "{activities=[8], limitedSearch=false}";
      return Response.ok(participantSearchComponent.searchParticipants(postedData), MediaType.APPLICATION_JSON).build();
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
      //postedData = "{activities:[12], participantType:'User', participant:1}"; 
      //postedData = "{activities:[8], participantType:'User', participant:2, department:false, activityData: {'Country':{'id':'India', 'States':[{ id: 'dd', name:'nameo'}, { id: 'dd2', name:'nameo2'}] } } }"; 
      //postedData = "{activities:[8], participant:{}, department:false, activityData: {Country:{id:'India',States:[{id:'MAH',cities:[{id:'Pn',Name:'Pune'},{id:'Pn2',Name:'Pune2'}]},{id:'Dl',cities:[{id:'DL1',Name:'Delhi'}]}]}} }";     
      
      return Response.ok(delegationComponent.delegate(postedData), MediaType.APPLICATION_JSON).build();
   }
    
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/availableCriticalities")
    public Response getAvailiableCriticalities()
    {
       try
       {
          
          return Response.ok(AbstractDTO.toJson(getActivityInstanceService().getCriticalities()), MediaType.APPLICATION_JSON).build();
       }
       catch (Exception e)
       {
          trace.error(e, e);

          return Response.serverError().build();
       }
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/allActivityStates")
    public Response getAllActivityStates()
    {
       try
       {
          
          return Response.ok(AbstractDTO.toJson(getActivityInstanceService().getAllActivityStates()), MediaType.APPLICATION_JSON).build();
       }
       catch (Exception e)
       {
          trace.error(e, e);

          return Response.serverError().build();
       }
    }
	
    
   /** 
    * @author Yogesh.Manware
    * @param httpRequest
    * @param paramName
    * @param defaultValue
    * @return
    */
   private String getParam(HttpServletRequest httpRequest, String paramName, String defaultValue)
   {
      Map<String, String[]> paramMap = httpRequest.getParameterMap();
      if (paramMap != null && paramMap.get(paramName) != null && paramMap.get(paramName)[0] != null)
      {
         System.out.println(paramName + " : " + paramMap.get(paramName)[0]);
         return paramMap.get(paramName)[0];
      }

      return defaultValue;
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
