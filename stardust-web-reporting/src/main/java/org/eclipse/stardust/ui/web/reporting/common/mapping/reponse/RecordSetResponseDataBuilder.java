/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.core.orm.DataField;

public class RecordSetResponseDataBuilder extends ReponseDataBuilder
{

   private JsonObject response;

   private JsonArray rows;

   private JsonArray currentRow;

   public RecordSetResponseDataBuilder(List<DataField> metaFields)
   {
      super(metaFields);

      response = new JsonObject();
      JsonArray columns = new JsonArray();
      for (DataField df : metaFields)
      {
         String name = df.getName();
         JsonPrimitive namePrimitive = JsonUtil.convertJavaToPrimitive(name);
         columns.add(namePrimitive);
      }
      response.add("columns", columns);

      rows = new JsonArray();
      response.add("rows", rows);
   }

   @Override
   public void addValue(DataField field, Object value)
   {
      if (currentRow == null)
      {
         next();
      }

      JsonPrimitive columnValue = JsonUtil.convertJavaToPrimitive(value);
      currentRow.add(columnValue);
   }

   @Override
   public void next()
   {
      if (currentRow != null)
      {
         rows.add(currentRow);
      }

      currentRow = new JsonArray();
   }

   @Override
   public JsonObject getResult()
   {
      return response;
   }
}
