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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.stardust.ui.web.common.util.GsonUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Subodh.Godbole
 * @version $Revision: $
 */
public class ActivityInstanceOutDataDTO
{
   public Long oid;
   public Map<String, Serializable> outData;

   /**
    * @param json
    * @return
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   public static List<ActivityInstanceOutDataDTO> toList(String json)
   {
      List<ActivityInstanceOutDataDTO> list = new ArrayList<ActivityInstanceOutDataDTO>();

      JsonArray jArray = JsonDTO.getJsonArray(json);
      for (JsonElement jElem : jArray)
      {
         JsonObject jObject = jElem.getAsJsonObject();
         
         ActivityInstanceOutDataDTO dto = new ActivityInstanceOutDataDTO();
         dto.oid = jObject.get("oid").getAsLong();
         dto.outData =  extractOutData(jObject, dto);
         list.add(dto);
      }
      
      return list;
   }
   
   /**
    * 
    * @param jObject
    * @param dto
    * @return
    */
   @SuppressWarnings({"unchecked", "rawtypes"})
   private static Map<String, Serializable> extractOutData(JsonObject jObject, ActivityInstanceOutDataDTO dto)
   {
      Map<String, Serializable> outData = (Map)GsonUtils.extractMap(jObject, "outData");
      Map<String, String> dataMapping = (Map)GsonUtils.extractMap(jObject, "dataMappings");
      
      for (Entry<String, Serializable> data : outData.entrySet())
      {  
           String id = data.getKey();
           if(Double.class.getCanonicalName().equals(dataMapping.get(id))){
              outData.put(id, Double.valueOf(data.getValue().toString()));
           }
      }
      
      return outData;
   }
   
}
