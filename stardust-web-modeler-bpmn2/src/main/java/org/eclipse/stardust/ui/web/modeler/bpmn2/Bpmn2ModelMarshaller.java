package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.isEmpty;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.common.CollectionUtils.newHashSet;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findContainingModel;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.findParticipatingProcesses;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils.getExtensionElement;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.ElementRefUtils.encodeReference;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractAsString;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.BoundaryEvent;
import org.eclipse.bpmn2.CallableElement;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.DataObjectReference;
import org.eclipse.bpmn2.DataStore;
import org.eclipse.bpmn2.DataStoreReference;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Documentation;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.ErrorEventDefinition;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.EventDefinition;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.Interface;
import org.eclipse.bpmn2.IntermediateCatchEvent;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.ItemAwareElement;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.ManualTask;
import org.eclipse.bpmn2.MessageEventDefinition;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Performer;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.Property;
import org.eclipse.bpmn2.ReceiveTask;
import org.eclipse.bpmn2.Resource;
import org.eclipse.bpmn2.ResourceRole;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.ScriptTask;
import org.eclipse.bpmn2.SendTask;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.ServiceTask;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.SubProcess;
import org.eclipse.bpmn2.TimerEventDefinition;
import org.eclipse.bpmn2.UserTask;
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

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.integration.ExternalXmlSchemaManager;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.marshaling.ModelMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.ApplicationJto;
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
import org.eclipse.stardust.ui.web.modeler.service.XsdSchemaUtils;

public class Bpmn2ModelMarshaller implements ModelMarshaller
{
   private static final Logger trace = LogManager.getLogger(Bpmn2ModelMarshaller.class);

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
      trace.info("Converting to JSON: " + element);

