package org.eclipse.stardust.ui.web.modeler.model.di;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;

public class GatewaySymbolJto extends NodeSymbolJto<ActivityJto>
{
   public GatewaySymbolJto()
   {
      this.type = ModelerConstants.GATEWAY_SYMBOL;
   }
}