package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;





//import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestQualityControl extends RecordingTestcase
{
   @Test
   public void testQualityControlPerformer() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/crossModelingQualityControl.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "crossModelingQualityControl");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "UserTask1", "User Task 1", ActivityImplementationType.MANUAL_LITERAL);
      assertQualityControlPerformer(activity, providerModel);

      // saveReplayModel("C:/tmp");
   }

   private void assertQualityControlPerformer(ActivityType activity, ModelType providerModel)
   {
      IModelParticipant qualityControlPerformer = activity.getQualityControlPerformer();
      assertThat(qualityControlPerformer, is(not(nullValue())));
      GenericModelingAssertions.assertProxyReference(providerModel, qualityControlPerformer);
   }
}