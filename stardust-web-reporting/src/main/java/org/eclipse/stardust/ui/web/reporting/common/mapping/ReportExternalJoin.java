package org.eclipse.stardust.ui.web.reporting.common.mapping;

import java.util.List;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;

public class ReportExternalJoin implements IValidateAble
{
   private String joinType;

   private String restUri;

   private List<ReportExternalJoinField> fields;

   private String internalKey;

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
}
