/*******************************************************************************
 * Copyright (c) 2015 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	Yogesh.Manware (SunGard CSA LLC) - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.stardust.ui.web.rest.service.dto.response;

/**
 * @author Yogesh.Manware
 * @version $Revision: $
 */
public class ErrorMessageDTO
{
   private String message;
   private String detailedMessage;
   private String stacktrace;

   public ErrorMessageDTO(String msg)
   {
      this.message = msg;
   }

   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   public String getDetailedMessage()
   {
      return detailedMessage;
   }

   public void setDetailedMessage(String detailedMessage)
   {
      this.detailedMessage = detailedMessage;
   }

   public String getStacktrace()
   {
      return stacktrace;
   }

   public void setStacktrace(String stacktrace)
   {
      this.stacktrace = stacktrace;
   }

}