package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.command;

import javax.annotation.Resource;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.di.Diagram;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Binding;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.TransitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ConnectionSymbolJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

@CommandHandler
public class SequenceFlowCommandsHandler
{
   private static final Logger trace = LogManager.getLogger(SequenceFlowCommandsHandler.class);

   @Resource
   private JsonMarshaller jsonIo;

   @Resource
   private ModelService modelService;

   @OnCommand(commandId = "connection.create")
   public void onCreateConnectionSymbol(Definitions model, EObject context, JsonObject details)
   {
      ConnectionSymbolJto jto = jsonIo.gson().fromJson(details, ConnectionSymbolJto.class);

      Bpmn2Binding modelBinding = (Bpmn2Binding) modelService.currentSession().modelRepository().getModelBinding(model);

      if (ModelerConstants.CONTROL_FLOW_LITERAL.equals(jto.modelElement.type))
      {
         TransitionJto transitionJto = jsonIo.gson().fromJson(details.getAsJsonObject(ModelerConstants.MODEL_ELEMENT_PROPERTY), TransitionJto.class);
         SequenceFlow flow = modelBinding.createSequenceFlow(model, transitionJto);

         flow.setSourceRef(resolveFlowNode(modelBinding, model, jto.fromModelElementOid));
         flow.setTargetRef(resolveFlowNode(modelBinding, model, jto.toModelElementOid));

         modelBinding.attachModelElement(context, flow);

         BPMNEdge flowSymbol = modelBinding.createConnectionSymbol(model, jto, flow);
         flowSymbol.setSourceElement(resolveFlowNodeSymbol(modelBinding, model, jto.fromModelElementOid));
         flowSymbol.setTargetElement(resolveFlowNodeSymbol(modelBinding, model, jto.toModelElementOid));

         Diagram diagram = null;
         if (null != flowSymbol.getSourceElement())
         {
            diagram = Bpmn2Utils.findContainingDiagram(flowSymbol.getSourceElement());
         }
         if ((null == diagram) && (null != flowSymbol.getTargetElement()))
         {
            diagram = Bpmn2Utils.findContainingDiagram(flowSymbol.getTargetElement());
         }

         modelBinding.attachDiagramElement(diagram, flowSymbol);
      }
   }

   private FlowNode resolveFlowNode(ModelBinding<Definitions> modelBinding, Definitions model, Long oid)
   {
      EObject element = (null != oid) ? modelBinding.getNavigator().findElementByOid(model, oid) : null;
      if (element instanceof BPMNShape)
      {
         BaseElement modelElement = ((BPMNShape) element).getBpmnElement();
         if (modelElement instanceof FlowNode)
         {
            return (FlowNode) modelElement;
         }
      }
      else if (element instanceof FlowNode)
      {
         return (FlowNode) element;
      }

      return null;
   }

   private BPMNShape resolveFlowNodeSymbol(ModelBinding<Definitions> modelBinding, Definitions model, Long oid)
   {
      EObject element = (null != oid) ? modelBinding.getNavigator().findElementByOid(model, oid) : null;
      if (element instanceof BPMNShape)
      {
         return (BPMNShape) element;
      }
      else if (element instanceof FlowNode)
      {
         // TODO resolve symbol from element
      }

      return null;
   }

   private static class ConnectionCreateJto
   {
      public String fromModelElementType;
      public Long fromModelElementOid;
      public String fromAnchorPointOrientation;

      public String toModelElementType;
      public Long toModelElementOid;
      public String toAnchorPointOrientation;

      public JsonObject modelElement;
   }
}
