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
      if(series == null)
      {
         series = new Series();
         seriesMapping.put(criteria, series);
      }

      series.add(values);
   }

   public JsonObject getResult()
   {
      JsonObject result = new JsonObject();
      for(String key: seriesMapping.keySet())
      {
         Series series = seriesMapping.get(key);
         series.sort();

         JsonArray seriesValuesJson = new JsonArray();
         for(ValuesArray valuesArray: series.getValues())
         {
            for(Object value: valuesArray.getValues())
            {
               seriesValuesJson.add(JsonUtil.convertJavaToPrimitive(value));
            }
         }

         result.add(key, seriesValuesJson);
      }

      return result;
   }
}
