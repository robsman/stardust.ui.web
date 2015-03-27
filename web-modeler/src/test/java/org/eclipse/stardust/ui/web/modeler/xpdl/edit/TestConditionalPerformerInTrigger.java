package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.carnot.TriggerType;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestConditionalPerformerInTrigger extends RecordingTestcase
{
   @Test
   public void testConditionalPerformerInTrigger() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      // enhance test that it should work for roles
      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/conditionalPerformerInTrigger.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "conditionalPerformerInTrigger", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "Process1", "Process 1");
      assertTrigger(process);

      //saveReplayModel("C:/tmp");
   }

   private void assertTrigger(ProcessDefinitionType process)
   {
      TriggerType c1 = null;
      TriggerType c2 = null;
      TriggerType c3 = null;
      TriggerType r1 = null;
      TriggerType r2 = null;
      TriggerType r3 = null;
      for(TriggerType trigger : process.getTrigger())
      {
         if(trigger.getId().equals("ManualTrigger1"))
         {
            c1 = trigger;
         }
         if(trigger.getId().equals("ManualTrigger2"))
         {
            c2 = trigger;
         }
         if(trigger.getId().equals("ManualTrigger3"))
         {
            c3 = trigger;
         }
         if(trigger.getId().equals("ManualTrigger4"))
         {
            r1 = trigger;
         }
         if(trigger.getId().equals("ManualTrigger5"))
         {
            r2 = trigger;
         }
         if(trigger.getId().equals("ManualTrigger6"))
         {
            r3 = trigger;
         }
      }
      assertThat(c1, is(not(nullValue())));
      assertThat(AttributeUtil.getAttribute(c1, PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT), is(nullValue()));
      assertThat(c2, is(not(nullValue())));
      assertThat(AttributeUtil.getAttribute(c2, PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT), is(nullValue()));
      assertThat(c3, is(not(nullValue())));
      assertThat(AttributeUtil.getAttribute(c3, PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT), is(nullValue()));
      assertThat(r1, is(not(nullValue())));
      assertThat(AttributeUtil.getAttribute(r1, PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT), is(not(nullValue())));
      assertThat(r2, is(not(nullValue())));
      assertThat(AttributeUtil.getAttribute(r2, PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT), is(not(nullValue())));
      assertThat(r3, is(not(nullValue())));
      assertThat(AttributeUtil.getAttribute(r3, PredefinedConstants.MANUAL_TRIGGER_PARTICIPANT_ATT), is(not(nullValue())));
   }

   @Override
   protected boolean includeConsumerModel()
   {
      return false;
   }
}