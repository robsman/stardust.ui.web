package org.eclipse.stardust.ui.web.modeler.utils.test;

import static java.util.Collections.singletonList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.IOException;

import com.google.gson.JsonObject;

import org.eclipse.stardust.ui.web.modeler.edit.jto.ChangeDescriptionJto;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;

public class ChangeApiDriver
{
   private final RequestExecutor requestExecutor;
   private final JsonMarshaller jsonIo;

   public ChangeApiDriver(RequestExecutor requestExecutor, JsonMarshaller jsonIo)
   {
      this.requestExecutor = requestExecutor;
      this.jsonIo = jsonIo;
   }

   public JsonObject performChange(String commandId, String modelId,
         Object targetElementId, JsonObject changeJson) throws IOException
   {
      ChangeDescriptionJto changeJto = new ChangeDescriptionJto();
      if (targetElementId instanceof String)
      {
         if (!isEmpty(modelId) && modelId.equals(targetElementId))
         {
            changeJto.oid = (String) targetElementId;
         }
         else
         {
            changeJto.uuid = (String) targetElementId;
         }
      }
      else if (targetElementId instanceof Number)
      {
         changeJto.oid = Long.toString(((Number) targetElementId).longValue());
      }
      changeJto.changes = changeJson;

      CommandJto cmdJto = new CommandJto();
      cmdJto.commandId = commandId;
      cmdJto.modelId = modelId;
      cmdJto.changeDescriptions = singletonList(changeJto);

      return performChange(cmdJto);
   }

   public JsonObject performChange(CommandJto cmdJto)
   {
      return requestExecutor.applyChange((JsonObject) jsonIo.gson().toJsonTree(cmdJto));
   }

   public JsonObject updateModelElement(String modelId, JsonObject changeJson) throws IOException
   {
      return performChange("modelElement.update", modelId, modelId, changeJson);
   }

   public JsonObject updateModelElement(String modelId, long targetElementOid, JsonObject changeJson) throws IOException
   {
      return performChange("modelElement.update", modelId, targetElementOid, changeJson);
   }
}
