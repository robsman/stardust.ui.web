package org.eclipse.stardust.ui.web.modeler.utils.test;

import static com.google.common.collect.Lists.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.StringUtils;
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
      ChangeDescriptorJto changeJto = new ChangeDescriptorJto();
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
      cmdJto.changeDescriptions.add(changeJto);

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

   public static class CommandJto
   {
      public String commandId;
      public String modelId;

      public List<ChangeDescriptorJto> changeDescriptions = newArrayList();
   }

   public static class ChangeDescriptorJto
   {
      public String uuid;
      public String oid;
      public JsonObject changes = new JsonObject();
   }
}
