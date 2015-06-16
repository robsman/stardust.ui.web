package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.emf.common.util.EList;
import org.eclipse.xsd.*;
import org.junit.Test;

import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationsType;

public class TestEditXSDImportNoNamespace extends RecordingTestcase
{
   @Test
   public void testEditXSDImportNoNamespace() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/editXSDImportNoNamespace.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "editXSDImportNoNamespace", false);

      assertmodifiedXSD(providerModel);

      saveReplayModel("C:/tmp");
   }

   private void assertmodifiedXSD(ModelType providerModel)
   {
      TypeDeclarationsType typeDeclarations = providerModel.getTypeDeclarations();
      assertThat(typeDeclarations, is(not(nullValue())));
      TypeDeclarationType typeDeclaration = typeDeclarations.getTypeDeclaration("XSDDataStructure1");
      assertThat(typeDeclaration, is(not(nullValue())));
      XSDSchema schema = typeDeclaration.getSchema();
      assertThat(schema, is(not(nullValue())));
      
      XSDTypeDefinition foundType = null;      
      EList<XSDTypeDefinition> typeDefinitions = schema.getTypeDefinitions();
      XSDTypeDefinition typeDefinition = typeDefinitions.get(0);
      if(typeDefinition instanceof XSDComplexTypeDefinition)
      {
         XSDParticle complexType = typeDefinition.getComplexType();
         XSDParticleContent content = complexType.getContent();
         if(content instanceof XSDModelGroup)
         {
            EList<XSDParticle> contents = ((XSDModelGroup) content).getContents();
            for(XSDParticle particle : contents)
            {
               XSDParticleContent particleContent = particle.getContent();
               if(particleContent instanceof XSDElementDeclaration)
               {
                  XSDElementDeclaration element = (XSDElementDeclaration) particleContent;
                  if(element.getName().equals("New1"))
                  {
                     foundType = element.getType();
                     break;
                  }                  
               }
            }            
         }
      }
            
      assertThat(foundType, is(not(nullValue())));
      assertThat("Address", is(foundType.getName()));            
   }

   @Override
   protected boolean includeConsumerModel()
   {
      return false;
   }
}