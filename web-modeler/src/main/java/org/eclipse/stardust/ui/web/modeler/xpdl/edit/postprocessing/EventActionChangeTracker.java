package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.EventActionType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.ui.web.modeler.edit.postprocessing.AbstractChangeTracker;

@Component
public class EventActionChangeTracker extends AbstractChangeTracker
{
   @Override
   protected void inspectChange(Modification change, EObject candidate)
   {
      if ((candidate instanceof EventActionType))
      {
         EventActionType action = (EventActionType) candidate;
         if (action.getType().getId().equals(PredefinedConstants.EXCLUDE_USER_ACTION))
         {
            if (action.eContainer() instanceof EventHandlerType)
            {
               EventHandlerType handler = (EventHandlerType) action.eContainer();
               change.markUnmodified(handler);
               if (handler.eContainer() instanceof ActivityType)
               {
                  ActivityType activity = (ActivityType) handler.eContainer();
                  change.markAlsoModified(activity);
               }
            }
         }
      }
   }
}
