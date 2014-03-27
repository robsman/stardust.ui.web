package org.eclipse.stardust.ui.web.reporting.common.validation;

import org.eclipse.stardust.ui.web.reporting.common.validation.ICustomValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationContext;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationProblem;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ValidateParent implements ICustomValidateAble
{
   public static final String CUSTOM_VALIDATION_MESSAGE = "hello custom validator";
   @NotNull
   private String a1;

   private String a2;

   @NotNull
   private ValidateChild child1;

   private ValidateChild child2;

   private boolean customValidationEnabled = false;

   public String getA1()
   {
      return a1;
   }

   public void setA1(String a1)
   {
      this.a1 = a1;
   }

   public String getA2()
   {
      return a2;
   }

   public void setA2(String a2)
   {
      this.a2 = a2;
   }

   public ValidateChild getChild1()
   {
      return child1;
   }

   public void setChild1(ValidateChild child1)
   {
      this.child1 = child1;
   }

   public ValidateChild getChild2()
   {
      return child2;
   }

   public void setChild2(ValidateChild child2)
   {
      this.child2 = child2;
   }

   public void setCustomValidationEnabled(boolean customValidationEnabled)
   {
      this.customValidationEnabled = customValidationEnabled;
   }

   @Override
   public void validate(ValidationContext validationContext)
   {
      if(customValidationEnabled)
      {
         ValidationProblem validationProblem = new ValidationProblem(CUSTOM_VALIDATION_MESSAGE);
         validationContext.addValidationProblem(validationProblem);
      }
   }
}
