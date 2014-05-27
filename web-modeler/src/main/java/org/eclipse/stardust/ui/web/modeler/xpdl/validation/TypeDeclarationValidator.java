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
package org.eclipse.stardust.ui.web.modeler.xpdl.validation;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.stardust.engine.core.struct.StructuredDataConstants;
import org.eclipse.stardust.model.xpdl.carnot.ModelType;
import org.eclipse.stardust.model.xpdl.carnot.util.ModelUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackage;
import org.eclipse.stardust.model.xpdl.xpdl2.ExternalPackages;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationsType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.modeling.validation.IModelValidator;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.stardust.modeling.validation.ValidationException;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDImport;
import org.eclipse.xsd.XSDSchema;
import org.eclipse.xsd.XSDTypeDefinition;

public class TypeDeclarationValidator implements IModelValidator
{
   private static final Issue[] ISSUE_ARRAY = new Issue[0];

   // validate references
   public Issue[] validate(ModelType model) throws ValidationException
   {
      List<Issue> result = CollectionUtils.newList();
      TypeDeclarationsType declarations = model.getTypeDeclarations();
      List<TypeDeclarationType> allDeclarations = declarations.getTypeDeclaration();

      for (Iterator<TypeDeclarationType> t = allDeclarations.iterator(); t.hasNext();)
      {
         TypeDeclarationType declaration = t.next();
         validateElements(model, result, declaration);
         validateNestedReferences(model, result, declaration);
         validateParentReferences(model, result, declaration);
      }

      return result.toArray(ISSUE_ARRAY);
   }

   private void validateElements(ModelType model, List<Issue> result,
         TypeDeclarationType declaration)
   {
      result.addAll(ElementValidator.validateElements(declaration));
   }

   private void validateNestedReferences(ModelType model, List<Issue> result,
         TypeDeclarationType declaration)
   {
      XSDComplexTypeDefinition typeDefinition = TypeDeclarationUtils.getComplexType(declaration);
      for (Iterator<EObject> i = typeDefinition.getSchema().eAllContents(); i.hasNext();)
      {
         EObject o = i.next();
         if (o instanceof XSDElementDeclaration)
         {
            XSDElementDeclaration elementDeclaration = (XSDElementDeclaration) o;
            String targetNamespace = elementDeclaration.getType().getTargetNamespace();
            XSDImport xsdImport = getImportByNamespace(declaration.getSchema(),
                  targetNamespace);
            if (xsdImport != null)
            {
               String location = ((XSDImport) xsdImport).getSchemaLocation();
               if (location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
               {
                  String typeId = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
                  QName qname = QName.valueOf(typeId);
                  model = getRefModel(model, qname);
                  TypeDeclarationsType referedDeclarations = model.getTypeDeclarations();
                  if (referedDeclarations.getTypeDeclaration(qname.getLocalPart()) == null)
                  {
                     result.add(Issue.error(declaration, MessageFormat.format(
                           "TypeDeclaration ''{0}'': referenced type ''{1}'' not found.", //$NON-NLS-1$
                           declaration.getId(), typeId)));
                  }
               }
            }

         }

      }
   }

   private void validateParentReferences(ModelType model, List<Issue> result,
         TypeDeclarationType declaration)
   {
      XSDComplexTypeDefinition typeDefinition = TypeDeclarationUtils.getComplexType(declaration);
      XSDTypeDefinition baseType = typeDefinition.getBaseType();
      if (baseType != null)
      {
         String baseTypeNameSpace = baseType.getTargetNamespace();
         if (baseTypeNameSpace != null)
         {
            XSDImport baseTypeImport = this.getImportByNamespace(declaration.getSchema(),
                  baseTypeNameSpace);
            if (baseTypeImport != null)
            {
               String location = ((XSDImport) baseTypeImport).getSchemaLocation();
               if (location.startsWith(StructuredDataConstants.URN_INTERNAL_PREFIX))
               {
                  String typeId = location.substring(StructuredDataConstants.URN_INTERNAL_PREFIX.length());
                  QName qname = QName.valueOf(typeId);
                  model = getRefModel(model, qname);
                  TypeDeclarationsType referedDeclarations = model.getTypeDeclarations();
                  if (referedDeclarations.getTypeDeclaration(qname.getLocalPart()) == null)
                  {
                     result.add(Issue.error(
                           declaration,
                           MessageFormat.format(
                                 "TypeDeclaration ''{0}'': referenced parent type ''{1}'' not found.", //$NON-NLS-1$
                                 declaration.getId(), typeId)));
                  }
               }

            }

         }
      }
   }

   private ModelType getRefModel(ModelType model, QName qname)
   {
      if (XMLConstants.NULL_NS_URI != qname.getNamespaceURI())
      {
         ExternalPackages packs = model.getExternalPackages();
         if (packs != null)
         {
            ExternalPackage refPack = packs.getExternalPackage(qname.getNamespaceURI());
            if (refPack != null)
            {
               ModelType refModel = ModelUtils.getExternalModel(refPack);
               if (refModel != null)
               {
                  model = refModel;
               }
            }
         }
      }
      return model;
   }

   private XSDImport getImportByNamespace(XSDSchema schema, String nameSpace)
   {
      List<XSDImport> xsdImports = TypeDeclarationUtils.getImports(schema);
      if (xsdImports != null)
      {
         for (Iterator<XSDImport> i = xsdImports.iterator(); i.hasNext();)
         {
            XSDImport xsdImport = i.next();
            String importNameSpace = xsdImport.getNamespace();
            if (nameSpace.equals(importNameSpace))
            {
               return xsdImport;
            }
         }
      }
      return null;
   }

}