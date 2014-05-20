/*******************************************************************************
* Copyright (c) 2014 SunGard CSA LLC and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*    Holger.Prause (SunGard CSA LLC) - initial API and implementation and/or initial documentation
*******************************************************************************/
package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.HashMap;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;

public class SeriesDataBuilder
{
   private HashMap<String, Series> seriesMapping;
   public SeriesDataBuilder()
   {
      this.seriesMapping = new HashMap<String, Series>();
   }

   public void add(String criteria, ValuesArray values)
   {
      Series series = seriesMapping.get(criteria);
      if (series == null)
      {
         series = new Series();
         seriesMapping.put(criteria, series);
      }

      series.add(values);
   }

   private ValuesArray getSeriesFillValue(ValuesArray template)
   {
      ValuesArray filler = new ValuesArray();
      Integer dimensionIndex = template.getDimensionIndex();
      List<Object> sourceValues = template.getValues();
      for(int i=0; i<sourceValues.size(); i++)
      {
         if(i != dimensionIndex)
         {
            filler.addValue(0);
         }
         else
         {
            filler.addValue(sourceValues.get(i));
         }
      }

      filler.setDimensionIndex(dimensionIndex);
      return filler;
   }

   private void synchronizeSeriesValue(Series from, Series to)
   {
      for(ValuesArray fromVa: from.getValues())
      {
         //if one series does not contain a value from another series(it will decided on the dimension value key)
         //add a filler for it
         if(!to.hasValue(fromVa))
         {
            ValuesArray filler = getSeriesFillValue(fromVa);
            to.add(filler);
         }
      }
   }

   private void synchronizeSeriesValues()
   {
      for (String seriesKey : seriesMapping.keySet())
      {
         Series target = seriesMapping.get(seriesKey);
         for(Series source: seriesMapping.values())
         {
            if(source != target)
            {
               synchronizeSeriesValue(source, target);
            }
         }
      }
   }

   public JsonObject getResult()
   {
      //requirement from ui - all series must have the same length and dimension values
      synchronizeSeriesValues();

      JsonObject result = new JsonObject();
      for(String key: seriesMapping.keySet())
      {
         Series series = seriesMapping.get(key);

         JsonArray seriesArrayJson = new JsonArray();
         for(ValuesArray valuesArray: series.getValues())
         {
            JsonArray seriesValuesArrayJson = new JsonArray();
            seriesArrayJson.add(seriesValuesArrayJson);
            for(Object value: valuesArray.getValues())
            {
               seriesValuesArrayJson.add(JsonUtil.convertJavaToPrimitive(value));
            }
         }

         result.add(key, seriesArrayJson);
      }

      return result;
   }
}
