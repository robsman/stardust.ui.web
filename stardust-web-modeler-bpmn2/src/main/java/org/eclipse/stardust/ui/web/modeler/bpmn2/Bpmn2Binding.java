package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.CollectionUtils.newConcurrentHashMap;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.createInternalId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Bpmn2Package;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ProcessType;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.bpmn2.di.BpmnDiFactory;
import org.eclipse.dd.dc.DcFactory;
import org.eclipse.dd.di.Diagram;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.dd.di.Edge;
import org.eclipse.dd.di.Shape;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelElementJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;
import org.eclipse.stardust.ui.web.modeler.model.TransitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ConnectionSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.LaneSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.NodeSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.PoolSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ShapeJto;
import org.eclipse.stardust.ui.web.modeler.spi.ModelBinding;

public class Bpmn2Binding extends ModelBinding<Definitions>
{
   private static final Bpmn2Package PKG_BPMN2 = Bpmn2Package.eINSTANCE;

   private static final Bpmn2Factory F_BPMN2 = Bpmn2Factory.eINSTANCE;

   private static final BpmnDiFactory F_BPMN2DI = BpmnDiFactory.eINSTANCE;

   private static final DcFactory F_BPMN2DC = DcFactory.eINSTANCE;

   private AtomicLong oidGenerator = new AtomicLong(1L);

   private final ConcurrentMap<Definitions, ConcurrentMap<EObject, String>> uuidRegistry = newConcurrentHashMap();

   private final ConcurrentMap<Definitions, ConcurrentMap<EObject, Long>> oidRegistry = newConcurrentHashMap();

   public Bpmn2Binding()
   {
      super(new Bpmn2Navigator(), new Bpmn2ModelMarshaller(),
            new Bpmn2ModelUnmarshaller());

      ((Bpmn2Navigator) navigator).setBinding(this);
      ((Bpmn2ModelMarshaller) marshaller).setBinding(this);
      ((Bpmn2ModelUnmarshaller) unmarshaller).setBinding(this);
   }

   @Override
   public boolean isCompatible(EObject model)
   {
      return model instanceof Definitions;
   }

   @Override
   public String getModelId(Definitions model)
   {
      return ((Definitions) model).getId();
   }

   public String findUuid(BaseElement element)
   {
      EObject context = element;
      do
      {
         if (context instanceof Definitions)
         {
            return findUuid((Definitions) context, element);
         }
         else
         {
            context = context.eContainer();
         }
      }
      while (context != null);

      throw new IllegalArgumentException("Element must be part of a BPMN2 model.");
   }

   public String findUuid(Definitions model, EObject element)
   {
      if (null == element)
      {
         throw new IllegalArgumentException("Element must not be null");
      }

      ConcurrentMap<EObject, String> modelUuids = uuidRegistry.get(model);
      if (null == modelUuids)
      {
         uuidRegistry.putIfAbsent(model, new ConcurrentHashMap<EObject, String>());
         modelUuids = uuidRegistry.get(model);
      }
      if ( !modelUuids.containsKey(element))
      {
         modelUuids.putIfAbsent(element, Bpmn2Utils.createInternalId());
      }

      return modelUuids.get(element);
   }

   public EObject findElementByUuid(Definitions model, String uuid)
   {
      if (null == model)
      {
         throw new IllegalArgumentException("Model must not be null");
      }

      Map<EObject, String> modelUuids = uuidRegistry.get(model);
      if (null != modelUuids)
      {
         for (Map.Entry<EObject, String> entry : modelUuids.entrySet())
         {
            if (uuid.equals(entry.getValue()))
            {
               return entry.getKey();
            }
         }
      }

      return null;
   }

   public Long findOid(BaseElement element)
   {
      EObject context = element;
      do
      {
         if (context instanceof Definitions)
         {
            return findOid((Definitions) context, element);
         }
         else
         {
            context = context.eContainer();
         }
      }
      while (context != null);

      throw new IllegalArgumentException("Element must be part of a BPMN2 model.");
   }

