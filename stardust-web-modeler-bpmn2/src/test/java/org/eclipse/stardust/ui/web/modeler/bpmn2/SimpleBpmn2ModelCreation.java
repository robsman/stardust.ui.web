package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test.Bpmn2TestUtils.createModel;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test.Bpmn2TestUtils.createTestProcess;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test.Bpmn2TestUtils.createTestProcessDiagram;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.bpmn2.Collaboration;
import org.eclipse.bpmn2.DataObject;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.Interface;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.ItemKind;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.ProcessType;
import org.eclipse.bpmn2.StartEvent;
import org.eclipse.bpmn2.UserTask;
import org.eclipse.bpmn2.di.BPMNDiagram;
import org.junit.After;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2FlowNodeBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2ItemDefinitionBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.builder.Bpmn2VariableBuilder;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.test.Bpmn2TestUtils;
import org.eclipse.stardust.ui.web.modeler.integration.ExternalXmlSchemaManager;
import org.eclipse.stardust.ui.web.modeler.model.ActivityJto;
import org.eclipse.stardust.ui.web.modeler.model.DataJto;
import org.eclipse.stardust.ui.web.modeler.model.EventJto;
import org.eclipse.stardust.ui.web.modeler.model.GatewayJto;
import org.eclipse.stardust.ui.web.modeler.model.TypeDeclarationJto;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler.ModelDescriptor;

public class SimpleBpmn2ModelCreation
{
   static final Bpmn2Binding binding = new Bpmn2Binding(null);

   private static final Bpmn2Navigator navigator = new Bpmn2Navigator(binding);

   private final ExternalXmlSchemaManager externalXmlSchemaManager = new ExternalXmlSchemaManager();

   private Definitions model;
   private Interface testWebService;
   private Process testProcess;
   private BPMNDiagram testProcessDiagram;

   @Test
   public void creatingAnEmptyModelMustResultInValidBpmn2()
   {
      this.model = createModel();

      assertThat(model, is(notNullValue()));
      assertThat(model.getId(), is(notNullValue()));
      assertThat(model.getName(), is(Bpmn2TestUtils.MODEL_NAME));

      assertThat(model.getTargetNamespace(), is(notNullValue()));
   }

   @Test
   public void creatingASimpleApplicationMustProperlyConfigureDefaultsAndAttachTheDefinition()
   {
      this.model = createModel();
      this.testWebService = Bpmn2TestUtils.createTestWebService(model);

      assertThat(testWebService, is(notNullValue()));
      assertThat(testWebService.getId(), is(notNullValue()));
      assertThat(testWebService.getName(), is(Bpmn2TestUtils.WEB_SERVICE_NAME));

      assertThat(testWebService.getImplementationRef(), is(not(nullValue())));

//      assertThat(navigator.findProcess(model, Bpmn2TestUtils.PROCESS_ID), is(testProcess));
   }

   @Test
   public void creatingAnEmptyProcessMustProperlyConfigureDefaultsAndAttachTheProcess()
   {
      this.model = createModel();
      this.testProcess = Bpmn2TestUtils.createTestProcess(model);

      assertThat(testProcess, is(notNullValue()));
      assertThat(testProcess.getId(), is(notNullValue()));
      assertThat(testProcess.getName(), is(Bpmn2TestUtils.PROCESS_NAME));

      assertThat(testProcess.getProcessType(), is(ProcessType.PRIVATE));
      assertThat(testProcess.isIsExecutable(), is(true));

      assertThat(navigator.findProcess(model, Bpmn2TestUtils.PROCESS_ID), is(testProcess));
   }

