package org.eclipse.stardust.ui.web.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.DocumentFilter;
import org.eclipse.stardust.engine.api.query.ProcessInstanceQuery;
import org.eclipse.stardust.engine.api.query.ProcessInstances;
import org.eclipse.stardust.engine.api.runtime.ProcessInstance;
import org.eclipse.stardust.ui.web.rest.service.DocumentSearchServiceBean;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchFilterDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessWrapperDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.ServiceFactoryUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 *
 * @author Abhay.Thappan
 * 
 *         Copy paste from web-reporting
 *
 */
@Component
@Path("/documentSearch")
public class DocumentSearchResource {
	private static final Logger trace = LogManager
			.getLogger(DocumentSearchResource.class);
	private final JsonMarshaller jsonIo = new JsonMarshaller();
	private final Gson prettyPrinter = new GsonBuilder().setPrettyPrinting()
			.create();

	@Resource
	private DocumentSearchServiceBean documentSearchService;

	@Resource
	private ServiceFactoryUtils serviceFactoryUtils;

	@Context
	private HttpServletRequest httpRequest;

	@GET
	/* @Produces(MediaType.APPLICATION_JSON) */
	@Path("/search/{serviceName}/{searchValue}")
	public Response search(@PathParam("serviceName") String serviceName,
			@PathParam("searchValue") String searchValue) {
		if (StringUtils.isNotEmpty(serviceName)
				&& StringUtils.isNotEmpty(searchValue)) {
			try {
				String result = documentSearchService.searchData(serviceName,
						searchValue);
				return Response.ok(result, MediaType.TEXT_PLAIN_TYPE).build();
			} catch (MissingResourceException mre) {
				return Response.status(Status.NOT_FOUND).build();
			} catch (Exception e) {
				return Response.status(Status.BAD_REQUEST).build();
			}
		} else {
			return Response.status(Status.FORBIDDEN).build();
		}
	}

	@GET
	/* @Produces(MediaType.APPLICATION_JSON) */
	@Path("/searchAttributes")
	public Response searchAttributes() {

		try {
			String result = documentSearchService
					.createDocumentSearchFilterAttributes();
			return Response.ok(result, MediaType.TEXT_PLAIN_TYPE).build();
		} catch (MissingResourceException mre) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@POST
	/* @Produces(MediaType.APPLICATION_JSON) */
	@Path("/searchByCriteria")
	public Response searchByCritera(
			@QueryParam("skip") @DefaultValue("0") Integer skip,
			@QueryParam("pageSize") @DefaultValue("8") Integer pageSize,
			@QueryParam("orderBy") @DefaultValue("documentName") String orderBy,
			@QueryParam("orderByDir") @DefaultValue("desc") String orderByDir,
			String postData) {

		try {
			Options options = new Options(pageSize, skip, orderBy,
					"desc".equalsIgnoreCase(orderByDir));
			populateFilters(options, postData);

			DocumentSearchCriteriaDTO documentSearchAttributes = getDocumentSearchCriteria(postData);

			QueryResultDTO result = documentSearchService.performSearch(
					options, documentSearchAttributes);
			Gson gson = new Gson();
			return Response.ok(gson.toJson(result), MediaType.TEXT_PLAIN_TYPE)
					.build();
		} catch (MissingResourceException mre) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	@GET
	// @Produces(MediaType.APPLICATION_JSON)
	@Path("/loadProcessByDocument/{documentId}")
	public Response loadProcessByDocument(
			@PathParam("documentId") String documentId) {
		try {

			QueryResultDTO resultDTO = documentSearchService.getProcessInstancesFromDocument(documentId);

			Gson gson = new Gson();
			return Response.ok(gson.toJson(resultDTO),
					MediaType.TEXT_PLAIN_TYPE).build();
		} catch (MissingResourceException mre) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
	
	@GET
	// @Produces(MediaType.APPLICATION_JSON)
	@Path("/loadUserDetails/{documentOwner}")
	public Response getUserDetails(
			@PathParam("documentOwner") String documentOwner) {
		try {

			UserDTO user = documentSearchService.getUserDetails(documentOwner);

			Gson gson = new Gson();
			return Response.ok(gson.toJson(user),
					MediaType.TEXT_PLAIN_TYPE).build();
		} catch (MissingResourceException mre) {
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}
	


	/**
	 * Populate the options with the post data.
	 * 
	 * @param options
	 * @param postData
	 * @return
	 */
	private Options populateFilters(Options options, String postData) {
		JsonMarshaller jsonIo = new JsonMarshaller();
		JsonObject postJSON = jsonIo.readJsonObject(postData);

		// For filter
		JsonObject filters = postJSON.getAsJsonObject("filters");
		if (null != filters) {
			DocumentSearchFilterDTO docSearchFilterDTO = new Gson().fromJson(
					postJSON.get("filters"), DocumentSearchFilterDTO.class);

			options.filter = docSearchFilterDTO;
		}
		return options;
	}

	private DocumentSearchCriteriaDTO getDocumentSearchCriteria(String postData) {
		DocumentSearchCriteriaDTO documentSearchCriteria = null;

		JsonMarshaller jsonIo = new JsonMarshaller();
		JsonObject postJSON = jsonIo.readJsonObject(postData);

		JsonObject documentSearchCriteriaJson = postJSON
				.getAsJsonObject("documentSearchCriteria");

		String documentSearchCriteriaJsonStr = documentSearchCriteriaJson
				.toString();
		if (StringUtils.isNotEmpty(documentSearchCriteriaJsonStr)) {
			try {
				documentSearchCriteria = DTOBuilder
						.buildFromJSONDocumentCriteria(
								documentSearchCriteriaJsonStr,
								DocumentSearchCriteriaDTO.class);
			} catch (Exception e) {
				trace.error("Error in Deserializing filter JSON", e);
			}
		}

		return documentSearchCriteria;
	}

}
