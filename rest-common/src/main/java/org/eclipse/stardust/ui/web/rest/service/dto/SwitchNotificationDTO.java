package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@DTOClass
public class SwitchNotificationDTO {
	
	@DTOAttribute("switched")
	public boolean switched;
	
	@DTOAttribute("messageKey")
	public String statusMessage;
	
	@DTOAttribute("abortedProcess")
	public ProcessInstanceDTO abortedProcess;
	
	@DTOAttribute("startedProcess")
	public ProcessInstanceDTO startedProcess;
	
}
