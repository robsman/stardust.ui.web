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

import org.eclipse.xsd.*;
import org.eclipse.xsd.util.XSDSwitch;

public class XsdIconProvider extends XSDSwitch<XsdIconProvider.XsdIcon>
{
   private static final String PREFIX = "{org.eclipse.xsd.edit}icons/full/obj16/"; //$NON-NLS-1$

   public static enum XsdIcon
   {
      Schema(PREFIX + "XSDSchema.gif"), //$NON-NLS-1$
      ElementUse(PREFIX + "XSDElementUse.gif"), //$NON-NLS-1$
      ElementDeclaration(PREFIX + "XSDElementDeclaration.gif"), //$NON-NLS-1$
      ComplexTypeDefinition(PREFIX + "XSDComplexTypeDefinition.gif"), //$NON-NLS-1$
      SimpleTypeDefinition(PREFIX + "XSDSimpleTypeDefinition.gif"), //$NON-NLS-1$
      ModelGroupAll(PREFIX + "XSDModelGroupAll.gif"), //$NON-NLS-1$
      ModelGroupChoice(PREFIX + "XSDModelGroupChoice.gif"), //$NON-NLS-1$
      ModelGroupSequence(PREFIX + "XSDModelGroupSequence.gif"), //$NON-NLS-1$
      ModelGroupUnresolved(PREFIX + "XSDModelGroupUnresolved.gif"), //$NON-NLS-1$
      EnumerationFacet(PREFIX + "XSDEnumerationFacet.gif"), //$NON-NLS-1$
      PatternFacet(PREFIX + "XSDPatternFacet.gif"), //$NON-NLS-1$
      AttributeDeclaration(PREFIX + "XSDAttributeDeclaration.gif"), //$NON-NLS-1$
      WildcardAttribute(PREFIX + "XSDWildcardAttribute.gif"), //$NON-NLS-1$
      WildcardElement(PREFIX + "XSDWildcardElement.gif"); //$NON-NLS-1$
      
      private String qualifiedName;
      private String simpleName;
      
      private XsdIcon(String qualifiedName)
      {
         this.qualifiedName = qualifiedName;
         simpleName = qualifiedName.substring(PREFIX.length());
      }

      public String getQualifiedName()
      {
         return qualifiedName;
      }

      public String getSimpleName()
      {
         return simpleName;
      }
   }
   
   public XsdIcon caseXSDSchema(XSDSchema schema)
   {
      return XsdIcon.Schema;
   }

   public XsdIcon caseXSDElementDeclaration(XSDElementDeclaration element)
   {
      return element.isElementDeclarationReference()
            ? XsdIcon.ElementUse
            : XsdIcon.ElementDeclaration;
   }

   public XsdIcon caseXSDComplexTypeDefinition(XSDComplexTypeDefinition complexType)
   {
      return XsdIcon.ComplexTypeDefinition;
   }

   public XsdIcon caseXSDSimpleTypeDefinition(XSDSimpleTypeDefinition simpleType)
   {
      return XsdIcon.SimpleTypeDefinition;
   }

   public XsdIcon caseXSDModelGroup(XSDModelGroup modelGroup)
   {
      switch (modelGroup.getCompositor().getValue())
      {
      case XSDCompositor.ALL: return XsdIcon.ModelGroupAll;
      case XSDCompositor.CHOICE: return XsdIcon.ModelGroupChoice;
      case XSDCompositor.SEQUENCE: return XsdIcon.ModelGroupSequence;
      }
      return XsdIcon.ModelGroupUnresolved;
   }

   public XsdIcon caseXSDEnumerationFacet(XSDEnumerationFacet enumeration)
   {
      return XsdIcon.EnumerationFacet;
   }

   public XsdIcon caseXSDPatternFacet(XSDPatternFacet pattern)
   {
      return XsdIcon.PatternFacet;
   }

   public XsdIcon caseXSDAttributeDeclaration(XSDAttributeDeclaration attribute)
   {
      return XsdIcon.AttributeDeclaration;
   }

   public XsdIcon caseXSDWildcard(XSDWildcard wildcard)
   {
      if (wildcard.eContainer() instanceof XSDComplexTypeDefinition)
      {
         return XsdIcon.WildcardAttribute;
      }
      return XsdIcon.WildcardElement;
   }
}