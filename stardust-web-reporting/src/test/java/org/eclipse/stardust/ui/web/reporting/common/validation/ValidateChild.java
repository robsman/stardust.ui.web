package org.eclipse.stardust.ui.web.reporting.common.validation;

import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

@SuppressWarnings("unused")
public class ValidateChild implements IValidateAble
{
   @NotNull
   private Integer b1;

   @NotNull
   private Long b2;

   private ValidateChildChild child3;

   private ValidateChildChild child4;

   public void setB1(Integer b1)
   {
      this.b1 = b1;
   }

   public void setB2(Long b2)
   {
      this.b2 = b2;
   }



   public void setChild3(ValidateChildChild child3)
   {
      this.child3 = child3;
   }

   public void setChild4(ValidateChildChild child4)
   {
      this.child4 = child4;
   }
}
