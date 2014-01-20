package org.eclipse.stardust.ui.web.modeler.common;

public class ItemNotFoundException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public ItemNotFoundException()
   {
      super();
   }

   public ItemNotFoundException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ItemNotFoundException(String message)
   {
      super(message);
   }

   public ItemNotFoundException(Throwable cause)
   {
      super(cause);
   }

}
