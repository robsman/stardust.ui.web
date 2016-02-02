package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

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
