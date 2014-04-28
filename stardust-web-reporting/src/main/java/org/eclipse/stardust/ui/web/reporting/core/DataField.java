package org.eclipse.stardust.ui.web.reporting.core;

import org.eclipse.stardust.ui.web.reporting.core.util.SqlUtil;

public class DataField extends AbstractColumn
{
   private DataFieldType type;

   public enum DataFieldType
   {
      SHORT_STRING,
      LONG_STRING,
      NUMBER
   }

   public DataField(String id, DataFieldType type)
   {
      super(id);
      this.type = type;
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
      result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
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
      if (getId() == null)
      {
         if (other.getId() != null)
            return false;
      }
      else if (!getId().equals(other.getId()))
         return false;
      if (type != other.type)
         return false;
      return true;
   }


}
