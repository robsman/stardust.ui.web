package org.eclipse.stardust.ui.web.modeler.model;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class DataJto extends ModelElementJto
{
   public DataJto()
   {
      this.type = ModelerConstants.DATA;
   }

   public String dataType;

   public String structuredDataTypeFullId;

   // TODO more details
}
