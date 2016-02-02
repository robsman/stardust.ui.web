package org.eclipse.stardust.ui.web.rest.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;
import org.eclipse.stardust.ui.web.rest.dto.response.ParticipantDTO;

@DTOClass
public class StartableProcessDTO
{
   public ProcessDefinitionDTO processDefinition;

   public List<ParticipantDTO> participantNodes;

   public String name;
}
