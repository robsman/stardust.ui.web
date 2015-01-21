package org.eclipse.stardust.ui.web.rest.service.utils;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.engine.api.query.UserGroupQuery;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.springframework.stereotype.Component;

/**
 * @author Aditya.Gaikwad
 * @version $Revision: $
 */
@Component
public class UserGroupUtils {

	@Resource
	private ServiceFactoryUtils serviceFactoryUtils;

	public List<UserGroup> getAllUserGroups() {
		UserGroupQuery userGroupQuery = UserGroupQuery.findAll();
		return serviceFactoryUtils.getQueryService().getAllUserGroups(
				userGroupQuery);
	}

	public UserGroup getUserGroup(String id) {
		return serviceFactoryUtils.getUserService().getUserGroup(id);
	}

	public UserGroup modifyUserGroup(UserGroup userGroup) {
		serviceFactoryUtils.getUserService().modifyUserGroup(userGroup);
		return null;
	}

	/**
	 * @param id
	 */
	public UserGroup deleteUserGroup(String id) {
		return serviceFactoryUtils.getUserService().invalidateUserGroup(id);
	}

	public UserGroup createUserGroup(String id, String name,
			String description, Date validFrom, Date validTo) {
		return serviceFactoryUtils.getUserService().createUserGroup(id, name,
				description, validFrom, validTo);
	}

}
