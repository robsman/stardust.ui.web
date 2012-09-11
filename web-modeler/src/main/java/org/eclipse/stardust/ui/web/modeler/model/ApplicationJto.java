package org.eclipse.stardust.ui.web.modeler.model;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class ApplicationJto extends ModelElementJto
{
   public ApplicationJto()
   {
      this.type = ModelerConstants.APPLICATION_KEY;
   }

   public String applicationType;

   // TODO more details
}
