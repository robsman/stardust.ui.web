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
package org.eclipse.stardust.ui.web.viewscommon.common;

public class PortalException extends Exception
{
   private final static String DETAIL_DELIM = " - ";

   private PortalErrorClass errorClass;

   public PortalException(PortalErrorClass errorClass, Throwable exception)
   {
      super(exception);
      this.errorClass = errorClass;
   }

   public PortalException(PortalErrorClass errorClass)
   {
      this(errorClass, null);
   }

   public PortalErrorClass getErrorClass()
   {
      return errorClass;
   }
}
