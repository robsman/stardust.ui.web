package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class ParticipantNodeDetailsDTO extends AbstractDTO
{
   public static enum NODE_TYPE {
      ROOT, MODEL, ORGANIZATON_SCOPED_EXPLICIT, ORGANIZATON_SCOPED_IMPLICIT, ROLE_SCOPED, ORGANIZATION_UNSCOPED, ROLE_UNSCOPED, USERGROUP, USER, DEPARTMENT, DEPARTMENT_DEFAULT;
   }

   public NODE_TYPE nodeType;

   public String id; // can be orgId, roleId, userGroupId

   public Long departmentOid;
}
