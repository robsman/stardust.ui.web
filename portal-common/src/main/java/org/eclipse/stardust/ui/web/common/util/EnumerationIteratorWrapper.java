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
package org.eclipse.stardust.ui.web.common.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * Wrapper to allow conversion between iterators and enumerations.
 */
public class EnumerationIteratorWrapper<E> implements Iterator<E>, Enumeration<E>
{
   /**
    *
    */
   private Iterator<E> srcItr;
   /**
    *
    */
   private Enumeration<E> srcEnum;

   /**
    *
    */
   public EnumerationIteratorWrapper(Enumeration<E> srcEnum)
   {
      this.srcEnum = srcEnum;
      this.srcItr = null;
   }

   /**
    *
    */
   public EnumerationIteratorWrapper(Iterator<E> srcItr)
   {
      this.srcItr = srcItr;
      this.srcEnum = null;
   }

   /**
    *
    */
   public E next()
   {
      if (srcItr != null)
      {
         return srcItr.next();
      }

      return srcEnum.nextElement();
   }

   /**
    *
    */
   public boolean hasNext()
   {
      if (srcItr != null)
      {
         return srcItr.hasNext();
      }

      return srcEnum.hasMoreElements();
   }

   /**
    *
    */
   public E nextElement()
   {
      if (srcItr != null)
      {
         return srcItr.next();
      }

      return srcEnum.nextElement();
   }

   /**
    *
    */
   public boolean hasMoreElements()
   {
      if (srcItr != null)
      {
         return srcItr.hasNext();
      }

      return srcEnum.hasMoreElements();
   }

   /**
    *
    */
   public void remove()
   {
   }
}
