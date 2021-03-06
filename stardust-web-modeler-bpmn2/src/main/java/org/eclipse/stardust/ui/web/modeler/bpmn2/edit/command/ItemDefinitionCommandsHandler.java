package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import static org.eclipse.stardust.common.StringUtils.isEmpty;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2ItemDefinitionBuilder;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.integration.ExternalXmlSchemaManager;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.TypeDeclarationJto;

import com.google.gson.JsonObject;

@CommandHandler
public class ItemDefinitionCommandsHandler
{
   private static final Logger trace = LogManager.getLogger(ItemDefinitionCommandsHandler.class);

   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ExternalXmlSchemaManager externalXmlSchemaManager;

   @OnCommand(commandId = "structuredDataType.create")
   public void createStructuredDataType(Definitions model, JsonObject details) {
      // create type declaration
      TypeDeclarationJto jto = jsonIo.gson().fromJson(details, TypeDeclarationJto.class);

      if (isEmpty(jto.id))
      {
         // must keep the original name as ID as otherwise the type can't be resolved from the schema
         jto.id = jto.name;
      }

      // create embedded schema type
      Bpmn2ItemDefinitionBuilder itemDefinitionBuilder = new Bpmn2ItemDefinitionBuilder(
            externalXmlSchemaManager);
      ItemDefinition xsdReference = itemDefinitionBuilder.createEmbeddedXsd(model, jto);
      itemDefinitionBuilder.attachItemDefinition(model, xsdReference);
   }

   @OnCommand(commandId = "typeDeclaration.create")
   public void createTypeDeclaration(Definitions model, JsonObject details)
   {
      // create type declaration
      TypeDeclarationJto jto = jsonIo.gson().fromJson(details, TypeDeclarationJto.class);

      if (isEmpty(jto.id))
      {
         // must keep the original name as ID as otherwise the type can't be resolved from the schema
         jto.id = jto.name;
      }

      if ("ExternalReference".equals(jto.typeDeclaration.type.classifier))
      {
         // import schema type by reference
         Bpmn2ItemDefinitionBuilder itemDefinitionBuilder = new Bpmn2ItemDefinitionBuilder(
               externalXmlSchemaManager);
         ItemDefinition xsdReference = itemDefinitionBuilder.createXsdReference(model, jto);
         itemDefinitionBuilder.attachItemDefinition(model, xsdReference);
      }
      else if ("SchemaType".equals(jto.typeDeclaration.type.classifier))
      {
         // create embedded schema type
         Bpmn2ItemDefinitionBuilder itemDefinitionBuilder = new Bpmn2ItemDefinitionBuilder(
               externalXmlSchemaManager);
         ItemDefinition xsdReference = itemDefinitionBuilder.createEmbeddedXsd(model, jto);
         itemDefinitionBuilder.attachItemDefinition(model, xsdReference);
      }
      else
      {
         throw new IllegalArgumentException("Unsupported classifier: " + jto.typeDeclaration.type.classifier);
      }
   }
}
