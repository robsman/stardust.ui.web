package org.eclipse.stardust.ui.web.modeler.edit.postprocessing;

import org.eclipse.emf.ecore.EObject;
import org.springframework.stereotype.Component;

import org.eclipse.stardust.model.xpdl.builder.session.Modification;
import org.eclipse.stardust.model.xpdl.carnot.AccessPointType;
import org.eclipse.stardust.model.xpdl.carnot.StartEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;

@Component
public class TriggerAccessPointChangeTracker extends AbstractChangeTracker
{
   @Override
   protected void inspectChange(Modification change, EObject candidate)
   {
      if (candidate instanceof AccessPointType)
      {
         TriggerType containingTrigger = change.findContainer(candidate,
               TriggerType.class);

         if (containingTrigger != null)
         {
            for (StartEventSymbol eventSymbol : containingTrigger.getStartingEventSymbols())
            {
               if (null != eventSymbol)
               {
                  change.markAlsoModified(containingTrigger);
               }
            }
            change.markUnmodified(candidate);
         }
      }
   }
}
