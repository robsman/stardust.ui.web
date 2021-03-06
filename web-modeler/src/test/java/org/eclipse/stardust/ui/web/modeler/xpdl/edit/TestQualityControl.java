package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.emf.common.util.EList;
//import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
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

      replay(requestStream, "crossModelingQualityControl", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "Process1", "Process 1");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "Activity1", "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      assertQualityControlPerformer(activity, providerModel, true);

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/deleteQualityControl.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "deleteQualityControl", false);

      assertQualityControlPerformer(activity, providerModel, false);

      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createQualityAssuranceCodes.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "createQualityAssuranceCodes", false);
      
      assertQualityControlCodes(consumerModel, activity);
      
      // saveReplayModel("C:/tmp");
   }

   private void assertQualityControlCodes(ModelType consumerModel, ActivityType activity)
   {
      QualityControlType qualityControl = consumerModel.getQualityControl();
      assertThat(qualityControl, is(not(nullValue())));
      EList<Code> codes = qualityControl.getCode();
      assertThat(codes.size(), is(3));
      
      int cnt = 0;
      for(Code code : codes)
      {
         switch (cnt)
         {
            case 0: 
               assertThat(code.getCode(), is(not(nullValue())));
               assertThat(code.getCode(), is("test2"));
               assertThat(code.getName(), is(not(nullValue())));
               assertThat(code.getName(), is("ConsumerModel QA Code 2"));
               assertThat(code.getValue(), is(nullValue()));
               break;         
            case 1: 
               assertThat(code.getCode(), is(not(nullValue())));
               assertThat(code.getCode(), is("ConsumerModelQACode3"));
               assertThat(code.getName(), is(not(nullValue())));
               assertThat(code.getName(), is("ConsumerModel QA Code 3"));
               assertThat(code.getValue(), is(not(nullValue())));
               assertThat(code.getValue(), is("comment"));               
               break;         
            case 2: 
               assertThat(code.getCode(), is(not(nullValue())));
               assertThat(code.getCode(), is("test1"));
               assertThat(code.getName(), is(not(nullValue())));
               assertThat(code.getName(), is("test1"));
               assertThat(code.getValue(), is(nullValue()));
               break;                        
         }
         cnt++;         
      }   
      
      EList<Code> validQualityCodes = activity.getValidQualityCodes();
      assertThat(validQualityCodes.size(), is(1));      
      Code code = validQualityCodes.get(0);
      assertThat(code.getCode(), is("test1"));
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
         assertThat(probability, is("95"));
         assertThat(formula, is("false"));
      }
      else
      {
         assertThat(qualityControlPerformer, is(nullValue()));
         assertThat(probability, is(nullValue()));
         assertThat(formula, is(nullValue()));
      }
   }
}