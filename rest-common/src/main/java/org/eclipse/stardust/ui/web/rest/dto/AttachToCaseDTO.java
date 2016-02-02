package org.eclipse.stardust.ui.web.rest.dto;

import java.util.List;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;
import org.eclipse.stardust.ui.web.rest.dto.core.DTOClass;

/**
 * 
 * @author Nikhil.Gahlot
 *
 */
@DTOClass
public class AttachToCaseDTO
{

   @DTOAttribute("sourceProcessOIDs")
   public List<Long> sourceProcessOIDs;

   @DTOAttribute("targetProcessOID")
   public String targetProcessOID;
}
