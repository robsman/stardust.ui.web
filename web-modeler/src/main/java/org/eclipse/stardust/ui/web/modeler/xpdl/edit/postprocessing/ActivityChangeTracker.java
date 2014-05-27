package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.impl.ChangeDescriptionImpl;
import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;
import org.springframework.stereotype.Component;

@Component
public class ActivityChangeTracker implements ChangePostprocessor
{
   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getRemovedElements())
      {
         if (candidate instanceof ActivityType)
         {
            trackModification(candidate, true, change);
         }
      }
   }

   private void trackModification(EObject candidate, boolean removed, Modification change)
   {
      EObject container = null;
      if (candidate.eContainer() instanceof ChangeDescriptionImpl)
      {
         ChangeDescriptionImpl changeDescription = (ChangeDescriptionImpl) candidate.eContainer();
         container = changeDescription.getOldContainer(candidate);
      }
      else
      {
         container = candidate.eContainer();
      }

      ModelType model = (ModelType) ModelUtils.findContainingModel(container);
      if (model != null)
      {
         ActivityType activityType = (ActivityType) candidate;
         if (activityType.getExternalRef() == null && activityType.getApplication() != null)
         {
            ApplicationType applicationType = activityType.getApplication();
            if (applicationType.getType() != null && applicationType.getType()
                  .getId()
                  .equals(ModelerConstants.DROOLS_APPLICATION_TYPE_ID))
            {
               model.getApplication().remove(applicationType);
            }
         }
      }
   }
}