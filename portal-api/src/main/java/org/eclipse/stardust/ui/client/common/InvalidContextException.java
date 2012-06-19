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
package org.eclipse.stardust.ui.client.common;

public class InvalidContextException extends RuntimeException
{
   private static final long serialVersionUID = 1L;

   private String clientType;

   public InvalidContextException(String message, String clientType)
   {
      super(message);
      this.clientType = clientType;
   }

   public String getClientType()
   {
      return clientType;
   }
}
