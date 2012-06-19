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

import java.util.List;

import org.springframework.beans.factory.FactoryBean;

/**
 * @author Robert.Sauer
 * @version $Revision: $
 */
public abstract class HierarchyFactoryBean<P, E> implements FactoryBean
{
   public static final String PRP_PARENT = "parent";

   public static final String PRP_ELEMENTS = "elements";

   protected P parent;

   protected List<E> elements;

   public void setParent(P parent)
   {
      this.parent = parent;
   }

   public void setElements(List<E> elements)
   {
      this.elements = elements;
   }

   public abstract Class<P> getObjectType();
   
   public abstract void applyElement(E element) throws Exception;

   public P getObject() throws Exception
   {
      if (this.elements != null && this.elements.size() > 0)
      {
         for (E element : elements)
         {
            applyElement(element);
         }
      }
      return this.parent;
   }
   
   public boolean isSingleton()
   {
      return true;
   }
}
