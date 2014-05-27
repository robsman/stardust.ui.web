package org.eclipse.stardust.ui.web.modeler.xpdl.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.edit.postprocessing.AbstractChangeTracker;

@Component
public class DataPathChangeTracker extends AbstractChangeTracker
{
   @Override
   protected void inspectChange(Modification change, EObject candidate)
   {
      if ((candidate instanceof DataPathType))
      {
         ProcessDefinitionType containingProcess = change.findContainer(candidate,
               ProcessDefinitionType.class);
         if (null != containingProcess)
         {
            change.markAlsoModified(containingProcess);
            change.markUnmodified(candidate);
         }
      }
   }
}
