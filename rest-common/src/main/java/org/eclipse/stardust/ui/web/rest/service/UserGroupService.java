/**
 * 
 */
package org.eclipse.stardust.ui.web.rest.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.runtime.UserGroup;
import org.eclipse.stardust.ui.web.rest.service.dto.UserGroupDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.builder.DTOBuilder;
import org.eclipse.stardust.ui.web.rest.service.utils.UserGroupUtils;
import org.springframework.stereotype.Component;

/**
 * @author Aditya.Gaikwad
 *
 */
@Component
public class UserGroupService {

	public static final Logger trace = LogManager
			.getLogger(UserGroupService.class);

	@Resource
	private UserGroupUtils userGroupUtils;

	public List<UserGroupDTO> getAllUserGroups() {
		List<UserGroup> allUserGroups = userGroupUtils.getAllUserGroups();
		return DTOBuilder.buildList(allUserGroups, UserGroupDTO.class);
	}

	public UserGroupDTO getUserGroup(String id) {
		UserGroup userGroup = userGroupUtils.getUserGroup(id);
		return DTOBuilder.build(userGroup, UserGroupDTO.class);
	}

	public UserGroupDTO modifyUserGroup(UserGroupDTO userGroupDTO) {
		UserGroup userGroup = userGroupUtils.getUserGroup(userGroupDTO.getId());
		userGroup.setDescription(userGroupDTO.getDescription());
		userGroup.setName(userGroupDTO.getName());
		userGroup.setValidFrom(userGroupDTO.getValidFrom());
		userGroup.setValidTo(userGroupDTO.getValidTo());

		// UserGroup userGroup = DTOBuilder.unbuild(userGroupDTO,
		// UserGroupDTO.class);

		userGroupUtils.modifyUserGroup(userGroup);
		return null;
	}

	/**
	 * @param id
	 */
	public UserGroupDTO deleteUserGroup(String id) {
		UserGroup deleteUserGroup = userGroupUtils.deleteUserGroup(id);
		return DTOBuilder.build(deleteUserGroup, UserGroupDTO.class);
	}

	public UserGroupDTO createUserGroup(String id, String name,
			String description, Date validFrom, Date validTo) {
		UserGroup createdUserGroup = userGroupUtils.createUserGroup(id, name,
				description, validFrom, validTo);
		return DTOBuilder.build(createdUserGroup, UserGroupDTO.class);
	}

}
