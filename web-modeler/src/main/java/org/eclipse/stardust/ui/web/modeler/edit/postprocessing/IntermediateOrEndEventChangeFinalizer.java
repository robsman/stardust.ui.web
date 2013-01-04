package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils.isEndEventHost;
import static org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils.isIntermediateEventHost;

import java.util.Iterator;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.AbstractEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.AttributeType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.eclipse.stardust.ui.web.modeler.marshaling.EventMarshallingUtils;

@Component
public class IntermediateOrEndEventChangeFinalizer implements ChangePostprocessor
{
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

   private void inspectChange(Modification change, EObject candidate)
   {
      if (candidate instanceof ActivityType)
      {
         processEventChange(change, (ActivityType) candidate);
      }
      else if (candidate instanceof EventHandlerType)
      {
         if (null != change.findContainer(candidate, ActivityType.class))
         {
            EventHandlerType eventHandler = (EventHandlerType) candidate;
            Long eventSymbolOid = EventMarshallingUtils.resolveHostedEvent(eventHandler);
            if (null != eventSymbolOid)
            {
               ProcessDefinitionType containingProcess = change.findContainer(
                     eventHandler, ProcessDefinitionType.class);
               if (markEventSymbolModified(eventSymbolOid, containingProcess, change))
               {
                  change.markUnmodified(candidate);
               }
            }
         }
      }
      else
      {
         AttributeType changedAttr = change.findContainer(candidate, AttributeType.class);
         if (null != changedAttr)
         {
            processEventChange(change, changedAttr);
         }
      }
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
