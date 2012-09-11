package org.eclipse.stardust.ui.web.modeler.model.di;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;

public class EventSymbolJto extends NodeSymbolJto<EventJto>
{
   public EventSymbolJto()
   {
      this.type = ModelerConstants.EVENT_SYMBOL;
   }
}