package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.getModelUuid;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.emf.ecore.EObject;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.util.SchemaLocatorAdapter;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2CoreElementsBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.edit.model.ModelConversionService;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ModelCommandsHandler;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

@CommandHandler
public class Bpmn2ModelCommandsHandler implements ModelCommandsHandler
{
   @Resource
   private ApplicationContext springContext;

   @Override
   public boolean handlesModel(String formatId)
   {
      return "bpmn2".equalsIgnoreCase(formatId);
   }

   @Override
   public ModificationDescriptor handleCommand(String commandId, EObject context, JsonObject request)
   {
      if ("model.create".equals(commandId))
      {
         return createModel(request);
      }
      else
      {
         Definitions model = (Definitions) context;

         if ("model.clone".equals(commandId))
         {
            return cloneModel(model, request);
         }
         else if ("model.update".equals(commandId))
         {
            // TODO other commands
         }
         else if ("model.delete".equals(commandId))
         {
            // TODO other commands
         }
      }
      return null;
   }

   private ModificationDescriptor createModel(JsonObject request)
   {
      // TODO Auto-generated method stub

      ModelJto modelJto = new ModelJto();
      modelJto.name = request.get(ModelerConstants.NAME_PROPERTY).getAsString();

      Definitions model = new Bpmn2CoreElementsBuilder().createModel(modelJto);

      String modelUuid = Bpmn2Utils.createInternalId();
      Bpmn2ExtensionUtils.setExtensionAttribute(model, "uuid", modelUuid);

      ModelService modelService = springContext.getBean(ModelService.class);
      modelService.getModelManagementStrategy().attachModel(modelUuid, model.getName(), model);
      modelService.getModelManagementStrategy().saveModel(modelService.findModel(modelUuid));

      // TODO review
      model.eResource().eAdapters().add(new SchemaLocatorAdapter());

      ModificationDescriptor changes = new ModificationDescriptor();
      ModelBinding<Definitions> modelBinding = modelService.currentSession()
            .modelRepository().getModelBinding(model);
      changes.added.add(modelBinding.getMarshaller().toModelJson(model));
      return changes;
   }

   private ModificationDescriptor cloneModel(Definitions model, JsonObject request)
   {
      ModelConversionService conversionService = springContext
            .getBean(ModelConversionService.class);

      String targetFormat = extractString(request, "targetFormat");
      if (isEmpty(targetFormat))
      {
         targetFormat = "xpdl";
      }
      EObject modelCopy = conversionService.convertModel(model, targetFormat);

      ModelService modelService = springContext.getBean(ModelService.class);
      ModificationDescriptor changes = new ModificationDescriptor();
      ModelBinding<EObject> modelBinding = modelService.currentSession()
            .modelRepository().getModelBinding(modelCopy);
      changes.added.add(modelBinding.getMarshaller().toModelJson(modelCopy));

      return changes;
   }

}
