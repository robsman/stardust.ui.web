package org.eclipse.stardust.ui.web.modeler.model;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class TransitionJto extends ModelElementJto
{
   public TransitionJto()
   {
      this.type = ModelerConstants.CONTROL_FLOW_LITERAL;
   }

   public boolean forkOnTraversal;

   public String conditionExpression;
   public boolean otherwise;

   // TODO more details
}
