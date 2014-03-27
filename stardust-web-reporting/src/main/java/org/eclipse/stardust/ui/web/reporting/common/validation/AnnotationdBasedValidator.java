package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class AnnotationdBasedValidator implements IValidator
{
   public static final String notNullMsgTemplate = "Field ''{0}'' for class ''{1}'' must not be null.}";

   public void validate(ValidationContext validationContext, IValidateAble validateAble)
   {
      if(validateAble != null)
      {
         Class< ? extends IValidateAble> validateAbleClass = validateAble.getClass();
         List<Field> notNullFields = getAnnotatedFields(validateAbleClass, NotNull.class);
         for (Field notNullField : notNullFields)
         {
            try
            {
               notNullField.setAccessible(true);
               Object fieldValue = notNullField.get(validateAble);

               if (fieldValue == null)
               {
                  String validationErrorMsg =
                        MessageFormat.format(notNullMsgTemplate, notNullField.getName(), validateAbleClass.getName());
                  ValidationProblem validationProblem = new ValidationProblem(validationErrorMsg);
                  validationContext.addValidationProblem(validationProblem);
               }
            }
            catch (Exception e)
            {
               StringBuffer errorMsg = new StringBuffer();
               errorMsg.append("Error performing annotation based validation for field: ");
               errorMsg.append(notNullField.getName()).append(" ");
               errorMsg.append(" for validation class ");
               errorMsg.append(validateAbleClass.getName());
               throw new RuntimeException(errorMsg.toString());
            }
         }

      }
   }

   private static List<Field> getAnnotatedFields(Class< ? extends IValidateAble> validateAbleClass,
         Class< ? extends Annotation> annotationClass)
   {
      List<Field> annotatedFields = new ArrayList<Field>();
      for (Field field : validateAbleClass.getDeclaredFields())
      {
         if (field.isAnnotationPresent(annotationClass))
         {
            annotatedFields.add(field);
         }
      }

      return annotatedFields;
   }

}
