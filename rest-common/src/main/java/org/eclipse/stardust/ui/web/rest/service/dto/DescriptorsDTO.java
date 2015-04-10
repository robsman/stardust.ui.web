package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class DescriptorsDTO extends AbstractDTO
{
  public String title;

public DescriptorsDTO(String title)
{
   super();
   this.title = title;
}
}
