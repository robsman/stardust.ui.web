package org.eclipse.stardust.ui.web.modeler.model.di;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

public class LaneSymbolJto extends ShapeJto
{
   public List<ActivitySymbolJto> activitySymbols = newArrayList();
   public List<GatewaySymbolJto> gatewaySymbols = newArrayList();
   public List<EventSymbolJto> eventSymbols = newArrayList();
}
