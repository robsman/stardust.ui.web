package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

public class TestDataMappings extends RecordingTestcase
{
   @Test
   public void testUIMashUpDatamappings() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      //consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createUIMashupDataMappingOperations.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "DatamappingProcess","Datamapping Process");

      //saveReplayModel("C:/development/");
   }

   protected boolean includeConsumerModel()
   {
      return false;
   }


}
