package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.util.List;

public class ValidationProblemsException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   private List<ValidationProblem> validationProblems;

   public ValidationProblemsException(List<ValidationProblem> validationProblems)
   {
      this.validationProblems = validationProblems;

   }

   @Override
   public String getMessage()
   {
      StringBuffer msg = new StringBuffer();
      msg.append("A validation exception occurred.").append("\n");
      if(validationProblems != null)
      {
         msg.append("The reported problems were: \n");
         for(ValidationProblem validationProblem: validationProblems)
         {
            msg.append(validationProblem.getMessage()).append("\n");
         }
      }

      return msg.toString();
   }
}
