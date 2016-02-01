package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class WorkflowOverviewCountsDTO
{
   public String directUserWorkCount;
   public String assignedActivitiesCount;
   public String criticalActivitiesCount;
}
