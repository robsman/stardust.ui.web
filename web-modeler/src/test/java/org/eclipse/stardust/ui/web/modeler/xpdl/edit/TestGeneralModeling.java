package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
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

      replay(requestStream);

      GenericModelingAssertions.assertPrimitiveData(providerModel, "ProvidedPrimitive", "ProvidedPrimitive", "String");
      GenericModelingAssertions.assertStructData(providerModel, "ProvidedStructData", "ProvidedStructData", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertDocumentData(providerModel, "ProvidedDocument", "ProvidedDocument", "ProvidedTypeDeclaration");
      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "ProvidedProcess");
      GenericModelingAssertions.assertProcessInterface(providerModel, "ProvidedProcess", 2);
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");
      GenericModelingAssertions.assertTypeDeclaration(providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertActivity(process, "Activity1",  "Activity 1", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertActivity(process, "Activity2",  "Activity 2", ActivityImplementationType.MANUAL_LITERAL);
      GenericModelingAssertions.assertRole(providerModel, "ProvidedRole", "ProvidedRole");

      //saveReplayModel("C:/development/");

   }
}
