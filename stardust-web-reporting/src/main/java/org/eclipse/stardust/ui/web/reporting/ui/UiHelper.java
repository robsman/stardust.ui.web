package org.eclipse.stardust.ui.web.reporting.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;

import org.eclipse.stardust.common.Money;
import org.eclipse.stardust.engine.core.pojo.data.Type;

/**
 * @author Yogesh.Manware
 * 
 */
public class UiHelper
{

   // private static final Logger trace = LogManager.getLogger(UiHelper.class);

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

   /**
    * This is an utility method to generate properties file as an java script object. Is used for building self-contained report
    * @param args
    */
   public static void main(String[] args)
   {
      Locale locale = new Locale("en", "US");
      ResourceBundle labels = ResourceBundle.getBundle("bpm-reporting-client-messages_en", locale);
      
      Enumeration<String> labelKeys = labels.getKeys();
      
      StringBuffer sb = new StringBuffer();
      
      while (labelKeys.hasMoreElements())
      {
         String lk = (String) labelKeys.nextElement();
         sb.append("\"").append(lk).append("\":");
         sb.append("\"").append(labels.getString(lk)).append("\",");
      }
      
      String flatString = sb.toString().substring(0, sb.length()-1);
      
      System.out.println("{" + flatString + "}");
   }
}
