package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@DTOClass
public class AbortNotificationDTO {
	
	@DTOAttribute("messageKey")
	public String statusMessage;
	
	@DTOAttribute("abortedProcess")
	public ProcessInstanceDTO abortedProcess;
	
	@DTOAttribute("targetProcess")
	public ProcessInstanceDTO targetProcess;
	
}
