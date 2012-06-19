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
 * An iterator wrapping another iterator and transforming it's contents according to
 * the provided transforming functor.
 *
 * @author ubirkemeyer
 * @version $Revision: 31058 $
 */
public class TransformingIterator<S, T> implements Iterator<T>
{
   private Iterator<? extends S> source;
   private Functor<S, T> transformer;

   public TransformingIterator(Iterator<? extends S> source, Functor<S, T> transformer, Predicate<S> filter)
   {
      if (filter != null)
      {
         this.source = new FilteringIterator<S>(source, filter);
      }
      else
      {
         this.source = source;
      }
      this.transformer = transformer;
   }

   public TransformingIterator(Iterator<? extends S> source, Functor<S, T> transformer)
   {
      this(source, transformer, null);
   }

   public boolean hasNext()
   {
      return source.hasNext();
   }

   public T next()
   {
      return transformer.execute(source.next());
   }

   public void remove()
   {
      source.remove();
   }
}
