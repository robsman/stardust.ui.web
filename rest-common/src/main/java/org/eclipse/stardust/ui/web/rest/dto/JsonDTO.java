/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.ui.web.rest.util.JsonMarshaller;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public class JsonDTO
{
   private final static JsonMarshaller jsonIo = new JsonMarshaller();

   /**
    * @return
    */
   public static JsonObject getJsonObject(String json)
   {
      return jsonIo.readJsonObject(json);
   }

   /**
    * @return
    */
   public static JsonArray getJsonArray(String json)
   {
      return jsonIo.readJsonArray(json);
   }

   /**
    * @param json
    * @return
    */
   @SuppressWarnings("unchecked")
   public static <T> List<T> getAsList(String json, Class<T> type)
   {
      JsonArray jArray = getJsonArray(json);
      List<T> list = new ArrayList<T>();
      for (JsonElement jElem : jArray)
      {
         list.add((T)getJsonElemAs(jElem, type));
      }

      return list;
   }

   /**
    * @param jElem
    * @param type
    * @return
    */
   public static Object getJsonElemAs(JsonElement jElem, Class<?> type)
   {
      if(type == Integer.class)
      {
         return jElem.getAsInt();
      }
      else if(type == Long.class)
      {
         return jElem.getAsLong();
      }
      else if(type == Double.class)
      {
         return jElem.getAsDouble();
      }
      else if(type == Float.class)
      {
         return jElem.getAsFloat();
      }
      else if(type == String.class)
      {
         return jElem.getAsString();
      }
      else
      {
         throw new RuntimeException("Not supported");
      }
   }

   /*
    *
    */
   public static Map<String, Object> getAsMap(String json)
   {
      return toObject(getJsonObject(json));
   }

   /**
    * @param elem
    * @return
    */
   private static Map<String, Object> toObject(JsonObject elem)
   {
      Map<String, Object> data = new HashMap<String, Object>();
      for (Entry<String, JsonElement> entry : elem.entrySet())
      {
         data.put(entry.getKey(), toObject(entry.getValue()));
      }

      return data;
   }

   /**
    * @param elem
    * @return
    */
   private static List<Object> toObject(JsonArray elem)
   {
      List<Object> data = new ArrayList<Object>();
      for (int i = 0; i < elem.size(); i++)
      {
         data.add(toObject(elem.get(i)));
      }

      return data;
   }

   /**
    * @param elem
    * @return
    */
   private static Object toObject(JsonElement elem)
   {
      if(elem.isJsonArray())
      {
         return toObject((JsonArray)elem);
      }
      else if(elem.isJsonObject())
      {
         return toObject((JsonObject)elem);
      }
      else if(elem.isJsonPrimitive())
      {
         return toObject((JsonPrimitive)elem);
      }
      else
      {
         return null;
      }
   }

   /**
    * @param elem
    * @return
    */
   public static Object toObject(JsonPrimitive elem)
   {
      if (elem.isNumber())
      {
         return elem.getAsNumber();
      }
      else if (elem.isString())
      {
         return elem.getAsString();
      }
      else if (elem.isBoolean())
      {
         return elem.getAsBoolean();
      }
      else
      {
         return null;
      }
   }
}
