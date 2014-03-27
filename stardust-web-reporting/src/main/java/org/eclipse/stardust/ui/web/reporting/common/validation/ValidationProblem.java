package org.eclipse.stardust.ui.web.reporting.common.validation;


public class ValidationProblem
{
   private String message;

   public ValidationProblem(String message)
   {
      this.message = message;
   }

   public String getMessage()
   {
      return message;
   }
}
