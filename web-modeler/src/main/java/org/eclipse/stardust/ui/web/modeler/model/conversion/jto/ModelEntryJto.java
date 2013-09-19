package org.eclipse.stardust.ui.web.modeler.model.conversion.jto;

import com.google.gson.JsonObject;

public class ModelEntryJto
{
   public String type;
   public String name;

   public JsonObject typeDeclarations;
   public JsonObject participants;
   public JsonObject dataItems;
   public JsonObject applications;
   public JsonObject processes;
}