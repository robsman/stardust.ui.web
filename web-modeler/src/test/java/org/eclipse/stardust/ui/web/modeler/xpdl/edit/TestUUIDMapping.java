package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.emf.ecore.EObject;
import org.junit.Test;

import org.eclipse.stardust.common.reflect.Reflect;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;

public class TestUUIDMapping extends RecordingTestcase
{

   @SuppressWarnings({"unchecked"})
   @Test
   public void testRemoveElementsFromUUIDMap() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/removeElementsFromUUIDMap.txt");
      requestStream = new InputStreamReader(requestInput);

      EObjectUUIDMapper mapper = modelService.currentSession().uuidMapper();                
      Map<UUID, EObject> objectMap = (Map<UUID, EObject>) Reflect.getFieldValue(mapper, "uuidEObjectMap");            
      List<EObject> unmappedObjects = (List<EObject>) Reflect.getFieldValue(mapper, "unmappedObjects");
      
      int mapSize = objectMap.size();
     
      replay(requestStream, "testRemoveElementsFromUUIDMap", false);
                 
      assertThat(unmappedObjects.size(), is(11));
      
      modelService.saveAllModels();
      
      assertThat(objectMap.size(), is(mapSize - 11));
      assertThat(unmappedObjects.size(), is(0));

      
   }
   
   @Test
   public void testPerformUndoSaveDelete() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      int dataCount = providerModel.getData().size();
      
      String command = "{\"commandId\":\"data.delete\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"ProvidedPrimitive\"}}]}";
      replaySimple(command, "testPerformUndoSaveDelete", null, false); 
      
      restController.undoMostCurrentChange();
      
      modelService.saveAllModels();
      
      replaySimple(command, "testPerformUndoSaveDelete", null, false); 
            
      assertThat(providerModel.getData().size(), is(dataCount - 1));
      
   }
    

}
