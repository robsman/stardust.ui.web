package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.xpdl.marshalling.EventMarshallingUtils.isEndEventHost;
import static org.eclipse.stardust.ui.web.modeler.xpdl.marshalling.EventMarshallingUtils.isIntermediateEventHost;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.util.FeatureMap;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.stardust.ui.web.modeler.xpdl.marshalling.EventMarshallingUtils;

import org.springframework.stereotype.Component;

@Component
public class IntermediateOrEndEventChangeFinalizer implements ChangePostprocessor
{
   private static final EStructuralFeature ID_FEATURE = CarnotWorkflowModelPackage.eINSTANCE.getIIdentifiableElement_Id();

   @Override
   public int getInspectionPhase()
   {
      return 10;
   }

   @Override
   public void inspectChange(Modification change)
   {
      // needs to go really low level as extended attributes will normally be hidden and
      // reported as change of owner
      for (EObject candidate : change.getChangeDescription().getObjectChanges().keySet())
      {
         inspectChange(change, candidate);
      }
      for (EObject candidate : change.getChangeDescription().getObjectsToDetach())
      {
         inspectChange(change, candidate);
      }
      for (EObject candidate : change.getChangeDescription().getObjectsToAttach())
      {
         inspectChange(change, candidate);
      }
   }

   private void inspectChange(Modification modification, EObject candidate)
   {
      if (candidate instanceof ActivityType)
      {
         processEventChange(modification, (ActivityType) candidate);
      }
      else if (candidate instanceof EventHandlerType)
      {
         if (null != modification.findContainer(candidate, ActivityType.class))
         {
            EventHandlerType eventHandler = (EventHandlerType) candidate;
            Object changedValue = getChangedValue(modification, eventHandler, ID_FEATURE);
            if (changedValue != null && !changedValue.equals(eventHandler.getId()))
            {
               ActivityType activity = ModelUtils.findContainingActivity(eventHandler);
               if (activity != null)
               {
                  String criteria = "ON_BOUNDARY_EVENT(" + changedValue + ')';
                  for (TransitionType transition : activity.getOutTransitions())
                  {
                     FeatureMap mixedNode = transition.getExpression().getMixed();
                     String expression = ModelUtils.getCDataString(mixedNode);
                     if (criteria.equals(expression))
                     {
                        ModelUtils.setCDataString(mixedNode, "ON_BOUNDARY_EVENT(" + eventHandler.getId() + ')', true);
                        for (TransitionConnectionType connection : transition.getTransitionConnections())
                        {
                           modification.markAlsoModified(connection);
                        }
                        break;
                     }
                  }
               }
               // TODO: update transition
            }
            Long eventSymbolOid = EventMarshallingUtils.resolveHostedEvent(eventHandler);
            if (null != eventSymbolOid)
            {
               ProcessDefinitionType containingProcess = modification.findContainer(
                     eventHandler, ProcessDefinitionType.class);
               if (markEventSymbolModified(eventSymbolOid, containingProcess, modification))
               {
                  modification.markUnmodified(candidate);
               }
            }
         }
      }
      else
      {
         AttributeType changedAttr = modification.findContainer(candidate, AttributeType.class);
         if (null != changedAttr)
         {
            processEventChange(modification, changedAttr);
         }
      }
   }

   private Object getChangedValue(Modification modification, EObject candidate, EStructuralFeature feature)
   {
      ChangeDescription desc = modification.getChangeDescription();
      EMap<EObject, EList<FeatureChange>> objectChanges = desc.getObjectChanges();
      if (objectChanges != null)
      {
         EList<FeatureChange> changes = objectChanges.get(candidate);
         if (changes != null)
         {
            for (FeatureChange change : changes)
            {
               if (feature.equals(change.getFeature()))
               {
                  return change.getValue();
               }
            }
         }
      }
      return null;
   }

   private boolean processEventChange(Modification change, ActivityType hostActivity)
   {
      if (isIntermediateEventHost(hostActivity) || isEndEventHost(hostActivity))
      {
         for (long eventSymbolOid : EventMarshallingUtils.resolveHostedEvents(hostActivity))
         {
            ProcessDefinitionType containingProcess = change.findContainer(hostActivity,
                  ProcessDefinitionType.class);
            if (markEventSymbolModified(eventSymbolOid, containingProcess, change))
            {
               change.markUnmodified(hostActivity);
               return true;
            }
         }
      }

      return false;
   }

   private void processEventChange(Modification change, AttributeType attr)
   {
      if ( !isEmpty(attr.getName())
            && attr.getName().startsWith(EventMarshallingUtils.PREFIX_HOSTED_EVENT))
      {
         ActivityType hostActivity = change.findContainer(attr, ActivityType.class);
         if ((null != hostActivity))
         {
            long symbolOid = Long.valueOf(attr.getName().substring(
                  EventMarshallingUtils.PREFIX_HOSTED_EVENT.length() + 1));
            ProcessDefinitionType containingProcess = change.findContainer(hostActivity,
                  ProcessDefinitionType.class);
            markEventSymbolModified(symbolOid, containingProcess, change);
         }
      }
   }

   private static boolean markEventSymbolModified(long symbolOid,
         ProcessDefinitionType containingProcess, Modification change)
   {
      for (DiagramType diagram : containingProcess.getDiagram())
      {
         for (Iterator<EObject> i = diagram.eAllContents(); i.hasNext();)
         {
            EObject symbolCandidate = i.next();
            if ((symbolCandidate instanceof AbstractEventSymbol)
                  && (symbolOid == ((AbstractEventSymbol) symbolCandidate).getElementOid()))
            {
               change.markAlsoModified(symbolCandidate);
               return true;
            }
         }
      }

      return false;
   }
}
