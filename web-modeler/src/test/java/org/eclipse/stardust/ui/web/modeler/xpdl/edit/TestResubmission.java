package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestResubmission extends RecordingTestcase
{
   @Test
   public void testCreateResubmission() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createResubmission.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCreateResubmission", false);

      //saveReplayModel("c:/development/");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process,
            "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(activity.getEventHandler().size(), is(1));
      EventHandlerType eventHandler = GenericModelingAssertions.assertEventHandler(activity, "Resubmission",
            "Resubmission", PredefinedConstants.TIMER_CONDITION, false);
      assertThat(eventHandler.getEventAction(), is(not(nullValue())));
      assertThat(eventHandler.getBindAction(), is(not(nullValue())));
      assertThat(eventHandler.getEventAction().size(), is(1));
      assertThat(eventHandler.getBindAction().size(), is(1));
      assertThat(eventHandler.isAutoBind(), is(false));
      
      BindActionType bindAction = eventHandler.getBindAction().get(0);
      GenericModelingAssertions.assertAttribute(bindAction, "carnot:engine:targetState", "7");

      EventActionType eventAction = eventHandler.getEventAction().get(0);
      GenericModelingAssertions.assertAttribute(eventAction, "carnot:engine:targetState", "5");

      GenericModelingAssertions.assertAttribute(eventHandler, "carnot:engine:useData", "false");

      GenericModelingAssertions.assertAttribute(eventHandler, "carnot:engine:period", "000000:000000:000000:000003:000000:000000");
   }

   @Test
   public void testChangeResubmission1() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeResubmission1.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testChangeResubmission1", true);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process,
            "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(activity.getEventHandler().size(), is(1));
      EventHandlerType eventHandler = GenericModelingAssertions.assertEventHandler(activity, "Resubmission",
            "Resubmission", PredefinedConstants.TIMER_CONDITION, false);
      assertThat(eventHandler.getEventAction(), is(not(nullValue())));
      assertThat(eventHandler.getBindAction(), is(not(nullValue())));
      assertThat(eventHandler.getEventAction().size(), is(2));
      assertThat(eventHandler.getBindAction().size(), is(1));
      assertThat(eventHandler.isAutoBind(), is(false));

      BindActionType bindAction = eventHandler.getBindAction().get(0);
      GenericModelingAssertions.assertAttribute(bindAction, "carnot:engine:targetState", "7");

      EventActionType eventAction = eventHandler.getEventAction().get(0);
      GenericModelingAssertions.assertAttribute(eventAction, "carnot:engine:targetState", "5");

      EventActionType delegateAction = eventHandler.getEventAction().get(1);
      GenericModelingAssertions.assertAttribute(delegateAction, "carnot:engine:targetWorklist", "defaultPerformer");

      GenericModelingAssertions.assertAttribute(eventHandler, "carnot:engine:useData", "true");

      GenericModelingAssertions.assertAttribute(eventHandler, "carnot:engine:period", null);
   }

   @Test
   public void testChangeResubmission2() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeResubmission2.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testChangeResubmission2", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process,
            "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(activity.getEventHandler().size(), is(1));
      EventHandlerType eventHandler = GenericModelingAssertions.assertEventHandler(activity, "Resubmission",
            "Resubmission", PredefinedConstants.TIMER_CONDITION, false);
      assertThat(eventHandler.getEventAction(), is(not(nullValue())));
      assertThat(eventHandler.getBindAction(), is(not(nullValue())));
      assertThat(eventHandler.getEventAction().size(), is(1));
      assertThat(eventHandler.getBindAction().size(), is(1));
      assertThat(eventHandler.isAutoBind(), is(false));

      BindActionType bindAction = eventHandler.getBindAction().get(0);
      GenericModelingAssertions.assertAttribute(bindAction, "carnot:engine:targetState", "7");

      EventActionType eventAction = eventHandler.getEventAction().get(0);
      GenericModelingAssertions.assertAttribute(eventAction, "carnot:engine:targetState", "5");

      GenericModelingAssertions.assertAttribute(eventHandler, "carnot:engine:useData", "false");

      GenericModelingAssertions.assertAttribute(eventHandler, "carnot:engine:period", "000000:000000:000000:000000:000000:000005");
   }

   @Test
   public void testDeleteResubmission() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/deleteResubmission.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDeleteResubmission", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process,
            "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(activity.getEventHandler().size(), is(0));
   }

   protected boolean includeConsumerModel()
   {
      return false;
   }

   public static EventActionType assertEventAction(EventHandlerType eventHandler,
         String id, String name, String type)
   {
      EventActionType eventAction = (EventActionType) ModelUtils.findIdentifiableElement(
            eventHandler,
            CarnotWorkflowModelPackage.eINSTANCE.getEventHandlerType_EventAction(), id);

      assertThat(eventAction.getName(), is(not(nullValue())));
      assertThat(eventAction.getType(), is(not(nullValue())));
      assertThat(eventAction.getType().getId(), is(not(nullValue())));
      assertThat(eventAction.getType().getName(), is(not(nullValue())));
      assertThat(eventAction.getName(), is(name));
      assertThat(eventAction.getType().getId(), is(type));
      // Todo: Assert the type explicitly?
      return eventAction;
   }

   protected boolean performResponseCallback()
   {
      return true;
   }

   public void testChangeResubmission1Callback(TestResponse response)
         throws AssertionError
   {
      if (response.getResponseNumber() == 10)
      {
         assertThat(response.getModified().size(), is(1));
         JsonObject activityJson = response.getModified().get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHas(activityJson, "resubmissionHandler");
      }

      if (response.getResponseNumber() == 9)
      {
         assertThat(response.getModified().size(), is(2));
         JsonObject resubmissionJson = response.getModified().get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHasKeyValue(resubmissionJson,
               "dataFullId=ProviderModel:currentUser", "dataPath=myDataPath",
               "useData=true", "defaultPerformer=false");
      }
   }
}