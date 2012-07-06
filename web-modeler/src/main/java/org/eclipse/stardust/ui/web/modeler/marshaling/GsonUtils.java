package org.eclipse.stardust.ui.web.modeler.marshaling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GsonUtils
{
   public static String extractString(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member) && member.isJsonPrimitive()
            && member.getAsJsonPrimitive().isString()
            ? member.getAsString()
            : (String) null;
   }

   public static Integer extractInt(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member) && member.isJsonPrimitive()
            && member.getAsJsonPrimitive().isNumber()
            ? (Integer) member.getAsInt()
            : null;
   }

   public static Long extractLong(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member) && member.isJsonPrimitive()
            && member.getAsJsonPrimitive().isNumber()
            ? (Long) member.getAsLong()
            : null;
   }

   public static Boolean extractBoolean(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member) && member.isJsonPrimitive()
            && member.getAsJsonPrimitive().isBoolean()
            ? (Boolean) member.getAsBoolean()
            : null;
   }

   public static String extractString(JsonObject json, String objectName, String memberName)
   {
      if (json.has(objectName) && json.get(objectName).isJsonObject())
      {
         return extractString(json.getAsJsonObject(objectName), memberName);
      }
      else
      {
         throw new NullPointerException("Missing JSON sub-object " + objectName);
      }
   }

   public static Integer extractInt(JsonObject json, String objectName, String memberName)
   {
      if (json.has(objectName) && json.get(objectName).isJsonObject())
      {
         return extractInt(json.getAsJsonObject(objectName), memberName);
      }
      else
      {
         throw new NullPointerException("Missing JSON sub-object " + objectName);
      }
   }

   public static Long extractLong(JsonObject json, String objectName, String memberName)
   {
      if (json.has(objectName) && json.get(objectName).isJsonObject())
      {
         return extractLong(json.getAsJsonObject(objectName), memberName);
      }
      else
      {
         throw new NullPointerException("Missing JSON sub-object " + objectName);
      }
   }
}
