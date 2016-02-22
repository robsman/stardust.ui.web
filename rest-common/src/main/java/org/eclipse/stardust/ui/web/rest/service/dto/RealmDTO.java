package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class RealmDTO extends AbstractDTO
{
   @DTOAttribute("id")
   public String id;
   
   @DTOAttribute("name")
   public String name;
   
   @DTOAttribute("description")
   public String description;

}
