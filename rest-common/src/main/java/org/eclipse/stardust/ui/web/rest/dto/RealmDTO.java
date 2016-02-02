package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

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
