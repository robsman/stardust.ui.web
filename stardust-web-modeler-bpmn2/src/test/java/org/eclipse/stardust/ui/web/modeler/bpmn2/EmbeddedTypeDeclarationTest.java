package org.eclipse.stardust.ui.web.modeler.bpmn2;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils;
import org.eclipse.stardust.ui.web.modeler.spi.ModelPersistenceHandler.ModelDescriptor;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.util.XSDConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EmbeddedTypeDeclarationTest
{
   private final Bpmn2PersistenceHandler persistenceHandler = new Bpmn2PersistenceHandler();

   private Definitions model;

   private ItemDefinition typeDeclChildLevel;

   private ItemDefinition typeDeclTopLevel;

   @Before
   public void loadModel() throws IOException
   {
      InputStream fis = getClass().getResourceAsStream("EmbeddedTypeDeclaration.bpmn");
      try
      {
         ModelDescriptor<Definitions> modelDescriptor = persistenceHandler.loadModel(
               "EmbeddedTypeDeclaration.bpmn", fis);
         this.model = modelDescriptor.model;
      }
      finally
      {
         fis.close();
      }

      assertThat(model, is(not(nullValue())));

      for (RootElement rootElement : model.getRootElements())
      {
         if (rootElement instanceof ItemDefinition)
         {
            if ("SDT_ChildLevel".equals(extractTypeName((ItemDefinition) rootElement)))
            {
               this.typeDeclChildLevel = (ItemDefinition) rootElement;
            }
            else if ("SDT_TopLevel".equals(extractTypeName((ItemDefinition) rootElement)))
            {
               this.typeDeclTopLevel = (ItemDefinition) rootElement;
            }
         }
      }
   }

   @Test
   public void modelMustContainChildLevelTypeDeclaration()
   {
      assertThat(typeDeclChildLevel, is(not(nullValue())));
   }

   @Test
   public void childLevelTypeDeclarationMustContainAnEmbeddedSchema()
   {
      EObject embeddedSchema = Bpmn2ExtensionUtils.getExtensionElement(typeDeclChildLevel, "schema", XMLConstants.W3C_XML_SCHEMA_NS_URI);

      assertThat(embeddedSchema, is(not(nullValue())));

      assertThat(embeddedSchema, is(instanceOf(XSDSchema.class)));
   }

   @Test
   public void modelMustContainSchemaImport()
   {
      assertThat(model.getImports().size(), is(1));

      ItemDefinition itemDefinition = (ItemDefinition) model.getRootElements().get(0);

      String schemaLocation = extractSchemaLocation(itemDefinition);
      String typeId = extractTypeName(itemDefinition);

      Import importSpec = itemDefinition.getImport();
      if ((null == importSpec) && !isEmpty(schemaLocation))
      {
         for (Import candidate : Bpmn2Utils.findContainingModel(itemDefinition)
               .getImports())
         {
            if (schemaLocation.equals(candidate.getLocation()))
            {
               importSpec = candidate;
               break;
            }
         }
      }

      if (null != importSpec)
      {
         if (XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001.equals(importSpec.getImportType()))
         {
            Assert.fail("Should not find an import for " + schemaLocation);
         }
      }
      else
      {
         XSDSchema schema = null;

         Object embeddedSchema = Bpmn2ExtensionUtils.getExtensionElement(
               itemDefinition, "schema", XMLConstants.W3C_XML_SCHEMA_NS_URI);
         if (embeddedSchema instanceof XSDSchema)
         {
            if (schemaLocation.equals(((XSDSchema) embeddedSchema).getTargetNamespace()))
            {
               schema = (XSDSchema) embeddedSchema;
            }
         }

         assertThat(schema, is(not(nullValue())));
      }
   }

   @Test
   public void modelMustContainSchemaAfterSave() throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      persistenceHandler.saveModel(model, baos);

      baos.close();

      String savedModel = new String(baos.toByteArray());
      System.out.println(savedModel);
   }

   private static String extractSchemaLocation(ItemDefinition itemDefinition)
   {
      if (itemDefinition.getStructureRef() instanceof InternalEObject)
      {
         URI proxyURI = ((InternalEObject) itemDefinition.getStructureRef()).eProxyURI();
         if (proxyURI.hasFragment())
         {
            return proxyURI.trimFragment().toString();
         }
      }

      return null;
   }

   private static String extractTypeName(ItemDefinition itemDefinition)
   {
      if (itemDefinition.getStructureRef() instanceof InternalEObject)
      {
         URI proxyURI = ((InternalEObject) itemDefinition.getStructureRef()).eProxyURI();
         if (proxyURI.hasFragment())
         {
            return proxyURI.fragment();
         }
      }

      return null;
   }
}
