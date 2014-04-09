package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
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


   private void testValidateCollection(IValidateAbleCollectionContainer container)
   {
      ValidatorApp app = ValidatorApp.getInstance();
      container.resetToNull();

      List<ValidationProblem> validationProblems = app.validate(container);
      assertEquals(1, validationProblems.size());
      //assert it has a NotNull property with name "validateAbles"
      assertTrue(containsProblemAboutField("validateAbles", container.getClass().getName(), validationProblems));

      //one element with 2 not null fields shoudl give you 2 problems
      container.add(new ValidateChild());
      validationProblems = app.validate(container);
      assertTrue(containsProblemAboutField("b1", ValidateChild.class.getName(), validationProblems));
      assertTrue(containsProblemAboutField("b2", ValidateChild.class.getName(), validationProblems));
      assertEquals(2, validationProblems.size());

      //two elements should give you 4 problems
      container.add(new ValidateChild());
      validationProblems = app.validate(container);
      assertEquals(4, validationProblems.size());

      //add another 2 element should give you a total of 8
      container.add(new ValidateChild());
      container.add(new ValidateChild());
      validationProblems = app.validate(container);
      assertEquals(8, validationProblems.size());

      //null values should not cause any problem
      container.add(null);
      validationProblems = app.validate(container);
      assertEquals(8, validationProblems.size());

      //empty collection should give you 0 problems
      container.resetToEmpty();
      validationProblems = app.validate(container);
      assertEquals(0, validationProblems.size());
   }


   public void testValidateCollection()
   {
      //validate implementation where the property uses wildcard type
      testValidateCollection(new WildCardCollectionContainer());
      //validate implementation where the property is strongly type
      testValidateCollection(new ParametrizedCollectionContainer());
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
