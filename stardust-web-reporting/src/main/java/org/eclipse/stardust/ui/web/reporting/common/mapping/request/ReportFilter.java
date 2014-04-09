package org.eclipse.stardust.ui.web.reporting.common.mapping.request;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.common.validation.IValidateAble;
import org.eclipse.stardust.ui.web.reporting.common.validation.annotations.NotNull;

public class ReportFilter implements IValidateAble
{
   public enum OperatorType {
      E("Equal"),
      LE("Less or Equal"),
      GE("Greater or Equal"),
      NE("Not Equal"),
      B("Between"),
      I("In"),
      NI("Not In"),
      L("Like");

      private String description;
      OperatorType(String description)
      {
         this.description = description;
      }
      @Override
      public String toString()
      {
         return description;
      }
   }

   private int index;
   @NotNull
   private JsonElement value;
   private String operator;
   @SuppressWarnings("unused")
   private String $$hashKey;
   private String dimension;
   private ReportFilterMetaData metadata;


   public int getIndex()
   {
      return index;
   }

   public JsonElement getValue()
   {
      return value;
   }

   public String getOperator()
   {
      return operator;
   }

   public String getDimension()
   {
      return dimension;
   }

   public ReportFilterMetaData getMetadata()
   {
      return metadata;
   }

   public boolean isSingleValue()
   {
      if(value != null && value.isJsonPrimitive())
      {
         return true;
      }

      return false;
   }

   public boolean isListValue()
   {
      if(value != null && value.isJsonArray())
      {
         return true;
      }

      return false;
   }

   public String getSingleValue()
   {
      if(isSingleValue())
      {
         return JsonUtil.getPrimitiveValueAsString(value);
      }

      return null;
   }

   public List<String> getListValues()
   {
      List<String> listValues = new ArrayList<String>();
      if(isListValue())
      {
         JsonArray jsonArray = value.getAsJsonArray();
         for (int i = 0; i < jsonArray.size(); i++)
         {
            JsonElement je = jsonArray.get(i);
            String listValue = JsonUtil.getPrimitiveValueAsString(je);
            listValues.add(listValue);
         }
      }

      return listValues;
   }
}
