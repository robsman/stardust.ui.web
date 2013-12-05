package org.eclipse.stardust.ui.web.modeler.bpmn2.utils;

import com.google.gson.JsonObject;

public class JsonExtensionPropertyUtils
{

   public static void syncExtProperty(String propertyName, JsonObject json, JsonObject extJson)
   {
      if (json.has(propertyName))
      {
         if (json.get(propertyName).isJsonNull())
         {
            extJson.remove(propertyName);
         }
         else
         {
            extJson.add(propertyName, json.get(propertyName));
         }
      }
   }

}
