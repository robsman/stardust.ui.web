package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;
import java.util.Set;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;
import org.eclipse.stardust.ui.web.rest.service.dto.request.DepartmentDTO;
import org.eclipse.stardust.ui.web.rest.service.dto.response.ParticipantDTO;

@DTOClass
public class StartableProcessDTO
{
   public ProcessDefinitionDTO processDefinition;

   public List<ParticipantDTO> participantNodes;

   public Set<DepartmentDTO> deptList;

   public String name;
}
