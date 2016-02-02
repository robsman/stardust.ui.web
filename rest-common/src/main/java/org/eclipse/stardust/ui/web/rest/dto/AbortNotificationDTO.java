package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

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
