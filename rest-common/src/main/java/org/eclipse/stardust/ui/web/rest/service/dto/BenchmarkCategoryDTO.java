package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class BenchmarkCategoryDTO extends AbstractDTO
{
   public String color; 
   public int index;
   public String name;
   public Long count;
}
