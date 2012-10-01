package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.CarnotWorkflowModelPackage;
import org.eclipse.stardust.model.xpdl.carnot.IIdentifiableElement;
import org.eclipse.stardust.ui.web.modeler.edit.spi.ChangePostprocessor;

@Component
public class TouchReferrersUponIdentityChangePostprocessor implements ChangePostprocessor
{
   private static final CarnotWorkflowModelPackage PKG_XPDL = CarnotWorkflowModelPackage.eINSTANCE;

   @Override
   public int getInspectionPhase()
   {
      return 100;
   }

   @Override
   public void inspectChange(Modification change)
   {
      for (EObject candidate : change.getModifiedElements())
      {
         if ((candidate instanceof IIdentifiableElement)
               && change.wasModified(candidate, PKG_XPDL.getIIdentifiableElement_Id()))
         {
            // TODO find all references and flag them as modified
            for (EObject other : candidate.eCrossReferences())
            {
               change.markAlsoModified(other);
            }

            // TODO find non-EMF references (e.g. from Stardust Attributes)
         }
      }
   }

}
