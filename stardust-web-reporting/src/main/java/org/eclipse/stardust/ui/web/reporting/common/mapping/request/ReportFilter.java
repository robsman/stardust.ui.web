package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportFilter implements IValidateAble
{
   private int index;
   @NotNull
   private JsonElement value;
   private JsonElement values;
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

   public JsonElement getValues()
   {
      return values;
   }

   public void setValues(JsonElement values)
   {
      this.values = values;
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