   public Long findOid(DiagramElement element)
   {
      EObject context = element;
      do
      {
         if (context instanceof Definitions)
         {
            return findOid((Definitions) context, element);
         }
         else
         {
            context = context.eContainer();
         }
      }
      while (context != null);

      throw new IllegalArgumentException("Element must be part of a BPMN2 model.");
   }

   public Long findOid(Definitions model, EObject element)
   {
      if (null == element)
      {
         throw new IllegalArgumentException("Element must not be null");
      }

      ConcurrentMap<EObject, Long> modelOids = oidRegistry.get(model);
      if (null == modelOids)
      {
         oidRegistry.putIfAbsent(model, new ConcurrentHashMap<EObject, Long>());
         modelOids = oidRegistry.get(model);
      }
      if ( !modelOids.containsKey(element))
      {
         modelOids.putIfAbsent(element, oidGenerator.getAndIncrement());
      }

      return modelOids.get(element);
   }

   public EObject findElementByOid(Definitions model, long oid)
   {
      if (null == model)
      {
         throw new IllegalArgumentException("Model must not be null");
      }

      Map<EObject, Long> modelOids = oidRegistry.get(model);
      if (null != modelOids)
      {
         for (Map.Entry<EObject, Long> entry : modelOids.entrySet())
         {
            if (oid == entry.getValue())
            {
               return entry.getKey();
            }
         }
      }

      return null;
   }

   @Override
   public Definitions createModel(ModelJto jto)
   {
      Definitions model = F_BPMN2.createDefinitions();
      model.setName(jto.name);

      model.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      // TODO review, externalize values
      model.setExporter("Eclipse Lightdust");
      model.setExporterVersion("0.0.1");

      // TODO expression language: JavaScript
      // TODO type language: XSD (this is the default, though)

      // TODO verify URL compatibility of ID
      model.setTargetNamespace("http://eclipse.org/stardust/model/" + model.getId());

      return model;
   }

   public Diagram createProcessDiagram(EObject process, ProcessDiagramJto jto)
   {
      assert (process instanceof Process);
      // ensure it is a top-level process
      assert (process.eContainer() instanceof Definitions);

      Definitions model = (Definitions) process.eContainer();

      BPMNDiagram diagram = F_BPMN2DI.createBPMNDiagram();

      diagram.setName(jto.name);
      diagram.setId( !isEmpty(jto.id)
            ? jto.id
            : createInternalId());

      // apply defaults
      diagram.setResolution(72F);

      // create plane and connect it to the process' default collaboration (to support pools)
      diagram.setPlane(F_BPMN2DI.createBPMNPlane());
      diagram.getPlane().setId(createInternalId());

      Participant poolElement = null;
      for (RootElement candidate : model.getRootElements())
      {
         if (candidate instanceof Collaboration)
         {
            Collaboration collab = (Collaboration) candidate;
            if ((1 == collab.getParticipants().size())
                  && (collab.getParticipants().get(0).getProcessRef() == process))
            {
               poolElement = collab.getParticipants().get(0);
               break;
            }
         }
      }
      if (null == poolElement)
      {
         poolElement = F_BPMN2.createParticipant();
         poolElement.setId(createInternalId());
         poolElement.setProcessRef((Process) process);

         Collaboration collab = F_BPMN2.createCollaboration();
         collab.setId(createInternalId());
         model.getRootElements().add(collab);

         collab.getParticipants().add(poolElement);
      }

      diagram.getPlane().setBpmnElement((Collaboration) poolElement.eContainer());

      // create the process' pool
      PoolSymbolJto poolJto = new PoolSymbolJto();
      poolJto.x = 0;
      poolJto.y = 0;
      poolJto.width = 800;
      poolJto.height = 400;
      BPMNShape pool = (BPMNShape) createNodeSymbol(model, poolJto, poolElement);
      diagram.getPlane().getPlaneElement().add(pool);

      // create the default lane
      LaneSymbolJto laneJto = new LaneSymbolJto();
      laneJto.name = "Default";
      laneJto.x = poolJto.x;
      laneJto.y = poolJto.y;
      laneJto.width = poolJto.width;
      laneJto.height = poolJto.height;
      BPMNShape lane = (BPMNShape) createNodeSymbol(model, laneJto, process);
      diagram.getPlane().getPlaneElement().add(lane);

      model.getDiagrams().add(diagram);

      return diagram;
   }

