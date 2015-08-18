package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.ProcessDefinitionType;
import org.eclipse.stardust.model.xpdl.xpdl2.ModeType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestImplementsProcessInterface extends RecordingTestcase
{

   @Test
   public void testImplementProcessInterface() throws Throwable
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/implementProcessInterface.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testImplementProcessInterface", false);

      //saveReplayModel("c:/development/");

      ProcessDefinitionType processInterface = GenericModelingAssertions.assertProcessInterface(providerModel, "FirstInterface", "First Interface", 4);
      ProcessDefinitionType interfaceImplementation = GenericModelingAssertions.assertProcessInterface(consumerModel, "ImplementingProcess", "Implementing Process", 4);

      //Process Interface
      GenericModelingAssertions.assertPrimitiveFormalParameter(processInterface, "LongParameter", "Long Parameter", ModeType.IN, TypeType.INTEGER);
      GenericModelingAssertions.assertPrimitiveFormalParameter(processInterface, "BooleanParameter", "Boolean Parameter", ModeType.OUT, TypeType.BOOLEAN);
      GenericModelingAssertions.assertStructFormalParameter(processInterface, "StructParameter", "Struct Parameter", ModeType.INOUT, "ProviderStructDeclaration");
      GenericModelingAssertions.assertDocumentFormalParameter(processInterface, "DocumentParameter", "Document Parameter", ModeType.OUT, "ProviderDocumentStructType");

      //Interface Implementation
      GenericModelingAssertions.assertPrimitiveFormalParameter(interfaceImplementation, "LongParameter", "Long Parameter", ModeType.IN, TypeType.INTEGER);
      GenericModelingAssertions.assertPrimitiveFormalParameter(interfaceImplementation, "BooleanParameter", "Boolean Parameter", ModeType.OUT, TypeType.BOOLEAN);
      GenericModelingAssertions.assertStructFormalParameter(interfaceImplementation, "StructParameter", "Struct Parameter", ModeType.INOUT, "ProviderStructDeclaration");
      GenericModelingAssertions.assertDocumentFormalParameter(interfaceImplementation, "DocumentParameter", "Document Parameter", ModeType.OUT, "ProviderDocumentStructType");

      GenericModelingAssertions.assertInterfaceReference(interfaceImplementation, "ProviderModel", "FirstInterface");


   }




}
