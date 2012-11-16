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
         for (StartEventSymbol eventSymbol : ((TriggerType) candidate).getStartingEventSymbols())
         {
            if (null != eventSymbol)
            {
               change.markAlsoModified(eventSymbol);
            }
         }
         change.markUnmodified(candidate);
      }
   }
}