   @Test
   public void creatingAnXsdTypeReferenceMustProperlyConfigureDefaultsAndAttachTheItemDefinition()
   {
      this.model = createModel();
      TypeDeclarationJto jto = new TypeDeclarationJto();
      jto.typeDeclaration.type.classifier = "ExternalReference";
      jto.typeDeclaration.type.location = "file:///E:/work/ipp/src.svn/pepper/web-modeler-omni-extensions/src/main/resources/META-INF/webapp/public/swift-2012/fin.202.COV.2012.xsd";
      jto.typeDeclaration.type.xref = "{urn:swift:xsd:fin.202.COV.2012}MT202_COV_Type";
      Bpmn2ItemDefinitionBuilder itemDefinitionBuilder = new Bpmn2ItemDefinitionBuilder(externalXmlSchemaManager);
      ItemDefinition tdMt202CovType = itemDefinitionBuilder.createXsdReference(model, jto);
      itemDefinitionBuilder.attachItemDefinition(model, tdMt202CovType);

      assertThat(tdMt202CovType, is(notNullValue()));
      assertThat(tdMt202CovType.getId(), is(notNullValue()));

      assertThat(tdMt202CovType.getItemKind(), is(ItemKind.INFORMATION));

//      assertThat(navigator.findProcess(model, Bpmn2TestUtils.PROCESS_ID), is(testProcess));
   }

   @Test
   public void creatingAnXsdTypedVariableMustProperlyConfigureDefaultsAndAttachTheDataObject()
   {
      this.model = createModel();
      this.testProcess = createTestProcess(model);

      TypeDeclarationJto jto = new TypeDeclarationJto();
      jto.typeDeclaration.type.classifier = "ExternalReference";
      jto.typeDeclaration.type.location = "file:///E:/work/ipp/src.svn/pepper/web-modeler-omni-extensions/src/main/resources/META-INF/webapp/public/swift-2012/fin.202.COV.2012.xsd";
      jto.typeDeclaration.type.xref = "{urn:swift:xsd:fin.202.COV.2012}MT202_COV_Type";
      Bpmn2ItemDefinitionBuilder itemDefinitionBuilder = new Bpmn2ItemDefinitionBuilder(externalXmlSchemaManager);
      ItemDefinition tdMt202CovType = itemDefinitionBuilder.createXsdReference(model, jto);
      itemDefinitionBuilder.attachItemDefinition(model, tdMt202CovType);

      DataJto varJto = new DataJto();
      varJto.name = "Swift - 202";
      varJto.dataType = ModelerConstants.STRUCTURED_DATA_TYPE_KEY;
      varJto.structuredDataTypeFullId = tdMt202CovType.getId();

      Bpmn2VariableBuilder variableBuilder = new Bpmn2VariableBuilder();
      DataObject xsdVariable = variableBuilder.createXsdVariable(model, varJto);
      variableBuilder.attachVariable(testProcess, xsdVariable);


      assertThat(xsdVariable, is(notNullValue()));
      assertThat(xsdVariable.getId(), is(notNullValue()));

      assertThat(xsdVariable.getItemSubjectRef(), is(tdMt202CovType));

//      assertThat(navigator.findProcess(model, Bpmn2TestUtils.PROCESS_ID), is(testProcess));
   }

   @Test
   public void creatingAnEmptyProcessDiagramMustProperlyConfigureDefaultsAndAttachTheDiagram()
   {
      this.model = createModel();
      this.testProcess = createTestProcess(model);
      this.testProcessDiagram = createTestProcessDiagram(testProcess);

      assertThat(testProcessDiagram, is(notNullValue()));
      assertThat(testProcessDiagram.getName(), is("Default"));

      assertThat(testProcessDiagram.getPlane(), is(notNullValue()));
      assertThat(testProcessDiagram.getPlane().getBpmnElement(), is(instanceOf(Collaboration.class)));
   }

   @Test
   public void creatingASimpleProcessDiagramMustProperlyConfigureDefaultsAndAttachTheDiagram()
   {
      this.model = createModel();
      this.testProcess = createTestProcess(model);
      this.testProcessDiagram = createTestProcessDiagram(testProcess);
      createTestFlow();

      assertThat(testProcessDiagram, is(notNullValue()));
      assertThat(testProcessDiagram.getName(), is("Default"));

      assertThat(testProcessDiagram.getPlane(), is(notNullValue()));
      assertThat(testProcessDiagram.getPlane().getBpmnElement(), is(instanceOf(Collaboration.class)));
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

      flowNodeBuilder.attachFlowNode(testProcess, startEvent);
      flowNodeBuilder.attachFlowNode(testProcess, step1);
      flowNodeBuilder.attachFlowNode(testProcess, split);
      flowNodeBuilder.attachFlowNode(testProcess, step2a);
      flowNodeBuilder.attachFlowNode(testProcess, step2b);
   }

   @After
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
