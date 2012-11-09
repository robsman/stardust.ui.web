package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import javax.annotation.Resource;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2DiBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2FlowNodeBuilder;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ActivitySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.DataSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.EventSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.GatewaySymbolJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

@CommandHandler
public class FlowNodeCommandsHandler
{
   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "dataSymbol.create")
   public void onCreateDataSymbol(Definitions model, EObject context, JsonObject details)
   {
      // create process definition
      String dataId;
      if (details.has(ModelerConstants.DATA_FULL_ID_PROPERTY))
      {
         dataId = GsonUtils.extractString(details, ModelerConstants.DATA_FULL_ID_PROPERTY);
         if (-1 != dataId.indexOf(":"))
         {
            dataId = dataId.substring(dataId.indexOf(":") + 1);
         }
      }
      else
      {
         DataJto jto = jsonIo.gson().fromJson(
               details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
               DataJto.class);
         dataId = jto.id;
      }

      // TODO find process
      Process process = null;
      if ((context instanceof BPMNShape) && ((BPMNShape) context).getBpmnElement() instanceof Lane)
      {
         process = Bpmn2Utils.findContainingProcess(((BPMNShape) context).getBpmnElement());
      }

      DataObject variable = null;
      for (FlowElement flowElement : process.getFlowElements())
      {
         if ((flowElement instanceof DataObject) && dataId.equals(((DataObject) flowElement).getId()))
         {
            variable = (DataObject) flowElement;
            break;
         }
      }
      if (null == variable)
      {
         // TODO create on the fly
      }

      // create event symbol
      DataSymbolJto symbolJto = jsonIo.gson().fromJson(details, DataSymbolJto.class);

      Bpmn2DiBuilder diBuilder = new Bpmn2DiBuilder();
      BPMNShape symbol = diBuilder.createNodeSymbol(model, symbolJto, variable);
      diBuilder.attachDiagramElement(context, symbol);
   }

   @OnCommand(commandId = "eventSymbol.create")
   public void onCreateEventSymbol(Definitions model, EObject context, JsonObject details)
   {
      // create process definition
      EventJto jto = jsonIo.gson().fromJson(
            details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
            EventJto.class);

      // TODO find process
      Process process = null;
      if ((context instanceof BPMNShape) && ((BPMNShape) context).getBpmnElement() instanceof Lane)
      {
         process = Bpmn2Utils.findContainingProcess(((BPMNShape) context).getBpmnElement());
      }

      Bpmn2FlowNodeBuilder flowNodeBuilder = new Bpmn2FlowNodeBuilder();
      Event event = flowNodeBuilder.createEvent(model, jto);
      flowNodeBuilder.attachFlowNode(process, event);
      // TODO modelBinding.updateModelElement(event, details);

      // create event symbol
      EventSymbolJto symbolJto = jsonIo.gson().fromJson(details, EventSymbolJto.class);

      Bpmn2DiBuilder diBuilder = new Bpmn2DiBuilder();
      BPMNShape symbol = diBuilder.createNodeSymbol(model, symbolJto, event);
      diBuilder.attachDiagramElement(context, symbol);
   }

   @OnCommand(commandId = "activitySymbol.create")
   public void onCreateActivitySymbol(Definitions model, EObject context, JsonObject details)
   {
      // create process definition
      ActivityJto jto = jsonIo.gson().fromJson(
            details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
            ActivityJto.class);

      // TODO find process
      Process process = null;
      if ((context instanceof BPMNShape) && ((BPMNShape) context).getBpmnElement() instanceof Lane)
      {
         process = Bpmn2Utils.findContainingProcess(((BPMNShape) context).getBpmnElement());
      }

      Bpmn2FlowNodeBuilder flowNodeBuilder = new Bpmn2FlowNodeBuilder();
      Activity event = flowNodeBuilder.createActivity(model, jto);
      flowNodeBuilder.attachFlowNode(process, event);
      // TODO modelBinding.updateModelElement(event, details);

      // create event symbol
      ActivitySymbolJto symbolJto = jsonIo.gson().fromJson(details, ActivitySymbolJto.class);

      Bpmn2DiBuilder diBuilder = new Bpmn2DiBuilder();
      BPMNShape symbol = diBuilder.createNodeSymbol(model, symbolJto, event);
      diBuilder.attachDiagramElement(context, symbol);
   }

   @OnCommand(commandId = "gateSymbol.create")
   public void onCreateGatewaySymbol(Definitions model, EObject context, JsonObject details)
   {
      // create process definition
      GatewayJto jto = jsonIo.gson().fromJson(
            details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY),
            GatewayJto.class);

      // TODO find process
      Process process = null;
      if ((context instanceof BPMNShape) && ((BPMNShape) context).getBpmnElement() instanceof Lane)
      {
         process = Bpmn2Utils.findContainingProcess(((BPMNShape) context).getBpmnElement());
      }

      Bpmn2FlowNodeBuilder flowNodeBuilder = new Bpmn2FlowNodeBuilder();
      Gateway event = flowNodeBuilder.createGateway(model, jto);
      flowNodeBuilder.attachFlowNode(process, event);
      // TODO modelBinding.updateModelElement(event, details);

      // create event symbol
      GatewaySymbolJto symbolJto = jsonIo.gson().fromJson(details, GatewaySymbolJto.class);

      Bpmn2DiBuilder diBuilder = new Bpmn2DiBuilder();
      BPMNShape symbol = diBuilder.createNodeSymbol(model, symbolJto, event);
      diBuilder.attachDiagramElement(context, symbol);
   }
}
