package org.eclipse.stardust.ui.web.reporting.common.validation;

import org.eclipse.stardust.reporting.rt.IValidateAble;

public interface IValidateAbleCollectionContainer extends IValidateAble
{
   public void add(ValidateChild validateAble);
   public void resetToNull();
   public void resetToEmpty();

}
