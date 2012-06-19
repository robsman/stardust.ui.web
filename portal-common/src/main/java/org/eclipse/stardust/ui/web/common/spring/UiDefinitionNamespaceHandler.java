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

import static org.eclipse.stardust.ui.web.common.spring.UiDefinitionTokens.E_PERSPECTIVE;
import static org.eclipse.stardust.ui.web.common.spring.UiDefinitionTokens.E_PERSPECTIVE_EXT;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class UiDefinitionNamespaceHandler extends NamespaceHandlerSupport
{

   public void init()
   {
      registerBeanDefinitionParser(E_PERSPECTIVE, new PerspectiveBeanDefinitionParser());
      registerBeanDefinitionParser(E_PERSPECTIVE_EXT, new PerspectiveExtensionBeanDefinitionParser());
   }

}
