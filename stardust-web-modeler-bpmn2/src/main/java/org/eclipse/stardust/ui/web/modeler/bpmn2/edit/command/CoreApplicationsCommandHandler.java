package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Interface;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2CoreElementsBuilder;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ApplicationJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@CommandHandler
public class CoreApplicationsCommandHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "webServiceApplication.create")
   public void createWebServiceApplication(Definitions model, JsonObject details)
   {
      ApplicationJto jto = jsonIo.gson().fromJson(details, ApplicationJto.class);

      jto.applicationType = ModelerConstants.WEB_SERVICE_APPLICATION_TYPE_ID;

      Bpmn2CoreElementsBuilder coreElementsBuilder = new Bpmn2CoreElementsBuilder();

      Interface webServiceApp = coreElementsBuilder.createApplicationDefinition(model, jto);
      coreElementsBuilder.attachToModel(model, webServiceApp);
   }

   @OnCommand(commandId = "messageTransformationApplication.create")
   public void createMessageTransformationApplication(Definitions model,
         JsonObject details)
   {

   }

   @OnCommand(commandId = "camelApplication.create")
   public void createCamelRouteApplication(Definitions model, JsonObject details)
   {

   }

   @OnCommand(commandId = "uiMashupApplication.create")
   public void createUiMashupApplication(Definitions model, JsonObject details)
   {

   }

   @OnCommand(commandId = "application.delete")
   public void deleteApplication(Definitions model, JsonObject request)
   {
   }
}
