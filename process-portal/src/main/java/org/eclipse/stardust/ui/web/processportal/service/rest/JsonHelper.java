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

package org.eclipse.stardust.ui.web.processportal.service.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.DateUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * @author Subodh.Godbole
 *
 */
public class JsonHelper
{
   private static final Logger trace = LogManager.getLogger(InteractionDataUtils.class);

   private String dateFormat = "yyyy-MM-dd";

   /**
    * 
    */
   public JsonHelper()
   {
   }

   /**
    * @param dateFormat
    */
   public JsonHelper(String dateFormat)
   {
      this.dateFormat = dateFormat;
   }

   /*
    * 
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void toJson(Map<String, ? extends Serializable> data, JsonObject parent)
   {
      for (Entry<String, ? extends Serializable> entry : data.entrySet())
      {
         if (null == entry.getValue())
         {
            continue;
         }
         
         if (entry.getValue() instanceof Map)
         {
            JsonObject json = new JsonObject();
            parent.add(entry.getKey(), json);
            toJson((Map)entry.getValue(), json);
         }
         else if (entry.getValue() instanceof List)
         {
            JsonArray json = new JsonArray();
            parent.add(entry.getKey(), json);
            toJson((List)entry.getValue(), json);
         }
         else // Primitive
         {
            JsonPrimitive primitive = toJson(entry.getValue());
            parent.add(entry.getKey(), primitive);
         }
      }
   }
   
   /*
    * 
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public void toJson(List<? extends Serializable> data, JsonArray parent)
   {
      for (Serializable value : data)
      {
         if (null == value)
         {
            continue;
         }

         if (value instanceof Map)
         {
            JsonObject json = new JsonObject();
            parent.add(json);
            toJson((Map)value, json);
         }
         else if (value instanceof List)
         {
            JsonArray json = new JsonArray();
            parent.add(json);
            toJson((List)value, json);
         }
         else // Primitive
         {
            JsonPrimitive primitive = toJson(value);
            if (null != primitive)
            {
               parent.add(primitive);
            }
         }
      }
   }

   /*
    * 
    */
   public JsonPrimitive toJson(Object value)
   {
      JsonPrimitive ret = null;

      try
      {
         if (value instanceof Float || value instanceof Double || value instanceof Number)
         {
            ret = new JsonPrimitive((Number)value);               
         }
         else if (value instanceof Boolean)
         {
            ret = new JsonPrimitive((Boolean)value);               
         }
         else if (value instanceof Character)
         {
            ret = new JsonPrimitive((Character)value);               
         }
         else if (value instanceof Date)
         {
            ret = new JsonPrimitive(DateUtils.format((Date)value, dateFormat, Locale.getDefault(), TimeZone.getDefault()));
         }
         else if (value instanceof Calendar)
         {
            ret = new JsonPrimitive(DateUtils.format(((Calendar)value).getTime(), dateFormat, Locale.getDefault(), TimeZone.getDefault()));
         }
         else if (value instanceof String)
         {
            ret = new JsonPrimitive((String)value);
         }
         else
         {
            ret = new JsonPrimitive(value.toString());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         trace.error("Something went wrong", e);
      }
      
      return ret;
   }

   /**
    * @param elem
    * @return
    */
   public Object toObject(JsonElement elem)
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
   public Map<String, Object> toObject(JsonObject elem)
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
   public List<Object> toObject(JsonArray elem)
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
   public Object toObject(JsonPrimitive elem)
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
