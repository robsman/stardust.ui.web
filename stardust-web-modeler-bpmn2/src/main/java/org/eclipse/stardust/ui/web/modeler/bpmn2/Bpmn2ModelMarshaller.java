package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2DcFactory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findContainingModel;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findParticipatingProcesses;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.CallableElement;
import org.eclipse.bpmn2.CatchEvent;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.dc.Bounds;
import org.eclipse.dd.dc.Point;
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
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
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
                  else if (plane.getBpmnElement() instanceof Collaboration)
                  {
                     List<Process> participatingProcesses = findParticipatingProcesses((Collaboration) plane.getBpmnElement());
                     if ((1 == participatingProcesses.size())
                           && participatingProcesses.contains(process))
                     {
                        jto = toProcessDiagramJto((BPMNDiagram) diagram);
                     }
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
         List<Process> participatingProcesses = findParticipatingProcesses((Collaboration) plane.getBpmnElement());
         if (1 != participatingProcesses.size())
         {
            throw new IllegalArgumentException("Unsupported diagram configuration: " + diagram);
         }
         else
         {
            process = participatingProcesses.get(0);
         }
      }
      else
      {
         throw new IllegalArgumentException("Unsupported diagram configuration: " + diagram);
      }

      Definitions model = findContainingModel(diagram);
      if (null == model)
      {
         throw new IllegalArgumentException("Must not pass a detached diagram: " + diagram);
      }

      // find pool/lane shapes
      BPMNShape processPoolShape = null;
      List<BPMNShape> otherPoolShapes = newArrayList();
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
                  processPoolShape = shape;
               }
               else
               {
                  otherPoolShapes.add(shape);
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

      PoolSymbolJto mainPoolJto;
      LaneSymbolJto defaultLane = null;
      boolean horizontalLanes = false;
      boolean verticalLanes = false;
      if (null != processPoolShape)
      {
         mainPoolJto = newShapeJto(processPoolShape, new PoolSymbolJto());

         if (processPoolShape.getBpmnElement() instanceof Participant)
         {
            Participant poolParticipant = (Participant) processPoolShape.getBpmnElement();
         }

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

            if (laneShape.getBpmnElement() instanceof Lane)
            {
               laneJto.name = ((Lane) laneShape.getBpmnElement()).getName();
            }

            mainPoolJto.laneSymbols.add(laneJto);
         }
      }
      else
      {
         // TODO dynamically determine pool/lane dimensions
         mainPoolJto = new PoolSymbolJto();
         mainPoolJto.x = 0;
         mainPoolJto.x = 0;
         mainPoolJto.width = 1000;
         mainPoolJto.height = 600;
      }

      // ensure pool shape fully encloses lanes and has proper orientation
      int right = mainPoolJto.x + mainPoolJto.width;
      int bottom = mainPoolJto.y + mainPoolJto.height;
      for (LaneSymbolJto laneSymbol : mainPoolJto.laneSymbols)
      {
         mainPoolJto.x = Math.min(mainPoolJto.x, laneSymbol.x);
         mainPoolJto.y = Math.min(mainPoolJto.y, laneSymbol.y);
         right = Math.max(right, laneSymbol.x + laneSymbol.width);
         bottom = Math.max(bottom, laneSymbol.y + laneSymbol.height);
      }
      mainPoolJto.width = right - mainPoolJto.x;
      mainPoolJto.height = bottom - mainPoolJto.y;

      if (horizontalLanes && !verticalLanes)
      {
         mainPoolJto.orientation = ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL;
      }

      if (null == defaultLane)
      {
         defaultLane = new LaneSymbolJto();
         defaultLane.x = 10;
         defaultLane.y = 10;
         defaultLane.width = 980;
         defaultLane.height = 580;

         mainPoolJto.laneSymbols.add(defaultLane);
      }

      // required to properly connect connections to node symbols
      Map<FlowNode, BPMNShape> nodeSymbolPerElement = newHashMap();
      for (DiagramElement symbol : plane.getPlaneElement())
      {
         if (symbol instanceof BPMNShape)
         {
            // find related lane
            LaneSymbolJto laneJto = null;
            for (LaneSymbolJto lane : mainPoolJto.laneSymbols)
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
            if (null == laneJto)
            {
               continue;
            }

            BPMNShape shape = (BPMNShape) symbol;

            if (shape.getBpmnElement() instanceof FlowNode)
            {
               nodeSymbolPerElement.put((FlowNode) shape.getBpmnElement(), shape);

               if (shape.getBpmnElement() instanceof Activity)
               {
                  ActivitySymbolJto symbolJto = newShapeJto(shape, new ActivitySymbolJto());

                  symbolJto.modelElement = toJto((Activity) shape.getBpmnElement());

                  laneJto.activitySymbols.add(symbolJto);
               }
               else if (shape.getBpmnElement() instanceof Gateway)
               {
                  GatewaySymbolJto symbolJto = newShapeJto(shape, new GatewaySymbolJto());

                  symbolJto.modelElement = toJto((Gateway) shape.getBpmnElement());

                  laneJto.gatewaySymbols.add(symbolJto);
               }
               else if (shape.getBpmnElement() instanceof Event)
               {
                  if ((shape.getBpmnElement() instanceof StartEvent) || (shape.getBpmnElement() instanceof EndEvent))
                  {
                     EventSymbolJto symbolJto = newShapeJto(shape, new EventSymbolJto());

                     symbolJto.modelElement = toJto((Event) shape.getBpmnElement());

                     laneJto.eventSymbols.add(symbolJto);
                  }
                  else if (shape.getBpmnElement() instanceof BoundaryEvent)
                  {
                     EventSymbolJto symbolJto = newShapeJto(shape, new EventSymbolJto());

                     symbolJto.modelElement = toJto((Event) shape.getBpmnElement());

                     laneJto.eventSymbols.add(symbolJto);
                  }
               }
            }
         }
         else if (symbol instanceof BPMNEdge)
         {
            BPMNEdge edge = (BPMNEdge) symbol;
            if (edge.getBpmnElement() instanceof SequenceFlow)
            {
               ConnectionSymbolJto symbolJto = new ConnectionSymbolJto();
               SequenceFlow sFlow = (SequenceFlow) edge.getBpmnElement();

               BPMNShape sourceNode = nodeSymbolPerElement.get(sFlow.getSourceRef());
               BPMNShape targetNode = nodeSymbolPerElement.get(sFlow.getTargetRef());
               if ((null == sourceNode) || (null == targetNode))
               {
                  // quick exist to cater for currently unsupported node types
                  continue;
               }

               symbolJto.type = ModelerConstants.CONTROL_FLOW_CONNECTION_LITERAL;
               symbolJto.modelElement = toJto(sFlow);

               symbolJto.fromModelElementOid = bpmn2Binding.findOid((Definitions) model, sourceNode);
               symbolJto.fromModelElementType = encodeNodeKind(sFlow.getSourceRef());
               symbolJto.toModelElementOid = bpmn2Binding.findOid((Definitions) model, targetNode);
               symbolJto.toModelElementType = encodeNodeKind(sFlow.getTargetRef());

               if ( !isEmpty(edge.getWaypoint()) && (2 <= edge.getWaypoint().size()))
               {
                  // use original coordinates to avoid having to adjust waypoints as well (see determineShapeBounds)
                  symbolJto.fromAnchorPointOrientation = determineAnchorPoint(sourceNode,
                        edge.getWaypoint().get(0), edge.getWaypoint().get(1));
                  symbolJto.toAnchorPointOrientation = determineAnchorPoint(targetNode,
                        edge.getWaypoint().get(edge.getWaypoint().size() - 1),
                        edge.getWaypoint().get(edge.getWaypoint().size() - 2));
               }

               jto.connections.add(symbolJto);
            }
         }
      }

      jto.poolSymbols.add(mainPoolJto);

      for (BPMNShape poolShape : otherPoolShapes)
      {
         PoolSymbolJto poolJto = newShapeJto(poolShape, new PoolSymbolJto());

         if (poolShape.getBpmnElement() instanceof Participant)
         {
            poolJto.id = ((Participant) poolShape.getBpmnElement()).getId();
            poolJto.name = ((Participant) poolShape.getBpmnElement()).getName();
            // TODO anything else?
         }

         poolJto.orientation = mainPoolJto.orientation;
         // grow pool to be at least as large as the main pool
         if (ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL.equals(poolJto.orientation))
         {
            if (poolJto.x > mainPoolJto.x)
            {
               poolJto.width += (poolJto.x - mainPoolJto.x);
               poolJto.x = mainPoolJto.x;
            }
            if ((poolJto.x + poolJto.width) < (mainPoolJto.x + mainPoolJto.width))
            {
               poolJto.width = (mainPoolJto.x + mainPoolJto.width) - poolJto.x;
            }
         }
         else if (ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL.equals(poolJto.orientation))
         {
            if (poolJto.y > mainPoolJto.y)
            {
               poolJto.height += (poolJto.y - mainPoolJto.y);
               poolJto.y = mainPoolJto.y;
            }
            if ((poolJto.y + poolJto.height) < (mainPoolJto.y + mainPoolJto.height))
            {
               poolJto.height = (mainPoolJto.y + mainPoolJto.height) - poolJto.y;
            }
         }

         jto.poolSymbols.add(poolJto);
      }

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

   public GatewayJto toJto(Gateway gateway)
   {
      GatewayJto jto = newModelElementJto(gateway, new GatewayJto());

      // prefix name due to current gateway-workarounds
      jto.name = "gateway" + jto.name;

      // TODO
      if (gateway instanceof ExclusiveGateway)
      {
         jto.gatewayType = ModelerConstants.XOR_GATEWAY_TYPE;
      }
      else if (gateway instanceof ParallelGateway)
      {
         jto.gatewayType = ModelerConstants.AND_GATEWAY_TYPE;
      }

      return jto;
   }

   public EventJto toJto(Event event)
   {
      EventJto jto = newModelElementJto(event, new EventJto());

      if (event instanceof StartEvent)
      {
         jto.eventType = ModelerConstants.START_EVENT;
      }
      else if (event instanceof BoundaryEvent)
      {
         jto.eventType = ModelerConstants.START_EVENT;
      }
      else if (event instanceof EndEvent)
      {
         jto.eventType = ModelerConstants.STOP_EVENT;
      }

      if ((null != jto) && (event instanceof CatchEvent))
      {
         jto.eventClass = encodeCatchEventType((CatchEvent) event);
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
      else if ( !isEmpty(sFlow.getName()))
      {
         jto.conditionExpression = sFlow.getName();
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

      Bounds bounds = determineShapeBounds(shape);
      jto.x = (int) bounds.getX();
      jto.y = (int) bounds.getY();
      jto.width = (int) bounds.getWidth();
      jto.height = (int) bounds.getHeight();

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

   public String encodeCatchEventType(CatchEvent event)
   {
      Set<String> eventClasses = newHashSet();
      for (EventDefinition eventDefinition : event.getEventDefinitions())
      {
         if (eventDefinition instanceof TimerEventDefinition)
         {
            eventClasses.add("timerEvent");
         }
         else if (eventDefinition instanceof MessageEventDefinition)
         {
            eventClasses.add("messageEvent");
         }
         else
         {
            // TODO more event classes ...
         }
      }
      return (1 == eventClasses.size()) ? eventClasses.iterator().next() : null; // TODO "complex" instead of null;
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

   private int determineAnchorPoint(BPMNShape fromShape, Point point, Point point2)
   {
      Bounds fromBounds = fromShape.getBounds(); // determineShapeBounds(fromShape);

      double dx = point.getX() - (fromBounds.getX()); // + fromBounds.getWidth() / 2.0);
      double dy = point.getY() - (fromBounds.getY()); // + fromBounds.getHeight() / 2.0);

      if ((dx == 0.0) && (dy == 0.0))
      {
         dx = point2.getX() - point.getX();
         dy = point2.getY() - point.getY();

      }

      if (Math.abs(dx) >= Math.abs(dy))
      {
         if (0L == Math.round(Math.abs(dx)))
         {
            return ModelerConstants.UNDEFINED_ORIENTATION_KEY;
         }

         // horizontal
         return (0.0 < dx) ? ModelerConstants.EAST_KEY : ModelerConstants.WEST_KEY;

      }
      else
      {
         // vertical
         return (0.0 < dy) ? ModelerConstants.SOUTH_KEY : ModelerConstants.NORTH_KEY;
      }
   }

   private Bounds determineShapeBounds(BPMNShape shape)
   {
      Definitions model = Bpmn2Utils.findContainingModel(shape);
      if ((null != model) && ("ADONIS".equals(model.getExporter())))
      {
         Bounds bounds = shape.getBounds();
         if ((shape.getBpmnElement() instanceof Lane))
         {
            bounds = cloneBounds(bounds);
            if (shape.isIsHorizontal())
            {
               bounds.setWidth(bounds.getWidth() + 100.0F);
            }
            else
            {
               bounds.setHeight(bounds.getHeight() + 100.0F);
            }
         }
         else if (shape.getBpmnElement() instanceof Activity)
         {
            bounds = cloneBounds(bounds);
            if (105.0F == bounds.getWidth())
            {
               // fix default width to be multiple of two
               bounds.setWidth(106.0F);
            }
         }
         else if (shape.getBpmnElement() instanceof Event)
         {
            bounds = cloneBounds(bounds);
            // fix missing width/height
            if (0.0F == bounds.getWidth())
            {
               bounds.setX(bounds.getX() + (106.0F - 24.0F) / 2.0F);
               bounds.setWidth(24.0F);
            }
            if (0.0F == bounds.getHeight())
            {
               bounds.setY(bounds.getY() + (56.0F - 24.0F) / 2.0F);
               bounds.setHeight(24.0F);
            }
         }
         else if (shape.getBpmnElement() instanceof Gateway)
         {
            bounds = cloneBounds(bounds);
            // fix missing width/height
            if (0.0F == bounds.getWidth())
            {
               bounds.setX(bounds.getX() + (106.0F - 40.0F) / 2.0F);
               bounds.setWidth(40.0F);
            }
            if (0.0F == bounds.getHeight())
            {
               bounds.setY(bounds.getY() + (56.0F - 40.0F) / 2.0F);
               bounds.setHeight(40.0F);
            }
         }

         return bounds;
      }
      else
      {
         return shape.getBounds();
      }
   }

   private Bounds cloneBounds(Bounds bounds)
   {
      Bounds clonedBounds;
      clonedBounds = bpmn2DcFactory().createBounds();
      clonedBounds.setX(bounds.getX());
      clonedBounds.setY(bounds.getY());
      clonedBounds.setWidth(bounds.getWidth());
      clonedBounds.setHeight(bounds.getHeight());

      return clonedBounds;
   }

   private static String nameOrId(String name, String id)
   {
      return !isEmpty(name) ? name : id;
   }
}
