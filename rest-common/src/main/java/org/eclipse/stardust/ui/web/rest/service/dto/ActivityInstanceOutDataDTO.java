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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.common.util.GsonUtils;

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
         dto.outData = (Map)GsonUtils.extractMap(jObject, "outData");

         list.add(dto);
      }
      
      return list;
   }
}
