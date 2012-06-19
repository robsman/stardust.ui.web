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
import static org.eclipse.stardust.ui.web.common.spring.UiDefinitionParserUtils.parseChildUiSections;
import static org.eclipse.stardust.ui.web.common.spring.UiDefinitionParserUtils.parsePerspective;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class PerspectiveBeanDefinitionParser extends AbstractBeanDefinitionParser
{

   @Override
   protected AbstractBeanDefinition parseInternal(Element element, ParserContext pc)
   {
      BeanDefinitionBuilder factory = rootBeanDefinition(PerspectiveFactoryBean.class);

      BeanDefinitionBuilder parent = parsePerspective(element);
      factory.addPropertyValue(PRP_PARENT, parent.getBeanDefinition());

      ManagedList childUiSections = parseChildUiSections(element);
      if (null != childUiSections)
      {
         factory.addPropertyValue(PRP_ELEMENTS, childUiSections);
      }

      return factory.getBeanDefinition();
   }

}
