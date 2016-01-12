package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@DTOClass
public class NotificationMessageDTO extends AbstractDTO
{
   @DTOAttribute("success")
   public Boolean success;
   
   @DTOAttribute("message")
   public String message;
   
}
