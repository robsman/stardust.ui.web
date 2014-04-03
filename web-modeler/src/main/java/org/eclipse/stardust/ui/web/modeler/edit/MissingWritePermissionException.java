package org.eclipse.stardust.ui.web.modeler.edit;

public class MissingWritePermissionException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   public MissingWritePermissionException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public MissingWritePermissionException(String message)
   {
      super(message);
   }
}
