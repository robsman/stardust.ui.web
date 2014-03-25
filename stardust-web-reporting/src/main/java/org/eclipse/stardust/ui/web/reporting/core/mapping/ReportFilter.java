package org.eclipse.stardust.ui.web.reporting.core.mapping;

import com.google.gson.JsonElement;

public class ReportFilter
{
   private int index;
   private JsonElement value;
   private String operator;
   private String $$hashKey;
   private String dimension;

   public JsonElement getValue()
   {
      return value;
   }

   public void setValue(JsonElement value)
   {
      this.value = value;
   }

   public String getOperator()
   {
      return operator;
   }

   public void setOperator(String operator)
   {
      this.operator = operator;
   }

   public String getDimension()
   {
      return dimension;
   }

   public void setDimension(String dimension)
   {
      this.dimension = dimension;
   }
}