   @Override
   public <T extends ModelElementJto> EObject createModelElement(Definitions model, T jto)
   {
      if (jto instanceof ProcessDefinitionJto)
      {
         return createProcess(model, (ProcessDefinitionJto) jto);
      }
      else if (jto instanceof EventJto)
      {
         return createEvent(model, (EventJto) jto);
      }
      else if (jto instanceof ActivityJto)
      {
         return createActivity(model, (ActivityJto) jto);
      }
      else if (jto instanceof GatewayJto)
      {
         return createGateway(model, (GatewayJto) jto);
      }
      else if (jto instanceof TransitionJto)
      {
         return createSequenceFlow(model, (TransitionJto) jto);
      }

      throw new IllegalArgumentException("Unsupported element: " + jto);
   }

   @Override
   public void attachModelElement(EObject container, EObject modelElement)
   {
      assert (null == modelElement.eContainer());

      if (modelElement instanceof Process)
      {
         if (container instanceof Definitions)
         {
            ((Definitions) container).getRootElements().add((Process) modelElement);
         }
      }
      else if (modelElement instanceof FlowElement)
      {
         if (container instanceof Process)
         {
            ((Process) container).getFlowElements().add((FlowElement) modelElement);
         }
      }

      if (null == modelElement.eContainer())
      {
         throw new IllegalArgumentException("Unsupported container/element combination: " + container.getClass() + "/" + modelElement.getClass());
      }
   }

   @Override
   public <T extends ShapeJto> BPMNShape createNodeSymbol(Definitions model, T jto,
         EObject modelElement)
   {
      BPMNShape shape = null;
      if (jto instanceof PoolSymbolJto)
      {
         if ((modelElement instanceof Participant) && (null != ((Participant) modelElement).getProcessRef()))
         {
            shape = F_BPMN2DI.createBPMNShape();

            shape.setId(createInternalId());
            shape.setBpmnElement((Participant) modelElement);

            // apply pool defaults
            shape.setIsHorizontal(true);

            // TODO bounds?
         }
      }
      else if (jto instanceof LaneSymbolJto)
      {
         LaneSymbolJto laneJto = (LaneSymbolJto) jto;

         if ((modelElement instanceof Process)
               && (1 >= ((Process) modelElement).getLaneSets().size()))
         {
            Process process = (Process) modelElement;

            if (0 == process.getLaneSets().size())
            {
               LaneSet laneSet = F_BPMN2.createLaneSet();
               laneSet.setId(createInternalId());
               laneSet.setName("Default");
               process.getLaneSets().add(laneSet);
            }

            LaneSet laneSet = process.getLaneSets().get(0);

            Lane lane = F_BPMN2.createLane();
            lane.setId(createInternalId());
            lane.setName(laneJto.name);

            laneSet.getLanes().add(lane);

            return createNodeSymbol(model, laneJto, lane);
         }
         else if (modelElement instanceof Lane)
         {
            shape = F_BPMN2DI.createBPMNShape();

            shape.setId(createInternalId());
            shape.setBpmnElement((Lane) modelElement);

            // apply lane defaults
            shape.setIsHorizontal(true);
         }
      }
      else if (jto instanceof NodeSymbolJto<?>)
      {
         shape = F_BPMN2DI.createBPMNShape();

         shape.setId(createInternalId());
         shape.setBpmnElement((FlowElement) modelElement);
      }
      // TODO more elements

      if (null != shape)
      {
         // register OID
         findOid(model, shape);

         shape.setBounds(F_BPMN2DC.createBounds());
         shape.getBounds().setX(jto.x);
         shape.getBounds().setY(jto.y);
         if (null != jto.width)
         {
            shape.getBounds().setWidth(jto.width);
         }
         if (null != jto.height)
         {
            shape.getBounds().setHeight(jto.height);
         }
      }
      else
      {
         throw new IllegalArgumentException("Not yet implemented: " + jto.getClass());
      }

      return shape;
   }

