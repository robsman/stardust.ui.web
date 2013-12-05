package org.eclipse.stardust.ui.web.modeler.model;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonArray;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;

public class ProcessDefinitionJto extends ModelElementJto
{
   public ProcessDefinitionJto()
   {
      this.type = ModelerConstants.PROCESS_KEY;
   }

   public Map<String, ActivityJto> activities = new LinkedHashMap<String, ActivityJto>();
   public Map<String, GatewayJto> gateways = new LinkedHashMap<String, GatewayJto>();
   public Map<String, EventJto> events = new LinkedHashMap<String, EventJto>();
   public Map<String, TransitionJto> controlFlows = new LinkedHashMap<String, TransitionJto>();

   // TODO
   public JsonArray dataPathes = new JsonArray();

   // TODO more details
}
