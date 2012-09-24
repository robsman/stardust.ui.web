package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findContainingApplication;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

public class ApplicationAccessPointChangeTracker implements ChangePostprocessor
{
   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getAddedElements())
      {
         trackApplicationAccessPointChanges(change, candidate);
      }
      for (EObject candidate : change.getModifiedElements())
      {
         trackApplicationAccessPointChanges(change, candidate);
      }
      for (EObject candidate : change.getRemovedElements())
      {
         trackApplicationAccessPointChanges(change, candidate);
      }
   }

   private void trackApplicationAccessPointChanges(Modification change, EObject candidate)
   {
      if ((candidate instanceof AccessPointType) || (candidate instanceof ContextType))
      {
         ApplicationType containingApplication = findContainingApplication(candidate);
         if (null != containingApplication)
         {
            change.markAlsoModified(containingApplication);
            change.markUnmodified(candidate);
         }
      }
   }
}
