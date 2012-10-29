package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

public abstract class AbstractChangeTracker implements ChangePostprocessor
{
   protected abstract void inspectChange(Modification change, EObject candidate);

   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getAddedElements())
      {
         inspectChange(change, candidate);
      }
      for (EObject candidate : change.getModifiedElements())
      {
         inspectChange(change, candidate);
      }
      for (EObject candidate : change.getRemovedElements())
      {
         inspectChange(change, candidate);
      }
   }
}
