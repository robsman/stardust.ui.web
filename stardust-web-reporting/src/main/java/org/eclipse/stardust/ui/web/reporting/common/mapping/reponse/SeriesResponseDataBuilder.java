/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public class SeriesResponseDataBuilder extends ReponseDataBuilder
{
   private HashMap<RequestColumn, JsonArray> seriesMapping = new HashMap<RequestColumn, JsonArray>();
   private List<RequestColumn> requestColumns;

   public SeriesResponseDataBuilder(List<RequestColumn> requestColumns)
   {
      this.requestColumns = requestColumns;
      for(RequestColumn rc: requestColumns)
      {
         seriesMapping.put(rc, new JsonArray());
      }
   }

   @Override
   public void next()
   {

   }

   @Override
   public void addValue(RequestColumn field, Object value)
   {
      JsonArray seriesValues = seriesMapping.get(field);
      seriesValues.add(JsonUtil.convertJavaToPrimitive(value));
   }

   @Override
   public JsonObject getResult()
   {
      JsonObject result = new JsonObject();
      JsonArray seriesSummary = new JsonArray();
      result.add("seriesIds", seriesSummary);
      //for keeping the order - iterate the list instead of map
      for(RequestColumn rc: requestColumns)
      {
         seriesSummary.add(new JsonPrimitive(rc.getId()));
         result.add(rc.getId(), seriesMapping.get(rc));
      }

      return result;
   }

}
