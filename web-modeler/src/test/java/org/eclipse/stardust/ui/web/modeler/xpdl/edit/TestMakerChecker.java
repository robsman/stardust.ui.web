package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestMakerChecker extends RecordingTestcase
{

   @Test
   public void testCreateUserExclusions() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createUserExclusions.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCreateUserExclusions", true);

      // saveReplayModel("c:/development/");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process,
            "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(activity.getEventHandler(), is(not(nullValue())));
      assertThat(activity.getEventHandler().size(), is(1));
      EventHandlerType eventHandler = GenericModelingAssertions.assertEventHandler(activity, "_excludeUser_",
            "_excludeUser_", PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, false);
      assertThat(eventHandler.getEventAction(), is(not(nullValue())));
      assertThat(eventHandler.getEventAction().size(), is(6));
      assertExcludeUserAction(eventHandler, "FirstUserExclusion", "FirstUserExclusion",
            "PrimitiveData1", "aDataPath");
      assertExcludeUserAction(eventHandler, "SecondUserExclusion", "SecondUserExclusion",
            "PrimitiveData1", "aDataPath");
      assertExcludeUserAction(eventHandler, "SecondUserExclusion1",
            "SecondUserExclusion", "PrimitiveData1", "aDataPath");
      assertExcludeUserAction(eventHandler, "NoDataPathUserExclusion",
            "NoDataPathUserExclusion", "PrimitiveData1", null);
      assertExcludeUserAction(eventHandler, "ExcludeUser", "Exclude User",
            "PrimitiveData1", "aDataPath");
      assertExcludeUserAction(eventHandler, "ExcludeUser1", "Exclude User 1",
            "PrimitiveData1", "aDataPath");
   }

   @Test
   public void testUpdateUserExclusions() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/updateUserExclusions.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testUpdateUserExclusions", false);

      // saveReplayModel("c:/development/");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process,
            "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(activity.getEventHandler(), is(not(nullValue())));
      assertThat(activity.getEventHandler().size(), is(1));
      EventHandlerType eventHandler = GenericModelingAssertions.assertEventHandler(activity, "_excludeUser_",
            "_excludeUser_", PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, true);
      assertThat(eventHandler.getEventAction(), is(not(nullValue())));
      assertThat(eventHandler.getEventAction().size(), is(6));
      assertExcludeUserAction(eventHandler, "RenamedExclusion", "Renamed Exclusion",
            "PrimitiveData1", "aDataPath");
      assertExcludeUserAction(eventHandler, "RenamedExclusion1", "Renamed Exclusion",
            "PrimitiveData1", null);
      assertExcludeUserAction(eventHandler, "SecondUserExclusion1",
            "SecondUserExclusion", "PROCESS_ID", "aDataPath");
      assertExcludeUserAction(eventHandler, "AddedDataPathExclusion",
            "AddedDataPathExclusion", "PrimitiveData1", "addedDataPath");
      assertExcludeUserAction(eventHandler, "ExcludeUser", "Exclude User",
            "PrimitiveData1", "aDataPath");
      assertExcludeUserAction(eventHandler, "ExcludeUser1", "Exclude User 1",
            "PrimitiveData1", "aDataPath");

   }

   @Test
   public void testDeleteUserExclusions() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/deleteUserExclusions.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDeleteUserExclusions", false);

      // saveReplayModel("c:/development/");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(
            providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process,
            "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertThat(activity.getEventHandler(), is(not(nullValue())));
      assertThat(activity.getEventHandler().size(), is(1));
      EventHandlerType eventHandler = GenericModelingAssertions.assertEventHandler(activity, "_excludeUser_",
            "_excludeUser_", PredefinedConstants.ACTIVITY_ON_ASSIGNMENT_CONDITION, true);
      assertThat(eventHandler.getEventAction(), is(not(nullValue())));
      assertThat(eventHandler.getEventAction().size(), is(2));
      assertExcludeUserAction(eventHandler, "ExcludeUser", "Exclude User",
            "PrimitiveData1", "aDataPath");
      assertExcludeUserAction(eventHandler, "ExcludeUser1", "Exclude User 1",
            "PrimitiveData1", "aDataPath");

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

   public static EventActionType assertExcludeUserAction(EventHandlerType eventHandler,
         String id, String name, String data, String dataPath)
   {
      EventActionType eventAction = assertEventAction(eventHandler, id, name,
            PredefinedConstants.EXCLUDE_USER_ACTION);
      GenericModelingAssertions.assertAttribute(eventAction,
            PredefinedConstants.EXCLUDED_PERFORMER_DATA, data);
      GenericModelingAssertions.assertAttribute(eventAction,
            PredefinedConstants.EXCLUDED_PERFORMER_DATAPATH, dataPath);
      return eventAction;
   }

   protected boolean performResponseCallback()
   {
      return true;
   }

   public void testCreateUserExclusionsCallback(TestResponse response)
         throws AssertionError
   {
      if (response.getCommandID().equals("excludeUserAction.create"))
      {
         assertThat(response.getAdded().size(), is(1));
         JsonObject actionJson = response.getAdded().get(0).getAsJsonObject();

         if (response.getResponseNumber() == 8)
         {
            GenericModelingAssertions.assertJsonHas(actionJson, "uuid", "name", "data");
         } else
         {
            GenericModelingAssertions.assertJsonHas(actionJson, "uuid", "name", "data", "dataPath");
         }

         assertThat(response.getModified().size(), is(1));
         JsonObject activityJson = response.getModified().get(0).getAsJsonObject();
         GenericModelingAssertions.assertJsonHas(activityJson, "onAssignmentHandler");

         JsonObject onAssignmentJson = activityJson.get("onAssignmentHandler").getAsJsonObject();
         GenericModelingAssertions.assertJsonHas(onAssignmentJson, "userExclusions", "logHandler");

         JsonArray userExclusions = onAssignmentJson.get("userExclusions").getAsJsonArray();

         for(JsonElement element: userExclusions) GenericModelingAssertions.assertJsonHas(element.getAsJsonObject(), "uuid", "name", "data");

      }
   }

}
