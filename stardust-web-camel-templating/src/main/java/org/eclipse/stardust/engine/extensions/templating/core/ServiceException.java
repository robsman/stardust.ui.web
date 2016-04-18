package org.eclipse.stardust.engine.extensions.templating.core;

public class ServiceException extends Exception
{
   private static final long serialVersionUID = -835805739085799743L;

   public ServiceException()
   {
      super();
   }

   public ServiceException(String message, Throwable cause)
   {
      super(message, cause);
   }

   public ServiceException(String message)
   {
      super(message);
   }

   public ServiceException(Throwable cause)
   {
      super(cause);
   }

}
