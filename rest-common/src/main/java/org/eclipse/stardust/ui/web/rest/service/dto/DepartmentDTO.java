package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class DepartmentDTO extends AbstractDTO
{
   public String departmentId;
   public String name;
   public String description;
   public Long parentDepartmentOID;
   public Long departmentOID;   
   public String orgId;
   public boolean modifyMode;  
}
