package org.eclipse.stardust.ui.web.modeler.model.conversion.jto;

import com.google.gson.JsonArray;

public class ProcessEntryJto
{
   public String type;
   public String id;
   public String name;

   public String modelId;

   public JsonArray activities;
   public JsonArray gateways;
   public JsonArray events;
}