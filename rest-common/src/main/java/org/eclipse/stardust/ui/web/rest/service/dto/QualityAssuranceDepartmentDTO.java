package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

public class QualityAssuranceDepartmentDTO extends AbstractDTO
{
   @DTOAttribute("name")
   public String name;
  
   public String qaPercentage;
  
   @DTOAttribute("id")
   public String id;
  
   @DTOAttribute("OID")
   public long oid;
   
   @DTOAttribute("organization.runtimeElementOID")
   public long orgRuntimeId;
   
   public String activityQualifiedId;
  
   public String processQualifiedId;
}
