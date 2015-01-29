package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

public class SwitchProcessDTO extends AbstractDTO {

	@DTOAttribute("processId")
	public String processId;
	
	@DTOAttribute("linkComment")
	public String linkComment;
}
