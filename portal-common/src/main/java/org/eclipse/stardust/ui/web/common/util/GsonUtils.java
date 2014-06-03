/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

/**
 * @author Subodh.Godbole
 *
 */
public class GsonUtils
{
   private static final Logger trace = LogManager.getLogger(GsonUtils.class);

   private final static JsonParser jsonParser = new JsonParser();

   /**
    * @param jsonText
    * @return
    */
   public static JsonObject readJsonObject(String jsonText)
   {
      try
      {
         JsonElement parsedJson = jsonParser.parse(jsonText);
         if ((null != parsedJson) && parsedJson.isJsonObject())
         {
            return parsedJson.getAsJsonObject();
         }
         else
         {
            trace.warn("Expected a JSON object, but received something else.");
            throw new IllegalArgumentException();
         }
      }
      catch (JsonParseException jpe)
      {
         trace.warn("Expected a JSON object, but received no valid JSON at all.", jpe);
         throw new IllegalArgumentException(jpe);
      }
   }

   /**
    * @param jsonText
    * @return
    */
   public static JsonElement readJsonElement(String jsonText)
   {
      try
      {
         JsonElement parsedJson = jsonParser.parse(jsonText);
         if (null != parsedJson)
         {
            return parsedJson;
         }
         else
         {
            trace.warn("Expected a JSON Elemnent, but received something else.");
            throw new IllegalArgumentException();
         }
      }
      catch (JsonParseException jpe)
      {
         trace.warn("Expected a JSON Elemnent, but received no valid JSON at all.", jpe);
         throw new IllegalArgumentException(jpe);
      }
   }

   /**
    * @param jsonText
    * @return
    */
   public static Map<String, Object> readJsonMap(String jsonText)
   {
      return processJson(readJsonObject(jsonText));
   }
   
   /**
    * @param data
    * @return
    */
   public static String stringify(Object data)
   {
      JsonElement jsonElement = readJsonObject(data);
      return null != jsonElement ? jsonElement.toString() : null;
   }

   /**
    * @param data
    * @return
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public static JsonElement readJsonObject(Object data)
   {
      JsonElement jsonElem = null;
      if (null != data)
      {
         // TODO Support other Primitives
         if (data instanceof String)
         {
            jsonElem = new JsonPrimitive((String)data);
         }
         else if (data instanceof Map)
         {
            JsonObject jsonObj = new JsonObject();
            for (Entry<String, Object> entry : ((Map<String, Object>)data).entrySet())
            {
               jsonObj.add(entry.getKey(), readJsonObject(entry.getValue()));
            }
            jsonElem = jsonObj;
         }
         else if (data instanceof List)
         {
            JsonArray jsonArray = new JsonArray();
            for (Object obj : ((List)data))
            {
               jsonArray.add(readJsonObject(obj));
            }
            jsonElem = jsonArray;
         }
      }
      
      return jsonElem;
   }
   
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

   /**
    * @param json
    * @param memberName
    * @return
    */
   public static JsonArray extractJsonArray(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member) && member.isJsonArray()
            ? (JsonArray) member.getAsJsonArray()
            : null;
   }
   
   public static Boolean extractBoolean(JsonObject json, String memberName)
   {
      return extractBoolean(json, memberName, null);
   }

   public static Boolean extractBoolean(JsonObject json, String memberName, Boolean defaultValue)
   {
      JsonElement member = json.get(memberName);
      return (null != member) && member.isJsonPrimitive()
            && member.getAsJsonPrimitive().isBoolean()
            ? (Boolean) member.getAsBoolean()
            : defaultValue;
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

   public static JsonObject extractObject(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member && !member.isJsonNull()) ? member.getAsJsonObject() : null;
   }

   /**
    * @param json
    * @param memberName
    * @return
    */
   public static Map<String, Object> extractMap(JsonObject json, String memberName)
   {
      JsonElement member = json.get(memberName);
      return (null != member) ? processJson(member.getAsJsonObject()) : null;
   }
   
   /**
    * @param jsonObj
    * @return
    */
   private static Map<String, Object> processJson(JsonObject jsonObj)
   {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      for (Entry<String, JsonElement> entry : jsonObj.entrySet())
      {
         map.put(entry.getKey(), processJson(entry.getValue()));         
      }
      
      return map;
   }   
   
   /**
    * @param jsonElem
    * @return
    */
   private static Object processJson(JsonElement jsonElem)
   {
      if (jsonElem.isJsonPrimitive())
      {
         if (jsonElem.getAsJsonPrimitive().isBoolean())
         {
            return jsonElem.getAsJsonPrimitive().getAsBoolean();
         }
         else if(jsonElem.getAsJsonPrimitive().isNumber())
         {
            return jsonElem.getAsJsonPrimitive().getAsNumber();
         }
         else
         {
            return jsonElem.getAsJsonPrimitive().getAsString();
         }
      }
      else if (jsonElem.isJsonArray())
      {
         List<Object> arrayData = new ArrayList<Object>();
         JsonArray array = jsonElem.getAsJsonArray();
         Iterator<JsonElement> it = array.iterator();
         while (it.hasNext())
         {
            arrayData.add(processJson(it.next()));
         }
         return arrayData;
      }
      else if (jsonElem.isJsonObject())
      {
         return processJson(jsonElem.getAsJsonObject());
      }
      
      return null;
   }
}
