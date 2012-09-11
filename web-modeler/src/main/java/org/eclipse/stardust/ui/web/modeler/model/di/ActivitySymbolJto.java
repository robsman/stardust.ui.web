package org.eclipse.stardust.ui.web.modeler.model.di;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;

public class ActivitySymbolJto extends NodeSymbolJto<ActivityJto>
{
   public ActivitySymbolJto()
   {
      this.type = ModelerConstants.ACTIVITY_SYMBOL;
   }
}