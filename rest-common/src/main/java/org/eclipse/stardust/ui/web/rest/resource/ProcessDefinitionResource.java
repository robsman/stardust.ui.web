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

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.JsonMarshaller;
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentDataDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Path("/process-definitions")
public class ProcessDefinitionResource
{
   private static final Logger trace = LogManager.getLogger(ProcessDefinitionResource.class);

	@Autowired
	private ProcessDefinitionService processDefinitionService;

	// private final JsonMarshaller jsonIo = new JsonMarshaller();

	/**
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("startable.json")
	public Response getStartableProcesses(@QueryParam("triggerType") String triggerType)
	{
		try
		{
			List<ProcessDefinitionDTO> startableProcesses = getProcessDefinitionService()
					.getStartableProcesses(triggerType);

			return Response.ok(AbstractDTO.toJson(startableProcesses), MediaType.APPLICATION_JSON).build();
		}
		catch (Exception e)
		{
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("descriptor-columns")
	public Response getDescriptors(@QueryParam("onlyFilterable") @DefaultValue("false") Boolean onlyFilterable)
	{
		try
		{
			List<DescriptorColumnDTO> descriptors = getProcessDefinitionService().getDescriptorColumns(onlyFilterable);

			return Response.ok(AbstractDTO.toJson(descriptors), MediaType.APPLICATION_JSON).build();
		}
		catch (Exception e)
		{
			trace.error(e, e);

			return Response.serverError().build();
		}
	}

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("{processDefinitionId}/scan-trigger-documents")
   public Response getScanTriggerDocuments(@PathParam("processDefinitionId") String processDefinitionId)
   {
      try
      {
         List<DocumentDataDTO> documents = getProcessDefinitionService().getAllDocumentData(processDefinitionId);

         return Response.ok(AbstractDTO.toJson(documents), MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);

         return Response.serverError().build();
      }
   }

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("all-processes")
	public Response getAllProcesses(@QueryParam("excludeActivities") @DefaultValue("false") Boolean excludeActivities)
	{
		try
		{
			return Response.ok(AbstractDTO.toJson(getProcessDefinitionService().getAllProcesses(excludeActivities)), MediaType.APPLICATION_JSON)
					.build();
		}
		catch (Exception e)
		{
			trace.error(e, e);
			return Response.serverError().build();
		}

	}
	
   @PUT
   @Produces(MediaType.APPLICATION_JSON)
   @Path("common-descriptors")
   public Response getCommonDescriptors(@QueryParam("onlyFilterable") @DefaultValue("false") Boolean onlyFilterable,
         String postData)
   {
      JsonMarshaller jsonIo = new JsonMarshaller();
      JsonObject postJSON = jsonIo.readJsonObject(postData);

      String procDefIDsStr = postJSON.get("procDefIDs").getAsString();

      String[] procDefIDsArr = procDefIDsStr.split(",");
      List<String> procDefIDs = Arrays.asList(procDefIDsArr);
      List<DescriptorColumnDTO> commonDescriptors = getProcessDefinitionService().getCommonDescriptors(procDefIDs,
            onlyFilterable);
      return Response.ok(AbstractDTO.toJson(commonDescriptors), MediaType.APPLICATION_JSON).build();
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   @Path("all-unique-processes")
   public Response getAllUniqueProcess(@QueryParam("excludeActivities") @DefaultValue("false") Boolean excludeActivities)
   {
      try
      {
         return Response.ok(AbstractDTO.toJson(getProcessDefinitionService().getAllUniqueProcess(excludeActivities)),
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
   @Path("all-business-processes")
   public Response getAllBusinessRelevantProcesses(
         @QueryParam("excludeActivities") @DefaultValue("false") Boolean excludeActivities)
   {
      try
      {
         return Response.ok(
               AbstractDTO.toJson(getProcessDefinitionService().getAllBusinessRelevantProcesses(excludeActivities)),
               MediaType.APPLICATION_JSON).build();
      }
      catch (Exception e)
      {
         trace.error(e, e);
         return Response.serverError().build();
      }
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
   public void setProcessDefinitionService(
           ProcessDefinitionService processDefinitionService)
   {
       this.processDefinitionService = processDefinitionService;
   }
}
