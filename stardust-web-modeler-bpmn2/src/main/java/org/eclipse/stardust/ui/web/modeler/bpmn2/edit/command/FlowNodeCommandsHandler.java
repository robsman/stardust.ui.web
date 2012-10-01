package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ActivitySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.EventSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.GatewaySymbolJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

@CommandHandler
public class FlowNodeCommandsHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "eventSymbol.create")
   public void onCreateEventSymbol(Definitions model, EObject context, JsonObject details)
   {
      // create process definition
      EventJto jto = jsonIo.gson().fromJson(
            details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
            EventJto.class);

      ModelBinding<Definitions> modelBinding = modelService.currentSession().modelRepository().getModelBinding(model);

      // TODO find process
      Process process = null;
      if ((context instanceof BPMNShape) && ((BPMNShape) context).getBpmnElement() instanceof Lane)
      {
         process = Bpmn2Utils.findContainingProcess(((BPMNShape) context).getBpmnElement());
      }

      Event event = (Event) modelBinding.createModelElement(model, jto);
      modelBinding.attachModelElement(process, event);
      // TODO modelBinding.updateModelElement(event, details);

      // create event symbol
      EventSymbolJto symbolJto = jsonIo.gson().fromJson(details, EventSymbolJto.class);

      BPMNShape symbol = (BPMNShape) modelBinding.createNodeSymbol(model, symbolJto, event);
      modelBinding.attachNodeSymbol(context, symbol);
   }

   @OnCommand(commandId = "activitySymbol.create")
   public void onCreateActivitySymbol(Definitions model, EObject context, JsonObject details)
   {
      // create process definition
      ActivityJto jto = jsonIo.gson().fromJson(
            details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
            ActivityJto.class);

      ModelBinding<Definitions> modelBinding = modelService.currentSession().modelRepository().getModelBinding(model);

      // TODO find process
      Process process = null;
      if ((context instanceof BPMNShape) && ((BPMNShape) context).getBpmnElement() instanceof Lane)
      {
         process = Bpmn2Utils.findContainingProcess(((BPMNShape) context).getBpmnElement());
      }

      Activity event = (Activity) modelBinding.createModelElement(model, jto);
      modelBinding.attachModelElement(process, event);
      // TODO modelBinding.updateModelElement(event, details);

      // create event symbol
      ActivitySymbolJto symbolJto = jsonIo.gson().fromJson(details, ActivitySymbolJto.class);

      BPMNShape symbol = (BPMNShape) modelBinding.createNodeSymbol(model, symbolJto, event);
      modelBinding.attachNodeSymbol(context, symbol);
   }

   @OnCommand(commandId = "gateSymbol.create")
   public void onCreateGatewaySymbol(Definitions model, EObject context, JsonObject details)
   {
      // create process definition
      GatewayJto jto = jsonIo.gson().fromJson(
            details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
            GatewayJto.class);

      ModelBinding<Definitions> modelBinding = modelService.currentSession().modelRepository().getModelBinding(model);

      // TODO find process
      Process process = null;
      if ((context instanceof BPMNShape) && ((BPMNShape) context).getBpmnElement() instanceof Lane)
      {
         process = Bpmn2Utils.findContainingProcess(((BPMNShape) context).getBpmnElement());
      }

      Gateway event = (Gateway) modelBinding.createModelElement(model, jto);
      modelBinding.attachModelElement(process, event);
      // TODO modelBinding.updateModelElement(event, details);

      // create event symbol
      GatewaySymbolJto symbolJto = jsonIo.gson().fromJson(details, GatewaySymbolJto.class);

      BPMNShape symbol = (BPMNShape) modelBinding.createNodeSymbol(model, symbolJto, event);
      modelBinding.attachNodeSymbol(context, symbol);
   }
}
