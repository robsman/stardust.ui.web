package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.getModelUuid;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.utils.Bpmn2ExtensionUtils.setExtensionValue;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.DocumentRoot;
import org.eclipse.bpmn2.Import;
import org.eclipse.bpmn2.ItemDefinition;
import org.eclipse.bpmn2.ItemKind;
import org.eclipse.bpmn2.util.ImportHelper;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.DynamicEObjectImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.util.NameIdUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.integration.ExternalXmlSchemaManager;
import org.eclipse.stardust.ui.web.modeler.model.TypeDeclarationJto;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDCompositor;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDFactory;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDPackage;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;

public class Bpmn2ItemDefinitionBuilder
{
   private final ExternalXmlSchemaManager externalXmlSchemaManager;

   public Bpmn2ItemDefinitionBuilder(ExternalXmlSchemaManager externalXmlSchemaManager)
   {
      this.externalXmlSchemaManager = externalXmlSchemaManager;
   }

   public void attachItemDefinition(Definitions model, ItemDefinition itemDefinition)
   {
      model.getRootElements().add(itemDefinition);
   }

   public ItemDefinition createXsdReference(Definitions model, TypeDeclarationJto jto)
   {
      ItemDefinition reference = bpmn2Factory().createItemDefinition();
      reference.setId( !isEmpty(jto.id) ? jto.id : Bpmn2Utils.createInternalId());

      if ( !"ExternalReference".equals(jto.typeDeclaration.type.classifier))
      {
         throw new IllegalArgumentException("Unsupported type declaration classifier: "
               + jto.typeDeclaration.type.classifier);
      }

      reference.setItemKind(ItemKind.INFORMATION);

      URI locationUri = URI.createURI(jto.typeDeclaration.type.location);
      URI canonicalLocationUri = ImportHelper.makeURICanonical(locationUri);

      XSDSchema xsdSchema = externalXmlSchemaManager.resolveSchemaFromUri(canonicalLocationUri.toString());

      // TODO create import, if necessary
      Import schemaImport = null;
      for (Import importSpec : model.getImports())
      {
         if (XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001.equals(importSpec.getImportType()))
         {
            if (canonicalLocationUri.toString().equals(importSpec.getLocation()))
            {
               schemaImport = importSpec;
               break;
            }
         }
      }
      if (null == schemaImport)
      {
         schemaImport = bpmn2Factory().createImport();
         schemaImport.setImportType(XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001);
         schemaImport.setLocation(canonicalLocationUri.toString());
         schemaImport.setNamespace(xsdSchema.getTargetNamespace());

         model.getImports().add(schemaImport);
      }

      DocumentRoot xmlDoc = (DocumentRoot) model.eContainer();
      if (null == xmlDoc)
      {
         xmlDoc = bpmn2Factory().createDocumentRoot();
         xmlDoc.setDefinitions(model);
      }
      if ( !xmlDoc.getXMLNSPrefixMap().containsValue(xsdSchema.getTargetNamespace()))
      {
         int counter = 1;
         while (xmlDoc.getXMLNSPrefixMap().containsKey("xsd_" + counter))
         {
            ++counter;
         }
         xmlDoc.getXMLNSPrefixMap().put("xsd_" + counter, xsdSchema.getTargetNamespace());
      }

      String xref = jto.typeDeclaration.type.xref;
      if (xref.startsWith("{"))
      {
         QName qnameRef = QName.valueOf(xref);

         if (qnameRef.getNamespaceURI().equals(xsdSchema.getTargetNamespace()))
         {
            for (XSDTypeDefinition typeDefinition : xsdSchema.getTypeDefinitions())
            {
               if (qnameRef.getLocalPart().equals(typeDefinition.getName()))
               {
                  InternalEObject typeDefinitionRef = new DynamicEObjectImpl();
                  URI uri = URI.createURI(schemaImport.getLocation() + "#" + typeDefinition.getName());
                  typeDefinitionRef.eSetProxyURI(uri);
                  reference.setImport(schemaImport);
                  reference.setStructureRef(typeDefinitionRef);
                  break;
               }
            }
         }
      }
      else
      {
         throw new IllegalArgumentException("Must provide a QName of the to be referenced element.");
      }

      reference.setIsCollection(false);

      return reference;
   }

