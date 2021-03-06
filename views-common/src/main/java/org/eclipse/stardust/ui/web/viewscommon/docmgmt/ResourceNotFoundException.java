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
package org.eclipse.stardust.ui.web.viewscommon.docmgmt;

/**
 * @author subodh.godbole
 *
 */
public class ResourceNotFoundException extends Exception
{
   private static final long serialVersionUID = 1L;

   /**
    * 
    */
   public ResourceNotFoundException()
   {
      super();
   }

   /**
    * @param message
    */
   public ResourceNotFoundException(String message)
   {
      super(message);
   }

   /**
    * @param message
    * @param exception
    */
   public ResourceNotFoundException(String message, Throwable exception)
   {
      super(message, exception);
   }

   /**
    * @param exception
    */
   public ResourceNotFoundException(Throwable exception)
   {
      super(exception);
   }
}
