package org.eclipse.stardust.ui.web.modeler.bpmn2.builder;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils.bpmn2Factory;

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
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;
import org.eclipse.xsd.util.XSDConstants;

import org.eclipse.stardust.ui.web.modeler.bpmn2.Bpmn2Utils;
import org.eclipse.stardust.ui.web.modeler.integration.ExternalXmlSchemaManager;
import org.eclipse.stardust.ui.web.modeler.model.TypeDeclarationJto;

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

      if ("ExternalReference".equals(jto.typeDeclaration.type.classifier))
      {
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
      }
      else if ("SchemaType".equals(jto.typeDeclaration.type.classifier))
      {
         // TODO
         throw new IllegalArgumentException("Not yet implemeted.");
      }
      else
      {
         throw new IllegalArgumentException("Unsupported type declaration classifier: "
               + jto.typeDeclaration.type.classifier);
      }

      return reference;
   }
}
