package org.eclipse.stardust.ui.web.modeler.model.di;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class PoolSymbolJto extends ShapeJto
{
   public String id;
   public String name;

   public String processId;

   public List<LaneSymbolJto> laneSymbols = newArrayList();

   public String orientation = ModelerConstants.DIAGRAM_FLOW_ORIENTATION_VERTICAL;
}
