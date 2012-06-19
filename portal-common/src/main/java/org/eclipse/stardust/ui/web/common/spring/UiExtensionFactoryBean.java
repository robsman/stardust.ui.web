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

import org.eclipse.stardust.ui.web.common.UiElement;
import org.eclipse.stardust.ui.web.common.UiExtension;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public class UiExtensionFactoryBean<E extends UiElement, X extends UiExtension<E>>
      extends HierarchyFactoryBean<X, E>
{
   private final Class<X> objectType;

   public UiExtensionFactoryBean(Class<X> objectType)
   {
      this.objectType = objectType;
   }

   public void applyElement(E element) throws Exception
   {
      parent.addElement(element);
   }

   public Class<X> getObjectType()
   {
      return objectType;
   }

}
