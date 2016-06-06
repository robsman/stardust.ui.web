package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.DataPathType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestDataPaths extends RecordingTestcase
{

   @Test
   public void testCreateDataPaths() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createDataPaths.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDataPaths", false);
     
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess", "ProvidedProcess");
      GenericModelingAssertions.assertDataPath(process, "DataPath_1", "DataPath_ 1", DirectionType.IN_LITERAL);
      GenericModelingAssertions.assertDataPath(process, "DataPath_2", "DataPath_ 2", DirectionType.OUT_LITERAL);
      GenericModelingAssertions.assertDataPath(process, "Renamed_3", "Renamed_3", DirectionType.IN_LITERAL);
      GenericModelingAssertions.assertDataPath(process, "Renamed_4", "Renamed_4", DirectionType.OUT_LITERAL);
   }
   
   @Test
   public void testAddDataPath() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createDataPaths.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDataPaths", false);
      
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess", "ProvidedProcess");
      DataPathType dataPath1 = GenericModelingAssertions.assertDataPath(process, "DataPath_1", "DataPath_ 1", DirectionType.IN_LITERAL);
      DataPathType dataPath2 = GenericModelingAssertions.assertDataPath(process, "DataPath_2", "DataPath_ 2", DirectionType.OUT_LITERAL);
      
      dataPath1.setId("FirstOne");
      dataPath2.setId("SecondOne");          
                
      String command = "{\"commandId\":\"modelElement.update\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"oid\":\"26\",\"changes\":{\"dataPathes\":[{\"id\":\"FirstOne\",\"name\":\"DataPath_ 1\",\"direction\":\"IN\",\"descriptor\":false,\"keyDescriptor\":false},{\"id\":\"SecondOne\",\"name\":\"DataPath_ 2\",\"direction\":\"OUT\",\"descriptor\":false,\"keyDescriptor\":false},{\"id\":\"Renamed_3\",\"name\":\"Renamed_3\",\"direction\":\"IN\",\"descriptor\":false,\"keyDescriptor\":false},{\"id\":\"DataPath_4\",\"name\":\"Renamed_4\",\"direction\":\"OUT\",\"descriptor\":false,\"keyDescriptor\":false},{\"id\":\"New_1\",\"name\":\"New 1\",\"direction\":\"IN\",\"descriptor\":false,\"keyDescriptor\":false}]}}]}";
      replaySimple(command, "testAddDataPath", null, true);    
      
      GenericModelingAssertions.assertDataPath(process, "FirstOne", "DataPath_ 1", DirectionType.IN_LITERAL);
      GenericModelingAssertions.assertDataPath(process, "SecondOne", "DataPath_ 2", DirectionType.OUT_LITERAL);
      GenericModelingAssertions.assertDataPath(process, "Renamed_3", "Renamed_3", DirectionType.IN_LITERAL);
      GenericModelingAssertions.assertDataPath(process, "Renamed_4", "Renamed_4", DirectionType.OUT_LITERAL);
      GenericModelingAssertions.assertDataPath(process, "DataPath_3", "DataPath_ 3", DirectionType.IN_LITERAL);
   }
   
  
}
