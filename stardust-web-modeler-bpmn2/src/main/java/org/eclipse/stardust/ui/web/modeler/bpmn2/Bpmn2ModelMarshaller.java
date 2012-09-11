package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.CallableElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.di.Diagram;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.dd.di.Shape;
import org.eclipse.emf.ecore.EObject;

import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelParticipantJto;
import org.eclipse.stardust.ui.web.modeler.model.PrimitiveDataJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;
import org.eclipse.stardust.ui.web.modeler.model.TransitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ActivitySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ConnectionSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.EventSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.GatewaySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.LaneSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.PoolSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ShapeJto;

public class Bpmn2ModelMarshaller implements ModelMarshaller
{
   private Bpmn2Binding bpmn2Binding;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   void setBinding(Bpmn2Binding bpmn2Binding)
   {
      this.bpmn2Binding = bpmn2Binding;
   }

   @Override
   public JsonObject toModelJson(EObject model)
   {
      assert model instanceof Definitions;

      Definitions bpmn2Model = (Definitions) model;

      ModelJto modelJto = new ModelJto();
      modelJto.uuid = bpmn2Model.getId();
      modelJto.id = bpmn2Model.getId();
      modelJto.name = nameOrId(bpmn2Model.getName(), bpmn2Model.getId());

      // TODO processes etc.
      for (RootElement root : bpmn2Model.getRootElements())
      {
         if (root instanceof Participant)
         {
            modelJto.participants.add(toJto((Participant) root));
         }
         else if (root instanceof Process)
         {
            modelJto.processes.add(toJto((Process) root));

            // expose process properties as global data (see BPMN2 -> WS-BPEL mapping)
            for (Property property : ((Process) root).getProperties())
            {
               modelJto.dataItems.add(toJto((Process) root, property));
            }
         }
      }

      return jsonIo.gson().toJsonTree(modelJto).getAsJsonObject();
   }

   @Override
   public JsonObject toProcessDiagramJson(EObject model, String processId)
   {
      assert model instanceof Definitions;

      for (RootElement root : ((Definitions) model).getRootElements())
      {
         if ((root instanceof Process) && processId.equals(((Process) root).getId()))
         {
            Process process = (Process) root;

            for (Diagram diagram : ((Definitions) model).getDiagrams())
            {
               if ((diagram.getRootElement() instanceof BPMNPlane)
                     && (process == ((BPMNPlane) diagram.getRootElement()).getBpmnElement()))
               {
                  BPMNPlane plane = (BPMNPlane) diagram.getRootElement();

                  ProcessDiagramJto jto = new ProcessDiagramJto();
                  jto.uuid = plane.getId();
                  jto.id = plane.getId();
                  jto.name = nameOrId(diagram.getName(), plane.getId());

                  // TODO dynamically determine pool/lane dimensions
                  LaneSymbolJto laneJto = new LaneSymbolJto();
                  laneJto.x = 10;
                  laneJto.y = 10;
                  laneJto.width = 980;
                  laneJto.height = 580;
                  PoolSymbolJto poolJto = new PoolSymbolJto();
                  poolJto.x = laneJto.x - 10;
                  poolJto.x = laneJto.y - 10;
                  poolJto.width = laneJto.width + 20;
                  poolJto.height = laneJto.height + 20;


                  // required to properly connect connections to node symbols
                  Map<FlowNode, BPMNShape> nodeSymbolPerElement = newHashMap();
                  for (DiagramElement symbol : plane.getPlaneElement())
                  {
                     if (symbol instanceof BPMNShape)
                     {
                        BPMNShape shape = (BPMNShape) symbol;
                        if (shape.getBpmnElement() instanceof Activity)
                        {
                           nodeSymbolPerElement.put((Activity) shape.getBpmnElement(), shape);

                           ActivitySymbolJto symbolJto = newShapeJto(shape, new ActivitySymbolJto());

                           symbolJto.oid = bpmn2Binding.findOid((Definitions) model, shape);

                           symbolJto.modelElement = toJto((Activity) shape.getBpmnElement());

                           laneJto.activitySymbols.add(symbolJto);
                        }
                        else if (shape.getBpmnElement() instanceof Gateway)
                        {
                           nodeSymbolPerElement.put((Gateway) shape.getBpmnElement(), shape);

                           GatewaySymbolJto symbolJto = newShapeJto(shape, new GatewaySymbolJto());

                           symbolJto.oid = bpmn2Binding.findOid((Definitions) model, shape);

                           symbolJto.modelElement = toJto((Gateway) shape.getBpmnElement());

                           laneJto.gatewaySymbols.add(symbolJto);
                        }
                        else if ((shape.getBpmnElement() instanceof StartEvent) || (shape.getBpmnElement() instanceof EndEvent))
                        {
                           nodeSymbolPerElement.put((Event) shape.getBpmnElement(), shape);

                           EventSymbolJto symbolJto = newShapeJto(shape, new EventSymbolJto());

                           symbolJto.oid = bpmn2Binding.findOid((Definitions) model, shape);

                           symbolJto.modelElement = toJto((Event) shape.getBpmnElement());

                           laneJto.eventSymbols.add(symbolJto);
                        }
                     }
                     else if (symbol instanceof BPMNEdge)
                     {
                        BPMNEdge edge = (BPMNEdge) symbol;
                        if (edge.getBpmnElement() instanceof SequenceFlow)
                        {
                           ConnectionSymbolJto symbolJto = new ConnectionSymbolJto();
                           SequenceFlow sFlow = (SequenceFlow) edge.getBpmnElement();

                           if (null == nodeSymbolPerElement.get(sFlow.getSourceRef()) || (null == nodeSymbolPerElement.get(sFlow.getTargetRef())))
                           {
                              // quick exist to cater for currently unsupported node types
                              continue;
                           }

                           symbolJto.modelElement = toJto(sFlow);

                           symbolJto.fromModelElementOid = bpmn2Binding.findOid((Definitions) model, nodeSymbolPerElement.get(sFlow.getSourceRef()));
                           symbolJto.fromModelElementType = encodeNodeKind(sFlow.getSourceRef());

                           symbolJto.toModelElementOid = bpmn2Binding.findOid((Definitions) model, nodeSymbolPerElement.get(sFlow.getTargetRef()));
                           symbolJto.toModelElementType = encodeNodeKind(sFlow.getTargetRef());

                           jto.connections.add(symbolJto);
                        }
                     }
                  }

                  poolJto.laneSymbols.add(laneJto);
                  jto.poolSymbols.add(poolJto);

                  return jsonIo.gson().toJsonTree(jto).getAsJsonObject();
               }
            }
         }
      }
      return new JsonObject();
   }

