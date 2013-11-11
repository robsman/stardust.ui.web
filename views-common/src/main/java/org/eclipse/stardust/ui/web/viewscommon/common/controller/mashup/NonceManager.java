package org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup;

import static java.lang.System.currentTimeMillis;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

public class NonceManager
{
   // every minute
   private static final long GC_INTERVAL = 1 * 60 * 1000;

   private final Random nonceValueGenerator;

   private final long maxValidity;

   private final ConcurrentHashMap<String, Nonce> nonceRegistry = new ConcurrentHashMap<String, Nonce>();

   private final AtomicLong nextScheduledGc = new AtomicLong(currentTimeMillis()
         + GC_INTERVAL);

   private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<NonceManager.LifecycleListener>();

   public NonceManager(long maxValidity)
   {
      this.maxValidity = maxValidity;
      this.nonceValueGenerator = new Random();
   }

   public NonceManager(long maxValidity, long seed)
   {
      this(maxValidity);

      nonceValueGenerator.setSeed(seed);
   }

   public void registerLifecycleListener(LifecycleListener listener)
   {
      synchronized (lifecycleListeners)
      {
         if (!lifecycleListeners.contains(listener))
         {
            lifecycleListeners.add(listener);
         }
      }
   }

   public void removeLifecycleListener(LifecycleListener listener)
   {
      synchronized (lifecycleListeners)
      {
         lifecycleListeners.remove(listener);
      }
   }

   public Nonce obtainNonce()
   {
      try
      {
         Nonce nonce;
         do
         {
            nonce = new Nonce(UUID.randomUUID().toString(), //
                  currentTimeMillis() + maxValidity);
         }
         while (null != nonceRegistry.putIfAbsent(nonce.getValue(), nonce));
         return nonce;
      }
      finally
      {
         nonceGc();
      }
   }

   public boolean isValidNonce(String nonceValue)
   {
      try
      {
         Nonce nonce = nonceRegistry.get(nonceValue);
         if ((null != nonce) && nonce.getValue().equals(nonceValue))
         {
            return (!nonce.wasUsed() && (nonce.getExpiry() >= currentTimeMillis()));
         }
         return false;
      }
      finally
      {
         nonceGc();
      }
   }

   public boolean consumeNonce(String nonceValue)
   {
      try
      {
         Nonce nonce = nonceRegistry.get(nonceValue);
         if ((null != nonce) && nonce.getValue().equals(nonceValue))
         {
            boolean wasUsed = nonce.use();
            if (wasUsed)
            {
               for (LifecycleListener listener : lifecycleListeners)
               {
                  listener.wasUsed(nonce);;
               }
            }
            return wasUsed;
         }

         return false;
      }
      finally
      {
         nonceGc();
      }
   }

   private void nonceGc()
   {
      long gcSchedule = nextScheduledGc.get();
      if (gcSchedule < currentTimeMillis())
      {
         if (nextScheduledGc.compareAndSet(gcSchedule, gcSchedule + GC_INTERVAL))
         {
            // owning the GC duties
            List<Nonce> candidates = new ArrayList<Nonce>(nonceRegistry.values());

            for (Nonce nonce : candidates)
            {
               // remove expired nonces
               if (nonce.getExpiry() < gcSchedule)
               {
                  for (LifecycleListener listener : lifecycleListeners)
                  {
                     listener.wasDestroyed(nonce);
                  }

                  nonceRegistry.remove(nonce.getValue(), nonce);
               }
            }
         }
      }
   }

   public static abstract class LifecycleListener
   {
      void wasUsed(Nonce nonce)
      {
      }

      void wasDestroyed(Nonce nonce)
      {
      }
   }
}
