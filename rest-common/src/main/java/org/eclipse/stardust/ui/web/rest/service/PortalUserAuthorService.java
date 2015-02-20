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
import java.util.List;

import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.ui.web.rest.service.dto.QueryResultDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.UserDTO;
import org.eclipse.stardust.ui.web.viewscommon.utils.UserUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

/**
 * @author Yogesh.Manware
 * 
 */
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.INTERFACES)
public class PortalUserAuthorService implements IUserService, ISearchHandler {
	public static final String SERVICE_NAME = "userService";

	@Override
	public String handle(String serviceName, String searchVal) {
		if (!SERVICE_NAME.equals(serviceName)) {
			return null;
		}
		return buildResult(searchVal);
	}

	/**
	 * @param searchVal
	 * @return
	 */
	private String buildResult(String searchVal) {
		// build result - parse search value if required
		List<User> users = searchUsers(searchVal, true, 20);
		List<UserDTO> userWrappers = new ArrayList<UserDTO>();
		for (User user : users) {
			UserDTO dto = new UserDTO();
			dto.setId(user.getId());
			dto.setName(UserUtils.getUserDisplayLabel(user));
			userWrappers.add(dto);
		}

		QueryResultDTO resultDTO = new QueryResultDTO();
		resultDTO.list = userWrappers;
		resultDTO.totalCount = userWrappers.size();

		Gson gson = new Gson();
		return gson.toJson(resultDTO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.stardust.ui.web.reporting.beans.spring.portal.IUserService
	 * #searchUsers (java.lang.String, boolean, int)
	 */
	public List<User> searchUsers(String searchValue, boolean onlyActive,
			int maxMatches) {
		return UserUtils.searchUsers(searchValue, true, 20);
	}

}
