package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Package;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test.Bpmn2TestUtils.createModel;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test.Bpmn2TestUtils.createTestProcess;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test.Bpmn2TestUtils.createTestProcessDiagram;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.ParallelGateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.eclipse.emf.ecore.EObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2DiBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2FlowNodeBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2SequenceFlowBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.EObjectMorpher;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.TransitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.GatewaySymbolJto;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler.ModelDescriptor;

public class SwitchElementTypeTest
{
   private Definitions model;

   private Process testProcess;

   private ExclusiveGateway xorGateway;

   @Before
   public void initTestProcess()
   {
      this.model = createModel();
      this.testProcess = createTestProcess(model);
      createTestFlow();

      BPMNDiagram testDiagram = createTestProcessDiagram(testProcess);

      for (FlowElement flowElement : testProcess.getFlowElements())
      {
         if (flowElement instanceof ExclusiveGateway)
         {
            this.xorGateway = (ExclusiveGateway) flowElement;
            break;
         }
      }

      Bpmn2DiBuilder diBuilder = new Bpmn2DiBuilder();

      GatewaySymbolJto nodeSymbolJto = new GatewaySymbolJto();
      nodeSymbolJto.x = 100;
      nodeSymbolJto.y = 100;
      nodeSymbolJto.width = 32;
      nodeSymbolJto.height = 32;
      diBuilder.attachDiagramElement(testDiagram,
            diBuilder.createNodeSymbol(model, nodeSymbolJto, xorGateway));

      traceGeneratedModel();
   }

   @Test
   public void switchXorToAndGatewayTest()
   {
      assertThat(xorGateway, is(not(nullValue())));

      ParallelGateway andGateway = (ParallelGateway) EObjectMorpher.morphType(
            xorGateway, bpmn2Package().getParallelGateway());

      assertThat(andGateway, is(not(nullValue())));
      assertThat(andGateway.getId(), is(xorGateway.getId()));

      assertThat(xorGateway.eContainer(), is(nullValue()));
      assertThat(andGateway.eContainer(), is((EObject) testProcess));
   }

   private void createTestFlow()
   {
      Bpmn2FlowNodeBuilder flowNodeBuilder = new Bpmn2FlowNodeBuilder();

      EventJto jto = new EventJto();
      jto.eventType = ModelerConstants.START_EVENT;
      jto.name = "Start";
      StartEvent startEvent = (StartEvent) flowNodeBuilder.createEvent(model, jto);

      ActivityJto step1Jto = new ActivityJto();
      step1Jto.name = "Step 1";
      step1Jto.activityType = ModelerConstants.MANUAL_ACTIVITY;
      UserTask step1 = (UserTask) flowNodeBuilder.createActivity(model, step1Jto);

      GatewayJto splitJto = new GatewayJto();
      splitJto.name = "Gateway 1";
      splitJto.id = "gateway-" + Bpmn2Utils.createInternalId();
      splitJto.gatewayType = ModelerConstants.XOR_GATEWAY_TYPE;
      Gateway split = flowNodeBuilder.createGateway(model, splitJto);

      ActivityJto step2aJto = new ActivityJto();
      step2aJto.name = "Step 2 - a";
      step2aJto.activityType = ModelerConstants.MANUAL_ACTIVITY;
      UserTask step2a = (UserTask) flowNodeBuilder.createActivity(model, step2aJto);

      ActivityJto step2bJto = new ActivityJto();
      step2bJto.name = "Step 2 - b";
      step2bJto.activityType = ModelerConstants.MANUAL_ACTIVITY;
      UserTask step2b = (UserTask) flowNodeBuilder.createActivity(model, step2bJto);

      Bpmn2SequenceFlowBuilder flowBuilder = new Bpmn2SequenceFlowBuilder();

      SequenceFlow startToStep1 = flowBuilder.createSequenceFlow(model, new TransitionJto());
      startToStep1.setSourceRef(startEvent);
      startToStep1.setTargetRef(step1);

      SequenceFlow step1ToXor = flowBuilder.createSequenceFlow(model, new TransitionJto());
      step1ToXor.setSourceRef(step1);
      step1ToXor.setTargetRef(split);

      SequenceFlow xorToStep2a = flowBuilder.createSequenceFlow(model, new TransitionJto());
      xorToStep2a.setSourceRef(split);
      xorToStep2a.setTargetRef(step2a);

      SequenceFlow xorToStep2b = flowBuilder.createSequenceFlow(model, new TransitionJto());
      xorToStep2b.setSourceRef(split);
      xorToStep2b.setTargetRef(step2b);

      flowNodeBuilder.attachFlowNode(testProcess, startEvent);
      flowNodeBuilder.attachFlowNode(testProcess, step1);
      flowNodeBuilder.attachFlowNode(testProcess, split);
      flowNodeBuilder.attachFlowNode(testProcess, step2a);
      flowNodeBuilder.attachFlowNode(testProcess, step2b);

      flowBuilder.attachSequenceFlow(testProcess, startToStep1);
      flowBuilder.attachSequenceFlow(testProcess, step1ToXor);
      flowBuilder.attachSequenceFlow(testProcess, xorToStep2a);
      flowBuilder.attachSequenceFlow(testProcess, xorToStep2b);
   }

   @After
   public void traceModelAfterChange()
   {
      traceGeneratedModel();
   }

   public void traceGeneratedModel()
   {
      if (null != model)
      {
         Bpmn2PersistenceHandler persistenceHandler = new Bpmn2PersistenceHandler();

         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         persistenceHandler.saveModel(model, baos);

         System.out.println("Generated model:\n" + new String(baos.toByteArray()));

         ModelDescriptor reloadedModel = persistenceHandler.loadModel("TestModel.bpmn", new ByteArrayInputStream(baos.toByteArray()));
         System.out.println("Reloaded model: " + reloadedModel);
      }
   }
}
