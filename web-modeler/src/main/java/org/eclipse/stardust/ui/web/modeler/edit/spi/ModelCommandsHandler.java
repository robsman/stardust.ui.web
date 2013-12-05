package org.eclipse.stardust.ui.web.modeler.edit.spi;

import java.util.UUID;

import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public interface ModelCommandsHandler
{
   boolean handlesModel(String formatId);

   ModificationDescriptor handleCommand(String commandId, EObject context,
         JsonObject request);

   static class ModificationDescriptor
   {
      private final String id = UUID.randomUUID().toString();

      public final JsonArray modified = new JsonArray();

      public final JsonArray added = new JsonArray();

      public final JsonArray removed = new JsonArray();

      private Exception failure;

      public String getId()
      {
         return id;
      }

      public boolean wasFailure()
      {
         return null != failure;
      }

      public Exception getFailure()
      {
         return failure;
      }

      public void setFailure(Exception failure)
      {
         this.failure = failure;
      }
   };
}
