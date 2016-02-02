package org.eclipse.stardust.ui.web.rest.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;

public class SwitchProcessDTO extends AbstractDTO {

	@DTOAttribute("processId")
	public String processId;
	
	@DTOAttribute("linkComment")
	public String linkComment;
	
	@DTOAttribute("processInstaceOIDs")
	public List<Long> processInstaceOIDs;
}
