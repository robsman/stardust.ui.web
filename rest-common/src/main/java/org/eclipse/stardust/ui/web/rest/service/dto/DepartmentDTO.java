package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class DepartmentDTO extends AbstractDTO
{
   @DTOAttribute("id")
   public String departmentId;
   @DTOAttribute("name")
   public String name;
   @DTOAttribute("description")
   public String description;
   @DTOAttribute("parentDepartment.oid")
   public Long parentDepartmentOID;
   @DTOAttribute("oid")
   public Long departmentOID;
   @DTOAttribute("runtimeOrganizationOID")
   public String orgId;

   public boolean modifyMode;

   public ModelParticipantDTO scopedParticipant;

}
