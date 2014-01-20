package org.eclipse.stardust.ui.web.modeler.model.conversion;

import com.google.gson.JsonObject;

public abstract class RequestExecutor
{
   public abstract JsonObject loadAllModels();

   public abstract JsonObject loadProcessDiagram(String modelId, String processId);

   public abstract JsonObject applyChange(JsonObject cmdJson);
}