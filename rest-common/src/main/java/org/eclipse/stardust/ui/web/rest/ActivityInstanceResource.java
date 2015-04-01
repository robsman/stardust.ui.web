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
import java.util.ArrayList;
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

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.error.ObjectNotFoundException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.dto.ProcessInstanceDetails;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.engine.api.query.ProcessDefinitionQuery;
import org.eclipse.stardust.engine.api.runtime.ProcessDefinitions;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.engine.api.runtime.SubprocessSpawnInfo;
import org.eclipse.stardust.ui.web.common.util.GsonUtils;
import org.eclipse.stardust.ui.web.rest.exception.PortalRestException;
import org.eclipse.stardust.ui.web.rest.service.ActivityInstanceService;
import org.eclipse.stardust.ui.web.rest.service.DelegationComponent;
import org.eclipse.stardust.ui.web.rest.service.ParticipantSearchComponent;
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbortNotificationDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityInstanceOutDataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.InstanceCountsDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JoinProcessDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.JsonDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.RelatedProcessesDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SelectItemDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.SwitchProcessDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.TrivialManualActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.utils.ActivityTableUtils;
import org.eclipse.stardust.ui.web.rest.service.utils.RelatedProcessSearchUtils;
import org.eclipse.stardust.ui.web.viewscommon.common.PortalException;
import org.eclipse.stardust.ui.web.viewscommon.messages.MessagesViewsCommonBean;
import org.eclipse.stardust.ui.web.viewscommon.utils.AuthorizationUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.CommonDescriptorUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.I18nUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessInstanceUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.ServiceFactoryUtils;
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

   @Autowired
   ProcessDefinitionService processDefService;
   
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

         String jsonOutput  = getActivityInstanceService().completeAll(activities, "default");

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
               .completeRendezvous(aiDTO.activityOID, documentDTO.uuid);

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
     */
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/searchAllParticipants/{searchText}/{maxMatches}")
   public Response searchAllParticipant(@PathParam("searchText") String searchText ,@PathParam("maxMatches") int maxMatches )
   {
      return Response.ok(participantSearchComponent.searchAllParticipants( searchText, maxMatches), MediaType.APPLICATION_JSON).build();
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
     * @author Nikhil.Gahlot
     * @param processInstanceOID
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/spawnableProcesses")
   public Response spawnableProcesses(String postedData, @QueryParam("type") String type)
   {
    	try {
    		List<Long> processInstOIDs = JsonDTO.getAsList(postedData, Long.class);
    		List<ProcessInstance> processInstances = new ArrayList<ProcessInstance>();
    		for (Long processInstOID : processInstOIDs) {
    			ProcessInstance processInstance = getProcessInstance(processInstOID);
    			processInstances.add(processInstance);
    		}
    		
    		if (CollectionUtils.isNotEmpty(processInstances)) {
    		   
    		   Integer modelOID = ProcessInstanceUtils.getProcessModelOID(processInstances);

    	         if (null == modelOID)
    	         {
    	            MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
    	            String mesg = propsBean.getString("views.switchProcessDialog.pisInDiffModels");
    	            trace.info(mesg);
    	            return Response.status(417).entity(mesg).build();
    	         }
    		   
	    		ProcessDefinitions pds = ServiceFactoryUtils.getQueryService().getProcessDefinitions(
		    	ProcessDefinitionQuery.findStartable(modelOID));
	    		
		        Object responseObj = pds;
		        if ("select".equals(type)) {
		    	    Map<String, ProcessDefinition> pdMap = CollectionUtils.newHashMap();
	
		            for (ProcessDefinition pd : pds)
		            {
		               pdMap.put(pd.getId(), pd);
		            }
	
		            List<ProcessDefinition> filteredPds = new ArrayList<ProcessDefinition>(pdMap.values());
		            ProcessDefinitionUtils.sort(filteredPds);
	
		            List<SelectItemDTO> items = new ArrayList<SelectItemDTO>();
		            for (ProcessDefinition pd : pdMap.values())
		            {
		        	  SelectItemDTO selectItem = new SelectItemDTO();
		        	  selectItem.label = I18nUtils.getProcessName(pd);
		        	  selectItem.value = pd.getQualifiedId();
		        	  items.add(selectItem);
		            }
		          
		            responseObj = items;
		        }
	
		        return Response.ok(GsonUtils.toJsonHTMLSafeString(responseObj), MediaType.APPLICATION_JSON).build();
    		}
    		return Response.ok("", MediaType.APPLICATION_JSON).build();
    	} catch (Exception e) {
			trace.error(e, e);
	        return Response.serverError().build();
		}
   }
    
   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/checkIfProcessesAbortable")
   public Response checkIfProcessesAbortable(String postedData, @QueryParam("type") String type)
   {
      try
      {
         List<Long> processInstOIDs = JsonDTO.getAsList(postedData, Long.class);
         MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
         List<AbortNotificationDTO> notAbortableProcesses = new ArrayList<AbortNotificationDTO>();

         for (Long processInstOID : processInstOIDs)
         {
            ProcessInstance processInstance = getProcessInstance(processInstOID);
            processInstance = ProcessInstanceUtils.getRootProcessInstance(processInstance, true);

            ProcessInstanceDTO processInstanceDTO = new ProcessInstanceDTO();
            processInstanceDTO.processName = ProcessInstanceUtils.getProcessLabel(processInstance);
            processInstanceDTO.oid = processInstance.getOID();

            AbortNotificationDTO switchNotificationDTO = null;

            if ("abortandstart".equals(type))
            {
               if (!AuthorizationUtils.hasAbortPermission(processInstance))
               {
                  switchNotificationDTO = new AbortNotificationDTO();
                  switchNotificationDTO.statusMessage = propsBean.getString("common.authorization.msg");
               }
               else if (!ProcessInstanceUtils.isAbortable(processInstance))
               {
                  switchNotificationDTO = new AbortNotificationDTO();
                  switchNotificationDTO.statusMessage = propsBean.getString("common.notifyProcessAlreadyAborted");
               }
               else if (processInstance.isCaseProcessInstance())
               {
                  switchNotificationDTO = new AbortNotificationDTO();
                  switchNotificationDTO.statusMessage = propsBean.getString("views.switchProcessDialog.caseAbort.message");
               }
            }
            else if ("abortandjoin".equals(type))
            {
               if (processInstance.isCaseProcessInstance() && !AuthorizationUtils.hasManageCasePermission(processInstance))
               {
                  switchNotificationDTO = new AbortNotificationDTO();
                  switchNotificationDTO.statusMessage = propsBean.getString("common.authorization.msg");
               }
               else if (!ProcessInstanceUtils.isAbortableState(processInstance))
               {
                  switchNotificationDTO = new AbortNotificationDTO();
                  switchNotificationDTO.statusMessage = propsBean.getString("common.notifyProcessAlreadyAborted");
               }
            }

            if (switchNotificationDTO != null)
            {
               switchNotificationDTO.abortedProcess = processInstanceDTO;

               notAbortableProcesses.add(switchNotificationDTO);
            }
         }
         return Response.ok(GsonUtils.toJsonHTMLSafeString(notAbortableProcesses), MediaType.APPLICATION_JSON).build();
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
    @Path("/{activityInstanceOid: \\d+}/spawnProcess")
   public Response spawnProcess(@PathParam("activityInstanceOid") long activityInstanceOid, String processNamesData)
   {
    
      try {
		List<ProcessInstanceDTO> subprocessInstances = new ArrayList<ProcessInstanceDTO>();
		  ActivityInstanceDTO aiDTO = getActivityInstanceService().getActivityInstance(
		            activityInstanceOid);
		  if (aiDTO != null) {
			String[] processNames = GsonUtils.fromJson(processNamesData, String[].class);
			if (CollectionUtils.isNotEmpty(processNames))
		    {
		       List<SubprocessSpawnInfo> infoList = CollectionUtils.newArrayList();
		       for (String process : processNames)
		       {
		          SubprocessSpawnInfo info = new SubprocessSpawnInfo(process, true, null);
		          infoList.add(info);
		       }
		       List<ProcessInstance> result = ServiceFactoryUtils.getWorkflowService().spawnSubprocessInstances(aiDTO.processInstance.oid,
		    		   infoList);
		       for (ProcessInstance pi : result) {
		    	   ProcessInstanceDTO dto = new ProcessInstanceDTO();
		    	   dto.processName = pi.getProcessName();
		    	   dto.oid = pi.getOID();
		    	   subprocessInstances.add(dto);
		       }
		    }
		  }
		  return Response.ok(GsonUtils.toJsonHTMLSafeString(subprocessInstances), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);
	        return Response.serverError().build();
		}
   }
    
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/switchProcess")
   public Response switchProcess(String processData)
   {
      try {
    	  List<AbortNotificationDTO> newProcessInstances = new ArrayList<AbortNotificationDTO>();
    	  SwitchProcessDTO processDTO = GsonUtils.fromJson(processData, SwitchProcessDTO.class);
    	  List<Long> processInstOIDs = processDTO.processInstaceOIDs;
    	  
    	  MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
    	  
    	  for (Long processInstOID : processInstOIDs) {
    		  ProcessInstance srcProcessInstance = getProcessInstance(processInstOID);
    		  
    		  // First check the permission
    		  if (!AuthorizationUtils.hasAbortPermission(srcProcessInstance) || !ProcessInstanceUtils.isAbortable(srcProcessInstance)
    				  || srcProcessInstance.isCaseProcessInstance()) {
    			  continue;
	    	  }

			  ProcessInstanceDTO source = new ProcessInstanceDTO();
			  source.processName = ProcessInstanceUtils.getProcessLabel(srcProcessInstance);
			  source.oid = srcProcessInstance.getOID();
			  
			  ProcessInstanceDTO target = null;
			  
			  AbortNotificationDTO switchNotificationDTO = new AbortNotificationDTO();
			  switchNotificationDTO.abortedProcess = source;
			  
			  try {
				  ProcessInstance pi = ServiceFactoryUtils.getWorkflowService().spawnPeerProcessInstance(
						  processInstOID, processDTO.processId, true, null, true, processDTO.linkComment);
				  
				  
				  if (pi != null) {
					  target = new ProcessInstanceDTO();
					  target.processName = ProcessInstanceUtils.getProcessLabel(pi);
					  target.oid = pi.getOID();
					  
					  switchNotificationDTO.targetProcess = target;
					  switchNotificationDTO.statusMessage = propsBean.getString("common.success");
				  }
			  } catch (Exception e) {
				  trace.error("Unable to abort the process with oid: " + processInstOID + " and target process id: " + processDTO.processId);
				  trace.error(e, e);
				  
				  switchNotificationDTO.statusMessage = propsBean.getString("common.fail");
			  }
			
        	  newProcessInstances.add(switchNotificationDTO);
		  }
    	  
		  return Response.ok(GsonUtils.toJsonHTMLSafeString(newProcessInstances), MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			trace.error(e, e);
	        return Response.serverError().build();
		}
   }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/abortAndJoinProcess")
    public Response abortAndJoinProcess(String processData)
    {
    	JoinProcessDTO processDTO = GsonUtils.fromJson(processData, JoinProcessDTO.class);
    	Long sourceProcessInstanceOid = Long.parseLong(processDTO.sourceProcessOID);
    	Long targetProcessInstanceOid = Long.parseLong(processDTO.targetProcessOID);
    	
    	ProcessInstance srcProcessInstance = getProcessInstance(sourceProcessInstanceOid);
    	
    	ProcessInstance targetProcessInstance = null;
    	
    	if (!srcProcessInstance.isCaseProcessInstance()) {
    		targetProcessInstance = ServiceFactoryUtils.getWorkflowService().joinProcessInstance(
        			sourceProcessInstanceOid, targetProcessInstanceOid, processDTO.linkComment);
    	} else {
    		  targetProcessInstance = ServiceFactoryUtils.getWorkflowService().mergeCases(
    				  sourceProcessInstanceOid, new long[] {sourceProcessInstanceOid}, processDTO.linkComment);
    		  
              CommonDescriptorUtils.reCalculateCaseDescriptors(srcProcessInstance);
              CommonDescriptorUtils.reCalculateCaseDescriptors(targetProcessInstance);
    	}
    	
    	MessagesViewsCommonBean propsBean = MessagesViewsCommonBean.getInstance();
    	
    	AbortNotificationDTO joinNotificationDTO = new AbortNotificationDTO();
    	
    	ProcessInstanceDTO source = new ProcessInstanceDTO();
    	source.processName = ProcessInstanceUtils.getProcessLabel(srcProcessInstance);
    	source.oid = srcProcessInstance.getOID();
    	joinNotificationDTO.abortedProcess = source;
    	
    	if (targetProcessInstance != null) {
	    	ProcessInstanceDTO target = new ProcessInstanceDTO();
	    	target.processName = ProcessInstanceUtils.getProcessLabel(targetProcessInstance);
	    	target.oid = targetProcessInstance.getOID();
	    	joinNotificationDTO.targetProcess = target;
    	}
    			
    	joinNotificationDTO.abortedProcess = source;
    	
    	joinNotificationDTO.statusMessage = propsBean.getString("common.success");
    	
    	return Response.ok(GsonUtils.toJsonHTMLSafeString(joinNotificationDTO), MediaType.APPLICATION_JSON).build();
    }
    
    
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getRelatedProcesses")
    public Response getRelatedProcesses(String processData, @QueryParam("matchAny") String matchAnyStr, @QueryParam("case") String caseStr)
    {
    	List<Long> processInstOIDs = JsonDTO.getAsList(processData, Long.class);
    	boolean matchAny = "true".equals(matchAnyStr);
    	boolean isCase = "true".equals(caseStr);
    	
    	boolean searchCases = !isCase;
    	
    	List<ProcessInstance> sourceProcessInstances = new ArrayList<ProcessInstance>();
    	for (Long processInstOID : processInstOIDs) {
  		  ProcessInstance srcProcessInstance = getProcessInstance(processInstOID);
  		  sourceProcessInstances.add(srcProcessInstance);
    	}
    	
    	List<ProcessInstance> result = RelatedProcessSearchUtils.getProcessInstances(sourceProcessInstances, matchAny, searchCases);
    	
    	List<RelatedProcessesDTO> relatedProcesses = new ArrayList<RelatedProcessesDTO>();
    	
    	for (ProcessInstance pi : result) {
    	    relatedProcesses.add(getRelatedProcessesDTO(pi));
        }
    	
    	return Response.ok(GsonUtils.toJsonHTMLSafeString(relatedProcesses), MediaType.APPLICATION_JSON).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/allCounts")
    public Response getAllCounts( )
    {
       try
       {
         InstanceCountsDTO acitivityInstanceCountDTO = getActivityInstanceService().getAllCounts();
         return Response.ok(GsonUtils.toJsonHTMLSafeString( acitivityInstanceCountDTO ), MediaType.APPLICATION_JSON).build();
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
    public Response getAllActivities(
          @QueryParam("skip") @DefaultValue("0") Integer skip,
          @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
          @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
          @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
    {
       try
       {
          Options options = new Options(pageSize, skip, orderBy,
                "asc".equalsIgnoreCase(orderByDir));
          populatePostData(options, postData);
          QueryResultDTO resultDTO = getActivityInstanceService().getAllInstances(options);
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
     * 
     * @param pi
     * @return
     */
	private RelatedProcessesDTO getRelatedProcessesDTO(ProcessInstance pi) {
		RelatedProcessesDTO dto = new RelatedProcessesDTO();
		MessagesViewsCommonBean COMMON_MESSAGE_BEAN = MessagesViewsCommonBean.getInstance();
		ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(pi.getModelOID(),
                pi.getProcessID());
		
		dto.processName = I18nUtils.getProcessName(processDefinition);;
	    dto.oid = pi.getOID();
		if (pi.getPriority() == 1) {
			dto.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.high");
		} else if (pi.getPriority() == -1) {
			dto.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.low");
		} else {
			dto.priority = COMMON_MESSAGE_BEAN.getString("common.priorities.normal");
		}
		
		dto.descriptorValues = ((ProcessInstanceDetails) pi).getDescriptors();
	    dto.startTime = pi.getStartTime();
	    
	    dto.caseInstance = pi.isCaseProcessInstance();
      if (pi.isCaseProcessInstance())
      {
         dto.caseOwner = ProcessInstanceUtils.getCaseOwnerName(pi);
      }
      else
      {
         dto.caseOwner = null;
      }
		
		return dto;
	}
	
	/**
	 * 
	 * @param processInstanceOID
	 * @return
	 */
	private ProcessInstance getProcessInstance(long processInstanceOID)
	{
	    return ProcessInstanceUtils.getProcessInstance(Long.valueOf(processInstanceOID));
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
