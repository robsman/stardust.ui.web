package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.bcc.jsf.UserItem;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class RoleManagerDetailsDTO extends AbstractDTO
{
   public String roleName;

   public String roleId;

   public String items;

   public String account;

   public String itemsPerUser;

   public boolean roleModifiable;

   public List<RoleManagerDetailUserDTO> assignedUserList;

   public List<RoleManagerDetailUserDTO> assignableUserList;
}
