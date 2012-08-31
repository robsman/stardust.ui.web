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
package org.eclipse.stardust.ui.web.common.app.messaging;

/**
 * @author Subodh.Godbole
 *
 */
public class MessageProcessingException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   /**
    * 
    */
   public MessageProcessingException()
   {
      super();
   }

   /**
    * @param message
    * @param cause
    */
   public MessageProcessingException(String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * @param message
    */
   public MessageProcessingException(String message)
   {
      super(message);
   }

   /**
    * @param cause
    */
   public MessageProcessingException(Throwable cause)
   {
      super(cause);
   }
}
