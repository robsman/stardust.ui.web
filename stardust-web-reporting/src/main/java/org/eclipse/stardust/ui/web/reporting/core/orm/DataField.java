package org.eclipse.stardust.ui.web.reporting.core.orm;

import org.eclipse.stardust.ui.web.reporting.core.util.SqlUtil;

public class DataField
{
   private String name;
   private DataFieldType type;

   public enum DataFieldType
   {
      SHORT_STRING,
      LONG_STRING,
      NUMBER
   }

   public DataField(String name, DataFieldType type)
   {
      this.name = name;
      this.type = type;
   }

   public String getName()
   {
      return name;
   }

   public DataFieldType getType()
   {
      return type;
   }

   public String getColumnDefinitionSql(boolean withSqlType)
   {
      return SqlUtil.getColumnDefinitionSql(this, withSqlType);
   }



   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      DataField other = (DataField) obj;
      if (name == null)
      {
         if (other.name != null)
            return false;
      }
      else if (!name.equals(other.name))
         return false;
      if (type != other.type)
         return false;
      return true;
   }
}
