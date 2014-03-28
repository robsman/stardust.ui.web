package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportExternalJoinField implements IValidateAble
{
   @NotNull
   private String id;

   private String externalKey;

   @NotNull
   private String useAs;

   public String getId()
   {
      return id;
   }

   public String getExternalKey()
   {
      return externalKey;
   }

   public String getUseAs()
   {
      return useAs;
   }
}
