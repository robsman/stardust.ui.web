package org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import org.eclipse.stardust.common.log.LogManager;
import org.eclipse.stardust.common.log.Logger;

@Service(MashupContextConfigManager.BEAN_NAME)
@Scope("singleton")
public class MashupContextConfigManager
{
   public static final String BEAN_NAME = "ippMashupContextConfigManager";

   private static final Logger trace = LogManager
         .getLogger(MashupContextConfigManager.class);

   private final ConcurrentMap<String, MashupContext> contextRegistry = new ConcurrentHashMap<String, MashupContext>();

   private final NonceManager nonceManager;

   /**
    * Initializes a new instance (with default TTL of 1 minute).
    *
    * @param maxNonceValidity
    *           the maximum amount of time a context registration will be valid (defaults
    *           to 1 minute)
    * @see #MashupContextConfigManager(long)
    */
   public MashupContextConfigManager()
   {
      // defaults to 1 minute
      this(1 * 60 * 1000);
   }

   /**
    * Initializes a new instance.
    *
    * @param maxTtl
    *           the maximum amount of time (in milliseconds) a context registration will
    *           be valid
    * @see #MashupContextConfigManager()
    */
   public MashupContextConfigManager(long maxTtl)
   {
      this.nonceManager = new NonceManager(maxTtl);

      nonceManager.registerLifecycleListener(new NonceManager.LifecycleListener()
      {
         @Override
         public void wasDestroyed(Nonce nonce)
         {
            if (trace.isDebugEnabled())
            {
               trace.debug("Disposing context config registration " + nonce.getValue());
            }

            // discard associated context
            contextRegistry.remove(nonce.getValue());
         }
      });
   }

   /**
    * Registers a context for later retrieval.
    *
    * @param context
    * @return a unique ID with built-in expiry (means, the ID will be considered invalid
    *         after a certain amount of time)
    * @see #MashupContextConfigManager(long)
    */
   public String registerContext(MashupContext context)
   {
      Nonce nonce;
      do
      {
         nonce = nonceManager.obtainNonce();
      }
      while (null != this.contextRegistry.putIfAbsent(nonce.getValue(), context));

      if (trace.isDebugEnabled())
      {
         trace.debug("Registered context config registration " + nonce.getValue()
               + " for URI " + context.uri);
      }

      return nonce.getValue();
   }

   /**
    *
    * @param contextId
    * @return
    */
   public MashupContext getContext(String contextId)
   {
      if (nonceManager.isValidNonce(contextId))
      {
         return contextRegistry.get(contextId);
      }
      else
      {
         return null;
      }
   }

   public long getContextExpiry(String contextId)
   {
      if (nonceManager.isValidNonce(contextId))
      {
         return nonceManager.getNonceExpiry(contextId);
      }
      else
      {
         return 0;
      }
   }

   public boolean consumeContext(String contextId, MashupContext context)
   {
      if (contextRegistry.get(contextId) == context)
      {
         boolean wasConsumed = nonceManager.consumeNonce(contextId);

         if (wasConsumed && trace.isDebugEnabled())
         {
            trace.debug("Consumed context config registration " + contextId + " for URI "
                  + context.uri);
         }

         return wasConsumed;
      }

      return false;
   }

   public void removeContext(String contextId)
   {
      contextRegistry.remove(contextId);
   }
}
