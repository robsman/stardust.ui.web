package org.eclipse.stardust.ui.web.modeler.model.conversion;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController.ChangeJto;

public class BeanInvocationExecutor extends RequestExecutor
{
   private final JsonMarshaller jsonIo;

   private final ModelService modelService;

   private final ModelerSessionController modelerSessionController;

   @Autowired
   public BeanInvocationExecutor(JsonMarshaller jsonIo, ModelService modelService,
         ModelerSessionController modelerSessionController)
   {
      this.jsonIo = jsonIo;
      this.modelService = modelService;
      this.modelerSessionController = modelerSessionController;
   }

   @Override
   public JsonObject loadAllModels()
   {
      String allModelsJson = modelService.getAllModels(false);

      return jsonIo.readJsonObject(allModelsJson);
   }

   @Override
   public JsonObject loadProcessDiagram(String modelId, String processId)
   {
      String processDiagramJson = modelService.loadProcessDiagram(modelId, processId);

      return jsonIo.readJsonObject(processDiagramJson);
   }

   @Override
   public JsonObject applyChange(JsonObject cmdJson)
   {
      ChangeJto response = modelerSessionController.applyChange(jsonIo.gson()
            .fromJson(cmdJson, CommandJto.class));

      return jsonIo.gson().toJsonTree(response).getAsJsonObject();
   }

}
