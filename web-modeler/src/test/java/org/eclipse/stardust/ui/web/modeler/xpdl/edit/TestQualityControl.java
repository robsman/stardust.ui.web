package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;









import org.junit.Ignore;
//import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestQualityControl extends RecordingTestcase
{
   @Test
   @Ignore
   public void testQualityControlPerformer() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/crossModelingQualityControl.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "crossModelingQualityControl", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "UserTask1", "User Task 1", ActivityImplementationType.MANUAL_LITERAL);
      assertQualityControlPerformer(activity, providerModel, true);

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/deleteQualityControl.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "deleteQualityControl", false);

      assertQualityControlPerformer(activity, providerModel, false);

      // saveReplayModel("C:/tmp");
   }

   private void assertQualityControlPerformer(ActivityType activity, ModelType providerModel, boolean exists)
   {
      IModelParticipant qualityControlPerformer = activity.getQualityControlPerformer();
      String probability = AttributeUtil.getCDataAttribute(activity, PredefinedConstants.QUALITY_ASSURANCE_PROBABILITY_ATT);
      String formula = AttributeUtil.getCDataAttribute(activity, PredefinedConstants.QUALITY_ASSURANCE_FORMULA_ATT);
      if(exists)
      {
         assertThat(qualityControlPerformer, is(not(nullValue())));
         GenericModelingAssertions.assertProxyReference(providerModel, qualityControlPerformer);
         assertThat(probability, is("92"));
         assertThat(formula, is("true"));
      }
      else
      {
         assertThat(qualityControlPerformer, is(nullValue()));
         assertThat(probability, is(nullValue()));
         assertThat(formula, is(nullValue()));
      }
   }
}