   public ModelParticipantJto toJto(Participant participant)
   {
      ModelParticipantJto jto = newModelElementJto(participant, new ModelParticipantJto());

      jto.type = ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY;

      return jto;
   }

   public DataJto toJto(Process process, Property property)
   {
      DataJto jto;
      if ((null == property.getItemSubjectRef())
            || (null == property.getItemSubjectRef().getStructureRef()))
      {
         // undefined data
         jto = newModelElementJto(property, new PrimitiveDataJto());
         ((PrimitiveDataJto) jto).primitiveDataType = ModelerConstants.STRING_PRIMITIVE_DATA_TYPE;
      }
      // TODO struct
      else
      {
         jto = newModelElementJto(property, new DataJto());
      }

      // prefix naming to cater for "global data" projection
      jto.id = process.getId() + "_" + jto.id;
      jto.name = nameOrId(process.getName(), process.getId()) + " - " + jto.name;

      return jto;
   }

   public ProcessDefinitionJto toJto(Process process)
   {
      ProcessDefinitionJto jto = newModelElementJto(process, new ProcessDefinitionJto());

      for (FlowElement flowElement : process.getFlowElements())
      {
         // TODO
         if (flowElement instanceof Activity)
         {
            jto.activities.add(toJto((Activity) flowElement));
         }
         else if (flowElement instanceof Gateway)
         {
            jto.gateways.add(toJto((Gateway) flowElement));
         }
         else if (flowElement instanceof SequenceFlow)
         {
            jto.controlFlows.add(toJto((SequenceFlow) flowElement));
         }
      }

      return jto;
   }

   public ActivityJto toJto(Activity activity)
   {
      ActivityJto jto = newModelElementJto(activity, new ActivityJto());

      // TODO
      jto.activityType = "manual";

      return jto;
   }

   public ActivityJto toJto(Gateway gateway)
   {
      ActivityJto jto = newModelElementJto(gateway, new ActivityJto());

      // prefix name due to current gateway-workarounds
      jto.name = "gateway" + jto.name;

      // TODO

      return jto;
   }

   public EventJto toJto(Event event)
   {
      EventJto jto = newModelElementJto(event, new EventJto());

      if (event instanceof StartEvent)
      {
         jto.eventType = ModelerConstants.START_EVENT;
      }
      else if (event instanceof EndEvent)
      {
         jto.eventType = ModelerConstants.STOP_EVENT;
      }

      // TODO

      return jto;
   }

   public TransitionJto toJto(SequenceFlow sFlow)
   {
      TransitionJto jto = newModelElementJto(sFlow, new TransitionJto());

      if (sFlow.getConditionExpression() instanceof FormalExpression)
      {
         // TODO otherwise
         jto.conditionExpression = ((FormalExpression) sFlow.getConditionExpression()).getBody();
      }

      return jto;
   }

   public <T extends BaseElement, J extends ModelElementJto> J newModelElementJto(T src, J jto)
   {
      jto.uuid = src.getId();
      jto.id = src.getId();

      String name;
      if (src instanceof Participant)
      {
         name = ((Participant) src).getName();
      }
      else if (src instanceof Property)
      {
         name = ((Property) src).getName();
      }
      else if (src instanceof FlowElement)
      {
         name = ((FlowElement) src).getName();
      }
      else if (src instanceof CallableElement)
      {
         name = ((CallableElement) src).getName();
      }
      else
      {
         throw new IllegalArgumentException("Unsupported model element: " + src);
      }

      // fall back to UUID if name is not defined
      jto.name = nameOrId(name, src.getId());

      jto.oid = bpmn2Binding.findOid(src);

      return jto;
   }

   public <T extends Shape, J extends ShapeJto> J newShapeJto(BPMNShape shape, J jto)
   {
      jto.x = (int) shape.getBounds().getX();
      jto.y = (int) shape.getBounds().getY();
      jto.width = (int) shape.getBounds().getWidth();
      jto.height = (int) shape.getBounds().getHeight();

      return jto;
   }

   public static String encodeNodeKind(FlowNode node)
   {
      if (node instanceof Activity)
      {
         return ModelerConstants.ACTIVITY_KEY;
      }
      else if (node instanceof Gateway)
      {
         return ModelerConstants.GATEWAY;
      }
      else if (node instanceof Event)
      {
         return ModelerConstants.EVENT_KEY;
      }
      else
      {
         throw new IllegalArgumentException("Unsupported flow node: " + node);
      }
   }

   private static String nameOrId(String name, String id)
   {
      return !isEmpty(name) ? name : id;
   }
}
