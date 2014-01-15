package org.eclipse.stardust.ui.web.modeler.model.conversion;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.rest.ModelerSessionRestController;

public class BeanInvocationExecutor extends RequestExecutor
{
   private final JsonMarshaller jsonIo;

   private final ModelService modelService;

   private final ModelerSessionRestController modelerSessionRestController;

   @Autowired
   public BeanInvocationExecutor(JsonMarshaller jsonIo, ModelService modelService,
         ModelerSessionRestController modelerSessionRestController)
   {
      this.jsonIo = jsonIo;
      this.modelService = modelService;
      this.modelerSessionRestController = modelerSessionRestController;
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
      Response response = modelerSessionRestController.applyChange(jsonIo.gson()
            .fromJson(cmdJson, CommandJto.class));

      if (Status.CREATED.getStatusCode() == response.getStatus()
            || Status.OK.getStatusCode() == response.getStatus())
      {
         JsonObject responseJson = jsonIo.readJsonObject((String) response.getEntity());

         return responseJson;
      }
      else
      {
         throw new WebApplicationException(response);
      }
   }

}
