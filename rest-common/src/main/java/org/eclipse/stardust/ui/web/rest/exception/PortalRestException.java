/*******************************************************************************
 * Copyright (c) 2014 SunGard CSA LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SunGard CSA LLC - initial API and implementation and/or initial documentation
 *******************************************************************************/

package org.eclipse.stardust.ui.web.rest.exception;

import java.io.Serializable;
import java.util.Locale;

import javax.ws.rs.core.Response.Status;

/**
 *
 * @author Yogesh.Manware
 *
 */

public class PortalRestException extends RuntimeException implements Serializable
{
   private static final long serialVersionUID = 1L;

   private PortalErrorClass portalErrorClass;

   public PortalRestException()
   {
      super();
   }

   /**
    * @param msg
    */
   public PortalRestException(PortalErrorClass portalErrorClass)
   {
      super();
      this.portalErrorClass = portalErrorClass;
   }

   /**
    * @param e
    */
   public PortalRestException(PortalErrorClass portalErrorClass, Exception e)
   {
      super(e);
      this.portalErrorClass = portalErrorClass;
   }

   /**
    * @param e
    */
   public PortalRestException(Status status, String i18nMessage)
   {
      super();
      this.portalErrorClass = new PortalErrorClass(status, i18nMessage);
   }

   public PortalRestException(Status status, String i18nMessage, Exception e)
   {
      super(e);
      this.portalErrorClass = new PortalErrorClass(status, i18nMessage);
   }

   public String getMessage(Locale locale)
   {
      return portalErrorClass.getLocalizedMessage(locale);
   }

   public Status getHttpStatus()
   {
      return portalErrorClass.getHttpStatus();
   }
}