package org.eclipse.stardust.ui.web.modeler.model.conversion.jto;

import com.google.gson.JsonObject;

public class ProcessEntryJto
{
   public String type;
   public String id;
   public String name;

   public String modelId;

   public JsonObject activities;
   public JsonObject gateways;
   public JsonObject events;
}