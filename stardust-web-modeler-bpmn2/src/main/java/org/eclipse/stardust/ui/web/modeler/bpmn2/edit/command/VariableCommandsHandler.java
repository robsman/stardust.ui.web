package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2VariableBuilder;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;

@CommandHandler
public class VariableCommandsHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @OnCommand(commandId = "structuredData.create")
   public void createStructuredData(Definitions model, JsonObject request)
   {
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);
      String id = request.has(ModelerConstants.ID_PROPERTY) //
            ? extractString(request, ModelerConstants.ID_PROPERTY)
            : Bpmn2Utils.createInternalId();

      DataJto jto = new DataJto();
      jto.id = id;
      jto.name = name;
      jto.dataType = ModelerConstants.STRUCTURED_DATA_TYPE_KEY;
      String typeName = extractString(request, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY);
      if (-1 != typeName.indexOf(":"))
      {
         typeName = typeName.substring(typeName.indexOf(":") + 1);
      }
      jto.structuredDataTypeFullId = typeName;

      Bpmn2VariableBuilder variableBuilder = new Bpmn2VariableBuilder();
      DataObject xsdVariable = variableBuilder.createXsdVariable(model, jto);
      for (RootElement rootElement : model.getRootElements())
      {
         if (rootElement instanceof Process)
         {
            variableBuilder.attachVariable((Process) rootElement, xsdVariable);
         }
      }
   }

}
