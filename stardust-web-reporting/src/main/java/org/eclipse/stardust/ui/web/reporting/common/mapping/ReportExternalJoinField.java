package org.eclipse.stardust.ui.web.reporting.common.mapping;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;

public class ReportExternalJoinField implements IValidateAble
{
   private String id;

   private String externalKey;

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
