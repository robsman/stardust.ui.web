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
package org.eclipse.stardust.ui.web.common.event;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.stardust.ui.web.common.event.EventObservers;
import org.eclipse.stardust.ui.web.common.event.UiEventBase;
import org.junit.Test;

/**
 * @author robert.sauer
 * @version $Revision: $
 */
public class EventsApi
{

   @Test
   public void singleObserverMultipleEvents()
   {
      final AtomicInteger nEvents = new AtomicInteger(0);

      E1Observer e1Obs = new E1Observer()
      {
         public void handleEvent(E1 event)
         {
            nEvents.incrementAndGet();
         }
      };

      e1Obs.handleEvent(new E1());

      assertEquals(1, nEvents.get());

      e1Obs.handleEvent(new E1a());

      assertEquals(2, nEvents.get());

      e1Obs.handleEvent(new E1b());

      assertEquals(3, nEvents.get());
   }

   @Test
   public void multipleObserverMultipleEvents()
   {
      final AtomicInteger nE1Events = new AtomicInteger(0);
      final AtomicInteger nE2Events = new AtomicInteger(0);

      ExObserver exObs = new ExObserver()
      {
         public void handleEvent(E1 event)
         {
            nE1Events.incrementAndGet();
         }

         public void handleEvent(E2 event)
         {
            nE2Events.incrementAndGet();
         }
      };

      exObs.handleEvent(new E1());

      assertEquals(1, nE1Events.get());
      assertEquals(0, nE2Events.get());

      exObs.handleEvent(new E1a());

      assertEquals(2, nE1Events.get());
      assertEquals(0, nE2Events.get());

      exObs.handleEvent(new E1b());

      assertEquals(3, nE1Events.get());
      assertEquals(0, nE2Events.get());

      exObs.handleEvent(new E2());

      assertEquals(3, nE1Events.get());
      assertEquals(1, nE2Events.get());

      exObs.handleEvent(new E2a());

      assertEquals(3, nE1Events.get());
      assertEquals(2, nE2Events.get());
   }

   @Test
   public void eventObservers()
   {
      final AtomicInteger nEvents = new AtomicInteger(0);

      EventObservers<E1Observer> obs = new EventObservers<E1Observer>();
      obs.add(new E1Observer()
      {
         public void handleEvent(E1 e)
         {
            nEvents.incrementAndGet();
         }
      });

      obs.notifyObservers(new E1());
      assertEquals(1, nEvents.get());

      obs.notifyObservers(new E1a());
      assertEquals(2, nEvents.get());

      obs.notifyObservers(new E1b());
      assertEquals(3, nEvents.get());
   }

   static class E1 extends UiEventBase<E1Observer>
   {
      @Override
      void notifyObserver(E1Observer observer)
      {
         observer.handleEvent(this);
      }
   }

   static interface E1Observer
   {
      void handleEvent(E1 e);
   }
   
   private static class E1a extends E1
   {
   }

   public static interface E1aObserver
   {
      void handleEvent(E1a e);
   }

   private static class E1b extends E1
   {
   }

   public static interface E1bObserver
   {
      void handleEvent(E1b e);
   }

   private static class E2 extends UiEventBase<E2Observer>
   {
      @Override
      void notifyObserver(E2Observer observer)
      {
         observer.handleEvent(this);
      }
   }

   public static interface E2Observer
   {
      void handleEvent(E2 e);
   }
   
   private static class E2a extends E2
   {
   }

   public static interface E2aObserver
   {
      void handleEvent(E2a e);
   }

   private static abstract class ExObserver implements E1Observer, E2Observer
   {
      public abstract void handleEvent(E1 event);

      public abstract void handleEvent(E2 event);
   }
}
