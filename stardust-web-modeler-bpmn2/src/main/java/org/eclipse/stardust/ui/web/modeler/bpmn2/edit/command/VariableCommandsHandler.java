package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.bpmn2.DataStore;
import org.eclipse.bpmn2.Definitions;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.core.pojo.data.Type;
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

   @OnCommand(commandId = "primitiveData.create")
   public void createPrimitiveData(Definitions model, JsonObject request)
   {
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);
      String id = request.has(ModelerConstants.ID_PROPERTY) //
            ? extractString(request, ModelerConstants.ID_PROPERTY)
            : Bpmn2Utils.createInternalId();

      DataJto jto = new DataJto();
      jto.id = id;
      jto.name = name;
      jto.dataType = ModelerConstants.PRIMITIVE_DATA_TYPE_KEY;
      jto.primitiveDataType = extractString(request, ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY);
      if (isEmpty(jto.primitiveDataType))
      {
         jto.primitiveDataType = Type.String.getId();
      }

      Bpmn2VariableBuilder variableBuilder = new Bpmn2VariableBuilder();
      DataStore globalVariable = variableBuilder.createGlobalPrimitiveVariable(model, jto);
      variableBuilder.attachVariable(model, globalVariable);
   }

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
      if ( !isEmpty(typeName) && ( -1 != typeName.indexOf(":")))
      {
         typeName = typeName.substring(typeName.indexOf(":") + 1);
      }
      jto.structuredDataTypeFullId = typeName;

      Bpmn2VariableBuilder variableBuilder = new Bpmn2VariableBuilder();
      DataStore xsdVariable = variableBuilder.createGlobalXsdVariable(model, jto);
      variableBuilder.attachVariable(model, xsdVariable);
   }

}
