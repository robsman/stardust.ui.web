package org.eclipse.stardust.ui.web.modeler.model.di;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class LaneSymbolJto extends ShapeJto
{
   public String id;
   public String name;

   public String participantFullId;

   public List<ActivitySymbolJto> activitySymbols = newArrayList();
   public List<GatewaySymbolJto> gatewaySymbols = newArrayList();
   public List<EventSymbolJto> eventSymbols = newArrayList();

   public LaneSymbolJto()
   {
      this.type = ModelerConstants.SWIMLANE_SYMBOL;
   }
}
