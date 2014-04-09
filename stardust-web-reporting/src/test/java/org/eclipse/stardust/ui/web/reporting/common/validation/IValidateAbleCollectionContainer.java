package org.eclipse.stardust.ui.web.reporting.common.validation;

public interface IValidateAbleCollectionContainer extends IValidateAble
{
   public void add(ValidateChild validateAble);
   public void resetToNull();
   public void resetToEmpty();

}
