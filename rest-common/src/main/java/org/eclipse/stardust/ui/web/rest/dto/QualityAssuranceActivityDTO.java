package org.eclipse.stardust.ui.web.rest.dto;

import org.eclipse.stardust.ui.web.rest.dto.core.DTOAttribute;

public class QualityAssuranceActivityDTO extends AbstractDTO
{
   
   @DTOAttribute("qualifiedId")
   public String activityQualifiedId;
   
   public String processQualifiedId;
   
   public String modelName;
  
   public String processName;
   
   public String activityName;
  
   public String defaultPerformer;
   
   public String performerType;
   
   public String qaPercentage;
   
   public boolean oldModel;
   
}
