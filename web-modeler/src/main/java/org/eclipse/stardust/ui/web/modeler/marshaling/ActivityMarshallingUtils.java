package org.eclipse.stardust.ui.web.modeler.marshaling;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.INodeSymbol;

public class ActivityMarshallingUtils
{
   public static INodeSymbol resolveSymbolAssociatedWithActivity(ActivityType activity, DiagramType diagram)
   {
      if ( !activity.getActivitySymbols().isEmpty())
      {
         return activity.getActivitySymbols().get(0);
      }

      if (EventMarshallingUtils.isIntermediateEventHost(activity))
      {
         return ModelBuilderFacade.findIntermediateEventSymbol(diagram,
               EventMarshallingUtils.resolveHostedEvents(activity).get(0));
      }
      else if (EventMarshallingUtils.isEndEventHost(activity))
      {
         return ModelBuilderFacade.findEndEventSymbol(diagram,
               EventMarshallingUtils.resolveHostedEvents(activity).get(0));
      }

      return null;
   }

   private ActivityMarshallingUtils()
   {
      // utility class
   }
}
