package org.eclipse.stardust.ui.web.modeler.marshaling;

import java.io.StringWriter;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;

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

   public static String extractAsString(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member) && member.isJsonPrimitive()
            ? member.getAsString()
            : (String) null;
   }

   /**
    * checks existence of non null attribute
    *
    * @param json
    * @param memberName
    * @return
    */
   public static boolean hasNotJsonNull(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member && !member.isJsonNull()) ? true : false;
   }


   public static boolean safeGetBool(JsonObject jsonObject, String memberName)
   {
      if (jsonObject.has(memberName))
      {
         JsonElement member = jsonObject.get(memberName);
         if (member.isJsonPrimitive())
         {
            return member.getAsBoolean();
         }
      }
      return false;
   }

   public static String safeGetAsString(JsonObject jsonObject, String memberName)
   {
      if (jsonObject.has(memberName))
      {
         JsonElement member = jsonObject.get(memberName);
         if (member.isJsonPrimitive())
         {
            return member.getAsString();
         }
      }
      return null;
   }

   public static JsonObject safeGetAsJsonObject(JsonObject jsonObject, String memberName)
   {
      if (jsonObject.has(memberName))
      {
         JsonElement member = jsonObject.get(memberName);
         if (member.isJsonObject())
         {
            return member.getAsJsonObject();
         }
      }
      return null;
   }

   public static JsonArray safeGetAsJsonArray(JsonObject jsonObject, String memberName)
   {
      if (jsonObject.has(memberName))
      {
         JsonElement member = jsonObject.get(memberName);
         if (member.isJsonArray())
         {
            return member.getAsJsonArray();
         }
      }
      return null;
   }

   public static String toPrettyString(JsonElement json)
   {
      Gson gson = new Gson();
      StringWriter writer = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setIndent("  ");
      // jsonWriter.setHtmlSafe(false); commented out because it's ignored.
      jsonWriter.setLenient(true);
      gson.toJson(json, jsonWriter);
      return writer.toString();
   }

   public static JsonObject deepCopy(JsonObject src)
   {
      return (JsonObject) doDeepCopy(src);
   }

   private static JsonElement doDeepCopy(JsonElement src)
   {
      if ((src instanceof JsonPrimitive) || (src instanceof JsonNull))
      {
         // immutable
         return src;
      }
      else if (src instanceof JsonArray)
      {
         JsonArray copy = new JsonArray();
         for (JsonElement element : (JsonArray) src)
         {
            copy.add(doDeepCopy(element));
         }
         return copy;
      }
      else if (src instanceof JsonObject)
      {
         JsonObject copy = new JsonObject();
         for (Map.Entry<String, JsonElement> attr : ((JsonObject) src).entrySet())
         {
            copy.add(attr.getKey(), doDeepCopy(attr.getValue()));
         }
         return copy;
      }
      else
      {
         throw new IllegalArgumentException("Unsupported JsonElement instance: "
               + src.getClass());
      }
   }
}