      if (element instanceof Definitions)
      {
         return toModelJson(element);
      }
      else if (element instanceof ItemDefinition)
      {
         return jsonIo.gson().toJsonTree(toJto((ItemDefinition) element));
      }
      else if (element instanceof Interface)
      {
         return jsonIo.gson().toJsonTree(toJto((Interface) element));
      }
      else if (element instanceof Process)
      {
         return jsonIo.gson().toJsonTree(toJto((Process) element));
      }
      else if (element instanceof BPMNDiagram)
      {
         return jsonIo.gson().toJsonTree(toProcessDiagramJto((BPMNDiagram) element, null));
      }
      else if (element instanceof DataStore)
      {
         return jsonIo.gson().toJsonTree(toJto((DataStore) element));
      }
      else if (element instanceof DataObject)
      {
         return jsonIo.gson().toJsonTree(toJto((DataObject) element));
      }
      else if (element instanceof Resource)
      {
         return jsonIo.gson().toJsonTree(toJto((Resource) element));
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
         else if (shape.getBpmnElement() instanceof DataStoreReference)
         {
            DataSymbolJto symbolJto = newShapeJto(shape, new DataSymbolJto());
            symbolJto.modelElement = toJto(((DataStoreReference) shape.getBpmnElement()).getDataStoreRef());

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
         else if (shape.getBpmnElement() instanceof Participant)
         {
            PoolSymbolJto symbolJto = newShapeJto(shape, new PoolSymbolJto());
            symbolJto.orientation = shape.isIsHorizontal()
                  ? ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL
                  : ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL;

            return jsonIo.gson().toJsonTree(symbolJto);
         }
         else if (shape.getBpmnElement() instanceof Lane)
         {
            LaneSymbolJto symbolJto = newShapeJto(shape, new LaneSymbolJto());
            symbolJto.orientation = shape.isIsHorizontal()
                  ? ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL
                  : ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL;

            return jsonIo.gson().toJsonTree(symbolJto);
         }
         else
         {
            trace.debug("Unsupported shape: " + shape.getBpmnElement());
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

      String modelUuid = Bpmn2Utils.getModelUuid(bpmn2Model);

      ModelJto modelJto = new ModelJto();
      modelJto.uuid = modelUuid;
      modelJto.id = modelUuid;
      modelJto.name = bpmn2Model.getName();
      if (isEmpty(modelJto.name))
      {
         modelJto.name = bpmn2Binding.getModelFileName(bpmn2Model);
         if ( !isEmpty(modelJto.name) && modelJto.name.endsWith(".bpmn"))
         {
            modelJto.name = modelJto.name.substring(0,
                  modelJto.name.length() - ".bpmn".length());
         }
      }

      // TODO processes etc.
      for (RootElement root : bpmn2Model.getRootElements())
      {
         if (root instanceof ItemDefinition)
         {
            modelJto.typeDeclarations.put(root.getId(), toJto((ItemDefinition) root));
         }
         else if (root instanceof Participant)
         {
            modelJto.participants.put(root.getId(), toJto((Participant) root));
         }
         else if (root instanceof Resource)
         {
            ModelParticipantJto jto = toJto((Resource) root);
            if (isEmpty(jto.parentUUID))
            {
               modelJto.participants.put(root.getId(), jto);
            }
         }
         else if (root instanceof DataStore)
         {
            modelJto.dataItems.put(root.getId(), toJto((DataStore) root));
         }
         else if (root instanceof Process)
         {
            modelJto.processes.put(root.getId(), toJto((Process) root));

            for (FlowElement flowElement : ((Process) root).getFlowElements())
            {
               if (flowElement instanceof DataObject)
               {
                  modelJto.dataItems.put(root.getId(), toJto((DataObject) flowElement));
               }
            }

            // expose process properties as global data (see BPMN2 -> WS-BPEL mapping)
            for (Property property : ((Process) root).getProperties())
            {
               modelJto.dataItems.put(root.getId(), toJto((Process) root, property));
            }
         }
      }

      return jsonIo.gson().toJsonTree(modelJto).getAsJsonObject();
   }

   @Override
   public JsonObject toProcessDiagramJson(EObject model, String processId)
   {
      assert model instanceof Definitions;

      BPMNDiagram primaryDiagram = null;
      BPMNDiagram fallbackDiagram = null;

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

                  if (process == plane.getBpmnElement())
                  {
                     primaryDiagram = (BPMNDiagram) diagram;
                     break;
                  }
                  else if (plane.getBpmnElement() instanceof Collaboration)
                  {
                     List<Process> participatingProcesses = findParticipatingProcesses((Collaboration) plane.getBpmnElement());
                     if (participatingProcesses.contains(process))
                     {
                        if (1 == participatingProcesses.size())
                        {
                           primaryDiagram = (BPMNDiagram) diagram;
                           break;
                        }
                        else if (null == fallbackDiagram)
                        {
                           fallbackDiagram = (BPMNDiagram) diagram;
                        }
                     }
                  }

               }
            }

            ProcessDiagramJto jto = null;
            if (null != primaryDiagram)
            {
               jto = toProcessDiagramJto(primaryDiagram, process);
            }
            else if (null != fallbackDiagram)
            {
               jto = toProcessDiagramJto(fallbackDiagram, process);
            }
            if (null != jto)
            {
               return jsonIo.gson().toJsonTree(jto).getAsJsonObject();
            }
         }
      }
      return new JsonObject();
   }

   public ProcessDiagramJto toProcessDiagramJto(BPMNDiagram diagram, Process process)
   {
      BPMNPlane plane = (BPMNPlane) diagram.getRootElement();

      if (null == process)
      {
         if (plane.getBpmnElement() instanceof Process)
         {
            process = (Process) plane.getBpmnElement();
         }
         else if (plane.getBpmnElement() instanceof Collaboration)
         {
            List<Process> participatingProcesses = findParticipatingProcesses((Collaboration) plane.getBpmnElement());
            if (1 != participatingProcesses.size())
            {
               throw new IllegalArgumentException("Unsupported diagram configuration: "
                     + diagram);
            }
            else
            {
               process = participatingProcesses.get(0);
            }
         }
         else
         {
            throw new IllegalArgumentException("Unsupported diagram configuration: "
                  + diagram);
         }
      }

      Definitions model = findContainingModel(diagram);
      if (null == model)
      {
         throw new IllegalArgumentException("Must not pass a detached diagram: "
               + diagram);
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
      if ((null == processPoolShape) && !otherPoolShapes.isEmpty())
      {
         // TODO quick hack to show LEO diagrams
         processPoolShape = otherPoolShapes.remove(0);
      }
      if (null != processPoolShape)
      {
         mainPoolJto = newShapeJto(processPoolShape, new PoolSymbolJto());
         mainPoolJto.processId = process.getId();
         if (processPoolShape.isIsHorizontal())
         {
            mainPoolJto.orientation = ModelerConstants.DIAGRAM_FLOW_ORIENTATION_HORIZONTAL;
         }

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
         mainPoolJto = new PoolSymbolJto();
         mainPoolJto.processId = process.getId();
         if ( !otherPoolShapes.isEmpty())
         {
            float left = otherPoolShapes.get(0).getBounds().getX();
            float top = otherPoolShapes.get(0).getBounds().getY();
            float right = left + otherPoolShapes.get(0).getBounds().getWidth();
            float bottom = top + otherPoolShapes.get(0).getBounds().getHeight();

            for (BPMNShape bpmnShape : otherPoolShapes)
            {
               left = Math.min(left, bpmnShape.getBounds().getX());
               top = Math.min(top, bpmnShape.getBounds().getY());
               right = Math.max(right, bpmnShape.getBounds().getX() + bpmnShape.getBounds().getWidth());
               bottom = Math.max(bottom, bpmnShape.getBounds().getX() + bpmnShape.getBounds().getHeight());
            }

            mainPoolJto.x = Math.round(left);
            mainPoolJto.y = Math.round(bottom + 1);
            mainPoolJto.width = Math.round(right - left);
            mainPoolJto.height = 600;
         }
         else
         {
            // TODO dynamically determine pool/lane dimensions
            mainPoolJto.x = 0;
            mainPoolJto.y = 0;
            mainPoolJto.width = 1000;
            mainPoolJto.height = 600;
         }
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
         defaultLane.x = mainPoolJto.x + 10;
         defaultLane.y = mainPoolJto.y + 10;
         defaultLane.width = mainPoolJto.width - 20;
         defaultLane.height = mainPoolJto.height - 20;

         defaultLane.orientation = mainPoolJto.orientation;

         mainPoolJto.laneSymbols.add(defaultLane);
      }

      // required to properly connect connections to node symbols
      Map<FlowNode, BPMNShape> nodeSymbolPerElement = newHashMap();

      // process edges in pass after modes
      List<BPMNEdge> edges = newArrayList();
      for (DiagramElement symbol : plane.getPlaneElement())
      {
         if (symbol instanceof BPMNShape)
         {
            // find related lane
            LaneSymbolJto laneJto = null;
            for (LaneSymbolJto lane : mainPoolJto.laneSymbols)
            {
               if (isWithinBounds(((BPMNShape) symbol).getBounds(), lane))
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
               if (shape.getBpmnElement() instanceof Activity)
               {
                  ActivitySymbolJto symbolJto = newShapeJto(shape,
                        new ActivitySymbolJto());

                  symbolJto.modelElement = toJto((Activity) shape.getBpmnElement());

                  laneJto.activitySymbols.put(symbolJto.modelElement.id, symbolJto);
                  nodeSymbolPerElement.put((FlowNode) shape.getBpmnElement(), shape);
               }
               else if (shape.getBpmnElement() instanceof Gateway)
               {
                  GatewaySymbolJto symbolJto = newShapeJto(shape, new GatewaySymbolJto());

                  symbolJto.modelElement = toJto((Gateway) shape.getBpmnElement());

                  laneJto.gatewaySymbols.put(symbolJto.modelElement.id, symbolJto);
                  nodeSymbolPerElement.put((FlowNode) shape.getBpmnElement(), shape);
               }
               else if (shape.getBpmnElement() instanceof Event)
               {
                  if ((shape.getBpmnElement() instanceof StartEvent)
                        || (shape.getBpmnElement() instanceof EndEvent))
                  {
                     EventSymbolJto symbolJto = newShapeJto(shape, new EventSymbolJto());

                     symbolJto.modelElement = toJto((Event) shape.getBpmnElement());

                     laneJto.eventSymbols.put(symbolJto.modelElement.id, symbolJto);
                     nodeSymbolPerElement.put((FlowNode) shape.getBpmnElement(), shape);
                  }
                  else if (shape.getBpmnElement() instanceof BoundaryEvent)
                  {
                     EventSymbolJto symbolJto = newShapeJto(shape, new EventSymbolJto());

                     symbolJto.modelElement = toJto((Event) shape.getBpmnElement());

                     laneJto.eventSymbols.put(symbolJto.modelElement.id, symbolJto);
                     nodeSymbolPerElement.put((FlowNode) shape.getBpmnElement(), shape);
                  }
               }
            }
            else if (((shape.getBpmnElement() instanceof DataObject)
                  || (shape.getBpmnElement() instanceof DataObjectReference)))
            {
               DataObject dataObject = (shape.getBpmnElement() instanceof DataObjectReference)
                     ? ((DataObjectReference) shape.getBpmnElement()).getDataObjectRef()
                     : (DataObject) shape.getBpmnElement();

               DataSymbolJto symbolJto = newShapeJto(shape, new DataSymbolJto());
               symbolJto.dataFullId = getFullId(dataObject);

               laneJto.dataSymbols.put(dataObject.getId(), symbolJto);
            }
            else if (((shape.getBpmnElement() instanceof DataStore)
                  || (shape.getBpmnElement() instanceof DataStoreReference)))
            {
               DataStore dataStore = (shape.getBpmnElement() instanceof DataStoreReference)
                     ? ((DataStoreReference) shape.getBpmnElement()).getDataStoreRef()
                     : (DataStore) shape.getBpmnElement();

               DataSymbolJto symbolJto = newShapeJto(shape, new DataSymbolJto());
               symbolJto.dataFullId = getFullId(dataStore);

               laneJto.dataSymbols.put(dataStore.getId(), symbolJto);
            }
            else
            {
               trace.debug("Unsupported shape: " + shape.getBpmnElement());
            }
         }
         else if (symbol instanceof BPMNEdge)
         {
            edges.add((BPMNEdge) symbol);
         }
      }

      for (BPMNEdge edge : edges)
      {
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

            symbolJto.fromModelElementOid = bpmn2Binding.findOid((Definitions) model,
                  sourceNode);
            symbolJto.fromModelElementType = encodeNodeKind(sFlow.getSourceRef());
            symbolJto.toModelElementOid = bpmn2Binding.findOid((Definitions) model,
                  targetNode);
            symbolJto.toModelElementType = encodeNodeKind(sFlow.getTargetRef());

            if ( !isEmpty(edge.getWaypoint()) && (2 <= edge.getWaypoint().size()))
            {
               // use original coordinates to avoid having to adjust waypoints as well
               // (see determineShapeBounds)
               symbolJto.fromAnchorPointOrientation = determineAnchorPoint(sourceNode,
                     edge.getWaypoint().get(0), edge.getWaypoint().get(1));
               symbolJto.toAnchorPointOrientation = determineAnchorPoint(targetNode,
                     edge.getWaypoint().get(edge.getWaypoint().size() - 1),
                     edge.getWaypoint().get(edge.getWaypoint().size() - 2));
            }

            jto.connections.put(symbolJto.modelElement.id, symbolJto);
         }
      }

      jto.poolSymbols.put(mainPoolJto.id, mainPoolJto);

      for (BPMNShape poolShape : otherPoolShapes)
      {
         PoolSymbolJto poolJto = newShapeJto(poolShape, new PoolSymbolJto());

         if (poolShape.getBpmnElement() instanceof Participant)
         {
            Participant poolParticipant = (Participant) poolShape.getBpmnElement();
            poolJto.id = poolParticipant.getId();
            poolJto.name = poolParticipant.getName();

            if (null != poolParticipant.getProcessRef())
            {
               poolJto.processId = poolParticipant.getProcessRef().getId();
            }

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

         jto.poolSymbols.put(poolJto.id, poolJto);
      }

      return jto;
   }

   private static boolean isWithinBounds(Bounds symbolBounds, ShapeJto bounds)
   {
      return (bounds.x <= symbolBounds.getX())
            && ((bounds.x + bounds.width) >= symbolBounds.getX() + symbolBounds.getWidth())
            && (bounds.y <= symbolBounds.getY())
            && ((bounds.y + bounds.height) >= symbolBounds.getY() + symbolBounds.getHeight());
   }

   public TypeDeclarationJto toJto(ItemDefinition itemDefinition)
   {
      TypeDeclarationJto jto = newModelElementJto(itemDefinition,
            new TypeDeclarationJto());

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
         for (Import candidate : Bpmn2Utils.findContainingModel(itemDefinition)
               .getImports())
         {
            if (schemaLocation.equals(candidate.getLocation()))
            {
               importSpec = candidate;
               break;
            }
         }
      }

      if ((null != importSpec)
            && (XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001.equals(importSpec.getImportType())))
      {
         XSDSchema importedSchema = externalXmlSchemaManager.resolveSchemaFromUri(importSpec.getLocation());
         if ((null != importedSchema) && !isEmpty(typeId))
         {
            for (XSDTypeDefinition typeDefinition : importedSchema.getTypeDefinitions())
            {
               if (typeId.equals(typeDefinition.getName()))
               {
                  jto.typeDeclaration.schema = XsdSchemaUtils.toSchemaJson(importedSchema);
                  jto.typeDeclaration.type.classifier = "ExternalReference";
                  jto.typeDeclaration.type.location = importSpec.getLocation();
                  jto.typeDeclaration.type.xref = typeDefinition.getQName(importedSchema);
               }
            }
         }
      }
      else
      {
         EObject xsdSchema = getExtensionElement(itemDefinition, "schema", XMLConstants.W3C_XML_SCHEMA_NS_URI);
         if ((xsdSchema instanceof XSDSchema) && !isEmpty(typeId))
         {
            XSDSchema embeddedSchema = (XSDSchema) xsdSchema;
            jto.typeDeclaration.schema = XsdSchemaUtils.toSchemaJson(embeddedSchema, typeId);
            jto.typeDeclaration.type.classifier = "SchemaType";
         }
      }

      return jto;
   }

   public ModelParticipantJto toJto(Resource resource)
   {
      ModelParticipantJto jto = newModelElementJto(resource, new ModelParticipantJto());

      loadDescription(resource, jto);
      loadExtensions(resource, jto);

      JsonObject extJson = Bpmn2ExtensionUtils.getExtensionAsJson(resource, "core");
      if (extJson.has(ModelerConstants.PARTICIPANT_TYPE_PROPERTY))
      {
         jto.type = extJson.get(ModelerConstants.PARTICIPANT_TYPE_PROPERTY).getAsString();
         if ( !isEmpty(jto.participantType))
         {
            // TODO review
            jto.participantType = jto.type;
         }
      }

      if (isEmpty(jto.type))
      {
         jto.type = ModelerConstants.ROLE_PARTICIPANT_TYPE_KEY;
      }

      if (extJson.has(ModelerConstants.PARENT_UUID_PROPERTY))
      {
         jto.parentUUID = extJson.get(ModelerConstants.PARENT_UUID_PROPERTY).getAsString();

         EObject parentResource = bpmn2Binding.findElementByUuid(
               findContainingModel(resource), jto.parentUUID);
         if (parentResource instanceof Resource)
         {
            JsonObject parentExtJson = Bpmn2ExtensionUtils.getExtensionAsJson(
                  (Resource) parentResource, "core");
            if (parentExtJson.has(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY)
                  && extractAsString(parentExtJson,
                        ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY).endsWith(jto.id))
            {
               jto.type = ModelerConstants.TEAM_LEADER_TYPE_KEY;
            }
         }
      }

      if (extJson.has(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY))
      {
         jto.teamLeadFullId = extJson.get(ModelerConstants.TEAM_LEAD_FULL_ID_PROPERTY).getAsString();
      }

      Definitions model = findContainingModel(resource);
      if (null != model)
      {
         for (RootElement rootElement : model.getRootElements())
         {
            if (rootElement instanceof Resource)
            {
               JsonObject extJson2 = Bpmn2ExtensionUtils.getExtensionAsJson(rootElement, "core");
               if (extJson2.has(ModelerConstants.PARENT_UUID_PROPERTY)
                     && jto.uuid.equals(extJson2.get(ModelerConstants.PARENT_UUID_PROPERTY).getAsString()))
               {
                  ModelParticipantJto childParticipant = toJto((Resource) rootElement);
                  jto.childParticipants.add(childParticipant);
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

   public ApplicationJto toJto(Interface application)
   {
      ApplicationJto jto = newModelElementJto(application, new ApplicationJto());

      loadExtensions(application, jto);

      // TODO resolve app type from model element
      jto.applicationType = ModelerConstants.WEB_SERVICE_APPLICATION_TYPE_ID;

      return jto;
   }

   public ProcessDefinitionJto toJto(Process process)
   {
      ProcessDefinitionJto jto = newModelElementJto(process, new ProcessDefinitionJto());

      loadDescription(process, jto);
      loadExtensions(process, jto);

      for (FlowElement flowElement : process.getFlowElements())
      {
         // TODO
         if (flowElement instanceof Activity)
         {
            jto.activities.put(flowElement.getId(), toJto((Activity) flowElement));
         }
         else if (flowElement instanceof Gateway)
         {
            jto.gateways.put(flowElement.getId(), toJto((Gateway) flowElement));
         }
         else if (flowElement instanceof Event)
         {
            jto.events.put(flowElement.getId(), toJto((Event) flowElement));
         }
         else if (flowElement instanceof SequenceFlow)
         {
            jto.controlFlows.put(flowElement.getId(), toJto((SequenceFlow) flowElement));
         }
      }

      return jto;
   }

   public DataJto toJto(DataStore variable)
   {
      DataJto jto = newModelElementJto(variable, new DataJto());

      toJto(variable, jto);

      return jto;
   }

   public DataJto toJto(DataObject variable)
   {
      DataJto jto = newModelElementJto(variable, new DataJto());

      toJto(variable, jto);

      return jto;
   }

   private DataJto toJto(ItemAwareElement variable, DataJto jto)
   {
      loadDescription(variable, jto);
      loadExtensions(variable, jto);

      if (null != variable.getItemSubjectRef())
      {
         // TODO
         jto.dataType = ModelerConstants.STRUCTURED_DATA_TYPE_KEY;
         jto.structuredDataTypeFullId = findContainingModel(variable).getId() + ":"
               + variable.getItemSubjectRef().getId();
      }
      else
      {
         JsonObject extJson = Bpmn2ExtensionUtils.getExtensionAsJson(variable, "core");
         if (extJson.has(ModelerConstants.DATA_TYPE_PROPERTY))
         {
            jto.dataType = extJson.get(ModelerConstants.DATA_TYPE_PROPERTY).getAsString();
            if ( !isEmpty(jto.dataType))
            {
               if (ModelerConstants.PRIMITIVE_DATA_TYPE_KEY.equals(jto.dataType))
               {
                  jto.primitiveDataType = extractAsString(extJson,
                        ModelerConstants.PRIMITIVE_DATA_TYPE_PROPERTY);
               }
               else if (ModelerConstants.STRUCTURED_DATA_TYPE_KEY.equals(jto.dataType))
               {
                  jto.structuredDataTypeFullId = "";
               }
            }
         }
      }
      return jto;
   }

   public ActivityJto toJto(Activity activity)
   {
      ActivityJto jto = newModelElementJto(activity, new ActivityJto());

      loadDescription(activity, jto);
      loadExtensions(activity, jto);

//      if (activity instanceof NoneTask)
//      {
//         jto.activityType = ModelerConstants.TASK_ACTIVITY;
//         jto.taskType = ModelerConstants.NONE_TASK_KEY;
//      }
//      else
      if (activity instanceof ManualTask)
      {
         jto.activityType = ModelerConstants.TASK_ACTIVITY;
         jto.taskType = ModelerConstants.MANUAL_TASK_KEY;
      }
      else if (activity instanceof UserTask)
      {
         jto.activityType = ModelerConstants.TASK_ACTIVITY;
         jto.taskType = ModelerConstants.USER_TASK_KEY;
      }
      else if (activity instanceof ServiceTask)
      {
         jto.activityType = ModelerConstants.TASK_ACTIVITY;
         jto.taskType = ModelerConstants.SERVICE_TASK_KEY;
      }
      else if (activity instanceof ScriptTask)
      {
         jto.activityType = ModelerConstants.TASK_ACTIVITY;
         jto.taskType = ModelerConstants.SCRIPT_TASK_KEY;
      }
      else if (activity instanceof SendTask)
      {
         jto.activityType = ModelerConstants.TASK_ACTIVITY;
         jto.taskType = ModelerConstants.SEND_TASK_KEY;
      }
      else if (activity instanceof ReceiveTask)
      {
         jto.activityType = ModelerConstants.TASK_ACTIVITY;
         jto.taskType = ModelerConstants.RECEIVE_TASK_KEY;
      }
//      else if (activity instanceof RuleTask)
//      {
//         jto.activityType = ModelerConstants.TASK_ACTIVITY;
//         jto.taskType = ModelerConstants.RULE_TASK_KEY;
//      }
      else if (activity instanceof SubProcess)
      {
         SubProcess subProcess = (SubProcess) activity;

         jto.activityType = ModelerConstants.SUBPROCESS_ACTIVITY;

         // jto.subprocessFullId = subProcess.get;
      }

      if ( !activity.getResources().isEmpty())
      {
         for (ResourceRole resourceRole : activity.getResources())
         {
            if (resourceRole instanceof Performer)
            {
               jto.participantFullId = encodeReference(resourceRole.getResourceRef());
               break;
            }
         }
      }

      return jto;
   }

   public GatewayJto toJto(Gateway gateway)
   {
      GatewayJto jto = newModelElementJto(gateway, new GatewayJto());

      loadDescription(gateway, jto);
      loadExtensions(gateway, jto);

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

      loadDescription(event, jto);
      loadExtensions(event, jto);

      if (event instanceof StartEvent)
      {
         StartEvent startEvent = (StartEvent) event;
         jto.eventType = ModelerConstants.START_EVENT;
         jto.eventClass = encodeEventClass(startEvent.getEventDefinitions());
         jto.throwing = false;
         jto.interrupting = startEvent.isIsInterrupting();
      }
      else if (event instanceof IntermediateCatchEvent)
      {
         IntermediateCatchEvent intermediateCatchEvent = (IntermediateCatchEvent) event;
         jto.eventType = ModelerConstants.INTERMEDIATE_EVENT;
         jto.eventClass = encodeEventClass(intermediateCatchEvent.getEventDefinitions());
         jto.throwing = false;
      }
      else if (event instanceof IntermediateThrowEvent)
      {
         IntermediateThrowEvent intermediateThrowEvent = (IntermediateThrowEvent) event;
         jto.eventType = ModelerConstants.INTERMEDIATE_EVENT;
         jto.eventClass = encodeEventClass(intermediateThrowEvent.getEventDefinitions());
         jto.throwing = true;
      }
      else if (event instanceof BoundaryEvent)
      {
         BoundaryEvent boundaryEvent = (BoundaryEvent) event;

         jto.eventType = ModelerConstants.INTERMEDIATE_EVENT;
         jto.eventClass = encodeEventClass(boundaryEvent.getEventDefinitions());

         // TODO Temporary
         if (boundaryEvent.getAttachedToRef() != null)
         {
            jto.bindingActivityUuid = boundaryEvent.getAttachedToRef().getId();
         }

         jto.interrupting = boundaryEvent.isCancelActivity();
         jto.throwing = false;
      }
      else if (event instanceof EndEvent)
      {
         EndEvent endEvent = (EndEvent) event;
         jto.eventType = ModelerConstants.STOP_EVENT;
         jto.eventClass = encodeEventClass(endEvent.getEventDefinitions());
         jto.throwing = true;
      }

      return jto;
   }

   public TransitionJto toJto(SequenceFlow sFlow)
   {
      TransitionJto jto = newModelElementJto(sFlow, new TransitionJto());

      loadDescription(sFlow, jto);
      loadExtensions(sFlow, jto);

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

   public String getFullId(BaseElement element)
   {
      Definitions model = Bpmn2Utils.findContainingModel(element);
      if (null != model)
      {
         return bpmn2Binding.getModelId(model) + ":" + element.getId();
      }

      return null;
   }

   /**
    *
    * @param <T>
    * @param <J>
    * @param src
    * @param jto
    * @return
    */
   public <T extends BaseElement, J extends ModelElementJto> J newModelElementJto(T src,
         J jto)
   {
      jto.uuid = bpmn2Binding.findUuid(src);
      jto.id = src.getId();

      String name;

      if (src instanceof ItemDefinition)
      {
         name = ((ItemDefinition) src).getId();
      }
      else if (src instanceof Interface)
      {
         name = ((Interface) src).getId();
      }
      else if (src instanceof Resource)
      {
         name = ((Resource) src).getName();
      }
      else if (src instanceof Participant)
      {
         name = ((Participant) src).getName();
      }
      else if (src instanceof DataStore)
      {
         name = ((DataStore) src).getName();
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
         jto.modelId = bpmn2Binding.getModelId(model);
         jto.modelUUID = bpmn2Binding.getModelId(model);
      }

      return jto;
   }

   /**
    *
    * @param <T>
    * @param <J>
    * @param shape
    * @param jto
    * @return
    */
   public <T extends Shape, J extends ShapeJto> J newShapeJto(BPMNShape shape, J jto)
   {
      jto.oid = bpmn2Binding.findOid(shape);

      Bounds bounds = shape.getBounds();
      jto.x = (int) bounds.getX();
      jto.y = (int) bounds.getY();
      jto.width = (int) bounds.getWidth();
      jto.height = (int) bounds.getHeight();

      return jto;
   }

   /**
    *
    * @param <T>
    * @param <J>
    * @param edge
    * @param jto
    * @return
    */
   public <T extends Edge, J extends ConnectionSymbolJto> J newEdgeJto(BPMNEdge edge,
         J jto)
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

   /**
    *
    * @param event
    * @return
    */
   public String encodeEventClass(List<EventDefinition> eventDefinitions)
   {
      Set<String> eventClasses = newHashSet();

      for (EventDefinition eventDefinition : eventDefinitions)
      {
         if (eventDefinition instanceof TimerEventDefinition)
         {
            eventClasses.add(ModelerConstants.TIMER_EVENT_CLASS_KEY);
         }
         else if (eventDefinition instanceof MessageEventDefinition)
         {
            eventClasses.add(ModelerConstants.MESSAGE_EVENT_CLASS_KEY);
         }
         else if (eventDefinition instanceof ErrorEventDefinition)
         {
            eventClasses.add(ModelerConstants.ERROR_EVENT_CLASS_KEY);
         }
         else
         {
            // TODO more event classes ...
         }
      }
      return (1 == eventClasses.size()) ? eventClasses.iterator().next() : null; // TODO
                                                                                 // "complex"
                                                                                 // instead
                                                                                 // of
                                                                                 // null;
   }

   /**
    *
    * @param node
    * @return
    */
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

   /**
    *
    * @param fromShape
    * @param point
    * @param point2
    * @return
    */
   private int determineAnchorPoint(BPMNShape fromShape, Point point, Point point2)
   {
      Bounds fromBounds = fromShape.getBounds(); // determineShapeBounds(fromShape);

      double dx = point.getX() - (fromBounds.getX() + fromBounds.getWidth() / 2.0);
      double dy = point.getY() - (fromBounds.getY() + fromBounds.getHeight() / 2.0);

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

   /**
    *
    * @param name
    * @param id
    * @return
    */
   private static String nameOrId(String name, String id)
   {
      return !isEmpty(name) ? name : id;
   }

   /**
    *
    * @param element
    * @param jto
    */
   private void loadDescription(BaseElement element, ModelElementJto jto)
   {
      Documentation description = Bpmn2ExtensionUtils.getDescription(element);

      if (description != null)
      {
         jto.description = description.getText();
      }
   }

   /**
    *
    * @param element
    * @param jto
    */
   private void loadExtensions(BaseElement element, ModelElementJto jto)
   {
      JsonElement attributes = Bpmn2ExtensionUtils.getExtensionAsJson(element, "core")
            .get(ModelerConstants.ATTRIBUTES_PROPERTY);

      if (attributes != null)
      {
         jto.attributes = attributes.getAsJsonObject();
      }
      else
      {
         jto.attributes = new JsonObject();
      }

   }
}
