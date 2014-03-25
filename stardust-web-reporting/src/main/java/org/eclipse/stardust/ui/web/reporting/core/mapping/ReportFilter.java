package org.eclipse.stardust.ui.web.reporting.core.mapping;

public class ReportFilter
{
   private int index;
   private Object value;
   private String operator;
   private String $$hashKey;
   private String dimension;


   public Object getValue()
   {
      return value;
   }

   public void setValue(Object value)
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
