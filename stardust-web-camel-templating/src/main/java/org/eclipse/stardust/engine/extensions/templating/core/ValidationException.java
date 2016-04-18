package org.eclipse.stardust.engine.extensions.templating.core;

public class ValidationException extends RuntimeException
{

   /**
    * 
    */
   private static final long serialVersionUID = 8654525327670278367L;

   private int statusCode;

   private String message;

   public ValidationException()
   {}

   public ValidationException(int statusCode, String message)
   {
      super();
      this.statusCode = statusCode;
      this.message = message;
   }

   public int getStatusCode()
   {
      return statusCode;
   }

   public void setStatusCode(int statusCode)
   {
      this.statusCode = statusCode;
   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

}
