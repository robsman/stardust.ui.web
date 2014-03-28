package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import java.util.List;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportExternalJoin implements IValidateAble
{
   private String joinType;

   @NotNull
   private String restUri;

   private List<ReportExternalJoinField> fields;

   private String internalKey;

   private String externalKey;

   public String getJoinType()
   {
      return joinType;
   }

   public String getRestUri()
   {
      return restUri;
   }

   public List<ReportExternalJoinField> getFields()
   {
      return fields;
   }

   public String getInternalKey()
   {
      return internalKey;
   }

   public String getExternalKey()
   {
      return externalKey;
   }
}
