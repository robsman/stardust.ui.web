package org.eclipse.stardust.ui.web.modeler.xpdl.validation;

import static edu.emory.mathcs.backport.java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelFactory;
import org.eclipse.stardust.modeling.validation.IModelElementValidator;
import org.eclipse.stardust.modeling.validation.ValidatorRegistry;
import org.eclipse.stardust.modeling.validation.impl.ProcessInterfaceValidator;
import org.eclipse.stardust.ui.web.validation.ProxyReferenceValidator;
import org.junit.Test;

public class XpdlValidationTest
{
   @Test
   public void validationExtensionRegistryCanBeResolved()
   {
      assertThat(ValidationExtensionRegistry.getInstance(), is(notNullValue()));
   }

   @Test
   public void commonElementValidatorsAreBeingDicovered()
   {
      ValidatorRegistry.setValidationExtensionRegistry(ValidationExtensionRegistry
            .getInstance());

      IModelElementValidator[] elementValidators = ValidatorRegistry
            .getModelElementValidators(CarnotWorkflowModelFactory.eINSTANCE
                  .createProcessDefinitionType());


      assertThat(elementValidators, is(notNullValue()));

      assertThat((List<Object>) asList(elementValidators), hasItem(isA(ProcessInterfaceValidator.class)));
   }

   @Test
   public void pepperSpecificElementValidatorsAreBeingDicovered()
   {
      ValidatorRegistry.setValidationExtensionRegistry(ValidationExtensionRegistry
            .getInstance());

      IModelElementValidator[] elementValidators = ValidatorRegistry
            .getModelElementValidators(CarnotWorkflowModelFactory.eINSTANCE
                  .createProcessDefinitionType());

      assertThat(elementValidators, is(notNullValue()));

      assertThat((List<Object>) asList(elementValidators), hasItem(isA(ProxyReferenceValidator.class)));
   }
}
