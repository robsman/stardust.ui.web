package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;

public class TestDataDuplicateId extends RecordingTestcase
{
   private static final String DUPLICATE_ID = "StructuredData1";
   
   @Test
   public void testEventHandler() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeDataDuplicateId.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "create Data duplicate Id", false);

      boolean found = false;
      for(DataType data : consumerModel.getData())
      {
         String dataId = data.getId();
         
         if(found)
         {
            assertThat(DUPLICATE_ID, is(not(dataId)));            
         }
         
         if(DUPLICATE_ID.equals(dataId))
         {
            found = true;
         }         
      }      
   }
}