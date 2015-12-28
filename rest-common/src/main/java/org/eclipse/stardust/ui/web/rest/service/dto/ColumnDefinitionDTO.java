package org.eclipse.stardust.ui.web.rest.service.dto;

import org.eclipse.stardust.ui.web.rest.service.dto.common.DTOClass;

@DTOClass
public class ColumnDefinitionDTO extends AbstractDTO
{
   public String columnTitle;
   public String prefScope;
   
   public ColumnDefinitionDTO(String columnTitle, String prefScope)
   {
      super();
      this.columnTitle = columnTitle;
      this.prefScope = prefScope;
   }

   public ColumnDefinitionDTO()
   {
      // TODO Auto-generated constructor stub
   }
}
