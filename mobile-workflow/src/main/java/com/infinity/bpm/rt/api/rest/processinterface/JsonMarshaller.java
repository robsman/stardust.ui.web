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

package com.infinity.bpm.rt.api.rest.processinterface;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * 
 * @author Ellie.Sepehri
 *
 */
@Component
@Scope("singleton")
public class JsonMarshaller
{
   private static final Logger trace = LogManager.getLogger(JsonMarshaller.class);

   private final Gson gson = new Gson();

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
}
