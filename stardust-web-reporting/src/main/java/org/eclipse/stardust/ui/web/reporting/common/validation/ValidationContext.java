package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationContext
{
   private List<ValidationProblem> validationProblems = new ArrayList<ValidationProblem>();

   public void addValidationProblem(ValidationProblem validationProblem)
   {
      if(validationProblem != null)
      {
         validationProblems.add(validationProblem);
      }
   }

   public List<ValidationProblem> getValidationProblems()
   {
      return validationProblems;
   }
}
