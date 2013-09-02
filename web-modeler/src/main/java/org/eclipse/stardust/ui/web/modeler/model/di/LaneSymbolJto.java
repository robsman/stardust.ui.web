package org.eclipse.stardust.ui.web.modeler.model.di;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class LaneSymbolJto extends ShapeJto
{
   public String id;
   public String name;

   public String orientation = ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL;

   public String participantFullId;

   public Map<String, ActivitySymbolJto> activitySymbols = new LinkedHashMap<String, ActivitySymbolJto>();
   public Map<String, GatewaySymbolJto> gatewaySymbols = new LinkedHashMap<String, GatewaySymbolJto>();
   public Map<String, EventSymbolJto> eventSymbols = new LinkedHashMap<String, EventSymbolJto>();
   public Map<String, DataSymbolJto> dataSymbols = new LinkedHashMap<String, DataSymbolJto>();

   public LaneSymbolJto()
   {
      this.type = ModelerConstants.SWIMLANE_SYMBOL;
   }
}
