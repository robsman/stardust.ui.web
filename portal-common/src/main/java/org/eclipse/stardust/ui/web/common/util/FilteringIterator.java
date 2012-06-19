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

import java.util.Iterator;

/**
 * An Iterator wrapping another Iterator and filtering the values according to the
 * provided predicate.
 *
 * @author ubirkemeyer
 * @version $Revision: 31058 $
 */
public class FilteringIterator<E> implements Iterator<E>
{
   private Iterator<? extends E> source;
   private E current;
   private Predicate<E> predicate;

   public FilteringIterator(Iterator<? extends E> source, Predicate<E> predicate)
   {
      this.source = source;
      if (predicate != null)
      {
         this.predicate = predicate;
      }
      else
      {
         this.predicate = new Predicate<E>()
         {
            public boolean accept(E o)
            {
               return true;
            }
         };
      }
   }

   public boolean hasNext()
   {
      if (current != null)
      {
         return true;
      }
      while (source.hasNext())
      {
         E candidate = source.next();
         if (predicate.accept(candidate))
         {
            current = candidate;
            return true;
         }
      }
      return false;
   }

   public E next()
   {
      if (current != null)
      {
         E result = current;
         current = null;
         return result;
      }
      while (source.hasNext())
      {
         E candidate = source.next();
         if (predicate.accept(candidate))
         {
            return candidate;
         }
      }
      return null;
   }

   public void remove()
   {
      throw new UnsupportedOperationException();
   }
}