   @Override
   public void attachNodeSymbol(EObject container, EObject nodeSymbol)
   {
      assert (nodeSymbol instanceof BPMNShape);

      attachDiagramElement(container, nodeSymbol);
   }

   public void attachDiagramElement(EObject container, EObject symbol)
   {
      assert (symbol instanceof Shape) || (symbol instanceof Edge);

      Diagram diagram = null;
      if (container instanceof BPMNShape)
      {
         // a lane
         diagram = Bpmn2Utils.findContainingDiagram(container);
      }
      else if (container instanceof Diagram)
      {
         diagram = (Diagram) container;
      }

      if (null == diagram)
      {
         throw new IllegalArgumentException("Unsupported container: " + container);
      }

      ((BPMNPlane) diagram.getRootElement()).getPlaneElement().add((DiagramElement) symbol);
   }

   public Process createProcess(Definitions model, ProcessDefinitionJto jto)
   {
      Process process = F_BPMN2.createProcess();

      process.setName(jto.name);
      process.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.deriveElementIdFromName(jto.name));

      // apply defaults
      process.setProcessType(ProcessType.PRIVATE);
      process.setIsExecutable(true);

      return process;
   }

   private Event createEvent(Definitions model, EventJto jto)
   {
      // TODO Auto-generated method stub
      Event event;
      if (ModelerConstants.START_EVENT.equals(jto.eventType))
      {
         event = F_BPMN2.createStartEvent();
      }
      else if (ModelerConstants.STOP_EVENT.equals(jto.eventType))
      {
         event = F_BPMN2.createEndEvent();
      }
      else
      {
         throw new IllegalArgumentException("Unsupported event type: " + jto.eventType);
      }

      event.setName(jto.name);
      event.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.createInternalId());

      return event;
   }

   private Activity createActivity(Definitions model, ActivityJto jto)
   {
      Activity activity;
      if (isEmpty(jto.activityType))
      {
         activity = F_BPMN2.createTask();
      }
      else if (ModelerConstants.MANUAL_ACTIVITY.equals(jto.activityType))
      {
         activity = F_BPMN2.createUserTask();
      }
      else
      {
         throw new IllegalArgumentException("Unsupported activity type: " + jto.activityType);
      }

      activity.setName(jto.name);
      activity.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.createInternalId());

      return activity;
   }

   private Gateway createGateway(Definitions model, GatewayJto jto)
   {
      Gateway gateway;
      if (ModelerConstants.XOR_GATEWAY_TYPE.equals(jto.gatewayType))
      {
         gateway = F_BPMN2.createInclusiveGateway();
      }
      else if (ModelerConstants.AND_GATEWAY_TYPE.equals(jto.gatewayType))
      {
         gateway = F_BPMN2.createParallelGateway();
      }
      else
      {
         throw new IllegalArgumentException("Unsupported gateway type: " + jto.gatewayType);
      }

      gateway.setName(jto.name);
      gateway.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.createInternalId());

      return gateway;
   }

   public SequenceFlow createSequenceFlow(Definitions model, TransitionJto jto)
   {
      SequenceFlow flow = F_BPMN2.createSequenceFlow();

      flow.setName(jto.name);
      flow.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.createInternalId());

      if ( !isEmpty(jto.conditionExpression))
      {
         flow.setConditionExpression(F_BPMN2.createFormalExpression());
         ((FormalExpression) flow.getConditionExpression()).setBody(jto.conditionExpression);
      }

      return flow;
   }

   public BPMNEdge createConnectionSymbol(Definitions model, ConnectionSymbolJto jto, BaseElement modelElement)
   {
      BPMNEdge edge = F_BPMN2DI.createBPMNEdge();

      edge.setBpmnElement(modelElement);

      return edge;
   }
}
