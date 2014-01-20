package org.eclipse.stardust.ui.web.modeler.common;

public class ConflictingRequestException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public ConflictingRequestException()
   {
   }

   public ConflictingRequestException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ConflictingRequestException(String message)
   {
      super(message);
   }

   public ConflictingRequestException(Throwable cause)
   {
      super(cause);
   }
}
