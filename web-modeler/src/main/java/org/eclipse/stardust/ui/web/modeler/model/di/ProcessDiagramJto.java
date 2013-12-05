package org.eclipse.stardust.ui.web.modeler.model.di;

import java.util.LinkedHashMap;
import java.util.Map;

public class ProcessDiagramJto
{
   public String uuid;
   public String id;
   public String name;

   public Map<String, PoolSymbolJto> poolSymbols = new LinkedHashMap<String, PoolSymbolJto>();

   public Map<String, ConnectionSymbolJto> connections = new LinkedHashMap<String, ConnectionSymbolJto>();
}
