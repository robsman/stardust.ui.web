package org.eclipse.stardust.ui.web.modeler.model;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class PrimitiveDataJto extends DataJto
{
   public PrimitiveDataJto()
   {
      this.dataType = ModelerConstants.PRIMITIVE_DATA_TYPE_KEY;
   }

   public String primitiveDataType;
}
