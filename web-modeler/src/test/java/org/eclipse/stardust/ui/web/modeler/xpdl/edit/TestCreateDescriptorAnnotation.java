package org.eclipse.stardust.ui.web.modeler.xpdl.edit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.xsd.*;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.eclipse.stardust.engine.core.struct.XPathAnnotations;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;

public class TestCreateDescriptorAnnotation extends RecordingTestcase
{
   private final static String STORAGE_SCOPE = "storage";
   private final static String UI_SCOPE = "ui";
   private final static String DESCRIPTOR_ANNOTATION = "descriptor";
   private final static String FIELDNAME_LABEL_ANNOTATION = "InputPreferences_label";

   @Test
   public void testCreateDescriptorAnnotation() throws Exception
   {
      providerModel = modelService.findModel(PROVIDER_MODEL_ID);

      InputStream requestInput = getClass().getResourceAsStream(
            "../../service/rest/requests/createDescriptorAnnotation.txt");
      InputStreamReader requestStream = new InputStreamReader(requestInput);

      replay(requestStream, "CreateDescriptorAnnotation", false);

      TypeDeclarationType typeDeclaration = providerModel.getTypeDeclarations().getTypeDeclaration("Datenstruktur1");
      assertThat(typeDeclaration, is(not(nullValue())));
      XSDComplexTypeDefinition complexType = TypeDeclarationUtils.getComplexType(typeDeclaration);
      assertThat(complexType, is(not(nullValue())));
      XSDComplexTypeContent content = complexType.getContent();

      String annotationValue = null;
      String annotationLabelValue = null;
      if (content != null && content instanceof XSDParticle)
      {
         XSDParticleContent particleContent = ((XSDParticle) content).getContent();
         if (particleContent != null && particleContent instanceof XSDModelGroup)
         {
            for (XSDParticle xsdParticle : ((XSDModelGroup) particleContent).getContents())
            {
               XSDParticleContent particleContent2 = xsdParticle.getContent();
               if(particleContent2 != null && particleContent2 instanceof XSDElementDeclaration)
               {
                  XSDElementDeclaration xsdElementDeclaration = (XSDElementDeclaration) particleContent2;
                  XSDAnnotation annotation = xsdElementDeclaration.getAnnotation();
                  if (annotation != null)
                  {
                     for (Element appInfo : annotation.getApplicationInformation())
                     {
                        NodeList children = appInfo.getChildNodes();
                        for (int i = 0, l = children.getLength(); i < l; i++)
                        {
                           Node node = children.item(i);
                           if (node instanceof Element && node.getLocalName().equals(STORAGE_SCOPE))
                           {
                              NodeList list = node.getChildNodes();
                              for (int j = 0, numItems = list.getLength(); j < numItems; j++)
                              {
                                 Node childNode = list.item(j);
                                 if(childNode instanceof Element && childNode.getLocalName().equals(DESCRIPTOR_ANNOTATION))
                                 {
                                    annotationValue = XPathAnnotations.getValue((Element) childNode);
                                 }
                              }
                           }
                           if (node instanceof Element && node.getLocalName().equals(UI_SCOPE))
                           {
                              NodeList list = node.getChildNodes();
                              for (int j = 0, numItems = list.getLength(); j < numItems; j++)
                              {
                                 Node childNode = list.item(j);
                                 if(childNode instanceof Element && childNode.getLocalName().equals(FIELDNAME_LABEL_ANNOTATION))
                                 {
                                    annotationLabelValue = XPathAnnotations.getValue((Element) childNode);
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      assertThat(annotationValue, is(not(nullValue())));
      assertThat(annotationValue, is("true"));
      assertThat(annotationLabelValue, is(not(nullValue())));
      assertThat(annotationLabelValue, is("Decriptor A2"));

      //saveReplayModel("C:/tmp");
   }

   @Override
   protected boolean includeConsumerModel()
   {
      return false;
   }
}