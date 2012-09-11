package org.eclipse.stardust.ui.web.modeler.model;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class ProcessDefinitionJto extends ModelElementJto
{
   public ProcessDefinitionJto()
   {
      this.type = ModelerConstants.PROCESS_KEY;
   }

   public List<ActivityJto> activities = newArrayList();
   public List<ActivityJto> gateways = newArrayList();
   public List<EventJto> events = newArrayList();
   public List<TransitionJto> controlFlows = newArrayList();

   // TODO more details
}
