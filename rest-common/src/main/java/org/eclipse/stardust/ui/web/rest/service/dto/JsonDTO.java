/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Anoop.Nair (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.rest.JsonMarshaller;

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
      else
      {
         throw new RuntimeException("Not supported");
      }
   }
}
