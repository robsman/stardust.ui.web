package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.carnot.util.AttributeUtil;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestModelRenamingCrossModel extends TestCrossModelSupport
{
   @Test
   public void testChangeModelIDAfterDragAndDrop() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testDragAndDropFromProviderToConsumer();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeModelID.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testChangeModelIDAfterDragAndDrop");

      //saveReplayModel("C:/development/");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerProcess", "ConsumerProcess");
      assertReferencedPrimitiveData(consumerModel, providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedDocumentData(consumerModel, providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");
      GenericModelingAssertions.assertReferencedRole(consumerModel, providerModel, "ProvidedRole", "ProvidedRole");
   }

   @Test
   public void testChangeModelIDAfterDropDown() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingByDropDown();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeModelID.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testChangeModelIDAfterDropDown");

      //saveReplayModel("C:/development/");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerSubProcess", "ConsumerSubProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ConsumerUIMashup", "ConsumerUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      assertReferencedApplication(consumerModel, providerModel, process, activity, "ProvidedUIMashup", "cnx://file/application/ProvidedUIMashup");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerStructData", "ConsumerStructData", "struct", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      assertReferencedTypeDeclaration(consumerModel, providerModel, "ConsumerDocData", "ConsumerDocData", "dmsDocument", "ProvidedTypeDeclaration", "cnx://file/typeDeclaration/ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(consumerModel, "ConsumerDocData", "ConsumerDocData", "RenamedModel{ProvidedTypeDeclaration}");

   }

   @Test
   public void testChangeModelIDAfterSwitchSubprocessFromLocalToRemote() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testSwitchSubprocessFromLocalToRemote();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeModelID.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testSwitchSubprocessFromLocalToRemote");

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(consumerModel, "ConsumerProcess", "ConsumerProcess");
      ActivityType activity = GenericModelingAssertions.assertActivity(process, "ProvidedProcess", "ProvidedProcess", ActivityImplementationType.SUBPROCESS_LITERAL);
      assertReferencedProcess(consumerModel, providerModel, process, activity, "ProvidedProcess", "cnx://file/processDefinition/ProvidedProcess");

      // saveReplayModel("C:/development/");

   }

   @Test
   public void testChangeModelIDAfterCrossModelingUIMashupParameter() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testCrossModelingUIMashupParameter();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/changeModelID.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);
      replay(requestStream, "testChangeModelIDAfterCrossModelingUIMashupParameter");

      ApplicationType application = GenericModelingAssertions.assertApplication(consumerModel, "ConsumerUIMashup");
      ContextType context = GenericModelingAssertions.assertApplicationContextType(application, "externalWebApp");

      AccessPointType accessPoint = assertReferencedStructAccessPoint(providerModel, context, "StructPArameterINAndOUT", "Struct PArameter IN And OUT", DirectionType.IN_LITERAL, "typeDeclaration:{RenamedModel}ProvidedTypeDeclaration");
      AttributeType attribute = AttributeUtil.getAttribute(accessPoint, "IS_INOUT_PARAM");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is("true"));

      assertReferencedStructAccessPoint(providerModel, context, "StructPArameterINAndOUT", "Struct PArameter IN And OUT", DirectionType.OUT_LITERAL, "typeDeclaration:{RenamedModel}ProvidedTypeDeclaration");
      attribute = AttributeUtil.getAttribute(accessPoint, "IS_INOUT_PARAM");
      assertThat(attribute, is(not(nullValue())));
      assertThat(attribute.getAttributeValue(), is("true"));


      //saveReplayModel("C:/development/");

   }



}
