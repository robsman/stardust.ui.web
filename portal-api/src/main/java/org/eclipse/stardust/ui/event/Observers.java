/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.stardust.ui.event;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a thread safe list that is designed for storing lists of observers. The
 * implementation is optimized for minimal memory footprint, frequent reads and infrequent
 * writes. Modification of the list is synchronized and relatively expensive, while
 * accessing the observers is very fast. Readers are given access to the underlying array
 * data structure for reading, with the trust that they will not modify the underlying
 * array.
 * <p>
 * <a name="same">A listener list handles the <i>same</i> listener being added multiple
 * times, and tolerates removal of observers that are the same as other observers in the
 * list. For this purpose, observers can be compared with each other using either equality
 * or identity, as specified in the list constructor.
 * </p>
 * <p>
 * This class can be used without OSGi running.
 * </p>
 * 
 * @since org.eclipse.equinox.common 3.2
 * 
 * @see org.eclipse.core.runtime.ListenerList
 */
public class Observers<O extends AbstractEventObserver>
{
   /**
    * Mode constant (value 0) indicating that observers should be considered the <a
    * href="#same">same</a> if they are equal.
    */
   public static final int EQUALITY = 0;

   /**
    * Mode constant (value 1) indicating that observers should be considered the <a
    * href="#same">same</a> if they are identical.
    */
   public static final int IDENTITY = 1;

   /**
    * Indicates the comparison mode used to determine if two observers are equivalent
    */
   private final boolean identity;

   /**
    * The list of observers. Initially empty but initialized to an array of size capacity
    * the first time a listener is added. Maintains invariant: observers != null
    */
   private volatile List<O> observers = emptyList();

   /**
    * Creates a listener list in which observers are compared using equality.
    */
   public Observers()
   {
      this(EQUALITY);
   }

   /**
    * Creates a listener list using the provided comparison mode.
    * 
    * @param mode
    *           The mode used to determine if observers are the <a href="#same">same</a>.
    */
   public Observers(int mode)
   {
      if (mode != EQUALITY && mode != IDENTITY)
      {
         throw new IllegalArgumentException();
      }

      this.identity = mode == IDENTITY;
   }
   
   public List<O> asList()
   {
      // this list is always unmodifiable, so it's safe to expose it
      return observers;
   }

   /**
    * Adds a listener to this list. This method has no effect if the <a
    * href="#same">same</a> listener is already registered.
    * 
    * @param observer
    *           the non-<code>null</code> listener to add
    */
   public synchronized boolean add(O observer)
   {
      // This method is synchronized to protect against multiple threads adding
      // or removing observers concurrently. This does not block concurrent readers.
      if (observer == null)
      {
         throw new IllegalArgumentException();
      }

      // check for duplicates
      final int oldSize = observers.size();
      for (int i = 0; i < oldSize; ++i)
      {
         Object observer2 = observers.get(i);
         if (identity ? observer == observer2 : observer.equals(observer2))
         {
            return false;
         }
      }
      // Thread safety: create new array to avoid affecting concurrent readers
      List<O> newObservers = new ArrayList<O>(oldSize + 1);
      newObservers.addAll(observers);
      newObservers.add(observer);
      // atomic assignment
      this.observers = unmodifiableList(newObservers);
      
      return true;
   }

   /**
    * Returns whether this listener list is empty.
    * 
    * @return <code>true</code> if there are no registered observers, and
    *         <code>false</code> otherwise
    */
   public boolean isEmpty()
   {
      return observers.isEmpty();
   }

   /**
    * Removes a listener from this list. Has no effect if the <a href="#same">same</a>
    * listener was not already registered.
    * 
    * @param listener
    *           the non-<code>null</code> listener to remove
    */
   public synchronized boolean remove(Object listener)
   {
      // This method is synchronized to protect against multiple threads adding
      // or removing observers concurrently. This does not block concurrent readers.
      if (listener == null)
      {
         throw new IllegalArgumentException();
      }

      int oldSize = observers.size();
      for (int i = 0; i < oldSize; ++i)
      {
         Object listener2 = observers.get(i);
         if (identity ? listener == listener2 : listener.equals(listener2))
         {
            if (oldSize == 1)
            {
               this.observers = emptyList();
            }
            else
            {
               // Thread safety: create new array to avoid affecting concurrent readers
               List<O> newObservers = new ArrayList<O>(oldSize - 1);
               newObservers.addAll(observers.subList(0, i));
               newObservers.addAll(observers.subList(i + 1, oldSize));
               // atomic assignment to field
               this.observers = unmodifiableList(newObservers);
            }

            return true;
         }
      }
      
      return false;
   }

   public <E extends AbstractEvent<O>> void notifyObservers(AbstractEvent<O> event)
   {
      // copy to be safe against concurrent modifications
      final List<O> observers = this.observers;
      for (int i = 0; i < observers.size(); i++ )
      {
         // TODO: wrap with try catch...
         event.notifyObserver(observers.get(i));
      }
   }

   /**
    * Returns the number of registered observers.
    * 
    * @return the number of registered observers
    */
   public int size()
   {
      return observers.size();
   }

   /**
    * Removes all observers from this list.
    */
   public synchronized void clear()
   {
      this.observers = emptyList();
   }
}
