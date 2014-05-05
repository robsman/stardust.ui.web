/*******************************************************************************
 * Copyright (c) 2011 - 2012 SunGard CSA LLC
 *******************************************************************************/

package org.eclipse.stardust.ui.web.reporting.common.mapping.reponse;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.eclipse.stardust.ui.web.reporting.common.JsonUtil;
import org.eclipse.stardust.ui.web.reporting.core.RequestColumn;

public class RecordSetDataBuilder
{

   private JsonObject response;

   private JsonArray rows;

   private JsonArray currentRow;

   public RecordSetDataBuilder(List<RequestColumn> metaFields)
   {
      response = new JsonObject();
      JsonArray columns = new JsonArray();
      for (RequestColumn rc : metaFields)
      {
         columns.add(new JsonPrimitive(rc.getId()));
      }
      response.add("columns", columns);

      rows = new JsonArray();
      response.add("rows", rows);
   }

   public void addValue(Object value)
   {
      if (currentRow == null)
      {
         nextRow();
      }

      JsonPrimitive columnValue = JsonUtil.convertJavaToPrimitive(value);
      currentRow.add(columnValue);
   }

   public void nextRow()
   {
      if (currentRow != null)
      {
         rows.add(currentRow);
      }

      currentRow = new JsonArray();
   }

   public JsonObject getResult()
   {
      return response;
   }
}
