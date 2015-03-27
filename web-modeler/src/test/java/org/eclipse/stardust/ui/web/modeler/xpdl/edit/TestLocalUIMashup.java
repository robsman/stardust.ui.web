package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.stardust.model.xpdl.carnot.ActivityImplementationType;
import org.eclipse.stardust.model.xpdl.carnot.ApplicationType;
import org.eclipse.stardust.model.xpdl.carnot.ContextType;
import org.eclipse.stardust.model.xpdl.carnot.DirectionType;
import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;
import org.junit.Test;

public class TestLocalUIMashup extends TestGeneralModeling
{
   @Test
   public void testCreateProcessWithUIMashup() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      testBasicModelElementsInProvider();
      initUUIDMap();

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createProcessWithUIMashup.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCreateProcessWithUIMashup", false);

      ProcessDefinitionType process = GenericModelingAssertions.assertProcess(providerModel, "UIMashupProcess", "UIMashupProcess");
      assertUIMashupProcess(process);

      //saveReplayModel("C:/development/");

   }

   private void assertUIMashupProcess(ProcessDefinitionType process)
   {
      GenericModelingAssertions.assertActivity(process, "ProvidedUIMashup", "ProvidedUIMashup", ActivityImplementationType.APPLICATION_LITERAL);
      ApplicationType application = GenericModelingAssertions.assertApplication(providerModel, "ProvidedUIMashup");
      ContextType context = GenericModelingAssertions.assertApplicationContextType(application, "externalWebApp");
      GenericModelingAssertions.assertAccessPoint(context, "MashupInText", "MashupInText", DirectionType.IN_LITERAL, "primitive", "String");
      GenericModelingAssertions.assertAccessPoint(context, "MashupOutText", "MashupOutText", DirectionType.OUT_LITERAL, "primitive", "String");
      GenericModelingAssertions.assertAccessPoint(context, "MashupINOUTStruct", "Mashup INOUT Struct", DirectionType.OUT_LITERAL, "struct", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertAccessPoint(context, "MashupINOUTStruct", "Mashup INOUT Struct", DirectionType.IN_LITERAL, "struct", "ProvidedTypeDeclaration");
   }



}
