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
package org.eclipse.stardust.ui.web.viewscommon.common.exceptions;

/**
 * @author Yogesh.Manware
 * 
 */
public class I18NException extends RuntimeException
{
   private static final long serialVersionUID = 2582781808303866758L;
   private String message; // This message must be a I18n msg

   /**
    * @param message
    */
   public I18NException(String message)
   {
      this.message = message;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Throwable#getMessage()
    */
   public String getMessage()
   {
      return message;
   }
}
