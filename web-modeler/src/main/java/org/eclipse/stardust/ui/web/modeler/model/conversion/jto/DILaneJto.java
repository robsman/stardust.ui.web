package org.eclipse.stardust.ui.web.modeler.model.conversion.jto;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class DILaneJto
{
   public JsonPrimitive x;
   public JsonPrimitive y;
   public JsonPrimitive width;
   public JsonPrimitive height;

   public JsonPrimitive orientation;

   public JsonObject dataSymbols;
   public JsonObject activitySymbols;
   public JsonObject gatewaySymbols;
   public JsonObject eventSymbols;
}