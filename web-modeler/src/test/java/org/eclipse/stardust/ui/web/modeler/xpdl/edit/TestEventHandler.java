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
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestEventHandler extends RecordingTestcase
{
   @Test
   public void testEventHandler() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createEventHandler.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "create Event Handler");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      EventHandlerType eventHandler = assertEventHandler(activity, "abc", "abc", "exception");      
      assertThat(eventHandler.isConsumeOnMatch(), is(true));
      assertThat(eventHandler.isLogHandler(), is(true));      
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/updateEventHandler.txt");
      requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "update Event Handler");

      process = GenericModelingAssertions.assertProcess(providerModel, "Process1", "Process 1");
      activity = GenericModelingAssertions.assertActivity(process, "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      eventHandler = assertEventHandler(activity, "abc", "abc", "exception");
      assertThat(eventHandler.isConsumeOnMatch(), is(false));
      assertThat(eventHandler.isLogHandler(), is(false));      
      
      // saveReplayModel("C:/tmp");
   }

   private EventHandlerType assertEventHandler(ActivityType activity, String id, String name, String type)
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
  
   protected boolean includeConsumerModel()
   {
      return false;
   }   
}