package org.eclipse.stardust.ui.web.modeler.xpdl.edit.batch;

import static java.util.Collections.EMPTY_LIST;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.EObject;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.batch.BatchChangesJto;
import org.eclipse.stardust.ui.web.modeler.edit.batch.BatchStepJto;
import org.eclipse.stardust.ui.web.modeler.edit.batch.VarSpecJto;
import org.eclipse.stardust.ui.web.modeler.edit.jto.ChangeDescriptionJto;
import org.eclipse.stardust.ui.web.modeler.edit.jto.CommandJto;
import org.eclipse.stardust.ui.web.modeler.marshaling.JsonMarshaller;
import org.eclipse.stardust.ui.web.modeler.model.conversion.BeanInvocationExecutor;
import org.eclipse.stardust.ui.web.modeler.model.conversion.RequestExecutor;
import org.eclipse.stardust.ui.web.modeler.service.DefaultModelManagementStrategy;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.stardust.ui.web.modeler.service.ModelerSessionController;
import org.eclipse.stardust.ui.web.modeler.utils.test.ChangeApiDriver;
import org.eclipse.stardust.ui.web.modeler.utils.test.MockServiceFactoryLocator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"../../../web-modeler-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class TestChecklistEditing
{
   @Resource
   MockServiceFactoryLocator mockServiceFactoryLocator;

   @Resource
   JsonMarshaller jsonIo;

   @Resource
   ModelService modelService;

   @Resource
   private ModelerSessionController restController;

   private ChangeApiDriver changeApiDriver;

   private ModelingSession mySession;

   @Test
   public void testCreatingAChecklistWillCreateANewModelAndMainProcess()
   {
      assertThat(mySession.modelRepository().findModel("SimpleChecklist1"),
            is(nullValue()));

      CommandJto cmdCreateModel = new CommandJto();
      cmdCreateModel.commandId = "model.create";

      ChangeDescriptionJto newModelDescriptor = new ChangeDescriptionJto();
      newModelDescriptor.changes = new JsonObject();
      newModelDescriptor.changes.addProperty("name", "Simple Checklist 1");
      newModelDescriptor.changes.addProperty("id", "SimpleChecklist1");
      newModelDescriptor.changes.addProperty("modelFormat", "xpdl");

      cmdCreateModel.changeDescriptions = newArrayList();
      cmdCreateModel.changeDescriptions.add(newModelDescriptor);

      changeApiDriver.performChange(cmdCreateModel);

      assertThat(mySession.modelRepository().findModel("SimpleChecklist1"),
            is(notNullValue()));
   }

   @Test
   public void testCreatingAChecklistMainProcessCanBeDoneInABatch()
   {
      // preconditions
      testCreatingAChecklistWillCreateANewModelAndMainProcess();

      ModelType checklistModel = (ModelType) mySession.modelRepository().findModel(
            "SimpleChecklist1");

      assertThat(checklistModel.getProcessDefinition().size(), is(0));

      // instantiate the batch edit
      CommandJto cmdCreateProcess = new CommandJto();
      cmdCreateProcess.commandId = "batchEdit.run";
      cmdCreateProcess.modelId = checklistModel.getId();

      BatchChangesJto batchChanges = new BatchChangesJto();

      // first create a new process definition
      BatchStepJto newModelDescriptor = new BatchStepJto();
      newModelDescriptor.commandId = "process.create";
      newModelDescriptor.oid = checklistModel.getId();
      newModelDescriptor.changes = new JsonObject();
      newModelDescriptor.changes.addProperty("name", checklistModel.getName());

      cmdCreateProcess.changeDescriptions = newArrayList();
      batchChanges.steps.add(newModelDescriptor);

      // declare an expression to retrieve the new process' main pool's OID
      newModelDescriptor.variables
            .add(VarSpecJto
                  .namedExpression(
                        "defaultPool.oid",
                        "added/[orientation='DIAGRAM_FLOW_ORIENTATION_VERTICAL']/poolSymbols/_default_pool__1/oid"));

      // TODO add change to set 'stardust:model:simpleModel' attribute for newly created model

      // now create a second lane to hold all interactive tasks
      BatchStepJto adminLaneDescriptor = new BatchStepJto();
      adminLaneDescriptor.commandId = "swimlaneSymbol.create";
      // use the previously declared variable targeting the main pool's OID
      adminLaneDescriptor.oid = "${defaultPool.oid}";
      adminLaneDescriptor.changes = new JsonObject();
      adminLaneDescriptor.changes.addProperty("x", 200);
      adminLaneDescriptor.changes.addProperty("y", 0);
      adminLaneDescriptor.changes.addProperty("width", 199);
      adminLaneDescriptor.changes.addProperty("height", 800);
      adminLaneDescriptor.changes.addProperty("orientation",
            "DIAGRAM_FLOW_ORIENTATION_VERTICAL");
      adminLaneDescriptor.changes.addProperty("participantFullId", checklistModel.getId()
            + ":Administrator");

      // declare additional expressions to retrieve the interactive lane's OID and ID
      adminLaneDescriptor.variables.add(VarSpecJto.namedExpression("adminLane.oid",
            "added/[type='swimlaneSymbol']/oid"));
      adminLaneDescriptor.variables.add(VarSpecJto.namedExpression("adminLane.id",
            "added/[type='swimlaneSymbol']/id"));

      batchChanges.steps.add(adminLaneDescriptor);

      BatchStepJto createStartEvent = new BatchStepJto();
      createStartEvent.commandId = "eventSymbol.create";
      createStartEvent.oid = "${adminLane.oid}";
      createStartEvent.changes = new JsonObject();

      JsonObject startEventSpec = new JsonObject();
      startEventSpec.addProperty("name", "Interactive Start");
      startEventSpec.addProperty("eventType", "startEvent");
      startEventSpec.add("implementation", new JsonNull());
      createStartEvent.changes.add("modelElement", startEventSpec);
      createStartEvent.changes.addProperty("parentSymbolId", "${adminLane.id}");
      createStartEvent.changes.addProperty("x", 10);
      createStartEvent.changes.addProperty("y", 10);
      createStartEvent.changes.addProperty("width", 16);
      createStartEvent.changes.addProperty("height", 16);

      createStartEvent.variables.add(VarSpecJto.namedExpression("startEventSymbol.oid",
            "added/[type='eventSymbol']/oid"));

      batchChanges.steps.add(createStartEvent);

      BatchStepJto renameStartEvent = new BatchStepJto();
      renameStartEvent.commandId = "modelElement.update";
      renameStartEvent.oid = "${startEventSymbol.oid}";
      renameStartEvent.changes = new JsonObject();
      renameStartEvent.changes.add("modelElement", new JsonObject());
      renameStartEvent.changes.get("modelElement").getAsJsonObject()
            .addProperty("name", "Interactive Start");

      batchChanges.steps.add(renameStartEvent);

      // TODO patch name of start event

      BatchStepJto createEndEvent = new BatchStepJto();
      createEndEvent.commandId = "eventSymbol.create";
      createEndEvent.oid = "${adminLane.oid}";
      createEndEvent.changes = new JsonObject();

      JsonObject endEventSpec = new JsonObject();
      endEventSpec.addProperty("name", "event");
      endEventSpec.addProperty("eventType", "stopEvent");
      endEventSpec.add("implementation", new JsonNull());
      createEndEvent.changes.add("modelElement", endEventSpec);
      createEndEvent.changes.addProperty("parentSymbolId", "${adminLane.id}");
      createEndEvent.changes.addProperty("x", 10);
      createEndEvent.changes.addProperty("y", 10);
      createEndEvent.changes.addProperty("width", 16);
      createEndEvent.changes.addProperty("height", 16);

      batchChanges.steps.add(createEndEvent);

      ChangeDescriptionJto batchDescriptor = new ChangeDescriptionJto();
      batchDescriptor.oid = checklistModel.getId();
      batchDescriptor.changes = jsonIo.gson().toJsonTree(batchChanges).getAsJsonObject();
      cmdCreateProcess.changeDescriptions.add(batchDescriptor);

      performCreateProcessBatchChange(cmdCreateProcess, checklistModel);
   }

   @Test
   public void testCreatingAChecklistMainProcessCanBeDoneInAScriptedBatch()
   {
      // preconditions
      testCreatingAChecklistWillCreateANewModelAndMainProcess();

      String modelId = "SimpleChecklist1";
      String modelName = "Simple Checklist 1";

      ModelType checklistModel = (ModelType) mySession.modelRepository().findModel(
            modelId);

      assertThat(checklistModel.getProcessDefinition().size(), is(0));

      BatchChangesJto batchChange = new BatchChangesJto();

      batchChange.variableBindings.add("model.id", new JsonPrimitive(modelId));
      batchChange.variableBindings.add("model.name", new JsonPrimitive(modelName));

      String batchSteps = "" //
            + "[" //
            // create a new process with identical name/id as the model
            + "  {" //
            + "    'commandId': 'process.create'," //
            + "    'oid': '${model.id}'," //
            + "    'changes': {" //
            + "      'name': '${model.name}'" //
            + "    }," //
            + "    'variables': [" //
            + "      {'name': 'defaultPool.oid', 'expression': \"added[orientation='DIAGRAM_FLOW_ORIENTATION_VERTICAL']/poolSymbols/_default_pool__1/oid\"}" //
            + "    ]" //
            + "  }," //
            // add a second swimlane for interactive tasks, initially associated with the
            // Administrator
            + "  {" //
            + "    'commandId': 'swimlaneSymbol.create'," //
            + "    'oid': '${defaultPool.oid}'," //
            + "    'changes': {" //
            + "      'height': 800," //
            + "      'orientation': 'DIAGRAM_FLOW_ORIENTATION_VERTICAL'," //
            + "      'participantFullId': '${model.id}:Administrator'," //
            + "      'width': 199," //
            + "      'x': 200," //
            + "      'y': 0" //
            + "    }," //
            + "    'variables': [" //
            + "      {'name': 'adminLane.id', 'expression': \"added[type='swimlaneSymbol']/id\"}," //
            + "      {'name': 'adminLane.oid', 'expression': \"added[type='swimlaneSymbol']/oid\"}" //
            + "    ]" //
            + "  }," //
            // create a start event/manual trigger
            + "  {" //
            + "    'commandId': 'eventSymbol.create'," //
            + "    'oid': '${adminLane.oid}'," //
            + "    'changes': {" //
            + "      'height': 16," //
            + "      'modelElement': {" //
            + "        'eventType': 'startEvent'," //
            + "        'implementation': null," //
            + "        'name': 'Interactive Start'" //
            + "      }," //
            + "      'parentSymbolId': '${adminLane.id}'," //
            + "      'width': 16," //
            + "      'x': 10," //
            + "      'y': 10" //
            + "    }," //
            + "    'variables': [" //
            + "      {'name': 'startEventSymbol.oid', 'expression': \"added[type='eventSymbol']/oid\"}" //
            + "    ]" //
            + "  }," //
            // patch the name of the just created start event/manual trigger
            + "  {" //
            + "    'commandId': 'modelElement.update'," //
            + "    'oid': '${startEventSymbol.oid}'," //
            + "    'changes': {" //
            + "      'modelElement': {" //
            + "        'name': 'Interactive Start'" //
            + "      }" //
            + "    }" //
            + "  }," //
            // create the end event
            + "  {" //
            + "    'commandId': 'eventSymbol.create'," //
            + "    'oid': '${adminLane.oid}'," //
            + "    'changes': {" //
            + "      'height': 16," //
            + "      'modelElement': {" //
            + "        'eventType': 'stopEvent'," //
            + "        'implementation': null," //
            + "        'name': 'event'" //
            + "      }," //
            + "      'parentSymbolId': '${adminLane.id}'," //
            + "      'width': 16," //
            + "      'x': 10," //
            + "      'y': 10" //
            + "    }," //
            + "    'variables': [" //
            + "      {'name': 'stopEventSymbol.oid', 'expression': \"added[type='eventSymbol']/oid\"}" //
            + "    ]" //
            + "  }" //
            + "]";

      batchChange.steps = jsonIo.gson().fromJson(batchSteps, new TypeToken<List<BatchStepJto>>(){}.getType());

      CommandJto batchCommand = new CommandJto();
      batchCommand.commandId = "batchEdit.run";
      batchCommand.modelId = modelId;
      batchCommand.changeDescriptions = newArrayList();
      batchCommand.changeDescriptions.add(new ChangeDescriptionJto());

      batchCommand.changeDescriptions.get(0).oid = modelId;
      batchCommand.changeDescriptions.get(0).changes = jsonIo.gson()
            .toJsonTree(batchChange).getAsJsonObject();

      // TODO run batch
      performCreateProcessBatchChange(batchCommand, checklistModel);
   }

   protected void performCreateProcessBatchChange(CommandJto cmdCreateProcess,
         ModelType checklistModel)
   {
      JsonObject batchResults = changeApiDriver.performChange(cmdCreateProcess);

      System.out.println(batchResults);

      assertThat(mySession.modelRepository().findModel("SimpleChecklist1"),
            is(sameInstance((EObject) checklistModel)));
      assertThat(checklistModel.getProcessDefinition().size(), is(1));

      ProcessDefinitionType checklistProcess = checklistModel.getProcessDefinition().get(
            0);

      assertThat(checklistProcess.getDiagram().size(), is(1));
      DiagramType checklistDiagram = checklistProcess.getDiagram().get(0);

      assertThat(checklistDiagram.getPoolSymbols().size(), is(1));
      PoolSymbol mainPool = checklistDiagram.getPoolSymbols().get(0);
      assertThat(mainPool.getStartEventSymbols().size(), is(0));
      assertThat(mainPool.getEndEventSymbols().size(), is(0));

      assertThat(mainPool.getLanes().size(), is(2));

      LaneSymbol defaultLane = mainPool.getLanes().get(0);
      assertThat(defaultLane.getParticipantReference(), is(nullValue()));
      assertThat(defaultLane.getStartEventSymbols().size(), is(0));
      assertThat(defaultLane.getEndEventSymbols().size(), is(0));

      LaneSymbol adminLane = mainPool.getLanes().get(1);
      assertThat(adminLane.getParticipantReference(), is(notNullValue()));
      assertThat(adminLane.getParticipantReference().getId(),
            is(PredefinedConstants.ADMINISTRATOR_ROLE));

      assertThat(adminLane.getStartEventSymbols().size(), is(1));

      assertThat(checklistProcess.getTrigger().size(), is(1));
      TriggerType checklistTrigger = checklistProcess.getTrigger().get(0);
      assertThat(checklistTrigger.getType().getId(),
            is(PredefinedConstants.MANUAL_TRIGGER));
      assertThat(checklistTrigger.getName(), is("Interactive Start"));

      assertThat(adminLane.getEndEventSymbols().size(), is(1));
   }

   @Before
   public void initServiceFactory()
   {
      assertThat(modelService, is(not(nullValue())));
      assertThat(jsonIo, is(not(nullValue())));
      assertThat(restController, is(not(nullValue())));

      RequestExecutor requestExecutor = new BeanInvocationExecutor(jsonIo, modelService,
            restController);
      this.changeApiDriver = new ChangeApiDriver(requestExecutor, jsonIo);

      mockServiceFactoryLocator.init();

      User mockUser = Mockito.mock(User.class);
      Mockito.when(mockUser.getAccount()).thenReturn("motu");

      UserService userService = mockServiceFactoryLocator.get().getUserService();
      Mockito.when(userService.getUser()).thenReturn(mockUser);

      // final Document xpdlModel = Mockito.mock(Document.class);
      // Mockito.when(xpdlModel.getId()).thenReturn(MODEL_ID);
      // Mockito.when(xpdlModel.getName()).thenReturn(MODEL_ID + ".xpdl");

      Folder modelsFolder = Mockito.mock(Folder.class);
      Mockito.when(modelsFolder.getDocuments()).thenReturn(EMPTY_LIST);

      DocumentManagementService dmsService = mockServiceFactoryLocator.get()
            .getDocumentManagementService();

      Mockito.when(dmsService.getFolder(DefaultModelManagementStrategy.MODELS_DIR))
            .thenReturn(modelsFolder);
      // Mockito.when(dmsService.retrieveDocumentContent(xpdlModel.getId())).thenAnswer(
      // new Answer<byte[]>()
      // {
      // @Override
      // public byte[] answer(InvocationOnMock invocation) throws Throwable
      // {
      // InputStream isModel = getClass().getResourceAsStream(
      // "../../service/rest/" + xpdlModel.getName());
      // try
      // {
      // return toByteArray(isModel);
      // }
      // finally
      // {
      // closeQuietly(isModel);
      // }
      // }
      // });
   }

   @Before
   public void initModelingSession()
   {
      this.mySession = modelService.currentSession();
      assertThat(mySession, is(not(nullValue())));
   }

   @After
   public void cleanup()
   {
      destroyMySession();
   }

   private void destroyMySession()
   {
      modelService.destroyModelingSession();
      this.mySession = null;
   }
}
