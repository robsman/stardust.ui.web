package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

public class ReportResultDataRowColumn
{
   private String javaType;

   private Object value;

   private String name;

   public ReportResultDataRowColumn(String name, String javaType, Object value)
   {
      this.name = name;
      this.javaType = javaType;
      this.value = value;
   }

   public String getName()
   {
      return name;
   }

   public String getJavaType()
   {
      return javaType;
   }

   public Object getValue()
   {
      return value;
   }
}
