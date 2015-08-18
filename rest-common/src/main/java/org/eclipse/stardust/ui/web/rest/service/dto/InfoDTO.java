package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class InfoDTO extends AbstractDTO
{
   public static enum MessageType {
      INFO, WARNING, ERROR
   }

   public MessageType messageType;

   public String details;

   public InfoDTO(MessageType messageType, String details)
   {
      super();
      this.messageType = messageType;
      this.details = details;
   }
}
