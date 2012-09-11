package org.eclipse.stardust.ui.web.modeler.model.di;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

public class ProcessDiagramJto
{
   public String uuid;
   public String id;
   public String name;

   public List<PoolSymbolJto> poolSymbols = newArrayList();

   public List<ConnectionSymbolJto> connections = newArrayList();
}
