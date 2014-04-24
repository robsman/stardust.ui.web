package org.eclipse.stardust.ui.web.viewscommon.common.controller.mashup;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

public final class Nonce implements Serializable
{
   private static final long serialVersionUID = 1L;

   private final String value;

   private final long expiry;

   private final AtomicInteger nUses = new AtomicInteger(0);

   public Nonce(String value, long expiry)
   {
      // TODO combine value with expiry to ensure repeated use of the same UUID will yield a different nonce
      this.value = value;
      this.expiry = expiry;
   }

   public String getValue()
   {
      return value;
   }

   public long getExpiry()
   {
      return expiry;
   }

   public boolean use()
   {
      return 1 == nUses.incrementAndGet();
   }

   public boolean wasUsed()
   {
      return 0 < nUses.get();
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (expiry ^ (expiry >>> 32));
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Nonce other = (Nonce) obj;
      if (expiry != other.expiry)
         return false;
      if (value == null)
      {
         if (other.value != null)
            return false;
      }
      else if (!value.equals(other.value))
         return false;
      return true;
   }
}
