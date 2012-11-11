package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;

@Component
public class StartEventTriggerChangeTracker extends AbstractChangeTracker
{
   @Override
   protected void inspectChange(Modification change, EObject candidate)
   {
      if (candidate instanceof TriggerType)
      {
         StartEventSymbol startEvent = ((TriggerType) candidate).getStartingEventSymbols()
               .get(0);

         if (null != startEvent)
         {
            change.markAlsoModified(startEvent);
            change.markUnmodified(candidate);
         }
      }
   }
}
