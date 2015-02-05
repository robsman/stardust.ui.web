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

import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.rest.service.ProcessDefinitionService;
import org.eclipse.stardust.ui.web.rest.service.dto.AbstractDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DescriptorColumnDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessDefinitionDTO;

/**
 * @author Anoop.Nair
 * @author Subodh.Godbole
 * @version $Revision: $
 */
@Component
@Path("/process-definitions")
public class ProcessDefinitionResource
{
	private static final Logger trace = LogManager
			.getLogger(ActivityInstanceResource.class);

	@Autowired
	private ProcessDefinitionService processDefinitionService;

	// private final JsonMarshaller jsonIo = new JsonMarshaller();

	/**
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("startable.json")
	public Response getStartableProcesses()
	{
		try
		{
			List<ProcessDefinitionDTO> startableProcesses = getProcessDefinitionService()
					.getStartableProcesses();

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
