package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
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


   @SuppressWarnings("unchecked")
   private Collection<? extends IValidateAble> readValidatableCollection(Field field, IValidateAble validateAble)
         throws RuntimeException
   {
      try
      {
         field.setAccessible(true);
         return (Collection< ? extends IValidateAble>) field.get(validateAble);
      }
      catch (IllegalAccessException e)
      {
         StringBuffer errorMsg = new StringBuffer();
         errorMsg.append("Error reading collection field ");
         errorMsg.append(field.getName());
         throw new RuntimeException(errorMsg.toString(), e);
      }
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
         //TODO: added special annotation to enable validation for type erased fields, we cannot use
         //reflection to detect such fields
         if (recursive)
         {
            Class< ? extends IValidateAble> validateAbleClass = validateAble.getClass();
            Field[] fields = validateAbleClass.getDeclaredFields();
            for (Field f : fields)
            {
               if(isValidateAbleCollection(f))
               {
                  Collection<? extends IValidateAble>
                     validateableCollection = readValidatableCollection(f, validateAble);
                  if(validateableCollection != null)
                  {
                     for(IValidateAble v: validateableCollection)
                     {
                        validate(validationContext, v, recursive);
                     }
                  }
               }

               if (isValidateAbleType(f.getType()))
               {
                  IValidateAble validateAbleField = readValidatableFieldValue(f,
                        validateAble);
                  validate(validationContext, validateAbleField, recursive);
               }
            }
         }
      }
   }

   private boolean isValidateAbleCollection(Field f)
   {
      if(Collection.class.isAssignableFrom(f.getType()))
      {
         Type genericType = f.getGenericType();
         if(genericType instanceof ParameterizedType)
         {
            //the class of the actual type argument -
            Class<?> actualTypeArgumentClass = null;
            ParameterizedType parameterizedType
               = (ParameterizedType) genericType;

            Type actualType = parameterizedType.getActualTypeArguments()[0];
            if(actualType instanceof WildcardType)
            {
               WildcardType wildCardType = (WildcardType) actualType;
               actualType = wildCardType.getUpperBounds()[0];
            }
            if(actualType instanceof Class)
            {
               actualTypeArgumentClass = (Class<?>) actualType;
            }

            if(actualTypeArgumentClass != null
                  && IValidateAble.class.isAssignableFrom(actualTypeArgumentClass))
            {
               return true;
            }
         }
      }

      return false;
   }

   private boolean isValidateAbleType(Class<?> typeClass)
   {
      return IValidateAble.class.isAssignableFrom(typeClass);
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
