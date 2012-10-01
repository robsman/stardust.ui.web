package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ProcessType;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.junit.After;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.ModelJto;
import org.eclipse.stardust.ui.web.modeler.model.ProcessDefinitionJto;
import org.eclipse.stardust.ui.web.modeler.model.di.ProcessDiagramJto;

public class SimpleBpmn2ModelCreation
{
   private static final String MODEL_NAME = "Simple BPMN2 Model";
   private static final String PROCESS_NAME = "Test Process";

   private static final String PROCESS_ID = UUID.randomUUID().toString();

   private static final Bpmn2Binding binding = new Bpmn2Binding();

   private static final Bpmn2Navigator navigator = new Bpmn2Navigator(binding);

   private Definitions model;
   private Process testProcess;
   private BPMNDiagram testProcessDiagram;

   @Test
   public void creatingAnEmptyModelMustResultInValidBpmn2()
   {
      this.model = createModel();

      assertThat(model, is(notNullValue()));
      assertThat(model.getId(), is(notNullValue()));
      assertThat(model.getName(), is(MODEL_NAME));

      assertThat(model.getTargetNamespace(), is(notNullValue()));
   }

   @Test
   public void creatingAnEmptyProcessMustProperlyConfigureDefaultsAndAttachTheProcess()
   {
      this.model = createModel();
      this.testProcess = createTestProcess();

      assertThat(testProcess, is(notNullValue()));
      assertThat(testProcess.getId(), is(notNullValue()));
      assertThat(testProcess.getName(), is(PROCESS_NAME));

      assertThat(testProcess.getProcessType(), is(ProcessType.PRIVATE));
      assertThat(testProcess.isIsExecutable(), is(true));

      assertThat(navigator.findProcess(model, PROCESS_ID), is(testProcess));
   }

   @Test
   public void creatingAnEmptyProcessDiagramMustProperlyConfigureDefaultsAndAttachTheDiagram()
   {
      this.model = createModel();
      this.testProcess = createTestProcess();
      this.testProcessDiagram = createTestProcessDiagram();

      assertThat(testProcessDiagram, is(notNullValue()));
      assertThat(testProcessDiagram.getName(), is("Default"));

      assertThat(testProcessDiagram.getPlane(), is(notNullValue()));
      assertThat(testProcessDiagram.getPlane().getBpmnElement(), is(instanceOf(Collaboration.class)));
   }

   @Test
   public void creatingASimpleProcessDiagramMustProperlyConfigureDefaultsAndAttachTheDiagram()
   {
      this.model = createModel();
      this.testProcess = createTestProcess();
      this.testProcessDiagram = createTestProcessDiagram();
      createTestFlow();

      assertThat(testProcessDiagram, is(notNullValue()));
      assertThat(testProcessDiagram.getName(), is("Default"));

      assertThat(testProcessDiagram.getPlane(), is(notNullValue()));
      assertThat(testProcessDiagram.getPlane().getBpmnElement(), is(instanceOf(Collaboration.class)));
   }

   private Definitions createModel()
   {
      ModelJto jto = new ModelJto();
      jto.name = MODEL_NAME;

      return binding.createModel(jto);
   }

   private Process createTestProcess()
   {
      ProcessDefinitionJto jto = new ProcessDefinitionJto();
      jto.uuid = PROCESS_ID;
      jto.id = PROCESS_ID;
      jto.name = PROCESS_NAME;

      Process process = (Process) binding.createModelElement(model, jto);

      binding.attachModelElement(model, process);

      return process;
   }

   private BPMNDiagram createTestProcessDiagram()
   {
      ProcessDiagramJto jto = new ProcessDiagramJto();
      jto.name = "Default";

      BPMNDiagram diagram = (BPMNDiagram) binding.createProcessDiagram(testProcess, jto);

      binding.attachModelElement(model, diagram);

      return diagram;
   }

   private void createTestFlow()
   {
      EventJto jto = new EventJto();
      jto.eventType = ModelerConstants.START_EVENT;
      jto.name = "Start";
      StartEvent startEvent = (StartEvent) binding.createModelElement(model, jto);

      ActivityJto step1Jto = new ActivityJto();
      step1Jto.name = "Step 1";
      step1Jto.activityType = ModelerConstants.MANUAL_ACTIVITY;
      UserTask step1 = (UserTask) binding.createModelElement(model, step1Jto);

      GatewayJto splitJto = new GatewayJto();
      splitJto.name = "Gateway 1";
      splitJto.id = "gateway-" + Bpmn2Utils.createInternalId();
      splitJto.gatewayType = ModelerConstants.XOR_GATEWAY_TYPE;
      Gateway split = (Gateway) binding.createModelElement(model, splitJto);

      ActivityJto step2aJto = new ActivityJto();
      step2aJto.name = "Step 2 - a";
      step2aJto.activityType = ModelerConstants.MANUAL_ACTIVITY;
      UserTask step2a = (UserTask) binding.createModelElement(model, step2aJto);

      ActivityJto step2bJto = new ActivityJto();
      step2bJto.name = "Step 2 - b";
      step2bJto.activityType = ModelerConstants.MANUAL_ACTIVITY;
      UserTask step2b = (UserTask) binding.createModelElement(model, step2bJto);

      binding.attachModelElement(testProcess, startEvent);
      binding.attachModelElement(testProcess, step1);
      binding.attachModelElement(testProcess, split);
      binding.attachModelElement(testProcess, step2a);
      binding.attachModelElement(testProcess, step2b);
   }

   @After
   public void traceGeneratedModel()
   {
      if (null != model)
      {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         new Bpmn2PersistenceHandler().saveModel(model, baos);

         System.out.println("Generated model:\n" + new String(baos.toByteArray()));
      }
   }
}
