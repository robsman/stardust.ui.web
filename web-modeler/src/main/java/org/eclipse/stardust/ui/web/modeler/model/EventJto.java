package org.eclipse.stardust.ui.web.modeler.model;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class EventJto extends ModelElementJto
{
   public EventJto()
   {
      this.type = ModelerConstants.EVENT_KEY;
   }

   public String eventType;

   // TODO more details
}