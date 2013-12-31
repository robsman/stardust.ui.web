/*******************************************************************************
 * Copyright (c) 2011 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.modeler.edit.model.element;

import static org.eclipse.stardust.common.StringUtils.isEmpty;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.extractString;
import static org.eclipse.stardust.ui.web.modeler.marshaling.GsonUtils.hasNotJsonNull;

import java.util.Iterator;

import javax.annotation.Resource;

import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.stardust.common.error.PublicException;
import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;
import org.eclipse.stardust.engine.api.model.PredefinedConstants;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.builder.common.EObjectUUIDMapper;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelBuilderFacade;
import org.eclipse.stardust.model.xpdl.builder.utils.ModelerConstants;
import org.eclipse.stardust.model.xpdl.carnot.*;
import org.eclipse.stardust.model.xpdl.util.NameIdUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.*;
import org.eclipse.stardust.model.xpdl.xpdl2.util.ExtendedAttributeUtil;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.ui.web.modeler.edit.ModelElementEditingUtils;
import org.eclipse.stardust.ui.web.modeler.edit.spi.CommandHandler;
import org.eclipse.stardust.ui.web.modeler.edit.spi.OnCommand;
import org.eclipse.stardust.ui.web.modeler.edit.utils.CommandHandlerUtils;
import org.eclipse.stardust.ui.web.modeler.service.ModelService;
import org.eclipse.xsd.*;
import org.springframework.context.ApplicationContext;

import com.google.gson.JsonObject;

/**
 * @author Shrikant.Gangal
 *
 */
@CommandHandler
public class DataChangeCommandHandler
{
   private static final Logger trace = LogManager.getLogger(DataChangeCommandHandler.class);

