package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidatorApp
{
   private static ValidatorApp instance = new ValidatorApp();

   private Map<Class< ? extends IValidator>, IValidator> validators;

   private ValidatorApp()
   {
      validators = new HashMap<Class< ? extends IValidator>, IValidator>();
      validators.put(AnnotationdBasedValidator.class, new AnnotationdBasedValidator());
   }

   public static ValidatorApp getInstance()
   {
      return instance;
   }

   private IValidateAble readValidatableFieldValue(Field field, IValidateAble validateAble)
         throws RuntimeException
   {
      try
      {
         field.setAccessible(true);
         return (IValidateAble) field.get(validateAble);
      }
      catch (IllegalAccessException e)
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Error reading field ");
         errorMsg.append(field.getName());
         throw new RuntimeException(errorMsg.toString(), e);
      }
   }

   private void validate(ValidationContext validationContext, IValidateAble validateAble,
         boolean recursive)
   {
      if (validateAble != null)
      {
         // call the registered validators
         for (IValidator validator : validators.values())
         {
            validator.validate(validationContext, validateAble);
         }

         // give possibility to apply custom logic
         // only for that specific class without requiring them to write a custom
         // validator
         if (validateAble instanceof ICustomValidateAble)
         {
            ICustomValidateAble customValidateAble = (ICustomValidateAble) validateAble;
            customValidateAble.validate(validationContext);
         }

         // also validate members of type IValidateAble if recursive validation is enabled
         if (recursive)
         {
            Class< ? extends IValidateAble> validateAbleClass = validateAble.getClass();
            Field[] fields = validateAbleClass.getDeclaredFields();
            for (Field f : fields)
            {
               if (IValidateAble.class.isAssignableFrom(f.getType()))
               {
                  IValidateAble validateAbleField = readValidatableFieldValue(f,
                        validateAble);
                  validate(validationContext, validateAbleField, recursive);
               }
            }
         }
      }
   }

   // main entry point for validation
   public List<ValidationProblem> validate(IValidateAble validateAble)
   {
      ValidationContext validationContext = new ValidationContext();
      validate(validationContext, validateAble, true);
      return validationContext.getValidationProblems();
   }

   // main entry point for validation
   public List<ValidationProblem> validate(IValidateAble validateAble, boolean recursive)
   {
      ValidationContext validationContext = new ValidationContext();
      validate(validationContext, validateAble, recursive);
      return validationContext.getValidationProblems();
   }
}
