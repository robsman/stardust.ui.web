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
public class CriticalityConfigDTO extends AbstractDTO
{

   @DTOAttribute("criticalities")
   public List<CriticalityDTO> criticalities;

   @DTOAttribute("defaultCriticalityFormula")
   public String defaultCriticalityFormula;

   @DTOAttribute("activityCreation")
   public boolean activityCreation;

   @DTOAttribute("activitySuspendAndSave")
   public boolean activitySuspendAndSave;

   @DTOAttribute("processPriorityChange")
   public boolean processPriorityChange;
}
