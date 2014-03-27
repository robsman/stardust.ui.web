package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.text.MessageFormat;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.stardust.ui.web.reporting.common.validation.AnnotationdBasedValidator;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidationProblem;
import org.eclipse.stardust.ui.web.reporting.common.validation.ValidatorApp;

public class ValidationTest extends TestCase
{
   public void testValidateNotNullNonRecursive()
   {
      ValidatorApp app = ValidatorApp.getInstance();
      ValidateParent root = new ValidateParent();

      List<ValidationProblem> validationProblems = app.validate(root, false);
      assertEquals(2, validationProblems.size());

      assertTrue(containsProblemAboutField("a1", root.getClass().getName(),
            validationProblems));
      assertTrue(containsProblemAboutField("child1", root.getClass().getName(),
            validationProblems));

      root.setA1("somevalue");
      validationProblems = app.validate(root, false);
      assertEquals(1, validationProblems.size());
      assertTrue(containsProblemAboutField("child1", root.getClass().getName(),
            validationProblems));

      root.setChild1(new ValidateChild());
      validationProblems = app.validate(root, false);
      assertEquals(0, validationProblems.size());
   }

   public void testValidateNotNullRecursivly()
   {
      ValidatorApp app = ValidatorApp.getInstance();
      ValidateParent root = new ValidateParent();
      root.setA1("somevalue");
      root.setA2("somevalue");

      ValidateChild child1 = new ValidateChild();
      root.setChild1(child1);

      List<ValidationProblem> validationProblems = app.validate(root);
      assertEquals(2, validationProblems.size());
      assertTrue(containsProblemAboutField("b1", ValidateChild.class.getName(),
            validationProblems));
      assertTrue(containsProblemAboutField("b2", ValidateChild.class.getName(),
            validationProblems));

      root.setChild1(null);
      validationProblems = app.validate(root);
      assertEquals(1, validationProblems.size());
      assertTrue(containsProblemAboutField("child1", ValidateParent.class.getName(),
            validationProblems));

      ValidateChildChild child3 = new ValidateChildChild();
      ValidateChildChild child4 = new ValidateChildChild();
      child1.setChild3(child3);
      child1.setChild4(child4);
      root.setChild1(child1);

      validationProblems = app.validate(root);
      assertEquals(4, validationProblems.size());
      assertTrue(containsProblemAboutField("b1", ValidateChild.class.getName(),
            validationProblems));
      assertTrue(containsProblemAboutField("b2", ValidateChild.class.getName(),
            validationProblems));
      assertTrue(containsProblemAboutField("c5", ValidateChildChild.class.getName(),
            validationProblems));
   }

   public void testCustomValidation()
   {
      ValidatorApp app = ValidatorApp.getInstance();
      ValidateParent root = new ValidateParent();
      List<ValidationProblem> validationProblems = app.validate(root);
      assertFalse(hasProblemWithMessage(ValidateParent.CUSTOM_VALIDATION_MESSAGE,
            validationProblems));

      root.setCustomValidationEnabled(true);
      validationProblems = app.validate(root);
      assertTrue(hasProblemWithMessage(ValidateParent.CUSTOM_VALIDATION_MESSAGE,
            validationProblems));

   }

   private boolean containsProblemAboutField(String fieldName, String className,
         List<ValidationProblem> validationProblems)
   {
      String msgTemplate = MessageFormat.format(
            AnnotationdBasedValidator.notNullMsgTemplate, fieldName, className);
      return hasProblemWithMessage(msgTemplate, validationProblems);
   }

   private boolean hasProblemWithMessage(String message,
         List<ValidationProblem> validationProblems)
   {
      for (ValidationProblem vp : validationProblems)
      {
         if (vp.getMessage().equals(message))
         {
            return true;
         }
      }

      return false;
   }

}
