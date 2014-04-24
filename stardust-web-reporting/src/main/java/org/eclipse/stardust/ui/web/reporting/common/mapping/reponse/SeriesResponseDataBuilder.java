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
import org.eclipse.stardust.ui.web.reporting.core.orm.DataField;

public class SeriesResponseDataBuilder extends ReponseDataBuilder
{
   private HashMap<DataField, JsonArray> seriesMapping = new HashMap<DataField, JsonArray>();
   private List<DataField> metaFields;

   public SeriesResponseDataBuilder(List<DataField> metaFields)
   {
      super(metaFields);
      this.metaFields = metaFields;
      for(DataField df: metaFields)
      {
         seriesMapping.put(df, new JsonArray());
      }
   }

   @Override
   public void next()
   {

   }

   @Override
   public void addValue(DataField field, Object value)
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
      for(DataField df: metaFields)
      {
         seriesSummary.add(new JsonPrimitive(df.getName()));
         result.add(df.getName(), seriesMapping.get(df));
      }

      return result;
   }

}
