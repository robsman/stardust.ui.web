package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ActivityType;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

import org.junit.Test;

public class TestCloneModel extends TestCrossModelSupport
{

   @Test
   public void testCloneModelBasic() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      String command = "{\"commandId\":\"model.clone\",\"modelId\":\"ProviderModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000001\",\"changes\":{}}]}";
      replaySimple(command, "testCloneModelBasic", null, true); 
      
      ModelType clonedModel = modelService.getModelManagementStrategy().getModels().get("CLONE_ProviderModel");
      
      assertThat(clonedModel, is(not(nullValue())));

      GenericModelingAssertions.assertPrimitiveData(clonedModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      GenericModelingAssertions.assertStructData(clonedModel, "ProvidedStructData", "ProvidedStructData", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(clonedModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(clonedModel, "ProvidedProcess", "ProvidedProcess");
      GenericModelingAssertions.assertProcessInterface(clonedModel, "ProvidedProcess", "ProvidedProcess", 2);
      GenericModelingAssertions.assertPrimitiveFormalParameter(process, "InString", "InString", ModeType.IN, TypeType.STRING);
      GenericModelingAssertions.assertStructFormalParameter(process, "OutStruct", "OutStruct", ModeType.IN, "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertRole(clonedModel, "ProvidedRole", "ProvidedRole");
      GenericModelingAssertions.assertTypeDeclaration(clonedModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration");
      ActivityType activity1 = GenericModelingAssertions.assertActivity(process, "Activity1",  "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      ActivityType activity2 = GenericModelingAssertions.assertActivity(process, "Activity2",  "Activity 2", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertRole(clonedModel, "ProvidedRole", "ProvidedRole");

      
      GenericModelingAssertions.assertTransition(activity1, activity2);
   }
   
   @Test
   public void testCloneConsumerModel() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testBasicModelElementsInProvider();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/dragAndDropFromProviderToConsumer.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testDragAndDropFromProviderToConsumer", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerProcess", "ConsumerProcess");
      assertReferencedPrimitiveData(consumerModel, providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedDocumentData(consumerModel, providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");
      GenericModelingAssertions.assertReferencedRole(consumerModel, providerModel, "ProvidedRole", "ProvidedRole");
      
      String command = "{\"commandId\":\"model.clone\",\"modelId\":\"ConsumerModel\",\"account\":\"motu\",\"changeDescriptions\":[{\"uuid\":\"00000000-0000-0000-0000-000000000150\",\"changes\":{}}]}";
           
      replaySimple(command, "testCloneConsumerModel", null, true); 
      
      ModelType clonedModel = modelService.getModelManagementStrategy().getModels().get("CLONE_ConsumerModel");
      
      assertThat(clonedModel, is(not(nullValue())));
      
      process = GenericModelingAssertions.assertProcess(clonedModel, "ConsumerProcess", "ConsumerProcess");
      assertReferencedPrimitiveData(clonedModel, providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(clonedModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedDocumentData(clonedModel, providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(clonedModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(clonedModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");
      GenericModelingAssertions.assertReferencedRole(clonedModel, providerModel, "ProvidedRole", "ProvidedRole");
      
   }
   


}
