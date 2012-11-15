package org.eclipse.stardust.ui.web.modeler.bpmn2.edit.postprocessing;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ExtensionAttributeValue;
import org.eclipse.bpmn2.ExtensionDefinition;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Lane;
import org.eclipse.bpmn2.LaneSet;
import org.eclipse.bpmn2.Participant;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.di.BPMNPlane;
import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.dd.di.Diagram;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xml.type.AnyType;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.common.Pair;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

@Service
public class ChangesetNormalizer implements ChangePostprocessor
{

   @Override
   public int getInspectionPhase()
   {
      return 1;
   }

   @Override
   public void inspectChange(Modification change)
   {
      // modified
      for (EObject element : change.getModifiedElements())
      {
         if (null == change.findContainer(element, Definitions.class))
         {
            // no BPMN2 element
            continue;
         }
         Pair<InspectionQualifier, EObject> inspectionResult = inspectModification(element);
         if ((null == inspectionResult) || (InspectionQualifier.Accept == inspectionResult.getFirst()))
         {
            continue;
         }
         else if (InspectionQualifier.Ignore == inspectionResult.getFirst())
         {
            change.markUnmodified(element);
         }
         else if (InspectionQualifier.ModifyInstead == inspectionResult.getFirst())
         {
            change.markAlsoModified(inspectionResult.getSecond());
            change.markUnmodified(element);
         }
      }
      // added
      for (EObject candidate : change.getAddedElements())
      {
         if ((null != change.findContainer(candidate, Definitions.class))
               && !isModelOrModelElement(candidate))
         {
            if ( !isIgnoredElement(candidate))
            {
               // report any change to a non-element sub-object as modification of the
               // containing parent element
               EObject changedElement = determineChangedElement(candidate);
               if ((candidate != changedElement)
                     && !(change.getAddedElements().contains(changedElement) || change.getRemovedElements()
                           .contains(changedElement)))
               {
                  change.markAlsoModified(changedElement);
               }
            }
            change.markUnmodified(candidate);
         }
      }
      // removed objects will automatically be reported as modifications of their
      // container (TODO check multi level containment)

      // removed
      for (EObject candidate : change.getRemovedElements())
      {
         if ((null != change.findContainer(candidate, Definitions.class))
               && !isModelOrModelElement(candidate))
         {
            change.markUnmodified(candidate);
         }
      }
   }

   private boolean isIgnoredElement(EObject element)
   {
      return (element instanceof LaneSet)
            || (element instanceof Collaboration)
            || ((element instanceof Participant) && (null != ((Participant) element).getProcessRef()));
   }

   public Pair<InspectionQualifier, EObject> inspectModification(EObject element)
   {
      // element whitelist
      if ((element instanceof Process) || (element instanceof FlowElement))
      {
         return ACCEPT;
      }
      else if ((element instanceof BPMNPlane) && (null != ((BPMNPlane) element).getOwningDiagram()))
      {
         return modifyInstead(((BPMNPlane) element).getOwningDiagram());
      }
      else if (element instanceof Lane)
      {
         // TODO find referring lane symbol and mark it as modified
         return IGNORE;
      }
      else if (element instanceof DiagramElement)
      {
         if ((element instanceof BPMNShape) || (element instanceof BPMNEdge))
         {
            return IGNORE;
         }
      }
      else if ((element instanceof ExtensionDefinition)
            || (element instanceof ExtensionAttributeValue)
            || (element instanceof AnyType))
      {
         BaseElement attributeContainer = Bpmn2Utils.findContainer(element, BaseElement.class);
         if (null != attributeContainer)
         {
            // TODO perform full "modified instead" resolution for container, too
            return modifyInstead(attributeContainer);
         }
         else
         {
            return IGNORE;
         }
      }
      else if (isIgnoredElement(element))
      {
         return IGNORE;
      }

      return ACCEPT;
   }

   public EObject determineChangedElement(EObject changedObject)
   {
      EObject element = changedObject;
      while ((null != element) && !isModelOrModelElement(element))
      {
         element = element.eContainer();
      }
      return (null != element) ? element : changedObject;
   }

   private boolean isModelOrModelElement(EObject changedObject)
   {
      return (changedObject instanceof Definitions)
            || (changedObject instanceof Process)
            || (changedObject instanceof FlowElement)
            || (changedObject instanceof Diagram)
            || (changedObject instanceof DiagramElement);
   }

   enum InspectionQualifier
   {
      Accept,
      Ignore,
      ModifyInstead,
   }

   private static final Pair<InspectionQualifier, EObject> ACCEPT = new Pair<InspectionQualifier, EObject>(
         InspectionQualifier.Accept, null);

   private static final Pair<InspectionQualifier, EObject> IGNORE = new Pair<InspectionQualifier, EObject>(
         InspectionQualifier.Ignore, null);

   private static final Pair<InspectionQualifier, EObject> modifyInstead(EObject element)
   {
      return new Pair<InspectionQualifier, EObject>(InspectionQualifier.ModifyInstead, element);
   }
}
