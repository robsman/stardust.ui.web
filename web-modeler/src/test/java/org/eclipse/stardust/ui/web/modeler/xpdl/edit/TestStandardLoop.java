package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.xpdl2.LoopStandardType;
import org.eclipse.stardust.model.xpdl.xpdl2.LoopType;
import org.eclipse.stardust.model.xpdl.xpdl2.LoopTypeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TestTimeType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.XpdlUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestStandardLoop extends RecordingTestcase
{
   @Test
   public void testStandardLoop() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createStandardLoop.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "createStandardLoop");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "UserTask1", "User Task 1", ActivityImplementationType.MANUAL_LITERAL);
      assertStandardLoop(activity, "test condition", TestTimeType.BEFORE);

      // saveReplayModel("C:/tmp");
   }

   private void assertStandardLoop(ActivityType activity, String condition, TestTimeType before)
   {
      LoopType loop = activity.getLoop();
      assertThat(loop, is(not(nullValue())));
      assertThat(loop.getLoopType(), is(not(nullValue())));
      assertThat(loop.getLoopType().getLiteral(), is(LoopTypeType.STANDARD.getLiteral()));
      LoopStandardType loopStandard = loop.getLoopStandard();
      assertThat(loopStandard, is(not(nullValue())));
      String loopStandardCondition = XpdlUtil.getLoopStandardCondition(loopStandard);
      assertThat(loopStandardCondition, is(not(nullValue())));
      assertThat(loopStandardCondition, is(condition));
      TestTimeType testTime = loopStandard.getTestTime();
      assertThat(testTime, is(not(nullValue())));
      assertThat(testTime.getLiteral(), is(before.getLiteral()));
   }

   @Override
   protected boolean includeConsumerModel()
   {
      return false;
   }
}