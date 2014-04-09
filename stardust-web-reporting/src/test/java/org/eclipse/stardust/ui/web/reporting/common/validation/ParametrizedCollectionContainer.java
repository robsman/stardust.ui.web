package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ParametrizedCollectionContainer implements IValidateAbleCollectionContainer
{
   @NotNull
   private Collection<ValidateChild> validateAbles;



   @Override
   public void add(ValidateChild validateAble)
   {
      if(validateAbles == null)
      {
         validateAbles = new ArrayList<ValidateChild>();
      }

      validateAbles.add(validateAble);
   }

   @Override
   public void resetToNull()
   {
      validateAbles = null;

   }

   @Override
   public void resetToEmpty()
   {
      validateAbles = new ArrayList<ValidateChild>();
   }

}
