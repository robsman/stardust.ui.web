package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.util.List;

import org.eclipse.stardust.common.config.Parameters;

public class ValidationHelper
{
   public static final String VALIDATION_ENABLED_KEY = "web.reporting.json.validation.enabled";

   public static void validate(IValidateAble validateAble)
   {
      if(Parameters.instance().getBoolean(VALIDATION_ENABLED_KEY, true))
      {
         ValidatorApp validator = ValidatorApp.getInstance();
         List<ValidationProblem> validationProblems = validator
               .validate(validateAble);
         if (validationProblems != null && !validationProblems.isEmpty())
         {
            throw new ValidationProblemsException(validationProblems);
         }
      }
   }
}
