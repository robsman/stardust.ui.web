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
package org.eclipse.stardust.ui.web.common.reflect;

/**
 * @author Subodh.Godbole
 *
 */
public class ReflectionException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   /**
    * @param message
    * @param e
    */
   public ReflectionException(String message, Throwable e)
   {
      super(message, e);
   }

   /**
    * @param message
    */
   public ReflectionException(String message)
   {
      super(message);
   }

   /**
    * @param e
    */
   public ReflectionException(Throwable e)
   {
      super(e);
   }
}
