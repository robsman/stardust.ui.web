package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Event;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Process;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;

public class Bpmn2FlowNodeBuilder
{
   public void attachFlowNode(Process process, FlowNode flowNode)
   {
      assert (null == flowNode.eContainer());

      process.getFlowElements().add(flowNode);
   }

   public Event createEvent(Definitions model, EventJto jto)
   {
      Event event;
      if (ModelerConstants.START_EVENT.equals(jto.eventType))
      {
         event = bpmn2Factory().createStartEvent();
      }
      else if (ModelerConstants.STOP_EVENT.equals(jto.eventType))
      {
         event = bpmn2Factory().createEndEvent();
      }
      else
      {
         throw new IllegalArgumentException("Unsupported event type: " + jto.eventType);
      }

      event.setName(jto.name);
      event.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.createInternalId());

      return event;
   }

   public Activity createActivity(Definitions model, ActivityJto jto)
   {
      Activity activity;
      if (isEmpty(jto.activityType))
      {
         activity = bpmn2Factory().createTask();
      }
      else if (ModelerConstants.MANUAL_ACTIVITY.equals(jto.activityType))
      {
         activity = bpmn2Factory().createUserTask();
      }
      else if (ModelerConstants.TASK_ACTIVITY.equals(jto.activityType))
      {
         activity = bpmn2Factory().createManualTask();
      }
      else if (ModelerConstants.SUBPROCESS_ACTIVITY.equals(jto.activityType))
      {
         activity = bpmn2Factory().createSubProcess();
      }
      else
      {
         throw new IllegalArgumentException("Unsupported activity type: " + jto.activityType);
      }

      activity.setName(jto.name);
      activity.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.createInternalId());

      return activity;
   }

   public Gateway createGateway(Definitions model, GatewayJto jto)
   {
      Gateway gateway;
      if (ModelerConstants.XOR_GATEWAY_TYPE.equals(jto.gatewayType))
      {
         gateway = bpmn2Factory().createExclusiveGateway();
      }
      else if (ModelerConstants.AND_GATEWAY_TYPE.equals(jto.gatewayType))
      {
         gateway = bpmn2Factory().createParallelGateway();
      }
      else
      {
         throw new IllegalArgumentException("Unsupported gateway type: " + jto.gatewayType);
      }

      gateway.setName(jto.name);
      gateway.setId( !isEmpty(jto.id)
            ? jto.id
            : Bpmn2Utils.createInternalId());

      return gateway;
   }
}
