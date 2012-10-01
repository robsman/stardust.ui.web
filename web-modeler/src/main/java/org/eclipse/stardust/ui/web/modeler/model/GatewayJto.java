package org.eclipse.stardust.ui.web.modeler.model;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class GatewayJto extends ModelElementJto
{
   public GatewayJto()
   {
      this.type = ModelerConstants.ACTIVITY_KEY;
   }

   public String gatewayType;
}