   @Resource
   private ApplicationContext springContext;

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "primitiveData.create")
   public void createPrimitiveData(ModelType model, JsonObject request)
   {
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);
      String primitiveType = extractString(request, ModelerConstants.PRIMITIVE_TYPE);
      DataType data = getModelBuilderFacade().createPrimitiveData(model, null, name, primitiveType);

      //Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(data);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "typeDeclaration.create")
   public void createTypeDeclaration(ModelType model, JsonObject request)
   {
      boolean duplicate = false;
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      if (isEmpty(id))
      {
         // must keep the original name as ID as otherwise the type can't be resolved from the schema
         id = name;
      }

      TypeDeclarationsType declarations = model.getTypeDeclarations();
      // lazily initialize type declarations container
      if (declarations == null)
      {
         declarations = XpdlFactory.eINSTANCE.createTypeDeclarationsType();
         model.setTypeDeclarations(declarations);
      }
      else if (declarations.getTypeDeclaration(id) != null)
      {
         duplicate = true;
      }

      if (trace.isDebugEnabled())
      {
         trace.debug("Creating Type Declaration " + request);
      }

      TypeDeclarationType declaration = XpdlFactory.eINSTANCE.createTypeDeclarationType();
      declarations.getTypeDeclaration().add(declaration);

      if(duplicate)
      {
         id = NameIdUtils.createIdFromName(null, declaration, id);    
         name = id;
      }
      
      declaration.setId(id);
      declaration.setName(name);

      JsonObject td = request.getAsJsonObject("typeDeclaration");
      JsonObject type = td.getAsJsonObject("type");

      if ("ExternalReference".equals(extractString(type, "classifier")))
      {
         // import schema type by reference
         ExternalReferenceType reference = XpdlFactory.eINSTANCE.createExternalReferenceType();
         reference.setLocation(extractString(type, "location"));

         // TODO: hasNotJsonNull required here?
         if (type.has("namespace"))
         {
            reference.setNamespace(extractString(type, "namespace"));
         }
         // TODO: hasNotJsonNull required here?
         if (type.has("xref"))
         {
            reference.setXref(extractString(type, "xref"));
         }

         declaration.setExternalReference(reference);
      }
      else if ("SchemaType".equals(extractString(type, "classifier")))
      {
         // TODO import schema by value

         // TODO: pass that value ?
         ExtendedAttributeUtil.createAttribute(declaration,
               PredefinedConstants.MODELELEMENT_VISIBILITY).setValue("Public"); //$NON-NLS-1$

         // TODO: support external references
         String classifier = type.getAsJsonPrimitive("classifier").getAsString();
         if (!"SchemaType".equals(classifier))
         {
            // TODO: change to error case ?
            throw new PublicException("Only Schema types are supported: '" + classifier + "'.");
         }

         JsonObject jsSschema = td.getAsJsonObject("schema");
         String targetNamespace = jsSschema.has("targetNamespace")
               ? jsSschema.getAsJsonPrimitive("targetNamespace").getAsString()
               : TypeDeclarationUtils.computeTargetNamespace(model, declaration.getId());

         SchemaTypeType schema = XpdlFactory.eINSTANCE.createSchemaTypeType();
         declaration.setSchemaType(schema);

         XSDSchema xsdSchema = XSDFactory.eINSTANCE.createXSDSchema();
         xsdSchema.getQNamePrefixToNamespaceMap().put(XSDPackage.eNS_PREFIX, XMLResource.XML_SCHEMA_URI);
         xsdSchema.setSchemaForSchemaQNamePrefix(XSDPackage.eNS_PREFIX);
         xsdSchema.setTargetNamespace(targetNamespace);

         String prefix = TypeDeclarationUtils.computePrefix(declaration.getId(), xsdSchema.getQNamePrefixToNamespaceMap().keySet());
         xsdSchema.getQNamePrefixToNamespaceMap().put(prefix, xsdSchema.getTargetNamespace());
         xsdSchema.setSchemaLocation(StructuredDataConstants.URN_INTERNAL_PREFIX + declaration.getId());
         schema.setSchema(xsdSchema);

         JsonObject jsTypes = jsSschema.getAsJsonObject("types");
         JsonObject jsType = jsTypes.getAsJsonObject(id);

         XSDTypeDefinition xsdTypeDefinition = jsType.has("body") ? createComplexType(id) : createSimpleType(xsdSchema, id);
         xsdSchema.getContents().add(xsdTypeDefinition);

         XSDElementDeclaration xsdElementDeclaration = XSDFactory.eINSTANCE.createXSDElementDeclaration();
         xsdElementDeclaration.setName(declaration.getId());
         xsdElementDeclaration.setTypeDefinition(xsdTypeDefinition);
         xsdSchema.getContents().add(xsdElementDeclaration);

         modelService().currentSession().modelElementUnmarshaller()
               .populateFromJson(declaration, request);
      }

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(declaration);
   }

   private XSDComplexTypeDefinition createComplexType(String id)
   {
      XSDComplexTypeDefinition xsdComplexTypeDefinition = XSDFactory.eINSTANCE.createXSDComplexTypeDefinition();
      xsdComplexTypeDefinition.setName(id);
      XSDParticle particle = XSDFactory.eINSTANCE.createXSDParticle();
      XSDModelGroup modelGroup = XSDFactory.eINSTANCE.createXSDModelGroup();
      particle.setContent(modelGroup);
      modelGroup.setCompositor(XSDCompositor.SEQUENCE_LITERAL);
      xsdComplexTypeDefinition.setContent(particle);
      return xsdComplexTypeDefinition;
   }

   private XSDSimpleTypeDefinition createSimpleType(XSDSchema xsdSchema, String id)
   {
      XSDSimpleTypeDefinition xsdSimpleTypeDefinition = XSDFactory.eINSTANCE.createXSDSimpleTypeDefinition();
      xsdSimpleTypeDefinition.setName(id);
      XSDSimpleTypeDefinition baseType = xsdSchema.resolveSimpleTypeDefinition(XMLResource.XML_SCHEMA_URI, "string"); //$NON-NLS-1$
      xsdSimpleTypeDefinition.setBaseTypeDefinition(baseType);
      return xsdSimpleTypeDefinition;
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "structuredData.create")
   public void createStructuredData(ModelType model, JsonObject request)
   {
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);

      String dataFullID = null;
      if (hasNotJsonNull(request, ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY))
      {
         dataFullID = extractString(request,
               ModelerConstants.STRUCTURED_DATA_TYPE_FULL_ID_PROPERTY);
      }
      else
      {
         dataFullID = model.getId() + ":";
      }
      DataType data = null;
      TypeDeclarationType typeDeclaration = getModelBuilderFacade().findTypeDeclaration(dataFullID);
      // For Java bound ENUM's create primitive else structured Data
      if (getModelBuilderFacade().isEnumerationJavaBound(typeDeclaration))
      {
         data = getModelBuilderFacade().createPrimitiveData(model, null, name, ModelerConstants.ENUM_PRIMITIVE_DATA_TYPE);
         getModelBuilderFacade().updateTypeForPrimitive(data, dataFullID);
      }
      else
      {
         data = getModelBuilderFacade().createStructuredData(model, null, name,
               dataFullID);   
      }
      
      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(data);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "documentData.create")
   public void createDocumentData(ModelType model, JsonObject request)
   {
      String name = extractString(request, ModelerConstants.NAME_PROPERTY);

      DataType data = getModelBuilderFacade().createDocumentData(model, null, name, null);

      // Map newly created data element to a UUID
      EObjectUUIDMapper mapper = modelService().uuidMapper();
      mapper.map(data);
   }

   /**
    * @param model
    * @param request
    */
   @OnCommand(commandId = "data.delete")
   public void deletetData(ModelType model, JsonObject request)
   {
      String id = extractString(request, ModelerConstants.ID_PROPERTY);
      DataType data = getModelBuilderFacade().findData(model, id);

      synchronized (model)
      {
         deleteDataDymbolsForData(model, data.getId());
         model.getData().remove(data);
      }
   }

   private ModelService modelService()
   {
      return springContext.getBean(ModelService.class);
   }

   /**
    * @param model
    * @param dataId
    */
   private void deleteDataDymbolsForData(ModelType model, String dataId)
   {
      for (ProcessDefinitionType pdt : model.getProcessDefinition())
      {
         for (DiagramType diagram : pdt.getDiagram())
         {
            for (PoolSymbol poolSymbol : diagram.getPoolSymbols())
            {
               for (LaneSymbol childLaneSymbol : poolSymbol.getChildLanes())
               {
                  Iterator<DataSymbolType> iter = childLaneSymbol.getDataSymbol().iterator();
                  while (iter.hasNext())
                  {
                     DataSymbolType dataSymbol = iter.next();
                     if (dataId.equals(dataSymbol.getData().getId()))
                     {
                        ModelElementEditingUtils.deleteDataMappingConnection(dataSymbol.getDataMappings());
                        iter.remove();
                     }
                  }
               }
            }
         }
      }
   }

   private ModelBuilderFacade getModelBuilderFacade()
   {
      return CommandHandlerUtils.getModelBuilderFacade(springContext);
   }
}