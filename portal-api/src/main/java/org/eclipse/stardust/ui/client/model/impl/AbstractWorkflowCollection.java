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
package org.eclipse.stardust.ui.client.model.impl;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.eclipse.stardust.ui.client.event.Observers;
import org.eclipse.stardust.ui.client.event.StatusEvent;
import org.eclipse.stardust.ui.client.event.StatusEventObserver;
import org.eclipse.stardust.ui.client.model.WorkflowCollection;


public abstract class AbstractWorkflowCollection<E> extends AbstractCollection<E> implements WorkflowCollection<E>
{
   protected volatile E[] items = null;
   
   protected Observers<StatusEventObserver> observers = new Observers<StatusEventObserver>();

   protected boolean closed;

   public void close()
   {
      if (!closed)
      {
         // TODO: close the items
         closed = true;
         observers.notifyObservers(StatusEvent.closed(this));
      }
   }

   public void addStatusListener(StatusEventObserver observer)
   {
      assert !closed;

      observers.add(observer);
   }

   public void removeStatusListener(StatusEventObserver observer)
   {
      observers.remove(observer);
   }

   @Override
   public int size()
   {
      assert !closed;

      return items == null ? 0 : items.length;
   }

   @Override
   public Iterator<E> iterator()
   {
      assert !closed;

      return new Iterator<E>()
      {
         private E[] clone = items;
         int counter = 0;
         
         public boolean hasNext()
         {
            assert !closed;

            return clone != null && counter < clone.length;
         }

         public E next()
         {
            assert !closed;

            if (clone == null)
            {
               throw new NoSuchElementException();
            }
            
            return clone[counter++];
         }

         public void remove()
         {
            assert !closed;

            throw new UnsupportedOperationException();
         }
      };
   }
   
   /**
    * Remove is not supported.
    */
   @Override
   public boolean remove(Object o)
   {
      assert !closed;

      throw new UnsupportedOperationException();
   }

   public abstract void update();
}
