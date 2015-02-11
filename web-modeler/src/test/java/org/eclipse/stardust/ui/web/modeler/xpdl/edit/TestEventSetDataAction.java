package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

//import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

public class TestEventSetDataAction extends RecordingTestcase
{
   @Test
   public void testCreateSimpleIntermediateEvent() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createSimpleIntermediateEvent.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "createSimpleIntermediateEvent");

      //saveReplayModel("C:/development");
   }

   @Test
   public void testEventSetDataAction() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      testCreateSimpleIntermediateEvent();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createEventSetDataAction.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "createEventSetDataAction");

   }

   @Override
   protected boolean includeConsumerModel()
   {
      return false;
   }




}