/*******************************************************************************
 * Copyright (c) 2013 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.business_object_management.rest;

import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import com.google.gson.*;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

// TODO Reuse already available marshaller; this is copy and paste

public class JsonMarshaller
{
   private static final Logger trace = LogManager.getLogger(JsonMarshaller.class);

   private final Gson gson = new GsonBuilder() //
      .registerTypeAdapter(JsonObject.class, new JsonObjectSerializationHandler())
      .registerTypeAdapter(JsonArray.class, new JsonArraySerializationHandler())
      .create();

   private final Gson gsonForUpdates = new GsonBuilder().serializeNulls()
         .registerTypeAdapter(JsonObject.class, new JsonObjectSerializationHandler())
         .registerTypeAdapter(JsonArray.class, new JsonArraySerializationHandler())
         .create();

   private final JsonParser jsonParser = new JsonParser();

   public Gson gson()
   {
      return gson;
   }

   public JsonObject readJsonObject(String jsonText)
         throws javax.ws.rs.WebApplicationException
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
            throw new WebApplicationException(Status.BAD_REQUEST);
         }
      }
      catch (JsonParseException jpe)
      {
         trace.warn("Expected a JSON object, but received no valid JSON at all.", jpe);
         throw new WebApplicationException(jpe, Status.BAD_REQUEST);
      }
   }

   public String writeJsonObject(JsonObject json)
   {
      return gson.toJson(json);
   }

   public void writeIntoJsonObject(JsonObject update, JsonObject master)
   {
      // merge
      mergeUpdate(update, master);
   }

   public <T> void writeIntoJsonObject(T update, JsonObject master)
   {
      // merge
      mergeUpdate(gsonForUpdates.toJsonTree(update).getAsJsonObject(), master);
   }

   private void mergeUpdate(JsonObject update, JsonObject master)
   {
      for (Map.Entry<String, JsonElement> attr : update.entrySet())
      {
         if ((null == attr.getValue()) || attr.getValue().isJsonNull())
         {
            // TODO use singleton to reduce memory footprint
            master.add(attr.getKey(), new JsonNull());
         }

         assertCompatibility(master.get(attr.getKey()), attr.getValue());
         if (attr.getValue() instanceof JsonPrimitive)
         {
            if (!master.has(attr.getKey()) || !master.get(attr.getKey()).equals(attr.getValue()))
            {
               master.add(attr.getKey(), attr.getValue());
            }
         }
         else if (attr.getValue() instanceof JsonObject)
         {
            if ( !master.has(attr.getKey()) || master.get(attr.getKey()).isJsonNull())
            {
               // copy whole subtree
               master.add(attr.getKey(), attr.getValue());
            }
            else
            {
               // recurse to merge attribute updates
               mergeUpdate(attr.getValue().getAsJsonObject(), master.get(attr.getKey()).getAsJsonObject());
            }
         }
         else if (attr.getValue() instanceof JsonArray)
         {
            // TODO any way to correlate elements between arrays? will need concept of
            // element identity to merge elements
            master.add(attr.getKey(), attr.getValue());
         }
      }
   }

   private void assertCompatibility(JsonElement master, JsonElement update)
   {
      if ((null == update) || update.isJsonNull())
      {
         // assume any attribute can be set to null
      }
      else if (null != update)
      {
         if ((null == master) || master.isJsonNull())
         {
            // assume compatibility as null carries no type
         }
         else if (update.isJsonPrimitive())
         {
            if ( !master.isJsonPrimitive())
            {
               throw new IllegalArgumentException(
                     "Must not structurally change JSON objects (expecting primitive, got " + master + ").");
            }
            // TODO further checks for check primitive types?
         }
         else if (update.isJsonArray())
         {
            if ( !master.isJsonArray())
            {
               throw new IllegalArgumentException(
                     "Must not structurally change JSON objects (expecting array, got " + master + ").");
            }
         }
         else if (update.isJsonObject())
         {
            if ( !master.isJsonObject())
            {
               throw new IllegalArgumentException(
                     "Must not structurally change JSON objects (expecting object, got " + master + ").");
            }
         }
      }
   }

   private static class JsonObjectSerializationHandler
         implements JsonSerializer<JsonObject>, JsonDeserializer<JsonObject>
   {
      @Override
      public JsonObject serialize(JsonObject src, Type typeOfSrc,
            JsonSerializationContext context)
      {
         // just use the object as is
         return src;
      }

      @Override
      public JsonObject deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException
      {
         if (json.isJsonObject() && JsonObject.class.equals(typeOfT))
         {
            // just use the object as is
            return json.getAsJsonObject();
         }
         else
         {
            return new JsonObject();
         }
      }
   }

   private static class JsonArraySerializationHandler
         implements JsonSerializer<JsonArray>, JsonDeserializer<JsonArray>
   {
      @Override
      public JsonArray serialize(JsonArray src, Type typeOfSrc,
            JsonSerializationContext context)
      {
         // just use the object as is
         return src;
      }

      @Override
      public JsonArray deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException
      {
         if (json.isJsonArray() && JsonArray.class.equals(typeOfT))
         {
            // just use the object as is
            return json.getAsJsonArray();
         }
         else
         {
            return new JsonArray();
         }
      }
   }
}
