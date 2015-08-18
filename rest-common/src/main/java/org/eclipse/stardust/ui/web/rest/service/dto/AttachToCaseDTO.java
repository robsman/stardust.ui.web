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
public class AttachToCaseDTO
{

   @DTOAttribute("sourceProcessOIDs")
   public List<Long> sourceProcessOIDs;

   @DTOAttribute("targetProcessOID")
   public String targetProcessOID;
}
