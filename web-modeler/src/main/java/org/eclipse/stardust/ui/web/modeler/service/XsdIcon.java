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

public enum XsdIcon
{
   Schema("XSDSchema.gif"), //$NON-NLS-1$
   ElementUse("XSDElementUse.gif"), //$NON-NLS-1$
   ElementDeclaration("XSDElementDeclaration.gif"), //$NON-NLS-1$
   ComplexTypeDefinition("XSDComplexTypeDefinition.gif"), //$NON-NLS-1$
   SimpleTypeDefinition("XSDSimpleTypeDefinition.gif"), //$NON-NLS-1$
   ModelGroupAll("XSDModelGroupAll.gif"), //$NON-NLS-1$
   ModelGroupChoice("XSDModelGroupChoice.gif"), //$NON-NLS-1$
   ModelGroupSequence("XSDModelGroupSequence.gif"), //$NON-NLS-1$
   ModelGroupUnresolved("XSDModelGroupUnresolved.gif"), //$NON-NLS-1$
   EnumerationFacet("XSDEnumerationFacet.gif"), //$NON-NLS-1$
   PatternFacet("XSDPatternFacet.gif"), //$NON-NLS-1$
   AttributeDeclaration("XSDAttributeDeclaration.gif"), //$NON-NLS-1$
   WildcardAttribute("XSDWildcardAttribute.gif"), //$NON-NLS-1$
   WildcardElement("XSDWildcardElement.gif"); //$NON-NLS-1$

   private static final String PREFIX = "{org.eclipse.xsd.edit}icons/full/obj16/"; //$NON-NLS-1$

   private String simpleName;
   private String qualifiedName;

   private XsdIcon(String simpleName)
   {
      this.simpleName = simpleName;
      this.qualifiedName = PREFIX + simpleName;
   }

   public String getSimpleName()
   {
      return simpleName;
   }

   public String getQualifiedName()
   {
      return qualifiedName;
   }
}