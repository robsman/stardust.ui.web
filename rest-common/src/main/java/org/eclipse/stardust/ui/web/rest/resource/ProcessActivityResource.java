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

import java.util.Iterator;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.model.ProcessDefinition;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.common.ProcessSearchParameterConstants;
import org.eclipse.stardust.ui.web.rest.service.ProcessActivityService;
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.dto.ActivityDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.viewscommon.utils.ProcessDefinitionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Aditya.Gaikwad
 *
 */
@Path("/processActivity")
public class ProcessActivityResource
{

   public static final Logger trace = LogManager.getLogger(ProcessActivityResource.class);

   @Autowired
   private ProcessActivityService processActivityService;

   @Autowired
   private ProcessDefinitionService processDefinitionService;

   @POST
   @Consumes(MediaType.APPLICATION_JSON)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/performSearch")
   public Response performSearch(@QueryParam("skip") @DefaultValue("0") Integer skip,
         @QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
         @QueryParam("orderBy") @DefaultValue("oid") String orderBy,
         @QueryParam("orderByDir") @DefaultValue("asc") String orderByDir, String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      JsonObject processSearchCriteriaJson = postJSON.getAsJsonObject("processSearchCriteria");

      ProcessSearchCriteriaDTO processSearchCriteria = null;

      try
      {
         processSearchCriteria = getProcessSearchCriteria(processSearchCriteriaJson);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      Options options = new Options(pageSize, skip, orderBy, "asc".equalsIgnoreCase(orderByDir));

      List<DescriptorColumnDTO> availableDescriptors = processDefinitionService.getDescriptorColumns(true);

      QueryResultDTO queryResultDTO = getProcessActivityService().performSearch(options, postData,
            processSearchCriteria, availableDescriptors);

      return Response.ok(queryResultDTO.toJson(), MediaType.APPLICATION_JSON).build();
   }

   /**
    * @param postData
    * @throws Exception
    */
   private ProcessSearchCriteriaDTO getProcessSearchCriteria(JsonObject processSearchCriteriaJson) throws Exception
   {

      List<ProcessDefinitionDTO> allProcesses = null;
      List<ProcessDefinitionDTO> processDTOList = CollectionUtils.newArrayList();
      List<ActivityDTO> activites = CollectionUtils.newArrayList();

      JsonElement selectedProcess = processSearchCriteriaJson.remove("procSrchProcessSelected");
      JsonArray asJsonArray = selectedProcess.getAsJsonArray();

      JsonElement descriptors = processSearchCriteriaJson.remove("descriptors");
      JsonObject descriptorsJsonObject = descriptors.getAsJsonObject();
      JsonElement formatted = descriptorsJsonObject.get("formatted");
      JsonObject formattedJsonObject = formatted.getAsJsonObject();

      for (int i = 0; i < asJsonArray.size(); i++)
      {
         String procDefID = asJsonArray.get(i).getAsString();
         if (procDefID != null && procDefID.equals("All"))
         {
            if (!processSearchCriteriaJson.get("showAuxiliaryProcess").getAsBoolean())
            {
               allProcesses = getProcessDefinitionService().getAllBusinessRelevantProcesses(false);
            }
            else
            {
               allProcesses = getProcessDefinitionService().getAllUniqueProcess(false);
            }

            filterProcessDefinitionList(processSearchCriteriaJson, allProcesses);

            processDTOList.addAll(allProcesses);
            break;
         }

         ProcessDefinition processDefinition = ProcessDefinitionUtils.getProcessDefinition(procDefID);
         ProcessDefinitionDTO processDefinitionDTO = DTOBuilder.build(processDefinition, ProcessDefinitionDTO.class);
         
         processDTOList.add(processDefinitionDTO);
      }

      if (processSearchCriteriaJson.get("filterObject").getAsInt() == 1)
      {
         JsonElement selectedActivities = processSearchCriteriaJson.remove("activitySrchSelected");
         JsonArray activitiesArray = selectedActivities.getAsJsonArray();
         for (int j = 0; j < activitiesArray.size(); j++)
         {
            String activityRuntimeElementOid = activitiesArray.get(j).getAsString();
            if (activityRuntimeElementOid != null && activityRuntimeElementOid.equals("All"))
            {
               for (ProcessDefinitionDTO procDef : processDTOList)
               {
                  if (allProcesses == null)
                  {
                     allProcesses = getProcessDefinitionService().getAllProcesses(false);
                  }
                  for (ProcessDefinitionDTO procDefinition : allProcesses)
                  {
                     if (procDefinition.id.equals(procDef.id))
                     {
                        activites.addAll(procDefinition.activities);
                        break;
                     }
                  }
               }
               break;
            }
            else
            {
               for (ProcessDefinitionDTO procDef : processDTOList)
               {
                  List<ActivityDTO> activities = procDef.activities;
                  for (ActivityDTO activityDTO : activities)
                  {
                     if (activityDTO.runtimeElementOid.equals(Long.parseLong(activityRuntimeElementOid)))
                     {
                        activites.add(activityDTO);
                        break;
                     }
                  }
               }
            }
         }
      }

      filterActivityList(processSearchCriteriaJson, activites);
      processDTOList.get(0).activities = activites;

      ProcessSearchCriteriaDTO processSearchCriteria = null;
      String processSearchCriteriaJsonStr = processSearchCriteriaJson.toString();
      if (StringUtils.isNotEmpty(processSearchCriteriaJsonStr))
      {
         try
         {
            processSearchCriteria = DTOBuilder.buildFromJSON(processSearchCriteriaJsonStr,
                  ProcessSearchCriteriaDTO.class);
         }
         catch (Exception e)
         {
            trace.error("Error in Deserializing filter JSON", e);
            throw e;
         }
      }
      processSearchCriteria.procSrchProcessSelected = processDTOList;

      processSearchCriteria.descriptors = formattedJsonObject;
      return processSearchCriteria;
   }

   /**
    * @param processSearchCriteriaJson
    * @param allProcesses
    */
   private void filterProcessDefinitionList(JsonObject processSearchCriteriaJson,
         List<ProcessDefinitionDTO> allProcesses)
   {
      String procSearchHierarchySelected = processSearchCriteriaJson.get("procSearchHierarchySelected").getAsString();
      if (procSearchHierarchySelected.equals(ProcessSearchParameterConstants.HIERARCHY_PROCESS)
            || procSearchHierarchySelected.equals(ProcessSearchParameterConstants.HIERARCHY_ROOT_PROCESS))
      {
         // ProcessDefinition list contains Case PD, remove the case PD
         filterCaseProcess(allProcesses);
      }
   }

   /**
    * @param allProcesses
    */
   private void filterCaseProcess(List<ProcessDefinitionDTO> allProcesses)
   {
      int casePresentPos = searchCaseProcess(allProcesses);
      if (casePresentPos > -1)
      {
         allProcesses.remove(casePresentPos);
      }
   }

   /**
    * @param allProcesses
    * @return
    */
   private int searchCaseProcess(List<ProcessDefinitionDTO> allProcesses)
   {
      int casePresentPos = -1;
      for (int i = 0; i < allProcesses.size(); i++)
      {
         String procId = getProcessId(allProcesses.get(i).id);
         if (procId.equals(PredefinedConstants.CASE_PROCESS_ID))
         {
            casePresentPos = i;
            return casePresentPos;
         }
      }
      return casePresentPos;
   }

   /**
    * @param id
    */
   private String getProcessId(String id)
   {
      String strippedId = id;
      if (id.indexOf("{") != -1)
      {
         int lastIndex = id.lastIndexOf("}");
         strippedId = id.substring(lastIndex + 1, id.length());
      }
      return strippedId;
   }

   /*
    * 
    */
   private void filterActivityList(JsonObject processSearchCriteriaJson, List<ActivityDTO> activities)
   {

      // For Interactive Activities
      if (!processSearchCriteriaJson.get("showInteractiveActivities").getAsBoolean())
      {
         Iterator<ActivityDTO> iterator = activities.iterator();
         while (iterator.hasNext())
         {
            ActivityDTO activityDTO = (ActivityDTO) iterator.next();
            if (activityDTO.interactive)
            {
               iterator.remove();
            }

         }
      }

      // For Non-Interactive Activities
      if (!processSearchCriteriaJson.get("showNonInteractiveActivities").getAsBoolean())
      {
         Iterator<ActivityDTO> iterator = activities.iterator();
         while (iterator.hasNext())
         {
            ActivityDTO activityDTO = (ActivityDTO) iterator.next();
            if (!activityDTO.interactive)
            {
               iterator.remove();
            }
         }
      }

      // For Auxiliary Activities
      if (!processSearchCriteriaJson.get("showAuxiliaryActivities").getAsBoolean())
      {
         Iterator<ActivityDTO> iterator = activities.iterator();
         while (iterator.hasNext())
         {
            ActivityDTO activityDTO = (ActivityDTO) iterator.next();
            if (activityDTO.auxillary)
            {
               iterator.remove();
            }

         }
      }
   }

   public ProcessActivityService getProcessActivityService()
   {
      return processActivityService;
   }

   public void setProcessActivityService(ProcessActivityService processActivityService)
   {
      this.processActivityService = processActivityService;
   }

   /**
    * @return
    */
   public ProcessDefinitionService getProcessDefinitionService()
   {
      return processDefinitionService;
   }

   /**
    * @param processDefinitionService
    */
   public void setProcessDefinitionService(ProcessDefinitionService processDefinitionService)
   {
      this.processDefinitionService = processDefinitionService;
   }

}