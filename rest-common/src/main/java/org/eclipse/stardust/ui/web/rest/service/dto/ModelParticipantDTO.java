package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.engine.api.runtime.DepartmentInfo;
import org.eclipse.stardust.ui.web.rest.service.dto.ParticipantNodeDetailsDTO.NODE_TYPE;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

public class ModelParticipantDTO extends AbstractDTO
{

   @DTOAttribute("qualifiedId")
   public String qualifiedId;

   @DTOAttribute("id")
   public String id;

   @DTOAttribute("name")
   public String name;

   @DTOAttribute("runtimeElementOID")
   public long runtimeElementOID;

   @DTOAttribute("departmentScoped")
   public Boolean departmentScoped;

   @DTOAttribute("department")
   public DepartmentInfo department;
   
   @DTOAttribute("definesDepartmentScope")
   public Boolean definesDepartmentScope;
   
  public String participantType;
   
   public NODE_TYPE nodeType;
   
   public List<UserDTO> users;
   
   public List<ModelParticipantDTO> subOrganizations;
   
   public List<ModelParticipantDTO> subRoles;
   
   public List<ModelParticipantDTO> subParticipants;
   
   public List<DepartmentDTO> scopedDepartments;
   
   public ModelParticipantDTO defaultDepartment;
   

}
