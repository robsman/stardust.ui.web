package org.eclipse.stardust.ui.web.rest.service.dto;


public class InfoDTO extends AbstractDTO{
	public static enum MessageType {
		INFO, WARNING, ERROR
	}

	private MessageType messageType;
	private String details;
	
	public InfoDTO(MessageType messageType, String details) {
		super();
		this.messageType = messageType;
		this.details = details;
	}
}
