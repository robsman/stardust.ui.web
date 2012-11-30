/*******************************************************************************
 * Copyright (c) 2012 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.modeler.service;

import static org.eclipse.stardust.common.CollectionUtils.newArrayList;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.stardust.common.CollectionUtils;
import org.eclipse.xsd.*;
import org.eclipse.xsd.util.XSDSwitch;

public class XsdContentProvider extends XSDSwitch<EObject[]>
{
   protected boolean showDirectContentOnly;

   public static EObject[] EMPTY_ARRAY = new EObject[0];

   public XsdContentProvider()
   {
      this(false);
   }

   public XsdContentProvider(boolean showDirectContentOnly)
   {
      this.showDirectContentOnly = showDirectContentOnly;
   }

   public EObject[] caseXSDSchema(XSDSchema schema)
   {
      String targetNamespace = schema.getTargetNamespace();
      if (targetNamespace == null)
      {
         targetNamespace = ""; //$NON-NLS-1$
 	  }
      List<XSDNamedComponent> all = null;
      /*if (showDirectContentOnly)
      {
         List<XSDSchemaContent> contents = schema.getContents();
         all = CollectionUtils.newList(contents.size());
         for (XSDSchemaContent item : contents)
         {
            if (item instanceof XSDElementDeclaration)
            {
               all.add((XSDElementDeclaration) item);
            }
         }
         for (XSDSchemaContent item : contents)
         {
            if (item instanceof XSDTypeDefinition)
            {
               all.add((XSDTypeDefinition) item);
            }
         }
      }
      else
      {*/
         List<XSDElementDeclaration> elements = schema.getElementDeclarations();
         List<XSDTypeDefinition> types = schema.getTypeDefinitions();
         all = CollectionUtils.newList(elements.size() + types.size());
         addElements(all, targetNamespace, elements);
         addElements(all, targetNamespace, types);
      //}
      return (EObject[]) all.toArray(new EObject[all.size()]);
   }

   private void addElements(List<XSDNamedComponent> targetList, String targetNamespace, List<? extends XSDNamedComponent> sourceList)
   {
      for (XSDNamedComponent element : sourceList)
      {
    	 String namespace = element.getTargetNamespace();
         if (targetNamespace.equals(namespace == null ? "" : namespace)) //$NON-NLS-1$
         {
            targetList.add(element);
         }
      }
   }

   public EObject[] caseXSDElementDeclaration(XSDElementDeclaration element)
   {
      if (!showDirectContentOnly && element.isElementDeclarationReference())
      {
         element = element.getResolvedElementDeclaration();
      }
      XSDTypeDefinition type = element.getAnonymousTypeDefinition();
      if (type == null && !showDirectContentOnly)
      {
         type = element.getTypeDefinition();
      }
      return type == null ? new EObject[0] : doSwitch(type);
   }

   public EObject[] caseXSDComplexTypeDefinition(XSDComplexTypeDefinition type)
   {
      EObject[] content = XsdContentProvider.EMPTY_ARRAY;
      if (type.getContent() != null)
      {
         content = doSwitch(type.getContent());
      }
      List<XSDComponent> result = CollectionUtils.newList();
      XSDWildcard wildcard = type.getAttributeWildcardContent();
      List<XSDAttributeGroupContent> attributes = type.getAttributeContents();
      addAttributes(result, attributes);
      if (wildcard != null)
      {
         result.add(wildcard);
      }
      EObject[] resultArray = new EObject[result.size() + content.length];
      result.toArray(resultArray);
      System.arraycopy(content, 0, resultArray, result.size(), content.length);
      return resultArray;
   }

   private void addAttributes(List<XSDComponent> result, List<XSDAttributeGroupContent> attributes)
   {
      // TODO: (fh) passing resolved declaration blocks type modification for unresolved elements, because eContainer is null.
      for (XSDAttributeGroupContent attribute : attributes)
      {
         if (attribute instanceof XSDAttributeUse)
         {
            XSDAttributeDeclaration attr = ((XSDAttributeUse) attribute).getContent(); //getAttributeDeclaration());
            if (attr.isAttributeDeclarationReference())
            {
               attr = attr.getResolvedAttributeDeclaration();
            }
            result.add(attr);
         }
         else if (attribute instanceof XSDAttributeGroupDefinition)
         {
            XSDAttributeGroupDefinition group = (XSDAttributeGroupDefinition) attribute;
            if (group.isAttributeGroupDefinitionReference())
            {
               group = group.getResolvedAttributeGroupDefinition();
            }
            addAttributes(result, group.getContents());
         }
      }
   }

   public EObject[] caseXSDSimpleTypeDefinition(XSDSimpleTypeDefinition type)
   {
      // TODO:
      List<EObject> result = newArrayList();

      result.addAll(type.getEnumerationFacets());
      result.addAll(type.getPatternFacets());

      XSDConstrainingFacet newElement = getNewItem(type);
      if (null != newElement)
      {
         result.add(newElement);
      }

      return result.toArray(new EObject[result.size()]);
   }

   protected <T> T getNewItem(XSDComponent parent)
   {
      return null;
   }

   public EObject[] caseXSDModelGroup(XSDModelGroup model)
   {
      List<Object> result = CollectionUtils.newList();
      List<XSDParticle> particles = model.getContents();
      addModelGroupContent(result, particles);
      XSDTerm newElement = getNewItem(model);
      if (newElement != null)
      {
         result.add(newElement);
      }
      return result.toArray(new EObject[result.size()]);
   }

   private void addModelGroupContent(List<Object> result, List<XSDParticle> particles)
   {
      for (XSDParticle particle : particles)
      {
         XSDParticleContent content = particle.getContent();
         if (content instanceof XSDModelGroupDefinition)
         {
            XSDModelGroupDefinition group = (XSDModelGroupDefinition) content;
            if (group.isModelGroupDefinitionReference())
            {
               group = group.getResolvedModelGroupDefinition();
            }
            EObject[] children = caseXSDModelGroup(group.getModelGroup());
            for (int j = 0; j < children.length; j++)
            {
               result.add(children[j]);
            }
         }
         else
         {
            result.add(content);
         }
      }
   }

   public EObject[] caseXSDParticle(XSDParticle particle)
   {
      XSDParticleContent content = particle.getContent();
      if (content instanceof XSDModelGroupDefinition)
      {
         XSDModelGroupDefinition group = (XSDModelGroupDefinition) content;
         if (group.isModelGroupDefinitionReference())
         {
            group = group.getResolvedModelGroupDefinition();
         }
         //return caseXSDModelGroup(group.getModelGroup());
         return new EObject[] {group.getModelGroup()};
      }
      return new EObject[] {particle.getContent()};
   }

   public EObject[] defaultCase(EObject object)
   {
      return XsdContentProvider.EMPTY_ARRAY;
   }
}