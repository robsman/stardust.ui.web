package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

import org.junit.Test;

public class TestGeneralModeling extends RecordingTestcase
{

   @Test
   public void testBasicModelElementsInProvider() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);

      GenericModelingAssertions.assertPrimitiveData(providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      GenericModelingAssertions.assertStructData(providerModel, "ProvidedStructData", "ProvidedStructData", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess", "ProvidedProcess");
      GenericModelingAssertions.assertProcessInterface(providerModel, "ProvidedProcess", "ProvidedProcess", 2);
      GenericModelingAssertions.assertPrimitiveFormalParameter(process, "InString", "InString", ModeType.IN, TypeType.STRING);
      GenericModelingAssertions.assertStructFormalParameter(process, "OutStruct", "OutStruct", ModeType.IN, "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");
      GenericModelingAssertions.assertTypeDeclaration(providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration");
      ActivityType activity1 = GenericModelingAssertions.assertActivity(process, "Activity1",  "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      ActivityType activity2 = GenericModelingAssertions.assertActivity(process, "Activity2",  "Activity 2", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");


      GenericModelingAssertions.assertTransition(activity1, activity2);
      

   }
   
   @Test
   public void testDeleteActivity() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      String command = "{\"commandId\":\"activitySymbol.delete\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"27\",\"changes\":{\"modelElement\":{\"id\":\"Activity1\"}}}]}";
      replaySimple(command, "testDeleteActivity", null, false);    

      GenericModelingAssertions.assertPrimitiveData(providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      GenericModelingAssertions.assertStructData(providerModel, "ProvidedStructData", "ProvidedStructData", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess", "ProvidedProcess");
      GenericModelingAssertions.assertProcessInterface(providerModel, "ProvidedProcess", "ProvidedProcess", 2);
      GenericModelingAssertions.assertPrimitiveFormalParameter(process, "InString", "InString", ModeType.IN, TypeType.STRING);
      GenericModelingAssertions.assertStructFormalParameter(process, "OutStruct", "OutStruct", ModeType.IN, "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");
      GenericModelingAssertions.assertTypeDeclaration(providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertActivity(process, "Activity2",  "Activity 2", ActivityImplementationType.MANUAL_LITERAL);

      
      GenericModelingAssertions.assertActivityDoesNotExist(process, "Activity1");
     
   }
   
   @Test
   public void testDeleteData() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      String command = "{\"commandId\":\"data.delete\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"ProvidedPrimitive\"}}]}";
      replaySimple(command, "testDeleteData (Primitive)", null, false);

      command = "{\"commandId\":\"data.delete\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"ProvidedStruct\"}}]}";
      replaySimple(command, "testDeleteData (Struct)", null, false);

      GenericModelingAssertions.assertDataDoesNotExist(providerModel, "ProvidedPrimitive");
      GenericModelingAssertions.assertDataDoesNotExist(providerModel, "ProvidedStruct");
      GenericModelingAssertions.assertStructData(providerModel, "ProvidedStructData", "ProvidedStructData", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess", "ProvidedProcess");
      GenericModelingAssertions.assertProcessInterface(providerModel, "ProvidedProcess", "ProvidedProcess", 2);
      GenericModelingAssertions.assertPrimitiveFormalParameter(process, "InString", "InString", ModeType.IN, TypeType.STRING);
      GenericModelingAssertions.assertStructFormalParameter(process, "OutStruct", "OutStruct", ModeType.IN, "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");
      GenericModelingAssertions.assertTypeDeclaration(providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertActivity(process, "Activity1",  "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertActivity(process, "Activity2",  "Activity 2", ActivityImplementationType.MANUAL_LITERAL);
     
   }
   
   @Test
   public void testDeleteProcess() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      String command = "{\"commandId\":\"process.delete\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"ProvidedProcess\"}}]}";
      replaySimple(command, "testDeleteProcess", null, false);

      GenericModelingAssertions.assertProcessDoesNotExist(providerModel, "ProvidedProcess");
     
   }

   @Test
   public void testCloneProcessBasicModelElementsInProvider() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCloneProcessBasicModelElementsInProvider", false);

      String command = "{\"commandId\":\"process.clone\",\"modelId\":\"ProviderModel\",\"changeDescriptions\":[{\"oid\":\"ProviderModel\",\"changes\":{\"id\":\"ProvidedProcess\"}}]}";

      replaySimple(command, "Clone Process", null, false);

      GenericModelingAssertions.assertPrimitiveData(providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      GenericModelingAssertions.assertStructData(providerModel, "ProvidedStructData", "ProvidedStructData", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "CLONE_ProvidedProcess", "CLONE - ProvidedProcess");
      GenericModelingAssertions.assertProcessInterface(providerModel, "CLONE_ProvidedProcess", "CLONE - ProvidedProcess", 2);
      GenericModelingAssertions.assertPrimitiveFormalParameter(process, "InString", "InString", ModeType.IN, TypeType.STRING);
      GenericModelingAssertions.assertStructFormalParameter(process, "OutStruct", "OutStruct", ModeType.IN, "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");
      GenericModelingAssertions.assertTypeDeclaration(providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration");
      ActivityType activity1 = GenericModelingAssertions.assertActivity(process, "Activity1",  "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      ActivityType activity2 = GenericModelingAssertions.assertActivity(process, "Activity2",  "Activity 2", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");

      GenericModelingAssertions.assertTransition(activity1, activity2);
      //saveReplayModel("C:/development/");

   }

   @Test
   public void testDeleteConnections() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testBasicModelElementsInProvider();

      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/deleteConnections.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDeleteConnections", false);

      //saveReplayModel("C:/development/");

   }
}
