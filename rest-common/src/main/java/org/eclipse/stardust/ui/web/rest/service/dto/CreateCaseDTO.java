package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */

@DTOClass
public class CreateCaseDTO
{

   @DTOAttribute("sourceProcessOIDs")
   public List<Long> sourceProcessOIDs;

   @DTOAttribute("caseName")
   public String caseName;

   @DTOAttribute("description")
   public String description;

   @DTOAttribute("note")
   public String note;

}
