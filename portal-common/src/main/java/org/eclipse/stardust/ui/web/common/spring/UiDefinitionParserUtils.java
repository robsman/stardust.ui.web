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
package org.eclipse.stardust.ui.web.common.spring;

import static org.eclipse.stardust.ui.web.common.spring.HierarchyFactoryBean.PRP_ELEMENTS;
import static org.eclipse.stardust.ui.web.common.spring.HierarchyFactoryBean.PRP_PARENT;
import static org.eclipse.stardust.ui.web.common.spring.UiDefinitionTokens.*;
import static org.eclipse.stardust.ui.web.common.util.CollectionUtils.newHashSet;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.util.xml.DomUtils.getChildElementsByTagName;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.stardust.ui.web.common.*;
import org.eclipse.stardust.ui.web.common.log.LogManager;
import org.eclipse.stardust.ui.web.common.log.Logger;
import org.eclipse.stardust.ui.web.common.util.StringUtils;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class UiDefinitionParserUtils
{
   private static final Logger trace = LogManager.getLogger(UiDefinitionParserUtils.class);

   @SuppressWarnings("unchecked")
   public static BeanDefinitionBuilder parsePerspective(Element element)
   {
      BeanDefinitionBuilder pd = rootBeanDefinition(PerspectiveDefinition.class);

      pd.addPropertyValue(A_NAME, element.hasAttribute(A_NAME)
            ? element.getAttribute(A_NAME)
            : element.getAttribute(A_ID));

      List<Element> preferences = getChildElementsByTagName(element, E_PREFERENCES);
      if(preferences != null && preferences.size() > 0)
      {
         BeanDefinitionBuilder child = parseUiPreferences(preferences.get(0));
         pd.addPropertyValue(A_PREFERENCES, child.getBeanDefinition());
      }

      if (element.hasAttribute(A_MESSAGE_BUNDLES))
      {
         pd.addPropertyValue(A_MESSAGES, parseMessageSource(element));
      }
      
      if (element.hasAttribute(A_REQUIRED_ROLES))
      {
         pd.addPropertyValue(A_REQUIRED_ROLES, element.getAttribute(A_REQUIRED_ROLES));
      }
      
      if (element.hasAttribute(A_EXCLUDE_ROLES))
      {
         pd.addPropertyValue(A_EXCLUDE_ROLES, element.getAttribute(A_EXCLUDE_ROLES));
      }

      if (element.hasAttribute(A_CONTROLLER))
      {
         pd.addPropertyValue(A_CONTROLLER, element.getAttribute(A_CONTROLLER));
      }

      if (element.hasAttribute(A_DEFAULT))
      {
         pd.addPropertyValue(A_DEFAULT_PERSPECTIVE, element.getAttribute(A_DEFAULT));
      }
      
      return pd;
   }

   public static BeanDefinitionBuilder parsePerspectiveExtension(Element element)
   {
      BeanDefinitionBuilder pd = rootBeanDefinition(PerspectiveExtension.class);

      pd.addPropertyValue(A_NAME, element.hasAttribute(A_NAME)
            ? element.getAttribute(A_NAME)
            : element.getAttribute(A_ID));

      String targetPerspective = element.getAttribute(A_TARGET_PERSPECTIVE);
      pd.addPropertyValue(A_TARGET_PERSPECTIVE, targetPerspective);
      if (StringUtils.isNotEmpty(targetPerspective) && targetPerspective.contains(","))
      {
         trace.warn("Multiple values for attribute '" + A_TARGET_PERSPECTIVE + "' is now deprecated. But it's used in '"
               + element.getAttribute(A_ID) + "'.");
      }
      
      if (element.hasAttribute(A_MESSAGE_BUNDLES))
      {
         pd.addPropertyValue(A_MESSAGES, parseMessageSource(element));
      }
      
      if (element.hasAttribute(A_REQUIRED_ROLES))
      {
         pd.addPropertyValue(A_REQUIRED_ROLES, element.getAttribute(A_REQUIRED_ROLES));
      }
      
      if (element.hasAttribute(A_EXCLUDE_ROLES))
      {
         pd.addPropertyValue(A_EXCLUDE_ROLES, element.getAttribute(A_EXCLUDE_ROLES));
      }

      return pd;
   }

   /**
    * @param element
    * @return
    */
   private static Node getMainNode(Element element)
   {
      if (null != element.getParentNode())
      {
         Node node = element.getParentNode();
         while(null != node)
         {
            if (E_PERSPECTIVE.equals(node.getLocalName()) || E_PERSPECTIVE_EXT.equals(node.getLocalName()))
            {
               return node;
            }
            
            node = node.getParentNode();
         }
      }
      return null;
   }

   /**
    * @param element
    * @return
    */
   private static String getDefinedIn(Element element)
   {
      Node node = getMainNode(element);
      String definedIn = (null != node) ? definedIn = node.getAttributes().getNamedItem(A_ID).getNodeValue() : "";
      return definedIn;
   }

   /**
    * @param element
    * @return
    */
   private static boolean isElementGlobal(Element element)
   {
      boolean global = false;
      Node node = getMainNode(element);
      if (null != node && E_PERSPECTIVE_EXT.equals(node.getLocalName()))
      {
         String targetPerspective = node.getAttributes().getNamedItem(A_TARGET_PERSPECTIVE).getNodeValue();
         if (StringUtils.isNotEmpty(targetPerspective))
         {
            targetPerspective = targetPerspective.trim();
            if ("*".equals(targetPerspective))
            {
               global = true;
            }
         }
      }

      return global;
   }

   @SuppressWarnings("unchecked")
   public static BeanDefinitionBuilder parseUiSection(Element element)
   {
      Class<? extends UiElement> beanClazz;
      if (E_MENU_SECTION.equals(element.getLocalName()))
      {
         beanClazz = MenuSection.class;
      }
      else if (E_LAUNCH_PANEL.equals(element.getLocalName()))
      {
         beanClazz = LaunchPanel.class;
      }
      else if (E_TOOLBAR_SECTION.equals(element.getLocalName()))
      {
         beanClazz = ToolbarSection.class;
      }
      else if (E_VIEW.equals(element.getLocalName()))
      {
         beanClazz = ViewDefinition.class;
      }
      else
      {
         throw new BeanCreationException("Unsupported UI section element: "
               + element.getNodeName());
      }

      BeanDefinitionBuilder uis = rootBeanDefinition(beanClazz);

      uis.addConstructorArgValue(element.getAttribute(A_NAME));
      uis.addConstructorArgValue(element.getAttribute(A_INCLUDE));
      uis.addConstructorArgValue(getDefinedIn(element));
      uis.addConstructorArgValue(isElementGlobal(element));
      
      if (element.hasAttribute(A_REQUIRED_ROLES))
      {
         uis.addPropertyValue(A_REQUIRED_ROLES, element.getAttribute(A_REQUIRED_ROLES));
      }
      
      if (element.hasAttribute(A_EXCLUDE_ROLES))
      {
         uis.addPropertyValue(A_EXCLUDE_ROLES, element.getAttribute(A_EXCLUDE_ROLES));
      }

      if (LaunchPanel.class == beanClazz)
      {
      }
      else if (ToolbarSection.class == beanClazz)
      {
         if (element.hasAttribute(A_REQUIRED_VIEW))
         {
            Set<String> requiredViews = newHashSet();
            Iterator<String> viewIds = StringUtils.split(element.getAttribute(A_REQUIRED_VIEW), ",");
            while (viewIds.hasNext())
            {
               requiredViews.add(viewIds.next().trim());
            }
            
            uis.addPropertyValue(A_REQUIRED_VIEW, requiredViews);
         }

         ManagedList childToolbarButtons = parseChildToolbarButtons(element);
         if (null != childToolbarButtons)
         {
            BeanDefinitionBuilder tsf = rootBeanDefinition(ToolbarSectionFactoryBean.class);
            tsf.addPropertyValue(PRP_PARENT, uis.getBeanDefinition());
            tsf.addPropertyValue(PRP_ELEMENTS, childToolbarButtons);

            uis = tsf;
         }
      }
      else if (ViewDefinition.class == beanClazz)
      {
         if (element.hasAttribute(A_REQUIRED_ROLES))
         {
            uis.addPropertyValue(A_REQUIRED_ROLES, element.getAttribute(A_REQUIRED_ROLES));
         }

         List<Element> preferences = getChildElementsByTagName(element, E_PREFERENCES);
         if(preferences != null && preferences.size() > 0)
         {
            BeanDefinitionBuilder child = parseUiPreferences(preferences.get(0));
            uis.addPropertyValue(A_PREFERENCES, child.getBeanDefinition());
         }

         if (element.hasAttribute(A_CONTROLLER))
         {
            uis.addPropertyValue(A_CONTROLLER, element.getAttribute(A_CONTROLLER));
         }
         
         if (element.hasAttribute(A_CLOSING_POLICY))
         {
            uis.addPropertyValue(A_CLOSING_POLICY, element.getAttribute(A_CLOSING_POLICY));
         }
         
         if (element.hasAttribute(A_IDENTITY_PARAMS))
         {
            uis.addPropertyValue(A_IDENTITY_PARAMS, element.getAttribute(A_IDENTITY_PARAMS));
         }
      }

      return uis;
   }

   @SuppressWarnings("unchecked")
   public static BeanDefinitionBuilder parseUiPreferences(Element element)
   {
      BeanDefinitionBuilder uis = rootBeanDefinition(PreferencesDefinition.class);

      List<Element> prefPages = getChildElementsByTagName(element,
            new String[] {E_PREFERENCE_PAGE});

      if ((null != prefPages) && (0 < prefPages.size()))
      {
         ManagedList prefPageList = new ManagedList(prefPages.size());
         for (int i = 0; i < prefPages.size(); ++i)
         {
            BeanDefinitionBuilder child = parseUiPreferencePage(prefPages.get(i));
            prefPageList.add(child.getBeanDefinition());
         }
         
         BeanDefinitionBuilder psf = rootBeanDefinition(PreferencesFactoryBean.class);
         psf.addPropertyValue(PRP_PARENT, uis.getBeanDefinition());
         psf.addPropertyValue(PRP_ELEMENTS, prefPageList);
         
         uis = psf;
      }
      
      return uis;
   }
   
   public static BeanDefinitionBuilder parseUiPreferencePage(Element element)
   {
      BeanDefinitionBuilder uis = rootBeanDefinition(PreferencePage.class);

      uis.addConstructorArgValue(element.getAttribute(A_NAME));
      uis.addConstructorArgValue(element.getAttribute(A_INCLUDE));
      uis.addConstructorArgValue(getDefinedIn(element));
      uis.addConstructorArgValue(isElementGlobal(element));

      return uis;
   }
   
   public static BeanDefinitionBuilder parseToolbarButton(Element element)
   {
      BeanDefinitionBuilder tb = rootBeanDefinition(ToolbarButton.class);

      tb.addConstructorArgValue(element.getAttribute(A_HANDLER));
      tb.addConstructorArgValue(element.getAttribute(A_ICON));
      tb.addConstructorArgValue(element.getAttribute(A_DISABLED_ICON));

      return tb;
   }

   private static BeanDefinitionBuilder parseUiExtension(Element element)
   {
      Class<? extends UiExtension<? >> uiExtensionClazz;
      String uiElementTag;

      if (E_MENU_EXTENSION.equals(element.getLocalName()))
      {
         uiExtensionClazz = MenuExtension.class;
         uiElementTag = E_MENU_SECTION;
      }
      else if (E_LAUNCHPAD_EXTENSION.equals(element.getLocalName()))
      {
         uiExtensionClazz = LaunchpadExtension.class;
         uiElementTag = E_LAUNCH_PANEL;
      }
      else if (E_TOOLBAR_EXTENSION.equals(element.getLocalName()))
      {
         uiExtensionClazz = ToolbarExtension.class;
         uiElementTag = E_TOOLBAR_SECTION;
      }
      else if (E_VIEWS_EXTENSION.equals(element.getLocalName()))
      {
         uiExtensionClazz = ViewsExtension.class;
         uiElementTag = E_VIEW;
      }
      else
      {
         throw new BeanCreationException("Unsupported UI extension element: "
               + element.getNodeName());
      }

      BeanDefinitionBuilder uixf = rootBeanDefinition(UiExtensionFactoryBean.class);
      uixf.addConstructorArgValue(uiExtensionClazz);

      BeanDefinitionBuilder uix = rootBeanDefinition(uiExtensionClazz);
      uixf.addPropertyValue(PRP_PARENT, uix.getBeanDefinition());

      if (element.hasAttribute(A_BEFORE))
      {
         uix.addPropertyValue(A_BEFORE, element.getAttribute(A_BEFORE));
      }
      if (element.hasAttribute(A_AFTER))
      {
         uix.addPropertyValue(A_AFTER, element.getAttribute(A_AFTER));
      }

      ManagedList childUiElements = parseChildUiSections(element, uiElementTag);
      if (null != childUiElements)
      {
         uixf.addPropertyValue(PRP_ELEMENTS, childUiElements);
      }

      return uixf;
   }

   @SuppressWarnings("unchecked")
   public static ManagedList parseChildUiSections(Element parentElement,
         String ... sectionElements)
   {
      ManagedList childUiSections = null;

      if ((null == sectionElements) || (0 == sectionElements.length))
      {
         sectionElements = new String[] {
               E_MENU_SECTION, E_LAUNCH_PANEL, E_TOOLBAR_SECTION, E_VIEW};
      }

      List<Element> childUiSectionElements = getChildElementsByTagName(parentElement,
            sectionElements);

      if ((null != childUiSectionElements) && (0 < childUiSectionElements.size()))
      {
         childUiSections = new ManagedList(childUiSectionElements.size());
         for (int i = 0; i < childUiSectionElements.size(); ++i)
         {
            Element childUiSectionElement = childUiSectionElements.get(i);

            BeanDefinitionBuilder child = parseUiSection(childUiSectionElement);

            childUiSections.add(child.getBeanDefinition());
         }
      }

      return childUiSections;
   }

   @SuppressWarnings("unchecked")
   public static ManagedList parseChildUiExtensions(Element parentElement)
   {
      ManagedList uiExtensions = null;

      List<Element> childElements = getChildElementsByTagName(parentElement,
            new String[] {
                  E_MENU_EXTENSION, E_LAUNCHPAD_EXTENSION, E_TOOLBAR_EXTENSION,
                  E_VIEWS_EXTENSION});

      if ((null != childElements) && (0 < childElements.size()))
      {
         uiExtensions = new ManagedList(childElements.size());
         for (int i = 0; i < childElements.size(); ++i)
         {
            Element childElement = childElements.get(i);

            BeanDefinitionBuilder child = parseUiExtension(childElement);

            uiExtensions.add(child.getBeanDefinition());
         }
      }

      return uiExtensions;
   }

   @SuppressWarnings("unchecked")
   public static ManagedList parseChildToolbarButtons(Element parentElement)
   {
      ManagedList childButtons = null;

      List<Element> childUiSectionElements = getChildElementsByTagName(parentElement,
            new String[] {E_TOOLBAR_BUTTON});

      if ((null != childUiSectionElements) && (0 < childUiSectionElements.size()))
      {
         childButtons = new ManagedList(childUiSectionElements.size());
         for (int i = 0; i < childUiSectionElements.size(); ++i)
         {
            Element childButtonElement = childUiSectionElements.get(i);

            BeanDefinitionBuilder child = parseToolbarButton(childButtonElement);

            childButtons.add(child.getBeanDefinition());
         }
      }

      return childButtons;
   }

   public static Object parseMessageSource(Element element)
   {
      BeanDefinitionBuilder msgSourceBeanDefinition = genericBeanDefinition(ResourceBundleMessageSource.class);
      msgSourceBeanDefinition.addPropertyValue("basenames", element
            .getAttribute(A_MESSAGE_BUNDLES));
      msgSourceBeanDefinition.addPropertyValue("useCodeAsDefaultMessage", false);

      BeanDefinitionBuilder adapterBeanDefinition = genericBeanDefinition(SpringMessageSource.class);
      adapterBeanDefinition.addConstructorArgValue(msgSourceBeanDefinition
            .getBeanDefinition());

      return adapterBeanDefinition.getBeanDefinition();
   }
}
