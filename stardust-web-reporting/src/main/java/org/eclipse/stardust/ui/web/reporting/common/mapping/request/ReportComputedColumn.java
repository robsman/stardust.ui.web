package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportComputedColumn implements IValidateAble
{
   @NotNull
   private String id;

   private String name;

   @NotNull
   private String type;

   @NotNull
   private String formula;

   public String getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public String getType()
   {
      return type;
   }

   public String getFormula()
   {
      return formula;
   }
}
