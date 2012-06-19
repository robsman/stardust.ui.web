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
package org.eclipse.stardust.ui.web.viewscommon.core;

public class UserDefinedException extends Exception
{

   public String message = "";

   private String others = null;

   public UserDefinedException(String errorMsg)
   {
      this.message = errorMsg;
   }

   public UserDefinedException(String errorMsg, String others)
   {
      this.message = errorMsg;
      this.others = others;
   }

   public String getMessage()
   {
      if (others == null)
      {
         return message + " " + "cannot be Empty";
      }
      else if (others.length() > 0)
      {
         return message + " " + others;
      }
      else
      {
         return message;
      }

   }
}
