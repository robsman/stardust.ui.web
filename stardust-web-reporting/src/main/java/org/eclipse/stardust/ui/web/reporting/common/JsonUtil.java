package org.eclipse.stardust.ui.web.reporting.common;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class JsonUtil
{

   //TODO: refactor this method / the caller code
   /**
   *
   * @param jsonObject
   * @param primitiveObject
   */
  public static void addPrimitiveObjectToJsonObject(JsonObject jsonObject, String key, Object value)
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


   public static Object convertToJavaObject(JsonPrimitive primitive)
   {
      if(primitive != null)
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
            throw new RuntimeException("JSON Primitive " + primitive + " has an unknown type.");
         }
      }

      return null;
   }
}
