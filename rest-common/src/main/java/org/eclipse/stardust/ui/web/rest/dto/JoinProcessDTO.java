package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;

public class JoinProcessDTO extends AbstractDTO {

	@DTOAttribute("sourceProcessOID")
	public String sourceProcessOID;
	
	@DTOAttribute("linkComment")
	public String linkComment;
	
	@DTOAttribute("targetProcessOID")
	public String targetProcessOID;
}
