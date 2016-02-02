package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

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
