package org.eclipse.stardust.ui.web.modeler.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ModelElementJto
{
   // needs to be a wrapper as upon creation the OID is still undefined
   public Long oid;

   public String uuid;
   public String id;
   public String name;
   public String description;

   public String modelId;

   public String type;

   public JsonObject attributes = new JsonObject();

   public JsonArray comments = new JsonArray();
}