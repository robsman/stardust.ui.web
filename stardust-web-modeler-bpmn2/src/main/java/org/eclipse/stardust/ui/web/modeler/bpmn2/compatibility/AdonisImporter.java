package org.eclipse.stardust.ui.web.modeler.bpmn2.compatibility;

import static org.eclipse.stardust.common.CollectionUtils.newHashMap;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2DcFactory;

import java.util.Map;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.dc.Bounds;
import org.eclipse.dd.dc.Point;
import org.eclipse.dd.di.Diagram;
import org.eclipse.dd.di.DiagramElement;

public class AdonisImporter
{
   public void fixModel(Definitions model)
   {
      for (Diagram diagram : model.getDiagrams())
      {
         if ((diagram instanceof BPMNDiagram) && (null != ((BPMNDiagram) diagram).getPlane()))
         {
            Map<BaseElement, BPMNShape> shapes = newHashMap();
            Map<BPMNShape, Point> oldOrigins = newHashMap();

            for (DiagramElement diagramElement : ((BPMNDiagram) diagram).getPlane().getPlaneElement())
            {
               // pass one: fix shape geometry
               if (diagramElement instanceof BPMNShape)
               {
                  BPMNShape shape = (BPMNShape) diagramElement;

                  shapes.put(shape.getBpmnElement(), shape);

                  Bounds bounds = shape.getBounds();
                  if (shape.getBpmnElement() instanceof Lane)
                  {
                     // TODO review
                     // increase size a little ...
                     if (shape.isIsHorizontal())
                     {
                        bounds.setWidth(bounds.getWidth() + 100.0F);
                     }
                     else
                     {
                        bounds.setHeight(bounds.getHeight() + 100.0F);
                     }
                  }
                  else if (shape.getBpmnElement() instanceof FlowNode)
                  {
                     // record original origin to be able to detect incorrect waypoints
                     Point oldOrigin = bpmn2DcFactory().createPoint();
                     oldOrigin.setX(shape.getBounds().getX());
                     oldOrigin.setY(shape.getBounds().getY());
                     oldOrigins.put(shape, oldOrigin);

                     if (shape.getBpmnElement() instanceof Activity)
                     {
                        if (105.0F == bounds.getWidth())
                        {
                           // fix default width to be multiple of two
                           bounds.setWidth(106.0F);
                        }
                     }
                     else if (shape.getBpmnElement() instanceof Event)
                     {
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
                  }
               }
            }

            for (DiagramElement diagramElement : ((BPMNDiagram) diagram).getPlane().getPlaneElement())
            {
               // pass three: fix waypoints
               if (diagramElement instanceof BPMNEdge)
               {
                  BPMNEdge edge = (BPMNEdge) diagramElement;
                  if (edge.getBpmnElement() instanceof SequenceFlow)
                  {
                     DiagramElement sourceShape = edge.getSourceElement();
                     if (null == sourceShape)
                     {
                        sourceShape = shapes.get(((SequenceFlow) edge.getBpmnElement()).getSourceRef());
                     }
                     if (sourceShape instanceof BPMNShape)
                     {
                        Point oldSourceOrigin = oldOrigins.get(sourceShape);

                        Point startPoint = edge.getWaypoint().get(0);
                        if ((oldSourceOrigin.getX() == startPoint.getX()) && (oldSourceOrigin.getY()) == startPoint.getY())
                        {
                           Bounds sourceBounds = ((BPMNShape) sourceShape).getBounds();
                           startPoint.setX(sourceBounds.getX() + sourceBounds.getWidth() / 2.0F);
                           startPoint.setY(sourceBounds.getY() + sourceBounds.getHeight() / 2.0F);
                        }
                     }

                     DiagramElement targetShape = edge.getTargetElement();
                     if (null == targetShape)
                     {
                        targetShape = shapes.get(((SequenceFlow) edge.getBpmnElement()).getTargetRef());
                     }
                     if (targetShape instanceof BPMNShape)
                     {
                        Point oldTargetOrigin = oldOrigins.get(targetShape);
                        Point endPoint = edge.getWaypoint().get(edge.getWaypoint().size() - 1);
                        if ((oldTargetOrigin.getX() == endPoint.getX()) && (oldTargetOrigin.getY()) == endPoint.getY())
                        {
                           Bounds targetBounds = ((BPMNShape) targetShape).getBounds();
                           endPoint.setX(targetBounds.getX() + targetBounds.getWidth() / 2.0F);
                           endPoint.setY(targetBounds.getY() + targetBounds.getHeight() / 2.0F);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
