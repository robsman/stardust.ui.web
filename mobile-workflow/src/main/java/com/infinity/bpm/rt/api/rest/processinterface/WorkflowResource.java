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

package com.infinity.bpm.rt.api.rest.processinterface;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.stardust.mobile.MobileWorkflowService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.gson.JsonObject;

/**
 * 
 * @author Ellie.Sepehri
 *
 */
@Path("/mobile-workflow/{randomPostFix}")
public class WorkflowResource {
	private final JsonMarshaller jsonIo = new JsonMarshaller();

	@Context
	private ServletContext servletContext;

	public MobileWorkflowService getMobileWorkflowService() {
		ApplicationContext context = WebApplicationContextUtils
				.getWebApplicationContext(servletContext);
		return (MobileWorkflowService) context.getBean("mobileWorkflowService");
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/login")
	public Response login(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(getMobileWorkflowService().login(json).toString()).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflow/getStartableProcesses")
	public Response getStartableProcesses() {
		try {
			return Response.ok(getMobileWorkflowService().getStartableProcesses().toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (RuntimeException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflow/getWorklist")
	public Response getWorklist() {
		try {
			return Response.ok(getMobileWorkflowService().getWorklist().toString(),
					MediaType.APPLICATION_JSON_TYPE).build();
		} catch (RuntimeException e) {
			e.printStackTrace();
			return Response.serverError().build();
		}
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/workflow/activateActivity")
	public Response activateActivity(String postedData) {
		try {
			JsonObject json = jsonIo.readJsonObject(postedData);

			return Response.ok(getMobileWorkflowService().activateActivity(json).toString()).build();
		} catch (Exception e) {
			e.printStackTrace();

			throw new RuntimeException(e);
		}
	}
}
