package org.eclipse.stardust.ui.web.modeler.model.conversion.jto;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class DILaneJto
{
   public JsonPrimitive x;
   public JsonPrimitive y;
   public JsonPrimitive width;
   public JsonPrimitive height;

   public JsonPrimitive orientation;

   public JsonArray dataSymbols;
   public JsonArray activitySymbols;
   public JsonArray gatewaySymbols;
   public JsonArray eventSymbols;
}