package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOAttribute;

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
