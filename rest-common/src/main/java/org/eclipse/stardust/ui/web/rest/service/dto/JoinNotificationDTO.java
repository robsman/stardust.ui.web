package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

public class JoinNotificationDTO {

	@DTOAttribute("messageKey")
	public String statusMessage;
	
	@DTOAttribute("abortedProcess")
	public ProcessInstanceDTO abortedProcess;
	
	@DTOAttribute("joinedProcess")
	public ProcessInstanceDTO joinedProcess;
}
