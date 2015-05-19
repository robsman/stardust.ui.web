package org.eclipse.stardust.ui.web.modeler.edit.batch;

public class VarSpecJto
{
   public static VarSpecJto namedExpression(String name, String expression)
   {
      VarSpecJto varSpecJto = new VarSpecJto();
      varSpecJto.name = name;
      varSpecJto.expression = expression;
      return varSpecJto;
   }

   public String name;

   public String expression;
}
