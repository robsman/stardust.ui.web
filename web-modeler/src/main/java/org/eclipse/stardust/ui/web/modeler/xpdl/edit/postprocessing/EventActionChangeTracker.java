package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.edit.postprocessing.AbstractChangeTracker;

@Component
public class EventActionChangeTracker extends AbstractChangeTracker
{
   @Override
   protected void inspectChange(Modification change, EObject candidate)
   {
      if ((candidate instanceof EventActionTypeType || candidate instanceof EventConditionTypeType))
      {
         change.markUnmodified(candidate);
      }
      if (candidate instanceof EventHandlerType)
      {
         EventHandlerType eventHandler = (EventHandlerType) candidate;
         if (eventHandler.getId().equals(ModelerConstants.RS_RESUBMISSION))
         {

            EObject container = null;
            if (candidate.eContainer() instanceof ChangeDescriptionImpl)
            {
               ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) candidate
                     .eContainer();
               container = changeDescription.getOldContainer(candidate);
            }
            else
            {
               container = candidate.eContainer();
            }

            if (container instanceof ActivityType)
            {
               change.markAlsoModified(container);
            }
         }
      }

      if ((candidate instanceof EventActionType))
      {
         EventActionType action = (EventActionType) candidate;
         boolean isExcludeUserAction = action.getType().getId()
               .equals(PredefinedConstants.EXCLUDE_USER_ACTION);
         boolean isDelegateAction = action.getType().getId()
               .equals(PredefinedConstants.DELEGATE_ACTIVITY_ACTION);

         if (isExcludeUserAction || isDelegateAction)
         {
            EObject container = null;
            if (candidate.eContainer() instanceof ChangeDescriptionImpl)
            {
               ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) candidate
                     .eContainer();
               container = changeDescription.getOldContainer(candidate);
            }
            else
            {
               container = candidate.eContainer();
            }

            if (container instanceof EventHandlerType)
            {
               EventHandlerType handler = (EventHandlerType) container;
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
