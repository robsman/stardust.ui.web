package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestCompositeDescriptors extends RecordingTestcase
{

   @Test
   public void testCompositeCreateDescriptor() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createCompositeDescriptors.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCreateCompositeDescriptors", false);
           
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess", "ProvidedProcess");
      DataPathType dataPath = GenericModelingAssertions.assertDataPath(process, "DataPath_1", "DataPath_ 1", null, true, false, DirectionType.IN_LITERAL, "http://www.link.com");
      GenericModelingAssertions.assertAttribute(dataPath, "type", "Link");
      GenericModelingAssertions.assertAttribute(dataPath, "text", "This is a link.");       
   }
   
   @Test
   public void validateCompositeDescriptor() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/validateCompositeDescriptors.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testValidateCompositeDescriptors", false);
            
      //JsonArray validation = modelService.validateModel("ProviderModel");
      
      /*JsonObject issue1 = validation.get(3).getAsJsonObject();
      JsonObject issue2 = validation.get(4).getAsJsonObject();
      JsonObject issue3 = validation.get(5).getAsJsonObject();
      JsonObject issue4 = validation.get(6).getAsJsonObject();
      JsonObject issue5 = validation.get(7).getAsJsonObject();
      
      GenericModelingAssertions.assertJsonHasKeyValue(issue1,"message=Composite descriptor 'FirstOne' has a circular dependency.");
      GenericModelingAssertions.assertJsonHasKeyValue(issue2,"message=Composite descriptor 'FirstOne' refers to a non existing data path '%{Hello}'.");
      GenericModelingAssertions.assertJsonHasKeyValue(issue3,"message=Composite descriptor 'SecondOne' has a circular dependency.");
      GenericModelingAssertions.assertJsonHasKeyValue(issue4,"message=Composite descriptor 'ThirdOne' refers to a non existing data path '%{Hello}'.");
      GenericModelingAssertions.assertJsonHasKeyValue(issue5,"message=Composite descriptor 'ForthOne' has a circular dependency.");*/            
   }

}
