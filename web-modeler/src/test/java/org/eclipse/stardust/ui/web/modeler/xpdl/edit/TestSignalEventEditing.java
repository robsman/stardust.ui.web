package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static java.util.Collections.EMPTY_LIST;
import static org.eclipse.stardust.common.CollectionUtils.newArrayList;
import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil.getAttribute;
import static org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil.getAttributeValue;
import static org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil.getBooleanValue;
import static org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils.findData;
import static org.eclipse.stardust.ui.web.modeler.xpdl.edit.batch.SignalEventSnippets.createInDataMapping;
import static org.eclipse.stardust.ui.web.modeler.xpdl.edit.batch.SignalEventSnippets.createOutDataMapping;
import static org.eclipse.stardust.ui.web.modeler.xpdl.edit.batch.SignalEventSnippets.deleteDataMapping;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.api.runtime.DocumentManagementService;
import org.eclipse.stardust.engine.api.runtime.Folder;
import org.eclipse.stardust.engine.api.runtime.User;
import org.eclipse.stardust.engine.api.runtime.UserService;
import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.DataMappingType;
import org.eclipse.stardust.model.xpdl.carnot.DataType;
import org.eclipse.stardust.model.xpdl.carnot.DiagramType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.EventHandlerType;
import org.eclipse.stardust.model.xpdl.carnot.IntermediateEventSymbol;
import org.eclipse.stardust.model.xpdl.carnot.LaneSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.PoolSymbol;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.edit.ModelingSession;
import org.eclipse.stardust.ui.web.modeler.edit.batch.BatchChangesJto;
import org.eclipse.stardust.ui.web.modeler.edit.batch.BatchStepJto;
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
@ContextConfiguration(locations = {"../../web-modeler-test-context.xml"})
@FixMethodOrder(MethodSorters.JVM)
public class TestSignalEventEditing
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
   public void testChecklistsCanWaitForSignals()
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
            + "      {'name': 'defaultLane.oid', 'expression': \"added[orientation='DIAGRAM_FLOW_ORIENTATION_VERTICAL']/poolSymbols/_default_pool__1/laneSymbols[0]/oid\"}," //
            + "      {'name': 'defaultLane.id', 'expression': \"added[orientation='DIAGRAM_FLOW_ORIENTATION_VERTICAL']/poolSymbols/_default_pool__1/laneSymbols[0]/id\"}" //
            + "    ]" //
            + "  }," //
            // create a catching intermediate signal event
            + "  {" //
            + "    'commandId': 'eventSymbol.create'," //
            + "    'oid': '${defaultLane.oid}'," //
            + "    'changes': {" //
            + "      'modelElement': {" //
            + "        'eventType': 'intermediateEvent'," //
            + "        'implementation': null," //
            + "        'name': 'Wait for FileArrival'" //
            + "      }," //
            + "      'parentSymbolId': '${defaultLane.id}'," //
            + "      'width': 16," //
            + "      'height': 16," //
            + "      'x': 10," //
            + "      'y': 10" //
            + "    }," //
            + "    'variables': [" //
            + "      {'name': 'signalEventSymbol.oid', 'expression': \"added[type='eventSymbol']/oid\"}" //
            + "    ]" //
            + "  }," //
            // patch the name of the just created start event/manual trigger
            + "  {" //
            + "    'commandId': 'modelElement.update'," //
            + "    'oid': '${signalEventSymbol.oid}'," //
            + "    'changes': {" //
            + "      'modelElement': {" //
            + "        'name': 'Wait for FileArrival'," //
            + "        'eventClass': 'signal'," //
            + "        'interrupting': false," //
            + "        'attributes': {" //
            + "          'stardust:bpmn:signal:pastSignalsGracePeriod': 10" //
            + "        }" //
            + "      }" //
            + "    }," //
            + "    'variables': [" //
            + "      {'name': 'signalEvent.uuid', 'expression': \"modified[type='eventSymbol']/modelElement/uuid\"}" //
            + "    ]" //
            + "  }," //
            + createInDataMapping("${signalEvent.uuid}", "${model.id}:CURRENT_DATE",
                  "getTime().getTime()", "signalProperty1")
            + "," //
            + createOutDataMapping("${signalEvent.uuid}", "signalProperty2",
                  "${model.id}:CURRENT_DATE", false) //
            + "," //
            + createOutDataMapping("${signalEvent.uuid}", "signalProperty3",
                  "${model.id}:CURRENT_DATE", true) //                  
            + "," //
            + deleteDataMapping("${dataFlow.uuid}")
            + "  " //
            + "  " //
            + "  " //
            + "  " //
            + "]";

      batchChange.steps = jsonIo.gson().fromJson(batchSteps,
            new TypeToken<List<BatchStepJto>>()
            {
            }.getType());

      CommandJto batchCommand = new CommandJto();
      batchCommand.commandId = "batchEdit.run";
      batchCommand.modelId = modelId;
      batchCommand.changeDescriptions = newArrayList();
      batchCommand.changeDescriptions.add(new ChangeDescriptionJto());

      batchCommand.changeDescriptions.get(0).oid = modelId;
      batchCommand.changeDescriptions.get(0).changes = jsonIo.gson()
            .toJsonTree(batchChange).getAsJsonObject();

      // run batch
      JsonObject batchResults = changeApiDriver.performChange(batchCommand);

      System.out.println(batchResults);

      // assert results
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

      assertThat(mainPool.getLanes().size(), is(1));

      LaneSymbol defaultLane = mainPool.getLanes().get(0);
      assertThat(defaultLane.getParticipantReference(), is(nullValue()));
      assertThat(defaultLane.getStartEventSymbols().size(), is(0));
      assertThat(defaultLane.getEndEventSymbols().size(), is(0));

      assertThat(checklistProcess.getActivity().size(), is(1));
      ActivityType catchSignalEvent = checklistProcess.getActivity().get(0);

      assertCatchSignalIntermediateEventValidity(catchSignalEvent);

      assertCatchSignalDataMappingValidity(catchSignalEvent, DirectionType.IN_LITERAL,
            "signalProperty1", findData(checklistProcess, PredefinedConstants.CURRENT_DATE), "getTime().getTime()");
      assertCatchSignalDataMappingValidity(catchSignalEvent, DirectionType.OUT_LITERAL,
            "signalProperty2", findData(checklistProcess, PredefinedConstants.CURRENT_DATE), null);

      // check the intermediate event is properly connected to its symbol
      assertThat(defaultLane.getIntermediateEventSymbols().size(), is(1));
      IntermediateEventSymbol catchSignalEventSymbol = defaultLane
            .getIntermediateEventSymbols().get(0);
      assertThat(
            getAttribute(catchSignalEvent,
                  "stardust:bpmnEvent:" + catchSignalEventSymbol.getElementOid()),
            is(notNullValue()));

      assertThat(defaultLane.getStartEventSymbols().size(), is(0));
      assertThat(defaultLane.getEndEventSymbols().size(), is(0));

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      modelService.currentSession().modelPersistenceService()
            .saveMode(checklistModel, baos);
      System.out.println(baos.toString());
   }

   public static void assertCatchSignalIntermediateEventValidity(
         ActivityType catchSignalEvent)
   {
      assertThat(catchSignalEvent.getImplementation(),
            is(ActivityImplementationType.ROUTE_LITERAL));
      assertThat(catchSignalEvent.isHibernateOnCreation(), is(true));
      assertThat(getBooleanValue(catchSignalEvent, "stardust:bpmnIntermediateEventHost"),
            is(true));
      assertThat(catchSignalEvent.getEventHandler().size(), is(1));
      EventHandlerType signalEventHandler = catchSignalEvent.getEventHandler().get(0);
      assertThat(signalEventHandler.getType().getId(), is("signal"));
      assertThat(signalEventHandler.getName(), is("Wait for FileArrival"));
      assertThat(signalEventHandler.isAutoBind(), is(false));
      assertThat(signalEventHandler.isConsumeOnMatch(), is(false));
      assertThat(
            getAttributeValue(signalEventHandler,
                  "stardust:bpmn:signal:pastSignalsGracePeriod"), is("10"));

      assertThat(signalEventHandler.getBindAction().size(), is(0));
      assertThat(signalEventHandler.getUnbindAction().size(), is(0));
      assertThat(signalEventHandler.getEventAction().size(), is(1));
      assertThat(signalEventHandler.getEventAction().get(0).getType().getId(),
            is(PredefinedConstants.COMPLETE_ACTIVITY_ACTION));
   }

   public static void assertCatchSignalDataMappingValidity(ActivityType catchSignalEvent,
         DirectionType inOrOut, String msgProperty, DataType data, String dataPath)
   {
      assertThat(catchSignalEvent.getEventHandler().size(), is(1));
      EventHandlerType signalEventHandler = catchSignalEvent.getEventHandler().get(0);
      assertThat(signalEventHandler.getType().getId(), is("signal"));

      List<DataMappingType> mappings = newArrayList();
      String signalContext = "event-" + signalEventHandler.getId();
      for (DataMappingType dataMapping : catchSignalEvent.getDataMapping())
      {
         if (signalContext.equals(dataMapping.getContext())
               && inOrOut.equals(dataMapping.getDirection())
               && msgProperty.equals(dataMapping.getName()))
         {
            mappings.add(dataMapping);
         }
      }

      assertThat(mappings.size(), is(1));

      DataMappingType dataMapping = mappings.get(0);

      assertThat(dataMapping.getApplicationAccessPoint(), is(nullValue()));
      assertThat(dataMapping.getApplicationPath(), is(nullValue()));

      assertThat(dataMapping.getData(), is(notNullValue()));
      assertThat(dataMapping.getData(), is(sameInstance(data)));

      if (!isEmpty(dataPath))
      {
         assertThat(dataMapping.getDataPath(), is(dataPath));
      }
      else
      {
         assertThat(dataMapping.getDataPath(), is(nullValue()));
      }

      // TODO more assertions
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
