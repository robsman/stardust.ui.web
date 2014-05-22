package org.eclipse.stardust.ui.web.reporting.common.validation;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.stardust.reporting.rt.IValidateAble;
import org.eclipse.stardust.reporting.rt.NotNull;

public class WildCardCollectionContainer implements IValidateAbleCollectionContainer
{
   @NotNull
   private Collection< ? extends IValidateAble> validateAbles;

   //make sure it does not got validated too - so pick a untyped collection which will not be recognized
   private Collection untypedCollection = new ArrayList();

   @Override
   public void add(ValidateChild validateAble)
   {
      untypedCollection.add(validateAble);
      validateAbles = untypedCollection;
   }

   @Override
   public void resetToNull()
   {
      validateAbles = null;
      untypedCollection = new ArrayList();
   }

   @Override
   public void resetToEmpty()
   {
      validateAbles = new ArrayList<IValidateAble>();
      untypedCollection = new ArrayList();
   }
}
