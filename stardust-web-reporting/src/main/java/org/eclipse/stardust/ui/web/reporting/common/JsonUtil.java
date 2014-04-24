package org.eclipse.stardust.ui.web.reporting.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtil
{
   private static final String DEFAULT_DATE_FORMAT = "yyyy/MM/dd hh:mm:ss:SSS";


   public static Date parseDate(String s)
   {
      if(StringUtils.isNotEmpty(s))
      {
         SimpleDateFormat f = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
         try
         {
            return f.parse(s);
         }
         catch (ParseException e)
         {
            throw new RuntimeException("Could not parse date from string: "+s);
         }
      }

      return null;
   }

   // TODO: refactor this method / the caller code
   /**
    *
    * @param jsonObject
    * @param primitiveObject
    */
   public static void addPrimitiveObjectToJsonObject(JsonObject jsonObject, String key,
         Object value)
   {
      if (value == null)
      {
         jsonObject.addProperty(key, (String) null);
      }
      else if (value instanceof Boolean)
      {
         jsonObject.addProperty(key, (Boolean) value);

      }
      else if (value instanceof Character)
      {
         jsonObject.addProperty(key, (Character) value);

      }
      else if (value instanceof Number)
      {
         jsonObject.addProperty(key, (Number) value);

      }
      else
      {
         jsonObject.addProperty(key, value.toString());
      }
   }

   public static JsonPrimitive convertJavaToPrimitive(Object value)
   {
      if(value != null)
      {
         if (value instanceof String)
         {
            return new JsonPrimitive((String) value);
         }
         else if (value instanceof Boolean)
         {
            return new JsonPrimitive((Boolean) value);
         }
         else if (value instanceof Number)
         {
            return new JsonPrimitive((Number) value);
         }
         else if (value instanceof Date)
         {
            Date date = (Date) value;
            SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
            return new JsonPrimitive(dateFormat.format(date));
         }
         else
         {
            throw new RuntimeException("Unsupported object type : "+value);
         }
      }

      return null;
   }

   public static Object convertPrimitiveToJava(JsonPrimitive primitive)
   {
      if (primitive != null)
      {
         if (primitive.isString())
         {
            return primitive.getAsString();
         }
         else if (primitive.isBoolean())
         {
            return primitive.getAsBoolean();
         }
         else if (primitive.isNumber())
         {
            return primitive.getAsNumber();
         }
         else
         {
            throw new RuntimeException("JSON Primitive " + primitive
                  + " has an unknown type.");
         }
      }

      return null;
   }

   private static void nullCheck(JsonElement jsonElement)
   {
      if (jsonElement == null || jsonElement.isJsonNull())
      {
         throw new RuntimeException("JsonElement argument is null");
      }
   }

   private static void primitiveValueCheck(JsonElement jsonElement)
   {
      nullCheck(jsonElement);
      if (!jsonElement.isJsonPrimitive())
      {
         StringBuilder msgBuilder = new StringBuilder();
         msgBuilder.append("JsonElement").append("'");
         msgBuilder.append(jsonElement).append("'");
         msgBuilder.append(" is not a JsonPrimitive");
         throw new RuntimeException(msgBuilder.toString());
      }
   }

   public static Date getPrimitiveValueAsDate(JsonElement jsonElement)
   {
      String primitiveValueAsString = getPrimitiveValueAsString(jsonElement);
      SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
      try
      {
         return dateFormat.parse(primitiveValueAsString);
      }
      catch (ParseException e)
      {
         StringBuilder msgBuilder = new StringBuilder();
         msgBuilder.append("JsonElement").append("'");
         msgBuilder.append(jsonElement).append("'");
         msgBuilder.append(" cannot be parsed as Date");
         throw new RuntimeException(msgBuilder.toString(), e);
      }
   }

   public static long getPrimitiveValueAsLong(JsonElement jsonElement)
   {
      nullCheck(jsonElement);
      primitiveValueCheck(jsonElement);

      JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
      return jsonPrimitive.getAsLong();
   }

   public static String getPrimitiveValueAsString(JsonElement jsonElement)
   {
      nullCheck(jsonElement);
      primitiveValueCheck(jsonElement);

      JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
      return jsonPrimitive.getAsString();
   }

   public static JsonPrimitive getPrimitiveProperty(JsonElement jsonElement,
         String property)
   {
      nullCheck(jsonElement);
      if (!jsonElement.isJsonObject())
      {
         StringBuilder msgBuilder = new StringBuilder();
         msgBuilder.append("JsonElement").append("'");
         msgBuilder.append(jsonElement).append("'");
         msgBuilder.append(" is not a JsonObject");
         throw new RuntimeException(msgBuilder.toString());
      }

      return getPrimitiveProperty(jsonElement.getAsJsonObject(), property);
   }

   public static JsonPrimitive getPrimitiveProperty(JsonObject jsonObject, String property)
   {
      if (StringUtils.isEmpty(property))
      {
         throw new RuntimeException("Property is null");
      }

      if (jsonObject == null || jsonObject.isJsonNull())
      {
         throw new RuntimeException("JsonObject is null");
      }

      JsonElement jsonProperty = jsonObject.get(property);
      if (jsonProperty != null && !jsonProperty.isJsonNull())
      {
         if (!jsonProperty.isJsonPrimitive())
         {
            StringBuilder msgBuilder = new StringBuilder();
            msgBuilder.append("Property").append("'");
            msgBuilder.append(property).append("'");
            msgBuilder.append(" is not a JsonPrimitive");
            throw new RuntimeException(msgBuilder.toString());
         }

         JsonPrimitive jsonPrimitive = (JsonPrimitive) jsonProperty.getAsJsonPrimitive();
         return jsonPrimitive;

      }

      return null;
   }

   public static JsonObject getAsJsonObject(JsonElement jsonElement)
   {
      if (jsonElement == null || jsonElement.isJsonNull())
      {
         throw new RuntimeException("JsonElement argument is null");
      }

      if (!jsonElement.isJsonObject())
      {
         StringBuilder msgBuilder = new StringBuilder();
         msgBuilder.append("JsonElement").append("'");
         msgBuilder.append(jsonElement).append("'");
         msgBuilder.append(" is not a JsonObject");
         throw new RuntimeException(msgBuilder.toString());
      }

      return jsonElement.getAsJsonObject();
   }

   public static String getStringProperty(JsonElement jsonElement, String property)
   {
      JsonObject jsonObject = getAsJsonObject(jsonElement);
      JsonPrimitive jsonPrimitive = getPrimitiveProperty(jsonObject, property);

      if (jsonPrimitive != null && !jsonPrimitive.isJsonNull())
      {
         return getPrimitiveValueAsString(jsonPrimitive);
      }

      return null;
   }

}
