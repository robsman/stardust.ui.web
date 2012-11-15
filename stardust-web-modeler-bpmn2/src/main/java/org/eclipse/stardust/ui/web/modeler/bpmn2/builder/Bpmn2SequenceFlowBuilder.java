package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.FormalExpression;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;

import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.model.TransitionJto;

public class Bpmn2SequenceFlowBuilder
{
   public void attachSequenceFlow(Process process, SequenceFlow sequenceFlow)
   {
      assert (null == sequenceFlow.eContainer());

      process.getFlowElements().add(sequenceFlow);
   }

   public SequenceFlow createSequenceFlow(Definitions model, TransitionJto jto)
   {
      SequenceFlow flow = bpmn2Factory().createSequenceFlow();

      flow.setName(jto.name);
      flow.setId( !isEmpty(jto.id) //
            ? jto.id
            : Bpmn2Utils.createInternalId());

      if ( !isEmpty(jto.conditionExpression))
      {
         flow.setConditionExpression(bpmn2Factory().createFormalExpression());
         ((FormalExpression) flow.getConditionExpression()).setBody(jto.conditionExpression);
      }

      return flow;
   }
}
