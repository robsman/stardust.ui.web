package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;
import java.util.Map;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class RoleAssignmentDataDTO extends AbstractDTO
{
  public String teamMember;
  public List<DescriptorsDTO> descriptors;
  public Map<String,Boolean> descriptorsValues;
}
