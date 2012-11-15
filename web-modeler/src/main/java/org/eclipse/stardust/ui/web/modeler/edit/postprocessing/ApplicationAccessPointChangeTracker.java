package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;

@Component
public class ApplicationAccessPointChangeTracker extends AbstractChangeTracker
{
   @Override
   protected void inspectChange(Modification change, EObject candidate)
   {
      if ((candidate instanceof AccessPointType) || (candidate instanceof ContextType))
      {
         ApplicationType containingApplication = change.findContainer(candidate,
               ApplicationType.class);
         if (null != containingApplication)
         {
            change.markAlsoModified(containingApplication);
            change.markUnmodified(candidate);
         }
      }
   }
}
