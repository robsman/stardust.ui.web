package org.eclipse.stardust.ui.web.reporting.core.util;

import org.eclipse.stardust.ui.web.reporting.core.DataField;
import org.eclipse.stardust.ui.web.reporting.core.DataField.DataFieldType;

public class SqlUtil
{
   public static String getColumnDefinitionSql(DataField df, boolean withSqlType)
   {
      StringBuilder sqlBuilder = new StringBuilder();
      sqlBuilder.append(df.getId());
      if(withSqlType)
      {
         sqlBuilder.append(" ");
         sqlBuilder.append(getSqlType(df.getType()));
      }

      return sqlBuilder.toString();
   }

   public static String getSqlValue(Object o)
   {
      if(o == null)
      {
         return "null";
      }

      if(o instanceof String)
      {
         return "'"+o+"'";
      }

      return o.toString();
   }

   private static String getSqlType(DataFieldType fieldType)
   {
      final String sqlType;
      switch(fieldType)
      {
         case SHORT_STRING:
            sqlType = "VARCHAR2(128 CHAR)";
            break;
         case LONG_STRING:
            sqlType = "CLOB";
            break;
         case NUMBER:
            sqlType = "NUMBER";
            break;
         default:
            throw new RuntimeException("Unsupported field type: "+fieldType);
      }

      return sqlType;
   }

}
