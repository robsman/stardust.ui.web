package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2DcFactory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2DiFactory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.createInternalId;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.di.Diagram;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.dd.di.Edge;
import org.eclipse.dd.di.Shape;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.model.di.ConnectionSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.LaneSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.NodeSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.PoolSymbolJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ShapeJto;

public class Bpmn2DiBuilder
{
   public BPMNDiagram createDiagram(EObject process, ProcessDiagramJto jto)
   {
      assert (process instanceof Process);
      // ensure it is a top-level process
      assert (process.eContainer() instanceof Definitions);

      Definitions model = (Definitions) process.eContainer();

      BPMNDiagram diagram = bpmn2DiFactory().createBPMNDiagram();

      diagram.setName(jto.name);
      diagram.setId( !isEmpty(jto.id)
            ? jto.id
            : createInternalId());

      // apply defaults
      diagram.setResolution(72F);

      // create plane and connect it to the process' default collaboration (to support pools)
      diagram.setPlane(bpmn2DiFactory().createBPMNPlane());
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
         poolElement = bpmn2Factory().createParticipant();
         poolElement.setId(createInternalId());
         poolElement.setProcessRef((Process) process);

         Collaboration collab = bpmn2Factory().createCollaboration();
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

   public void attachDiagram(Definitions model, BPMNDiagram diagram)
   {
      assert (null == diagram.eContainer());

      model.getDiagrams().add(diagram);
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

   public <T extends ShapeJto> BPMNShape createNodeSymbol(Definitions model, T jto,
         EObject modelElement)
   {
      BPMNShape shape = null;
      if (jto instanceof PoolSymbolJto)
      {
         if ((modelElement instanceof Participant) && (null != ((Participant) modelElement).getProcessRef()))
         {
            shape = bpmn2DiFactory().createBPMNShape();

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
               LaneSet laneSet = bpmn2Factory().createLaneSet();
               laneSet.setId(createInternalId());
               laneSet.setName("Default");
               process.getLaneSets().add(laneSet);
            }

            LaneSet laneSet = process.getLaneSets().get(0);

            Lane lane = bpmn2Factory().createLane();
            lane.setId(createInternalId());
            lane.setName(laneJto.name);

            laneSet.getLanes().add(lane);

            return createNodeSymbol(model, laneJto, lane);
         }
         else if (modelElement instanceof Lane)
         {
            shape = bpmn2DiFactory().createBPMNShape();

            shape.setId(createInternalId());
            shape.setBpmnElement((Lane) modelElement);

            // apply lane defaults
            shape.setIsHorizontal(true);
         }
      }
      else if (jto instanceof NodeSymbolJto<?>)
      {
         shape = bpmn2DiFactory().createBPMNShape();

         shape.setId(createInternalId());
         shape.setBpmnElement((FlowElement) modelElement);
      }
      // TODO more elements

      if (null != shape)
      {
         // TODO register OID
//         findOid(model, shape);

         shape.setBounds(bpmn2DcFactory().createBounds());
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

   public BPMNEdge createConnectionSymbol(Definitions model, ConnectionSymbolJto jto, BaseElement modelElement)
   {
      BPMNEdge edge = bpmn2DiFactory().createBPMNEdge();

      edge.setBpmnElement(modelElement);

      return edge;
   }
}
