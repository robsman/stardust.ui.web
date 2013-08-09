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
package org.eclipse.stardust.ui.web.validation;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.stardust.common.StringUtils;
import org.eclipse.stardust.model.xpdl.xpdl2.TypeDeclarationType;
import org.eclipse.stardust.model.xpdl.xpdl2.util.TypeDeclarationUtils;
import org.eclipse.stardust.modeling.validation.Issue;
import org.eclipse.xsd.XSDAttributeDeclaration;
import org.eclipse.xsd.XSDAttributeGroupContent;
import org.eclipse.xsd.XSDAttributeUse;
import org.eclipse.xsd.XSDComplexTypeContent;
import org.eclipse.xsd.XSDComplexTypeDefinition;
import org.eclipse.xsd.XSDElementDeclaration;
import org.eclipse.xsd.XSDModelGroup;
import org.eclipse.xsd.XSDModelGroupDefinition;
import org.eclipse.xsd.XSDParticle;
import org.eclipse.xsd.XSDParticleContent;
import org.eclipse.xsd.XSDTerm;
import org.eclipse.xsd.XSDTypeDefinition;

public class ElementValidator
{
   private static List<Issue> messages;

   public static List<Issue> validateElements(TypeDeclarationType declaration)
   {
      messages = new ArrayList<Issue>();
      XSDComplexTypeDefinition complexType = TypeDeclarationUtils.getComplexType(declaration);
      if(complexType != null)
      {
         visit(complexType, declaration);
      }
      return messages;
   }

   public static void visit(XSDComplexTypeDefinition complexType, TypeDeclarationType declaration)
   {
	  List<String> attributeNames = new ArrayList<String>();
      for (XSDAttributeGroupContent attribute : complexType.getAttributeContents())
      {
         if (attribute instanceof XSDAttributeUse)
         {
            XSDAttributeDeclaration attr = ((XSDAttributeUse) attribute).getContent();
            String name = attr.getName();
            if (name != null)
            {
                if (attributeNames.contains(name))
                {
                   messages.add(Issue.error(
                         declaration,
                         MessageFormat.format(
                               "Duplicate identifier: ''{0}''", //$NON-NLS-1$
                               name)));
                }
                attributeNames.add(name);
            }
         }
      }

      List<String> names = new ArrayList<String>();
      XSDComplexTypeContent content = complexType.getContent();
      if (content instanceof XSDParticle)
      {
         visit((XSDParticle) content, declaration);
         XSDParticleContent particleContent = ((XSDParticle) content).getContent();
         if (particleContent instanceof XSDModelGroup)
         {
             for (XSDParticle xsdParticle : ((XSDModelGroup) particleContent).getContents())
             {
                XSDParticleContent xsdParticleContent = xsdParticle.getContent();
                if (xsdParticleContent instanceof XSDElementDeclaration)
                {
                	String name = ((XSDElementDeclaration) xsdParticleContent).getName();
                    if (name != null)
                    {
                    	if (names.contains(name))
                    	{
                        messages.add(Issue.error(
                              declaration,
                              MessageFormat.format(
                                    "Duplicate identifier: ''{0}''", //$NON-NLS-1$
                                    name)));
                    	}
                    	names.add(name);
                    }
                }
             }
         }
      }
   }

   public static void visit(XSDParticle particle, TypeDeclarationType declaration)
   {
      XSDParticleContent particleContent = particle.getContent();
      if (particleContent instanceof XSDModelGroupDefinition)
      {
      }
      else if(particleContent instanceof XSDTerm)
      {
         visit((XSDTerm) particleContent, declaration);
      }
   }

   public static void visit(XSDTerm term, TypeDeclarationType declaration)
   {
      if (term instanceof XSDElementDeclaration)
      {
         visit((XSDElementDeclaration) term, declaration);
      }
      else if (term instanceof XSDModelGroup)
      {
         visit((XSDModelGroup) term, declaration);
      }
   }

   public static void visit(XSDModelGroup group, TypeDeclarationType declaration)
   {
      List<String> names = new ArrayList<String>();
      for (XSDParticle xsdParticle : ((XSDModelGroup) group).getContents())
      {
         XSDParticleContent particleContent = xsdParticle.getContent();

         // check for element declarations
         if (particleContent instanceof XSDElementDeclaration)
         {
            String name = ((XSDElementDeclaration) particleContent).getName();
            if (name != null)
            {
                if (names.contains(name))
                {
                   messages.add(Issue.error(
                         declaration,
                         MessageFormat.format(
                               "Duplicate identifier: ''{0}''", //$NON-NLS-1$
                               name)));
                }
                names.add(name);
            }
         }
         visit((XSDParticle) xsdParticle, declaration);
      }
   }

   public static void visit(XSDElementDeclaration element, TypeDeclarationType declaration)
   {
      XSDTypeDefinition type = ((XSDElementDeclaration) element).getAnonymousTypeDefinition();
      if (type instanceof XSDComplexTypeDefinition)
      {
         visit((XSDComplexTypeDefinition) type, declaration);
      }
      else if (type == null)
      {
         String name = element.getName();
         if (!isValidElementName(name))
         {
            messages.add(Issue.error(
                  declaration,
                  MessageFormat.format(
                        "''{0}'' is not a valid identifier.", //$NON-NLS-1$
                        name)));
         }
      }
   }

   // validate name
   public static boolean isValidElementName(String name)
   {
      if(StringUtils.isEmpty(name))
      {
         return false;
      }
      char ch = name.charAt(0);
      if (!Character.isLetter(ch)
            && ch != ':' && ch != '_')
      {
         return false;
      }
      for (int i = 1; i < name.length(); i++)
      {
         ch = name.charAt(i);
         if (!Character.isLetter(ch)
               && !Character.isDigit(ch)
               && ch != '.' && ch != '-' && ch != '_')
         {
            return false;
         }
      }
      return true;
   }
}