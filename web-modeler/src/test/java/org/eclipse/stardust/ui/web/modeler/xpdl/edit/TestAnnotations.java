package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.ui.web.modeler.utils.test.GenericModelingAssertions;

public class TestAnnotations extends RecordingTestcase
{

   @Test
   public void testAnnotations() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);
      consumerModel = modelService.findModel(CONSUMER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createBasicModelElementsInProvider.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testBasicModelElementsInProvider", false);
      
      requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createAnnotations.txt");
      requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "testCreateAnnotations", false);
      
      TypeDeclarationType declaration = GenericModelingAssertions.assertTypeDeclaration(providerModel, "ProvidedTypeDeclaration", "ProvidedTypeDeclaration");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "InputPreferences_label", "MyLabel");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "InputPreferences_labelKey", "MyLabelKey");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "InputPreferences_showDescription", "true");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "NumericInputPreferences_showGroupingSeparator", "true");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "InputPreferences_mandatory", "true");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "StringInputPreferences_stringInputType", "TEXTAREA");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "BooleanInputPreferences_readonlyOutputType", "TEXTOUTPUT");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "descriptor", "true");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "persistent", "false");
      GenericModelingAssertions.assertAnnotationForTypeDeclaration(declaration, "New3", "indexed", "false");
       
   }
   

   
   
   

}
