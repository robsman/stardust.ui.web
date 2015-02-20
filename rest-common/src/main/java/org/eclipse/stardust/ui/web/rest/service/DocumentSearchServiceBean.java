/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.query.QueryResult;
import org.eclipse.stardust.engine.api.runtime.Document;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.Options;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchCriteriaDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.DocumentSearchResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.ProcessInstanceDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.DocumentSearchUtils;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 *
 * @author Abhay.Thappan This is copy paste from web-reporting.
 */

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class DocumentSearchServiceBean {

	private static final Logger trace = LogManager
			.getLogger(DocumentSearchServiceBean.class);

	@Resource
	private DocumentSearchHandlerChain documentSearchHandlerChain;

	@Resource
	private DocumentSearchUtils documentSearchUtils;

	/**
	 * @param serviceName
	 * @param searchValue
	 * @return
	 */
	public String searchData(String serviceName, String searchValue) {
		return documentSearchHandlerChain.handleRequest(serviceName,
				searchValue);
	}

	public String createDocumentSearchFilterAttributes() {
		return documentSearchUtils.getFilterAttributes();
	}

	public QueryResultDTO performSearch(Options options,
			DocumentSearchCriteriaDTO documentSearchAttributes) {
		QueryResult<Document> docs = documentSearchUtils.performSearch(options,
				documentSearchAttributes);
		return buildDocumentSearchResult(docs);

	}

	public QueryResultDTO getProcessInstancesFromDocument(String documentId) {
		List<ProcessInstanceDTO> processList = documentSearchUtils
				.getProcessInstancesFromDocument(documentId);
		QueryResultDTO resultDTO = new QueryResultDTO();
		resultDTO.list = processList;
		resultDTO.totalCount = processList.size();
		return resultDTO;

	}

	private QueryResultDTO buildDocumentSearchResult(QueryResult<Document> docs) {
		List<DocumentSearchResultDTO> list = new ArrayList<DocumentSearchResultDTO>();

		for (Document doc : docs) {
			DocumentSearchResultDTO docSearchResultDTO = new DocumentSearchResultDTO(
					doc);
			list.add(docSearchResultDTO);
		}

		QueryResultDTO resultDTO = new QueryResultDTO();
		resultDTO.list = list;
		resultDTO.totalCount = list.size();

		return resultDTO;

	}

	public UserDTO getUserDetails(String documentOwner) {
		User user = UserUtils.getUser(documentOwner);
		UserDTO userDTO = DTOBuilder.build(user, UserDTO.class);
		userDTO.setName(UserUtils.getUserDisplayLabel(user));
		userDTO.setValidFrom(new Date());
		userDTO.setValidTo(new Date());
		userDTO.setEMail("abhay.thappan@sungard.com");
		userDTO.setDescription("Master user");
		return userDTO;
	}
}
