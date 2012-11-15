package org.eclipse.stardust.ui.web.modeler.model.di;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;

public class ConnectionSymbolJto
{
   public Long oid;

   public String type;
   public ModelElementJto modelElement;

   public int fromAnchorPointOrientation = ModelerConstants.UNDEFINED_ORIENTATION_KEY;
   public Long fromModelElementOid;
   public String fromModelElementType;

   public int toAnchorPointOrientation = ModelerConstants.UNDEFINED_ORIENTATION_KEY;
   public Long toModelElementOid;
   public String toModelElementType;
}
