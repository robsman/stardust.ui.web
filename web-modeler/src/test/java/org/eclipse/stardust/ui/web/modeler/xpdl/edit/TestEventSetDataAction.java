package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;


//import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestEventSetDataAction extends RecordingTestcase
{
   @Test
   public void testCreateSimpleIntermediateEvent() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createSimpleIntermediateEvent.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "createSimpleIntermediateEvent", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "MyEvent", ActivityImplementationType.MANUAL_LITERAL);
      EventHandlerType eventHandler = assertEventHandler(activity, "MyEvent", "MyEvent", "exception");
      assertThat(eventHandler.getEventAction().size(),  is(1));
      assertEventAction(eventHandler, "AbortActivity", "Abort Activity", "abortActivity");

      //saveReplayModel("C:/development");
   }

   @Test
   public void testCreateEventSetDataAction() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      testCreateSimpleIntermediateEvent();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createEventSetDataAction.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "createEventSetDataAction", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "MyEvent", ActivityImplementationType.MANUAL_LITERAL);
      EventHandlerType eventHandler = assertEventHandler(activity, "MyEvent", "MyEvent", "exception");
      assertThat(eventHandler.getEventAction().size(),  is(2));
      assertEventAction(eventHandler, "AbortActivity", "Abort Activity", "abortActivity");
      EventActionType action = assertEventAction(eventHandler, "_setDataAction_", "_setDataAction_", "setData");

      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, "MyStruct");
      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, "MyDataPath");
      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT, "carnot:engine:exception");
      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, "getMessage()");

   }



   @Test
   public void testRemoveEventSetDataAction() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      testCreateEventSetDataAction();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/removeEventSetDataAction.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "removeEventSetDataAction", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "MyEvent", ActivityImplementationType.MANUAL_LITERAL);
      EventHandlerType eventHandler = assertEventHandler(activity, "MyEvent", "MyEvent", "exception");
      assertThat(eventHandler.getEventAction().size(),  is(1));
      assertEventAction(eventHandler, "AbortActivity", "Abort Activity", "abortActivity");

   }

   @Test
   public void testCloneProcessCreateSimpleIntermediateEvent() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createSimpleIntermediateEvent.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "createSimpleIntermediateEvent", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"Process1\"}}]}";

      replaySimple(command, "testCloneProcessCreateSimpleIntermediateEvent", null, false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "CLONE_Process1", "CLONE - Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "MyEvent", ActivityImplementationType.MANUAL_LITERAL);
      EventHandlerType eventHandler = assertEventHandler(activity, "MyEvent", "MyEvent", "exception");
      assertThat(eventHandler.getEventAction().size(),  is(1));
      assertEventAction(eventHandler, "AbortActivity", "Abort Activity", "abortActivity");

      //saveReplayModel("C:/development");
   }

   @Test
   public void testCloneProcessCreateEventSetDataAction() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      testCreateSimpleIntermediateEvent();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createEventSetDataAction.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCloneProcessCreateEventSetDataAction", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"Process1\"}}]}";

      replaySimple(command, "testCloneProcessCreateSimpleIntermediateEvent", null, false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "CLONE_Process1", "CLONE - Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "MyEvent", ActivityImplementationType.MANUAL_LITERAL);
      EventHandlerType eventHandler = assertEventHandler(activity, "MyEvent", "MyEvent", "exception");
      assertThat(eventHandler.getEventAction().size(),  is(2));
      assertEventAction(eventHandler, "AbortActivity", "Abort Activity", "abortActivity");
      EventActionType action = assertEventAction(eventHandler, "_setDataAction_", "_setDataAction_", "setData");

      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_DATA_ID_ATT, "MyStruct");
      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_DATA_PATH_ATT, "MyDataPath");
      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_NAME_ATT, "carnot:engine:exception");
      GenericModelingAssertions.assertAttribute(action, PredefinedConstants.SET_DATA_ACTION_ATTRIBUTE_PATH_ATT, "getMessage()");

   }

   private EventHandlerType assertEventHandler(ActivityType activity, String id,
         String name, String type)
   {
      EventHandlerType foundHandler = null;
      for (Iterator<EventHandlerType> i = activity.getEventHandler().iterator(); i
            .hasNext();)
      {
         EventHandlerType eventHandler = i.next();
         if (eventHandler.getId().equals(id))
         {
            foundHandler = eventHandler;
         }
      }
      assertThat(foundHandler, is(not(nullValue())));
      assertThat(foundHandler.getName(), is(name));
      assertThat(foundHandler.getType().getId(), is(type));
      return foundHandler;
   }

   private EventActionType assertEventAction(EventHandlerType eventHandler, String id,  String name, String type)
   {
      EventActionType foundAction = null;
      for (Iterator<EventActionType> i = eventHandler.getEventAction().iterator(); i
            .hasNext();)
      {
         EventActionType eventAction = i.next();
         if (eventAction.getId().equals(id))
         {
            foundAction = eventAction;
         }
      }
      assertThat(foundAction, is(not(nullValue())));
      assertThat(foundAction.getName(), is(name));
      assertThat(foundAction.getType().getId(), is(type));
      return foundAction;
   }




   @Override
   protected boolean includeConsumerModel()
   {
      return false;
   }




}