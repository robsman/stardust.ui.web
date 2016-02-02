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