   public ItemDefinition createEmbeddedXsd(Definitions model, TypeDeclarationJto jto)
   {
      ItemDefinition reference = bpmn2Factory().createItemDefinition();
      reference.setId( !isEmpty(jto.name)
            ? NameIdUtils.createIdFromName(jto.name)
            : Bpmn2Utils.createInternalId());

      if ( !isEmpty(jto.typeDeclaration.type.classifier)
            && !"SchemaType".equals(jto.typeDeclaration.type.classifier))
      {
         throw new IllegalArgumentException("Unsupported type declaration classifier: "
               + jto.typeDeclaration.type.classifier);
      }

      reference.setItemKind(ItemKind.INFORMATION);

      XSDSchema xsdSchema = createStructuredTypeDefinition(reference.getId(), jto.name,
            getModelUuid(model));

      setExtensionValue(reference, "schema", XMLConstants.W3C_XML_SCHEMA_NS_URI, xsdSchema);

      // TODO create import, if necessary
      DocumentRoot xmlDoc = (DocumentRoot) model.eContainer();
      if (null == xmlDoc)
      {
         xmlDoc = bpmn2Factory().createDocumentRoot();
         xmlDoc.setDefinitions(model);
      }
      if ( !xmlDoc.getXMLNSPrefixMap().containsValue(xsdSchema.getTargetNamespace()))
      {
         int counter = 1;
         while (xmlDoc.getXMLNSPrefixMap().containsKey("xsd_" + counter))
         {
            ++counter;
         }
         xmlDoc.getXMLNSPrefixMap().put("xsd_" + counter, xsdSchema.getTargetNamespace());
      }

      InternalEObject typeDefinitionRef = new DynamicEObjectImpl();
      URI uri = URI.createURI(xsdSchema.getTargetNamespace() + "#" + reference.getId());
      typeDefinitionRef.eSetProxyURI(uri);
      reference.setStructureRef(typeDefinitionRef);

      reference.setIsCollection(false);

      return reference;
   }

   /**
    * @see ModelBuilderFacade#createTypeDeclaration
    * TODO consolidate
    */
   public static XSDSchema createStructuredTypeDefinition(String typeID, String typeName, String modelId)
   {
      if (StringUtils.isEmpty(typeID))
      {
         typeID = NameIdUtils.createIdFromName(typeName);
      }

      XSDSchema xsdSchema = XSDFactory.eINSTANCE.createXSDSchema();

      xsdSchema.getQNamePrefixToNamespaceMap().put(XSDPackage.eNS_PREFIX,
            XMLResource.XML_SCHEMA_URI);
      xsdSchema.setSchemaForSchemaQNamePrefix(XSDPackage.eNS_PREFIX);

      xsdSchema.setTargetNamespace(TypeDeclarationUtils.computeTargetNamespace(modelId,
            typeID));
      String prefix = TypeDeclarationUtils.computePrefix(typeID,
            xsdSchema.getQNamePrefixToNamespaceMap().keySet());
      xsdSchema.getQNamePrefixToNamespaceMap()
            .put(prefix, xsdSchema.getTargetNamespace());
      xsdSchema.setSchemaLocation(StructuredDataConstants.URN_INTERNAL_PREFIX
            + typeID);

      XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();
      xsdComplexTypeDefinition.setName(typeID);
      XSDParticle particle = XSDFactory.eINSTANCE.createXSDParticle();
      XSDModelGroup modelGroup = XSDFactory.eINSTANCE.createXSDModelGroup();
      particle.setContent(modelGroup);
      modelGroup.setCompositor(XSDCompositor.SEQUENCE_LITERAL);
      xsdComplexTypeDefinition.setContent(particle);
      xsdSchema.getContents().add(xsdComplexTypeDefinition);

      XSDElementDeclaration xsdElementDeclaration = XSDFactory.eINSTANCE.createXSDElementDeclaration();
      xsdElementDeclaration.setName(typeID);
      xsdElementDeclaration.setTypeDefinition(xsdComplexTypeDefinition);
      xsdSchema.getContents().add(xsdElementDeclaration);

      // propagate ns-prefix mappings to DOM
      xsdSchema.updateElement(true);

      return xsdSchema;
   }
}
