package org.eclipse.stardust.ui.web.reporting.ui;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.engine.core.pojo.data.Type;

/**
 * @author Yogesh.Manware
 * 
 */
public class UiHelper
{

   //private static final Logger trace = LogManager.getLogger(UiHelper.class);

   /**
    * @param value
    * @return
    */
   public static DataTypes mapDesciptorType(Class< ? > value)
   {
      Type t = mapToSimpleTypeXsdType(value);

      if (t.equals(Type.Boolean))
      {
         return DataTypes.BOOLEAN;
      }
      else if (t.equals(Type.Long) || t.equals(Type.Byte) || t.equals(Type.Double) || t.equals(Type.Float)
            || t.equals(Type.Integer) || t.equals(Type.Short) || t.equals(Type.Money))
      {
         return DataTypes.INTEGER;
      }
      else if (t.equals(Type.String))
      {
         return DataTypes.STRING;
      }
      else if (t.equals(Type.Timestamp) || t.equals(Type.Calendar))
      {
         return DataTypes.TIMESTAMP;
      }
      else if (t.equals(Type.Enumeration))
      {
         return DataTypes.ENUMERATION;
      }

      return DataTypes.STRING;
   }

   /**
    * @param value
    *           referred from ProcessPortal#DataFlowUtils.java
    * @return
    */
   public static Type mapToSimpleTypeXsdType(Class< ? > value)
   {
      if (value == null || String.class.equals(value))
      {
         return Type.String;
      }
      else if (Long.class.equals(value))
      {
         return Type.Long;
      }
      else if (Integer.class.equals(value))
      {
         return Type.Integer;
      }
      else if (Short.class.equals(value))
      {
         return Type.Short;
      }
      else if (Byte.class.equals(value))
      {
         return Type.Byte;
      }
      else if (Double.class.equals(value))
      {
         return Type.Double;
      }
      else if (Float.class.equals(value))
      {
         return Type.Float;
      }
      else if (Boolean.class.equals(value))
      {
         return Type.Boolean;
      }
      else if (Date.class.equals(value))
      {
         return Type.Timestamp;
      }
      else if (Calendar.class.equals(value) || Money.class.equals(value))
      {
         return Type.Calendar;
      }
      else if (Character.class.equals(value))
      {
         return Type.Char;
      }
      return Type.String;
   }

}
