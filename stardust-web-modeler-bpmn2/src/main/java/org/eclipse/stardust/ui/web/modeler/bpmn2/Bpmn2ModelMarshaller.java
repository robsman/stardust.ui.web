package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;

import java.util.List;
import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.CallableElement;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.dc.Bounds;
import org.eclipse.dd.di.Diagram;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.dd.di.Edge;
import org.eclipse.dd.di.Shape;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.integration.ExternalXmlSchemaManager;
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
import org.eclipse.stardust.ui.web.modeler.model.TypeDeclarationJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ActivitySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ConnectionSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.DataSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.EventSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.GatewaySymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.LaneSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.PoolSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ShapeJto;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;

public class Bpmn2ModelMarshaller implements ModelMarshaller
{
   private Bpmn2Binding bpmn2Binding;

   private final JsonMarshaller jsonIo = new JsonMarshaller();

   // TODO wire to Spring bean
   private final ExternalXmlSchemaManager externalXmlSchemaManager = new ExternalXmlSchemaManager();

   void setBinding(Bpmn2Binding bpmn2Binding)
   {
      this.bpmn2Binding = bpmn2Binding;
   }

   @Override
   public JsonElement toJson(EObject element)
   {
      if (element instanceof Definitions)
      {
         return toModelJson(element);
      }
      else if (element instanceof Process)
      {
         return jsonIo.gson().toJsonTree(toJto((Process) element));
      }
      else if (element instanceof BPMNDiagram)
      {
         return jsonIo.gson().toJsonTree(toProcessDiagramJto((BPMNDiagram) element));
      }
      else if (element instanceof DataObject)
      {
         return jsonIo.gson().toJsonTree(toJto((DataObject) element));
      }
      else if (element instanceof Event)
      {
         return jsonIo.gson().toJsonTree(toJto((Event) element));
      }
      else if (element instanceof Activity)
      {
         return jsonIo.gson().toJsonTree(toJto((Activity) element));
      }
      else if (element instanceof Gateway)
      {
         return jsonIo.gson().toJsonTree(toJto((Gateway) element));
      }
      else if (element instanceof SequenceFlow)
      {
         return jsonIo.gson().toJsonTree(toJto((SequenceFlow) element));
      }
      else if (element instanceof BPMNShape)
      {
         BPMNShape shape = (BPMNShape) element;

         if (shape.getBpmnElement() instanceof DataObject)
         {
            DataSymbolJto symbolJto = newShapeJto(shape, new DataSymbolJto());
            symbolJto.modelElement = toJto((DataObject) shape.getBpmnElement());

            return jsonIo.gson().toJsonTree(symbolJto);
         }
         else if (shape.getBpmnElement() instanceof Event)
         {
            EventSymbolJto symbolJto = newShapeJto(shape, new EventSymbolJto());
            symbolJto.modelElement = toJto((Event) shape.getBpmnElement());

            return jsonIo.gson().toJsonTree(symbolJto);
         }
         else if (shape.getBpmnElement() instanceof Activity)
         {
            ActivitySymbolJto symbolJto = newShapeJto(shape, new ActivitySymbolJto());
            symbolJto.modelElement = toJto((Activity) shape.getBpmnElement());

            return jsonIo.gson().toJsonTree(symbolJto);
         }
         else if (shape.getBpmnElement() instanceof Gateway)
         {
            GatewaySymbolJto symbolJto = newShapeJto(shape, new GatewaySymbolJto());
            symbolJto.modelElement = toJto((Gateway) shape.getBpmnElement());

            return jsonIo.gson().toJsonTree(symbolJto);
         }
      }
      else if (element instanceof BPMNEdge)
      {
         BPMNEdge edge = (BPMNEdge) element;

         if (edge.getBpmnElement() instanceof SequenceFlow)
         {
            ConnectionSymbolJto symbolJto = newEdgeJto(edge, new ConnectionSymbolJto());
            SequenceFlow flow = (SequenceFlow) edge.getBpmnElement();
            symbolJto.modelElement = toJto(flow);
            symbolJto.fromModelElementType = encodeNodeKind(flow.getSourceRef());
            symbolJto.toModelElementType = encodeNodeKind(flow.getTargetRef());

            return jsonIo.gson().toJsonTree(symbolJto);
         }
      }

      throw new IllegalArgumentException("Not yet implemented: " + element);
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
         if (root instanceof ItemDefinition)
         {
            modelJto.typeDeclarations.add(toJto((ItemDefinition) root));
         }
         else if (root instanceof Participant)
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
               if (diagram.getRootElement() instanceof BPMNPlane)
               {
                  BPMNPlane plane = (BPMNPlane) diagram.getRootElement();

                  ProcessDiagramJto jto = null;
                  if (process == plane.getBpmnElement())
                  {
                     jto = toProcessDiagramJto((BPMNDiagram) diagram);
                  }
                  else if ((plane.getBpmnElement() instanceof Collaboration)
                        && (1 == ((Collaboration) plane.getBpmnElement()).getParticipants()
                              .size())
                        && (process == ((Collaboration) plane.getBpmnElement()).getParticipants()
                              .get(0)
                              .getProcessRef()))
                  {
                     jto = toProcessDiagramJto((BPMNDiagram) diagram);
                  }

                  if (null != jto)
                  {
                     return jsonIo.gson().toJsonTree(jto).getAsJsonObject();
                  }
               }
            }
         }
      }
      return new JsonObject();
   }

   public ProcessDiagramJto toProcessDiagramJto(BPMNDiagram diagram)
   {
      BPMNPlane plane = (BPMNPlane) diagram.getRootElement();

      Process process;
      if (plane.getBpmnElement() instanceof Process)
      {
         process = (Process) plane.getBpmnElement();
      }
      else if (plane.getBpmnElement() instanceof Collaboration)
      {
         Collaboration collab = (Collaboration) plane.getBpmnElement();
         if ((1 != collab.getParticipants().size()) || (null == collab.getParticipants().get(0).getProcessRef()))
         {
            throw new IllegalArgumentException("Unsupported diagram configuration: " + diagram);
         }
         else
         {
            process = collab.getParticipants().get(0).getProcessRef();
         }
      }
      else
      {
         throw new IllegalArgumentException("Unsupported diagram configuration: " + diagram);
      }

      Definitions model = Bpmn2Utils.findContainingModel(diagram);
      if (null == model)
      {
         throw new IllegalArgumentException("Must not pass a detached diagram: " + diagram);
      }

      // find pool/lane shapes
      BPMNShape poolShape = null;
      List<BPMNShape> laneShapes = newArrayList();
      for (DiagramElement element : plane.getPlaneElement())
      {
         if (element instanceof BPMNShape)
         {
            BPMNShape shape = (BPMNShape) element;
            if (shape.getBpmnElement() instanceof Participant)
            {
               Participant participant = (Participant) shape.getBpmnElement();
               if (process == participant.getProcessRef())
               {
                  // TODO found the pool symbol
                  poolShape = shape;
               }
            }
            else if (shape.getBpmnElement() instanceof Lane)
            {
               // TODO found a lane symbol
               laneShapes.add(shape);
            }
         }
      }

      ProcessDiagramJto jto = new ProcessDiagramJto();
      jto.uuid = plane.getId();
      jto.id = plane.getId();
      jto.name = nameOrId(diagram.getName(), plane.getId());

      PoolSymbolJto poolJto;
      LaneSymbolJto defaultLane = null;
      boolean horizontalLanes = false;
      boolean verticalLanes = false;
      if (null != poolShape)
      {
         poolJto = newShapeJto(poolShape, new PoolSymbolJto());

         for (BPMNShape laneShape : laneShapes)
         {
            LaneSymbolJto laneJto = newShapeJto(laneShape, new LaneSymbolJto());
            if (null == defaultLane)
            {
               defaultLane = laneJto;
            }
            if (laneShape.isIsHorizontal())
            {
               horizontalLanes = true;
               laneJto.orientation = ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL;
            }
            else
            {
               verticalLanes = true;
            }

            poolJto.laneSymbols.add(laneJto);
         }
      }
      else
      {
         // TODO dynamically determine pool/lane dimensions
         poolJto = new PoolSymbolJto();
         poolJto.x = 0;
         poolJto.x = 0;
         poolJto.width = 1000;
         poolJto.height = 600;
      }

      // ensure pool shape fully encloses lanes and has proper orientation
      int right = poolJto.x + poolJto.width;
      int bottom = poolJto.y + poolJto.height;
      for (LaneSymbolJto laneSymbol : poolJto.laneSymbols)
      {
         poolJto.x = Math.min(poolJto.x, laneSymbol.x);
         poolJto.y = Math.min(poolJto.y, laneSymbol.y);
         right = Math.max(right, laneSymbol.x + laneSymbol.width);
         bottom = Math.max(bottom, laneSymbol.y + laneSymbol.height);
      }
      poolJto.width = right - poolJto.x;
      poolJto.height = bottom - poolJto.y;

      if (horizontalLanes && !verticalLanes)
      {
         poolJto.orientation = ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL;
      }

      if (null == defaultLane)
      {
         defaultLane = new LaneSymbolJto();
         defaultLane.x = 10;
         defaultLane.y = 10;
         defaultLane.width = 980;
         defaultLane.height = 580;

         poolJto.laneSymbols.add(defaultLane);
      }

      // required to properly connect connections to node symbols
      Map<FlowNode, BPMNShape> nodeSymbolPerElement = newHashMap();
      for (DiagramElement symbol : plane.getPlaneElement())
      {
         if (symbol instanceof BPMNShape)
         {
            // find related lane
            LaneSymbolJto laneJto = null;
            for (LaneSymbolJto lane : poolJto.laneSymbols)
            {
               Bounds bounds = ((BPMNShape) symbol).getBounds();
               if ((lane.x <= bounds.getX())
                     && ((lane.x + lane.width) >= bounds.getX() + bounds.getWidth())
                     && (lane.y <= bounds.getY())
                     && ((lane.y + lane.height) >= bounds.getY() + bounds.getHeight()))
               {
                  laneJto = lane;
                  break;
               }
            }

            BPMNShape shape = (BPMNShape) symbol;
            if (shape.getBpmnElement() instanceof Activity)
            {
               nodeSymbolPerElement.put((Activity) shape.getBpmnElement(), shape);

               ActivitySymbolJto symbolJto = newShapeJto(shape, new ActivitySymbolJto());

               symbolJto.modelElement = toJto((Activity) shape.getBpmnElement());

               laneJto.activitySymbols.add(symbolJto);
            }
            else if (shape.getBpmnElement() instanceof Gateway)
            {
               nodeSymbolPerElement.put((Gateway) shape.getBpmnElement(), shape);

               GatewaySymbolJto symbolJto = newShapeJto(shape, new GatewaySymbolJto());

               symbolJto.modelElement = toJto((Gateway) shape.getBpmnElement());

               laneJto.gatewaySymbols.add(symbolJto);
            }
            else if ((shape.getBpmnElement() instanceof StartEvent) || (shape.getBpmnElement() instanceof EndEvent))
            {
               nodeSymbolPerElement.put((Event) shape.getBpmnElement(), shape);

               EventSymbolJto symbolJto = newShapeJto(shape, new EventSymbolJto());

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

      jto.poolSymbols.add(poolJto);

      return jto;
   }

   public TypeDeclarationJto toJto(ItemDefinition itemDefinition)
   {
      TypeDeclarationJto jto = newModelElementJto(itemDefinition, new TypeDeclarationJto());

      // TODO
      String schemaLocation = null;
      String typeId = null;
      if (itemDefinition.getStructureRef() instanceof InternalEObject)
      {
         URI proxyURI = ((InternalEObject) itemDefinition.getStructureRef()).eProxyURI();
         if (proxyURI.hasFragment())
         {
            schemaLocation = proxyURI.trimFragment().toString();
            typeId = proxyURI.fragment();
         }
      }

      Import importSpec = itemDefinition.getImport();
      if ((null == importSpec) && !isEmpty(schemaLocation))
      {
         for (Import candidate : Bpmn2Utils.findContainingModel(itemDefinition).getImports())
         {
            if (schemaLocation.equals(candidate.getLocation()))
            {
               importSpec = candidate;
               break;
            }
         }
      }

      if ((null != importSpec) && (XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001.equals(importSpec.getImportType())))
      {
         XSDSchema importedSchema = externalXmlSchemaManager.resolveSchemaFromUri(importSpec
               .getLocation());
         if ((null != importedSchema) && !isEmpty(typeId))
         {
            for (XSDTypeDefinition typeDefinition : importedSchema.getTypeDefinitions())
            {
               if (typeId.equals(typeDefinition.getName()))
               {
                  ModelService.loadSchemaInfo(jto.typeDeclaration.schema, importedSchema);

                  jto.typeDeclaration.type.classifier = "ExternalReference";
                  jto.typeDeclaration.type.location = importSpec.getLocation();
                  jto.typeDeclaration.type.xref = typeDefinition.getQName(importedSchema);
               }
            }
         }
      }

      return jto;
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

   public DataJto toJto(DataObject variable)
   {
      DataJto jto = newModelElementJto(variable, new DataJto());

      if (null != variable.getItemSubjectRef())
      {
         // TODO
         jto.dataType = ModelerConstants.STRUCTURED_DATA_TYPE_KEY;
         jto.structuredDataTypeFullId = Bpmn2Utils.findContainingModel(variable).getId() + ":" + variable.getItemSubjectRef().getId();
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
      jto.uuid = bpmn2Binding.findUuid(src);
      jto.id = src.getId();

      String name;
      if (src instanceof ItemDefinition)
      {
         name = ((ItemDefinition) src).getId();
      }
      else if (src instanceof Participant)
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

      Definitions model = Bpmn2Utils.findContainingModel(src);
      if (null != model)
      {
         jto.modelId = model.getId();
      }

      return jto;
   }

   public <T extends Shape, J extends ShapeJto> J newShapeJto(BPMNShape shape, J jto)
   {
      jto.oid = bpmn2Binding.findOid(shape);

      jto.x = (int) shape.getBounds().getX();
      jto.y = (int) shape.getBounds().getY();
      jto.width = (int) shape.getBounds().getWidth();
      jto.height = (int) shape.getBounds().getHeight();

      return jto;
   }

   public <T extends Edge, J extends ConnectionSymbolJto> J newEdgeJto(BPMNEdge edge, J jto)
   {
      jto.oid = bpmn2Binding.findOid(edge);

      if (null != edge.getSourceElement())
      {
         jto.fromModelElementOid = bpmn2Binding.findOid(edge.getSourceElement());
      }
      if (null != edge.getTargetElement())
      {
         jto.toModelElementOid = bpmn2Binding.findOid(edge.getTargetElement());
      }
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
